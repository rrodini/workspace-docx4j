package com.rodini.contestgen.extract;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.rodini.contestgen.common.Initialize;
import com.rodini.contestgen.model.Ballot;
import com.rodini.contestgen.model.Referendum;
/**
 * ReferendumExtractor processes all ballots and extracts the referendums
 * from each.
 * 
 * @author Bob Rodini
 *
 */
public class ReferendumExtractor {
	
	static final Logger logger = LogManager.getLogger(ReferendumExtractor.class);

	// prevent instantiation
	public ReferendumExtractor() { }
	
	/**
	 * extract isolates the referendums from the each ballot's page2 text.
	 * Once done it updates each ballot with a list of Referendum objects.
	 * 
	 * @param ballots list of Ballot objects.
	 */
	public static void extract(List<Ballot> ballots) {
		for (Ballot ballot: ballots) {
			extractPageReferendums(ballot);
		}
	}
	/**
	 * extractPageReferendums extracts referendums from the ballot's page2 text.
	 * 
	 * @param ballot Ballot object.
	 */
	static void extractPageReferendums(Ballot ballot) {
		String precinctNo = ballot.getPrecinctNo();
		String precinctName = ballot.getPrecinctName();
		List<Referendum> referendumList = new ArrayList<>();
		String page2Text = ballot.getPage2Text();
		if (!page2Text.isBlank()) {
			referendumList = extractReferendums(precinctNo, precinctName, page2Text);
		}
		ballot.setReferendums(referendumList);
	}
	/**
	 * extractReferendums extracts Referendum objects from the pageText.
	 * 
	 * @param precinctNo
	 * @param precinctName
	 * @param pageText text to extract referendum information.
	 * 
	 * @return List of Referendum objects.
	 */
	static List<Referendum> extractReferendums(String precinctNo, String precinctName, String pageText) {
		List<Referendum> referendumList = new ArrayList<>();
		Pattern regex = Initialize.referendumRegex;
		Matcher m = regex.matcher(pageText);
		// There may be more than one referendum question.
		while (m.find()) {
			// TODO: Add try/catch
			String question = m.group("question");
			if (question.endsWith("\n")) {
				question = question.substring(0, question.length() - 1);
			}
			String text = m.group("text");
			logger.info(String.format("referendum extraction: %s question: %s", precinctNo + "_" + precinctName, question));
			Referendum ref = new Referendum(precinctNo, precinctName, question, text);
			referendumList.add(ref);
		}
		return referendumList;
	}
}
