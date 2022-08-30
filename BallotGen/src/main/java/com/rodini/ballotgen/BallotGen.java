package com.rodini.ballotgen;

//import org.apache.logging.log4j.LogManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
		Utils.setLoggingLevel();
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
					Initialize.formatsText);
					gdb.generate();
		}
		System.out.printf("End of BallotGen app%n");
		logger.info("End of BallotGen app");
	}
}
