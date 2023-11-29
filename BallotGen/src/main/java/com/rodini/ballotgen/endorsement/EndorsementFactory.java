package com.rodini.ballotgen.endorsement;

import static com.rodini.ballotgen.endorsement.EndorsementMode.*;
import static com.rodini.ballotgen.endorsement.EndorsementScope.*;
import static java.util.stream.Collectors.toSet;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.rodini.ballotgen.contest.Contest;

/**
 * EndorsementFactory processes the endorsementsCSVText into Endorsement objects.
 * It is the only class that knows the format of the CSV file (endorsements.csv).
 * 
 * @author Bob Rodini
 */
public class EndorsementFactory {
	private static final Logger logger = LogManager.getLogger(EndorsementFactory.class);

	static Map <String, List<Endorsement>> candidateEndorsements = new HashMap<>();
	// Get the names of endorseMode as a String
	static final Set<String> endorseModeNames = Arrays.stream(EndorsementMode.values())
			.map(e -> e.toString()).collect(toSet());
	// Get the names of endorseScope as a String.
	static final Set<String> endorseScopeNames = Arrays.stream(EndorsementScope.values())
			.map(e -> e.toString()).collect(toSet());
	
	// Process the zone number. It must be numeric.
	static int processNumber(int lineNo, String numStr) {	
		int val = 0;
		try {
			val = Integer.parseInt(numStr);
		} catch (NumberFormatException e) {
			logger.error(String.format("CSV line #%d zone number expected but got: %s", lineNo, numStr));
		}	
		return val;
	}
	// Process a valid line to an Endorsement object.
	static void processLine(String name, EndorsementMode mode, EndorsementScope type, int zoneNo) {
		Endorsement e = new Endorsement(name, mode, type, zoneNo);
		// Eliminate case-sensitivity here!
		// Retrieval must use upper-case names!
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
		if (fields.length < 3) {
			logger.error(String.format("CSV line #%d has fewer than 3 fields", lineNo));
			return;
		}
		if (fields.length > 4) {
			logger.error(String.format("CSV line #%d has more than 4 fields", lineNo));
			return;
		}
		// Defaults
		String name = "";
		EndorsementMode mode = UNENDORSED;
		EndorsementScope scope = ZONE;
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
				// EndorsementMode - ENDORSED, UNENDORSED, ANTIENDORSED
				String endorseMode = fields[1].trim().toUpperCase();
				if (endorseMode.isBlank() || !endorseModeNames.contains(endorseMode)) {
					logger.error(String.format("CSV line #%d endorsement mode %s has error", lineNo, endorseMode));
					return;
				}
				mode = EndorsementMode.valueOf(endorseMode);
				break;
			case 2:
				// EndorsementScope - STATE, COUNTY, ZONE
				String endorseScope = fields[2].trim().toUpperCase();
				if (endorseScope.isBlank() || !endorseScopeNames.contains(endorseScope)) {
					logger.error(String.format("CSV line #%d endorsement scope %s has error", lineNo, endorseScope));
					return;
				}
				scope = EndorsementScope.valueOf(endorseScope);
				if (scope == ZONE && fields.length < 4) {
					logger.error(String.format("CSV line #%d zone # missing", lineNo));
					return;
				}
				if ((scope == STATE || scope == COUNTY) && fields.length > 3) {
					logger.error(String.format("CSV line #%d has too many fields", lineNo));
					return;
				}
				break;
			case 3:
				// zone #
				String zoneStr = fields[3].trim();
				if (zoneStr.isBlank()  ) {
					logger.error(String.format("CSV line #%d zone # is blank", lineNo));
					return;
				}
				zoneNo = processNumber(lineNo, zoneStr);
				break;
			}
		}
		processLine(name, mode, scope, zoneNo);
	}
	// Process the input CSV text.
	public static void processCSVText(String csvText) {
		logger.debug("Processing endorsments CSV text");
		if (csvText.isBlank()) {
			// there may not be an endorsement file.
			return;
		}
		String[] csvLines = csvText.split("\n");
		// No header, so start at 0
		for (int i = 0; i < csvLines.length; i++) {
			String csvLine = csvLines[i];
			// lines that start with # are comments
			if (!csvLine.startsWith("#")) {
				String[] fields = csvLine.split(",");
				processData(i + 1, fields);
			}
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
