package com.rodini.textcleaner;

import static java.nio.file.LinkOption.NOFOLLOW_LINKS;
import static java.util.stream.Collectors.joining;

import java.io.FileWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
 * In the case where the text was extracted after Abbyy software messes
 * up the precinct name at the beginning of each ballot, the program
 * also fixes the problem by virtue of another input file.
 * 
 * CLI arguments:
 * args[0] - path to extracted VS text file.
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
	static String COUNTY;		// chester vs. bucks
	static String txtFilePath;
	// properties
	static String precinctRegex;
	static Pattern compiledPrecinctRegex;
	static boolean insertPrecinctNames;
	static String precinctNamesFile;
	
	// prevent instances
	private TextCleaner() {
	}

	public static void main(String[] args) {
		// Get the logging level from JVM parameter on command line.
		Utils.setLoggingLevel(LogManager.getRootLogger().getName());
		String version = Utils.getEnvVariable(ENV_BALLOTGEN_VERSION, true);
		String startMsg = String.format("Start of Text Cleaner app. Version: %s", version);
		Utils.logAppMessage(logger, startMsg, true);
		COUNTY = Utils.getEnvVariable(ENV_BALLOTGEN_COUNTY, true);
		startMsg = String.format("Text Cleaner for: %s Co.", COUNTY);
		Utils.logAppMessage(logger, startMsg, false);
		initialize(args);
		
		// Below is the heart of the program.
		String txtFileContents = Utils.readTextFile(txtFilePath);
		String newFileContents;
		if (insertPrecinctNames) {
			txtFileContents = insertPrecinctNames(txtFileContents);
		}
		newFileContents = processLines(txtFileContents);
 		// guarantee a newline before end of file.
 		if (!newFileContents.endsWith("\n")) {
 			newFileContents = newFileContents + "\n";
 		}
		try (FileWriter newTextFile = new FileWriter(txtFilePath, StandardCharsets.UTF_8, false)) {
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
		String msg0;
		if (args.length < 1) {
			Utils.logFatalError("initialize: missing command line argument:\n" +
					"args[0]: path to text file");
		} else {
			msg0 = String.format("path to text file: %s", args[0]);
			Utils.logAppMessage(logger, msg0, true);
		}
		// validate the CLI args
		txtFilePath = args[0];
		msg0 = String.format("", txtFilePath);
		if (!Files.exists(Path.of(txtFilePath), NOFOLLOW_LINKS)) {
			Utils.logFatalError("can't find \"" + txtFilePath + "\" file.");
		}
		if (!txtFilePath.endsWith("txt")) {
			Utils.logFatalError("file \"" + txtFilePath + "\" doesn't end with TXT extension.");
		}
		String propsFilePath = RESOURCE_PATH + PROPS_FILE;
		// initialize textcleaner properties
		initProperties(propsFilePath);
	}
	/**
	 * initProperties reads in text cleaners properties.
	 * Currently, this is just the precinct name insertion.
	 */
	static void initProperties(String propsFilePath) {
		props = Utils.loadProperties(propsFilePath);
		String value = Utils.getPropValue(props, "insertPrecinctNames");
		if (value == null) {
			value = "false";
		}
		insertPrecinctNames = Boolean.parseBoolean(value);
//System.out.printf("insertPrecinctNames: %b%n", insertPrecinctNames);
		logger.info(String.format("insertPrecinctName: %s", value));
		if (insertPrecinctNames) {
			// Utils will detect any errors
			precinctRegex = Utils.getPropValue(props, "precinctRegex");
			compiledPrecinctRegex = Utils.compileRegex(precinctRegex);
//System.out.printf("compiledPrecinctRegex: %s%n", compiledPrecinctRegex);
			precinctNamesFile = Utils.getPropValue(props, "precinctNamesFile");
//System.out.printf("precinctNamesFile: %s%n", precinctNamesFile);
			if (!Files.exists(Path.of(precinctNamesFile), NOFOLLOW_LINKS)) {
				Utils.logFatalError("can't find \"" + precinctNamesFile + "\" file.");
			} else {
				String msg0 = String.format("path to precinctNamesFile: %s", precinctNamesFile);
				Utils.logAppMessage(logger, msg0, true);
			}
			if (!precinctNamesFile.endsWith("txt")) {
				Utils.logFatalError("file \"" + precinctNamesFile + "\" doesn't end with TXT extension.");
			}
		}
	}
	/**
	 * getPrecinctName returns the 2 line precinct name from the pre-defined precinctNamesFile.
	 * Example: OFFICIAL DEMOCRATIC GENERAL PRIMARY BALLOT
	 *          005 ATGLEN
	 * @param which index of the precinct name.
	 * @param names array of precinct names.
	 * @return
	 */
	static String getPrecinctName(int which, String [] names) {
		String line1 = names[2*which];
		String line2 = names[2*which + 1];
		return line1 + "\n" + line2 + "\n";
	}
	/**
	 * insertPrecinctNames uses a regex to find the position where each 
	 * precinct name needs to be inserted into the contents of the text file.
	 * 
	 * @param contents text from VS specimen.
	 * @return text with the precinct names inserted.
	 */
	static String insertPrecinctNames(String contents) {
		// Read in all of the pre-defined precinct names.
		// ATTENTION: if any precincts are added or deleted the file must be updated.
		String precinctNamesText = Utils.readTextFile(precinctNamesFile);
		String [] precinctNamesLines = precinctNamesText.split("\n");
		int precinctCount = precinctNamesLines.length / 2;
		// counter object is a workaround to the "effectively final" problem.
		AtomicInteger counter = new AtomicInteger(0);
		Matcher m = compiledPrecinctRegex.matcher(contents);
		if (!m.find()) {
			logger.error("Zero precinct names inserted.");
		}
		// Need to reset after first m.find().
		m.reset();
		// replaceAll streaming API does all of the work.
		String newContents = m.replaceAll(x -> 
		  {String name = getPrecinctName(counter.get(), precinctNamesLines); counter.incrementAndGet(); return name;});
		if (counter.get() != precinctCount) {
			logger.error(String.format("count mismatch. count: %d precinctCount: %d", counter.get(), precinctCount));
		}
		return newContents;
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
	 * 5. Eliminate space at end of line.
	 * 6. Eliminate prefix of "§ ". // Abbyy artifact
	 * @param line input line
	 * @return null or processed line.
	 */
	static String processLine(String line) {
		String newLine = line;
		boolean startsBadly = line.startsWith("0 ") || line.startsWith("O ") || line.startsWith("o ")  || line.startsWith("7 ") || line.startsWith("§ ");
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
