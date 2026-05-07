package com.rodini.contestgen;

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.io.StringWriter;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import com.rodini.ballotutils.Utils;
import com.rodini.contestgen.model.Ballot;
import com.rodini.voteforprocessor.model.Contest;
import com.rodini.zoneprocessor.Zone;
import com.rodini.zoneprocessor.ZoneFactory;
import com.rodini.zoneprocessor.ZoneProcessor;

class TestGenerateEndorsementsFile {

	static List<Ballot> ballots;
	static List<Contest> commonContests;
	static String csvText;
	static String ZONE13 = "13"; // ZoneNo 13 : Great Valley Democrats
	
	@BeforeAll
	static void setUpBeforeClass() throws Exception {
		// BALLOTS FOR 2025 PRIMARY: 350_MALVERN, 065_CHARLESTOWN
		ballots = GenerateTestBallots.genBallot2();
		commonContests = GenerateEndorsementsFile.processCommonContests(ballots);
		String csvFilePath = "/Users/robert/Documents/Sample Ballot Production/SampleBallotGen-1.7.0/chester-zone/chester-precincts-zones.csv";
		csvText = Utils.readTextFile(csvFilePath);
// TBD - Many ERRORS zone logo files not found
		ZoneProcessor.processCSVText(csvText);
	}

	@Test
	void testProcessCommonContests() {
//		System.out.printf("Common contests:%n");
//		for (Contest contest: commonContests) {
//			System.out.printf("  Contest: %s%n", contest.getName());
//		}
		assertEquals(7, commonContests.size());
	}
	@Test
	void testIsCommonContest() {
		Contest common0 = commonContests.get(0);
		Contest common6 = commonContests.get(6);
		assertTrue(GenerateEndorsementsFile.isCommonContest
				(common0, commonContests));
		assertTrue(GenerateEndorsementsFile.isCommonContest
				(common6, commonContests));
		Contest pageBreakContest = new Contest("000", "NOWHERE",
				"PAGE_BREAK", "", "", null, 0);
		assertTrue(GenerateEndorsementsFile.isCommonContest
				(pageBreakContest, commonContests));
	}
	@Test
	void testGenerateCommonCandidates() {
		String expected = 
"""
# Common contest candidates
Brandon Neuman,Judge of the Superior Court,endorsed,county
Stella Tsai,Judge of the Commonwealth Court,endorsed,county
Mackenzie Smith,Judge of the Court of Common Pleas 15th Judicial District,endorsed,county
Clay Cauley Sr.,Judge of the Court of Common Pleas 15th Judicial District,endorsed,county
Caroline Bradley,Clerk of Courts,endorsed,county
Nick Cherubino,Controller,endorsed,county
Sophia Garcia-Jackson,Coroner,endorsed,county
Patricia A. Maisano,Treasurer,endorsed,county
""";
		String generatedCandidatesString = null;
		try (StringWriter sw = new StringWriter(); ) 
		{
			GenerateEndorsementsFile.generateCommonCandidatesEndorsements(sw, commonContests);
			generatedCandidatesString = sw.toString();
		} catch (IOException e) {
			fail("Unexpected IOException: " + e.getMessage());
		}
		
		System.out.println("**generateCommonEndorsements**");
		System.out.println(generatedCandidatesString);
		System.out.println();
		
		
		assertEquals(expected, generatedCandidatesString);
	}
	@Test
	void testProcessZoneBallots() {
		Map<String, List<Contest>> zoneContestsMap =
		GenerateEndorsementsFile.processZoneBallots(ballots,
		commonContests, ZoneProcessor.getPrecinctZoneMap());
		Set<String> zoneNos = ZoneFactory.getZones().keySet();
		assertEquals(18, zoneNos.size());
		// TODO: hard-wired Zone 13 - Great Valley Dems
		List<Contest> zoneContests = zoneContestsMap.get(ZONE13);
		assertEquals(15, zoneContests.size());
	}
	@Test
	void testGenerateZoneCandidates() {
		String expected =
"""
# Zone 13 Great Valley Democrats
Deborah Kuhn,Inspector of Elections 065 Charlestown,endorsed,zone,13
Vicki Sharpless,Inspector of Elections 350 Malvern,endorsed,zone,13
Hugo Schmitt,Judge of Elections 350 Malvern,endorsed,zone,13
James C. Kovaleski,Magisterial District Judge District 15-2-01,endorsed,zone,13
Zeyn B. Uzman,Mayor Malvern Borough,endorsed,zone,13
Pete Papadopoulos,Mayor Malvern Borough,endorsed,zone,13
Angela Riccetti,Member of Council Malvern Borough,endorsed,zone,13
Zoe Warner,Member of Council Malvern Borough,endorsed,zone,13
Dan Kunze,Member of Council Malvern Borough,endorsed,zone,13
Andrea Rizzo,School Director Great Valley Region 1,endorsed,zone,13
Stacey Kahan,School Director Great Valley Region 1,endorsed,zone,13
Lorie Sollenberger,School Director Great Valley Region 2,endorsed,zone,13
Hugh D. Willig,Township Supervisor Charlestown Township,endorsed,zone,13
Jill Green,Township Supervisor Unexpired 2 Year Term Charlestown Township,endorsed,zone,13
Louis Rubinfield,Township Supervisor Unexpired 2 Year Term Charlestown Township,endorsed,zone,13
Madeleine Carlson,Township Supervisor Unexpired 4 Year Term Charlestown Township,endorsed,zone,13
""";
		String generatedCandidatesString = null;
		Map<String, List<Contest>> zoneContestsMap =
		GenerateEndorsementsFile.processZoneBallots(ballots,
		commonContests, ZoneProcessor.getPrecinctZoneMap());
		Set<String> zoneNos = ZoneFactory.getZones().keySet();
		Zone zone13 = ZoneFactory.getZones().get(ZONE13);
		List<Contest> zoneContests = zoneContestsMap.get(ZONE13);
		try (StringWriter sw = new StringWriter(); ) 
		{
			GenerateEndorsementsFile.generateZoneContestsEndorsements(
					sw, zone13, zoneContests);
			generatedCandidatesString = sw.toString();
		} catch (IOException e) {
			fail("Unexpected IOException: " + e.getMessage());
		}
		
		System.out.println("**generateZoneEndorsements**");
		System.out.println(generatedCandidatesString);
		System.out.println();

		
//		System.out.printf("%s%n", generatedCandidatesString);
		assertEquals(expected, generatedCandidatesString);
	}
		
	
	
}
