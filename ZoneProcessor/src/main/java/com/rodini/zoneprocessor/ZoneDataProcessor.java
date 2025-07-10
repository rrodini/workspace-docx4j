package com.rodini.zoneprocessor;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.rodini.ballotutils.Utils;

/**
 * ZoneDataProcessor processes the zone lines of the precincts-zones CSV file
 * into zone objects.
 * 
 * Notes:
 * 1) the CSV input file must not be edited by Excel since Excel embeds invisible
 *    UTF8 character at the beginning of the file.
 * 
 * @author Bob Rodini
 */
public class ZoneDataProcessor {
	static final Logger logger = LogManager.getLogger(ZoneDataProcessor.class);
	static final int MIN_FIELD_NO = 3;
	static final int MAX_FIELD_NO = 5;
	
	
	private ZoneDataProcessor() {
	}

	// Process valid data from single CSV line.
	static void processLine(String zoneNo, String zoneName, String zoneLogoPath, String zoneUrl, String zoneChunkPath) {
		// Precinct No must be 3 characters.
		// Zone No must be 2 characters.
		String normalZoneNo = Utils.normalizeZoneNo(Integer.parseInt(zoneNo));
		logger.debug(String.format("zoneNo: %s zoneName: %s zoneLogoPath: %s zoneUrl: %s zoneChunkPath: %s", 
				normalZoneNo, zoneName, zoneLogoPath, zoneUrl, zoneChunkPath));
		Zone zone = ZoneFactory.findOrCreate(normalZoneNo, zoneName, zoneLogoPath, zoneUrl, zoneChunkPath);
	}

	// Process data from single CSV line.
	// Format: zone no. (number), zone name (text), zone logo (image), zone website (url), zone chunk (docx)
	static void processData(int lineNo, String[] fields) {
		if (fields.length < MIN_FIELD_NO ) {
			logger.error(String.format("zone CSV line #%d fewer than 3 fields", lineNo));
			return;
		}
		if (fields.length > MAX_FIELD_NO) {
			logger.error(String.format("zone CSV line #%d more than 5 fields", lineNo));
			return;
		}
		String field0 = "";
		String field1 = "";
		String field2 = "";
		String field3 = "";
		String field4 = "";
		boolean exists;
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
					logger.info(String.format("CSV line #%d zone logo path %s is blank", lineNo, field2));
				}
				exists = Utils.checkFileExists(field2);
				if (!exists) {
					logger.info(String.format("CSV line #%d zone logo path %s invalid", lineNo, field2));
				}
				break;
			case 3:
				// zone website url.
				field3 = fields[3].trim();
				if (field3.isBlank()) {
					logger.info(String.format("CSV line #%d zone url %s is blank", lineNo, field3));
				}
				break;
			case 4:
				// zone chunk.
				field4 = fields[4].trim();
				if (field4.isBlank()) {
					logger.info(String.format("CSV line #%d zone chunk %s is blank", lineNo, field4));
				}
				exists = Utils.checkFileExists(field4);
				if (!exists) {
					logger.info(String.format("CSV line #%d zone chunk path %s invalid", lineNo, field4));
				}
				break;
			}
		}
		processLine(field0, field1, field2, field3, field4);
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
