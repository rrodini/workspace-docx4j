package com.rodini.ballotgen;

import static com.rodini.ballotgen.EndorsementType.*;
import static java.util.stream.Collectors.toSet;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
/**
 * EndorsementFactory processes the endorsementsCSVTest into Endorsement objects.
 * It is the only class that knows the format of the CSV file (endorsements.csv).
 * 
 * @author Bob Rodini
 */
public class EndorsementFactory {
	private static final Logger logger = LoggerFactory.getLogger(EndorsementFactory.class);

	static Map <String, List<Endorsement>> candidateEndorsements = new HashMap<>();
	// Get the names of endorser as a String.
	static final Set<String> endorserNames = Arrays.stream(EndorsementType.values())
			.map(e -> e.toString()).collect(toSet());
	
	// Process the zone number. It must be numeric.
	static int processNumber(int lineNo, String numStr) {	
		int val = 0;
		try {
			val = Integer.parseInt(numStr);
		} catch (NumberFormatException e) {
			logger.error(String.format("CSV line #%d zone number error. See: ", lineNo, numStr));
		}	
		return val;
	}
	// Process a valid line to an Endorsement object.
	static void processLine(String name,EndorsementType type, int zoneNo) {
		Endorsement e = new Endorsement(name, type, zoneNo);
		// Eliminate case-sensitivity here!
		// Retrieval must upper-case names!
		name = name.toUpperCase();
		List<Endorsement> endorsements = candidateEndorsements.get(name);
		if (endorsements == null) {
			endorsements = new ArrayList<Endorsement>();
		}
		endorsements.add(e);
		candidateEndorsements.put(name, endorsements);
	}
	// Process and validate data from single CSV line.
	static void processData(int lineNo, String[] fields) {
		if (fields.length < 2) {
			logger.error(String.format("CSV line #%d fewer than 2 fields", lineNo));
			return;
		}
		if (fields.length > 3) {
			logger.error(String.format("CSV line #%d more than 3 fields", lineNo));
			return;
		}
		// Defaults
		String name = "";
		EndorsementType type = ZONE;
		int zoneNo = 0;
		for (int i = 0; i < fields.length; i++) {
			switch (i) {
			case 0:
				// Candidate Name - should be unique.
				name = fields[0].trim();
				if (name.isBlank() || name.length() < 3) {
					logger.error(String.format("CSV line #%d candidate name %s has error", lineNo, name));
					return;
				}
				break;
			case 1:
				// EndorsementType - STATE, COUNTY, ZONE
				String endorser = fields[1].trim().toUpperCase();
				if (endorser.isBlank() || !endorserNames.contains(endorser)) {
					logger.error(String.format("CSV line #%d endorsement type %s has error", lineNo, endorser));
					return;
				}
				type = EndorsementType.valueOf(endorser);
				if (type == ZONE && fields.length < 3) {
					logger.error(String.format("CSV line #%d zone # missing", lineNo));
					return;
				}
				if ((type == STATE || type == COUNTY) && fields.length > 2) {
					logger.error(String.format("CSV line #%d has too many fields", lineNo));
					return;
				}
				break;
			case 2:
				// zone #
				String zoneStr = fields[2].trim();
				if (zoneStr.isBlank()  ) {
					logger.error(String.format("CSV line #%d zone # is blank", lineNo));
					return;
				}
				zoneNo = processNumber(lineNo, zoneStr);
				break;
			}
		}
		processLine(name, type, zoneNo);
	}
	// Process the input CSV text.
	public static void processCSVText(String csvText) {
		logger.debug("Processing endorsments CSV text");
		String[] csvLines = csvText.split("\n");
		// No header, so start at 0
		for (int i = 0; i < csvLines.length; i++) {
			String csvLine = csvLines[i];
			//System.out.println(csvLine);
			String[] fields = csvLine.split(",");
			processData(i + 1, fields);
		}
	}
	// Get the map of endorsements for a candidate.
	public static Map<String, List<Endorsement>> getCandidateEndorsements() {
		return candidateEndorsements;
	}
	// For testing only.
	public static void clearCandidateEndorsements() {
		candidateEndorsements = new HashMap<>();
	}
}
