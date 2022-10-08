package com.rodini.contestgen;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * ContestNameExtractor - attempts to match the contest text against
 * pre-defined contest name formats (regexes).
 * 
 * Note: All matches must contain a capture group named "name"
 * 
 * @author Bob Rodini
 *
 */
//Samples:
//

//FORMAT 1: 2-3 lines: 1-2: contest name, 2: instruction

//Justice of the Supreme Court  <= contest name (1 line)
//Vote for ONE                  <= contest instruction
//

//Judge of the                  <= contest name (2 lines)
//Court of Common Pleas
//Vote for no more than TWO     <= contest instruction

//FORMAT 2: 3-4 lines: 1-2: contest name, 2: term, 3: instruction

//Treasurer                     <= contest name
//4 Year Term                   <= contest term
//Vote for ONE                  <= contest instruction

//School Director               <= contest name (2 lines)
//West Chester Region 3
//4 Year Term
//Vote for no more than TWO

//Township Supervisor
//East Brandywine Township
//Unexpired 4 Year Term         <= variation on term
//Vote for ONE

//Magisterial District Judge    <= contest name (2 lines)
//District 15-1-01              
//6 Year Term                   <= contest term
//Vote for ONE                  <= contest instruction

public class ContestNameExtractor {
	private static final Logger logger = LoggerFactory.getLogger(ContestNameExtractor.class);
	private static Pattern [] cnPatterns;
	private static boolean initialized = false;
	private Matcher m;
	/**
	 * constructor
	 */
	ContestNameExtractor() {
		this.cnPatterns = ContestNameMarkers.getContestNamePatterns();
	}
	
	/**
	 * match - attempt to match the text against one of the patterns.
	 * The match attempt starts at the most complex pattern (most keywords)
	 * and goes down to the least complex pattern.
	 * 
	 * @param contestText text to match
	 * @return format index where match occurs (or -1 if no match).
	 */
	public int match(String contestText) {
		int count = cnPatterns.length;
		// the regexes are ordered simplest to most complex.
		// start at complex and work down.
		for (int i = count - 1; i >= 0; i--) {
			m = cnPatterns[i].matcher(contestText);
			if (m.find()) {
				// return one-relative value.
				return i + 1;
			}
		}
		// dump patterns
		//dumpPatterns();
		return -1; // no match!!
	}
	
	public static void dumpPatterns() {
		for (int i = 0; i < cnPatterns.length; i++) {
			Pattern pat = cnPatterns[i];
			String patRegex = pat.toString();
			patRegex = patRegex.replaceAll("\n", "\\\\\\n");
			System.out.printf("%s%n", patRegex);
		}
	}
	
	// There was a match so get the name
	/**
	 * getContestName - after a match get the name of the contest.
	 * The name may contain an embedded \n character.
	 * 
	 * @return name of the contest.
	 */
	public String getContestName() {
		String name = "Unknown contest";
		try {
			name = m.group("name");
//System.out.printf("getContestName: name: %s%n", name);
		} catch (Exception e) {
			String msg = e.getMessage();
			logger.error(msg);
		}
		if (name.endsWith("\n")) {
			name = name.substring(0, name.length() - 1);
		}
//System.out.printf("getContestName: name: %s%n", name);
		return name;
	}
	
}
