package com.rodini.contestgen.extract;

import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.rodini.contestgen.model.Ballot;
import com.rodini.voteforprocessor.extract.PageContestExtractor;
import com.rodini.voteforprocessor.model.Contest;
/**
 * PageContestExtractor processes all ballots and extracts the contests
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
	public static void extractContests(Ballot ballot) {
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
		pageContests = PageContestExtractor.extractPageContests(precinctNo, precinctName, ballot.getPage1Text());
		contests.addAll(pageContests);
		logger.info(String.format("page 2 contest extraction for: : %s", precinctNoName));
		pageContests = PageContestExtractor.extractPageContests(precinctNo, precinctName, ballot.getPage2Text());
		if (pageContests.size()!= 0 ) {
			// There should be a "Vote Both Sides" indicator on Voter Services specimen.
			logger.info(String.format("extract page break: %s", PAGE_BREAK));
			contests.add(new Contest(precinctNo, precinctName, PAGE_BREAK, "", "", null, 0));
			contests.addAll(pageContests);
		}
		ballot.setContests(contests);
	}

}
