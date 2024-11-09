package com.rodini.ballotgen;

import java.util.List;
import java.util.Map;
import java.util.Set;

//import org.apache.logging.log4j.LogManager;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.rodini.ballotgen.common.BallotUtils;
//import com.rodini.ballotutils.Utils.*;
import com.rodini.ballotgen.common.GenDocxBallot;
import com.rodini.ballotgen.common.Initialize;
import static com.rodini.ballotgen.common.BallotGenOutput.*;
import com.rodini.ballotgen.endorsement.Endorsement;
import com.rodini.ballotutils.Utils;
import static com.rodini.ballotutils.Utils.ATTN;
/**
 * BallotGen is the program that generated the precinct level .docx (Word) files.
 * It also generates the unique ballot .docx (Word) files.
 * 
 * It is dependent on an upstream program (ContestGen) to generate precinct
 * level contest(s) files (e.g. Atglen_contests.txt) and the Ballot_Summary.txt file.
 * 
 * CLI arguments:
 * args[0] - path to directory for generated municipal level DOCX files (e.g "NNN_XYZ_contests.docx").
 * args[1] - path to directory with contests TXT files (e.g. "NNN_XYZ_contests.txt").
 *
 * ENV variables:
 * BALLOTGEN_VERSION version # of Ballot Gen Software (e.g. "1.4.0")
 * BALLOTGEN_COUNTY  county for Ballot Gen (e.g. "chester")
 * 
 * @author Bob Rodini
 *
 */
public class BallotGen {
	
	private static final Logger logger = LogManager.getRootLogger();
	static final String ENV_BALLOTGEN_VERSION = "BALLOTGEN_VERSION";
	static final String ENV_BALLOTGEN_COUNTY = "BALLOTGEN_COUNTY";
	static final String UNIQUE_TITLE = "UNIQUE_";  // e.g. unique_01.docx
	static       int precinctBallotCount = 0;
	static       int uniqueBallotCount = 0;
	
	public static void main(String[] args){
		// Get the logging level from JVM parameter on command line.
		Utils.setLoggingLevel(LogManager.getRootLogger().getName());
		String version = Utils.getEnvVariable(ENV_BALLOTGEN_VERSION, true);
		String msg = String.format("Start of BallotGen app. Version: %s", version);
		Utils.logAppMessage(logger, msg, true);
		Initialize.COUNTY = Utils.getEnvVariable(ENV_BALLOTGEN_COUNTY, true);
		msg = String.format("Ballots for: %s Co.", Initialize.COUNTY);
		Utils.logAppMessage(logger, msg, false);
		Initialize.start(args);
		if (Initialize.ballotGenOutput == PRECINCT || Initialize.ballotGenOutput == BOTH) {
			// Generate all precinct ballots.
			genPrecinctBallotFiles(Initialize.msWordPrecinctTemplateFile);
		}
		if (Initialize.ballotGenOutput == UNIQUE || Initialize.ballotGenOutput == BOTH) {
			// Generate unique ballots.
			genUniqueBallotFiles(Initialize.msWordUniqueTemplateFile);
		}
		terminate();
		msg = String.format("Generated %d precinct ballots", precinctBallotCount);
		Utils.logAppMessage(logger, msg, false);
		msg = String.format("Generated %d unique ballots", uniqueBallotCount);
		Utils.logAppMessage(logger, msg, false);
		Utils.logAppErrorCount(logger);
		msg = "End of BallotGen app";
		Utils.logAppMessage(logger, msg, true);
	}
	/** 
	 * genPrecinctBallotFiles generates either PRECINCT ballots (1 per precinct).
	 */
	private static void genPrecinctBallotFiles(String msWordTemplate) {
		for (String ballotFile: Initialize.ballotFiles) {
			String precinctBallotFile = BallotUtils.getPrecinctNoName(ballotFile);
			GenDocxBallot gdb = new GenDocxBallot(
					msWordTemplate, 
					ballotFile,  // ./chester-output/NNN_municipal_XYZ_VS.txt
					precinctBallotFile, // NNN_municipal_XYZ
// TBD - TO BE DELETED.
//					Initialize.contestLevel,
					Initialize.formatsText,
					Initialize.endorsementProcessor,
					Initialize.writeinProcessor);								
			gdb.generate();
			precinctBallotCount++;
		}
	}
	
	/** 
	 * genUniqueBallotFiles generates either UNIQUE ballots (see ../chester-contests/BallotSummary.txt).
	 * After this report is parsed, the data structure Initialize.uniqueFirstBallotFile triggers
	 * the generation of the sample ballot that is shared between precincts.
	 * Initialize.ballotFiles list.
	 */
	private static void genUniqueBallotFiles(String msWordTemplate) {
		// The logic works because ballotFile names are unique across the county.
		for (String ballotFile: Initialize.ballotFiles) {
//System.out.printf("genUniqueBallotFiles: candidate: %s%n", ballotFile);
			String precinctBallotFile = BallotUtils.getPrecinctNoName(ballotFile);
			if (Initialize.uniqueFirstBallotFile.contains(precinctBallotFile)) {
				// TBD - set values for uniquePrecinctNos, uniquePrecinctNames, uniquePrecinctNoNames
//System.out.printf("genUniqueBallotFiles: first match: %s%n", ballotFile);
				int uniqueNo = Initialize.uniqueFirstBallotFile.indexOf(precinctBallotFile);
				String uniqueBallotFile = UNIQUE_TITLE + 
						com.rodini.ballotutils.Utils.normalizeNo(uniqueNo, 2) +
						"_" + precinctBallotFile;
				GenDocxBallot gdb = new GenDocxBallot(
						msWordTemplate,
						ballotFile,  // same as chester-contests/NNN_municipal_XYZ_contests.txt and chester-output/NNN_municipal_XYZ_VS.txt
						uniqueBallotFile, // unique_NN
	// TBD - TO BE DELETED.
	//					Initialize.contestLevel,
						Initialize.formatsText,
						Initialize.endorsementProcessor,
						Initialize.writeinProcessor);								
				gdb.generate();
				uniqueBallotCount++;
			}
		}
	}

	/**
	 * Terminate ends the BallotGen with summary information.
	 */
	private static void terminate() {
		// Generate list of endorsed candidates as a means for checking for errors
		// The first loop just shows how many cumulative endorsements any candidate received.
		// The second loop checks that a candidate with and explicit endorsement was
		// endorsed on any ballot.  If the answer is "no" the candidate's name is probably misspelled.		
		Set<String> names = GenDocxBallot.endorsedCandidates.keySet();
		String line = "Endorsed Candidate      No. endorsements";
		//             STEPHANIE GIBSON WILLIAMS     <= long name
		logger.info(line);
		// First loop - messages always recorded.
		for (String name: names) {
			line = String.format("%-25s %5d", name, GenDocxBallot.endorsedCandidates.get(name));
			logger.log(ATTN, line);
		}
		Map<String,List<Endorsement>> candidateEndorsements = Initialize.endorsementProcessor.getCandidateEndorsements();
		names = candidateEndorsements.keySet();
		// Second loop - ERROR messages.
		for (String name: names) {
			if (GenDocxBallot.endorsedCandidates.get(name) == null) {
				
				line = String.format("%s received endorsements (below) but did not appear on any ballot:", name);
				logger.error(line);
				System.out.println(line);
				for (Endorsement end: candidateEndorsements.get(name)) {
					line = "   " + end.toString();
					logger.error(line);
					System.out.println(line);
				}
			}
		}

	}
}
