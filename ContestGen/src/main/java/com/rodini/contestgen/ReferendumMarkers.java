package com.rodini.contestgen;

import java.util.Properties;
import java.util.regex.Pattern;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.rodini.ballotutils.Utils;

/**
 * ReferendumMarkers gets the regexe used to extract referendum data from ballot text.
 * 
 * @author Bob Rodini
 *
 */
public class ReferendumMarkers {
	static final Logger logger = LogManager.getLogger(ReferendumMarkers.class);

	// test or resource values
	private static String referendumFormat;
	// compiled patterns
	private static Pattern referendumPattern;
	
	public static void initialize(String resourceFilePath) {
		// read format from resource file
		Properties props = Utils.loadProperties(resourceFilePath);
		referendumFormat = Utils.getPropValue(props, ContestGen.COUNTY + ".referendum.format");
		logger.debug(String.format("referendum.format: %s%n", referendumFormat));		
		referendumPattern = Utils.compileRegex(referendumFormat);
	}

	public static Pattern getReferendumPattern() {
		return referendumPattern;
	}
}
