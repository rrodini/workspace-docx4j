package com.rodini.ballotgen;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class TestContest {
	Contest contest;
	
	@BeforeEach
	void setUp() throws Exception {
		GeneralCandidate cand1 = new GeneralCandidate("Maria McLaughlin", Party.DEMOCRATIC, "");
		GeneralCandidate cand2 = new GeneralCandidate("Kevin Brobson", Party.REPUBLICAN, "");
		List<Candidate> cands = List.of(cand1, cand2);
		contest = new Contest("Justice of the Supreme Court", "", "Vote for ONE", cands);
	}

	@AfterEach
	void tearDown() throws Exception {
	}

	@Test
	void testConstructor() {
		assertEquals("Justice of the Supreme Court", contest.getName());
		assertEquals("Vote for ONE", contest.getInstructions());
		assertTrue(2 == contest.getCandidates().size());
	}
	@Test
	void testToString() {
		String contestText = "Contest: " +
							 "Justice of the Supreme Court\n" +
							 "Vote for ONE\n" +
							 "Candidates: Maria McLaughlin : Democratic, " +
							 "Kevin Brobson : Republican"
							 ;
		assertEquals(contestText, contest.toString());
	}

}
