package com.rodini.ballotgen.common;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.rodini.ballotgen.common.BallotUtils;
class TestBallotUtils {
	
	final String ballotTextFilePath = "./chester-output/750_East_Whiteland_4_VS.txt";
	final String pathName = "./chester-output";
	final String precinctNoName = "750_East_Whiteland_4";
	final String precinctNo = "750";
	final String precinctName = "East_Whiteland_4";
	@BeforeEach
	void setUp() throws Exception {
	}

	@AfterEach
	void tearDown() throws Exception {
	}

	@Test
	void testGetPrecinctNoName() {
		assertEquals(precinctNoName, BallotUtils.getPrecinctNoName(ballotTextFilePath));
	}
	@Test
	void testGetPathNameOnly() {
		assertEquals(pathName, BallotUtils.getPathNameOnly(ballotTextFilePath));
	}
	@Test
	void testGetPrecinctNo() {
		assertEquals(precinctNo, BallotUtils.getPrecinctNo(precinctNoName));
	}
	@Test
	void testGetPrecinctName() {
		assertEquals(precinctName, BallotUtils.getPrecinctName(precinctNoName));
	}
}
