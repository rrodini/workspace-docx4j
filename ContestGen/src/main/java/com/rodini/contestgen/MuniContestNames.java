package com.rodini.contestgen;

import java.util.ArrayList;
import java.util.List;

/** 
 * MuniContestName is a simple container for a list of ContestName objects.
 * One for each municipality.
 * 
 * @author Bob Rodini
 *
 */

public class MuniContestNames extends MuniContestsQuestions {
	
	List<ContestName> contestNames;

	public MuniContestNames(String muniName) {
		super(muniName);
		contestNames = new ArrayList<ContestName>();
	}
	/**
	 * add adds a ContestName object to the list.
	 * @param c ContestName object to add.
	 */
	public void add(ContestName c) {
		contestNames.add(c);
	}
	/**
	 * get returns the list of ContestName objects.
	 * @return the list of ContestName objects.
	 */
	public List<ContestName> get() {
		return contestNames;
	}
	/**
	 * intersect performs a mathematical set intersection between
	 * the invoking object (this) and the parametric object (that)
	 * under the assumption that common contests are at the beginning
	 * of each list and are in the same cardinal order.
	 * 
	 * Note:
	 * - This method must be called as each new municipality is processed.
	 * 
	 * @param that another MuniContestNames object
	 * @return a new list reflecting the common contests.
	 */
	public MuniContestNames intersect(MuniContestNames that) {
		MuniContestNames common = new MuniContestNames("common");
		List<ContestName> thisList = this.contestNames;
		List<ContestName> thatList = that.contestNames;
		int size = Math.min(thisList.size(), thatList.size());
		for (int i = 0; i < size; i++) {
			if (thisList.get(i).getName().equals(thatList.get(i).getName()) &&
				thisList.get(i).getFormat() ==   thatList.get(i).getFormat()  ) {
				common.add(thisList.get(i));
			} else {
				// something's not common so break loop;
				break;
			}
		}
		return common;
	}
	/**
	 * getMuniContestsText returns a long string with all of
	 * the contest names.
	 * 
	 * Note:
	 * - This should only be called AFTER all of the contests for
	 *   the municipality have been identified.
	 */
	public String getMuniContestsText() {
		StringBuilder sb = new StringBuilder();
		contestNames.forEach(cn -> sb.append(cn + "\n"));
		return sb.toString();
	}
}
