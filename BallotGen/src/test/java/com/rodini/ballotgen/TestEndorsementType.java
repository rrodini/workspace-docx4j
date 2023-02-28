package com.rodini.ballotgen;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class TestEndorsementType {

	@BeforeEach
	void setUp() throws Exception {
	}

	@AfterEach
	void tearDown() throws Exception {
	}

	@Test
	void testEnum() {
		for (EndorsementType type: EndorsementType.values()) {
			switch (type) {
		case STATE:
			assertEquals("STATE", type.toString());
			break;
		case COUNTY:
			assertEquals("COUNTY", type.toString());
			break;
		case ZONE:
			assertEquals("ZONE", type.toString());
			break;
			}
		}
	}

}
