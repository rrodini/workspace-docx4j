package com.rodini.contestgen.extract;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.rodini.ballotutils.Utils;
import com.rodini.contestgen.common.Initialize;
class TestBallotExtractor {

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
	void testExtractNamesGood() {
		// This is a full specimen
		String specimenText = Utils.readTextFile("./src/test/java/Primary-Dems-2021.txt");
		Initialize.precinctNameRegex = Utils.compileRegex("(?m)^OFFICIAL MUNICIPAL PRIMARY ELECTION BALLOT\\n(?<id>\\d+) (?<name>.*)$\\n");
		List<String> names = BallotExtractor.extractPrecinctNoNames(specimenText);
//		for (String name: names) {
//			System.out.printf("%s%n", name);
//		}
		assertEquals(231, names.size());
	}

	@Test
	void testExtractNamesBad() {
		// This is a full specimen
		String specimenText = Utils.readTextFile("./src/test/java/Primary-Dems-2021.txt");
		// regex compiles but is no good for extraction.
		Initialize.precinctNameRegex = Utils.compileRegex("\\ddd");
		List<String> names = BallotExtractor.extractPrecinctNoNames(specimenText);
		assertEquals(0, names.size());
	}

}
