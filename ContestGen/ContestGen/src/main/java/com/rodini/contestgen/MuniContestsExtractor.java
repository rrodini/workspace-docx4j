package com.rodini.contestgen;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;;
/**
 * MuniContestExtractor "rips" the text of each municipality
 * and creates ordered contest objects. The text passed to 
 * the constructor doesn't have to worry about whether there
 * are one or two pages to the ballot as that has already
 * been taken care of.
 * 
 * @author Bob Rodini
 *
 */

public class MuniContestsExtractor {
	private static final Logger logger = LoggerFactory.getLogger(MuniContestsExtractor.class);

// Sample:
//		Justice of the Supreme Court
//		Vote for ONE                
//		Maria McLaughlin            
//		Democratic                  
//		Kevin Brobson               
//		Republican                  
//		Write-in                    
//		Judge of the Superior Court 
//		Vote for ONE                
//		Timika Lane                 
//		Democratic                  
//		Megan Sullivan              
//		Republican                  
//		Write-in                    
//      ...
//		Judge Of Elections          
//		005 Atglen                  
//		Vote for ONE                
//		Paul J. Bigas               
//		Democratic                  
//		Write-in                    
//		Inspector Of Elections      
//		005 Atglen                  
//		Vote for ONE                
//		Write-in                    


	
	private String muniName;	// Name of municipality
	private String muniContestsText;  // All of the municipal text where contests can be found
	private MuniContestNames muniContestNames;  // use list to maintain order of contests
	private final String WRITE_IN = "Write-in\n";
	/**
	 * constructor
	 * 
	 * @param muniName name of municipality
	 * @param muniContestsText text of contests
	 */
	public MuniContestsExtractor(String muniName, String muniContestsText) {
		this.muniName = muniName;
		this.muniContestsText = muniContestsText;
		muniContestNames = new MuniContestNames(muniName);
	}
	/**
	 * extractContestName extracts text for one contest
	 * and uses ContestNameMatcher object to parse the text.
	 * 
	 * @param start index into contestsText
	 * @param end index into contestsText
	 */
	void extractContestName(final int start,final int end) {
		String contestText = muniContestsText.substring(start, end);
		ContestNameExtractor cnm = new ContestNameExtractor();
		int format = cnm.match(contestText);
System.out.printf("extractContestName: format: %d%n", format);
		if (format == -1) {
			logger.error("No format for: " + contestText);
			return;
		}
		String name = cnm.getContestName();
System.out.printf("extractContestName: name: %s%n", name);
		muniContestNames.add(new ContestName(name, format));
	}
	/**
	 * findContestEnd finds the index of the string past the 
	 * "Write-in" entries for a contest.
	 * 
	 * @param start index into contestsTexts
	 * @return index past the next "Write-in\n" entries
	 */
	int findContestEnd(final int start) {
		int len = WRITE_IN.length();
		int end = muniContestsText.indexOf(WRITE_IN, start);
		// there should be at least one "Write-in" line.
		if (end == -1) {
			logger.error("Can't find \"" + WRITE_IN.trim() + "\"" );
			return muniContestsText.length();
		}
		// Skip over multiple "Write-in" lines.
		while ( end + len <= muniContestsText.length() &&
				muniContestsText.substring(end, end+len).equals(WRITE_IN)) {
			end = end + len;
		}
		return end;
	}
	/**
	 * extractContestText - extracts the text for one contest.
	 */
	void extractContestText() {
		int start = 0;
		int end = 0;
		while (start < muniContestsText.length()) {
			end = findContestEnd(start);
			extractContestName(start, end);
			start = end;
		}
	}
	/**
	 * extract extracts all of the contests in the given text.
	 * 
	 * @return list of ContestName objects.
	 */
	MuniContestNames extract() {
		extractContestText();
		return muniContestNames;
	}
	/**
	 * getMuniName get the municipality name.
	 * 
	 * @return municipality name.
	 */
	String getMuniName() {
		return muniName;
	}
	/**
	 * getMuniContestsText get the text of all contests.
	 * 
	 * @return the text of all contests.
	 */
	String getMuniContestsText() {
		return muniContestsText;
	}

}
