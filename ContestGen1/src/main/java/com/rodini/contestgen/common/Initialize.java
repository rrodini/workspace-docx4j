package com.rodini.contestgen.common;

import java.util.List;
import java.util.Properties;
import java.util.regex.Pattern;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import static org.apache.logging.log4j.Level.DEBUG;

import com.rodini.contestgen.common.ContestGenOutput;
import static com.rodini.contestgen.common.ContestGenOutput.BOTH;
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
//	static Properties props;
	public static final String RESOURCE_PATH = "./resources/";
	public static final String PROPS_FILE = "contestgen.properties";
	public static int     precinctNameRepeatCount;
	// All regexes are compiled after loading
	public static Pattern precinctNameRegex;
	public static int     precinctBallotPageCount;
	public static Pattern precinctBallotPage1Regex;
	public static Pattern precinctBallotPage2Regex;
	public static Pattern [] contestRegex;
	public static Pattern referendumRegex;
	public static Pattern retentionQuestionRegex;
	public static Pattern retentionNameRegex;
	public static String  writeIn;  	// Write-in vs. Write-In
	public static ContestGenOutput contestGenOutput;

//	Property names within contestgen.properties. 
//  Notes:
//  1. External names preserved for backwards compatibility, e.g. muniNameRepeatCount NOT precinctNameRepeatCount
//  2. All properties are *county* specific, e.g. chester.muniNameRepeatCount
	private static final String PROP_PRECINCT_NAME_REPEAT_COUNT = ".muniNameRepeatCount";
	private static final String PROP_PRECINCT_NAME_REGEX = ".muniNameRegex";
	private static final String PROP_PRECINCT_BALLOT_PAGE_COUNT = ".muniTextPageCount";
	private static final String PROP_PRECINCT_BALLOT_PAGE1_REGEX = ".muniTextPage1Regex";
	private static final String PROP_PRECINCT_BALLOT_PAGE2_REGEX = ".muniTextPage2Regex";
	private static final String PROP_CONTEST_REGEX    = ".contest.format";
	private static final String PROP_REFERENDUM_REGEX = ".referendum.format";
	private static final String PROP_RETENTION_QUESTION_REGEX  = ".retention.question.format";
	private static final String PROP_RETENTION_NAME_REGEX      = ".retention.name.format";
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
	 * validateIntProperty looks for the given property by name and 
	 * validates that the string value converts to an number.
	 * 
	 * @param props Properties object
	 * @param propName property name
	 * @param defaultVal value to use in case conversion fails.
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
	static void validateProperties() {
		Properties props = Utils.loadProperties(RESOURCE_PATH + PROPS_FILE);
		Utils.logProperties(logger, DEBUG, props);
		// chester.muniNameRepeatCount=2
	    precinctNameRepeatCount = validateIntProperty(props, ContestGen1.COUNTY + PROP_PRECINCT_NAME_REPEAT_COUNT, 2);
		// chester.muniNameRegex=regex
	    precinctNameRegex = validateRegexProperty(props, ContestGen1.COUNTY + PROP_PRECINCT_NAME_REGEX);
		// chester.muniTextPageCount=2
	    precinctBallotPageCount = validateIntProperty(props, ContestGen1.COUNTY + PROP_PRECINCT_BALLOT_PAGE_COUNT, 2);
		// chester.muniTextPage1Regex=
	    precinctBallotPage1Regex = validateRegexProperty(props, ContestGen1.COUNTY + PROP_PRECINCT_BALLOT_PAGE1_REGEX);
		// chester.muniTextPage2Regex=
	    precinctBallotPage2Regex = validateRegexProperty(props, ContestGen1.COUNTY + PROP_PRECINCT_BALLOT_PAGE2_REGEX);
	    // chester.contest.format.1, chester.contest.format.2, ...
	    contestRegex = validateOrderedRegexProperties(props, ContestGen1.COUNTY + PROP_CONTEST_REGEX);
	    // chester.referendum.format
	    referendumRegex = validateRegexProperty(props, ContestGen1.COUNTY + PROP_REFERENDUM_REGEX);
	    // chester.retention.question.format=
	    retentionQuestionRegex = validateRegexProperty(props, ContestGen1.COUNTY + PROP_RETENTION_QUESTION_REGEX);
	    // chester.retention.name.format=
	    retentionNameRegex = validateRegexProperty(props, ContestGen1.COUNTY + PROP_RETENTION_NAME_REGEX);
	    // chester.write.in=
	    writeIn= Utils.getPropValue(props, ContestGen1.COUNTY + WRITE_IN);
	    // chester.contestgen.output=
	    validateContestGenOutput(props, ContestGen1.COUNTY + CONTESTGEN_OUTPUT);
	}
	/** 
	 * start the initialization process.
	 * @param args
	 */
	public static void start(String[] args) {
		validateCommandLineArgs(args);
		validateProperties();
	}

}
