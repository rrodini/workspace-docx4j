package com.rodini.ballotgen;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.stream.Collectors.toList;

import java.util.ArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.rodini.zoneprocessor.Zone;
import com.rodini.zoneprocessor.GenMuniMap;
import com.rodini.ballotutils.Utils;

/**
 * WriteinFactory processes the writeinCSVText into Endorsement objects.
 * It is the only class that knows the format of the CSV file (writeins.csv).
 * 
 * @author Bob Rodini
 */


public class WriteinFactory {
	private static final Logger logger = LoggerFactory.getLogger(WriteinFactory.class);
	// Precinct no. (key)  List of Writeins (value)
	// 754                 Writein1, Writein2, ...
	private static Map <String, List<Writein>> precinctWriteins = new HashMap<>();
	// Map constructed by zoneProcessor component.
	private static Map<String, Zone> precinctToZoneMap;
	// Process a number. It must be numeric.
	static int processNumber(int lineNo, String numStr) {	
		int val = 0;
		try {
			val = Integer.parseInt(numStr);
		} catch (NumberFormatException e) {
			logger.error(String.format("CSV line #%d number expected but got: %s", lineNo, numStr));
		}	
		return val;
	}
		
	// Process a valid line to an Writein object.
	static void processLine(String candName, String contestName,
			int zoneNo, int [] precinctNos) {
		// turn int [] to List of strings representing precinct nos.
		List<String> muniNoList = Arrays.stream(precinctNos)
				.mapToObj(i -> Utils.normalizeMuniNo(i))
				.collect(toList());
		Writein writein = new Writein(candName, contestName, zoneNo, muniNoList);
		for (String muniNo: muniNoList) {
			List<Writein> writeins = precinctWriteins.get(muniNo);
			if (writeins == null) {
				writeins = new ArrayList<>();
			}
			writeins.add(writein);
			precinctWriteins.put(muniNo, writeins);
		}
	}
	// Process and validate data from single CSV line.
	static void processData(int lineNo, String[] fields) {
		int len = fields.length;
		if (len < 6) {
			logger.error(String.format("CSV line #%d has fewer than 6 fields", lineNo));
			return;
		}
		String candName = "";
		String contestName = "";
		int zoneNo = 0;
		int [] precinctNos = null;
		for (int i = 0; i < fields.length; i++) {
			String field = fields[i].trim();
			switch (i) {
			case 0:
				// Candidate name
				candName = field;
				if (candName.isBlank() || candName.length() < 3) {
					logger.error(String.format("CSV line #%d write-in name %s has error", lineNo, candName));
					return;
				}
				break;
			case 1:
				// Contest name
				contestName = field;
				if (contestName.isBlank() || contestName.length() < 3) {
					logger.error(String.format("CSV line #%d write-in contest name %s has error", lineNo, contestName));
					return;
				}
				break;
			case 2:
				// literal "Zone"
				if (!field.toUpperCase().equals("ZONE")) {
					logger.error(String.format("CSV line #%d field should be \"Zone\" but is \"%s\"", lineNo, field));
					return;
				}
				break;
			case 3:
				// Zone #
				if (field.isBlank()  ) {
					logger.error(String.format("CSV line #%d zone # is blank", lineNo));
					return;
				}
				zoneNo = processNumber(lineNo, field);
				if (zoneNo == 0) {
					return;
				}
				break;
			case 4:
				// literal "Precincts"
				if (!field.toUpperCase().equals("PRECINCTS")) {
					logger.error(String.format("CSV line #%d field should be \"Precincts\" but is \"%s\"", lineNo, field));
					return;
				}
				break;
			case 5: /* 5 to N */
				String [] precinctStrs = Arrays.copyOfRange(fields, 5, len);
				precinctNos = new int[precinctStrs.length];
				for (int j=0; j < precinctStrs.length; j++) {
					String precinctStr = precinctStrs[j].trim();
					int no = processNumber(lineNo, precinctStr);
					if (no > 0) {
						precinctNos[j] = no;
					} else {
						// bad precinctNo so quit.
						return;
					}
				}
				precinctNos = validateZoneOwnership(lineNo, zoneNo, precinctNos);
				break;
			}
		}
		processLine(candName, contestName, zoneNo, precinctNos);
	}
	// zoneNo - zone that is "writing in" the candidate
	// precinctNos - array of precincts for write-in candidate
	private static int[] validateZoneOwnership(int lineNo, int zoneNo, int[] precinctNos) {
		if (precinctToZoneMap == null || precinctToZoneMap.keySet().size() == 0) {
			Utils.logFatalError("failed to initialize precinctToZoneMap. Quitting");
		}
		int [] validNos = new int[precinctNos.length];
		int j = 0;
		//String zoneStr = normalizeNo(zoneNo, 2);
		String zoneStr = Utils.normalizeZoneNo(zoneNo);
		for (int i = 0; i < precinctNos.length; i ++) {
			String precinctStr = Utils.normalizeMuniNo(precinctNos[i]);
			if (!GenMuniMap.zoneOwnsPrecinct(zoneStr, precinctStr)) {
				logger.error(String.format("CSV line #%d rejected. Zone %s doesn't own Precinct %s",lineNo, zoneStr, precinctStr));
			} else {
				validNos[j++] = precinctNos[i];
			}
		}
		return validNos;
	}

	// process the input CSV text.
	public static void processCSVText(String csvText) {
		logger.debug("Processing writeins CSV text");
		if (csvText.isBlank()) {
			// there may not be a write-in file.
			return;
		}
		String[] csvLines = csvText.split("\n");
		// No header, so start at 0
		for (int i = 0; i < csvLines.length; i++) {
			String csvLine = csvLines[i];
			//System.out.println(csvLine);
			String[] fields = csvLine.split(",");
			processData(i + 1, fields);
		}

	}
	// Set the precinctZonesMap
	// Note - must be called before process() method.
	public static void setPrecinctToZones(Map<String, Zone> precinctToZoneMap) {
		WriteinFactory.precinctToZoneMap = precinctToZoneMap;
	}
	
	// Get the map of write-ins for a precinct.
	public static Map<String, List<Writein>> getPrecinctWriteins() {
		return precinctWriteins;
	}
	// For testing only.
	public static void clearPrecinctWriteins() {
		precinctWriteins = new HashMap<>();
	}}
