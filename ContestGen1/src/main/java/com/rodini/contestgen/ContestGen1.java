package com.rodini.contestgen;

import static com.rodini.ballotutils.Utils.ATTN;

import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.rodini.ballotutils.Utils;
import com.rodini.contestgen.common.Initialize;
import static com.rodini.contestgen.common.ContestGenOutput.*;
import com.rodini.contestgen.extract.BallotExtractor;
import com.rodini.contestgen.extract.ContestExtractor;
import com.rodini.contestgen.extract.PageExtractor;
import com.rodini.contestgen.extract.ReferendumExtractor;
import com.rodini.contestgen.extract.RetentionExtractor;
import com.rodini.contestgen.model.Ballot;

/**
 * ContestGen1 is the program that analyzes the text of the Voter Services specimen
 * file and extracts the ballot text and precinct level contests from it. 
 * 
 * At the end of the run the contests folder (args[1]) should be populated as follows:
 *   005_Atglen_contests.txt
 *   010_Avondale_contests.txt
 *   ...
 *   Ballot_Summary.txt      <= summary report
 * And the text ballots folder (args[2]) should be populated as follows;
 *   municipal0.txt (to be changed to 005_Atglen)
 *   municipal1.txt (to be changed to 010_Avondale)
 *   
 * CLI arguments: 
 * args[0] path Voter Services specimen text file.
 * args[1] path to directory for generated municipal level "NNN_XYZ_contests.txt" files.
 * args[2] path to directory for generated municipal level "municipalN.txt" files.
 * 
 * ENV variables:
 * BALLOTGEN_VERSION version # of Ballot Gen Software (e.g. "1.4.0")
 * BALLOTGEN_COUNTY  county for Ballot Gen (e.g. "chester")
 * 
 * @author Bob Rodini
 *
 */

public class ContestGen1 {
	private static final Logger logger = LogManager.getRootLogger();
	public  static final String ENV_BALLOTGEN_VERSION = "BALLOTGEN_VERSION";
	public  static final String ENV_BALLOTGEN_COUNTY = "BALLOTGEN_COUNTY";
	public  static final String PROPS_FILE = "contestgen.properties";
	public  static final String RESOURCE_PATH = "./resources/";
	public  static final String CONTESTS_FILE = "_contests.txt";
	public  static final String BALLOT_FILE = "municipal";
	public 	static final String TEXT_EXT = ".txt";
	public 	static final String PAGE_BREAK = "PAGE_BREAK"; // pseudo contest name
	public 	static final String SUMMARY_FILE_NAME = "Ballot_Summary.txt";
	public 	static String COUNTY;		// chester vs. bucks

	public  static List<Ballot> ballots;

	// prevent instances
	private ContestGen1() { }
	/** 
	 * main entry point for program. Check for necessary ENV variables.
	 * @param args CLI arguments
	 */
	public static void main(String[] args) {
		Utils.setLoggingLevel(LogManager.getRootLogger().getName());
		String version = Utils.getEnvVariable(ENV_BALLOTGEN_VERSION, true);
		COUNTY = Utils.getEnvVariable(ENV_BALLOTGEN_COUNTY, true);
		String startMsg = String.format("Start of ContestGen1 app. Version: %s", version);
		Utils.logAppMessage(logger, startMsg, true);
		startMsg = String.format("Contests for: %s Co.", COUNTY);
		Utils.logAppMessage(logger, startMsg, false);		
		// check for 3 command args
		if (args.length < 3) {
			Utils.logFatalError("missing CLI arguments:\n" +
					"args[0]: path Voter Services specimen text file.\n" +
					"args[1]: path to directory for generated precinct level \"NNN_XYZ_contests.txt\" files." +
					"args[2]: path to directory for generated precinct level \"municipalN.txt\" files.");
		} else {
			String msg0 = String.format("path to Voter Services specimen text: %s", args[0]);
			String msg1 = String.format("path to directory for contests:       %s", args[1]);
			String msg2 = String.format("path to directory for ballots :       %s", args[2]);
			System.out.println(msg0);
			logger.log(ATTN, msg0);
			System.out.println(msg1);
			logger.log(ATTN, msg1);
			System.out.println(msg2);
			logger.log(ATTN, msg2);
		}
		// Validate CLI args and property values.
		Initialize.start(args);
		ballots = BallotExtractor.extract(Initialize.specimenText);
		PageExtractor.extract(ballots);
		ContestExtractor.extract(ballots);
		ReferendumExtractor.extract(ballots);
		RetentionExtractor.extract(ballots);
		if (Initialize.contestGenOutput == BALLOTS || Initialize.contestGenOutput == BOTH) {
			GenerateBallotFiles.generate(ballots);
		}
		if (Initialize.contestGenOutput == CONTESTS || Initialize.contestGenOutput == BOTH) {
			GenerateContestFiles.generate(ballots);
		}
		GenerateBallotSummary.generate(ballots);
		Utils.logAppErrorCount(logger);
		Utils.logAppMessage(logger, "End of ContestGen1 app.", true);
	}

}
