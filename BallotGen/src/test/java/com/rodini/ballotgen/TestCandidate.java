package com.rodini.ballotgen;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class TestGeneralCandidate {

	@Test
	void testConstructor() {
		GeneralCandidate candidate = new GeneralCandidate("Joe Biden", Party.DEMOCRATIC);
		assertEquals("Joe Biden", candidate.getName());
		assertEquals(Party.DEMOCRATIC, candidate.getParty());
	}
	@Test
	void testToString() {
		String expected = "Joe Biden : Democratic";
		GeneralCandidate candidate = new GeneralCandidate("Joe Biden", Party.DEMOCRATIC);
		assertEquals(expected, candidate.toString());
	}
}
