package com.rodini.contestgen;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class TestContestName {
	ContestName cn;

	@BeforeEach
	void setUp() throws Exception {
		cn = new ContestName("Justice of the Supreme Court", 1);
	}

	@AfterEach
	void tearDown() throws Exception {
	}

	@Test
	void testConstructor() {
		assertTrue(cn != null);
	}
	@Test
	void testGetName() {
		assertEquals("Justice of the Supreme Court", cn.getName());
	}
	@Test
	void testGetFormat() {
		assertEquals(1, cn.getFormat());
	}
	@Test
	void testToString() {
		assertEquals("Justice of the Supreme Court, 1", cn.toString());
	}

}
