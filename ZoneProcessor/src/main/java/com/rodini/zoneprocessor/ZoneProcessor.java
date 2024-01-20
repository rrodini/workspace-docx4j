package com.rodini.zoneprocessor;

import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.rodini.ballotutils.Utils;

/**
 * ZoneProcessor class generates the precinctZone data structure and also
 * generates Zone and Precinct objects in the process.
 * 
 * Notes: 
 * 1) Clients of this module must read the zone CSV file and then call
 *    processCSVText() method.
 * 2) As of v. 1.5.x BallotGen and BallotZipper are clients.
 * 3) muniNo (municipality #) and precinctNo (precinct #) are the same.
 * 
 * @author Bob Rodini
 *
 */
public class ZoneProcessor {
	static final Logger logger = LogManager.getLogger(ZoneProcessor.class);

	// PrecinctNo (key) Zone object(value)
	// 020              Zone7
	private static final Map<String, Zone> precinctZoneMap = new TreeMap<>();

	// Disable constructor
	private ZoneProcessor() {
	}

	// Process the input CSV text.
	/** 
	 * processCSVText processed the contents of the precinct-zone CVS file into zone objects
	 * and Precinct objects.  It relies on its client to read the file from disk.
	 * 
	 * @param csvText contents of CSV file.
	 */
	public static void processCSVText(String csvText) {
		// Don't miss the last line of the precinctsText!
		if (!csvText.endsWith("\n")) {
			csvText += "\n";
		}
		logger.debug(String.format("Processing csvText:\n%s", csvText));
		if (csvText.isEmpty()) {
			Utils.logFatalError("precincts-zones CSV file is empty.");
		}
//		String regex = "(?mi)^Zones$\n(?<zonesdata>((.*\n)*))^Precincts$\n(?<precinctsdata>((.*\n)*))";
		String regex = "(?mi)^Zones$\\n(?<zonesdata>((.*\\n)*))^Precincts$\\n(?<precinctsdata>((.*\\n)*))";
		logger.debug("Regex: " + regex);
		// Since precincts-zones CSV is hard-coded, this regex can be hard-coded.
		Pattern pattern = Pattern
				.compile(regex);
		Matcher matcher = pattern.matcher(csvText);
		if (!matcher.find()) {
			Utils.logFatalError("precincts-zones CSV file does not match format");
		}
		String zonesText = matcher.group("zonesdata");
		String precinctsText = matcher.group("precinctsdata");
		ZoneDataProcessor.processZonesText(zonesText);
		PrecinctDataProcessor.processPrecinctsText(precinctsText);
		checkZones();
		checkPrecincts();
	}
	/**
	 * checkZones checks that each zone has at least one precinct.
	 */
	private static void checkZones() {
		Set<String> zoneKeys = ZoneFactory.getZones().keySet();
		Map<String, Precinct> precinctMap = PrecinctFactory.getPrecincts();
		Set<String> precinctKeys = precinctMap.keySet();
		for (String zoneNo: zoneKeys) {
			boolean foundPrecinct = false;
//			System.out.printf("zoneNo: %s%n", zoneNo);
			for (String precinctNo: precinctKeys) {
				Precinct precinct = precinctMap.get(precinctNo);
//				System.out.printf("precinctNo: %s%n", precinctNo);
				if (precinct.getZoneNo().equals(zoneNo)) {
//					System.out.printf("precinctNo: %s belongs to zoneNo: %s%n", precinctNo, zoneNo);
					foundPrecinct = true;
					break;
				}
			}
			if (!foundPrecinct) {
				logger.error(String.format("zone: %s has no precincts.", zoneNo));
			}
		}
	}
	/**
	 * checkPrecincts checks that each precinct belongs to a zone.
	 * As a side-effect it creates the precinctZoneMap.
	 */
	private static void checkPrecincts() {
		Map<String, Zone> zoneMap = ZoneFactory.getZones();
		Set<String> zoneKeys = zoneMap.keySet();
		Map<String, Precinct> precinctMap = PrecinctFactory.getPrecincts();
		Set<String> precinctKeys = precinctMap.keySet();
		for (String precinctNo: precinctKeys) {
			Precinct precinct = precinctMap.get(precinctNo);
			String zoneNo = precinct.getZoneNo();
			if (zoneKeys.contains(zoneNo)) {
				Zone zone = zoneMap.get(zoneNo);
				precinctZoneMap.put(precinctNo, zone);
			} else {
				logger.error(String.format("precinct: %s has no zone.", precinctNo));
			}
		}
	}
	/**
	 * zoneOwnsPrecinct checks that the given zone "owns" the given precinct.
	 * 
	 * @param zoneNo zone identity.
	 * @param precinctNo precinct identity.
	 * @return true => zone "owns" precinct.
	 */
	public static boolean zoneOwnsPrecinct(String zoneNo, String precinctNo) {
		Zone zone = precinctZoneMap.get(precinctNo);
		if (zone == null) {
			return false;
		}
		return zone.getZoneNo().equals(zoneNo);
	}
	/** 
	 * getPrecinctZoneMap returns this map to clients.
	 * @return 
	 */
	public static Map<String, Zone> getPrecinctZoneMap() {
		return precinctZoneMap;
	}

	// Used only for testing
	public static void clearPrecinctZoneMap() {
		precinctZoneMap.clear();
		// Must clear these too.
		PrecinctFactory.clearPrecincts();
		ZoneFactory.clearZones();
	}
}
