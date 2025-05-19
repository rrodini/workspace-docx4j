package com.rodini.contestprocessor;

import java.util.ArrayList;
import java.util.List;


import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Contest is a value object that holds information about
 * a particular political contest (race).
 */
public class Contest {
	private static final Logger logger = LogManager.getLogger(Contest.class);

	// use this to avoid null values
	public final static Contest GENERIC_CONTEST = new Contest("Generic contest", "Generic term", "Generic instructions", new ArrayList<Candidate>());
	
	private String name; // e.g. "Justice of the Supreme Court"
	private String term; // e.g. "6 Year term" or "Vote for no more than EIGHT" or ""
	private String instructions; // e.g. "VOTE for one"
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
	private List<Candidate> candidates;
	
	public Contest(String name, String term, String instructions, List<Candidate> candidates) {
		this.name = name;
		this.term = term;
		this.instructions = instructions;
		this.candidates = candidates;
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
	/**
	 * processContestName handles the special case where the name is
	 * split over two lines of text, e.g. "Judge of the\nCourt of Commonwealth Pleas"
	 * 
	 * Note: Not sure why embedded \n doesn't work, but this does.
	 * @param name contest name which may have embedded \n
	 * @return new contest name build dynamically
	 */
	// TODO: Determine if this method does anything.
	public static String processContestName(String name) {
		String [] elements = name.split("\\\\n");
		StringBuffer sb = new StringBuffer();
		int i;
		for (i = 0; i < elements.length - 1; i++) {
			sb.append(elements[i]);
			sb.append("\n");
		}
		if (i < elements.length) {
			sb.append(elements[i]);
		}
		return sb.toString();
	}

}
