package com.rodini.ballotgen;

import static java.nio.file.LinkOption.NOFOLLOW_LINKS;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.rodini.ballotutils.Utils;
import com.rodini.zoneprocessor.GenMuniMap;
import com.rodini.zoneprocessor.Zone;
/** 
 * Initialize class gets the program ready to generate sample ballots.
 * It attempts to validate critical inputs and FAIL EARLY if things
 * are amiss.
 * 
 * @author Bob Rodini
 *
 */
public class Initialize {
	private static final Logger logger = LoggerFactory.getLogger(Initialize.class);
	// Global variables
	public static ElectionType elecType;	// e.g. PRIMARY or GENERAL
	public static Party endorsedParty;		// e.g. Democratic (or NULL)
	public static List<String> ballotFiles; // e.g. List: "Atglen_VS.txt","Avondale_VS.txt",..,"West_Chester_7_VS.txt"
	public static String ballotContestsPath;
	public static String formatsText;		// read from properties file
	public static String msWordTemplateFile = "";	// MS Word template file
	public static ContestFileLevel contestLevel; 	// e.g. COMMON or MUNICIPAL
	public static ENVIRONMENT env;			// TEST vs. PRODUCTION
	public static Properties ballotGenProps;
	public static Properties contestGenProps;
	private static Map<String, Zone> precinctToZoneMap;
	        static Map<String, List<Endorsement>> candidateEndorsements;
	public static EndorsementProcessor endorsementProcessor;
	public static boolean writeInDisplay;
	public static int [] columnBreaks = {999};
	

//	ATTENTION: Within Eclipse you must put ./resources in the dependencies
	        static final String RESOURCE_PATH = "./resources/";
	        static final String CONTESTGEN_RESOURCE_PATH = "../contestgen/resources/";
	        static final String PROPS_FILE = "ballotgen.properties";
	        static final String CONTESTGEN_PROPS_FILE = "contestgen.properties";
//	Property names - must match names within ballotgen.properties
	private static final String PROP_ENDORSED_PARTY = "endorsed.party";
	private static final String PROP_WORD_TEMPLATE_DEFAULT = ".word.template.default";
	private static final String PROP_CONTEST_FORMAT_PREFIX = ".contest.format";
	private static final String CONTEST_FILE_LEVEL = "contest.file.level";
	        static final String COMMON_CONTESTS_FILE = "common_contests.txt";
	private static final String PRECINCT_TO_ZONE_FILE = ".precinct.to.zone.file";
	private static final String ENDORSEMENTS_FILE = ".endorsements.file";
	private static final String WRITE_IN_DISPLAY = ".write.in.display";
	private static final String COlUMN_BREAK_CONTEST_COUNT = ".column.break.contest.count";
	        static       String COUNTY;
	        static       String WRITE_IN;
	

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
			Utils.logFatalError("missing CLI arguments:\n" +
					"args[0]: path to directory for generated municipal level \"NNN_XYZ_contests.docx\" files.\n" + 
					"args[1]: path to directory municipal level \"NNN_XYZ_contests.docx\" files.");
		} else {
			String msg0 = String.format("output  dir: %s", args[0]);
			String msg1 = String.format("contest dir: %s", args[1]);
			System.out.println(msg0);
			System.out.println(msg1);
			logger.info(msg0);
			logger.info(msg1);
		}
		String ballotFilePath = args[0];
		processBallotFiles(ballotFilePath);
		ballotContestsPath = args[1];
		processBallotContests(ballotContestsPath);
		msWordTemplateFile = RESOURCE_PATH + Utils.getPropValue(ballotGenProps, COUNTY + PROP_WORD_TEMPLATE_DEFAULT);
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
				Utils.logFatalError("invalid args[0] value, not a file or directory: " + ballotFilePath);
			}
		} catch (SecurityException e) {
			Utils.logFatalError("can't access this file/directory: " + ballotFilePath);
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
			Utils.logFatalError("invalid args[1] value, ballotContestsPath does not exist: " + ballotContestsPath);
		}
		String commonFilePath = ballotContestsPath + File.separator + COMMON_CONTESTS_FILE;
		if (!Files.exists(Path.of(commonFilePath), NOFOLLOW_LINKS)) {
			Utils.logFatalError("can't find \"" + COMMON_CONTESTS_FILE + "\" file here: " + commonFilePath);
		}
	}
	/**
	 * validateElectionType get/display election type.
	 */
	static void validateElectionType() {
		String type = Utils.getPropValue(ballotGenProps, "election.type");
		logger.info(String.format("election.type: %s", type));
		elecType = ElectionType.toEnum(type);
	}
	/**
	 * validateEndorsedParty get/display the endorsed party.
	 */
	static void validateEndorsedParty() {
		String endorsedPartyString = Utils.getPropValue(ballotGenProps, PROP_ENDORSED_PARTY);
		logger.info(String.format("endorsed.party: %s", endorsedPartyString));
		endorsedParty = endorsedPartyString.isEmpty()? null : Party.toEnum(endorsedPartyString);
	}
	/**
	 * validateContestFileLevel get/display the contest granularity.
	 */
	static void validateContestFileLevel() {
		String level = Utils.getPropValue(ballotGenProps, CONTEST_FILE_LEVEL);
		logger.info(String.format("%s: %s", CONTEST_FILE_LEVEL, level));
		contestLevel = ContestFileLevel.valueOf(level);
	}
	/**
	 * validateFormatsText reads the formats (regexes) from the properties
	 * file and gets them into a long string that ContestFactory
	 * expects.
	 */
	static void validateFormatsText() {
		// This is a good place to read the Write-in string value.
		WRITE_IN = Utils.getPropValue(contestGenProps, COUNTY + ".write.in");
		List<String> formatLines = Utils.getPropOrderedValues(contestGenProps, COUNTY + ".ballotgen.contest.format");
		formatsText = formatLines.stream()
				.collect(joining("\n"));
		logger.info(String.format("formatsText:%n%s%n", formatsText));
	}
	/**
	 * validateContestFormats reads the contest formats (regexes) from the properties
	 * file and displays them.
	 */
	static void validateContestFormats() {
		String format;
		int count = 1;
		do {
			// Don't know how many there will be
			String key = Integer.toString(count);
			String propName =  COUNTY + PROP_CONTEST_FORMAT_PREFIX + "." + key;
			format = Utils.getPropValue(contestGenProps, propName);
			logger.info(String.format("%s.%d: %s", propName, count, format));
			if (format != null) {
				count++;				
			}
		} while (format != null);
		logger.info(String.format("there are %d contest formats in the contestgen properties file", count-1));
	}
	/**
	 * validateWordTemplate validates the existence of the Word template file.
	 */
	static void validateWordTemplate() {
		if (msWordTemplateFile.isEmpty()) {
			Utils.logFatalError("MS Word template file not specified (blank)");
		}
		if (!Files.exists(Path.of(msWordTemplateFile))) {
			Utils.logFatalError("MS Word template file does not exist: " + msWordTemplateFile);
		}
		if (!msWordTemplateFile.endsWith(".dotx")) {
			Utils.logFatalError("MS Word template file should end with \"dotx\": " + msWordTemplateFile);
		}
	}
	/**
	 * validatePrecinctZoneFile validates the existence of the zones to precincts file.
	 */
	static void validatePrecinctZoneFile() {
		String precinctZoneFile = Utils.getPropValue(ballotGenProps, COUNTY + PRECINCT_TO_ZONE_FILE);
		logger.info(String.format("%s: %s", COUNTY + PRECINCT_TO_ZONE_FILE, precinctZoneFile));
		String precinctZoneCSVText = "";
		if (!Utils.checkFileExists(precinctZoneFile)) {
			logger.info(String.format("%s does not exist: %s ", COUNTY + PRECINCT_TO_ZONE_FILE, precinctZoneFile));
			logger.info("Cannot endorse at the zone/precinct level.");
		} else {
			logger.info(String.format("%s: %s ", COUNTY + PRECINCT_TO_ZONE_FILE, precinctZoneFile));
			precinctZoneCSVText = Utils.readTextFile(precinctZoneFile);
		}
		GenMuniMap.processCSVText(precinctZoneCSVText);
		precinctToZoneMap = GenMuniMap.getMuniNoMap();
	}
	/**
	 * validateEndorsementsFile validates the existence of the endorsements file.
	 */
	static void validateEndorsementsFile() {
		String endorsementsFile = Utils.getPropValue(ballotGenProps, COUNTY + ENDORSEMENTS_FILE);
		logger.info(String.format("%s: %s", COUNTY + ENDORSEMENTS_FILE, endorsementsFile));
		String endorsementsCSVText = "";
		if (!Utils.checkFileExists(endorsementsFile)) {
			logger.info(String.format("%s does not exist: %s ", COUNTY + ENDORSEMENTS_FILE, endorsementsFile));
			logger.info("Can only endorse at the party level in general elections.");
		} else {
			logger.info(String.format("%s: %s ", COUNTY + ENDORSEMENTS_FILE, endorsementsFile));
			endorsementsCSVText = Utils.readTextFile(endorsementsFile);
		}
		EndorsementFactory.processCSVText(endorsementsCSVText);
		candidateEndorsements = EndorsementFactory.getCandidateEndorsements();
	}
	/**
	 * validateWriteInDisplay reads/displays the WRITE_IN_DISPLAY property value.
	 */
	static void validateWriteInDisplay() {
		String value = ballotGenProps.getProperty(COUNTY + WRITE_IN_DISPLAY);
		if (value == null) {
			value = "false";
		}
		writeInDisplay = Boolean.parseBoolean(value);
		logger.info(String.format("%s: %s", COUNTY + WRITE_IN_DISPLAY, value));
	}
	/**
	 * validateColumnBreakContestCount reads/displays the COlUMN_BREAK_CONTEST_COUNT property value.
	 */
	static void validateColumnBreakContestCount() {
		String value = ballotGenProps.getProperty(COUNTY + COlUMN_BREAK_CONTEST_COUNT);
		if (value == null) {
			// default is contest # 999, so no harm done.
			return;
		}
		String [] counts = value.split(",");
		// Each value should be an integer
		int i = 0;
		int preVal = -1;
		// Resize as per the property length + one for sentinel.
		columnBreaks = new int[counts.length+1];
		for (int j = 0; j < counts.length; j++) {
			String strVal = counts[j].trim();
			int val;
			try {
				val = Integer.parseInt(strVal);
			} catch (NumberFormatException e) {
				logger.error(String.format("bad %s property: %s", COUNTY + COlUMN_BREAK_CONTEST_COUNT, value));
				return;
			}
			if (val < preVal) {
				logger.error(String.format("bad %s property: %s", COUNTY + COlUMN_BREAK_CONTEST_COUNT, value));
				return;
			}
			columnBreaks[i++] = val;

		}
		// sentinel value
		columnBreaks[i] = 999;
		logger.info(String.format("%s: %s", COUNTY + COlUMN_BREAK_CONTEST_COUNT, value));
	}
	/**
	 * start begins the initialization process.
	 * @param args CLI arguments
	 */
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
		validateContestFormats();
		validatePrecinctZoneFile();
		validateEndorsementsFile();
		// create the endorsement processor
		endorsementProcessor  = new EndorsementProcessor(elecType, endorsedParty,
				candidateEndorsements, precinctToZoneMap);
		validateWriteInDisplay();
		validateColumnBreakContestCount();
	}
	
	

}
