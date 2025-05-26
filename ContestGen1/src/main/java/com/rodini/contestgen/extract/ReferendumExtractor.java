package com.rodini.contestgen.extract;

import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.rodini.contestgen.model.Ballot;
import com.rodini.voteforprocessor.extract.PageReferendumExtractor;
import com.rodini.voteforprocessor.model.Referendum;
/**
 * PageReferendumExtractor processes all ballots and extracts the referendums
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
			referendumList = PageReferendumExtractor.extractReferendums(precinctNo, precinctName, page2Text);
		}
		ballot.setReferendums(referendumList);
	}
}
