package com.rodini.zoneprocessor;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.rodini.ballotutils.Utils;

/**
 * ZoneDataProcessor processes the zone lines of the precincts-zones CSV file
 * into zone objects.
 * 
 * @author Bob Rodini
 */
public class ZoneDataProcessor {
	static final Logger logger = LogManager.getLogger(ZoneDataProcessor.class);

	private ZoneDataProcessor() {
	}

	// Process valid data from single CSV line.
	static void processLine(String zoneNo, String zoneName, String zoneLogoPath) {
		// Precinct No must be 3 characters.
		// Zone No must be 2 characters.
		String normalZoneNo = Utils.normalizeZoneNo(Integer.parseInt(zoneNo));
		logger.debug(String.format("zoneNo: %s zoneName: %s zoneLogoPath: %s", normalZoneNo, zoneName, zoneLogoPath));
		Zone zone = ZoneFactory.findOrCreate(normalZoneNo, zoneName, zoneLogoPath);
	}

	// Process data from single CSV line.
	static void processData(int lineNo, String[] fields) {
		if (fields.length < 3) {
			logger.error(String.format("zone CSV line #%d fewer than 3 fields", lineNo));
			return;
		}
		if (fields.length > 3) {
			logger.error(String.format("zone CSV line #%d more than 3 fields", lineNo));
			return;
		}
		String field0 = "";
		String field1 = "";
		String field2 = "";
		for (int i = 0; i < fields.length; i++) {
			switch (i) {
			case 0:
				// zone no. - should be unique.
				field0 = fields[0].trim();
				if (field0.isBlank() || field0.length() > 2) {
					logger.error(String.format("CSV line #%d zone no. %s has error", lineNo, field0));
					return;
				}
				break;
			case 1:
				// zone name.
				field1 = fields[1].trim();
				break;
			case 2:
				// zone logo path.
				field2 = fields[2].trim();
				if (field2.isBlank()) {
					logger.error(String.format("CSV line #%d zone logo path %s is blank", lineNo, field2));
					return;
				}
				boolean exists = Utils.checkFileExists(field2);
				if (!exists) {
					logger.error(String.format("CSV line #%d zone logo path %s invalid", lineNo, field2));
					return;
				}
				processLine(field0, field1, field2);
				break;
			}
		}
	}

	/**
	 * processZonesText processes the zonesText into Zone objects.
	 * 
	 * @param zonesText part of precincts-zones CSV text.
	 */
	static void processZonesText(String zonesText) {
		logger.debug("Processing zones CSV text");
		String[] zoneLines = zonesText.split("\n");
		for (int i = 0; i < zoneLines.length; i++) {
			String csvLine = zoneLines[i];
			// Comment lines start with #.
			if (!csvLine.startsWith("#")) {
				String[] fields = csvLine.split(",");
				processData(i + 1, fields);
			}
		}
	}

}
