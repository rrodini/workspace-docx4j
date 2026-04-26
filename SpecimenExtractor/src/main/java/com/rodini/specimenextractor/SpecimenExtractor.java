package com.rodini.specimenextractor;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.rodini.ballotutils.Utils;

public class SpecimenExtractor {
	private static final Logger logger = LogManager.getRootLogger();

	private SpecimenExtractor() {}
	
	public static void main(String[] args){
		// Get the logging level from JVM parameter on command line.
		Utils.setLoggingLevel(LogManager.getRootLogger().getName());
		String msg = String.format("Start of SpecimenExtractor app. Version: %s", "0.1.0");
		Utils.logAppMessage(logger, msg, true);
//
		Utils.logAppMessage(logger, msg, false);
		Utils.logAppErrorCount(logger);
		msg = "End of SpecimenExtractor app.";
		Utils.logAppMessage(logger, msg, true);
	}
}
