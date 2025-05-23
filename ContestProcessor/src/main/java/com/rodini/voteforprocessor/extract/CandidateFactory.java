package com.rodini.voteforprocessor.extract;

//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.rodini.ballotutils.ElectionType;
import com.rodini.ballotutils.Party;
import com.rodini.ballotutils.Utils;
import com.rodini.voteforprocessor.model.Candidate;
import com.rodini.voteforprocessor.model.GeneralCandidate;
import com.rodini.voteforprocessor.model.PrimaryCandidate;

import static com.rodini.ballotutils.ElectionType.*;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


/**
 * CandidateFactory creates Candidate objects.  This is tricky because the regexes that
 * extract the candidate lines do not know how to distinguish the lines that are names
 * and the lines that are attributes following the name. And those contests that are
 * "tickets" are especially tricky.
 * 
 * The type of election (general or primary) and the names of the contests are used to
 * disambiguate the lines.  Unfortunately the contest names are hard-wired in the
 * program at this time.
 * 
 */
public class CandidateFactory {
	private static final Logger logger = LogManager.getLogger(CandidateFactory.class);

	private final String contestName;	 // name of the contest / office
	private final String candidatesText; // text of the regex <candidate> group
	private final ElectionType elecType; // GENERAL or PRIMARY
	private final Party endorsedParty;

	// NOTES: 
	// 1. Unique prefixes of these contest names will do.
	// 2. Formatting such are line breaks (e.g.\n) should be removed by the caller
	
	// See properties file.
//	private static final List<String> namesOfTicketContests = List.of(
//			"GOVERNOR AND LIEUTENANT GOVERNOR",
//			"PRESIDENT AND VICE-PRESIDENT"
//			);
//	private static final List<String> namesOfTicketContests = 
//			Arrays.asList(Utils.getPropValue(ballotGenProps, PROP_TICKET_CONTEST_NAMES).split(","));
	
//	private static final List<String> namesOfLocalContests = List.of(
//			"PRESIDENT OF THE UNITED STATES"   // not exactly local but no region line beneath
//			"AUDITOR",
//			"CONSTABLE",
//			"DEMOCRATIC COUNTY COMMISSIONER",
//			"INSPECTOR OF ELECTIONS",
//			"JUDGE OF ELECTIONS",
//			"MAGISTERIAL DISTRICT JUDGE",
//			"MAYOR",
//			"MEMBER OF COUNCIL",
//			"SCHOOL DIRECTOR",
//			"TOWNSHIP COMMISSIONER",
//			"TOWNSHIP SUPERVISOR",
//			"TOWNSHIP SUPERVISOR AT LARGE",
//			"TOWNSHIP DISTRICT SUPERVISOR",
//			"TAX COLLECTOR",
//			"DISTRICT SUPERVISOR",
//			"COUNCIL",
//			"SUPERVISOR",
//			"BOROUGH COUNCIL"
//			);
//	private static final List<String> namesOfLocalContests = 
//			Arrays.asList(Utils.getPropValue(ballotGenProps, PROP_LOCAL_CONTEST_NAMES).split(","));
	// School district regions below have boundaries that extend beyond Chester County.
	// This results in the candidate name being followed by their county of residence.
	// Yet another anomaly in a primary ballot.
//	private static final List<String> namesOfLocalContestsExceptions = List.of(
//			"AUDITOR GENERAL",
//			"SCHOOL DIRECTOR OCTORARA REGION 1",
//			"SCHOOL DIRECTOR UNIONVILLE CHADDS FORD REGION C",
//			"SCHOOL DIRECTOR TWIN VALLEY REGION 2",
//			"SCHOOL DIRECTOR SPRING FORD REGION 3"
//			);
//	private static final List<String> namesOfLocalContestsExceptions = 
//			Arrays.asList(Utils.getPropValue(ballotGenProps, PROP_LOCAL_CONTEST_EXCEPTION_NAMES).split(","));

	List<Candidate> candidates = new ArrayList<Candidate>();
	/**
	 * Constructor
	 * @param contestName contest that the candidates are vying for.
	 * @param candidatesText candidate text as isolated by regex.
	 * @param type PRIMARY or GENERAL
	 * @param endorsedParty favored party for endorsement.
	 */
	public CandidateFactory(String contestName, String candidatesText, ElectionType type, Party endorsedParty) {
		this.contestName = contestName;
		this.candidatesText = candidatesText;
		this.elecType = type;
		this.endorsedParty = endorsedParty;
		if (candidatesText == null) {
			logger.error("candidateText must not be null");
		}
		if (candidatesText.isBlank()) {
			// candidates returns list of zero length.
			return;
		}
		processCandidatesText();
	}
	/** 
	 * processCandidatesText takes the raw lines isolated by the regex
	 * and converts them to Candidate objects.
	 */
	private void processCandidatesText() {
		if (elecType == PRIMARY) {
			if (isLocalContest(contestName)) {
				// each line is a candidate name.
				createPrimaryCandidates();
			} else {
				// first line is a candidate name, second line is candidate's region.
				createPrimaryRegionalCandidates();
			}
		} else { // elecType == GENERAL
			if (isTicketContest(contestName)) {
				// first line is top of ticket, second line is party, third line is bottom of ticket, fourth line is arbitrary text.
				createGeneralTicketCandidates();
			} else {
				// first line is candidate name, second line is party.
				createGeneralCandidates();
			}
		}
		
	}
	/**
	 * isLocalContest uses the pre-defined list of local contests to determine
	 * if this contest is a local one.
	 * 
	 * Note:
	 * - there is an exception list. This is due to Voter Services listing the county of the candidate's county.
	 *   This happens when a school district spans two counties (e.g. Atglen spans Chester and Lancaster county.
	 * @param contestName office name.
	 * @return true => yes
	 */
	private boolean isLocalContest(String contestName) {
		boolean local = false;
		contestName = normalizeContestName(contestName);
		local = contestNameMatch(contestName, Initialize.namesOfLocalContests) && 
				!contestNameExceptionMatch(contestName, Initialize.namesOfLocalContestsExceptions) ;
		return local;
	}
	/**
	 * isTicketContest uses the pre-defined list oF ticket contests to determine
	 * if this contest is a ticket.
	 * @param contestName office name
	 * @return true => yes
	 */
	private boolean isTicketContest(String contestName) {
		boolean ticket = false;
		contestName = normalizeContestName(contestName);
		ticket = contestNameMatch(contestName, Initialize.namesOfTicketContests);
		return ticket;
	}
	/**
	 *  replace \n used for formatting and remove case-sensitivity.
	 * @param name contest name e.g. Governor and Lieutenant\nGovernor
	 * @return GOVERNOR AND LIEUTENANT GOVERNOR
	 */
	private String normalizeContestName(String name) {
		return name.replaceAll("\\n", " ").toUpperCase();
	}
	/**
	 * contestNameMatch determines if the normalized contest name matches
	 * any of the list of prefix contest names.
	 * 
	 * @param normalName normalized contest name.
	 * @param prefixNames list of contest prefix names.
	 * @return
	 */
	private boolean contestNameMatch(String normalName, List<String> prefixNames)  {
		boolean match = false;
		for (String prefix: prefixNames) {
			if (normalName.startsWith(prefix)) {
				match = true;
				break;
			}
		}
		return match;
	}
	/**
	 * contestNameExceptionMatch determines if the contest name belongs to one
	 * of the names in the exception list.
	 * 
	 * @param normalName normalized contest name.
	 * @param exceptionNames list of exception names.
	 * @return
	 */
	private boolean contestNameExceptionMatch(String normalName, List<String> exceptionNames) {
		boolean match = false;
		for (String excep: exceptionNames) {
			if (normalName.startsWith(excep)) {
				match = true;
				break;
			}
		}
		return match;
	}
	/**
	 * createPrimaryCandidates process the lines into a list
	 * of PrimaryCandidate objects.
	 */
	private void createPrimaryCandidates() {
		// each line is a candidate name.
		String [] lines = candidatesText.split("\n");
		for (String line: lines) {
			createPrimaryCandidate(line, endorsedParty, "");
		}
	}
	/**
	 * createPrimaryRegionalCandidates process the lines into a list
	 * of PrimaryCandidate objects.
	 */
	private void createPrimaryRegionalCandidates() {
		// first line is a candidate name, second line is candidate's region.
		String [] lines = candidatesText.split("\n");
		int lineCount = lines.length;
		for (int i = 0; i < lineCount; i=i+2) {
			String name = lines[i];
			String region = "";
			if (i+1 < lineCount) {
				region = lines[i+1];
			} else {
				logger.error(String.format("candidate %s is missing region line", name));
			}
			createPrimaryCandidate(name, endorsedParty, region);
		}
	}
	/**
	 * createGeneralCandidates process the lines into a list 
	 * of GeneralCandidate objects.
	 */
	private void createGeneralCandidates() {
		// first line is candidate name, second line is party.
		String [] lines = candidatesText.split("\n");
		int lineCount = lines.length;
		for (int i = 0; i < lineCount; i=i+2) {
			Party party = null;
			String name = lines[i].trim();
			String partyStr = "";
			if (i+1 < lineCount) {
				partyStr = lines[i+1].trim();
//System.out.printf("candidate party: %s length: %d%n",partyStr,partyStr.length());
				party = Party.toEnum(partyStr);
				createGeneralCandidate(name, party, partyStr, false);
			} else {
				logger.error(String.format("general candidate %s is missing party line", name));
				break;
			}
		}
	}
	/**
	 * createGeneralTicketCandidates process the lines into a list 
	 * of GeneralCandidate objects.
	 */
	private void createGeneralTicketCandidates() {	
		// first line is top of ticket, second line is party, third line is bottom of ticket, fourth line is arbitrary text.
		String [] lines = candidatesText.split("\n");
		int lineCount = lines.length;
		for (int i = 0; i < lineCount; i=i+4) {
			Party party = null;
			String topName = lines[i].trim();
			String linePartyStr = "";
			String partyStr = "";
			if (i+1 < lineCount) {
				linePartyStr = lines[i+1].trim();
				partyStr = linePartyStr;
//System.out.printf("candidate party: %s length: %d%n",partyStr,partyStr.length());
				if (linePartyStr.startsWith("President, ")) {
					// 2024 Presidential Election - "President, Democratic"
					// Must remove office name.
					partyStr = linePartyStr.substring(11);
				}
				party = Party.toEnum(partyStr);
				createGeneralCandidate(topName, party, linePartyStr, false);
			} else {
				logger.error(String.format("top of ticket candidate %s is missing party line", topName));
				break;
			}
			String bottomName = "";
			if (i+2 < lineCount) {
				bottomName = lines[i+2].trim();	
			} else {
				logger.error(String.format("bottom of ticket candidate name is missing. Top name: %s"), topName);
				break;
			}
			if (i+3 < lineCount) {
				String textBeneathName = lines[i+3].trim();
				createGeneralCandidate(bottomName, party, textBeneathName, true);
			} else {
				logger.error(String.format("bottom of ticket candidate party is missing. Bottom name: %s"), bottomName);
				break;
			}
		}
	}
	/** 
	 * createPrimaryCandidate create a PrimaryCandidate object with given attributes.
	 * Add to the list of candidates.
	 * 
	 * @param name of candidate
	 * @param party of candidate
	 * @param region of candidate
	 */
	private void createPrimaryCandidate(String name, Party party, String region) {
		Candidate c = new PrimaryCandidate(name, party, region);
		candidates.add(c);
	}
	/** 
	 * createGeneralCandidate create a GeneralCandidate object with given attributes.
	 * Add to the list of candidates.
	 * 
	 * @param name of candidate
	 * @param party of candidate
	 * @param region of candidate
	 */
	private void createGeneralCandidate(String name, Party party, String textBeneathName, boolean isBottom) {
		Candidate c = new GeneralCandidate(name, party, textBeneathName, isBottom);
		candidates.add(c);
	}
	/**
	 * getCandidates returns the list of candidates.
	 * @return list of candidates
	 */
	public List<Candidate> getCandidates() {
		return candidates;
	}
	/**
	 * clearCandidates - for testing only.
	 */
	public void clearCandidates() {
		candidates = new ArrayList<>();
	}
}
