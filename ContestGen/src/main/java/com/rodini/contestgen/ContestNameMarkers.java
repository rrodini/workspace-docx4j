package com.rodini.contestgen;

import java.util.List;
import java.util.Properties;
import java.util.regex.Pattern;

import com.rodini.ballotutils.Utils;

/**
 * 
 * ContestNameMarkers - 
 */
public class ContestNameMarkers {
	// use during testing
	private static final String [] testContestNameFormats = {
			"^(?<name>(.*\n){1,3})(?<instructions>^Vote.*)\n(?<candidates>((.*\n){1})*)^Write-in$",
			"^(?<name>(.*\n){1,3})(?<term>^(\\d Year |Unexpired ).*)\n(?<instructions>^Vote.*)\n(?<candidates>((.*\n){1})*)^Write-in$",
			"^(?<name>(.*\n){1,3})(?<region>^Region [A-Z].*)\n(?<term>^(\\d |Unexpired ).*)\n(?<instructions>^Vote.*)\n(?<candidates>((.*\n){1})*)^Write-in$",
	};
	// test or resource values
	private static String [] contestNameFormats;
	// compiled patterns
	private static Pattern [] contestNamePatterns;
	
	public static void initialize(String resourceFilePath) {
		// read formats from resource file
		Properties props = Utils.loadProperties(resourceFilePath);
		List<String> formatList = Utils.getPropOrderedValues(props, "contest.format");
		contestNameFormats = new String[formatList.size()];
		for (int i = 0; i < formatList.size(); i++) {
			contestNameFormats[i] = (String) formatList.get(i);
			//System.out.printf("resource: %s%n", contestNameFormats[i]);		
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
