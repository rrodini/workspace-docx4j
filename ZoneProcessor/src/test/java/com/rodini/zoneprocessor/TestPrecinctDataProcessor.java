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

class TestPrecinctDataProcessor {

	private static MockedAppender mockedAppender;
	private static Logger logger;

	@BeforeAll
	static void setupClass() {
	    mockedAppender = new MockedAppender();
	    mockedAppender.start();
	    logger = (Logger)LogManager.getLogger(ZoneFactory.class);
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
		ZoneFactory.clearZones();
	    mockedAppender.messages.clear();
	}

	@AfterEach
	void tearDown() throws Exception {
	}
	
	@Test
	void testPrecinctData1() {
		String precinctText =
		"""
		#precinct no,precinct name, zone no
		5,ATGLEN,3
		""";
		PrecinctDataProcessor.processPrecinctsText(precinctText);
		Map<String, Precinct> map = PrecinctFactory.getPrecincts();
		Set<String> keys = map.keySet();
		assertEquals(1, keys.size());
		Precinct precinct1 = map.get("005");
		assertEquals("005", precinct1.getPrecinctNo());
		assertEquals("ATGLEN", precinct1.getPrecinctName());
		assertEquals("03", precinct1.getZoneNo());
	}

	@Test
	void testPrecinctData3() {
		String precinctText =
		"""
		#precinct no,precinct name, zone no
		5,ATGLEN,3
		10,AVONDALE,8
		14,BIRMINGHAM 1,6
		""";
		PrecinctDataProcessor.processPrecinctsText(precinctText);
		Map<String, Precinct> map = PrecinctFactory.getPrecincts();
		Set<String> keys = map.keySet();
		assertEquals(3, keys.size());
		Precinct precinct1 = map.get("005");
		assertEquals("005", precinct1.getPrecinctNo());
		assertEquals("ATGLEN", precinct1.getPrecinctName());
		assertEquals("03", precinct1.getZoneNo());
		Precinct precinct2 = map.get("010");
		assertEquals("010", precinct2.getPrecinctNo());
		assertEquals("AVONDALE", precinct2.getPrecinctName());
		assertEquals("08", precinct2.getZoneNo());
		Precinct precinct3 = map.get("014");
		assertEquals("014", precinct3.getPrecinctNo());
		assertEquals("BIRMINGHAM 1", precinct3.getPrecinctName());
		assertEquals("06", precinct3.getZoneNo());
	}
}
