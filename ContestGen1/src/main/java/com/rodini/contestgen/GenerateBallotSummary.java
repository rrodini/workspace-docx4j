package com.rodini.contestgen;

import static com.rodini.ballotutils.Utils.ATTN;
import static java.util.stream.Collectors.joining;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
//import java.util.stream.Collectors;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.rodini.contestgen.common.Initialize;
import com.rodini.contestgen.model.Ballot;
import com.rodini.contestgen.model.Contest;
import com.rodini.contestgen.model.Referendum;
import com.rodini.contestgen.model.Retention;

public class GenerateBallotSummary {
	static final Logger logger = LogManager.getLogger(GenerateBallotSummary.class);

    // prevent instantiation.
	private GenerateBallotSummary() {}
	
    /**
     * generate the Ballot_Summary.txt file.  This file is used by BallotGen to
     * generate the UNIQUE ballots for this election.
     * 
     * Note: If the format of Ballot_Summary.txt changes, then BallotGen needs changes.
     * 
     * @param ballots List of Ballot objects.
     */
	public static void generate(List<Ballot> ballots) {
		try (PrintWriter pw = new PrintWriter(new File(Initialize.outContestPath + File.separator + ContestGen1.SUMMARY_FILE_NAME))) {
			generateHeading(pw, ballots);
			generateUniqueBallotSection(pw, ballots);
			generateReferendumSection(pw, ballots);
			generateRetentionSection(pw, ballots);
		} catch (IOException ex) {
			String msg = String.format("IOException writing ballot summary report: %s", ex.getMessage());			
			logger.error(msg);
			System.out.println(msg);
		}
	}
	/**
	 * generateHeading writes the header section.  This can be anything. 
	 * @param pw PrintWriter.
	 * @param ballots list of Ballots.
	 */
	static void generateHeading(PrintWriter pw, List<Ballot> ballots) {
		String line = "Summary Report (Ballot/Referendum/Retention)";
		System.out.println(line);
		pw.println(line);
		line = "Precinct count: " + Integer.toString(ballots.size());
		System.out.println(line);
		pw.println(line);
	}
	/** 
	 * generateUniqueBallotSection writes the unique ballot section.
	 * Each line represents a ballot that is identical across all of the listed
	 * precinct-leve ballots.
	 * 
	 * @param pw PreintWriter
	 * @param ballots list of Ballots.
	 */
	static void generateUniqueBallotSection(PrintWriter pw, List<Ballot> ballots) {
		//  uniqueBallotList is the data structure that determines ballot uniqueness
		//  based on the lists of VoteFor objects that a ballot may contain.
		//  uniqueString    List<precinctNoName>
		Map<String,         List<String>         > uniqueBallotList = new HashMap<>();
		// loop to populate the data structure.
		for (Ballot ballot: ballots) {
			List<String> precinctNoNameList;
			String ballotString = getUniqueString(ballot);
			if (uniqueBallotList.containsKey(ballotString)) {
				precinctNoNameList = uniqueBallotList.get(ballotString);
			} else {
				precinctNoNameList = new ArrayList<>();
			}
			precinctNoNameList.add(ballot.getPrecinctNoName());
			uniqueBallotList.put(ballotString, precinctNoNameList);
		}
		Set<String> uniqueKeys = uniqueBallotList.keySet();
		String line = String.format("Unique ballot count: %s", uniqueKeys.size());
		System.out.println(line);
		logger.log(ATTN, line);
		pw.println(line);
		line = String.format("Precincts with identical ballots:");
		pw.println(line);
		// loop to generate unique ballot section
		int count = 0;
		for (String uniqueKey: uniqueKeys) {
			List<String> precinctNoNameList = uniqueBallotList.get(uniqueKey);
			String precinctNoNameString = precinctNoNameList.stream().collect(joining(","));
			line = String.format("Ballot %3d: %s", count, precinctNoNameString);
			pw.println(line);
			count++;
		}
	}
	/**
	 * getUniqueString takes a precint-level ballot and returns a long string
	 * (to be used as a hash) that lists the contest name, referendum questions,
	 * and the retention office names.
	 * 
	 * @param ballot precinct-level ballot.
	 * @return long string that characterizes the ballot.
	 */
	static String getUniqueString(Ballot ballot) {
		String uniqueString;
		String contestsString = ballot.getContests().stream().map(Contest::getContestName).collect(joining(""));
		String refsString    = ballot.getReferendums().stream().map(Referendum::getRefQuestion).collect(joining(""));
		String retsString    = ballot.getRetentions().stream().map(Retention::getOfficeName).collect(joining(""));
		uniqueString = contestsString + refsString + retsString;
		return uniqueString;
	}
	/**
	 * generateReferendumSection writes the referendum section of the ballot summary.
	 * This helps identify the ballots (by precinct #) that have the same referendum question.
	 * The BallotGen program just ignores this section.
	 * 
	 * @param pw PrintWriter.
	 * @param ballots list of ballots.
	 */
	static void generateReferendumSection(PrintWriter pw, List<Ballot> ballots) {
		//  question        List<precinctNo>
		Map<String,         List<String>         > uniqueRefList = new HashMap<>();
		// loop to populate the data structure.
		for (Ballot ballot: ballots) {
			List<String> precinctNoList;
			String refString = ballot.getReferendums().stream().map(Referendum::getRefQuestion).collect(joining(""));
			if (uniqueRefList.containsKey(refString)) {
				precinctNoList = uniqueRefList.get(refString);
			} else {
				precinctNoList = new ArrayList<>();
			}
			precinctNoList.add(ballot.getPrecinctNo());
			uniqueRefList.put(refString, precinctNoList);
		}
		String line = "Referendum Summary";
		System.out.println(line);
		pw.println(line);
		line = String.format("Unique referendum questions: %d", uniqueRefList.size());
		System.out.println(line);
		logger.log(ATTN, line);
		pw.println(line);
		Set<String> refKeys = uniqueRefList.keySet();
		int count = 0;
		for (String refKey: refKeys) {
			List<String> precinctNos = uniqueRefList.get(refKey);
			line = String.format("Referendum %d:", count);
			pw.println(line);
			// refQuestion text seems to end w. \n
			pw.print(refKey);
			String precinctNosString = precinctNos.stream().collect(joining(","));
			pw.println(": precincts: " + precinctNosString);
			count++;
		}
	}
	/**
	 * generateRetentionSection writes the retention section of the ballot summary.
	 * This helps identify the judges that are up for a retention vote.
	 * The BallotGen program just ignores this section.
	 * 
	 * @param pw PrintWriter.
	 * @param ballots list of ballots.
	 */
	static void generateRetentionSection(PrintWriter pw, List<Ballot> ballots) {
		//  judgeName       ballot count
		Map<String,         Integer> uniqueJudgeName = new HashMap<>();
		// loop to populate the data structure.
		for (Ballot ballot: ballots) {
			List<Retention> retentions = ballot.getRetentions();
			for (Retention ret: retentions) {
				int nameCount;
				String judgeName = ret.getJudgeName();
				if (uniqueJudgeName.containsKey(judgeName)) {
					nameCount = uniqueJudgeName.get(judgeName);
				} else {
					nameCount = 0;
				}
				nameCount++;
				uniqueJudgeName.put(judgeName, nameCount);
			}
		}
		String line = "Retention Summary";
		System.out.println(line);
		pw.println(line);
		line = String.format("Unique retention judge names: %d", uniqueJudgeName.size());
		System.out.println(line);
		logger.log(ATTN, line);
		pw.println(line);
		Set<String> retKeys = uniqueJudgeName.keySet();
		for (String retKey: retKeys) {
			int retCount = uniqueJudgeName.get(retKey);
			line = String.format("%s: %d", retKey, retCount);
			pw.println(line);
		}
	}
}
