package com.rodini.tabreplacer;

import static java.nio.file.LinkOption.NOFOLLOW_LINKS;
import static java.util.stream.Collectors.joining;

import java.io.FileWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

//import org.apache.logging.log4j.LogManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.rodini.ballotutils.Utils;
/**
 * TabReplacer is a simple program that takes an input text file and replaces
 * all internal tab characters with space characters. The reason for this
 * program is that Bucks Co. Voter Services uses tabs instead of spaces in 
 * their specimen ballots.
 * 
 * CLI arguments:
 * args[0] - path to single text file.
 *
 * ENV variables:
 * BALLOTGEN_VERSION version # of Ballot Gen Software (e.g. "1.4.0")
 * BALLOTGEN_COUNTY  county for Ballot Gen (e.g. "chester")
 *
 * @author Bob Rodini
 *
 */
public class TabReplacer {
	
	private static final Logger logger = LoggerFactory.getLogger(TabReplacer.class);
	static final String ENV_BALLOTGEN_VERSION = "BALLOTGEN_VERSION";
	static final String ENV_BALLOTGEN_COUNTY = "BALLOTGEN_COUNTY";
	static final String PROPS_FILE = "tabreplacer.properties";
	static Properties props;
	static final String RESOURCE_PATH = "./resources/";
	static final String TABREPLACER_RESOURCE_PATH = "../tabreplacer/resources/";
	static String COUNTY;		// chester vs. bucks
	static String txtFilePath;
	
	// prevent instances
	private TabReplacer() {
	}

	public static void main(String[] args) {
		// Get the logging level from JVM parameter on command line.
		Utils.setLoggingLevel("com.rodini.tabreplacer");
		String version = Utils.getEnvVariable(ENV_BALLOTGEN_VERSION, true);
		String startMsg = String.format("Start of TabReplacer app. Version: %s", version);
		System.out.println(startMsg);
		logger.info(startMsg);
		COUNTY = Utils.getEnvVariable(ENV_BALLOTGEN_COUNTY, true);
		startMsg = String.format("Tab replacement for: %s Co.", COUNTY);
		System.out.println(startMsg);
		logger.info(startMsg);
		initialize(args);
		
		// Below is the heart of the program.
		String txtFileContents = Utils.readTextFile(txtFilePath);
		String newFileContents = txtFileContents.replaceAll("\t", " ");
 		// guarantee a newline before end of file.
 		if (!newFileContents.endsWith("\n")) {
 			newFileContents = newFileContents + "\n";
 		}
		try (FileWriter newTextFile = new FileWriter(txtFilePath)) {
			newTextFile.write(newFileContents);
		} catch (Exception ex) {
			Utils.logFatalError(ex.getMessage());
		}
	}

	/* private */
	static void initialize(String[] args) {
		// check the # of command line args
		if (args.length < 1) {
			Utils.logFatalError("initialize: missing command line argument:\n" +
					"args[0]: path to text file");
		} else {
			String msg0 = String.format("path to text file: %s", args[0]);
			System.out.println(msg0);
			logger.info(msg0);
		}
		// validate the CLI args
		txtFilePath = args[0];
		if (!Files.exists(Path.of(txtFilePath), NOFOLLOW_LINKS)) {
			Utils.logFatalError("can't find \"" + txtFilePath + "\" file.");
		}
		if (!txtFilePath.endsWith("txt")) {
			Utils.logFatalError("file \"" + txtFilePath + "\" doesn't end with TXT extension.");
		}
		String propsFilePath = RESOURCE_PATH + PROPS_FILE;
		// get tabreplacer properties (if any)
		props = Utils.loadProperties(propsFilePath);
	}
	
	
	
}
