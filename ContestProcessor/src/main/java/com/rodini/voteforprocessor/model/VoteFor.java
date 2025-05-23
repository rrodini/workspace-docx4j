package com.rodini.voteforprocessor.model;
/**
 * VoteFor class is the abstract parent of concrete classes: Contest, Referendum, Retention.
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
