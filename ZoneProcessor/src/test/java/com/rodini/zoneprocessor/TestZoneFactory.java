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

class TestZoneFactory {

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
	void testOneZone() {
		String zoneNo = "03";
		String zoneName = "Generic Zone Three"; 
		String zoneLogoPath = "./zone-logo-03.jpg"; 
		String zoneUrl = "https://www-zone-03.com/";
		String zoneChunkPath = "./default.docx"; 
		
		Zone zone1 = ZoneFactory.findOrCreate(zoneNo, zoneName, zoneLogoPath, zoneUrl, zoneChunkPath);
		Map<String, Zone> map = ZoneFactory.getZones();
		Set<String> keys = map.keySet();
		assertEquals(1, keys.size());
		assertEquals(zone1, map.get(zoneNo));
	}
	@Test
	void testTwoZones() {
		String zoneNo1 = "03";
		String zoneName1 = "Generic Zone Three"; 
		String zoneLogoPath1 = "./zone-logo-03.jpg"; 
		String zoneUrl1 = "https://www-zone-03.com/";
		String zoneChunkPath1 = "./default.docx"; 
		String zoneNo2 = "06";
		String zoneName2 = "Generic Zone Six"; 
		String zoneLogoPath2 = "./zone-logo-06.jpg"; 
		String zoneUrl2 = "https://www-zone-06.com/";
		String zoneChunkPath2 = "./default.docx"; 
		Zone zone1 = ZoneFactory.findOrCreate(zoneNo1, zoneName1, zoneLogoPath1, zoneUrl1, zoneChunkPath1);
		Zone zone2 = ZoneFactory.findOrCreate(zoneNo2, zoneName2, zoneLogoPath2, zoneUrl2, zoneChunkPath2);
		Map<String, Zone> map = ZoneFactory.getZones();
		Set<String> keys = map.keySet();
		assertEquals(2, keys.size());
		assertEquals(zone1, map.get(zoneNo1));	
		assertEquals(zone2, map.get(zoneNo2));	
	}
	@Test
	void testDuplicateZones() {
		String zoneNo1 = "03";
		String zoneName1 = "Generic Zone Three"; 
		String zoneLogoPath1 = "./zone-logo-03.jpg"; 
		String zoneUrl1 = "https://www-zone-03.com/";
		String zoneChunkPath1 = "./default.docx"; 
		Zone zone1 = ZoneFactory.findOrCreate(zoneNo1, zoneName1, zoneLogoPath1, zoneUrl1, zoneChunkPath1);
		Zone zone2 = ZoneFactory.findOrCreate(zoneNo1, zoneName1, zoneLogoPath1, zoneUrl1, zoneChunkPath1);
		Map<String, Zone> map = ZoneFactory.getZones();
		Set<String> keys = map.keySet();
		assertEquals(1, keys.size());
		assertEquals(zone1, map.get(zoneNo1));	
		assertEquals(1, mockedAppender.messages.size());
		String message = mockedAppender.messages.get(0);
		assertEquals("zoneNo 03 is duplicated.", message);		
	}
}
