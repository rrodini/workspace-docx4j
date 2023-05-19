package com.rodini.ballotgen;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class TestEndorsementMode {

	@BeforeEach
	void setUp() throws Exception {
	}

	@AfterEach
	void tearDown() throws Exception {
	}

	@Test
	void testEnum() {
		for (EndorsementMode mode: EndorsementMode.values()) {
			switch (mode) {
		case ENDORSED:
			assertEquals("ENDORSED", mode.toString());
			break;
		case UNENDORSED:
			assertEquals("UNENDORSED", mode.toString());
			break;
		case ANTIENDORSED:
			assertEquals("ANTIENDORSED", mode.toString());
			break;
			}
		}
	}

}
