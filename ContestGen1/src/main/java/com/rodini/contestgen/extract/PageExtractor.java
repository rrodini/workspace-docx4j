package com.rodini.contestgen.extract;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.Level;

import com.rodini.ballotutils.Utils;
import com.rodini.contestgen.common.Initialize;
import com.rodini.contestgen.model.Ballot;
/**
 * PageExtractor has the responsibility for isolating the page text for each Ballot object.
 * This isolation helps with the creation of VoteFor objects.
 * 
 * @author Bob Rodini
 *
 */
public class PageExtractor {
	static final Logger logger = LogManager.getLogger(PageExtractor.class);
	// prevent instantiation.
	private PageExtractor() {}
	/**
	 * extract isolates the page1 and page2 text from the ballot objects' rawText field
	 * and updates the fields within each ballot object.
	 * 
	 * @param ballots list of Ballot objects.
	 */
	public static void extract(List<Ballot> ballots) {
		for (Ballot ballot: ballots) {
			extractPages(ballot);
		}
	}
	/**
	 * extractPages isolates the page1 and page2 text for one ballot object.
	 * It is important to isolate page1 from page2 for several reasons.
	 * 1. Two page ballots require a PAGE BREAK (e.g. Vote Both Sides) blurb.
	 * 2. Referendum questions and retention questions are always placed on page2
	 *     by Chesco Voter Services.
	 *     
	 * @param ballot Ballot object.
	 */
	static void extractPages(Ballot ballot) {
		String rawText = ballot.getRawText();
		String precinctNoName = ballot.getPrecinctNoName();
		// OLD logic.
//		Pattern page1Regex = Initialize.precinctBallotPage1Regex;
//		Pattern page2Regex = Initialize.precinctBallotPage2Regex;
//		int pageCount = Initialize.precinctBallotPageCount;
//		String precinctNoName = ballot.getPrecinctNoName();
//		// extract page1
//		String page1Text = extractPage(precinctNoName, rawText, page1Regex, 1);
//		Utils.logLines(logger, Level.DEBUG, precinctNoName + " page1 text:", page1Text.split("\n"));
//		// ATTENTION:
//		// page2Text may be empty due to two reasons:
//		// 1. For all ballots only page 1 have text (short ballot)
//		// 2. Some ballots have only 1 page whereas others have 2.
//		String page2Text = "";
//		// extract page2
//		if (pageCount == 2) {
//			page2Text = extractPage(precinctNoName, rawText, page2Regex, 2);
//		}
//		Utils.logLines(logger, Level.DEBUG, precinctNoName + " page2 text:", page2Text.split("\n"));
		String page1Text = "";
		String page2Text = "";
		if (extractPageCount(rawText) == 1) {
			// Use just one regex.
			page1Text = extractPage(precinctNoName, rawText, Initialize.precinctOnePageRegex, 1);
		} else {
			// Use two regexes.
			page1Text = extractPage(precinctNoName, rawText, Initialize.precinctTwoPage1Regex, 1);
			page2Text = extractPage(precinctNoName, rawText, Initialize.precinctTwoPage2Regex, 2);
		}	
		ballot.setPage1Text(page1Text);
		ballot.setPage2Text(page2Text);
	}
	/**
	 * Use the precinctPageBreakRegex to recognize a two-page ballot.
	 * Recent VS specimens use "Vote Both Sides" as a marker for a two pager.
	 * @param rawText complete precinct ballot text
	 * @return 1 or 2.
	 */
	static int extractPageCount(String rawText) {
		int pageCount = 1;
		Pattern pageBreakRegex = Initialize.precinctPageBreakRegex;
		Matcher m = pageBreakRegex.matcher(rawText);
		if (m.find()) {
			pageCount = 2;
		}
		return pageCount;
	}
	
	/**
	 * extractPage isolates a page of contest text from the ballot. The regex to do
	 * this must be pre-tested and correctly entered as a property value.
	 * 
	 * It was recently discovered (General election of 2024) that some ballots have
	 * two pages of text and others only one page of text. This now (v1.7.0) requires
	 * a precise regex for one page ballots and two precise regexes for two page
	 * ballots.
	 * 
	 * @param precinctNoName ballot identifier.
	 * @param rawText the raw text of the ballot.
	 * @param pageRegex regex designed to isolate the given page.
	 * @param pageNo 1 or 2.
	 * @return text of page 1 or 2.
	 */
	static String extractPage(String precinctNoName, String rawText, Pattern pageRegex, int pageNo) {
		String pageText = "";
		Matcher m = pageRegex.matcher(rawText);
		if (!m.find()) {
			String msg = String.format("no match for precinctNoName: %s precinct page %d.  regex: %s", precinctNoName, pageNo, pageRegex.pattern());
			logger.error(msg);
		} else {
			try {
				pageText = m.group("page");
				logger.info(String.format("page extraction: %s page #: %d", precinctNoName, pageNo));
				Utils.logLines(logger, Level.DEBUG, "page lines:" , pageText.split("\n"));
			} catch (Exception e) {
				String msg = String.format("no match for precinctNoName: %s precinct page %d.  regex: %s reason: %s", precinctNoName, pageNo, pageRegex.pattern(), e.getMessage());
				logger.error(msg);
			}
		}
		// OLD logic and should not be necessary if the page level regexes are properly designed.
//		if (!pageText.endsWith(Initialize.writeIn)) {
//			pageText = pageText + Initialize.writeIn;
//		}
		return pageText;
	}

}
