package com.rodini.ballotgen;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * General Candidate class represents a candidate of a known party.
 * This makes endorsements easy e.g. all "Democratic" candidates
 * 
 * Note: There is a kluge for "tickets" such as Governor and Lt. Governor
 *       and President and Vice President as the Voter Services text
 *       appears as below:
 *       
 *       Josh Shapiro
 *       Democratic
 *       Austin Davis
 *       Lieutenant Governor, Democratic
 *       
 *       The party of Austin Davis will be unrecognized (null) and will not
 *       receive an endorsement (but that's okay)
 */
public class GeneralCandidate extends Candidate {

	private static final Logger logger = LoggerFactory.getLogger(GeneralCandidate.class);
	
	// general election use party
	private Party party;
	private String textBeneathName;

	
	GeneralCandidate(String name, Party party, String textBeneathName) {
		super(name);
		this.party = party;
		if (party != null) {
			this.textBeneathName = party.toString();
		} else {
			this.textBeneathName = textBeneathName;
		}
		// typically Initialize.endorsedParty is Party.DEMOCRATIC is
		// but now the value is read from a property file.
		this.endorsed = party == Initialize.endorsedParty;
	}

	public Party getParty() {
		return party;
	}
	
	public String getTextBeneathName() {
		return textBeneathName;
	}
	
	@Override
	public String toString() {
		return String.format("%s : %s", name, textBeneathName);
	}

}
