package com.rodini.contestgen;

//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

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
	private static final Logger logger = LogManager.getLogger(MuniContestsExtractor.class);

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
	private String muniPage1Text;  // Page 1 of municipal text where contests can be found
	private String muniPage2Text;  // Page 2 of municipal text where contests can be found
	private MuniContestNames muniContestNames;  // use list to maintain order of contests
	/**
	 * constructor
	 * 
	 * @param muniName name of municipality
	 * @param muniPage1Text, String muniPage2Text text of contests
	 */
	public MuniContestsExtractor(String muniName, String muniPage1Text, String muniPage2Text) {
		this.muniName = muniName;
		this.muniPage1Text = muniPage1Text;
		this.muniPage2Text = muniPage2Text;
		logger.info(String.format("MuniContestsExtractor: name: %s", muniName));
		muniContestNames = new MuniContestNames(muniName);
	}
	/**
	 * extractContestName extracts text for one contest
	 * and uses ContestNameMatcher object to parse the text.
	 * 
	 * @param start index into contestsText
	 * @param end index into contestsText
	 */
	void extractContestName(String pageText, final int start,final int end) {
		String contestText = pageText.substring(start, end);
		ContestNameExtractor cnm = new ContestNameExtractor();
		int format = cnm.match(contestText);
		logger.info(String.format("extractContestName: format: %d", format));
		if (format == -1) {
			// this error is typical for long referendum questions.
			logger.error(String.format("contest error muniName: %s text: %s: ", muniName, contestText));
			return;
		}
		String name = cnm.getContestName();
		logger.info(String.format("extractContestName: name: %s", name));
		muniContestNames.add(new ContestName(name, format));
	}
	/**
	 * findContestEnd finds the index of the string past the 
	 * "Write-in" entries for a contest.
	 * 
	 * @param start index into contestsTexts
	 * @return index past the next "Write-in\n" entries
	 */
	int findContestEnd(String pageText, final int start) {
		int len = ContestGen.WRITE_IN.length();
		int end = pageText.indexOf(ContestGen.WRITE_IN, start);
		// there should be at least one "Write-in" line.
		if (end == -1) {
			logger.error("Can't find \"" + ContestGen.WRITE_IN.trim() + "\"" );
			return pageText.length();
		}
		// Skip over multiple "Write-in" lines.
		while ( end + len <= pageText.length() &&
				pageText.substring(end, end+len).equals(ContestGen.WRITE_IN)) {
			end = end + len;
		}
		return end;
	}
	/**
	 * extractContestText - extracts the text for one contest.
	 */
	/* private */
	void extractContestText(String pageText) {
		int start = 0;
		int end = 0;
		while (start < pageText.length()) {
			end = findContestEnd(pageText, start);
			extractContestName(pageText, start, end);
			start = end;
		}
	}
	
	/* private */
	void extractPageBreak() {
		logger.info(String.format("extractPageBreak: %s", ContestGen.PAGE_BREAK));
		muniContestNames.add(new ContestName(ContestGen.PAGE_BREAK, 0));
	}
	/**
	 * extract extracts all of the contests in the given text.
	 * 
	 * @return list of ContestName objects.
	 */
	public MuniContestNames extract() {
		// extract from page 1
		extractContestText(muniPage1Text);
		if (!muniPage2Text.isBlank()) {
			// write page-break
			extractPageBreak();
			// extract from page 2
			extractContestText(muniPage2Text);
		}
		return muniContestNames;
	}
	/**
	 * getMuniName get the municipality name.
	 * 
	 * @return municipality name.
	 */
	public String getMuniName() {
		return muniName;
	}
	/**
	 * getMuniPage1Text get the text of page 1.
	 * 
	 * @return the text of page 1 contests.
	 */
	public String getMuniPage1Text() {
		return muniPage1Text;
	}
	/**
	 * getMuniPage2Text get the text of page 2.
	 * 
	 * @return the text of page 2 contests.
	 */
	public String getMuniPage2Text() {
		return muniPage2Text;
	}
	/**
	 * getContestsText get the text of both page 1 and page 2.
	 * 
	 * @return the text of both page 1 and page 2.
	 */
	public String getMuniContestsText() {
		return muniPage1Text + muniPage2Text;
	}
}
