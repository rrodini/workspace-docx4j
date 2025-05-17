package com.rodini.ballotgen.common;

import static com.rodini.ballotgen.common.Initialize.LOCAL_CONTEST_EXCEPTION_NAMES;
import static com.rodini.ballotgen.common.Initialize.TICKET_CONTEST_NAMES;
import static com.rodini.ballotgen.common.Initialize.ballotGenProps;
import static com.rodini.ballotgen.common.BallotReportParser.parseBallotReport;
import static com.rodini.ballotgen.common.BallotGenOutput.*;

import static java.nio.file.LinkOption.NOFOLLOW_LINKS;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.stream.Stream;

//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.rodini.ballotgen.contest.ContestFileLevel;
import com.rodini.ballotgen.endorsement.Endorsement;
import com.rodini.ballotgen.endorsement.EndorsementFactory;
import com.rodini.ballotgen.endorsement.EndorsementProcessor;
import com.rodini.ballotgen.writein.Writein;
import com.rodini.ballotgen.writein.WriteinFactory;
import com.rodini.ballotgen.writein.WriteinProcessor;
import com.rodini.ballotutils.Utils;
import com.rodini.ballotutils.ElectionType;
import com.rodini.ballotutils.Party;
import com.rodini.zoneprocessor.ZoneProcessor;
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
	private static final Logger logger = LogManager.getLogger(Initialize.class);
	// Global variables
	public static ElectionType elecType;	// e.g. PRIMARY or GENERAL
	public static Party endorsedParty;		// e.g. Democratic (or NULL)
	public static List<String> ballotFiles; // e.g. List: "Atglen_VS.txt","Avondale_VS.txt",..,"West_Chester_7_VS.txt"
	public static String ballotContestsPath;
	public static String formatsText;		// read from properties file
	public static String referendumFormat;		// read from properties file
	public static String retentionFormat;		// read from properties file
	public static String msWordPrecinctTemplateFile = "";	// MS Word precinct template file
	public static String msWordUniqueTemplateFile = "";	    // MS Word unique template file
	// 10/29/24 - contestLevel not used.  Kept solely for backward compatibility.
	public static ContestFileLevel contestLevel; 	// e.g. COMMON or MUNICIPAL
	public static ENVIRONMENT env;			// TEST vs. PRODUCTION
	public static Properties ballotGenProps;
	public static Properties contestGenProps;
		   static Map<String, Zone> precinctToZoneMap;
	       static Map<String, List<Endorsement>> candidateEndorsements;
	       static Map <String,List<Writein>> precinctWriteins;
	public static EndorsementProcessor endorsementProcessor;
	public static WriteinProcessor writeinProcessor;
	public static boolean writeInDisplay;
	public static boolean pageBreakDisplay;
	public static String columnBreaks = "ZZZZ"; // sentinel value
	public static BallotGenOutput ballotGenOutput;
	public static List<String> uniqueFirstBallotFile; // ballot file that triggers unique_ballot_xx.docx
	public static Map<Integer, List<String>> uniqueBallotFiles; // precinctNoNames that belong to a unique ballot.
	

//	ATTENTION: Within Eclipse you must put ./resources in the dependencies
	public  static final String RESOURCE_PATH = "./resources/";
	public  static final String CONTESTGEN_RESOURCE_PATH = "../contestgen/resources/";
	public  static final String PROPS_FILE = "ballotgen.properties";
	public  static final String CONTESTGEN_PROPS_FILE = "contestgen.properties";
//	Property names - must match names within ballotgen.properties
	private static final String PROP_ELECTION_TYPE = "election.type";
	private static final String PROP_ENDORSED_PARTY = "endorsed.party";
	private static final String PROP_WORD_TEMPLATE_DEFAULT = ".word.template.default";
	private static final String PROP_WORD_TEMPLATE_UNIQUE = ".word.template.unique";
	private static final String PROP_CONTEST_FORMAT_PREFIX = ".contest.format";
	private static final String PROP_REFERENDUM_FORMAT = ".ballotgen.referendum.format";
	private static final String PROP_RETENTION_FORMAT = ".ballotgen.retention.format";
	
	private static final String CONTEST_FILE_LEVEL = "contest.file.level";
	public  static final String COMMON_CONTESTS_FILE = "common_contests.txt";
	private static final String PRECINCT_TO_ZONE_FILE = ".precinct.to.zone.file";
	private static final String ENDORSEMENTS_FILE = ".endorsements.file";
	private static final String WRITEINS_FILE = ".write.ins.file";
	private static final String WRITE_IN_DISPLAY = ".write.in.display";
	private static final String COlUMN_BREAK_CONTEST_COUNT = ".column.break.contest.count";
	private static final String COlUMN_BREAK_CONTEST_NAME = ".column.break.contest.name";
	public	static       String COUNTY;
	public  static       String WRITE_IN;
	public  static final String PAGE_BREAK = "PAGE_BREAK"; // pseudo contest name
	public  static       String PAGE_BREAK_WORDING;
	public  static final String TICKET_CONTEST_NAMES = "ticket.contest.names";
	public  static final String LOCAL_CONTEST_NAMES = "local.contest.names";
	public  static final String LOCAL_CONTEST_EXCEPTION_NAMES = "local.contest.exception.names";
	public  static       List<String> namesOfTicketContests;
	public  static       List<String> namesOfLocalContests;
	public  static       List<String> namesOfLocalContestsExceptions;
	public  static	     BallotGenOutput ballotgenOutput = PRECINCT;  // Default to PRECINCT ballots
	public  static final String BALLOTGEN_OUTPUT = "ballotgen.output";
	public  static final String BALLOT_SUMMARY_FILE = "Ballot_Summary.txt";

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
		msWordPrecinctTemplateFile = Utils.getPropValue(ballotGenProps, COUNTY + PROP_WORD_TEMPLATE_DEFAULT);
		msWordUniqueTemplateFile = Utils.getPropValue(ballotGenProps, COUNTY + PROP_WORD_TEMPLATE_UNIQUE);
		logger.info("msWordPrecinctTemplateFile: " + msWordPrecinctTemplateFile);
		logger.info("msWordUniqueTemplateFile: " + msWordUniqueTemplateFile);
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
						.sorted()  // alphabetic order by file name
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
		// 06/13/2023 Dropped support for common_contests.txt
//		String commonFilePath = ballotContestsPath + File.separator + COMMON_CONTESTS_FILE;
//		if (!Files.exists(Path.of(commonFilePath), NOFOLLOW_LINKS)) {
//			Utils.logFatalError("can't find \"" + COMMON_CONTESTS_FILE + "\" file here: " + commonFilePath);
//		}
	}
	/**
	 * validateElectionType get/display election type.
	 */
	static void validateElectionType() {
		String type = Utils.getPropValue(contestGenProps, PROP_ELECTION_TYPE);
		logger.info(String.format("election.type: %s", type));
		elecType = ElectionType.toEnum(type);
	}
	/**
	 * validateEndorsedParty get/display the endorsed party.
	 */
	static void validateEndorsedParty() {
		String endorsedPartyString = Utils.getPropValue(contestGenProps, PROP_ENDORSED_PARTY);
		logger.info(String.format("endorsed.party: %s", endorsedPartyString));
		endorsedParty = endorsedPartyString.isEmpty()? null : Party.toEnum(endorsedPartyString);
	}
	/**
	 * validateContestFileLevel get/display the contest granularity.
	 */
	static void validateContestFileLevel() {
		String level = Utils.getPropValue(ballotGenProps, CONTEST_FILE_LEVEL);
		logger.info(String.format("%s: %s", CONTEST_FILE_LEVEL, level));
		// Line below deactivated since property is OBSOLETE.
//		contestLevel = ContestFileLevel.valueOf(level);
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
	
	static void validateReferendumFormat() {
		// referendumFormat is a regex.
		String propName =  COUNTY + PROP_REFERENDUM_FORMAT;
		referendumFormat = Utils.getPropValue(contestGenProps, propName);
		logger.info(String.format("%s: %s", propName, referendumFormat));
	}
	
	static void validateRetentionFormat() {
		// retentionFormat is a regex.
		String propName =  COUNTY + PROP_RETENTION_FORMAT;
		retentionFormat = Utils.getPropValue(contestGenProps, propName);
		logger.info(String.format("%s: %s", propName, retentionFormat));
	}
	static void validateWordTemplate(String templateFile, String which) {
		if (templateFile.isEmpty()) {
			Utils.logFatalError(String.format("MS Word %s template file not specified (blank)", which));
		}
		if (!Files.exists(Path.of(templateFile))) {
			Utils.logFatalError(String.format("MS Word %s template file does not exist: %s", which, templateFile));
		}
		if (!templateFile.endsWith(".dotx")) {
			Utils.logFatalError(String.format("MS Word %s template file should end with \"dotx\": %s", which, templateFile));
		}
	}
	
	/**
	 * validateWordTemplates validates the existence of the Word template files.
	 */
	static void validateWordTemplates() {
		validateWordTemplate(msWordPrecinctTemplateFile, "PRECINCT");
		validateWordTemplate(msWordUniqueTemplateFile, "UNIQUE");
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
			precinctZoneCSVText = Utils.readTextFile(precinctZoneFile);
		}
		ZoneProcessor.processCSVText(precinctZoneCSVText);
		precinctToZoneMap = ZoneProcessor.getPrecinctZoneMap();
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
	 * validateWriteinsFile validates the existence of the Write-ins file.
	 */
	static void validateWriteinsFile() {
		String writeinsFile = Utils.getPropValue(ballotGenProps, COUNTY + WRITEINS_FILE);
		logger.info(String.format("%s: %s", COUNTY + WRITEINS_FILE, writeinsFile));
		String writeinsCSVText = "";
		if (!Utils.checkFileExists(writeinsFile)) {
			logger.info(String.format("%s does not exist: %s ", COUNTY + WRITEINS_FILE, writeinsFile));
		} else {
			logger.info(String.format("%s: %s ", COUNTY + WRITEINS_FILE, writeinsFile));
			writeinsCSVText = Utils.readTextFile(writeinsFile);
		}
		// must set this map BEFORE processing CSV file.
		WriteinFactory.setPrecinctToZones(precinctToZoneMap);
		WriteinFactory.processCSVText(writeinsCSVText);
		precinctWriteins = WriteinFactory.getPrecinctWriteins();
	}

	/**
	 * validateWriteInDisplay reads/displays the WRITE_IN_DISPLAY property value.
	 */
	static void validateWriteInDisplay() {
		String value = Utils.getPropValue(ballotGenProps,COUNTY + WRITE_IN_DISPLAY);
		if (value == null) {
			value = "false";
		}
		writeInDisplay = Boolean.parseBoolean(value);
		logger.info(String.format("%s: %s", COUNTY + WRITE_IN_DISPLAY, value));
	}
	/**
	 * validateColumnBreakContestNAME reads/displays the COlUMN_BREAK_CONTEST_NAME property value.
	 */
	static void validateColumnBreakContestName() {
		String value = Utils.getPropValue(ballotGenProps, COUNTY + COlUMN_BREAK_CONTEST_NAME);
		if (value == null) {
			value = "";
			return;
		}
		columnBreaks = value;
		logger.info(String.format("%s: %s",  COUNTY + COlUMN_BREAK_CONTEST_NAME, columnBreaks));
	}
	/**
	 * validateColumnBreakContestCount reads/displays the COlUMN_BREAK_CONTEST_COUNT property value.
	 * Note:
	 * - OBSOLETE: Superseded by COlUMN_BREAK_CONTEST_COUNT property value.
	 */
	static void validateColumnBreakContestCount() {
		String value = Utils.getPropValue(ballotGenProps, COUNTY + COlUMN_BREAK_CONTEST_COUNT);
		if (value == null) {
			// default is contest # 999, so no harm done.
			return;
		}
		columnBreaks = value;
//		String [] counts = value.split(",");
//		// Each value should be an integer
//		int i = 0;
//		int preVal = -1;
//		// Resize as per the property length + one for sentinel.
//		columnBreaks = new int[counts.length+1];
//		for (int j = 0; j < counts.length; j++) {
//			String strVal = counts[j].trim();
//			int val;
//			try {
//				val = Integer.parseInt(strVal);
//			} catch (NumberFormatException e) {
//				logger.error(String.format("bad %s property: %s", COUNTY + COlUMN_BREAK_CONTEST_COUNT, value));
//				return;
//			}
//			if (val < preVal) {
//				logger.error(String.format("bad %s property: %s", COUNTY + COlUMN_BREAK_CONTEST_COUNT, value));
//				return;
//			}
//			columnBreaks[i++] = val;
//			preVal = val;
//		}
//		// sentinel value
//		columnBreaks[i] = 999;
//		logger.info(String.format("%s: %s", COUNTY + COlUMN_BREAK_CONTEST_COUNT, value));
	}
	
	static void validatePageBreak() {
		boolean display = true;
		String strDisplay = Utils.getPropValue(ballotGenProps,"page.break.display");
		if (strDisplay != null) {
			display = Boolean.valueOf(strDisplay);
		}
		pageBreakDisplay = display;
		logger.info(String.format("%s: %s", "pageBreakDisplay", Boolean.toString(display)));
		String value = Utils.getPropValue(ballotGenProps,"page.break.wording");
		if (value == null) {
			value = "Page Break";
		}
		PAGE_BREAK_WORDING = value;
		logger.info(String.format("%s: %s", "PAGE_BREAK_WORDING", value));
	}
	/**
	 * Used by CandidateFactory (of all things).
	 */
	static void validateTicketAndLocalContestNames() {
		String 	contestNames;
		contestNames = Utils.getPropValue(ballotGenProps, TICKET_CONTEST_NAMES);
		namesOfTicketContests = Arrays.asList(contestNames.split(","));
		contestNames = Utils.getPropValue(ballotGenProps, LOCAL_CONTEST_NAMES);
		namesOfLocalContests = Arrays.asList(contestNames.split(","));
		contestNames = Utils.getPropValue(ballotGenProps, LOCAL_CONTEST_EXCEPTION_NAMES);
		namesOfLocalContestsExceptions = Arrays.asList(contestNames.split(","));
	}
	static void validateBallotGenOutput() {
		String propValue;
		propValue = Utils.getPropValue(ballotGenProps, BALLOTGEN_OUTPUT);
		ballotGenOutput = BallotGenOutput.toEnum(propValue);
		if (ballotGenOutput == null) {
			ballotGenOutput = PRECINCT;
		}
	}
	/**
	 * validateBallotReport ensures that the Ballot_Report.txt file exists.
	 */
	static void validateBallotReport() {
		String ballotSummaryPath = Initialize.ballotContestsPath + File.separator + BALLOT_SUMMARY_FILE;
		if (!Utils.checkFileExists(ballotSummaryPath)) {
			Utils.logFatalError("Can't find file: " + ballotSummaryPath);
		}
		// Report is there, so parse it.
		BallotReportParser.parseBallotReport(ballotSummaryPath);
	}
	
	/**
	 * start begins the initialization process.
	 * @param args CLI arguments
	 */
	public static void start(String [] args) {
		ballotGenProps = Utils.loadProperties(RESOURCE_PATH + PROPS_FILE);
		contestGenProps = Utils.loadProperties(CONTESTGEN_RESOURCE_PATH + CONTESTGEN_PROPS_FILE);
		env = ENVIRONMENT.valueOf(Utils.getPropValue(ballotGenProps, "environment"));
//		contestLevel = ContestFileLevel.valueOf(Utils.getPropValue(ballotGenProps, CONTEST_FILE_LEVEL));
		validateCommandLineArgs(args);
		validateWordTemplates();
		// very little validation here.
		validateElectionType();
		validateEndorsedParty();
		validateContestFileLevel();
		validateFormatsText();
		validateContestFormats();
		validateReferendumFormat();
		validateRetentionFormat();
		validatePrecinctZoneFile();
		validateEndorsementsFile();
		validateWriteinsFile();
		validateBallotGenOutput();
		// create the endorsement processor
		endorsementProcessor  = new EndorsementProcessor(elecType, endorsedParty,
				candidateEndorsements, precinctToZoneMap);
		// create the write-in processor
		writeinProcessor = new WriteinProcessor(precinctWriteins);
		validateWriteInDisplay();
//		validateColumnBreakContestCount();
		validateColumnBreakContestName();
		validatePageBreak();
		validateTicketAndLocalContestNames();
		validateBallotReport();
	}
	
	

}
