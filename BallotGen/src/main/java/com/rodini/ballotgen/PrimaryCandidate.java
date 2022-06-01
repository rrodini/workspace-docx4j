package com.rodini.ballotgen;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
/**
 * General Candidate class represents a candidate of a know region.
 * This makes endorsements hard as an auxiliary file is needed.
 */public class PrimaryCandidate extends Candidate {

	private static final Logger logger = LoggerFactory.getLogger(PrimaryCandidate.class);

	// this could be a county, township, or "Male - Tredyffrin township"
	private String residence;

	
	public PrimaryCandidate(String name, String residence) {
		super(name);
		this.residence = residence;
		// need list of endorsements
		this.endorsed = false;
	}
	
	public PrimaryCandidate(String name) {
		super(name);
		this.residence = "";
		this.endorsed = false;
	}
	public String getResidence() {
		return residence;
	}
	
	@Override
	public String toString() {
		return String.format("%s : %s", name, residence);
	}

}
