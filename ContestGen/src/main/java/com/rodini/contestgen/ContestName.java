package com.rodini.contestgen;
/** 
 * ContestName is a simple container for the name
 * and the format # (regex) to be used by the BallotGen program.
 * @author Bob Rodini
 *
 */
public class ContestName {
	private String name;	// name of the contest
	private int format;		// index of format that works

	public ContestName(String name, int format) {
		this.name = name;
		this.format = format;
	}

	String getName() {
		return name;
	}

	int getFormat() {
		return format;
	}
	@Override
	public String toString() {
		return String.format("%s, %d", name, format);
	}
}
