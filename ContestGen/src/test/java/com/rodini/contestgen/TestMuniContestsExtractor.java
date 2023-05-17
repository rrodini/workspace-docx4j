package com.rodini.contestgen;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.Logger;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class TestMuniContestsExtractor {
	private static MockedAppender mockedAppender;
	private static Logger logger;
	private MuniContestsExtractor mce;
	
	String contestsText =						// sample from 2021 General Election
			"Justice of the Supreme Court\n" +
			"Vote for ONE\n" +
			"Maria McLaughlin\n" +
			"Democratic\n" +
			"Kevin Brobson\n" +
			"Republican\n" +
			"Write-in\n" +
			"Judge of the Superior Court\n" +
			"Vote for ONE\n" +
			"Timika Lane\n" +
			"Democratic\n" +
			"Megan Sullivan\n" +
			"Republican\n" +
			"Write-in\n" +
			"Judge of the Commonwealth Court\n" +
			"Vote for no more than TWO\n" +
			"Lori A. Dumas\n" +
			"Democratic\n" +
			"David Lee Spurgeon\n" +
			"Democratic\n" +
			"Stacy Marie Wallace\n" +
			"Republican\n" +
			"Drew Crompton\n" +
			"Republican\n" +
			"Write-in\n" +
			"Write-in\n" +
			"Judge of the\n" +
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
			"Write-in\n" +
			"Treasurer\n" +
			"4 Year Term\n" +
			"Vote for ONE\n" +
			"Patricia A. Maisano\n" +
			"Democratic\n" +
			"Jennifer Nicolas\n" +
			"Republican\n" +
			"Write-in\n" +
			"Controller\n" +
			"4 Year Term\n" +
			"Vote for ONE\n" +
			"Margaret Reif\n" +
			"Democratic\n" +
			"Regina Mauro\n" +
			"Republican\n" +
			"Write-in\n" +
			"Clerk Of Courts\n" +
			"4 Year Term\n" +
			"Vote for ONE\n" +
			"Yolanda Van de Krol\n" +
			"Democratic\n" +
			"Carmela Z. Ciliberti\n" +
			"Republican\n" +
			"Write-in\n" +
			"Coroner\n" +
			"4 Year Term \n" +
			"Vote for ONE\n" +
			"Sophia Garcia-Jackson\n" +
			"Democratic\n"  +
			"Frank Speidel\n"  +
			"Republican\n"  +
			"Write-in\n"  +
			"School Director\n"  +
			"Octorara Region 1\n"  +
			"Unexpired 2 Year Term\n"  +
			"Vote for ONE\n"  +
			"Lisa M. Yelovich\n"  +
			"Democratic/Republican\n"  +
			"Write-in\n"  +
			"Mayor\n"  +
			"Atglen Borough\n"  +
			"4 Year Term\n"  +
			"Vote for ONE\n"  +
			"Darren Hodorovich\n"  +
			"Independent\n"  +
			"Write-in\n"  +
			"Member Of Council\n"  +
			"Atglen Borough\n"  +
			"4 Year Term\n"  +
			"Vote for no more than THREE\n"  +
			"Charles H. Palmer Jr\n"  +
			"Republican\n"  +
			"Joshua Glick\n"  +
			"Republican\n"  +
			"Write-in\n"  +
			"Write-in\n"  +
			"Write-in\n"  +
			"Member Of Council\n"  +
			"Atglen Borough\n"  +
			"Unexpired 2 Year Term\n"  +
			"Vote for ONE\n"  +
			"Write-in\n"  +
			"Tax Collector\n"  +
			"Atglen Borough\n"  +
			"4 Year Term\n"  +
			"Vote for ONE\n"  +
			"Write-in\n"  +
			"Constable\n"  +
			"Atglen Borough\n"  +
			"6 Year Term\n"  +
			"Vote for ONE\n"  +
			"Write-in\n"  +
			"Judge Of Elections\n"  +
			"005 Atglen\n"  +
			"Vote for ONE\n"  +
			"Paul J. Bigas\n"  +
			"Democratic\n"  +
			"Write-in\n"  +
			"Inspector Of Elections\n"  +
			"005 Atglen\n"  +
			"Vote for ONE\n"  +
			"Write-in\n";

	
	/**
	 * For some reason mvn test will not work if this is @Before, but in eclipse it works! As a
	 * result, we use @BeforeClass.
	 */
	@BeforeAll
	static void setupClass() {
	    mockedAppender = new MockedAppender();
	    mockedAppender.start();
	    logger = (Logger)LogManager.getLogger(MuniContestsExtractor.class);
	    logger.addAppender(mockedAppender);
	    logger.setLevel(Level.ERROR);
	}

	@AfterAll
	public static void teardown() {
		logger.removeAppender(mockedAppender);
		mockedAppender.stop();
	}
	
	@BeforeEach
	void setUp() throws Exception {
	    ContestNameMarkers.initialize("./src/test/java/Chester-General-2021.properties");
	    ContestGen.COUNTY="Chester";
	    ContestGen.WRITE_IN="Write-in\n";
		mce = new MuniContestsExtractor("Atglen", contestsText, "");
	}

	@AfterEach
	void tearDown() throws Exception {
	}
	
	@Test
	public void testExtract1() {
	System.out.printf("testExtract1:%n");
	System.out.printf("WRITE_IN: %s%n", ContestGen.WRITE_IN);
		MuniContestNames muniContestNames = mce.extract();
		List<ContestName> contestNames = muniContestNames.get();
		assertEquals(16, contestNames.size());
		List<String> names = new ArrayList<>();
		for (ContestName cn: contestNames) {
			names.add(cn.getName());
			System.out.printf("Contest Name: %s%n", cn.getName());
		}
		assertTrue(names.contains("Justice of the Supreme Court"));
		assertTrue(names.contains("Judge of the\nCourt of Common Pleas"));
		assertTrue(names.contains("School Director\nOctorara Region 1"));
		assertTrue(names.contains("Inspector Of Elections\n005 Atglen"));
	}
}
