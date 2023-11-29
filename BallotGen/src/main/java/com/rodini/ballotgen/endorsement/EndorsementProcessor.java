package com.rodini.ballotgen.endorsement;
/**
 * EndorsementProcessor processes the endorsements for a candidate.
 * A candidate can be explicitly ENDORSED, UNENDORSED, or ANTIENDORSED.
 * Each of these "modes" result in explicit styling of the candidates name
 * on the sample ballot. Note that ANTIENDORSED is the default and is rarely
 * used in the endorsements CSV file.
 * 
 * Endorsements are quite different between Primary and General elections.
 * In a primary election, a candidate can be endorsed at any of these levels:
 * - State
 * - County
 * - Zone
 * Primary endorsements are the results of endorsement conventions. A candidate
 * must be endorsed at some level in order to get the darkened circle next
 * to his / her name.
 * 
 * In a general election, a candidate is endorse by their party (e.g. Democratic).
 * Note: See the "endorsed.party" property that is part of the program's properties file.
 * 
 * There is still some ambiguity regarding certain offices where the candidate
 * is allowed to cross-file.  The ballot party label here will be Democratic/Republican.
 * In this case an endorsement file is used so that only Democrats and not
 * Republicans who cross-file are endorsed.
 * 
 * @author Bob Rodini
 *
 */

import java.util.List;
import java.util.Map;

//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.rodini.ballotgen.common.ElectionType;
import com.rodini.ballotgen.common.Party;
import com.rodini.zoneprocessor.ZoneProcessor;
import com.rodini.zoneprocessor.Zone;

import static com.rodini.ballotgen.common.ElectionType.*;
import static com.rodini.ballotgen.endorsement.EndorsementMode.*;
import static com.rodini.ballotgen.endorsement.EndorsementScope.*;

public class EndorsementProcessor {
	private static final Logger logger = LogManager.getLogger(EndorsementProcessor.class);

	// Candidate name (key) List of Endorsements (value)
	// SAMANTHA JOUIN       End1, End2, ...
	private Map<String,List<Endorsement>> candidateEndorsements;
	
	// Precinct No (key)     Zone (value)
	// 020                   07        Zone # (String here)
	//                       Bradford  Zone name
	private Map<String, Zone> precinctToZone;
	private ElectionType elecType;
	private Party endorsedParty;
/**
 * 
 * EndorsementProcessor processes the endorsements file data into data structures
 * needed to perform endorsements.
 * 
 * @param elecType Primary or General.
 * @param endorsedParty party that receives default endorsements.
 * @param candidateEndorsements list of endorsements for candidate.
 * @param precinctToZone precinct to zone map.
 */
	public EndorsementProcessor(ElectionType elecType, Party endorsedParty,
			Map<String,List<Endorsement>> candidateEndorsements,
			Map<String, Zone> precinctToZone) {
		this.candidateEndorsements = candidateEndorsements;
		this.precinctToZone = precinctToZone;
		this.elecType = elecType;
		this.endorsedParty = endorsedParty;
	}
	// constructor for testing only
	public EndorsementProcessor(ElectionType elecType, Party endorsedParty,
			String endorsementsCSVText, String precinctZoneCSVText) {
		// process the endorsementsText (CSV lines) into entries of candidateEndorsers
		EndorsementFactory.processCSVText(endorsementsCSVText);
		candidateEndorsements = EndorsementFactory.getCandidateEndorsements();
		// tell ZoneProcessor to process the zoneText (CSV lines) into the muniNoToZone map.
		ZoneProcessor.processCSVText(precinctZoneCSVText);
		precinctToZone = ZoneProcessor.getPrecinctZoneMap();
		this.elecType = elecType;
		this.endorsedParty = endorsedParty;
	}
	/**
	 * getEndorsementMode determines if the candidate on the muniNo (precinct) ballot should be endorsed.
	 * See the general rules for endorsement in the class documentation.
	 * 
	 * @param candidateName name of candidate on ballot (Spelling is exact, but not case-sensitive).
	 * @param contestName name of office candidate is seeking (NOT PRESENTLY USED).
	 * @param candidateParty candidate's party on ballot.
	 * @param muniNo precinct # of ballot on which candidate appears.
	 * @return EndorsementMode value.
	 */
	public EndorsementMode getEndorsementMode(String candidateName, String contestName, Party candidateParty, String muniNoStr) {
		// Set the default value.
		EndorsementMode mode = ANTIENDORSED;
		if (elecType == GENERAL) {
			mode = getEndorsementModeForGeneral(candidateName, contestName, candidateParty, muniNoStr);
		} else {
			mode = getEndorsmentModeByEndorsement(candidateName, contestName, muniNoStr);
		}
		return mode;
	}
	// referendum questions and retention questions.
	public EndorsementMode getEndorsementMode(String question, String muniNoStr) {
		logger.debug(String.format("get ref endorsement for %s%n precinct #%s", question, muniNoStr));
		EndorsementMode mode = getEndorsmentModeByEndorsement(question, "", muniNoStr);
		return mode;
	}
	
	/**
	 * getCandidateEndorsements return the list of candidates with 
	 * @return
	 */
	public Map<String,List<Endorsement>> getCandidateEndorsements() {
		return candidateEndorsements;
	}
	/**
	 * getEndorsementModeForGeneral return the endorsement mode for candidate in a General election.
	 * 
	 * @param candidateName candidate name.
	 * @param contestName contest name.
	 * @param candidateParty candidate's party.
	 * @param muniNoStr precinct with contest.
	 * @return EndorsementMode value.
	 */
	private EndorsementMode getEndorsementModeForGeneral(String candidateName, String contestName, Party candidateParty, String muniNoStr) {
		// Set the default value.
		EndorsementMode mode = ANTIENDORSED;
		if (candidateParty == endorsedParty) {
			mode = ENDORSED;
			logger.info(String.format("endorsed by favored party: %s%n", candidateName));
		} else {
			mode = getEndorsmentModeByEndorsement(candidateName, contestName, muniNoStr);
		}
		return mode;
	}
	/**
	 * getEndorsementModeByEndorsement return the endorsement mode for candidate based on explicit endorsement.
	 * 
	 * @param candidateName candidate name.
	 * @param contestName contest name.
	 * @param muniNoStr precinct with contest.
	 * @return EndorsementMode value.
	 */
	private EndorsementMode getEndorsmentModeByEndorsement(String candidateName,String contestName, String muniNoStr) {
		// Set the default value.
		EndorsementMode mode = ANTIENDORSED;
		// Attention: key is upper case to avoid case-sensitivity;
		String candidateName1 = candidateName.toUpperCase();
		List<Endorsement> endorsements = candidateEndorsements.get(candidateName1);
		if (endorsements != null) {
			for (Endorsement end: endorsements) {
				EndorsementScope scope = end.getScope();
				if (scope == STATE || scope == COUNTY) {
					mode = end.getMode();
					break;
				} else { // type == ZONE
					int zoneNo = end.getZoneNo();
					// Get the Zone that owns the precinct
					Zone zone = precinctToZone.get(muniNoStr);
					// Did the zone endorse this candidate?
					if (zoneNo == Integer.parseInt(zone.getZoneNo())) {
						mode = end.getMode();
						break;
					}
				}
			}
		}
		logger.info(String.format("%s has endorsement mode: %s", candidateName, mode.toString()));
		return mode;
	}

}
