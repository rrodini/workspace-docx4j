package com.rodini.voteforprocessor.extract;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.rodini.voteforprocessor.extract.Initialize;
import com.rodini.voteforprocessor.model.Contest;
/**
 * ContestExtractor extracts multiple contests from the contests text of the ballot.
 * The contest text contains contests, referendums text, and retentions text.
 * The "Vote Both Sides" marker has already been eliminated.
 * 
 * The algorithm *must* preserve the order of the contests on the VS ballot.
 * It does this by applying the contest regexs from 1 to N sequentially through
 * the text. Only the first hit is used each iteration, then the process is repeated.
 */
public class ContestExtractor {
	private static final Logger logger = LogManager.getLogger(ContestExtractor.class);
	// prevent instantiation.
	private ContestExtractor() {}

	/** 
	 * extractPageContests extracts the contests on the combined pages of a ballot.
	 * The text "Vote Both Sides" has been removed.
	 * 
	 * @param precinctNo
	 * @param precinctName
	 * @param ballotText ballot text
	 * @return List of Contest objects
	 */
	static List<Contest> extractPageContests(String precinctNo, String precinctName, String ballotText) {
		List<Contest> contests = new ArrayList<>();
		int start = 0;
		int end = 0;
		if (ballotText.isEmpty() || ballotText.lastIndexOf(Initialize.WRITE_IN) == -1) {
			// some defensive programming here.
			return contests;
		}
		int lastWriteIn = ballotText.lastIndexOf(Initialize.WRITE_IN) + Initialize.WRITE_IN.length();
		ballotText = ballotText.substring(0, lastWriteIn);
		//  Assume that the last "Write-in" line marks end of all contests.
		while (start < ballotText.length()) {
			end = findContestEnd(ballotText, start);
			Contest contest = findContestFormat(precinctNo, precinctName, ballotText.substring(start, end));
			if (contest != null) {
				contests.add(contest);
			}
			start = end;
		}
		return contests;
	}
	
	/**
	 * findContestEnd finds the index of the string past the "Write-in" entries for a contest.
	 * 
	 * @param start index into contestsTexts
	 * @return index past the next "Write-in\n" entries
	 */
	static int findContestEnd(String contestText, int start) {
		int len = Initialize.WRITE_IN.length();
		int end = contestText.indexOf(Initialize.WRITE_IN, start);
		// there should be at least one "Write-in" line.
		if (end == -1) {
			logger.error("Can't find \"" + Initialize.WRITE_IN.trim() + "\"" );
			return contestText.length();
		}
		// Skip over multiple "Write-in" lines.
		while ( end + len <= contestText.length() &&
				contestText.substring(end, end+len).equals(Initialize.WRITE_IN)) {
			end = end + len;
		}
		return end;
	}
	/**
	 * findContestFormat uses the pre-tested list of regexes to extract the text of
	 * a contest from a subsection of a page.
	 * 
	 * @param precinctNo
	 * @param precinctName
	 * @param contestText subsection of page as determined by methods above.
	 * @return Contest object.
	 */
	static Contest findContestFormat(String precinctNo, String precinctName, String contestText) {
//System.out.println("findContestFormat: contestText:");
//System.out.println(contestText);
		Pattern [] regexes = Initialize.contestRegexes;
		Contest contest = null;
		String contestName = "Unknown contest";
		// try all regexes for a contest. Note that these
		// are ordered from the least complex (good) to the
		// most complex (bad).
		int count = regexes.length;
//System.out.printf("count: %d%n", count);
		int i;
		Matcher m = null;
		for (i = 0; i < count; i++) {
			m = regexes[i].matcher(contestText);
			if (m.find()) {
				contestName = m.group("name").trim();
				// return one-relative value.
				i++;
				break;
			}
		}
		if (i == count) {
			// No match! this error is typical for long referendum questions.
			logger.error(String.format("no regex match. precinctName: %s text: %s", precinctName, contestText));
		} else {
			logger.info(String.format("contest extraction: %s name: %s", precinctNo + "_" + precinctName, contestName));
			contest = ContestFactory.extractContest(precinctNo, precinctName, contestName, m, i);
		}
		return contest;
	}

	
	
}
