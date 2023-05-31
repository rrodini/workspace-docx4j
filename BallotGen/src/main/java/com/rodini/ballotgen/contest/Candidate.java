package com.rodini.ballotgen.contest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.rodini.ballotgen.common.Party;

/**
 * Candidate class is just a value object that holds candidate name.
 */
public abstract class Candidate {
	private static final Logger logger = LoggerFactory.getLogger(Candidate.class);

	protected String name;
	protected Party party;
	
	protected Candidate(String name, Party party) {
		this.name = name;
		this.party = party;
	}	
	public String getName() {
		return name;
	}
	public Party getParty() {
		return party;
	}
}
