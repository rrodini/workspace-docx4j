package com.rodini.ballotutils;

//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


public enum Party {
	DEMOCRATIC("Democratic"),
	REPUBLICAN("Republican"),
	INDEPENDENT("Independent"),
	DEM_REP("Democratic/Republican"),
	REP_DEM("Republican/Democratic"),
	LIBERTARIAN("Libertarian"),
	NOAFFILIATION("No Affiliation"),
	LEADERSHIPFORALL("Leadership for All"),
	GREEN_PARTY("Green Party"),
	KEYSTONE("Keystone"),
	AMERICAN_SOLIDARITY_PARTY("American Solidarity Party"),
	CONSTITUTION_PARTY("Constitution Party"),
	FORWARD_PARTY("Forward Party");

	
	private static final Logger logger = LogManager.getLogger(Party.class);

	private String display;
	Party(String display) {
		this.display = display;
	}
	public static Party toEnum(String display) {
		Party party = null;
		switch (display) {
		case "Democratic":	party = DEMOCRATIC; break;
		case "Republican":	party = REPUBLICAN; break;
		case "Independent":	party = INDEPENDENT; break;
		case "Democratic/Republican":	party = DEM_REP; break;
		case "Republican/Democratic":	party = REP_DEM; break;
		case "Libertarian":	party = LIBERTARIAN; break;
		case "No Affiliation":	party = NOAFFILIATION; break;
		case "Leadership for All":	party = LEADERSHIPFORALL; break;
		case "Green Party":	party = GREEN_PARTY; break;
		case "Keystone":	party = KEYSTONE; break;
		// Below is no longer an ERROR, just an INFO
		default: logger.info("can't convert string to Party: " + display);
		}
		return party;
	}
	@Override
	public String toString() {
		return display;
	}
	
}
