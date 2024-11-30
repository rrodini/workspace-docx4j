package com.rodini.contestgen.model;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class TestContest {
	@BeforeAll
	static void setUpBeforeClass() throws Exception {
	}

	@AfterAll
	static void tearDownAfterClass() throws Exception {
	}

	@BeforeEach
	void setUp() throws Exception {
	}

	@AfterEach
	void tearDown() throws Exception {
	}

	@Test
	void testNewContest() {
		String precinctNo = "005";
		String precinctName = "Atglen";
		String contestName = "Justice of the Supreme Court";
		int contestFormat = 1;
		Contest contest = new Contest(precinctNo, precinctName, contestName, contestFormat);
		assertTrue(contest != null);
		assertEquals(precinctNo, contest.getPrecinctNo());
		assertEquals(precinctName, contest.getPrecinctName());
		assertEquals(contestName, contest.getContestName());
		assertEquals(contestFormat, contest.getContestFormat());
		String expected = "Contest:\n  name: Justice of the Supreme Court format:1";
		assertEquals(expected, contest.toString());
	}

}
