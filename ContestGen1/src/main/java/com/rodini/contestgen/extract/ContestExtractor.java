package com.rodini.contestgen.extract;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.rodini.contestgen.common.Initialize;
import com.rodini.contestgen.model.Ballot;
import com.rodini.contestgen.model.Contest;
/**
 * ContestExtractor processes all ballots and extracts the contests
 * from each.
 * 
 * @author Bob Rodini
 *
 */
public class ContestExtractor {

	static final Logger logger = LogManager.getLogger(ContestExtractor.class);
	// prevent instantiation.
	private ContestExtractor() {
	}
	static final String PAGE_BREAK = "PAGE_BREAK"; // pseudo contest name

	/**
	 * extract isolates the contests from the each ballot's page1 and page2 text.
	 * Once done it updates each ballot with a list of Contest objects.
	 * 
	 * @param ballots list of Ballot objects.
	 */
	public static void extract(List<Ballot> ballots) {
		for (Ballot ballot: ballots) {
			extractContests(ballot);
		}
	}
	//@formatter: off
	/**
	 * extractContests extracts contests from one ballot's page1 and page2 text.
	 * Experience with a four year cycle of both Primary and General ballots shows:
	 * 1. Contests can appear on page1 and page2, but are never split between pages.
	 * 2. If contest appears on page2, then Voter Services has inserted "Vote both sides" instructions.
	 *    It is important to pass the location to the BallotGen program.
	 * 3. Some ballots may have contests only on page1 (namely, a short ballot).
	 * 
	 * @param ballot
	 */
	//@formatter: on
	static void extractContests(Ballot ballot) {
		String precinctNo = ballot.getPrecinctNo();
		String precinctName = ballot.getPrecinctName();
		String precinctNoName = precinctNo + "_" + precinctName;
		List<Contest> contests = new ArrayList<>();
		List<Contest> pageContests = null;
		// Isolate sections of text under two assumptions:
		// 1. each contest ends with a "Write-in" line.
		// 2. contests can be on Page 1 or Page 2 but
		//    are never split across two pages.
		logger.info(String.format("page 1 contest extraction for: : %s", precinctNoName));
		pageContests = extractPageContests(precinctNo, precinctName, ballot.getPage1Text());
		contests.addAll(pageContests);
		logger.info(String.format("page 2 contest extraction for: : %s", precinctNoName));
		pageContests = extractPageContests(precinctNo, precinctName, ballot.getPage2Text());
		if (pageContests.size()!= 0 ) {
			// There should be a "Vote Both Sides" indicator on Voter Services specimen.
			logger.info(String.format("extract page break: %s", PAGE_BREAK));
			contests.add(new Contest(precinctNo, precinctName, PAGE_BREAK, 0));
			contests.addAll(pageContests);
		}
		ballot.setContests(contests);
	}
	/** 
	 * extractPageContests extracts the contest on a particular page of a ballot.
	 * 
	 * @param precinctNo
	 * @param precinctName
	 * @param pageText page text previously extracted.
	 * @return List of Contest objects.
	 */
	static List<Contest> extractPageContests(String precinctNo, String precinctName, String pageText) {
		List<Contest> contests = new ArrayList<>();
		int start = 0;
		int end = 0;
		if (pageText.isEmpty() || pageText.lastIndexOf(Initialize.writeIn) == -1) {
			// some defensive programming here.
			return contests;
		}
		int lastWriteIn = pageText.lastIndexOf(Initialize.writeIn) + Initialize.writeIn.length();
		pageText = pageText.substring(0, lastWriteIn);
		//  Assume that the last "Write-in" line marks end of all contests.
		while (start < pageText.length()) {
			end = findContestEnd(pageText, start);
			Contest contest = extractContest(precinctNo, precinctName, pageText.substring(start, end));
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
		int len = Initialize.writeIn.length();
		int end = contestText.indexOf(Initialize.writeIn, start);
		// there should be at least one "Write-in" line.
		if (end == -1) {
			logger.error("Can't find \"" + Initialize.writeIn.trim() + "\"" );
			return contestText.length();
		}
		// Skip over multiple "Write-in" lines.
		while ( end + len <= contestText.length() &&
				contestText.substring(end, end+len).equals(Initialize.writeIn)) {
			end = end + len;
		}
		return end;
	}
	/**
	 * extractContest uses the pre-tested list of regexes to extract the text of
	 * a contest from a subsection of a page.
	 * 
	 * @param precinctNo
	 * @param precinctName
	 * @param contestText subsection of page as determined by methods above.
	 * @return 0 or 1 Contest objects.
	 */
	static Contest extractContest(String precinctNo, String precinctName, String contestText) {
		Pattern [] regexes = Initialize.contestRegex;
		Contest contest = null;
		String contestName = "Unknown contest";
		// try all regexes for a contest. Note that these
		// are ordered from the least complex (good) to the
		// most complex (bad).
		int count = regexes.length;
		int i;
		for (i = 0; i < count; i++) {
			Matcher m = regexes[i].matcher(contestText);
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
			contest = new Contest(precinctNo, precinctName, contestName, i);
		}
		return contest;
	}
	
}
