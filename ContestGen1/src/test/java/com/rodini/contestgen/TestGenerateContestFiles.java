package com.rodini.contestgen;

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.rodini.contestgen.model.Ballot;
import com.rodini.voteforprocessor.model.Contest;
import com.rodini.voteforprocessor.model.Referendum;
import com.rodini.voteforprocessor.model.Retention;

class TestGenerateContestFiles {
	 
	static Ballot ballot;
	
	@BeforeAll
	static void setUpBeforeClass() throws Exception {
		// Cheat = no rawText, just extracted VoteFors
		ballot = new Ballot("305_KENNETT_SQUARE_N", "rawText - not used");
		List<Contest> contests = new ArrayList<>();
		List<Referendum> referendums = new ArrayList<>();
		List<Retention> retentions = new ArrayList<>();
		Contest contest1 = new Contest("305", "KENNETT_SQUARE_N", "Justice of the Supreme Court", "term", "instructions", null, 1);
		Contest contest2 = new Contest("305", "KENNETT_SQUARE_N", "Judge of the Court of Common Pleas\n15th Judicial District", "term", "instructions", null, 1);
		contests.add(contest1);
		contests.add(contest2);
		Referendum ref1 = new Referendum("305", "KENNETT_SQUARE_N", "Borough of Kennett Square:\nLibrary Tax Referendum", "refText - not used");
		referendums.add(ref1);
		Retention ret1 = new Retention("305","KENNETT_SQUARE_N", "question", "Superior Court Retention\nElection Question", "Jack Panella");
		Retention ret2 = new Retention("305","KENNETT_SQUARE_N", "question", "Superior Court Retention\nElection Question", "Victor P. Stabile");
		Retention ret3 = new Retention("305","KENNETT_SQUARE_N", "question", "Court of Common Pleas Retention\nElection Question", "Patrick Carmody");
		retentions.add(ret1);
		retentions.add(ret2);
		retentions.add(ret3);
		ballot.setContests(contests);
		ballot.setReferendums(referendums);
		ballot.setRetentions(retentions);
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
	void testGenerateContests() {
		String expected =  // notice the justification and double escape
"""
Contests
Justice of the Supreme Court,1
Judge of the Court of Common Pleas\\n15th Judicial District,1				
""";
		String generatedContests = null;
		try (StringWriter sw = new StringWriter(); ) 
		{
			GenerateContestFiles.generateContests(sw, ballot);
			generatedContests = sw.toString();
		} catch (IOException e) {
			fail("Unexpected IOException: " + e.getMessage());
		}
		assertEquals(expected, generatedContests);
	}
	@Test
	void testGenerateReferendums() {
		String expected =  // notice the justification and double escape
"""
Referendums
Borough of Kennett Square:\\nLibrary Tax Referendum
""";
		String generatedReferendums = null;
		try (StringWriter sw = new StringWriter(); ) 
		{
			GenerateContestFiles.generateReferendums(sw, ballot);
			generatedReferendums = sw.toString();
		} catch (IOException e) {
			fail("Unexpected IOException: " + e.getMessage());
		}
		assertEquals(expected, generatedReferendums);
	}
	@Test
	void testGenerateRetentions() {
		String expected =  // notice the justification and double escape
"""
Retentions
Superior Court Retention\\nElection Question,Jack Panella
Superior Court Retention\\nElection Question,Victor P. Stabile
Court of Common Pleas Retention\\nElection Question,Patrick Carmody
""";
		String generatedRetentions = null;
		try (StringWriter sw = new StringWriter(); ) 
		{
			GenerateContestFiles.generateRetentions(sw, ballot);
			generatedRetentions = sw.toString();
		} catch (IOException e) {
			fail("Unexpected IOException: " + e.getMessage());
		}
		assertEquals(expected, generatedRetentions);
	}
//	@Test
//	void test3() {
//	}
//	@Test
//	void test4() {
//	}
//	@Test
//	void test5() {
//	}
//	@Test
//	void test6() {
//	}

	
	
}
