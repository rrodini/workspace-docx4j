package com.rodini.contestgen;

import java.util.List;
import java.util.Properties;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.rodini.ballotutils.Utils;

/**
 * 
 * ContestNameMarkers - 
 */
public class ContestNameMarkers {
	static final Logger logger = LoggerFactory.getLogger(ContestNameMarkers.class);
	// test or resource values
	private static String [] contestNameFormats;
	// compiled patterns
	private static Pattern [] contestNamePatterns;
	
	public static void initialize(String resourceFilePath) {
		// read formats from resource file
		Properties props = Utils.loadProperties(resourceFilePath);
		List<String> formatList = Utils.getPropOrderedValues(props, ContestGen.COUNTY + ".contest.format");
		contestNameFormats = new String[formatList.size()];
		for (int i = 0; i < formatList.size(); i++) {
			contestNameFormats[i] = (String) formatList.get(i);
			logger.debug(String.format("format.%d: %s%n", i, contestNameFormats[i]));		
		}		
		contestNamePatterns = new Pattern[contestNameFormats.length];		
		for (int i = 0; i < contestNameFormats.length; i++) {
			compileRegex(i, contestNameFormats[i]);
		}
	}
	
	/**
	 * compileRegex - compile the regex into Pattern object
	 * and place into array.
	 * 
	 * @param index position in cnPattern array.
	 * @param regex regular expression to be compiled.
	 */
	private static void compileRegex(int index, String regex) {
		contestNamePatterns[index] = Utils.compileRegex(regex);
	}
	
	public static Pattern[] getContestNamePatterns() {
		return contestNamePatterns;
	}

}
