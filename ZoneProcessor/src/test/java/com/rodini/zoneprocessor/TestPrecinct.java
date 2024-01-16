package com.rodini.zoneprocessor;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class TestPrecinct {

	@BeforeEach
	void setUp() throws Exception {
	}

	@AfterEach
	void tearDown() throws Exception {
	}
	
	@Test
	void testConstructor() {
		Precinct precinct = new Precinct("005", "Generic precinct","03");
		assertEquals("005", precinct.getPrecinctNo());
		assertEquals("Generic precinct", precinct.getPrecinctName());
		assertEquals("03", precinct.getZoneNo());
	}	
}
