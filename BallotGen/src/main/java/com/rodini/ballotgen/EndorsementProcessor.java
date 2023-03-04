package com.rodini.ballotgen;
/**
 * EndorsementProcessor processes the endorsements for a candidate.
 * 
 * Endorsements are quite different between Primary and General elections.
 * In a primary election, a candidate can be endorsed at any of these levels:
 * - State
 * - County
 * - Zone
 * Primary endorsements are the results of endorsement conventions. A candidate
 * must be endorsed at some level in order to get the darkened circle next
 * to his / her name.
 * Note: A single endorsement file is used to assign endorsements.
 * 
 * In a general election, a candidate is endorse by their party (e.g. Democratic).
 * Note: See the "endorsed.party" property that is part of the program's properties file.
 * 
 * There is still some ambiguity regarding certain offices where the candidate
 * is allowed to cross-file.  The ballot party label here will be Democratic/Republican.
 * In this case an endorsement file is used so that only Democrats who cross-file
 * are endorsed.
 * 
 * @author Bob Rodini
 *
 */

import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.rodini.zoneprocessor.GenMuniMap;
import com.rodini.zoneprocessor.Zone;
import static com.rodini.ballotgen.ElectionType.*;
import static com.rodini.ballotgen.EndorsementType.*;

public class EndorsementProcessor {
	private static final Logger logger = LoggerFactory.getLogger(EndorsementProcessor.class);

	// Candidate name (key) List of Endorsements (value)
	// SAMANTHA JOUIN       End1, End2, ...
	private static Map<String,List<Endorsement>> candidateEndorsements;
	
	// Precinct No (key)     Zone (value)
	// 020                   7         Zone # (String here)
	//                       Bradford  Zone name
	private static Map<String, Zone> precinctToZone;
	private static ElectionType elecType;
	private static Party endorsedParty;
/**
 * 
 * EndorsementProcessor processes the data needed into the data structures
 * needed to perform endorsements.
 * 
 * @param elecType Primary or General
 * @param endorsementsCSVText text of endorsements.csv
 * @param precinctZoneCSVText text of precinct-zones.csv
 */
	public EndorsementProcessor(ElectionType elecType, Party endorsedParty,
			Map<String,List<Endorsement>> candidateEndorsements,
			Map<String, Zone> precinctToZone) {
		EndorsementProcessor.candidateEndorsements = candidateEndorsements;
		EndorsementProcessor.precinctToZone = precinctToZone;
		EndorsementProcessor.elecType = elecType;
		EndorsementProcessor.endorsedParty = endorsedParty;
	}
	public EndorsementProcessor(ElectionType elecType, Party endorsedParty,
			String endorsementsCSVText, String precinctZoneCSVText) {
		// process the endorsementsText (CSV lines) into entries of candidateEndorsers
		EndorsementFactory.processCSVText(endorsementsCSVText);
		candidateEndorsements = EndorsementFactory.getCandidateEndorsements();
		// tell ZoneProcessor to process the zoneText (CSV lines) into the muniNoToZone map.
		GenMuniMap.processCSVText(precinctZoneCSVText);
		precinctToZone = GenMuniMap.getMuniNoMap();
		EndorsementProcessor.elecType = elecType;
		EndorsementProcessor.endorsedParty = endorsedParty;
	}
	/**
	 * isEnsorsed determines if the candidate on the muniNo (precinct) ballot should be endorsed
	 * See the general rules for endorsement in the class documentation.
	 * 
	 * @param candidateName name of candidate on ballot (Spelling is exact, but not case-sensitive).
	 * @param contestName name of office candidate is seeking (NOT PRESENTLY USED).
	 * @param candidateParty candidate's party on ballot.
	 * @param muniNo precinct # of ballot on which candidate appears.
	 * @return
	 */
	public boolean isEndorsed(String candidateName, String contestName, Party candidateParty, String muniNoStr) {
		boolean endorsed = false;
		if (elecType == GENERAL) {
			endorsed = isEndorsedForGeneral(candidateName, contestName, candidateParty, muniNoStr);
		} else {
			endorsed = isEndorsedByEndorsement(candidateName, contestName, muniNoStr);
		}
		return endorsed;
	}
	
	public Map<String,List<Endorsement>> getCandidateEndorsements() {
		return candidateEndorsements;
	}
	
	private boolean isEndorsedForGeneral(String candidateName, String contestName, Party candidateParty, String muniNoStr) {
		boolean endorsed = false;
		if (candidateParty == endorsedParty) {
			endorsed = true;
			logger.info(String.format("endorsed by favored party: %s%n", candidateName));
		} else {
			endorsed = isEndorsedByEndorsement(candidateName, contestName, muniNoStr);
		}
		return endorsed;
	}

	private boolean isEndorsedByEndorsement(String candidateName,String contestName, String muniNoStr) {
		boolean endorsed = false;
		// Attention: key is upper case to avoid case-sensitivity;
		candidateName = candidateName.toUpperCase();
		List<Endorsement> endorsements = candidateEndorsements.get(candidateName);
		if (endorsements != null) {
			for (Endorsement end: endorsements) {
				EndorsementType type = end.getType();
				if (type == STATE || type == COUNTY) {
					endorsed = true;
					break;
				} else { // type == ZONE
					int zoneNo = end.getZoneNo();
					// Get the Zone that owns the precinct
					Zone zone = precinctToZone.get(muniNoStr);
					// Did the zone endorse this candidate?
					if (zoneNo == Integer.parseInt(zone.getZoneNo())) {
						endorsed = true;
						break;
					}
				}
			}
		}
		if (endorsed) {
			logger.info(String.format("endorsed by endorsement: %s%n", candidateName));
		}
		return endorsed;
	}

}
