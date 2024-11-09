package com.rodini.ballotgen.common;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.rodini.ballotutils.*;
/**
 * BallotReportParser processes the Ballot_Summary.txt file produced by contestgen program.
 * This report should always be produced although it may not represent the entire county.
 * 
 * @author Bob Rodini
 *
 */
public class BallotReportParser {
	
	private static final Logger logger = LogManager.getLogger(BallotReportParser.class);
	private static final String uniqueBallotRegex = "Ballot\\s+(?<uniqueNo>\\d+): (?<precinctNoNames>.*)$\\n";
	
	/**
	 * oarseBallotReport parses the Ballot_Summary.txt file that is produced by ContestGen program.
	 * Obviously, the format of this report must be kept in sync with this code.
	 * Notes:
	 * 1. Two data structures are populated: uniqueFirstBallotFile and uniqueBallotFiles.
	 * 
	 * @param ballotSummaryPath path to the file BallotReport.txt.
	 */
	static void parseBallotReport(String ballotSummaryPath) {
		Initialize.uniqueFirstBallotFile = new ArrayList<>();
		Initialize.uniqueBallotFiles = new HashMap<> ();
		String ballotReportText = Utils.readTextFile(ballotSummaryPath);
		Pattern compiledUniqueBallotRegex = Utils.compileRegex(uniqueBallotRegex);
		Matcher m = compiledUniqueBallotRegex.matcher(ballotReportText);
		int uniqueCount = 0;
		while (m.find()) {
			// m.group(1) == "unique #"
			String uniqueNoText = m.group(1).trim();
			int uniqueNo = Integer.parseInt(uniqueNoText);
			// m.group(2) == "precinctNoNames"
			String ballotPrecinctNoNameText = m.group(2).trim();
//System.out.printf("Ballot%3d: %s%n", uniqueNo, ballotPrecinctNoNameText);
			logger.debug(String.format("Ballot%3d: %s", uniqueNo, ballotPrecinctNoNameText));
			String [] precinctNoNames = ballotPrecinctNoNameText.split(",");
			Initialize.uniqueBallotFiles.put(uniqueNo, Arrays.asList(precinctNoNames));
			Initialize.uniqueFirstBallotFile.add(precinctNoNames[0]);
//System.out.printf("uniqueFirstPrecinctNoName: %s%n", precinctNoNames[0]);
			logger.debug(String.format("uniqueFirstPrecinctNoName: %s", precinctNoNames[0]));
			uniqueCount++;
		}
		logger.debug("Unique ballot count: " + uniqueCount);
	}

}
