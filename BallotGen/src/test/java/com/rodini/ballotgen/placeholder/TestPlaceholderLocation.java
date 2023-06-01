package com.rodini.ballotgen.placeholder;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class TestPlaceholderLocation {

	@BeforeEach
	void setUp() throws Exception {
	}

	@AfterEach
	void tearDown() throws Exception {
	}

	@Test
	void testEnum() {
		for (PlaceholderLocation loc: PlaceholderLocation.values()) {
			switch (loc) {
			case HEADER:
				break;
			case BODY:
				break;
			case FOOTER:
				break;
			default:
				fail("Improper Placeholder location");
			}
		}
		assertTrue(true);
	}

}
