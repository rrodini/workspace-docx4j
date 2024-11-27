package com.rodini.contestgen.extract;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.Logger;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import java.util.regex.Pattern;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.rodini.ballotutils.Utils;
import com.rodini.contestgen.common.Initialize;
import com.rodini.contestgen.extract.MockedAppender;
import com.rodini.contestgen.model.Ballot;
import com.rodini.contestgen.model.Contest;


class TestContestExtractor {

	private static MockedAppender mockedAppender;
	private static Logger logger;
	
	private static String contestText1 =
					"Justice of the Supreme Court\n" +    // <= one line
					"Vote for ONE\n" +
					"Maria McLaughlin\n" +
					"Democratic\n" +
					"Kevin Brobson\n" +
					"Republican\n" +
					"Write-in\n";
	private static String contestText2 =
					"Judge of the\n" + 			// <= two lines
					"Court of Common Pleas\n" +
					"Vote for no more than TWO\n" +
					"Alita Rovito\n" +
					"Democratic\n" +
					"Tony Verwey\n" +
					"Democratic\n" +
					"Lou Mincarelli\n" +
					"Republican\n" +
					"PJ Redmond\n" +
					"Republican\n" +
					"Write-in\n" +
					"Write-in\n";
	private static String contestText3 =				
					"Treasurer\n" +			// <= one line
					"4 Year Term\n" +		// term
					"Vote for ONE\n" +		// instructions
					"Patricia A. Maisano\n" +
					"Democratic\n" +
					"Jennifer Nicolas\n" +
					"Republican\n" +
					"Write-in";
	private static String contestText4 =
					"Member Of Council\n" +  // <= two lines 
					"Atglen Borough\n" +
					"Unexpired 4 Year Term\n" +		  // term
					"Vote for no more than THREE\n" + // instructions
					"Charles H. Palmer Jr\n" +
					"Republican\n" +
					"Joshua Glick\n" +
					"Republican\n" +
					"Write-in\n" +
					"Write-in\n" +
					"Write-in\n";
	private static String contestText6 =
			"Justice of the Supreme Court\n" +    // <= NO MATCH
					//"Vote for ONE\n" +
					"Maria McLaughlin\n" +
					"Democratic\n" +
					"Kevin Brobson\n" +
					"Republican\n" +
					"Write-in\n";
	String page1Text =
	"""
	Justice of the Supreme Court
	Vote for ONE
	Maria McLaughlin
	Democratic
	Kevin Brobson
	Republican
	Write-in
	Judge of the
	Superior Court
	Vote for ONE
	Timika Lane
	Democratic
	Megan Sullivan
	Republican
	Write-in
	Judge of the Commonwealth Court
	Vote for no more than TWO
	Lori A. Dumas
	Democratic
	David Lee Spurgeon
	Democratic
	Stacy Marie Wallace
	Republican
	Drew Crompton
	Republican
	Write-in
	Write-in
	Judge of the
	Court of Common Pleas
	Vote for no more than TWO
	Alita Rovito
	Democratic
	Tony Verwey
	Democratic
	Lou Mincarelli
	Republican
	PJ Redmond
	Republican
	Write-in
	Write-in
	Treasurer
	4 Year Term
	Vote for ONE
	Patricia A. Maisano
	Democratic
	Jennifer Nicolas
	Republican
	Write-in
	Controller
	4 Year Term
	Vote for ONE
	Margaret Reif
	Democratic
	Regina Mauro
	Republican
	Write-in
	Clerk Of Courts
	4 Year Term
	Vote for ONE
	Yolanda Van de Krol
	Democratic
	Carmela Z. Ciliberti
	Republican
	Write-in
	Coroner
	4 Year Term
	Vote for ONE
	Sophia Garcia-Jackson
	Democratic
	Frank Speidel
	Republican
	Write-in
	School Director
	Octorara Region 1
	Unexpired 2 Year Term
	Vote for ONE
	Lisa M. Yelovich
	Democratic/Republican
	Write-in
	Mayor
	Atglen Borough
	4 Year Term
	Vote for ONE
	Darren Hodorovich
	Independent
	Write-in
	Member Of Council
	Atglen Borough
	4 Year Term
	Vote for no more than THREE
	Charles H. Palmer Jr
	Republican
	Joshua Glick
	Republican
	Write-in
	Write-in
	Write-in
	Member Of Council
	Atglen Borough
	Unexpired 2 Year Term
	Vote for ONE
	Write-in
	Tax Collector
	Atglen Borough
	4 Year Term
	Vote for ONE
	Write-in
	Constable
	Atglen Borough
	6 Year Term
	Vote for ONE
	Write-in
	""";
	String page2Text =
	"""
	Judge Of Elections
	005 Atglen
	Vote for ONE
	Paul J. Bigas
	Democratic
	Write-in
	Inspector Of Elections
	005 Atglen
	Vote for ONE
	Write-in
	Superior Court Retention
	Election Question
	Shall John T. Bender be retained for
	an additional term as Judge of the
	Superior Court of the Commonwealth
	of Pennsylvania?
	Yes
	No
	Superior Court Retention
	Election Question
	Shall Mary Jane Bowes be retained
	for an additional term as Judge of the
	Superior Court of the Commonwealth
	of Pennsylvania?
	Yes
	No
	Commonwealth Court Retention
	Election Question
	Shall Anne Covey be retained for an
	additional term as Judge of the
	Commonwealth Court of the
	Commonwealth of Pennsylvania?
	Yes
	No
	Commonwealth Court Retention
	Election Question
	Shall Renee Cohn Jubelirer be
	retained for an additional term as
	Judge of the Commonwealth Court of
	the Commonwealth of Pennsylvania?
	Yes
	No
	""";

	/**
	 * For some reason mvn test will not work if this is @Before, but in eclipse it works! As a
	 * result, we use @BeforeClass.
	 */
	@BeforeAll
	static void setupClass() {
	    mockedAppender = new MockedAppender();
	    mockedAppender.start();
	    logger = (Logger)LogManager.getLogger(ContestExtractor.class);
	    logger.addAppender(mockedAppender);
	    logger.setLevel(Level.ERROR);
		// regexes taken from "Chester-General-2021.properties"
		Pattern regex1 = Utils.compileRegex("^(?<name>(.*\n){1,3})(?<instructions>^Vote.*)\n(?<candidates>((.*\n){1})*)^Write-in$");
		Pattern regex2 = Utils.compileRegex("^(?<name>(.*\n){1,3})(?<term>^(\\d Year |Unexpired ).*)\n(?<instructions>^Vote.*)\n(?<candidates>((.*\n){1})*)^Write-in$");
		Pattern regex3 = Utils.compileRegex("^(?<name>(.*\n){1,3})(?<region>^Region [A-Z].*)\n(?<term>^(\\d |Unexpired ).*)\n(?<instructions>^Vote.*)\n(?<candidates>((.*\n){1})*)^Write-in$");
		Pattern [] contestRegex = {regex1, regex2, regex3};
		Initialize.contestRegex = contestRegex;
		Initialize.writeIn = "Write-in";
	}

	@AfterAll
	public static void teardown() {
		logger.removeAppender(mockedAppender);
		mockedAppender.stop();
	}
	
	@BeforeEach
	void setUp() throws Exception {
	}

	@AfterEach
	void tearDown() throws Exception {
		mockedAppender.messages.clear();
	}
	@Test
	void testExtractContest1() {
		Contest contest = ContestExtractor.extractContest("005", "ATGLEN", contestText1);
		assertTrue(contest != null);
		assertEquals("Justice of the Supreme Court", contest.getContestName());
		assertEquals(1, contest.getContestFormat());
	}
	@Test
	void testExtractContest2() {
		Contest contest = ContestExtractor.extractContest("005", "ATGLEN", contestText2);
		assertTrue(contest != null);
		assertEquals("Judge of the\nCourt of Common Pleas", contest.getContestName());
		assertEquals(1, contest.getContestFormat());
	}
	@Test
	void testExtractContest3() {
		Contest contest = ContestExtractor.extractContest("005", "ATGLEN", contestText3);
		assertTrue(contest != null);
		assertEquals("Treasurer\n4 Year Term", contest.getContestName());
		assertEquals(1, contest.getContestFormat());
	}
	@Test
	void testExtractContest4() {
		Contest contest = ContestExtractor.extractContest("005", "ATGLEN", contestText4);
		//System.out.printf("name: %s format: %d%n", name, format);
		assertTrue(contest != null);
		assertEquals("Member Of Council\nAtglen Borough\nUnexpired 4 Year Term", contest.getContestName());
		assertEquals(1, contest.getContestFormat());
	}
	@Test
	void testExtractContest6() {
		Contest contest = ContestExtractor.extractContest("005", "ATGLEN", contestText6);
		//System.out.printf("name: %s format: %d%n", name, format);
		assertTrue(contest == null);
		assertEquals(1, mockedAppender.messages.size());
		assertTrue(mockedAppender.messages.get(0).startsWith("no regex match."));
	}
	@Test
	void testFindContestEnd() {
		String contestText = contestText1 + contestText2;
		int end = ContestExtractor.findContestEnd(contestText, 0);
		// Do not count the trailing \n after Write-in
		assertEquals(103, end);
		end = ContestExtractor.findContestEnd(contestText, end);
		// Do not count the trailing \n after Write-in
		assertEquals(268, end);
	}
	@Test
	void testExtractPageContests() {
		List<Contest> contests1 = ContestExtractor.extractPageContests("005", "ATGLEN", page1Text);
		assertEquals(14, contests1.size());
		List<Contest> contests2 = ContestExtractor.extractPageContests("005", "ATGLEN", "");
		assertEquals(0, contests2.size());
	}
	@Test
	void testExtractContests() {
		// construct artificial ballot first.
		Ballot ballot = new Ballot("005_ATGLEN", "");
		ballot.setPage1Text(page1Text);
		ballot.setPage2Text(page2Text);
		ContestExtractor.extractContests(ballot);
		List<Contest> contests = ballot.getContests();
		assertEquals(17, contests.size());
	}
	
	
}
