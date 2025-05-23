package com.rodini.voteforprocessor.extract;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.rodini.voteforprocessor.model.Retention;
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
				Retention ret = new Retention(precinctNo, precinctName, question, officeName, m2.group("name"));
				retentionList.add(ret);
			} else {
				logger.error("judge name not found in retention question:");
			}
		}
		return retentionList;
	}
}
