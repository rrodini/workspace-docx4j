package com.rodini.ballotgen;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class TestEndorsement {

	Endorsement end;
	@BeforeEach
	void setUp() throws Exception {
		end = new Endorsement("Robert A Rodini",
				EndorsementType.ZONE, 13);
	}

	@AfterEach
	void tearDown() throws Exception {
	}

	@Test
	void testConstructor() {
		assertTrue(end != null);
	}
	@Test
	void testGetName() {
		assertTrue("Robert A Rodini".equals(end.getName()));
		
	}
	@Test
	void testGetType() {
		assertTrue(EndorsementType.ZONE == end.getType());
	}
	@Test
	void testGetZoneNo() {
		assertTrue(13 == end.getZoneNo());
	}
	@Test 
	void testToString() {
		String expected = "Endorsement: Robert A Rodini, ZONE, 13";
		assertEquals(expected, end.toString());
	}
	
}