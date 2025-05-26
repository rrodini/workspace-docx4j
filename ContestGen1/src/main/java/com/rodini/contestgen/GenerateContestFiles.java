package com.rodini.contestgen;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.rodini.contestgen.common.Initialize;
import com.rodini.contestgen.model.Ballot;
import com.rodini.voteforprocessor.model.Contest;
import com.rodini.voteforprocessor.model.Referendum;
import com.rodini.voteforprocessor.model.Retention;
//@formatter: off
/**
 * GenerateContestFiles generates the precinct-level files in the ./contests folder.
 * Example: ./chester-contests/
 *             005_ATGLEN_contests.txt
 *             010_AVONDALE_contests.txt
 *             014_BIRMINGHAM_1_contests.txt
 *             ...                 
 * 
 * @author Bob Rodini
 *
 */
//@formatter: on
public class GenerateContestFiles {
	static final Logger logger = LogManager.getLogger(GenerateContestFiles.class);
	// prevent instantiation
	private GenerateContestFiles() {}
	/**
	 * GenerateContestFiles processes the list of ballots and generates a NNN_NAME_contest.txt
	 * file for each.
	 * @param ballots List of Ballot objects.
	 */
	public static void generate(List<Ballot> ballots) {
		for (Ballot ballot: ballots) {
			String precinctNoName = ballot.getPrecinctNoName();
			String contestFilePath = Initialize.outContestPath + File.separator + precinctNoName + ContestGen1.CONTESTS_FILE;
			String msg = String.format("writing file: %s", contestFilePath);
			logger.info(msg);
			try (FileWriter contestsFile = new FileWriter(contestFilePath, StandardCharsets.UTF_8, false);) {
				generateContests(contestsFile, ballot);
				generateReferendums(contestsFile, ballot);
				generateRetentions(contestsFile, ballot);
			} catch (IOException e) {
				logger.error(e.getMessage());
			}
		}
	}
	/**
	 * generateContests generates the Contests section within the contest file.
	 * 
	 * @param cf contest file
	 * @param ballot 
	 * @throws IOException
	 */
	static void generateContests(Writer cf, Ballot ballot) throws IOException {
		cf.write("Contests\n");
		for (Contest contest: ballot.getContests()) {
			String contestName = contest.getName();
			contestName = contestName.replaceAll("\n", "\\\\\\n");
			cf.write(String.format("%s,%d%n", contestName, contest.getFormatIndex()));
		}	
	}
	/**
	 * generateReferendums generates the Referendums section within the contest file.
	 * Note that there may be no referendums.
	 * @param cf contest file
	 * @param ballot 
	 * @throws IOException
	 */
	static void generateReferendums(Writer cf, Ballot ballot) throws IOException {
		cf.write("Referendums\n");
		for (Referendum ref: ballot.getReferendums()) {
			String refQuestion = ref.getRefQuestion();
			refQuestion = refQuestion.replaceAll("\n", "\\\\\\n");
			cf.write(String.format("%s%n", refQuestion));
		}	
	}
	/**
	 * generateRetentions generates the Retentions section within the contest file.
	 * Note that there may be no retentions.
	 * @param cf contest file
	 * @param ballot 
	 * @throws IOException
	 */
	static void generateRetentions(Writer cf, Ballot ballot) throws IOException {
		cf.write("Retentions\n");
		for (Retention ret: ballot.getRetentions()) {
			String officeName = ret.getOfficeName();
			String judgeName = ret.getJudgeName();
			officeName = officeName.replaceAll("\n", "\\\\\\n");
			judgeName = judgeName.replaceAll("\n", "\\\\\\n");
			cf.write(String.format("%s,%s%n", officeName, judgeName));
		}	
	}

}
