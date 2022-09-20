package com.rodini.ballotgen;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class TestCandidate {
//  General Candidate
	@Test
	void testConstructor1() {
		GeneralCandidate candidate = new GeneralCandidate("Joe Biden", Party.DEMOCRATIC, "");
		assertEquals("Joe Biden", candidate.getName());
		assertEquals(Party.DEMOCRATIC, candidate.getParty());
		assertEquals("Democratic", candidate.getTextBeneathName());
	}
	@Test
	void testConstructor2() {
		GeneralCandidate candidate = new GeneralCandidate("Austin Davis", null, "Lieutenant Governor, Democratic");
		assertEquals("Austin Davis", candidate.getName());
		assertEquals(null, candidate.getParty());
		assertEquals("Lieutenant Governor, Democratic", candidate.getTextBeneathName());
	}
	@Test
	void testToString1() {
		String expected = "Joe Biden : Democratic";
		GeneralCandidate candidate = new GeneralCandidate("Joe Biden", Party.DEMOCRATIC, "");
		assertEquals(expected, candidate.toString());
	}
	@Test
	void testToString2() {
		String expected = "Austin Davis : Lieutenant Governor, Democratic";
		GeneralCandidate candidate = new GeneralCandidate("Austin Davis", null, "Lieutenant Governor, Democratic");
		assertEquals(expected, candidate.toString());
	}
// Primary Candidate
	@Test
	void testConstructor3() {
		PrimaryCandidate candidate = new PrimaryCandidate("Joe Biden", "Delaware");
		assertEquals("Joe Biden", candidate.getName());
		assertEquals("Delaware", candidate.getResidence());
	}
	@Test
	void testToString3() {
		String expected = "Joe Biden : Delaware";
		PrimaryCandidate candidate = new PrimaryCandidate("Joe Biden", "Delaware");
		assertEquals(expected, candidate.toString());
	}
}
