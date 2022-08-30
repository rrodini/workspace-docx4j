package com.rodini.ballotzipper;

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
	    logger = (Logger)LogManager.getLogger(GenMuniMap.class);
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
		GenMuniMap.clearMuniNoMap();
	}

	@AfterEach
	void tearDown() throws Exception {
	}

	@Test
	void testMuniMapGoodCsv() {
		String [] args = {"./src/test/java/test-good.csv", "./src/test/java/test-dir-02", "./src/test/java/test-dir-02"};
		Initialize.initialize(args);
		GenMuniMap.processCSVFile();
		assertEquals(0, mockedAppender.messages.size());
		Map<String, Zone> muniNoMap = GenMuniMap.getMuniNoMap();
		assertEquals(3, muniNoMap.keySet().size());
	}
	@Test
	void testMuniMapBadCsv() {
		String [] args = {"./src/test/java/test-bad.csv", "./src/test/java/test-dir-02", "./src/test/java/test-dir-02"};
		Initialize.initialize(args);
		GenMuniMap.processCSVFile();
		assertEquals(7, mockedAppender.messages.size());
		// Errors:
		// CSV line #2 precinct no. 0005 has error
		// CSV line #3 precinct no.  has error
		// CSV line #4 fewer than 4 fields
		// CSV line #5 zone name  has error
		// CSV line #7 fewer than 4 fields
		// CSV line #8 more than 4 fields
		// CSV line #9 zone no. 600 has error
		Map<String, Zone> muniNoMap = GenMuniMap.getMuniNoMap();
		assertEquals(1, muniNoMap.keySet().size());
	}
	@Test
	void testMuniMapDuplicateCsv() {
		String [] args = {"./src/test/java/test-duplicate.csv", "./src/test/java/test-dir-02", "./src/test/java/test-dir-02"};
		Initialize.initialize(args);
		GenMuniMap.processCSVFile();
		assertEquals(1, mockedAppender.messages.size());
		Map<String, Zone> muniNoMap = GenMuniMap.getMuniNoMap();
		assertEquals(3, muniNoMap.keySet().size());
	}
	
}
