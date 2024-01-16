package com.rodini.zoneprocessor;

import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * PrecinctFactory creates a new Precinct provided it doesn't already exist.
 * 
 * @author Bob Rodini
 *
 */
public class PrecinctFactory {
	static final Logger logger = LogManager.getLogger(PrecinctFactory.class);

	// key: precinctNo value: Precinct object.
	private static Map<String, Precinct> precinctMap = new TreeMap<>();
	// prevent instantiation.
	private PrecinctFactory() {
	}
	/**
	 * findOrCreate a new Precinct object. Create is the expected outcome.
	 * 
	 * @param precinctNo
	 * @param precinctName
	 * @param zoneNo
	 * @return Precinct object.
	 */
	public static Precinct findOrCreate(String precinctNo, String precinctName, String zoneNo) {
		Precinct precinct = null;
		Set<String> keySet = precinctMap.keySet();
		if (!keySet.contains(precinctNo)) {
			precinct = new Precinct(precinctNo, precinctName, zoneNo);
			precinctMap.put(precinctNo, precinct);
		} else {
			// Normally this would be an error, but precinct #356 in Chester County is duplicated!
			logger.info(String.format("precinctNo %s is duplicated.", precinctNo));
		}
		return precinct;
	}
	/**
	 * getPrecincts returns the map or Precincts.
	 * @return
	 */
	public static Map<String, Precinct> getPrecincts() {
		return precinctMap;
	}

	// Use only for testing!
	public static void clearPrecincts() {
		precinctMap = new TreeMap<>();
	}

}
