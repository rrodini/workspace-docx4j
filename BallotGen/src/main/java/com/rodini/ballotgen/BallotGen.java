package com.rodini.ballotgen;

import java.util.List;
import java.util.Map;
import java.util.Set;

//import org.apache.logging.log4j.LogManager;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.rodini.ballotgen.common.Initialize;
import com.rodini.ballotgen.endorsement.Endorsement;
import com.rodini.ballotutils.Utils;
import static com.rodini.ballotutils.Utils.ATTN;
/**
 * BallotGen is the program that generated the municipal level .docx (Word) files.
 * It is dependent on an upstream program (ContestGen) to generate municipal
 * level contest(s) files (e.g. Atglen_contests.txt) or common_contests.txt).
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
	
	private static final Logger logger = LogManager.getLogger(BallotGen.class);
	static final String ENV_BALLOTGEN_VERSION = "BALLOTGEN_VERSION";
	static final String ENV_BALLOTGEN_COUNTY = "BALLOTGEN_COUNTY";
	
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
		// TODO: use a loop here if ballotFiles size > 1
		for (String ballotFile: Initialize.ballotFiles) {
			GenDocxBallot gdb = new GenDocxBallot(
					Initialize.msWordTemplateFile, 
					ballotFile,
					Initialize.contestLevel,
					Initialize.formatsText,
					Initialize.endorsementProcessor,
					Initialize.writeinProcessor);								
			gdb.generate();
		}
		terminate();
		msg = String.format("Generated %d docx files", Initialize.docxGenCount);
		Utils.logAppMessage(logger, msg, false);
		Utils.logAppErrorCount(logger);
		msg = "End of BallotGen app";
		Utils.logAppMessage(logger, msg, true);
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
