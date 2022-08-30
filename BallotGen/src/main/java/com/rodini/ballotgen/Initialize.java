package com.rodini.ballotgen;

import static com.rodini.ballotgen.Utils.logFatalError;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.stream.Stream;
import static java.nio.file.LinkOption.NOFOLLOW_LINKS;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
/** 
 * Initialize gets the program ready to generate sample ballots.
 * It attempts to validate critical inputs and FAIL EARLY if things
 * are amiss.
 * 
 * @author Bob Rodini
 *
 */
public class Initialize {
	// Global variables
	public static ElectionType elecType;	// e.g. PRIMARY or GENERAL
	public static Party endorsedParty;		// e.g. Democratic (or NULL)
	public static List<String> ballotFiles; // e.g. List: "Atglen_VS.txt","Avondale_VS.txt",..,"West_Chester_7_VS.txt"
//	public static String contestsText;  	// e.g. Justice of the Supreme Court,1
//											// 		Judge of the Superior Court,1
	public static String ballotContestsPath;
	public static String formatsText;		// read from properties file
	public static String msWordTemplateFile = "";	// MS Word template file
	public static ContestFileLevel contestLevel; 	// e.g. COMMON or MUNICIPAL
	public static ENVIRONMENT env;			// TEST vs. PRODUCTION
	public static Properties ballotGenProps;
	public static Properties contestGenProps;

//	ATTENTION: Within Eclipse you must put ./resources in the dependencies
	public static final String RESOURCE_PATH = "./resources/";
	static final String CONTESTGEN_RESOURCE_PATH = "../contestgen/resources/";
	public static final String PROPS_FILE = "ballotgen.properties";
	static final String CONTESTGEN_PROPS_FILE = "contestgen.properties";

	private static final Logger logger = LoggerFactory.getLogger(Initialize.class);
	private static final String PROP_ENDORSED_PARTY = "endorsed.party";
	private static final String PROP_WORD_TEMPLATE_DEFAULT = "word.template.default";
	private static final String PROP_CONTEST_FORMAT_PREFIX = "contest.format";
	private static final String CONTEST_FILE_LEVEL = "contest.file.level";
	public static final String COMMON_CONTESTS_FILE = "common_contests.txt";

	/**
	 * validateCommandLineArgs checks that there are at least 2 CLI args
	 * args[0] - ballotFilePath path to single ballot text file or directory containing such.
	 * args[1] - ballotContestsPath contest files.
	 * 
	 * @param args command line arguments
	 */
	/* private */
	static void validateCommandLineArgs(String [] args) {
		// check for 2 command args
		if (args.length < 2) {
			logFatalError("missing CLI arguments:\n" +
					"args[0]: path to single ballot text file or directory containing such.\n" +
					"args[1]: path to directory for generated municipal level \"NNN_XYZ_contests.txt\" files.");
		} else {
			String msg0 = String.format("input dir:      : %s", args[0]);
			String msg1 = String.format("contest file/dir: %s", args[1]);
			System.out.println(msg0);
			System.out.println(msg1);
			logger.info(msg0);
			logger.info(msg1);
		}
		String ballotFilePath = args[0];
		processBallotFiles(ballotFilePath);
		ballotContestsPath = args[1];
		processBallotContests(ballotContestsPath);
		msWordTemplateFile = RESOURCE_PATH + Utils.getPropValue(ballotGenProps, PROP_WORD_TEMPLATE_DEFAULT);
		logger.info("msWordTemplateFile: " + msWordTemplateFile);
	}
	/**
	 * processBallotFiles take the CLI argument and process it into
	 * the list of files (by name) to be processed.
	 * @param ballotFilePath path to file or directory
	 */
	/* private */
	static void processBallotFiles(String ballotFilePath) {
		File path = new File(ballotFilePath);
		try {
			if (path.isDirectory()) {
				ballotFiles = Stream.of(path.listFiles())
						.filter(file -> !file.isDirectory() && file.getName().endsWith(".txt"))
						.map(File::getName)
						.map(name -> ballotFilePath + File.separator + name)
						.collect(toList());

			} else if (path.isFile()) {
				ballotFiles = new ArrayList<String>();
				ballotFiles.add(ballotFilePath);
				
			} else {
				logFatalError("invalid args[0] value, not a file or directory: " + ballotFilePath);
			}
		} catch (SecurityException e) {
			logFatalError("can't access this file/directory: " + ballotFilePath);
		}
		// prepare logging message
		String fileListString = ballotFiles.toString();
		int len = fileListString.length();
		if (len > 200) {
			fileListString = fileListString.substring(0, 100) + "..." + 
		                     fileListString.substring(len - 100, len);
		}
		logger.info(String.format("files to process: %n" + fileListString));
	}
	/**
	 * processBallotContests validates the ballotContestsPath.
	 * 
	 * @param ballotContestsPath the contest names and formats
	 */
	/* private */
	static void processBallotContests(String ballotContestsPath) {
		File path = new File(ballotContestsPath);
		if (!path.isDirectory()) {
			logFatalError("invalid args[1] value, ballotContestsPath does not exist: " + ballotContestsPath);
		}
		String commonFilePath = ballotContestsPath + File.separator + COMMON_CONTESTS_FILE;
		if (!Files.exists(Path.of(commonFilePath), NOFOLLOW_LINKS)) {
			logFatalError("can't find \"" + COMMON_CONTESTS_FILE + "\" file here: " + commonFilePath);
		}
	}

	static void validateElectionType() {
		String type = Utils.getPropValue(ballotGenProps, "election.type");
		logger.info(String.format("election.type: %s", type));
		elecType = ElectionType.toEnum(type);
	}
	
	static void validateEndorsedParty() {
		String endorsedPartyString = Utils.getPropValue(ballotGenProps, PROP_ENDORSED_PARTY);
		logger.info(String.format("endorsed.party: %s", endorsedPartyString));
		endorsedParty = endorsedPartyString.isEmpty()? null : Party.toEnum(endorsedPartyString);
	}
	
	static void validateContestFileLevel() {
		String level = Utils.getPropValue(ballotGenProps, "contest.file.level");
		logger.info(String.format("contet.file.level: %s", level));
		contestLevel = ContestFileLevel.valueOf(level);
	}
	/**
	 * validateFormatsText reads the formats (regexes) from the properties
	 * file and gets them into a long string that ContestFactory
	 * expects.
	 */
	static void validateFormatsText() {
		List<String> formatLines = Utils.getPropOrderedValues(contestGenProps, "ballotgen.contest.format");
		formatsText = formatLines.stream()
				.collect(joining("\n"));
		logger.info(String.format("formatsText:%n%s%n", formatsText));
	}


	static void validateContestFormats () {
		String format;
		int count = 1;
		do {
			// Don't know how many there will be
			String key = Integer.toString(count);
			String propName = PROP_CONTEST_FORMAT_PREFIX + "." + key;
			format = Utils.getPropValue(contestGenProps, propName);
			logger.info(String.format("contestgen.contest.format.%d: %s", count, format));
			if (format != null) {
				count++;				
			}
		} while (format != null);
		logger.info(String.format("there are %d contest formats in the contestgen properties file", count-1));
	}
	

	static void validateWordTemplate() {
		if (msWordTemplateFile.isEmpty()) {
			logFatalError("MS Word template file not specified (blank)");
		}
		if (!Files.exists(Path.of(msWordTemplateFile))) {
			logFatalError("MS Word template file does not exist: " + msWordTemplateFile);
		}
		if (!msWordTemplateFile.endsWith(".dotx")) {
			logFatalError("MS Word template file should end with \"dotx\": " + msWordTemplateFile);
		}
	}
	
	public static void start(String [] args) {
		ballotGenProps = Utils.loadProperties(RESOURCE_PATH + PROPS_FILE);
		contestGenProps = Utils.loadProperties(CONTESTGEN_RESOURCE_PATH + CONTESTGEN_PROPS_FILE);
		env = ENVIRONMENT.valueOf(Utils.getPropValue(ballotGenProps, "environment"));
		contestLevel = ContestFileLevel.valueOf(Utils.getPropValue(ballotGenProps, CONTEST_FILE_LEVEL));
		validateCommandLineArgs(args);
		validateWordTemplate();
		// very little validation here.
		validateElectionType();
		validateEndorsedParty();
		validateContestFileLevel();
		validateFormatsText();
	}
	
	

}
