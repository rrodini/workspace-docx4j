package com.rodini.ballotgen.common;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public enum ElectionType {
	PRIMARY("Primary"),
	GENERAL("General");
	
	private static final Logger logger = LoggerFactory.getLogger(ElectionType.class);

	private String display;
	ElectionType(String display) {
		this.display = display;
	}
	public static ElectionType toEnum(String display) {
		ElectionType type = null;
		switch (display) {
		case "Primary":	type = PRIMARY; break;
		case "General":	type = GENERAL; break;
		}
		if (type == null) {
			logger.error("cannot convert value to ElectionType enum: " + display);
		}
		return type;
	}
	@Override
	public String toString() {
		return display;
	}

}
