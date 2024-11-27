package com.rodini.contestgen.extract;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.rodini.contestgen.common.Initialize;
import com.rodini.contestgen.model.Ballot;
import com.rodini.contestgen.model.Retention;
/** 
 * RetentionExtractor processes all ballots and extracts the retentions
 * from each.
 * 
 * @author Bob Rodini
 *
 */
public class RetentionExtractor {
	
	static final Logger logger = LogManager.getLogger(RetentionExtractor.class);

	// prevent instantiation
	public RetentionExtractor() { }
	
	/**
	 * extract isolates the retention questions from the each ballot's page2 text.
	 * Once done it updates each ballot with a list of Retention objects.
	 * 
	 * @param ballots list of Ballot objects.
	 */
	public static void extract(List<Ballot> ballots) {
		for (Ballot ballot: ballots) {
			extractPageRetentions(ballot);
		}
	}
	/**
	 * extractPageRetentions extracts retentions from the ballot's page2 text.
	 * 
	 * @param ballot Ballot object.
	 */
	static void extractPageRetentions(Ballot ballot) {
		String precinctNo = ballot.getPrecinctNo();
		String precinctName = ballot.getPrecinctName();
		List<Retention> retentionList = new ArrayList<>();
		String page2Text = ballot.getPage2Text();
		if (!page2Text.isBlank()) {
			retentionList = extractRetentions(precinctNo, precinctName, page2Text);
		}
		ballot.setRetentions(retentionList);
	}
	/**
	 * extractRetentions extracts Retention objects from the pageText.
	 * 
	 * @param precinctNo
	 * @param precinctName
	 * @param pageText text to extract retention information.
	 * 
	 * @return List of Retention objects.
	 */
	static List<Retention> extractRetentions(String precinctNo, String precinctName, String pageText) {
		List<Retention> retentionList = new ArrayList<>();
		Pattern questionRegex = Initialize.retentionQuestionRegex;
		Pattern nameRegex = Initialize.retentionNameRegex;
		Matcher m1 = questionRegex.matcher(pageText);
		while (m1.find()) {
			String question = m1.group("question");
			String officeName = m1.group("office");
			question += "\n";
			// TODO: Add try/catch
			Matcher m2 = nameRegex.matcher(question);
			if (m2.find()) {
				logger.info(String.format("retention extraction: %s question: %s", precinctNo + "_" + precinctName, question));
				Retention ret = new Retention(precinctNo, precinctName, officeName, m2.group("name"));
				retentionList.add(ret);
			} else {
				logger.error("judge name not found in retention question:");
			}
		}
		return retentionList;
	}
}
