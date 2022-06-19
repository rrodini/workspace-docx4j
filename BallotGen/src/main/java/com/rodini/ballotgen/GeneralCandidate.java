package com.rodini.ballotgen;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * General Candidate class represents a candidate of a known party.
 * This makes endorsements easy e.g. all "Dems"
 */
public class GeneralCandidate extends Candidate {

	private static final Logger logger = LoggerFactory.getLogger(GeneralCandidate.class);
	
	// general election use party
	private Party party;

	
	GeneralCandidate(String name, Party party) {
		super(name);
		this.party = party;
		// typically Initialize.endorsedParty is Party.DEMOCRATIC is
		// but now the value is read from a property file.
		this.endorsed = party == Initialize.endorsedParty;
	}

	public Party getParty() {
		return party;
	}
	
	@Override
	public String toString() {
		return String.format("%s : %s", name, party.toString());
	}

}
