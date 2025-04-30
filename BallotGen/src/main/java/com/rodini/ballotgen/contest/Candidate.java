package com.rodini.ballotgen.contest;

//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


import com.rodini.ballotgen.common.Party;

/**
 * Candidate class is just a value object that holds candidate name.
 */
public abstract class Candidate {
	private static final Logger logger = LogManager.getLogger(Candidate.class);

	protected String name;
	protected Party party;
	
	protected Candidate(String name, Party party) {
		// normalize the candidate name
		this.name = normalizeName(name);
		this.party = party;
	}
	// convert "Clay Cauley, Sr." => "Clay Cauley Sr."
	private String normalizeName(String name) {
		String normalizedName = name;
		int commaIndex = name.indexOf(",");
		if (commaIndex > 0) {
			normalizedName = name.substring(0, commaIndex) + name.substring(commaIndex+1, name.length());
		}
		return normalizedName;
	}
	
	public String getName() {
		return name;
	}
	public Party getParty() {
		return party;
	}
}
