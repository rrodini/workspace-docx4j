package com.rodini.ballotgen;

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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Utils {
	
	private static final Logger logger = LoggerFactory.getLogger(Utils.class);
	private static final String JVM_LOG_LEVEL = "log.level";
	
	/** 
	 * Log a fatal error and stop program.
	 * @param msg message to display / log.
	 */
	public static void logFatalError(String msg) {
		System.out.println(msg);
		logger.error(msg);
		System.exit(1);	// non-zero exit code == fatal error
	}
	// just for log4j
	public static void setLoggingLevel() {
		Map<String, org.apache.logging.log4j.Level> logLevels = Map.of(
				"ERROR", ERROR,
				"WARN", WARN,
				"INFO", INFO,
				"DEBUG", DEBUG,
				"TRACE", TRACE
				);
		// get logging level from JVM argument
		String level = System.getProperty(JVM_LOG_LEVEL, "ERROR").toUpperCase();
		org.apache.logging.log4j.Level log4jLevel = logLevels.get(level);
		if (log4jLevel == null) {
			log4jLevel = ERROR;
		}
		org.apache.logging.log4j.core.config.Configurator.setLevel("com.rodini.ballotgen",log4jLevel);
	}

	public static Properties loadProperties(String resourcePath) {
		// get properties
		Properties props = new Properties();
		try (FileInputStream resourceStream = new FileInputStream(resourcePath);) {
			props.load(resourceStream);
		} catch (Exception e) {
			logFatalError("initialize: cannot load properties file: " + resourcePath);
		}
		logger.info("properties file loaded from: " + resourcePath);
		return props;
	}
	
	public static String getPropValue(Properties props, String propName) {
		String value = props.getProperty(propName);
		if (value == null) {
			logger.debug("propery value not found for property name: " + propName);
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
//			Not an error (actuall expected)
//			logger.error("no property names starting with: " + propNamePrefix);
		}
		return propValues;
	}
	
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
	
	public static Pattern compileRegex(String regex) {
		Pattern pattern = null;
		try {
			pattern = Pattern.compile(regex, Pattern.MULTILINE);
		} catch (Exception e) {
			String msg = String.format("can't compile regex: %s msg: %s%n", regex , e.getMessage());
			Utils.logFatalError(msg);
		}
		return pattern;
	}

	/**
	 * processContestName handles the special case where the name is
	 * split over two lines of text, e.g. "Judge of the\nCourt of Commonwealth Pleas"
	 * 
	 * Note: Not sure why embedded \n doesn't work, but this does.
	 * @param name contest name which may have embedded \n
	 * @return new contest name build dynamically
	 */
	public static String processContestName(String name) {
		String [] elements = name.split("\\\\n");
		StringBuffer sb = new StringBuffer();
		int i;
		for (i = 0; i < elements.length - 1; i++) {
			sb.append(elements[i]);
			sb.append("\n");
		}
		if (i < elements.length) {
			sb.append(elements[i]);
		}
		return sb.toString();
	}
	
	
	
}
