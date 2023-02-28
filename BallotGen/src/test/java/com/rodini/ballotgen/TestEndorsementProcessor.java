package com.rodini.ballotgen;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.rodini.zoneprocessor.GenMuniMap;

class TestEndorsementProcessor {
//	Taken from 2023 zone realignment spreadsheet
	String precinctZoneCSVText = 
		"precinct,name,zone name,zone #" +
		"5,ATGLEN,Honeybrook Elverson,2\n" +
		"220,ELVERSON,Honeybrook Elverson,2\n" +
		"65,CHARLESTOWN,Great Valley,13\n" +
		"350,MALVERN,Great Valley,13\n" +
		"770,WILLISTOWN N-1,Great Valley,13\n" +
		"775,WILLISTOWN N-2,Great Valley,13";
	
	@BeforeEach
	void setUp() throws Exception {
	}

	@AfterEach
	void tearDown() throws Exception {
		GenMuniMap.clearMuniNoMap();
		EndorsementFactory.clearCandidateEndorsements();
	}
	@Test
	void testGeneral01() {
		// Straight endorsement by party in general election.
		String endorsementsCSVText = "";
		EndorsementProcessor ep = new EndorsementProcessor(ElectionType.GENERAL,
				Party.DEMOCRATIC, endorsementsCSVText, precinctZoneCSVText );
		String name1 = "Maria McLaughlan";
		String name2 = "Kevin Brobson";
		String contest = "Justice of the Supreme Court";
		// Endorse the favored party
		boolean endorsed01 = ep.isEndorsed(name1, contest, Party.DEMOCRATIC, "770");
		boolean endorsed02 = ep.isEndorsed(name2, contest, Party.REPUBLICAN, "770");
		assertTrue(endorsed01);
		assertFalse(endorsed02);
	}
	@Test
	void testGeneral02() {
		// Endorsement by zone for cross-filers in general election.
		String endorsementsCSVText = "Tricia Bliven-Chasinoff,zone,13";
		EndorsementProcessor ep = new EndorsementProcessor(ElectionType.GENERAL,
				Party.DEMOCRATIC, endorsementsCSVText, precinctZoneCSVText );
		String name1 = "Tricia Bliven-Chasinoff";
		String name2 = "Sallie Campbell";
		String contest = "School Director\nGreat Valley Region 3";
		// assume that both have cross-filee
		boolean endorsed1 = ep.isEndorsed(name1, contest, Party.DEM_REP, "770");
		boolean endorsed2 = ep.isEndorsed(name2, contest, Party.DEM_REP, "770");
		assertTrue(endorsed1);
		assertFalse(endorsed2);
	}
	@Test
	void testPrimary01() {
		// Endorsements at the State, County, and zone levels
		String endorsementsCSVText = "Maria McLaughlin, State\n" +
				"Alita Rovito, County\n" +
				"Ken Flinchbaugh, ZONE, 13\n" +
				"Tony Verwey, zone, 5";
		EndorsementProcessor ep = new EndorsementProcessor(ElectionType.PRIMARY,
				Party.DEMOCRATIC, endorsementsCSVText, precinctZoneCSVText);
		String name1 = "MARIA MCLAUGHLIN";
		String name2 = "Alita Rovito";
		String name3 = "TONY VERWAY";
		String name4 = "KEN FLINCHBAUGH";
		String contest1 = "Justice of the Supreme Court";
		String contest2 = "Judge of the Court of Common\nPleas";
		String contest3 = "Judge of the Court of Common\nPleas";
		String contest4 = "Tax Collector\nWillistown Township";
		boolean endorsed1 = ep.isEndorsed(name1, contest1, null, "770");
		boolean endorsed2 = ep.isEndorsed(name2, contest2, null, "770");
		boolean endorsed3 = ep.isEndorsed(name3, contest3, null, "770");
		boolean endorsed4 = ep.isEndorsed(name4, contest4, null, "770");
		assertTrue(endorsed1);
		assertTrue(endorsed2);
		assertFalse(endorsed3);
		assertTrue(endorsed4);
	}
	@Test
	void testPrimary02() {
		// No endorsements.
		String endorsementsCSVText = "";
		EndorsementProcessor ep = new EndorsementProcessor(ElectionType.PRIMARY,
				Party.DEMOCRATIC, endorsementsCSVText, precinctZoneCSVText);
		String name1 = "MARIA MCLAUGHLIN";
		String name2 = "Alita Rovito";
		String name3 = "TONY VERWAY";
		String name4 = "KEN FLINCHBAUGH";
		String contest1 = "Justice of the Supreme Court";
		String contest2 = "Judge of the Court of Common\nPleas";
		String contest3 = "Judge of the Court of Common\nPleas";
		String contest4 = "Tax Collector\nWillistown Township";
		boolean endorsed1 = ep.isEndorsed(name1, contest1, null, "770");
		boolean endorsed2 = ep.isEndorsed(name2, contest2, null, "770");
		boolean endorsed3 = ep.isEndorsed(name3, contest3, null, "770");
		boolean endorsed4 = ep.isEndorsed(name4, contest4, null, "770");
		assertFalse(endorsed1);
		assertFalse(endorsed2);
		assertFalse(endorsed3);
		assertFalse(endorsed4);
	}
}
