package com.rodini.contestgen.model;

public class Referendum extends VoteFor {
	String refQuestion;
	String refText;
		
	public Referendum(String precinctNo, String precinctName, String refQuestion, String refText) {
		super(precinctNo, precinctName);
		this.refQuestion = refQuestion;
		this.refText = refText;
	}

	public String getRefQuestion() {
		return refQuestion;
	}

	public String getRefText() {
		return refText;
	}
	
	public String toString() {
		return String.format("Referendum:%n question: %s text:%s", refQuestion, refText);
	}

}
