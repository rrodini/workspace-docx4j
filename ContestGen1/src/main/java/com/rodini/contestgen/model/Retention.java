package com.rodini.contestgen.model;

public class Retention extends VoteFor{

	private String officeName;
	private String judgeName; 

	public Retention(String precinctNo, String precinctName, String officeName, String judgeName) {
		super(precinctNo, precinctName);
		this.officeName = officeName;
		this.judgeName = judgeName;
	}

	public String getOfficeName() {
		return officeName;
	}
	
	public String getJudgeName() {
		return judgeName;
	}
    
	public String toString() {
		return String.format("Retention:%n office: %s judge:%s", officeName, judgeName);	
	}
}
