package com.rodini.contestgen.extract;

import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.rodini.contestgen.model.Ballot;
import com.rodini.voteforprocessor.extract.PageRetentionExtractor;
import com.rodini.voteforprocessor.model.Retention;
/** 
 * PageRetentionExtractor processes all ballots and extracts the retentions
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
			retentionList = PageRetentionExtractor.extractRetentions(precinctNo, precinctName, page2Text);
		}
		ballot.setRetentions(retentionList);
	}
}
