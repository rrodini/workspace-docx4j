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

class TestZoneDataProcessor {

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
	void testZoneData1() {
		String zoneText =
		"""
		#zone no,zone name,zone logo path
		3,OXGrove,./src/test/java/zone-logo-03.jpg
		""";
		ZoneDataProcessor.processZonesText(zoneText);
		Map<String, Zone> map = ZoneFactory.getZones();
		Set<String> keys = map.keySet();
		assertEquals(1, keys.size());
		Zone zone1 = map.get("03");
		assertEquals("03", zone1.getZoneNo());
		assertEquals("OXGrove", zone1.getZoneName());
		assertEquals("./src/test/java/zone-logo-03.jpg", zone1.getZoneLogoPath());
	}
	@Test
	void testZoneData3() {
		String zoneText =
		"""
    	#zone no,zone name,zone logo path
	    3,OXGrove,./src/test/java/zone-logo-03.jpg
	    6,Thornbury,./src/test/java/zone-logo-06.jpg
	    8,KAD,./src/test/java/zone-logo-08.jpg
		""";
		ZoneDataProcessor.processZonesText(zoneText);
		Map<String, Zone> map = ZoneFactory.getZones();
		Set<String> keys = map.keySet();
		assertEquals(3, keys.size());
		Zone zone1 = map.get("03");
		assertEquals("03", zone1.getZoneNo());
		assertEquals("OXGrove", zone1.getZoneName());
		assertEquals("./src/test/java/zone-logo-03.jpg", zone1.getZoneLogoPath());
		Zone zone2 = map.get("06");
		assertEquals("06", zone2.getZoneNo());
		assertEquals("Thornbury", zone2.getZoneName());
		assertEquals("./src/test/java/zone-logo-06.jpg", zone2.getZoneLogoPath());
		Zone zone3 = map.get("08");
		assertEquals("08", zone3.getZoneNo());
		assertEquals("KAD", zone3.getZoneName());
		assertEquals("./src/test/java/zone-logo-08.jpg", zone3.getZoneLogoPath());
	}
}
