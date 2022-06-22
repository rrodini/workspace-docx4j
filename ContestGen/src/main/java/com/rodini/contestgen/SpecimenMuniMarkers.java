package com.rodini.contestgen;

import java.util.Properties;
import java.util.regex.Pattern;

public class SpecimenMuniMarkers {

	private static int repeatCount; // # of times the ballot name is repeated
	private static String muniNameRegex;
	private static Pattern muniNamePattern;
	
	public static void initialize(boolean useResourceFile, String resourceFilePath) {
		if (useResourceFile) {
			Properties props;
			props = Utils.loadProperties(resourceFilePath);
			repeatCount = Integer.parseInt(Utils.getPropValue(props, "muniNameRepeatCount"));
			muniNameRegex = Utils.getPropValue(props, "muniNameRegex");
//System.out.printf("muniNameRegex: %s%n", muniNameRegex);
		} else {
			repeatCount = 2;
			muniNameRegex = "(?m)^OFFICIAL MUNICIPAL ELECTION BALLOT$\n(?<id>\\d+)[\\s]*(?<name>.*)\n";
		}
		muniNamePattern = Utils.compileRegex(muniNameRegex);
	}
	/**
	 * getRepeatCount return the # of times the ballot name repeats in 
	 * the specimen text.
	 * @return the # of times the ballot name repeats in the specimen text.
	 */
	static int getRepeatCount() {
		return repeatCount;
	}
	/**
	 * getMuniNamePattern returns the compiled regex that demarcates
	 * the municipality text in the master ballot.
	 * @return the compiled regex that demarcates the municipality text.
	 */
	static Pattern getMuniNamePattern() {
		return muniNamePattern;
	}

}
