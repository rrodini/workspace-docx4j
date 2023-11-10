package com.rodini.textcleaner;

import static java.nio.file.LinkOption.NOFOLLOW_LINKS;
import static java.util.stream.Collectors.joining;

import java.io.FileWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.rodini.ballotutils.Utils;
import static com.rodini.ballotutils.Utils.ATTN;

/**
 * TextCleaner is a simple program that takes an input text file and cleans
 * the text from the locked PDF conversion process. The reason for this
 * program is that Chester Co. Voter Services now prohibits direct text
 * extraction from their specimen ballot that BallotGen must work around.
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
public class TextCleaner {
	
	private static final Logger logger = LogManager.getLogger(TextCleaner.class);
	static final String ENV_BALLOTGEN_VERSION = "BALLOTGEN_VERSION";
	static final String ENV_BALLOTGEN_COUNTY = "BALLOTGEN_COUNTY";
	static final String PROPS_FILE = "textcleaner.properties";
	static Properties props;
	static final String RESOURCE_PATH = "./resources/";
	static final String TABREPLACER_RESOURCE_PATH = "../textcleaner/resources/";
	static String COUNTY;		// chester vs. bucks
	static String txtFilePath;
	
	// prevent instances
	private TextCleaner() {
	}

	public static void main(String[] args) {
		// Get the logging level from JVM parameter on command line.
		Utils.setLoggingLevel(LogManager.getRootLogger().getName());
		String version = Utils.getEnvVariable(ENV_BALLOTGEN_VERSION, true);
		String startMsg = String.format("Start of Text Cleaner app. Version: %s", version);
		System.out.println(startMsg);
		Utils.logAppMessage(logger, startMsg, true);
		COUNTY = Utils.getEnvVariable(ENV_BALLOTGEN_COUNTY, true);
		startMsg = String.format("Text Cleaner for: %s Co.", COUNTY);
		Utils.logAppMessage(logger, startMsg, false);
		initialize(args);
		
		// Below is the heart of the program.
		String txtFileContents = Utils.readTextFile(txtFilePath);
		String newFileContents = processLines(txtFileContents);
 		// guarantee a newline before end of file.
 		if (!newFileContents.endsWith("\n")) {
 			newFileContents = newFileContents + "\n";
 		}
		try (FileWriter newTextFile = new FileWriter(txtFilePath)) {
			newTextFile.write(newFileContents);
		} catch (Exception ex) {
			Utils.logFatalError(ex.getMessage());
		}
		Utils.logAppErrorCount(logger);
		String endMsg = String.format("End of Text Cleaner app.");
		Utils.logAppMessage(logger, endMsg, true);
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
	/* private */
	/**
	 * processLines - process all the lines of the file.
	 * @param contents original lines
	 * @return modified lines separated by \n.
	 */
	static String processLines(String contents) {
		String newLines = "";
		String [] lines = contents.split("\n");
		for (String line: lines) {
			String newLine = processLine(line);
			if (newLine != null) {
				newLines += newLine + "\n";
			}
		}
		return newLines;
	}
	
	/* private */
	/**
	 * processLine - process a single line. Rules:
	 * 1. Eliminate blank lines.
	 * 2. Eliminate lines that are only "0 " or "O "
	 * 3. Eliminate prefix of "0 " or "O ".
	 * 4. Eliminate lines that start with "Typ:"
	 * @param line input line
	 * @return null or processed line.
	 */
	static String processLine(String line) {
		String newLine = line;
		boolean startsBadly = line.startsWith("0 ") || line.startsWith("O ") || line.startsWith("o ")  || line.startsWith("7 ");
		if (line.isBlank()) {
			newLine = null;
		} else if (startsBadly) {
			// chop off the first two characters
			newLine = line.substring(2);
		}
		if (newLine.endsWith(" ")) {
			// remove the trailing space
			newLine = newLine.substring(0, newLine.length()-1);
		}
		if (newLine.isBlank() || line.startsWith("Typ:")) {
			newLine = null;
		}
		return newLine;
	}
	
	
}
