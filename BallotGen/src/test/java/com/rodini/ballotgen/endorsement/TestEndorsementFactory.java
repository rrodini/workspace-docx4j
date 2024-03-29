package com.rodini.ballotgen.endorsement;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.Logger;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.rodini.ballotgen.common.MockedAppender;
import com.rodini.ballotgen.endorsement.Endorsement;
import com.rodini.ballotgen.endorsement.EndorsementFactory;
import com.rodini.ballotgen.endorsement.EndorsementScope;
import com.rodini.ballotutils.Utils;

class TestEndorsementFactory {

	private static MockedAppender mockedAppender;
	private static Logger logger;

	@BeforeEach
	void setUp() throws Exception {
	    mockedAppender = new MockedAppender();
	    mockedAppender.start();
	    logger = (Logger)LogManager.getLogger(EndorsementFactory.class);
	    logger.addAppender(mockedAppender);
	    logger.setLevel(Level.ERROR);
	}

	@AfterEach
	void tearDown() throws Exception {
		logger.removeAppender(mockedAppender);
		mockedAppender.stop();
		EndorsementFactory.clearCandidateEndorsements();
	}

	@Test
	void testEdorsements01() {
		String endorsementsCSVText = Utils.readTextFile("./src/test/java/test-endorsements-01.csv");
		EndorsementFactory.processCSVText(endorsementsCSVText);
		Map<String, List<Endorsement>> candidateEnds = EndorsementFactory.getCandidateEndorsements();
//		for (String name: candidateEnds.keySet()) {
//			System.out.println("Candidate: " + name);
//			for (Endorsement end: candidateEnds.get(name)) {
//				System.out.println("  " + end.toString());
//			}
//		}		
		Set<String> names = candidateEnds.keySet();
		assertTrue(names.contains("ROBERT A RODINI"));
		assertTrue(names.contains("SAMANTHA JOUIN"));
		List<Endorsement> ends1 = candidateEnds.get("ROBERT A RODINI");
		assertEquals(2, ends1.size());
		List<Endorsement> ends2 = candidateEnds.get("SAMANTHA JOUIN");
		assertEquals(1, ends2.size());
		assertEquals(0, mockedAppender.messages.size());
	}
	
	@Test
	void testEdorsements02() {
		String endorsementsCSVText = Utils.readTextFile("./src/test/java/test-endorsements-02.csv");
		EndorsementFactory.processCSVText(endorsementsCSVText);
		Map<String, List<Endorsement>> candidateEnds = EndorsementFactory.getCandidateEndorsements();
		assertEquals(3, mockedAppender.messages.size());
		String expected1 = "CSV line #2 has fewer than 3 fields";
		assertTrue(mockedAppender.messages.get(0).startsWith(expected1));
		String expected2 = "CSV line #3 endorsement scope ZOOM has error";
		assertTrue(mockedAppender.messages.get(1).startsWith(expected2));
		String expected3 = "CSV line #4 zone # missing";
		assertTrue(mockedAppender.messages.get(2).startsWith(expected3));
		assertEquals(0, candidateEnds.keySet().size());
	}
	@Test
	void testEndorsement03() {
		String endorsementsCSVText = "Tricia Bliven-Chasinoff,endorsed,zone,13";
		String [] element = endorsementsCSVText.split(",");
		EndorsementFactory.processCSVText(endorsementsCSVText);
		Map<String, List<Endorsement>> candidateEnds = EndorsementFactory.getCandidateEndorsements();
		assertEquals(1, candidateEnds.size());
		Endorsement end = candidateEnds.get(element[0].toUpperCase()).get(0);
		assertEquals(element[0],end.getName());
		assertEquals(EndorsementScope.ZONE, end.getScope());
		assertEquals(Integer.parseInt(element[3]),end.getZoneNo());
	}
	@Test
	void testEdorsements04() {
		String endorsementsCSVText = Utils.readTextFile("./src/test/java/test-endorsements-03.csv");
		EndorsementFactory.processCSVText(endorsementsCSVText);
		Map<String, List<Endorsement>> candidateEnds = EndorsementFactory.getCandidateEndorsements();
//		for (String name: candidateEnds.keySet()) {
//			System.out.println("Candidate: " + name);
//			for (Endorsement end: candidateEnds.get(name)) {
//				System.out.println("  " + end.toString());
//			}
//		}		
		Set<String> names = candidateEnds.keySet();
		assertTrue(names.contains("BONNIE J. WOLFF"));
		assertTrue(names.contains("HONEY BROOK TOWNSHIP BOARD OF SUPERVISORS REFERENDUM"));
		assertTrue(names.contains("JOHN L. HALL"));
		List<Endorsement> ends1 = candidateEnds.get("BONNIE J. WOLFF");
		assertEquals(1, ends1.size());
		assertEquals(EndorsementMode.ENDORSED, ends1.get(0).getMode());
		List<Endorsement> ends2 = candidateEnds.get("HONEY BROOK TOWNSHIP BOARD OF SUPERVISORS REFERENDUM");
		assertEquals(1, ends2.size());
		assertEquals(EndorsementMode.UNENDORSED, ends2.get(0).getMode());
		List<Endorsement> ends3 = candidateEnds.get("JOHN L. HALL");
		assertEquals(1, ends3.size());
		assertEquals(EndorsementMode.ANTIENDORSED, ends3.get(0).getMode());
		assertEquals(0, mockedAppender.messages.size());
	}
	

	
	
}
