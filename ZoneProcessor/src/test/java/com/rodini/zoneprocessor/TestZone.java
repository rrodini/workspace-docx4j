package com.rodini.zoneprocessor;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class TestZone {

	@BeforeEach
	void setUp() throws Exception {
	}

	@AfterEach
	void tearDown() throws Exception {
	}

	@Test
	void testConstructor() {
		Zone zone = new Zone("03", "Generic zone","./zone-logo-03.jpg", "https://www-zone-03.com/", "./default.docx");
		assertEquals("03", zone.getZoneNo());
		assertEquals("Generic zone", zone.getZoneName());
		assertEquals("./zone-logo-03.jpg", zone.getZoneLogoPath());
		assertEquals("https://www-zone-03.com/", zone.getZoneUrl());
		assertEquals("./default.docx", zone.getZoneChunkPath());
	}	
}
