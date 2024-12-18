package com.rodini.contestgen.model;

/**
 * Contest is a simple representation of a Contest (aka Office) on a ballot.
 * 
 * @author Bob Rodini
 *
 */
public class Contest extends VoteFor {
	String contestName;
	int contestFormat;
	
	public Contest(String precinctNo, String precinctName, String contestName, int contestFormat) {
		super(precinctNo, precinctName);
		this.contestName = contestName;
		this.contestFormat = contestFormat;
	}
	
	public String getContestName() {
		return contestName;
	}

	public int getContestFormat() {
		return contestFormat;
	}

	public String toString() {
		return String.format("Contest:%n  name: %s format:%d", contestName, contestFormat);
	}
	
}
