package com.rodini.voteforprocessor.extract;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.rodini.voteforprocessor.model.Referendum;
/**
 * PageReferendumExtractor processes all ballots and extracts the referendums
 * from each.
 * 
 * Note: The wording of referendums varies greatly between different precincts.
 * This means that one regex will probably NOT match the referendum question exactly.
 * The solution is to use a number of regexes (like contest regexes) but this is NOT implemented
 * at present.
 * 
 * @author Bob Rodini
 *
 */
public class PageReferendumExtractor {
	
	static final Logger logger = LogManager.getLogger(PageReferendumExtractor.class);

	// prevent instantiation
	private PageReferendumExtractor() { }
	
	/**
	 * extractReferendums extracts Referendum objects from the pageText.
	 * 
	 * @param precinctNo
	 * @param precinctName
	 * @param pageText text to extract referendum information.
	 * 
	 * @return List of Referendum objects.
	 */
	public static List<Referendum> extractReferendums(String precinctNo, String precinctName, String pageText) {
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
