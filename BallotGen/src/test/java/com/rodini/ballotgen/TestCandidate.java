package com.rodini.ballotgen;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class TestCandidate {
//  General Candidate
	@Test
	void testConstructor1() {
		GeneralCandidate candidate = new GeneralCandidate("Joe Biden", Party.DEMOCRATIC, "", false);
		assertEquals("Joe Biden", candidate.getName());
		assertEquals("", candidate.getTextBeneathName());
		assertEquals(Party.DEMOCRATIC, candidate.getParty());
	}
	@Test
	void testConstructor2() {
		GeneralCandidate candidate = new GeneralCandidate("Austin Davis", Party.DEMOCRATIC, "Lieutenant Governor, Democratic", true);
		assertEquals("Austin Davis", candidate.getName());
		assertEquals(Party.DEMOCRATIC, candidate.getParty());
		assertEquals("Lieutenant Governor, Democratic", candidate.getTextBeneathName());
	}
	@Test
	void testToString1() {
		String expected = "Joe Biden : Democratic ";
		GeneralCandidate candidate = new GeneralCandidate("Joe Biden", Party.DEMOCRATIC, "", false);
		assertEquals(expected, candidate.toString());
		assertEquals(Party.DEMOCRATIC, candidate.getParty());
	}
	@Test
	void testToString2() {
		String expected = "Austin Davis : Democratic Lieutenant Governor, Democratic";
		GeneralCandidate candidate = new GeneralCandidate("Austin Davis", Party.DEMOCRATIC, "Lieutenant Governor, Democratic", true);
		assertEquals(expected, candidate.toString());
	}
// Primary Candidate
	@Test
	void testConstructor3() {
		PrimaryCandidate candidate = new PrimaryCandidate("Joe Biden", Party.DEMOCRATIC, "Delaware");
		assertEquals("Joe Biden", candidate.getName());
		assertEquals(Party.DEMOCRATIC, candidate.getParty());
		assertEquals("Delaware", candidate.getResidence());
	}
	@Test
	void testToString3() {
		String expected = "Joe Biden : Democratic Delaware";
		PrimaryCandidate candidate = new PrimaryCandidate("Joe Biden", Party.DEMOCRATIC, "Delaware");
		assertEquals(expected, candidate.toString());
		assertEquals(Party.DEMOCRATIC, candidate.getParty());
	}
}
