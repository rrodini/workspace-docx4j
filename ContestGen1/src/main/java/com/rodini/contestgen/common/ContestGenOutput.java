package com.rodini.contestgen.common;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
/**
 * ContestGenOutput is a simple enum class that converts a string value to an enum object.
 * It exists so that the corresponding property value can be written in upper or lower case.
 * @author Bob Rodini
 *
 */
public enum ContestGenOutput {
	CONTESTS("contests"),
	BALLOTS("ballots"),
	BOTH("both");
	private static final Logger logger = LogManager.getLogger(ContestGenOutput.class);

	private String display;
	ContestGenOutput(String display) {
		this.display = display;
	}

	public static ContestGenOutput toEnum(String display) {
		ContestGenOutput output = null;
		switch (display) {
		case "contests": output = CONTESTS; break;
		case "CONTESTS": output = CONTESTS; break;
		case "ballots" : output = BALLOTS;  break;
		case "BALLOTS" : output = BALLOTS;  break;
		case "both"    : output = BOTH;     break;
		case "BOTH"    : output = BOTH;     break;
		default: logger.error("can't convert property to ContestGenOutput: " + display);
		}
		return output;
	}
	@Override
	public String toString() {
		return display;
	}

}
