package com.rodini.ballotgen.writein;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.rodini.ballotgen.writein.WriteinFactory;
import com.rodini.ballotgen.writein.WriteinProcessor;
import com.rodini.ballotutils.Utils;
import com.rodini.zoneprocessor.ZoneProcessor;
import com.rodini.zoneprocessor.Zone;

class TestWriteinProcessor {

	// Map constructed by zoneProcessor component.
	private String precinctZoneCSVText;

	
	@BeforeEach
	void setUp() throws Exception {
	    // use this data for every test.
	    precinctZoneCSVText = Utils.readTextFile("./src/test/java/test-precinct-zone.csv");
	}
	
	@AfterEach
	void tearDown() throws Exception {
		ZoneProcessor.clearPrecinctZoneMap();
		WriteinFactory.clearPrecinctWriteins();
	}
	@Test
	void testNoWriteinAnyContest() {
		String writeinCVSText = "";
		WriteinProcessor wp = new WriteinProcessor(writeinCVSText, precinctZoneCSVText);
		String precinctStr = "005"; // Atglen
		assertFalse(wp.precinctHasWriteins(precinctStr));
	}
	@Test
	void testNoWriteinForContest() {
		String writeinCVSText = Utils.readTextFile("./src/test/java/test-writeins-good-01.csv");
		WriteinProcessor wp = new WriteinProcessor(writeinCVSText, precinctZoneCSVText);
		String precinctStr = "014"; // Birmingham
		assertTrue(wp.precinctHasWriteins(precinctStr));
		String contestName = "Auditor";
		List<String> names = wp.findCandidatesForContest(precinctStr, contestName);
		assertEquals(0, names.size());
	}
	@Test
	void testSingleWriteinForContest() {
		String writeinCVSText = Utils.readTextFile("./src/test/java/test-writeins-good-01.csv");
		WriteinProcessor wp = new WriteinProcessor(writeinCVSText, precinctZoneCSVText);
		String precinctStr = "014"; // Birmingham
		assertTrue(wp.precinctHasWriteins(precinctStr));
		String contestName = "Township Supervisor Birmingham";
		String expectedCandName = "Jane Doe";
		List<String> names = wp.findCandidatesForContest(precinctStr, contestName);
		assertEquals(1, names.size());
		assertEquals(expectedCandName, names.get(0));		
		precinctStr = "015"; // Birmingham
		assertTrue(wp.precinctHasWriteins(precinctStr));
		names = wp.findCandidatesForContest(precinctStr, contestName);
		assertEquals(1, names.size());
		assertEquals(expectedCandName, names.get(0));		
	}
	@Test
	void testMultipleWriteinsForContest() {
		String writeinCVSText = Utils.readTextFile("./src/test/java/test-writeins-good-03.csv");
		WriteinProcessor wp = new WriteinProcessor(writeinCVSText, precinctZoneCSVText);
		String precinctStr = "014"; // Birmingham
		assertTrue(wp.precinctHasWriteins(precinctStr));
		String contestName = "Township Supervisor Birmingham";
		String [] expectedCandNames = {"John Doe", "Jane Doe"};
		List<String> names = wp.findCandidatesForContest(precinctStr, contestName);
		assertEquals(2, names.size());
		assertEquals(expectedCandNames[0], names.get(0));		
		assertEquals(expectedCandNames[1], names.get(1));		
	}
	@Test
	void testWriteinsForMultipleContests() {
		String writeinCVSText = Utils.readTextFile("./src/test/java/test-writeins-good-04.csv");
		WriteinProcessor wp = new WriteinProcessor(writeinCVSText, precinctZoneCSVText);
		String contestName = "Township Supervisor Birmingham";
		String precinctStr = "014";
		String expectedCandName = "Jane Doe";
		List<String> names = wp.findCandidatesForContest(precinctStr, contestName);
		assertEquals(1, names.size());
		assertEquals(expectedCandName, names.get(0));
		contestName = "School Director Unexpired 2 Year Term " +
					  "Unionville Chadds Ford Region B";	
		expectedCandName = "John Doe";
		names = wp.findCandidatesForContest(precinctStr, contestName);
		assertEquals(1, names.size());
		assertEquals(expectedCandName, names.get(0));
	}
	@Test
	void test5() {
	}

}
