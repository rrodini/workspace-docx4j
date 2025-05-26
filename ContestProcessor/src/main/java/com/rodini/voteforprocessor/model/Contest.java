package com.rodini.voteforprocessor.model;

import java.util.ArrayList;
import java.util.List;


import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Contest is a value object that holds information about a particular political contest (office).
 * It is used by ContestGen1 to generate files like 350_MALVERN_contests.txt
 * and by BallotGen to generate the Contest paragraphs of the docx file. 
 */
public class Contest extends VoteFor {
	private static final Logger logger = LogManager.getLogger(Contest.class);

	// use this to avoid null values
	public final static Contest GENERIC_CONTEST = new Contest("000", "No precinct",
			"Generic contest", "Generic term", "Generic instructions", new ArrayList<Candidate>(), 0);
	
	private String name; // e.g. "Justice of the Supreme Court"
	private String term; // e.g. "6 Year term" or "Vote for no more than EIGHT" or ""
	private String instructions; // e.g. "VOTE for one"
	private List<Candidate> candidates;
	private int    formatIndex;
	public Contest(String precinctNo, String precinctName, String name, 
			String term, String instructions, List<Candidate> candidates,
			int formatIndex) {
		super(precinctNo, precinctName);
		this.name = name;
		this.term = term;
		this.instructions = instructions;
		this.candidates = candidates;
		this.formatIndex = formatIndex;
	}
	public String getName() {
		return name;
	}
	public String getTerm() {
		return term;
	}
	public String getInstructions() {
		return instructions;
	}
	public List<Candidate> getCandidates() {
		return candidates;
	}
	public int getFormatIndex() {
		return formatIndex;
	}
	@Override
	public String toString() {
		// shorten to essential info
		StringBuilder sb = new StringBuilder("Contest: ");
		sb.append(name + "\n");
		if (!term.isEmpty()) {
			sb.append(term + "\n");
		}
		sb.append(instructions + "\n");
		sb.append("Candidates: ");
		for (Candidate c: candidates) {
			sb.append(c.toString() + ", ");
		}
		String contestText = sb.toString();
		// strip off the final ", " from last candidate
		return contestText.substring(0, contestText.length()  - 2);
	}


}
