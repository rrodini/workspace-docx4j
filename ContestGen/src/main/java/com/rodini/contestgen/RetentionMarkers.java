package com.rodini.contestgen;

import java.util.Properties;
import java.util.regex.Pattern;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.rodini.ballotutils.Utils;
/**
 * RetentionMarkers gets the regexes used to extract retention data from ballot text.
 * 
 * @author Bob Rodini
 *
 */
public class RetentionMarkers {
	static final Logger logger = LogManager.getLogger(RetentionMarkers.class);

	private static String retQuestionFormat;
	private static String retNameFormat;
	// compiled patterns
	private static Pattern retQuestionPattern;
	private static Pattern retNamePattern;
	
	public static void initialize(String resourceFilePath) {
		// read format from resource file
		Properties props = Utils.loadProperties(resourceFilePath);
		retQuestionFormat = Utils.getPropValue(props, ContestGen.COUNTY + ".retention.question.format");
		logger.debug(String.format("retention.question.format: %s", retQuestionFormat));		
		retQuestionPattern = Utils.compileRegex(retQuestionFormat);
		retNameFormat = Utils.getPropValue(props, ContestGen.COUNTY + ".retention.name.format");
		logger.debug(String.format("retention.name.format: %s", retNameFormat));		
		retNamePattern = Utils.compileRegex(retNameFormat);
	}

	public static Pattern getRetQuestionPattern() {
		return retQuestionPattern;
	}
	
	public static Pattern getRetNamePattern() {
		return retNamePattern;
	}
}



