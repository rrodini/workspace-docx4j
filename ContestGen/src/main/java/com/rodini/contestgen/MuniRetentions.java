package com.rodini.contestgen;

import java.util.ArrayList;
import java.util.List;

public class MuniRetentions extends MuniContestsQuestions{

	List<Retention> retentions;

	public MuniRetentions(String muniName) {
		super(muniName);
		retentions = new ArrayList<Retention>();
	}
	/**
	 * add adds a Retention object to the list.
	 * @param r ContestName object to add.
	 */
	public void add(Retention ret) {
		retentions.add(ret);
	}
	/**
	 * get returns the list of ContestName objects.
	 * @return the list of ContestName objects.
	 */
	public List<Retention> get() {
		return retentions;
	}
	
	public String getMuniReferendumsText() {
		StringBuilder sb = new StringBuilder();
		retentions.forEach(ret -> sb.append(ret + "\n"));
		return sb.toString();
	}

}
