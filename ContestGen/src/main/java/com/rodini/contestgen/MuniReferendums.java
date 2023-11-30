package com.rodini.contestgen;

import java.util.ArrayList;
import java.util.List;

public class MuniReferendums extends MuniContestsQuestions {

	List<Referendum> referendums;

	public MuniReferendums(String muniName) {
		super(muniName);
		referendums = new ArrayList<Referendum>();
	}
	/**
	 * add adds a Referendum object to the list.
	 * @param r ContestName object to add.
	 */
	public void add(Referendum ref) {
		referendums.add(ref);
	}
	/**
	 * get returns the list of ContestName objects.
	 * @return the list of ContestName objects.
	 */
	public List<Referendum> get() {
		return referendums;
	}
	
	public String getMuniReferendumsText() {
		StringBuilder sb = new StringBuilder();
		referendums.forEach(ref -> sb.append(ref + "\n"));
		return sb.toString();
	}

	
}
