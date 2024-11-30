package com.rodini.contestgen.extract;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

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
	 * 1. There may be only one logical page to each ballot.
	 * 2. Referendum questions and retention questions are always placed on page2
	 *     by Chesco Voter Services.
	 *     
	 * @param ballot Ballot object.
	 */
	static void extractPages(Ballot ballot) {
		String rawText = ballot.getRawText();
		Pattern page1Regex = Initialize.precinctBallotPage1Regex;
		Pattern page2Regex = Initialize.precinctBallotPage2Regex;
		int pageCount = Initialize.precinctBallotPageCount;
		String precinctNoName = ballot.getPrecinctNoName();
		// extract page1
		String page1Text = extractPage(precinctNoName, rawText, page1Regex, 1);
		// ATTENTION:
		// page2Text may be due to two reasons:
		// 1. For all ballots only page 1 have text (short ballot)
		// 2. Some ballots have only 1 page whereas others have 2.
		String page2Text = "";
		// extract page2
		if (pageCount == 2) {
			page2Text = extractPage(precinctNoName, rawText, page2Regex, 2);
		}
		ballot.setPage1Text(page1Text);
		ballot.setPage2Text(page2Text);
	}
	/**
	 * extractPage isolates a page of raw text of the ballot. The regex to do
	 * this must be pre-tested and correctly entered as a property value.
	 * 
	 * It was recently discovered (General election of 2024) that some ballots have
	 * two pages of text and others only one page of text. This means that a call
	 * on extractPage for the 2nd page might fail, and this is not an error.
	 * 
	 * @param precinctNoName ballot identifier.
	 * @param rawText rawText of the ballot.
	 * @param pageRegex regex designed to isolate the given page.
	 * @param pageNo 1 or 2.
	 * @return text of page 1 or 2.
	 */
	static String extractPage(String precinctNoName, String rawText, Pattern pageRegex, int pageNo) {
		String pageText = "";
		Matcher m = pageRegex.matcher(rawText);
		if (!m.find()) {
			if (pageNo == 1) {
				// Definitely an error on page 1.
				// Maybe an error on page 2. Requires human inspection.
				String msg = String.format("no match for precinctNoName: %s precinct page %d.  regex: %s", precinctNoName, pageNo, pageRegex.pattern());
				logger.error(msg);
			}
		} else {
			try {
				pageText = m.group("page");
				logger.info(String.format("page extraction: %s page #: %d", precinctNoName, pageNo));
			} catch (Exception e) {
				String msg = e.getMessage();
				logger.error(msg);
			}
		}
		return pageText;
	}

}
