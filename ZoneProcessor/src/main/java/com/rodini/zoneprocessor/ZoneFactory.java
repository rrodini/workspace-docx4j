package com.rodini.zoneprocessor;

import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * ZoneFactory creates a new Zone provided it doesn't already exist.
 * 
 * @author Bob Rodini
 *
 */
public class ZoneFactory {
	static final Logger logger = LogManager.getLogger(ZoneFactory.class);
	// key: zoneNo value: Zone object.
	private static Map<String, Zone> zoneMap = new TreeMap<>();
	// prevent instantiation.
	private ZoneFactory() {
	}
	/**
	 * findOrCreate a new Zone object. Create is the expected outcome.
	 * 
	 * @param zoneNo
	 * @param zoneName
	 * @param zoneLogoPath
	 * @return Zone object.
	 */
	public static Zone findOrCreate(String zoneNo, String zoneName, String zoneLogoPath,
			String zoneUrl, String zoneChunkPath) {
		Zone zone = null;
		Set<String> keySet = zoneMap.keySet();
		if (!keySet.contains(zoneNo)) {
			zone = new Zone(zoneNo, zoneName, zoneLogoPath, zoneUrl, zoneChunkPath);
			zoneMap.put(zoneNo, zone);
		} else {
			// TODO: check for duplicate zone names, duplicate zone file paths?
			logger.error(String.format("zoneNo %s is duplicated.", zoneNo));
		}
		return zone;
	}
	/**
	 * getZones returns the map of Zones.
	 * @return
	 */
	public static Map<String, Zone> getZones() {
		return zoneMap;
	}

	// Use only for testing!
	public static void clearZones() {
		zoneMap = new TreeMap<>();
	}
}
