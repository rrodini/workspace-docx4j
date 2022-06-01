package com.rodini.ballotgen;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Candidate class is just a value object that holds candidate name.
 */
public abstract class Candidate {
	private static final Logger logger = LoggerFactory.getLogger(Candidate.class);

	protected String name;
	protected boolean endorsed;
	
	Candidate(String name) {
		this.name = name;
	}	
	public String getName() {
		return name;
	}
	public boolean getEndorsement() {
		return endorsed;
	}
}
