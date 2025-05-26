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
import org.junit.jupiter.api.Disabled;

import com.rodini.contestgen.model.Ballot;
import com.rodini.voteforprocessor.model.Contest;
import com.rodini.voteforprocessor.model.Referendum;
import com.rodini.voteforprocessor.model.Retention;

class TestGenerateBallotSummary {

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
	void testGenerateRetentionSection() {
		String expected = // Do not indent!
"""
Retention Summary
Unique retention judge names: 3
Jack Panella: 1
Victor P. Stabile: 1
Patrick Carmody: 1
""";
		String generatedRetentionSection = null;
		try (StringWriter sw = new StringWriter(); ) 
		{
			GenerateBallotSummary.generateRetentionSection(sw, List.of(ballot));
			generatedRetentionSection = sw.toString();
		} catch (IOException e) {
			fail("Unexpected IOException: " + e.getMessage());
		}
		assertEquals(expected, generatedRetentionSection);
	}
	@Test
	void testGenerateReferendumSection() {
		String expected =  // Do not indent!
"""
Referendum Summary
Unique referendum questions: 1
Referendum 0:
Borough of Kennett Square:
Library Tax Referendum: precincts: 305
""";
		String generatedReferendumSection = null;
		try (StringWriter sw = new StringWriter(); ) 
		{
			GenerateBallotSummary.generateReferendumSection(sw, List.of(ballot));
			generatedReferendumSection = sw.toString();
		} catch (IOException e) {
			fail("Unexpected IOException: " + e.getMessage());
		}
		assertEquals(expected, generatedReferendumSection);
	}
	@Test
	void testGetUniqueString() {
		String expected = // eol at the end of each string is NOT desired.
				"Justice of the Supreme Court"
				+ "Judge of the Court of Common Pleas\n15th Judicial District"
				+ "Borough of Kennett Square:\nLibrary Tax Referendum"
				+ "Superior Court Retention\nElection Question"
				+ "Superior Court Retention\nElection Question"
				+ "Court of Common Pleas Retention\nElection Question";
		String uniqueString = GenerateBallotSummary.getUniqueString(ballot);
		assertEquals(expected, uniqueString);
	}
	@Test
	void testGenerateUniqueBallotSection() {
		String expected = // Do not indent!
"""
Unique ballot count: 1
Precincts with identical ballots:
Ballot   0: 305_KENNETT_SQUARE_N
""";
		String generatedUniqueBallotSection = null;
		try (StringWriter sw = new StringWriter(); ) 
		{
			GenerateBallotSummary.generateUniqueBallotSection(sw, List.of(ballot));
			generatedUniqueBallotSection = sw.toString();
		} catch (IOException e) {
			fail("Unexpected IOException: " + e.getMessage());
		}
		assertEquals(expected, generatedUniqueBallotSection);
	}
	@Disabled
	@Test
	void testGenerateHeading() {
		String expected = // Do not indent!
"""
Summary Report (Ballot/Referendum/Retention)
Precinct count: 1
""";
		String generatedHeading = null;
		try (StringWriter sw = new StringWriter(); ) 
		{
			GenerateBallotSummary.generateHeading(sw, List.of(ballot));
			generatedHeading = sw.toString();
		} catch (IOException e) {
			fail("Unexpected IOException: " + e.getMessage());
		}
		String [] expectedLines = expected.split("\n");
		String [] generatedLines = generatedHeading.split("\n");
		assertTrue(generatedHeading.endsWith(expectedLines[0]));
		assertEquals(expectedLines[1], generatedLines[1]);
	}
}
