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

import com.rodini.ballotutils.Utils;
/**
 * Uses: test-good.csv
 *       test-bad.csv
 *       test-duplicate.csv
 *       
 * @author Bob Rodini
 *
 */
class TestGenMuniMap {

	private static MockedAppender mockedAppender;
	private static Logger logger;

	@BeforeAll
	static void setupClass() {
	    mockedAppender = new MockedAppender();
	    mockedAppender.start();
	    logger = (Logger)LogManager.getLogger(ZoneProcessor.class);
	    logger.addAppender(mockedAppender);
	    logger.setLevel(Level.ERROR);
	}

	@AfterAll
	public static void teardown() {
		logger.removeAppender(mockedAppender);
		mockedAppender.stop();
	}

	
	@BeforeEach
	void setUp() throws Exception {
	    mockedAppender.messages.clear();
		ZoneFactory.clearZones();
		ZoneProcessor.clearMuniNoMap();
	}

	@AfterEach
	void tearDown() throws Exception {
	}

	@Test
	void testMuniMapGoodCsv() {
		String csvText = Utils.readTextFile("./src/test/java/test-good.csv");
		ZoneProcessor.processCSVText(csvText);
		assertEquals(0, mockedAppender.messages.size());
		Map<String, Zone> muniNoMap = ZoneProcessor.getPrecinctZoneMap();
		assertEquals(3, muniNoMap.keySet().size());
	}
	@Test
	void testMuniMapBadCsv() {
		String csvText = Utils.readTextFile("./src/test/java/test-bad.csv");
		ZoneProcessor.processCSVText(csvText);
		assertEquals(7, mockedAppender.messages.size());
		// Errors:
		// CSV line #2 precinct no. 0005 has error
		// CSV line #3 precinct no.  has error
		// CSV line #4 fewer than 4 fields
		// CSV line #5 zone name  has error
		// CSV line #7 fewer than 4 fields
		// CSV line #8 more than 4 fields
		// CSV line #9 zone no. 600 has error
		Map<String, Zone> muniNoMap = ZoneProcessor.getPrecinctZoneMap();
		assertEquals(1, muniNoMap.keySet().size());
	}
	@Test
	void testMuniMapDuplicateCsv() {
		String csvText = Utils.readTextFile("./src/test/java/test-duplicate.csv");
		ZoneProcessor.processCSVText(csvText);
		assertEquals(1, mockedAppender.messages.size());
		Map<String, Zone> muniNoMap = ZoneProcessor.getPrecinctZoneMap();
		assertEquals(3, muniNoMap.keySet().size());
	}
	
}
