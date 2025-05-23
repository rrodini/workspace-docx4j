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

class TestContestExtractor {

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
	void testExtractPageContests1() {
		// 005_ATGLEN Primary 2024
		List<Contest> contests;
		List<String> contestNames = Arrays.asList(
				"President of the United States",
				"United States Senator",
				"Attorney General",
				"Auditor General",
				"State Treasurer",
				"Representative in Congress\n6th District",
				"Representative in the General\nAssembly\n74th District",
				"Delegate to the National\nConvention\n6th District"
				);
		// contestgen_2024_Primary_Dems.properties
		Properties props = Utils.loadProperties("./src/test/java/contestgen_2024_Primary_Dems.properties");
		Initialize.start(props);
		contests = ContestExtractor.extractPageContests("OO5",  "ATGLEN", ATGLEN_PRIMARY_2024_BALLOT);
		// There should be 8 contests.
		assertEquals(8, contests.size());
		// And their names should match these
		for (int i = 0; i < 8; i++) {
			String contestName = contests.get(i).getName();
			assertEquals(contestNames.get(i), contestName);			
		}
	}
	
	@Test
	void testExtractPageContests2() {
		// 350_MALVERN Primary 2023
		List<Contest> contests;
		List<String> contestNames = Arrays.asList(
				"Justice of the Supreme Court",
				"Judge of the Superior Court",
				"Judge of the Commonwealth Court",
				"Judge of the Court of Common Pleas\n10 Year Term",
				"County Commissioner",
				"District Attorney",
				"Sheriff",
				"Prothonotary",
				"Register of Wills",
				"Recorder of Deeds",
				"School Director\nGreat Valley Region 2",
				"Member of Council\nMalvern Borough"
				);
		// contestgen_2024_Primary_Dems.properties
		Properties props = Utils.loadProperties("./src/test/java/contestgen_2024_Primary_Dems.properties");
		Initialize.start(props);
		contests = ContestExtractor.extractPageContests("350",  "MALVERN", MALVERN_PRIMARY_2023_BALLOT);
		// There should be 8 contests.
		assertEquals(12, contests.size());
		// And their names should match these
		for (int i = 0; i < 12; i++) {
			String contestName = contests.get(i).getName();
			assertEquals(contestNames.get(i), contestName);			
		}
		
	}

	@Test
	void testExtractPageContests3() {
		// 005_ATGLEN General 2024
		List<Contest> contests;
		List<String> contestNames = Arrays.asList(
				"Presidential Electors",
				"United States Senator",
				"Attorney General",
				"Auditor General",
				"State Treasurer",
				"Representative in Congress\n6th District",
				"Representative in the General\nAssembly\n74th District"
				);
		// contestgen_2024_general_election.properties
		Properties props = Utils.loadProperties("./src/test/java/contestgen_2024_General_Election.properties");
		Initialize.start(props);
		contests = ContestExtractor.extractPageContests("OO5",  "ATGLEN", ATGLEN_GENERAL_2024_BALLOT);
		// There should be 8 contests.
		assertEquals(7, contests.size());
		// And their names should match these
		for (int i = 0; i < 7; i++) {
			String contestName = contests.get(i).getName();
			assertEquals(contestNames.get(i), contestName);			
		}
		// Format (regex) 1 should match first contest format index.
		assertEquals(1, contests.get(0).getFormatIndex());
		// Format (regex) 2 should match second contest format index.
		assertEquals(2, contests.get(1).getFormatIndex());
	}
	

}
