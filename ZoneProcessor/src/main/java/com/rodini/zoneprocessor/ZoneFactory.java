package com.rodini.zoneprocessor;


import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;

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
	//             key: zoneNo  value: Zone object
	private static Map<String,  Zone> zoneMap = new TreeMap<>();
	
	public static Zone findOrCreate(String zoneNo, String zoneName) {
		Zone zone = null;
		Set<String> keySet = zoneMap.keySet();
		if (!keySet.contains(zoneNo)) {
			zone = new Zone(zoneNo, zoneName);
			zoneMap.put(zoneNo, zone);
		} else {
			zone = zoneMap.get(zoneNo);
			String name = zone.getZoneName();
			if (!zoneName.equals(name)) {
				logger.warn(String.format("zoneName %s differs from %s", zoneName, name));
			}
		}
		return zone;
	}
	
	public static Map<String, Zone> getZones() {
		return zoneMap;
	}
	// Use only for testing!
	public static void clearZones() {
		zoneMap = new TreeMap<>();
	}
}
