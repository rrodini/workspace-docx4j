package com.rodini.ballotgen.endorsement;

import static com.rodini.ballotgen.endorsement.EndorsementMode.*;
import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.rodini.ballotutils.ElectionType;
import com.rodini.ballotutils.Party;
import com.rodini.ballotgen.endorsement.EndorsementFactory;
import com.rodini.ballotgen.endorsement.EndorsementMode;
import com.rodini.ballotgen.endorsement.EndorsementProcessor;
import com.rodini.zoneprocessor.ZoneProcessor;

class TestEndorsementProcessor {
//	Taken from 2023 zone realignment spreadsheet
	String precinctZoneCSVText = 
	"""
	Zones
	#zone #,zone name,zone logo path
	2,Honeybrook Elverson Dems,./src/test/java/zone-logo-02.jpg
	13,Great Valley Dems,./src/test/java/zone-logo-13.jpg
	Precincts
	#precinct #,precinct name,zone #
	5,ATGLEN,2
	220,ELVERSON,2
	65,CHARLESTOWN,13
	350,MALVERN,13
	770,WILLISTOWN N-1,13
	775,WILLISTOWN N-2,13
	""";
	
	@BeforeEach
	void setUp() throws Exception {
	}

	@AfterEach
	void tearDown() throws Exception {
		ZoneProcessor.clearPrecinctZoneMap();
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
		EndorsementMode mode01 = ep.getEndorsementMode(name1, contest, Party.DEMOCRATIC, "770");
		EndorsementMode mode02 = ep.getEndorsementMode(name2, contest, Party.REPUBLICAN, "770");
		// Dems are automatically endorsed in general election.
		assertEquals(ENDORSED, mode01);
		// non-Dems are automatically anti-endorsed in general election.
		assertEquals(ANTIENDORSED, mode02);
	}
	@Test
	void testGeneral02() {
		// Endorsement by zone for cross-filers in general election.
		String endorsementsCSVText = "Tricia Bliven-Chasinoff,endorsed,zone,13\n" +
									 "Sallie Campbell,antiendorsed,zone,13";
		EndorsementProcessor ep = new EndorsementProcessor(ElectionType.GENERAL,
				Party.DEMOCRATIC, endorsementsCSVText, precinctZoneCSVText );
		String name1 = "Tricia Bliven-Chasinoff";
		String name2 = "Sallie Campbell";
		String contest = "School Director\nGreat Valley Region 3";
		// assume that both have cross-filed.
		EndorsementMode mode01 = ep.getEndorsementMode(name1, contest, Party.DEM_REP, "770");
		EndorsementMode mode02 = ep.getEndorsementMode(name2, contest, Party.DEM_REP, "770");
		assertEquals(ENDORSED, mode01);
		assertEquals(ANTIENDORSED, mode02);
	}
	@Test
	void testPrimary01() {
		// Endorsements at the State, County, and zone levels
		String endorsementsCSVText = "Maria McLaughlin, endorsed, State\n" +
				"Alita Rovito, unendorsed, County\n" +
				"Ken Flinchbaugh, endorsed, ZONE, 13\n" +
				"Tony Verwey, antiendorsed, zone, 5";
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
		EndorsementMode mode01 = ep.getEndorsementMode(name1, contest1, null, "770");
		EndorsementMode mode02 = ep.getEndorsementMode(name2, contest2, null, "770");
		EndorsementMode mode03 = ep.getEndorsementMode(name3, contest3, null, "770");
		EndorsementMode mode04 = ep.getEndorsementMode(name4, contest4, null, "770");
		assertEquals(ENDORSED, mode01);
		assertEquals(UNENDORSED, mode02);
		assertEquals(ANTIENDORSED, mode03);
		assertEquals(ENDORSED, mode04);
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
		EndorsementMode mode01 = ep.getEndorsementMode(name1, contest1, null, "770");
		EndorsementMode mode02 = ep.getEndorsementMode(name2, contest2, null, "770");
		EndorsementMode mode03 = ep.getEndorsementMode(name3, contest3, null, "770");
		EndorsementMode mode04 = ep.getEndorsementMode(name4, contest4, null, "770");
		assertEquals(ANTIENDORSED, mode01);
		assertEquals(ANTIENDORSED, mode02);
		assertEquals(ANTIENDORSED, mode03);
		assertEquals(ANTIENDORSED, mode04);
	}
}
