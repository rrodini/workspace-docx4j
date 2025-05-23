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

class TestReferendumExtractor {

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
	void testExtractReferendums1() {
		// 005_ATGLEN Primary 2024
		List<Referendum> refs;
		// contestgen_2024_Primary_Dems.properties
		Properties props = Utils.loadProperties("./src/test/java/contestgen_2024_Primary_Dems.properties");
		Initialize.start(props);
		refs = ReferendumExtractor.extractReferendums("OO5",  "ATGLEN", ATGLEN_PRIMARY_2024_BALLOT);
		// There should be 0 contests.
		assertEquals(0, refs.size());
	}
	
	@Test
	void testExtractReferendums2() {
		String expectedRefText =
		"""
		Shall the ordinance entitled "An
		Ordinance Offering a Conservation
		Easement and Declaration of
		Restrictive Covenants to the
		Willistown Conservation Trust, Inc.,
		for Certain Borough-owned Property
		in the Ruthland Avenue - Randolph
		Woods Tract" be adopted granting a
		conservation easement to the
		Willistown Conservation Trust, Inc.,
		for the purpose of preserving a
		0.87-acre tract of land owned by the
		Borough, off of Ruthland Avenue and
		adjacent to the Malvern Fire
		Department property?				
		""";
		// 350_MALVERN Primary 2023
		List<Referendum> refs;
		// contestgen_2024_Primary_Dems.properties
		Properties props = Utils.loadProperties("./src/test/java/contestgen_2024_Primary_Dems.properties");
		Initialize.start(props);
		refs = ReferendumExtractor.extractReferendums("350",  "MALVERN", MALVERN_PRIMARY_2023_BALLOT);
		// There should be 1 referendum.
		assertEquals(1, refs.size());
		// And its fields should match these
		Referendum ref = refs.get(0);
		assertEquals("Malvern Borough Referendum", ref.getRefQuestion());
		assertEquals(expectedRefText, ref.getRefText());
	}


}
