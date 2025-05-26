package com.rodini.ballotgen.common;

import static com.rodini.ballotgen.common.BallotGenOutput.PRECINCT;
import static java.util.stream.Collectors.toList;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.stream.Stream;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.rodini.ballotgen.endorsement.Endorsement;
import com.rodini.ballotgen.endorsement.EndorsementFactory;
import com.rodini.ballotgen.endorsement.EndorsementProcessor;
import com.rodini.ballotgen.writein.Writein;
import com.rodini.ballotgen.writein.WriteinFactory;
import com.rodini.ballotgen.writein.WriteinProcessor;
import com.rodini.ballotutils.ElectionType;
import com.rodini.ballotutils.Party;
import com.rodini.ballotutils.Utils;
import com.rodini.zoneprocessor.Zone;
import com.rodini.zoneprocessor.ZoneProcessor;
/** 
 * Initialize class gets the program ready to generate sample ballots.
 * It attempts to validate critical inputs and FAIL EARLY if things
 * are amiss.
 * 
 * Note: Many property values are now read by voteforprocessor component.
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
	private static final String PROP_WORD_TEMPLATE_DEFAULT = ".word.template.default";
	private static final String PROP_WORD_TEMPLATE_UNIQUE = ".word.template.unique";
	
	private static final String PRECINCT_TO_ZONE_FILE = ".precinct.to.zone.file";
	private static final String ENDORSEMENTS_FILE = ".endorsements.file";
	private static final String WRITEINS_FILE = ".write.ins.file";
	private static final String COlUMN_BREAK_CONTEST_COUNT = ".column.break.contest.count";
	private static final String COlUMN_BREAK_CONTEST_NAME = ".column.break.contest.name";
	public	static       String COUNTY;
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
	}
	/**
	 * validateWordTemplate checks that there is a MS Word template (.dotx) file.
	 * @param templateFile .dotx file
	 * @param which PRECINC or UNIQUE template
	 */
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
		validateCommandLineArgs(args);
		validateWordTemplates();
		validatePrecinctZoneFile();
		validateEndorsementsFile();
		validateWriteinsFile();
		validateBallotGenOutput();
		// the votefor processor is separate component
		// but it must be inititialized.
		com.rodini.voteforprocessor.extract.Initialize.start(contestGenProps);
		// create the endorsement processor
		endorsementProcessor  = new EndorsementProcessor(elecType, endorsedParty,
				candidateEndorsements, precinctToZoneMap);
		// create the write-in processor
		writeinProcessor = new WriteinProcessor(precinctWriteins);
//		validateColumnBreakContestCount();
		validateColumnBreakContestName();
		validatePageBreak();
		validateBallotReport();
	}
	
	

}
