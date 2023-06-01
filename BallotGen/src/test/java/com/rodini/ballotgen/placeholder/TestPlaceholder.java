package com.rodini.ballotgen.placeholder;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static com.rodini.ballotgen.placeholder.PlaceholderLocation.*;
class TestPlaceholder {
	
	
	@BeforeEach
	void setUp() throws Exception {
	}

	@AfterEach
	void tearDown() throws Exception {
	}

	@Test
	void testConstructor() {
		Placeholder ph = new Placeholder("placeholder", HEADER, null);
		assertTrue(ph instanceof Placeholder);
	}

	@Test
	void testGetters() {
		Placeholder ph = new Placeholder("placeholder", BODY, null);
		assertEquals("placeholder", ph.getName());
		assertEquals(BODY, ph.getLoc());
		assertEquals(null, ph.getReplaceParagraph());
	}
	
}
