package com.rodini.voteforprocessor.extract;

import java.util.List;
import java.util.regex.Matcher;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.rodini.voteforprocessor.model.Candidate;
import com.rodini.voteforprocessor.model.Contest;

/**
 * ContestFactory is called when a match from a contest regex has been made.
 * 
 * @author Bob Rodini
 *
 */
public class ContestFactory {
	
	private static final Logger logger = LogManager.getLogger(ContestFactory.class);
	/**
	 * extractContest is called when a match from a contest regex has been made.
	 * 
	 * @param precinctNo precinct # with contest.
	 * @param precinctName precinct name with contest.
	 * @param contestName contest name.
	 * @param m Matcher object with contest fields.
	 * @param formatIndex regex (format) index used.
	 * @return Contest object.
	 */
	public static Contest extractContest(String precinctNo, String precinctName, String contestName, Matcher m, int formatIndex) {
		Contest contest = Contest.GENERIC_CONTEST;
		logger.info("contestName: " + contestName);
		try {
			// "term" is optional
			String term = getMatchGroup(m, "term");
			String instructions = getMatchGroup(m, "instructions");
			String candidatesText = getMatchGroup(m, "candidates");
			CandidateFactory cf = new CandidateFactory(contestName, candidatesText, Initialize.elecType, Initialize.endorsedParty);
			List<Candidate> candidates = cf.getCandidates();
			contest = new Contest(precinctNo, precinctName, contestName,
					      term, instructions, candidates, formatIndex);
		} catch (Exception e) {
			String msg = e.getMessage();
			logger.error(msg);
		}
		if (contest == Contest.GENERIC_CONTEST) {
			String msg = String.format("COULD NOT GENERATE CONTEST FOR: %s", contestName);
			logger.error(msg);
		}
		return contest;
	}
	/** 
	 * getMatchGroup attempts to get the value of the named match
	 * group.  There are some formats (regexes) that will not match.
	 * 
	 * @param m Matcher object
	 * @param groupName name for group
	 * @return value for group
	 */
	/* private */
	static String getMatchGroup(Matcher m, String groupName) {
		String value = "";
		try {
			value = m.group(groupName);
		} catch (Exception e) {
			String msg = e.getMessage();
			if (msg.contains("<term>")) {
				// this is expected for some formats (regexes).
				logger.info(msg);
			} else {
				logger.error(msg);
			}
		}
		return value;
	}
}
