package com.rodini.contestgen.model;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class TestRetention {
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
		String officeName = "JUSTICE OF THE SUPREME\n"
				+ "	COURT RETENTION ELECTION";
		String judgeName = "THOMAS G SAYLOR";
		Retention ret = new Retention(precinctNo, precinctName, officeName, judgeName);
		assertTrue(ret != null);
		assertEquals(precinctNo, ret.getPrecinctNo());
		assertEquals(precinctName, ret.getPrecinctName());
		assertEquals(officeName, ret.getOfficeName());
		assertEquals(judgeName, ret.getJudgeName());
		String expected = String.format("Retention:%n office: %s judge:%s", officeName, judgeName);
		assertEquals(expected, ret.toString());
	}

}
