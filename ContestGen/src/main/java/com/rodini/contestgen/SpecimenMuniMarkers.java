package com.rodini.contestgen;

import java.util.Properties;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.rodini.ballotutils.Utils;

public class SpecimenMuniMarkers {
	private static final Logger logger = LoggerFactory.getLogger(SpecimenMuniMarkers.class);

	private static int repeatCount; // # of times the ballot name is repeated
	private static String muniNameRegex;
	private static Pattern muniNamePattern;
	
	public static void initialize(String resourceFilePath) {
		Properties props;
		props = Utils.loadProperties(resourceFilePath);
		repeatCount = Integer.parseInt(Utils.getPropValue(props, "muniNameRepeatCount"));
		muniNameRegex = Utils.getPropValue(props, "muniNameRegex");
		logger.info(String.format("muniNameRegex: %s%n", muniNameRegex));
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
