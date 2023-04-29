package com.rodini.ballotgen;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * BallotFactory is a pseudo-container for the contests on a 
 * municipality's ballot. It was retrofitted into the object
 * model to accommodate a ballot that lists the same contest
 * name twice e.g. AUDITOR - 4 year term vs. AUDITOR - 6 year term.
 * 
 * @author Bob Rodini
 *
 */


public class BallotFactory {
	private static final Logger logger = LoggerFactory.getLogger(BallotFactory.class);
	
	private final String ballotText;
	// (key, entry) example:  "Auditor", 0
	private final Map<String, Integer> contestNameIndexes;
	

	public BallotFactory(String ballotText) {
		this.ballotText = ballotText;
		contestNameIndexes = new HashMap<> ();
	}
	/**
	 * getBallotText returns the ballot text for this ballot.
	 * @return ballot text
	 */
	String getBallotText() {
		return ballotText;
	}
	/**
	 * getStartIndex gets the starting index for scanning for the contest name.
	 * @param contestName contest name
	 * @return index to start scanning
	 */
	int getStartIndex(String contestName) {
		int index = 0;
		if (contestNameIndexes.get(contestName) != null ) {
			index = contestNameIndexes.get(contestName);
		}
		logger.debug(String.format("contestName: %s start: %d%n", contestName, index));
		return index;
	}
	/**
	 * setEndIndex sets the starting index for next scan for contest name.
	 * @param contestName contest name
	 * @param index next start index
	 */
	void setEndIndex(String contestName, int index) {
		logger.debug(String.format("contestName: %s end:   %d%n", contestName, index));
		contestNameIndexes.put(contestName, index);
	}

}
