package com.rodini.ballotgen;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class TestEndorsementScope {

	@BeforeEach
	void setUp() throws Exception {
	}

	@AfterEach
	void tearDown() throws Exception {
	}

	@Test
	void testEnum() {
		for (EndorsementScope scope: EndorsementScope.values()) {
			switch (scope) {
		case STATE:
			assertEquals("STATE", scope.toString());
			break;
		case COUNTY:
			assertEquals("COUNTY", scope.toString());
			break;
		case ZONE:
			assertEquals("ZONE", scope.toString());
			break;
			}
		}
	}

}
