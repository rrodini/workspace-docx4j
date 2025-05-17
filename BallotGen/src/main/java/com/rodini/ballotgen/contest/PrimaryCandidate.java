package com.rodini.ballotgen.contest;


import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.rodini.ballotutils.Party;
/**
 * General Candidate class represents a candidate of a know region.
 * This makes endorsements hard as an auxiliary file is needed.
 */public class PrimaryCandidate extends Candidate {

	private static final Logger logger = LogManager.getLogger(PrimaryCandidate.class);

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
