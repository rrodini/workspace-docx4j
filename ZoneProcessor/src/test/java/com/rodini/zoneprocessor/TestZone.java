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
	void testConstlructor() {
		Zone zone = new Zone("02", "Generic zone");
		assertEquals("02", zone.getZoneNo());
		assertEquals("Generic zone", zone.getZoneName());
	}	
}
