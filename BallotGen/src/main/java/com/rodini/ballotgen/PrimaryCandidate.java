package com.rodini.ballotgen;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
/**
 * General Candidate class represents a candidate of a know region.
 * This makes endorsements hard as an auxiliary file is needed.
 */public class PrimaryCandidate extends Candidate {

	private static final Logger logger = LoggerFactory.getLogger(PrimaryCandidate.class);

	// this could be a county, township, or "Male - Tredyffrin township"
	private String residence;  // Note: May be "".

	
	public PrimaryCandidate(String name, Party party, String residence) {
		super(name, party);
		this.residence = residence;
	}
	
	public String getResidence() {
		return residence;
	}
	
	@Override
	public String toString() {
		return String.format("%s : %s %s", name, party.toString(), residence);
	}

}
