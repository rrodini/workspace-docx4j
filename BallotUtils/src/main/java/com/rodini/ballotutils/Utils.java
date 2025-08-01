package com.rodini.ballotutils;

import static java.nio.file.LinkOption.NOFOLLOW_LINKS;
import static java.util.stream.Collectors.joining;
import static org.apache.logging.log4j.Level.DEBUG;
import static org.apache.logging.log4j.Level.ERROR;
import static org.apache.logging.log4j.Level.INFO;
import static org.apache.logging.log4j.Level.TRACE;
import static org.apache.logging.log4j.Level.WARN;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.time.format.DateTimeFormatter;
import java.time.LocalDateTime;
// SLF4J
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;

// LOG4J
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.Appender;
import org.apache.logging.log4j.core.appender.FileAppender;
import org.apache.logging.log4j.core.config.LoggerConfig;
import org.apache.logging.log4j.spi.LoggerContext;

/**
 * Utils class contains common utility methods across all ballot
 * generation programs.  Subject to additions due to refactoring of workspace code.
 * 
 * @author Bob Rodini
 *
 */
public class Utils {

	private static final Logger logger = LogManager.getLogger(Utils.class);
	static final String JVM_LOG_LEVEL = "log.level";
	// Custom log4j level. Should always be logged.
	public final static Level ATTN = Level.forName("ATTN", 90);
	private static final int ZONE_STR_LEN = 2; 	// Normalized zoneNo is two digits
	private static final int PRECINCT_NO_STR_LEN = 3;	// Normalized precinctNo is three digits
	
	/** 
	 * Log a fatal error and stop the program.
	 * @param msg message to display / log.
	 */
	public static void logFatalError(String msg) {
		System.out.println(msg);
		logger.error(msg);
		System.exit(1);	// non-zero exit code means fatal error
	}
	// just for log4j
	/** 
	 * setLoggingLevel sets the logging LOG4J level dynamically. It
	 *  uses a non-public LOG4J API which seems to work.
	 *  
	 *  This method is typically called at program start in order to
	 *  set the level of the root logger.
	 *  
	 * @param loggerName name whose level to set.
	 */
	public static void setLoggingLevel(String loggerName) {
		Map<String, org.apache.logging.log4j.Level> logLevels = Map.of(
				"ERROR", ERROR,
				"WARN", WARN,
				"INFO", INFO,
				"DEBUG", DEBUG,
				"TRACE", TRACE,
				// custom LOG4J level
				"ATTN", ATTN
				);
		// get logging level from JVM argument
		String strLevel = System.getProperty(JVM_LOG_LEVEL, "DEFAULT").toUpperCase();
		Level level = logLevels.get(strLevel);
		if (level != null) {
			org.apache.logging.log4j.core.config.Configurator.setLevel(loggerName,level);
		} else {
			// use the current logger's level
			level = logger.getLevel();
		}
		// echo out for the world to see.
		String logMsg = String.format("log4jLevel: %s", level.toString());
		System.out.println(logMsg);
		logger.log(ATTN, logMsg);
	}
	//@formatter: off
	/**
	 * logAppMessage - logs an application message at ATTN level. An application message is typically logged
	 * at application startup or shutdown. Typical usage below:
	 * {@snippet :
	 * 	String version = Utils.getEnvVariable(ENV_BALLOTGEN_VERSION, true);
	 *  String msg = String.format("Start of BallotGen app. Version: %s", version);
	 *  Utils.logAppMessage(logger, msg, true);
	 * }
	 * 
	 * @param logger logger to use.
	 * @param msg message to log.
	 * @param addTime true => add date/time to the msg.
	 */
	//@formatter: on
	public static void logAppMessage(Logger logger, String msg, boolean addTime) {
		String dateTime = "";
		if (addTime) {
			dateTime = " " + getDateTimeString();
		}
		String totalMsg = msg + dateTime;
		logger.log(ATTN, totalMsg);
		System.out.println(totalMsg);
	}
	/**
	 * getLogFilePath queries the LOG4J API to get the name of the log file for
	 * the application. This code only works due to the convention that the appender
	 * is given the name "BallotGen" in all ballot generation applications.
	 * 
	 * @param logger Logger object.
	 * @return null or log file path.
	 */
	private static String getLogFilePath(Logger logger) {
		final String BALLOTGEN = "BallotGen";
	    FileAppender fileAppender = null;
		String logFilePath = null;
		Map<String, Appender> appenderMap = ((org.apache.logging.log4j.core.Logger) logger).getAppenders();
		Appender currAppender = appenderMap.get(BALLOTGEN);
	    if (currAppender instanceof FileAppender) {
	        fileAppender = (FileAppender) currAppender;
	    }
		if (fileAppender != null) {
		    logFilePath = fileAppender.getFileName();
		}
		return logFilePath;
	}
	/**
	 * logAppErrorCount logs the number of ERROR messages written to the log file.
	 * It is typically called at the end of a ballot generation program.
	 * 
	 * Note: There is no log4j API that does this, so scan the log file for entries
	 * that start "ERROR"
	 * 
	 * @param logger root logger.
	 */
	public static void logAppErrorCount(Logger logger) {
		String logFilePath = getLogFilePath(logger);
		if (logFilePath != null) {
			int errors = 0;
			String logFileText = readTextFile(logFilePath);
			Pattern pat = compileRegex("^ERROR.*");
			Matcher m = pat.matcher(logFileText);
			errors = (int) m.results().count();
			logAppMessage(logger, String.format("Error count: %d", errors), false);
		}
	}
	/**
	 * logLines - log the lines at the desired level.
	 * 
	 * @param logger logger object to use.
	 * @param level level to log at.
	 * @param heading heading for the lines to follow.
	 * @param lines lines to log.
	 */
	public static void logLines(Logger logger, Level level, String heading, String [] lines) {
		logger.log(level, heading);
		for (int i = 0; i < lines.length; i++) {
			logger.log(level, lines[i]);
		}
	}
	/**
	 * loadProperties loads the program's properties from a Java properties file.
	 * 
	 * @param resourcePath path to properties file.
	 * @return Properties object.
	 */
	public static Properties loadProperties(String resourcePath) {
		// get properties
		Properties props = new Properties();
		try (FileInputStream resourceStream = new FileInputStream(resourcePath);) {
			props.load(resourceStream);
			logger.info("properties file loaded from: " + resourcePath);
		} catch (Exception e) {
			logger.error("cannot load properties file: " + resourcePath);
		}
		return props;
	}
	// get a property value.
	/** 
	 * getPropValue gets a specific property value from a Properties object.
	 * 
	 * @param props Properties object.
	 * @param propName name of property.
	 * @return value (String) of property.
	 */
	public static String getPropValue(Properties props, String propName) {
		String value = props.getProperty(propName);
		if (value == null) {
			logger.info("property value not found for property name: " + propName);
		}
		return value;
	}
	/** 
	 * getPropOrderedValues gets a list of name-related properties.
	 * Example:  contest.format.1
	 *           contest.format.2
	 *           
	 * @param props Properties object.
	 * @param propNamePrefix name prefix for the related properties.
	 * @return List of name-related property values.
	 */
	public static List<String> getPropOrderedValues(Properties props, String propNamePrefix) {
		List<String> propValues = new ArrayList<>();
		int i = 1;
		String propName = propNamePrefix + "." + Integer.toString(i);
		String propValue = null;
		do {
			propValue = getPropValue(props, propName);
			if (propValue != null) {
				propValues.add(propValue);
				i++;
				propName = propNamePrefix + "." + Integer.toString(i);
			}
		} while (propValue != null);
		if (propValues.size() == 0 ) {
			logger.error("no property names starting with: " + propNamePrefix);
		}
		return propValues;
	}
	/**
	 * logProperties log the contents of a properties file. Can be used before
	 * or after all of the properties are validated.
	 * 
	 * @param logger logger to use.
	 * @param level logging level.
	 * @param props contents of a properties file.,
	 */
	public static void logProperties(Logger logger, Level level, Properties props) {
		Set<String> propNames = props.stringPropertyNames();
		logger.log(level, "Properties:");
		for (String propName: propNames) {
			logger.log(level, String.format("%s: %s", propName, props.getProperty(propName)));
		}
	}
	/**
	 * checkFileExists checks if a file exists. If it doesn't an ERROR is logged.
	 * 
	 * @param filePath path to the file.
	 * @return true/false reflecting existence.
	 */
	public static boolean checkFileExists(String filePath) {
		boolean exists = true;
		if (!Files.exists(Path.of(filePath), NOFOLLOW_LINKS)) {
			logger.error("file \"" + filePath + "\" does not exist");
			exists = false;
		}
		return exists;
	}
	/**
	 * checkDirExists checks if a directory exists. If it doesn't an ERROR is logged.
	 * 
	 * @param dirPath path to the file.
	 * @return true/false reflecting existence.
	 */
	public static boolean checkDirExists(String dirPath) {
		boolean exists = true;
		if (!Files.exists(Path.of(dirPath), NOFOLLOW_LINKS)) {
			logger.error("directory \"" + dirPath + "\" does not exist");
			exists = false;
		} else if (!Files.isDirectory(Path.of(dirPath), NOFOLLOW_LINKS)) {
			logger.error("directory \"" + dirPath + "\" does not exist");
			exists = false;
		}
		return exists;
	}
	//formatter: off
	/**
	 * readTextFile reads an entire text file.  Typical usage below:
	 * {@snippet :
	 * // Lines of file delimited by \n characters.
	 * precinctZoneCSVText = Utils.readTextFile(precinctZoneFile);
	 * }
	 * 
	 * @param textFilePath path to txt file.
	 * @return text file contents as a string.
	 */
	//formatter: on
	public static String readTextFile(String textFilePath) {
 		List<String> textLines = null;
		try {
			textLines = Files.readAllLines(Path.of(textFilePath));
		} catch (IOException e) {
			logFatalError(String.format("cannot read file: %s reason: %s", textFilePath, e.getMessage()));
		}
		if (textLines == null) {
			logFatalError(String.format("cannot read file : %s contents contain non UTF-8 character", textFilePath));
		}
 		String text = textLines.stream().collect(joining("\n"));
 		return text;
	}
	/** 
	 * compileRegex compiles a regular expression. Since these are critical 
	 * to the operation of ballot generation, a fatal error is reported if 
	 * the regex fails to compile.
	 * 
	 * @param regex regular expression.
	 * @return Pattern object.
	 */
	public static Pattern compileRegex(String regex) {
		Pattern pattern = null;
		try {
			pattern = Pattern.compile(regex, Pattern.MULTILINE);
		} catch (Exception e) {
			String msg = String.format("can't compile regex: %s msg: %s%n", regex , e.getMessage());
			logFatalError(msg);
		}
		return pattern;
	}
	// check that an environment variable exists
	/**
	 * getEnvVariable gets the value of an environmental variable. E.g. BALLOTGEN_VERSION.
	 * 
	 * @param name name of env variable.
	 * @param necessary is the env variable's value really needed?
	 * @return value of the env variable.
	 */
	public static String getEnvVariable(String name, boolean necessary) {
		String value = System.getenv(name);
		boolean exists = value != null && !value.isBlank();
		if (necessary && !exists) {
			logFatalError("Env variable " + name  + " is undefined or blank");
		}
		return value;
	}
	/**
	 * normalizeNo normalizes a # by supplying leading zeros to reach desired length.
	 * Typical usage:  precinct #s are length 3, zone #s are length 2.
	 * Fatal error is recorded because these strings are used as keys throughout code.
	 * 
	 * @param no number to normalize.
	 * @param maxlen desired length for normalized number.
	 * @return normalized number as string.
	 */
	public static String normalizeNo(int no, int maxlen) {
		if (no < 0) {
			logFatalError(String.format("can't normalize #: %d since it is negative", no));
		}
		String str = Integer.toString(no);
		int strlen = str.length();
		if (strlen > maxlen) {
			logFatalError(String.format("can't normalize #: %d since it exceeds max length: %d", no, maxlen));
		}
		String zeros = "0".repeat(maxlen);
		return zeros.substring(0, maxlen - str.length()) + str;
	}
	/**
	 * normalizeZoneNo normalize a zone # to 2 digit string.
	 * 
	 * @param zoneNo zone # to normalize.
	 * @return normalized zone # string.
	 */
	public static String normalizeZoneNo(int zoneNo) {
		return normalizeNo(zoneNo, ZONE_STR_LEN);
	}
	/**
	 * normalizePrecinctNo normalize a precinct # to 3 digit string.
	 * 
	 * @param precinctNo precinct # to normalize.
	 * @return normalized precinct # string.
	 */
	public static String normalizePrecinctNo(int precinctNo) {
		return normalizeNo(precinctNo, PRECINCT_NO_STR_LEN);
	}
	/**
	 * getDateTimeString - returns the current Date and Time as 
	 * as string for printing. Example: Nov 7, 2023, 3:03:25 PM
	 */
	public static String getDateTimeString() {
		DateTimeFormatter dtf = DateTimeFormatter.ofLocalizedDateTime(java.time.format.FormatStyle.MEDIUM);
		LocalDateTime lt = LocalDateTime.now();
		return dtf.format(lt);
	}
	//@formatter: off
	/**
	 * getPrecinctNoName extracts the PrecinctNoName value from a file path
	 * that is shared between ballot generation programs (ContestGen, BallotNamer, BallotGen).
	 * Example:  "./chester-output/750_East_Whiteland_4_VS.txt" => "750_East_Whiteland_4"
	 * 
	 * @param ballotTextFilePath absolute or relative path to extracted text files.
	 * @return "750_East_Whiteland_4"
	 */
	//@formatter: on
	public static String getPrecinctNoName(String ballotTextFilePath) {
		final String FILE_SUFFIX = "_VS";
		File textFile = new File(ballotTextFilePath);
		String fileName = textFile.getName();
		String pathName = textFile.getPath();
		logger.debug(String.format("pathName: %s", pathName));
		String precinctNoName;
		int lastDot = fileName.lastIndexOf(".");
		precinctNoName = fileName.substring(0, lastDot);
		// if suffix was added, then remove it.
		if (precinctNoName.endsWith(FILE_SUFFIX)) {
			precinctNoName = precinctNoName.substring(0, precinctNoName.length() - FILE_SUFFIX.length());
		}
		logger.debug(String.format("precinctNoName: %s", precinctNoName));
		return precinctNoName;
	}
	/**
	 * getPathNameOnly extracts the path to the ballot output folder.
	 * Example:  "./chester-output/750_East_Whiteland_4_VS.txt" => "./chester-output"
	 * 
	 * @param ballotTextFilePath absolute or relative path to extracted text files.
	 * @return "./chester-output"
	 */
	public static String getPathNameOnly(String ballotTextFilePath) {
		File textFile = new File(ballotTextFilePath);
		String pathName = textFile.getPath();
		int lastSeparator = pathName.lastIndexOf(File.separator);
		pathName = pathName.substring(0, lastSeparator);
		return pathName;
	}
	/**
	 * getPrecinctNo returns the 3 digit precinct # that Voter Services uses.
	 * 
	 * @param precinctNoName Example: 750_East_Whiteland_4
	 * @return 750
	 */
	public static String getPrecinctNo(String precinctNoName) {
		String precinctNo;
		precinctNo = precinctNoName.substring(0, 3);
		return precinctNo;
	}
	/**
	 * getPrecinctName returns the string following the 3 digit precinct # that Voter Services uses.
	 * 
	 * @param precinctNoName Example: 750_East_Whiteland_4
	 * @return East_Whiteland_4
	 */
	public static String getPrecinctName(String precinctNoName) {
		String precinctName;
		precinctName = precinctNoName.substring(4);
		return precinctName;
	}
	
}
