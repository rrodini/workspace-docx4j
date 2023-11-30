package com.rodini.contestgen;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * RetentionExtractor uses its markers (regexes) to find matches against page
 * 2 text of a precinct ballot.
 * Note: All retentions are assumed to be found on page 2.
 * 
 * @author Bob Rodini
 *
 */
public class RetentionExtractor {
	private static final Logger logger = LogManager.getLogger(RetentionExtractor.class);
	private static Pattern  retQuestionPattern;
	private static Pattern  retNamePattern;
	private Matcher m1;
	private Matcher m2;
	private MuniRetentions muniRetentions;
	/**
	 * RetentionExtractor constructor.
	 * 
	 * @param muniRetentions MuniRetentions object (basically a list).
	 */
	public RetentionExtractor(MuniRetentions muniRetentions) {
		this.muniRetentions = muniRetentions;
		retQuestionPattern = RetentionMarkers.getRetQuestionPattern();
		retNamePattern = RetentionMarkers.getRetNamePattern();
	}
	/**
	 * match tries to match the regex against page2 text.
	 * Note: no harm if match not found.
	 * 
	 * @param page2Text text from page 2.
	 */
	public void match(String page2Text) {
		m1 = retQuestionPattern.matcher(page2Text);
		while (m1.find()) {
			String question = m1.group("question");
			String office = m1.group("office");
			m2 = retNamePattern.matcher(question);
			if (m2.find()) {
				Retention ret = RetentionFactory.create(office, m2.group("name"));
				muniRetentions.add(ret);
			} else {
				logger.error("judge name not found in retention question:\n" + question);
			}
		}
	}
}
