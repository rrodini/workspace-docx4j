package com.rodini.ballotgen;

import java.util.List;
import java.util.Map;
import java.util.Set;

//import org.apache.logging.log4j.LogManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.rodini.ballotutils.Utils;
/**
 * BallotGen is the program that generated the municipal level .docx (Word) files.
 * It is dependent on an upstream program (ContestGen) to generate municipal
 * level contest(s) files (e.g. Atglen_contests.txt) or common_contests.txt).
 * 
 * CLI arguments:
 * args[0] - path to single ballot text file or directory containing such.
 * args[1] - path to directory for generated municipal level "NNN_XYZ_contests.txt" files.
 *
 * @author Bob Rodini
 *
 */
public class BallotGen {
	
	private static final Logger logger = LoggerFactory.getLogger(BallotGen.class);
	static final String ENV_BALLOTGEN_VERSION = "BALLOTGEN_VERSION";
	
	public static void main(String[] args){
		// Get the logging level from JVM parameter on command line.
		Utils.setLoggingLevel("com.rodini.ballotgen");
		String version = System.getenv(ENV_BALLOTGEN_VERSION);
		String startMsg = String.format("Start of BallotGen app. Version: %s", version);
		System.out.println(startMsg);
		logger.info(startMsg);
		Initialize.start(args);
		// TODO: use a loop here if ballotFiles size > 1
		for (String ballotFile: Initialize.ballotFiles) {
			GenDocxBallot gdb = new GenDocxBallot(
					Initialize.msWordTemplateFile, 
					ballotFile,
					Initialize.contestLevel,
					Initialize.formatsText,
					Initialize.endorsementProcessor);					
			gdb.generate();
		}
		terminate();
		System.out.printf("End of BallotGen app%n");
		logger.info("End of BallotGen app");
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
		//             STEPHANIE GIBSON WILLIAMS           
		logger.info(line);
		// First loop
		for (String name: names) {
			line = String.format("%-25s %5d", name, GenDocxBallot.endorsedCandidates.get(name));
			logger.info(line);
		}
		Map<String,List<Endorsement>> candidateEndorsements = Initialize.endorsementProcessor.getCandidateEndorsements();
		names = candidateEndorsements.keySet();
		// Second loop.
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
