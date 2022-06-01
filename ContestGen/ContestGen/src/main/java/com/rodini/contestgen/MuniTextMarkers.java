package com.rodini.contestgen;

import java.util.Properties;
import java.util.regex.Pattern;
/**
 * MuniTextMarkers is a container for markers that
 * delimit the contest text area of a 1 or 2 page
 * municipal ballot.
 * 
 * These markers may vary from election to election and
 * must be carefully designed for each election.
 * 
 * @author Bob Rodini
 *
 */
public class MuniTextMarkers {
	private static int pageCount;
	private static String page1Regex;
	private static String page2Regex;
	private static Pattern page1Pattern;
	private static Pattern page2Pattern;
	
	public static void initialize(boolean useResourceFile, String resourceFilePath) {
		if (useResourceFile) {
			// use resource data
			Properties props;
			props = Utils.loadProperties(resourceFilePath);
			pageCount = Integer.parseInt(Utils.getPropValue(props, "muniTextPageCount"));
			page1Regex = Utils.getPropValue(props, "muniTextPage1Regex");
			page2Regex = Utils.getPropValue(props, "muniTextPage2Regex");
//System.out.printf("page2Regex: %s%n", page2Regex);
		} else {
			// use test data
			pageCount = 2;
			page1Regex = "(?m)(.*?)(^CONTESTS AND QUESTIONS\\.$\n)(?<page>((.*)\n)*?)^Continued from front side of ballot$(.*)";
			// special case since some pages end:  Write-in Superior Court Retention\n
            // rather than:                        Write-in Superior\nCourt Retention\n
			page2Regex = "(?m)(.*?)(^FOLLOWING JUDICIAL RETENTION\nQUESTIONS$\n)(?<page>((.*)\n)*?)^(Write-in\n|Write-in )Superior Court Retention$(.*)";
			// compile 1x for entire application
		}
		page1Pattern = Utils.compileRegex(page1Regex);
		page2Pattern = Utils.compileRegex(page2Regex);
	}
	/**
	 * getPageCount returns the # of pages over which the contest text
	 * is spread.  Typically 1, but elections w/ local races require 2.
	 * @return # of pages over which the contest text is spread.
	 */
	static int getPageCount() {
		return pageCount;
	}
	/**
	 * getPage1Pattern return the compiled regex that demarcates 
	 * the contest text on page 1.
	 * @return compiled pattern for regex1.
	 */
	static Pattern getPage1Pattern() {
		return page1Pattern;
	}
	/**
	 * getPage2Pattern return the compiled regex that demarcates 
	 * the contest text on page 2.
	 * @return compiled pattern for regex2.
	 */
	static Pattern getPage2Pattern() {
		return page2Pattern;
	}

	
}
