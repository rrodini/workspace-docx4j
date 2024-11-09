package com.rodini.ballotgen.common;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
/**
 * BallotGenOutput is a simple enum class that converts a string value to an enum object.
 * @author Bob Rodini
 *
 */
public enum BallotGenOutput {
	PRECINCT("precinct"),
	UNIQUE("unique"),
	BOTH("both");
	private static final Logger logger = LogManager.getLogger(BallotGenOutput.class);

	private String display;
	BallotGenOutput(String display) {
		this.display = display;
	}

	public static BallotGenOutput toEnum(String display) {
		BallotGenOutput output = null;
		switch (display) {
		case "precinct": output = PRECINCT; break;
		case "PRECINCT": output = PRECINCT; break;
		case "unique"  : output = UNIQUE;   break;
		case "UNIQUE"  : output = UNIQUE;   break;
		case "both"    : output = BOTH;     break;
		case "BOTH"    : output = BOTH;     break;
		default: logger.error("can't convert property to BallotGenOutput: " + display);
		}
		return output;
	}
	@Override
	public String toString() {
		return display;
	}

}
