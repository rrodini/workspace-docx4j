package com.rodini.zoneprocessor;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Map;
import java.util.Set;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.Logger;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class TestPrecinctFactory {

	private static MockedAppender mockedAppender;
	private static Logger logger;

	@BeforeAll
	static void setupClass() {
	    mockedAppender = new MockedAppender();
	    mockedAppender.start();
	    logger = (Logger)LogManager.getLogger(PrecinctFactory.class);
	    logger.addAppender(mockedAppender);
	    // Use WARN here since duplicate precincts #s happen in Chesco!
	    logger.setLevel(Level.INFO);
	}

	@AfterAll
	public static void teardown() {
		logger.removeAppender(mockedAppender);
		mockedAppender.stop();
	}

	
	@BeforeEach
	void setUp() throws Exception {
		PrecinctFactory.clearPrecincts();
	    mockedAppender.messages.clear();
	}

	@AfterEach
	void tearDown() throws Exception {
	}
	@Test
	void testOnePrecinct() {
		String precinctNo = "005";
		String precinctName = "ATGLEN";		
		String precinctZoneNo = "03";
		Precinct precinct1 = PrecinctFactory.findOrCreate(precinctNo, precinctName, precinctZoneNo);
		Map<String, Precinct> map = PrecinctFactory.getPrecincts();
		Set<String> keys = map.keySet();
		assertEquals(1, keys.size());
		assertEquals(precinct1, map.get(precinctNo));	
	}
	@Test
	void testTwoPrecincts() {
		String precinctNo1 = "005";
		String precinctName1 = "ATGLEN";		
		String precinctZoneNo1 = "03";
		Precinct precinct1 = PrecinctFactory.findOrCreate(precinctNo1, precinctName1, precinctZoneNo1);
		String precinctNo2 = "010";
		String precinctName2 = "AVONDALE";		
		String precinctZoneNo2 = "08";
		Precinct precinct2 = PrecinctFactory.findOrCreate(precinctNo2, precinctName2, precinctZoneNo2);
		Map<String, Precinct> map = PrecinctFactory.getPrecincts();
		Set<String> keys = map.keySet();
		assertEquals(2, keys.size());
		assertEquals(precinct1, map.get(precinctNo1));	
		assertEquals(precinct2, map.get(precinctNo2));	
	}
	@Test
	void testDuplicatePrecincts() {
		String precinctNo1 = "005";
		String precinctName1 = "ATGLEN";		
		String precinctZoneNo1 = "03";
		Precinct precinct1 = PrecinctFactory.findOrCreate(precinctNo1, precinctName1, precinctZoneNo1);
		Precinct precinct2 = PrecinctFactory.findOrCreate(precinctNo1, precinctName1, precinctZoneNo1);
		Map<String, Precinct> map = PrecinctFactory.getPrecincts();
		Set<String> keys = map.keySet();
		assertEquals(1, keys.size());
		assertEquals(precinct1, map.get(precinctNo1));	
		assertEquals(1, mockedAppender.messages.size());
		String message = mockedAppender.messages.get(0);
		assertEquals("precinctNo 005 is duplicated.", message);		
	}
}
