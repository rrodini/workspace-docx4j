package com.rodini.ballotutils;

import static java.nio.file.LinkOption.NOFOLLOW_LINKS;
import static java.util.stream.Collectors.joining;
import static org.apache.logging.log4j.Level.DEBUG;
import static org.apache.logging.log4j.Level.ERROR;
import static org.apache.logging.log4j.Level.INFO;
import static org.apache.logging.log4j.Level.TRACE;
import static org.apache.logging.log4j.Level.WARN;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Pattern;
// SLF4J
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
// LOG4J
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

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
	static final int ZONE_STR_LEN = 2; 	// Normalized zoneNo is two digits
	static final int MUNI_STR_LEN = 3;	// Normalized muniNo is three digits
	
	/** 
	 * Log a fatal error and stop program.
	 * @param msg message to display / log.
	 */
	public static void logFatalError(String msg) {
		System.out.println(msg);
		logger.error(msg);
		System.exit(1);	// non-zero exit code means fatal error
	}
	// just for log4j
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
		String strLevel = System.getProperty(JVM_LOG_LEVEL, "ERROR").toUpperCase();
//		org.apache.logging.log4j.Level log4jLevel = logLevels.get(strLevel);
//      System.out.printf("log4jLevel: %s%n", log4jLevel);
//		if (log4jLevel == null) {
//			log4jLevel = ERROR;
//		}
//		org.apache.logging.log4j.core.config.Configurator.setLevel(loggerName,log4jLevel);
		Level level = logLevels.get(strLevel);
		System.out.printf("log4jLevel: %s%n", level);
		if (level == null) {
			level = ERROR;
		}
		org.apache.logging.log4j.core.config.Configurator.setLevel(loggerName,level);
	}
	// load program properties (as per Java conventions).
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
	public static String getPropValue(Properties props, String propName) {
		String value = props.getProperty(propName);
		if (value == null) {
			logger.info("property value not found for property name: " + propName);
		}
		return value;
	}
	// specifically for contest.format.1
	//                  contest.format.2
	public static List<String> getPropOrderedValues(Properties props, String propNamePrefix) {
		List<String> propNames = new ArrayList<>();
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
//			Not an error (actually expected)
//			logger.error("no property names starting with: " + propNamePrefix);
		}
		return propValues;
	}
	// check if file exits.
	public static boolean checkFileExists(String filePath) {
		boolean exists = true;
		if (!Files.exists(Path.of(filePath), NOFOLLOW_LINKS)) {
			logger.error("file \"" + filePath + "\" does not exist");
			exists = false;
		}
		return exists;
	}
	// read all the text of a file.
	public static String readTextFile(String textFilePath) {
 		List<String> textLines = null;
		try {
			textLines = Files.readAllLines(Path.of(textFilePath));
		} catch (IOException e) {
			logFatalError("cannot read file: " + textFilePath);
		}
 		String text = textLines.stream().collect(joining("\n"));
 		return text;
	}
	// compile a regular expression.
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
	public static String getEnvVariable(String name, boolean necessary) {
		String value = System.getenv(name);
		boolean exists = value != null && !value.isBlank();
		if (necessary && !exists) {
			logFatalError("Env variable " + name  + " is undefined or blank");
		}
		return value;
	}
	
	// Normalize an int to a string with leading zeros.
	// Errors are considered fatal since zoneNo/zoneStr
	// and muniNo/muniStr are treated as keys throughout the code.
	public static String normalizeNo(int no, int maxlen) {
		String str = Integer.toString(no);
		int strlen = str.length();
		if (strlen > maxlen) {
			logFatalError(String.format("can't normalize no: $d since it exceeds max length: %d", no, maxlen));
		}
		String zeros = "0".repeat(maxlen);
		return zeros.substring(0, maxlen - str.length()) + str;
	}
	// zoneNos are always 2 digits
	public static String normalizeZoneNo(int zoneNo) {
		return normalizeNo(zoneNo, ZONE_STR_LEN);
	}
	// muniNos are always 3 digits
	public static String normalizeMuniNo(int muniNo) {
		return normalizeNo(muniNo, MUNI_STR_LEN);
	}

}
