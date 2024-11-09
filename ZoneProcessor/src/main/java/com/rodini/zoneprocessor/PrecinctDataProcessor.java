package com.rodini.zoneprocessor;

import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.rodini.ballotutils.Utils;

public class PrecinctDataProcessor {
	static final Logger logger = LogManager.getLogger(PrecinctDataProcessor.class);

	private PrecinctDataProcessor() {
	}

	// Process valid data from single CSV line.
	static void processLine(String precinctNo, String precinctName, String zoneNo) {
		// Precinct No must be 3 characters.
		String normalPrecinctNo = Utils.normalizePrecinctNo(Integer.parseInt(precinctNo));
		// Zone No must be 2 characters.
		String normalZoneNo = Utils.normalizeZoneNo(Integer.parseInt(zoneNo));
		logger.debug(String.format("precinctNo: %s precinctName: %s zoneNo: %s", normalPrecinctNo, precinctName,
				normalZoneNo));
		Precinct precinct = PrecinctFactory.findOrCreate(normalPrecinctNo, precinctName, normalZoneNo);
	}

	// Process data from single CSV line.
	static void processData(int lineNo, String[] fields) {
		if (fields.length < 3) {
			logger.error(String.format("precinct CSV line #%d fewer than 3 fields", lineNo));
			return;
		}
		if (fields.length > 3) {
			logger.error(String.format("precinct CSV line #%d more than 3 fields", lineNo));
			return;
		}
		String field0 = "";
		String field1 = "";
		String field2 = "";
		for (int i = 0; i < fields.length; i++) {
			switch (i) {
			case 0:
				// precinct no. - should be unique.
				field0 = fields[0].trim();
				if (field0.isBlank() || field0.length() > 3) {
					logger.error(String.format("precinct CSV line #%d precinct no. %s has error", lineNo, field0));
					return;
				}
				break;
			case 1:
				// precinct name - not used.
				field1 = fields[1].trim();
				break;
			case 2:
				// zone no.
				field2 = fields[2].trim();
				if (field2.isBlank()) {
					logger.error(String.format("precinct CSV line #%d zone no. %s has error", lineNo, field2));
					return;
				}
				processLine(field0, field1, field2);
				break;
			}
		}
	}

	/**
	 * processPrecinctsText processes the precinctsText into Precinct objects.
	 * 
	 * @param precinctsText part of precincts-zones CSV text.
	 */
	public static void processPrecinctsText(String precinctsText) {
		logger.debug("Processing precincts CSV text");
		String[] precinctLines = precinctsText.split("\n");
		for (int i = 0; i < precinctLines.length; i++) {
			String csvLine = precinctLines[i];
			// Comment lines start with #.
			if (!csvLine.startsWith("#")) {
				String[] fields = csvLine.split(",");
				processData(i + 1, fields);
			}
		}
	}

}
