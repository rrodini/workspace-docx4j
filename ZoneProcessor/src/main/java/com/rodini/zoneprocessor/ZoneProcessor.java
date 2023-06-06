package com.rodini.zoneprocessor;

import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.rodini.ballotutils.Utils;

/** 
 * ZoneProcessor class generates the precinctZone data structure and also generates zone objects 
 * in the process. This map will later be aligned with the DocxNoMap.
 * 
 * Note: User of this module must read the zone CSV file and then call 
 * processCSVText() method.
 * 
 * @author Bob Rodini
 *
 */
public class ZoneProcessor {
	static final Logger logger = LoggerFactory.getLogger(ZoneProcessor.class);

	// Municipality No (key) Zone (value)
	// 020                   7
	//                       Bradford
	//                       muniFiles(020), muniFiles(021), ...
	private static final Map<String, Zone> muniNoMap = new TreeMap<>();
	
	
	// Disable constructor
	private ZoneProcessor() {
	}
	// Process valid data from single CSV line.
	static void processLine(String precinctNo, String precinctName, String zoneNo, String zoneName) {
		// Precinct No must be 3 characters.
		String normalPrecinctNo = Utils.normalizeMuniNo(Integer.parseInt(precinctNo));
		// Zone No must be 2 characters.
		String normalZoneNo = Utils.normalizeZoneNo(Integer.parseInt(zoneNo));
		logger.debug(String.format("precinctNo: %s precinctName: %s zoneNo: %s zoneName: %s",
				normalPrecinctNo, precinctName, normalZoneNo, zoneName));
//		System.out.printf("precinctNo: %s precinctName: %s zoneNo: %s zoneName: %s%n",
//				normalPrecinctNo, precinctName, normalZoneNo, zoneName);
		Zone zone = ZoneFactory.findOrCreate(normalZoneNo, zoneName);
		Set<String> muniNoKeys = muniNoMap.keySet();
		if (muniNoKeys.contains(normalPrecinctNo)) {
			logger.error(String.format("duplicate precinct no. %s", normalPrecinctNo));
		} else {
			muniNoMap.put(normalPrecinctNo, zone);
		}
	}
	// Process data from single CSV line.
	static void processData(int lineNo, String [] fields) {
		if (fields.length < 4) {
			logger.error(String.format("CSV line #%d fewer than 4 fields", lineNo));
			return;
		}
		if (fields.length > 4) {
			logger.error(String.format("CSV line #%d more than 4 fields", lineNo));
			return;
		}
		for (int i=0; i < fields.length; i++) {
			switch (i) {
			case 0:
				// precinct no. - should be unique.
				if (fields[0].isBlank() || fields[0].length() > 3) {
					logger.error(String.format("CSV line #%d precinct no. %s has error", lineNo, fields[0]));
					return;
				}
				break;
			case 1:
				// precinct name - not used since name may change.
				break;
			case 2:
				// zone name - expect many duplicates.
				if (fields[2].isBlank()) {
					logger.error(String.format("CSV line #%d zone name %s has error", lineNo, fields[2]));
					return;
				}
				break;
			case 3:
				// zone no. - expect many duplicates.
				if (fields[3].isBlank() || fields[3].length() > 2) {
					logger.error(String.format("CSV line #%d zone no. %s has error", lineNo, fields[3]));
					return;
				}
				// notice the change in field order.
				processLine(fields[0], fields[1], fields[3], fields[2]);
				break;
			}			
		}
	}
	// Process the input CSV text.
	public static void processCSVText(String csvText) {
		logger.debug("Processing CSV text");
		String [] csvLines = csvText.split("\n");
		// skip the header line by starting at 1.
		for (int i = 1; i < csvLines.length; i++) {
			String csvLine = csvLines[i];
			String [] fields = csvLine.split(",");
			processData(i+1,fields);
		}
	}
	// zoneOwnsPrecinct is needed for semantic checks in other programs.
	public static boolean zoneOwnsPrecinct(String zoneNo, String precinctNo) {
		Zone zone = muniNoMap.get(precinctNo);
		if (zone == null) {
			return false;
		}
		return zone.getZoneNo().equals(zoneNo);
	}
	// Return the map to clients.
	public static Map<String, Zone> getPrecinctZoneMap() {
		return muniNoMap;
	}
	// Used only for testing
	public static void clearMuniNoMap() {
		muniNoMap.clear();
	}
}
