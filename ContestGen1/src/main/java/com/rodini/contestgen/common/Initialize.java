package com.rodini.contestgen.common;

import java.util.List;
import java.util.Properties;
import java.util.regex.Pattern;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import static org.apache.logging.log4j.Level.DEBUG;

import static com.rodini.contestgen.common.ContestGenOutput.BOTH;
//import static com.rodini.voteforprocessor.extract.Initialize.start;
import com.rodini.ballotutils.Utils;
import com.rodini.contestgen.ContestGen1;

/**
 * Initialize class has the job of validating program inputs as thoroughly
 * as possible.  This includes CLI parameters and Property file values.
 * 
 * @author Bob Rodini
 *
 */
public class Initialize {

	static final Logger logger = LogManager.getLogger(Initialize.class);
//  Global variables here.
	public static String specimenText; // text of the Voter Services specimen.
	public static String outContestPath;		// path to contest output directory
	public static String outBallotPath;		// path to ballot output directory
	public static final String RESOURCE_PATH = "./resources/";
	public static final String PROPS_FILE = "contestgen.properties";
	public static Properties contestGenProps;
	public static int     precinctNameRepeatCount;
	// All regexes are compiled after loading
	public static Pattern precinctNameRegex;
//	NEW - Next four variables are used by v1.7.0+
	public static Pattern precinctPageBreakRegex;
	public static Pattern precinctOnePageRegex;
	public static Pattern precinctTwoPage1Regex;
	public static Pattern precinctTwoPage2Regex;

	public static Pattern [] contestRegex;
	public static Pattern referendumRegex;
	public static Pattern retentionQuestionRegex;
	public static Pattern retentionNameRegex;
	public static Pattern electionNameRegex;
	public static boolean precinctNoNameFileName;
	public static String  writeIn;  	// Write-in vs. Write-In
	public static String  endorsementsAllFileName;
	public static ContestGenOutput contestGenOutput;

//	Property names within contestgen.properties. 
//  Notes:
//  1. External names preserved for backwards compatibility, e.g. muniNameRepeatCount NOT precinctNameRepeatCount
//  2. All properties are *county* specific, e.g. chester.muniNameRepeatCount
	private static final String PROP_ELECTION_NAME_REGEX = ".electionNameRegex";
	private static final String PROP_PRECINCT_NAME_REPEAT_COUNT = ".muniNameRepeatCount";
	private static final String PROP_PRECINCT_NAME_REGEX = ".muniNameRegex";
//  NEW - Next four regexes are used by v1.7.0+
	private static final String PROP_PRECINCT_PAGE_BREAK_REGEX = ".pageBreakRegex";
	private static final String PROP_PRECINCT_ONE_PAGE_REGEX = ".onePageRegex";
	private static final String PROP_PRECINCT_TWO_PAGE1_REGEX = ".twoPage1Regex";
	private static final String PROP_PRECINCT_TWO_PAGE2_REGEX = ".twoPage2Regex";
	private static final String PROP_PRECINCTNONAME_FILENAME = ".precinctNoName.fileName";
	private static final String PROP_ENDORSEMENTS_ALL_FILENAME = ".endorsements.all.fileName";
	public  static final String WRITE_IN = ".write.in";
	private static final String CONTESTGEN_OUTPUT = ".contestgen.output";
	/**
	 * validateCommandLineArgumenst checks the CLI args as follows:
	 * 1. Number are correct.
	 * 2. Paths are valid and of the right type.
	 * 
	 * @param args CLI args.
	 */
	static void validateCommandLineArgs(String [] args) {
		// check args[0] is present and is a TXT file
		String specimenFilePath = args[0];
		Utils.checkFileExists(specimenFilePath);
		if (!specimenFilePath.endsWith("txt")) {
			Utils.logFatalError("file \"" + specimenFilePath + "\" doesn't end with TXT extension.");
		}
		specimenText = Utils.readTextFile(specimenFilePath);		
		// check args[1] is present and a directory
		outContestPath = args[1];
		Utils.checkDirExists(outContestPath);
		// check args[2] is present and a directory
		outBallotPath = args[2];
		Utils.checkDirExists(outBallotPath);
	}
	/**
	 * validateBooleanProperty looks for the given property by name and 
	 * validates that the string value converts to a boolean.
	 * 
	 * @param props Properties object
	 * @param propName property name
	 * @param defaultVal value to use in case missing or conversion fails.
	 * @return property value as boolean.
	 */
	static boolean validateBooleanProperty(Properties props, String propName, boolean defaultVal) {
		boolean val = defaultVal;
		String propVal = Utils.getPropValue(props, propName);
		if (propVal == null || propVal.isBlank()) {
			logger.error(String.format("property %s is missing.", propName));
		} else {
			try {
				val = Boolean.parseBoolean(propVal);
			} catch (NumberFormatException ex) {
				logger.error(String.format("property %s is not a boolean. See: %s", propName, propVal));
			}
		}
		return val;
	}
	/**
	 * validateIntProperty looks for the given property by name and 
	 * validates that the string value converts to a number.
	 * 
	 * @param props Properties object
	 * @param propName property name
	 * @param defaultVal value to use in case missing or conversion fails.
	 * @return property value as int.
	 */
	static int validateIntProperty(Properties props, String propName, int defaultVal) {
		int val = defaultVal;
		String propVal = Utils.getPropValue(props, propName);
		if (propVal == null || propVal.isBlank()) {
			logger.error(String.format("property %s is missing.", propName));
		} else {
			try {
				val = Integer.parseInt(propVal);
			} catch (NumberFormatException ex) {
				logger.error(String.format("property %s is not an integer. See: %s", propName, propVal));
			}
		}
		return val;
	}
	/**
	 * validateRegexProperty looks for the given property by name and
	 * validates that the string value can be compiled as a regex Pattern.
	 * 
	 * @param props Properties object
	 * @param propName property name
	 * @return property value compiled as Pattern.
	 */
	static Pattern validateRegexProperty(Properties props, String propName) {
		Pattern pat = null;
		String propVal = Utils.getPropValue(props, propName);
		if (propVal == null || propVal.isBlank()) {
			logger.error(String.format("property %s is missing.", propName));
		} else {
			pat = Utils.compileRegex(propVal);
		}
		return pat;
	}
	/**
	 * validateOrderedRegexProperties matches Utils.getPropOrderedValues logic
	 * which finds a sequence of property names that start with the same prefix.
	 * there is not a whole lot of validation, but the regexes must compile.
	 * 
	 * @param props Properties object
	 * @param propPrefix String prefix for sequence of related regex properties
	 * @return List of compile Pattern objects.
	 */
	static Pattern [] validateOrderedRegexProperties(Properties props, String propPrefix) {
		List<String> regexList = Utils.getPropOrderedValues(props, propPrefix);
		Pattern [] patList = new Pattern [regexList.size()];
		for (int i = 0; i < regexList.size(); i++) {
			patList[i] = Utils.compileRegex(regexList.get(i));
		}
		return patList;
	}
	/**
	 * validateContestGenOutput validates the CONTESTGEN_OUTPUT property.
	 * This controls how much output ContestGen writes to files.
	 * 
	 * @param props Properties object
	 * @param propName CONTESTGEN_OUTPUT.
	 */
	static void validateContestGenOutput(Properties props, String propName) {
		String propValue;
		propValue = Utils.getPropValue(props, propName);
		contestGenOutput = ContestGenOutput.toEnum(propValue);
		if (contestGenOutput == null) {
			contestGenOutput = BOTH;
		}
	}
	/**
	 * validateProperties loads and then validates properties needed by ContestGen1.
	 * The most important properties are the Regular Expressions (regexes) that
	 * are used to isolate sections of text that need to be processed.
	 */
	public static void validateProperties(Properties props) {
//		Properties props = Utils.loadProperties(RESOURCE_PATH + PROPS_FILE);
//		Utils.logProperties(logger, DEBUG, props);
		electionNameRegex = validateRegexProperty(props, ContestGen1.COUNTY + PROP_ELECTION_NAME_REGEX);
		precinctNameRepeatCount = validateIntProperty(props, ContestGen1.COUNTY + PROP_PRECINCT_NAME_REPEAT_COUNT, 2);
	    precinctNameRegex = validateRegexProperty(props, ContestGen1.COUNTY + PROP_PRECINCT_NAME_REGEX);
	    precinctPageBreakRegex = validateRegexProperty(props, ContestGen1.COUNTY + PROP_PRECINCT_PAGE_BREAK_REGEX);
	    precinctOnePageRegex = validateRegexProperty(props, ContestGen1.COUNTY + PROP_PRECINCT_ONE_PAGE_REGEX);
	    precinctTwoPage1Regex = validateRegexProperty(props, ContestGen1.COUNTY + PROP_PRECINCT_TWO_PAGE1_REGEX);
	    precinctTwoPage2Regex = validateRegexProperty(props, ContestGen1.COUNTY + PROP_PRECINCT_TWO_PAGE2_REGEX);
	    // start the VoteFor processor with contestgen properties
	    com.rodini.voteforprocessor.extract.Initialize.start(props);
	    precinctNoNameFileName = validateBooleanProperty(props, ContestGen1.COUNTY + PROP_PRECINCTNONAME_FILENAME, true);
	    writeIn= Utils.getPropValue(props, ContestGen1.COUNTY + WRITE_IN);
	    endorsementsAllFileName = Utils.getPropValue(props, ContestGen1.COUNTY + PROP_ENDORSEMENTS_ALL_FILENAME);
	    // chester.contestgen.output=
	    validateContestGenOutput(props, ContestGen1.COUNTY + CONTESTGEN_OUTPUT);
	}
	/** 
	 * start the initialization process.
	 * @param args
	 */
	public static void start(String[] args) {
		validateCommandLineArgs(args);
		contestGenProps = Utils.loadProperties(RESOURCE_PATH + PROPS_FILE);
		Utils.logProperties(logger, DEBUG, contestGenProps);
		validateProperties(contestGenProps);
	}

}
