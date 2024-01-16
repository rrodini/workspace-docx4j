package com.rodini.zoneprocessor;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Map;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.Logger;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Disabled;

import com.rodini.ballotutils.Utils;
/**
 * Uses: test-good.csv
 *       test-bad.csv
 *       test-duplicate.csv
 *       
 * @author Bob Rodini
 *
 */
class TestZoneProcessor {

	private static MockedAppender mockedAppender;
	private static Logger logger;

	@BeforeAll
	static void setupClass() {
	    mockedAppender = new MockedAppender();
	    mockedAppender.start();
	    logger = (Logger)LogManager.getLogger(ZoneProcessor.class);
	    logger.addAppender(mockedAppender);
	}

	@AfterAll
	public static void teardown() {
		logger.removeAppender(mockedAppender);
		mockedAppender.stop();
	}

	
	@BeforeEach
	void setUp() throws Exception {
	    mockedAppender.messages.clear();
	    logger.setLevel(Level.ERROR);
		ZoneFactory.clearZones();
		PrecinctFactory.clearPrecincts();
		ZoneProcessor.clearPrecinctZoneMap();
	}

	@AfterEach
	void tearDown() throws Exception {
	}
	//@Disabled
	@Test
	void testPrecinctZoneMapGoodCsv() {
		String csvText = Utils.readTextFile("./src/test/java/test-good.csv");
		ZoneProcessor.processCSVText(csvText);
		assertEquals(0, mockedAppender.messages.size());
		Map<String, Zone> precinctZoneMap = ZoneProcessor.getPrecinctZoneMap();
		assertEquals(3, precinctZoneMap.keySet().size());
		// Note that zoneNo and precinctNo are normalized.
		assertEquals(true, ZoneProcessor.zoneOwnsPrecinct("03", "005"));
		assertEquals(true, ZoneProcessor.zoneOwnsPrecinct("08", "010"));
		assertEquals(true, ZoneProcessor.zoneOwnsPrecinct("06", "014"));
	}
	//@Disabled
	@Test
	void testPrecinctZoneMapBadCsv() {
		String csvText = Utils.readTextFile("./src/test/java/test-bad.csv");
		ZoneProcessor.processCSVText(csvText);
		// size == 2 since ZoneProcessor errors are monitored.
		assertEquals(2, mockedAppender.messages.size());
		// Errors:
		// precinct CSV line #2 precinct no. 0005 has error
		// precinct CSV line #3 precinct no.  has error
		// precinct CSV line #4 fewer than 3 fields
		// precinct CSV line #5 more than 3 fields
		// precinct CSV line #7 fewer than 3 fields
		// precinct CSV line #8 more than 3 fields
		// precinct: 010 has no zone.
		// precinct: 017 has no zone.
	}
	//@Disabled
	@Test
	void testPrecinctZoneMapDuplicateCsv() {
		// Need to log the PrecinctFactory class but can't switch.
		logger = (Logger)LogManager.getLogger(PrecinctFactory.class);
		logger.setLevel(Level.INFO);
		String csvText = Utils.readTextFile("./src/test/java/test-duplicate.csv");
		// Duplicate precinct # no longer an ERROR (11/24/2023)
		ZoneProcessor.processCSVText(csvText);
		assertEquals(0, mockedAppender.messages.size());
		Map<String, Zone> muniNoMap = ZoneProcessor.getPrecinctZoneMap();
		assertEquals(3, muniNoMap.keySet().size());
	}
	//@Disabled
	@Test
	void testPrecinctZoneMap2024() {
		System.out.println("Start 2024 precinct-zone CSV");
		String csvText = Utils.readTextFile("./src/test/java/test-precinct-zone-2024.csv");
		ZoneProcessor.processCSVText(csvText);
		System.out.println("End 2024 precinct-zone CSV");
	}
}
