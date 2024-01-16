package com.rodini.ballotgen.writein;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.Logger;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.rodini.zoneprocessor.ZoneProcessor;
import com.rodini.zoneprocessor.Zone;
import com.rodini.ballotgen.common.MockedAppender;
import com.rodini.ballotgen.writein.Writein;
import com.rodini.ballotgen.writein.WriteinFactory;
import com.rodini.ballotutils.Utils;

class TestWriteinFactory {

	private static MockedAppender mockedAppender;
	private static Logger logger;
	
	// Map constructed by zoneProcessor component.
	private static Map<String, Zone> precinctToZoneMap;
	

	@BeforeEach
	void setUp() throws Exception {
	    mockedAppender = new MockedAppender();
	    mockedAppender.start();
	    logger = (Logger)LogManager.getLogger(WriteinFactory.class);
	    logger.addAppender(mockedAppender);
	    logger.setLevel(Level.ERROR);
	    // need zone processing
	    ZoneProcessor.processCSVText(Utils.readTextFile("./src/test/java/test-precinct-zone.csv"));
	    precinctToZoneMap = ZoneProcessor.getPrecinctZoneMap();
	    WriteinFactory.setPrecinctToZones(precinctToZoneMap);
	}

	@AfterEach
	void tearDown() throws Exception {
		logger.removeAppender(mockedAppender);
		mockedAppender.stop();
		ZoneProcessor.clearPrecinctZoneMap();
		WriteinFactory.clearPrecinctWriteins();
	}
	@Test
	void testWriteins01() {
		// Lots of syntax errors in this file.
		String writeinsCSVText = Utils.readTextFile("./src/test/java/test-writeins-bad-01.csv");
        String [] expected = {
        		"CSV line #1 has fewer than 6 fields",
        		"CSV line #2 write-in name JD has error",
        		"CSV line #3 write-in contest name  has error",
        		"CSV line #4 field should be \"Zone\" but is \"zoom\"",
        		"CSV line #5 number expected but got: 6x",
        		"CSV line #6 field should be \"Precincts\" but is \"precinct\"",
        		"CSV line #7 number expected but got: 15x",
        		"CSV line #8 number expected but got: "
        };
		WriteinFactory.processCSVText(writeinsCSVText);
		// one error for each line
		int size = mockedAppender.messages.size();
		assertEquals(8, size);
		for (int i = 0; i < size; i++) {
		//	System.out.printf("i: %d%n", i);
			assertTrue(mockedAppender.messages.get(i).startsWith(expected[i]));
		}
		
	}
	void dumpPrecinctZoneMap() {
		Set<String> precinctStrs = precinctToZoneMap.keySet();
		for (String precinctStr: precinctStrs) {
			String output = "precinct: " + precinctStr + " zone: " + precinctToZoneMap.get(precinctStr).getZoneNo();
			System.out.println(output);
		}
	}
	void dumpPrecinctWriteins() {
		
		Map <String, List<Writein>> precinctWriteins = WriteinFactory.getPrecinctWriteins();
		Set<String> precinctStrs = precinctWriteins.keySet();
		for (String precinctStr: precinctStrs) {
			String output = "precinct: " + precinctStr + " writeins: " + precinctWriteins.get(precinctStr);
			System.out.println(output);
		}
	}
	
	@Test
	void testWriteins02() {
		// Semantic error in this file. Zone doesn't own precinct.
		String writeinsCSVText = Utils.readTextFile("./src/test/java/test-writeins-bad-02.csv");
		WriteinFactory.processCSVText(writeinsCSVText);
		String expected = "CSV line #1 rejected. Zone 06 doesn't own Precinct 018";
		int size = mockedAppender.messages.size();
		assertEquals(1, size);
		assertTrue(mockedAppender.messages.get(0).startsWith(expected));
	}
	@Test
	void testWriteins03() {
		String writeinsCSVText = Utils.readTextFile("./src/test/java/test-writeins-good-01.csv");
		WriteinFactory.processCSVText(writeinsCSVText);
		// one write-in for precinct ballot "014"
		List<Writein> writeins = WriteinFactory.getPrecinctWriteins().get("014");
		assertEquals(1, writeins.size());
		assertEquals("Township Supervisor Birmingham", writeins.get(0).getContestName());
		assertEquals("Jane Doe", writeins.get(0).getCandidateName());
		// one write-in for precinct ballot "015"
		writeins = WriteinFactory.getPrecinctWriteins().get("015");
		assertEquals(1, writeins.size());
		assertEquals("Township Supervisor Birmingham", writeins.get(0).getContestName());
		assertEquals("Jane Doe", writeins.get(0).getCandidateName());
	}
	@Test
	void testWriteins04() {
		String writeinsCSVText = Utils.readTextFile("./src/test/java/test-writeins-good-02.csv");
		WriteinFactory.processCSVText(writeinsCSVText);
		
		//dumpPrecinctWriteins();
		
		// one write-in for precinct ballot "005"
		List<Writein> writeins = WriteinFactory.getPrecinctWriteins().get("005");
		assertEquals(1, writeins.size());
		assertEquals("Member of Council Atglen Borough", writeins.get(0).getContestName());
		assertEquals("John Doe", writeins.get(0).getCandidateName());
		// one write-in for precinct ballot "014"
		writeins = WriteinFactory.getPrecinctWriteins().get("014");
		assertEquals(1, writeins.size());
		assertEquals("Township Supervisor Birmingham", writeins.get(0).getContestName());
		assertEquals("Jane Doe", writeins.get(0).getCandidateName());
		// one write-in for precinct ballot "015"
		writeins = WriteinFactory.getPrecinctWriteins().get("015");
		assertEquals(1, writeins.size());
		assertEquals("Township Supervisor Birmingham", writeins.get(0).getContestName());
		assertEquals("Jane Doe", writeins.get(0).getCandidateName());
	}
}
