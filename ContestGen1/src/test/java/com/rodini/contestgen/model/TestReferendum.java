package com.rodini.contestgen.model;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class TestReferendum {
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
		String precinctNo = "025";
		String precinctName = "WEST BRADFORD 1";
		String refQuestion = "WEST BRADFORD TOWNSHIP\nOPEN SPACE REFERENDUM";
		String refText = "\"Do you favor the imposition of a\n"
				+ "tax on earned income of\n"
				+ "Township residents by West\n"
				+ "Bradford Township at the rate of\n"
				+ "one-quarter of one percent\n"
				+ "annually to be used to preserve,\n"
				+ "conserve, and acquire open\n"
				+ "space property interest and\n"
				+ "benefits?\"";
		Referendum ref = new Referendum(precinctNo, precinctName, refQuestion, refText);
		assertTrue(ref != null);
		assertEquals(precinctNo, ref.getPrecinctNo());
		assertEquals(precinctName, ref.getPrecinctName());
		assertEquals(refQuestion, ref.getRefQuestion());
		assertEquals(refText, ref.getRefText());
		String expected = String.format("Referendum:%n question: %s text:%s", refQuestion, refText);
		assertEquals(expected, ref.toString());
	}

}
