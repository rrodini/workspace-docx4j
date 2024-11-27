package com.rodini.contestgen.extract;

import static org.junit.jupiter.api.Assertions.*;

import java.util.regex.Pattern;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.Logger;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.rodini.ballotutils.Utils;
import com.rodini.contestgen.common.Initialize;

class TestPageExtractor {
	private static MockedAppender mockedAppender;
	private static Logger logger;

	String expectedText1 =
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
	String expectedText2 =
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

	@BeforeAll
	static void setUpBeforeClass() throws Exception {
	    mockedAppender = new MockedAppender();
	    mockedAppender.start();
	    logger = (Logger)LogManager.getLogger(PageExtractor.class);
	    logger.addAppender(mockedAppender);
	    logger.setLevel(Level.ERROR);
	}

	@AfterAll
	static void tearDownAfterClass() throws Exception {
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
	void testExtractPageWithTwoPageBallot() {
		// Regexes for ROW OFFICE General Election of 2021 featuring Judge Retentions.
		// See "Chester-General-2021.properties"
		String rawText = Utils.readTextFile("./src/test/java/Muni-2021-Raw-Text1.txt");
		int     pageCount = 2;
		Pattern page1Regex = Utils.compileRegex("(?m)(.*?)(^CONTESTS AND QUESTIONS\\.$\n)(?<page>((.*)\n)*?)^Continued from front side of ballot$(.*)");
		Pattern page2Regex = Utils.compileRegex("(?m)(.*?)(^FOLLOWING JUDICIAL RETENTION\nQUESTIONS$\n)((?<page>((.*)\n)*^(No\n)))");
		String page1Text = PageExtractor.extractPage("005_Atglen", rawText, page1Regex, 1);
		String page2Text = PageExtractor.extractPage("005_Atglen", rawText, page2Regex, 2);
		assertEquals(expectedText1, page1Text);
		assertEquals(expectedText2, page2Text);
	}

	@Test
	void testExtractPageWithOnePageBallot() {
		// Regexes for ROW OFFICE General Election of 2021 with no Judge Retentions.
		// See "Chester-General-2021.properties"
		String rawText = Utils.readTextFile("./src/test/java/Muni-2021-Raw-Text2.txt");
		int     pageCount = 2;
		Pattern page1Regex = Utils.compileRegex("(?m)(.*?)(^CONTESTS AND QUESTIONS\\.$\n)(?<page>((.*)\n)*?)^Continued from front side of ballot$(.*)");
		Pattern page2Regex = Utils.compileRegex("(?m)(.*?)(^FOLLOWING JUDICIAL RETENTION\nQUESTIONS$\n)((?<page>((.*)\n)*^(No\n)))");
		String page1Text = PageExtractor.extractPage("005_Atglen", rawText, page1Regex, 1);
		String page2Text = PageExtractor.extractPage("005_Atglen", rawText, page2Regex, 2);
		assertEquals(expectedText1, page1Text);
		assertEquals("", page2Text);
	}

	@Test
	void testExtractPageWithBadRegex1() {
		// regex1 is plain bad.
		// Regexes for ROW OFFICE General Election of 2021 with no Judge Retentions.
		// But this ballot has one page.
		// See "Chester-General-2021.properties"
		String rawText = Utils.readTextFile("./src/test/java/Muni-2021-Raw-Text2.txt");
		int     pageCount = 1;
		Pattern page1Regex = Utils.compileRegex("\\ddd");
//		Pattern page2Regex = Utils.compileRegex("(?m)(.*?)(^FOLLOWING JUDICIAL RETENTION\nQUESTIONS$\n)((?<page>((.*)\n)*^(No\n)))");
		String page1Text = PageExtractor.extractPage("005_Atglen", rawText, page1Regex, 1);
		assertEquals(1, mockedAppender.messages.size());
		assertTrue(mockedAppender.messages.get(0).startsWith("no match for precinctNoName: 005_Atglen"));
	}

	
	@Test
	void testExtractPageWithBadRegex2() {
		// missing ?<page> within regex.
		// Regexes for ROW OFFICE General Election of 2021 with no Judge Retentions.
		// But this ballot has one page.
		// See "Chester-General-2021.properties"
		String rawText = Utils.readTextFile("./src/test/java/Muni-2021-Raw-Text2.txt");
		int     pageCount = 1;
		Pattern page1Regex = Utils.compileRegex("(?m)(.*?)(^CONTESTS AND QUESTIONS\\.$\n)(((.*)\n)*?)^Continued from front side of ballot$(.*)");
//		Pattern page2Regex = Utils.compileRegex("(?m)(.*?)(^FOLLOWING JUDICIAL RETENTION\nQUESTIONS$\n)((?<page>((.*)\n)*^(No\n)))");
		String page1Text = PageExtractor.extractPage("005_Atglen", rawText, page1Regex, 1);
		assertEquals(1, mockedAppender.messages.size());
		assertEquals("No group with name <page>", mockedAppender.messages.get(0));
	}

	
}
