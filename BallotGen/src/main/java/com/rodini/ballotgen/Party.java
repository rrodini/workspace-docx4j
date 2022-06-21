package com.rodini.ballotgen;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public enum Party {
	DEMOCRATIC("Democratic"),
	REPUBLICAN("Republican"),
	INDEPENDENT("Independent"),
	DEM_REP("Democratic/Republican"),
	REP_DEM("Republican/Democratic"),
	LIBERTARIAN("Libertarian"),
	NOAFFILIATION("No Affiliation"),
	LEADERSHIPFORALL("Leadership for All");
	
	private static final Logger logger = LoggerFactory.getLogger(Party.class);

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
		default: logger.error("can't convert string to Party: " + display);
		}
		if (party == null) {
			logger.error("cannot convert value to Party enum: " + display);
		}
		return party;
	}
	@Override
	public String toString() {
		return display;
	}
	
}