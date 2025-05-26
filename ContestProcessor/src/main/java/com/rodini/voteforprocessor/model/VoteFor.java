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
	
	/**
	 * processContestName handles the special case where the name (or question) is
	 * split over two lines of text, e.g. "Judge of the\nCourt of Commonwealth Pleas"
	 * 
	 * Note: Not sure why embedded \n doesn't work, but this does.
	 * @param name contest name which may have embedded \n
	 * @return new contest name build dynamically
	 */
	public static String processName(String name) {
		String [] elements = name.split("\\\\n");
		StringBuffer sb = new StringBuffer();
		int i;
		for (i = 0; i < elements.length - 1; i++) {
			sb.append(elements[i]);
			sb.append("\n");
		}
		if (i < elements.length) {
			sb.append(elements[i]);
		}
		return sb.toString();
	}
}
