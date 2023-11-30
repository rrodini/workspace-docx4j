package com.rodini.contestgen;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * ReferendumExtractor uses its markers (regexes) to find matches against page
 * 2 text of a precinct ballot.
 * Note: All referendums are assumed to be found on page 2.
 * 
 * @author Bob Rodini
 *
 */
public class ReferendumExtractor {
	private static final Logger logger = LogManager.getLogger(ReferendumExtractor.class);
	private static Pattern  referendumPattern;
	private Matcher m;
	private MuniReferendums muniReferendums;
	private String muniNo;
	/**
	 * ReferendumExtractor constructor.
	 * 
	 * @param muniReferendums MuniReferendums object (basically a list).
	 * @param muniNo precinct #.
	 */
	public ReferendumExtractor(MuniReferendums muniReferendums, String muniNo) {
		this.muniReferendums = muniReferendums;
		this.muniNo = muniNo;
		referendumPattern = ReferendumMarkers.getReferendumPattern();
	}
	/**
	 * match tries to match the regex against page2 text.
	 * Note: no harm if match not found.
	 * 
	 * @param page2Text text from page 2.
	 */
	public void match(String page2Text) {
		m = referendumPattern.matcher(page2Text);
		// There may be more than one referendum question.
		while (m.find()) {
			String question = m.group("question");
			if (question.endsWith("\n")) {
				question = question.substring(0, question.length() - 1);
			}
			String text = m.group("text");
			Referendum ref = ReferendumFactory.create(question, text, muniNo);
			muniReferendums.add(ref);
		}
	}
}
