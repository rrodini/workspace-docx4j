package com.rodini.contestgen.model;
/**
 * VoteFor class is the abstract parent of concrete classes: Contest, Referendum, Retention.
 * The subclass names are similar to those in BallotGen program.
 * 
 * @author Bob Rodini
 *
 */
public abstract class VoteFor {
	protected String precinctNo;
	protected String precinctName;
	
	public VoteFor(String precinctNo, String precinctName) {
		this.precinctNo = precinctNo;
		this.precinctName = precinctName;
	}
	String getPrecinctNo() {
		return precinctNo;
	}

	String getPrecinctName() {
		return precinctName;
	}
	
}
