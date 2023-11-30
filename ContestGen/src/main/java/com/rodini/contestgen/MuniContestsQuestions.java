package com.rodini.contestgen;

import java.util.List;

public abstract class MuniContestsQuestions {

	String muniName;
	String muniNo;	// precinct # - 3 characters

	
	public MuniContestsQuestions(String muniName) {
		this.muniName = muniName;
		this.muniNo = muniName.substring(0, 3);
	}

	/**
	 * getMuniName return the municipality's name.
	 * @return return the municipality's name.
	 */
	String getMuniName() {
		return muniName;
	}
	/**
	 * getMuniNo return the municipality's #.
	 * @return return the municipality's #.
	 */
	String getMuniNo() {
		return muniNo;
	}
	
}
