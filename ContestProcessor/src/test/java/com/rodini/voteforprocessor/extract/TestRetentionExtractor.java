package com.rodini.voteforprocessor.extract;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import org.apache.logging.log4j.Level;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.rodini.voteforprocessor.model.*;
import com.rodini.ballotutils.Utils;

import static com.rodini.voteforprocessor.extract.TestBallotText.*;

class TestRetentionExtractor {

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
	void testExtractRetentions1() {
		// 005_ATGLEN Primary 2024
		List<Retention> rets;
		// contestgen_2024_Primary_Dems.properties
		Properties props = Utils.loadProperties("./src/test/java/contestgen_2024_Primary_Dems.properties");
		Initialize.start(props);
		rets = RetentionExtractor.extractRetentions("OO5",  "ATGLEN", ATGLEN_PRIMARY_2024_BALLOT);
		// There should be 0 contests.
		assertEquals(0, rets.size());
	}
	
	@Test
	void testExtractRetentions2() {
		// 300 Honeybrook General 2023
		List<Retention> rets;
		// contestgen_2024_Primary_Dems.properties
		Properties props = Utils.loadProperties("./src/test/java/contestgen_2024_Primary_Dems.properties");
		Initialize.start(props);
		rets = RetentionExtractor.extractRetentions("300",  "MALVERN", HONEYBROOK_GENERAL_2023_BALLOT);
		// There should be 4 retentions.
		assertEquals(4, rets.size());
		// And its fields should match these
		Retention ret = rets.get(0);
		assertEquals("Superior Court Retention", ret.getOfficeName());
		assertEquals("Jack Panella", ret.getJudgeName());
		ret = rets.get(1);
		assertEquals("Superior Court Retention", ret.getOfficeName());
		assertEquals("Victor P. Stabile", ret.getJudgeName());
		ret = rets.get(2);
		assertEquals("Court of Common Pleas Retention", ret.getOfficeName());
		assertEquals("Patrick Carmody", ret.getJudgeName());
		ret = rets.get(3);
		assertEquals("Court of Common Pleas Retention", ret.getOfficeName());
		assertEquals("John L. Hall", ret.getJudgeName());
	}


}
