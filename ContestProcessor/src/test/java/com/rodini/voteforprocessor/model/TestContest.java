package com.rodini.voteforprocessor.model;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.rodini.ballotutils.Party;
//import com.rodini.ballotgen.contest.Candidate;
//import com.rodini.ballotgen.contest.Contest;
//import com.rodini.ballotgen.contest.GeneralCandidate;
import com.rodini.voteforprocessor.model.Candidate;
import com.rodini.voteforprocessor.model.Contest;
import com.rodini.voteforprocessor.model.GeneralCandidate;

class TestContest {
	Contest contest;
	String precinctNo = "350";
	String precinctName = "Malvern";
	
	@BeforeEach
	void setUp() throws Exception {
		GeneralCandidate cand1 = new GeneralCandidate("Maria McLaughlin", Party.DEMOCRATIC, "", false);
		GeneralCandidate cand2 = new GeneralCandidate("Kevin Brobson", Party.REPUBLICAN, "", false);
		List<Candidate> cands = List.of(cand1, cand2);
		contest = new Contest(precinctNo, precinctName, "Justice of the Supreme Court", "", "Vote for ONE", cands, 1);
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
							 "Candidates: Maria McLaughlin : Democratic , " +
							 "Kevin Brobson : Republican "
							 ;
		assertEquals(contestText, contest.toString());
	}

}
