package com.rodini.voteforprocessor.model;


import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.rodini.ballotutils.Party;

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

	private static final Logger logger = LogManager.getLogger(GeneralCandidate.class);
	
	// general election use party
	private String textBeneathName;
	private boolean bottomOfTicket;

	
	public GeneralCandidate(String name, Party party, String textBeneathName, boolean bottomOfTicket) {
		super(name, party);
		this.textBeneathName = textBeneathName;
		this.bottomOfTicket = bottomOfTicket;
	}

	public Party getParty() {
		return party;
	}
	
	public String getTextBeneathName() {
		return textBeneathName;
	}
	
	public boolean getBottomOfTicket() {
		return bottomOfTicket;
	}
	
	@Override
	public String toString() {
		return String.format("%s : %s %s", name, party.toString(), textBeneathName);
	}

}
