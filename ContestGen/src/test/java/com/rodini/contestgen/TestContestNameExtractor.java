package com.rodini.contestgen;

import static org.junit.jupiter.api.Assertions.*;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.Logger;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.rodini.contestgen.MockedAppender;

class TestContestNameExtractor {

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
	private static String contestText5 =
					"Member Of Council\n" +  // <= two lines 
					"Atglen Borough\n" +
					"Region A\n" +           // bogus (not in real ballot)
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
			"Justice of the Supreme Court\n" +    // <= one line
					//"Vote for ONE\n" +          // NO MATCH
					"Maria McLaughlin\n" +
					"Democratic\n" +
					"Kevin Brobson\n" +
					"Republican\n" +
					"Write-in\n";
	private static String contestText7 =
					"Inspector Of Elections\n" +
					"759 W Whiteland 1\n" +
					"Vote for ONE\n" +
					"Write-in\n";					// <= no candidates
	
	ContestNameExtractor cne;

	/**
	 * For some reason mvn test will not work if this is @Before, but in eclipse it works! As a
	 * result, we use @BeforeClass.
	 */
	@BeforeAll
	static void setupClass() {
	    mockedAppender = new MockedAppender();
	    mockedAppender.start();
	    logger = (Logger)LogManager.getLogger(ContestNameExtractor.class);
	    logger.addAppender(mockedAppender);
	    logger.setLevel(Level.ERROR);
//	    ContestNameMarkers.initialize(false, null);
	    ContestGen.COUNTY = "chester";
		ContestNameMarkers.initialize("./src/test/java/Chester-General-2021.properties");
	}

	@AfterAll
	public static void teardown() {
		logger.removeAppender(mockedAppender);
		mockedAppender.stop();
	}
	
	@BeforeEach
	void setUp() throws Exception {
	    cne = new ContestNameExtractor();
	}

	@AfterEach
	void tearDown() throws Exception {
	}

	@Test
	void testConstructor() {
		assertTrue(cne != null);
	}
	@Test
	void testContestMatch1() {
		int format = cne.match(contestText1);
		String name = cne.getContestName();
		//System.out.printf("name: %s format: %d%n", name, format);
		assertEquals(1, format);
		assertEquals("Justice of the Supreme Court", name);
	}
	@Test
	void testContestMatch2() {
		int format = cne.match(contestText2);
		String name = cne.getContestName();
		//System.out.printf("name: %s format: %d%n", name, format);
		assertEquals(1, format);
		assertEquals("Judge of the\nCourt of Common Pleas", name);
	}
	@Test
	void testContestMatch3() {
		int format = cne.match(contestText3);
		String name = cne.getContestName();
		//System.out.printf("name: %s format: %d%n", name, format);
		assertEquals(2, format);
		assertEquals("Treasurer", name);
	}
	@Test	//@DisplayName("test for contest name of 2 lines format 2")
	void testContestMatch4() {
		int format = cne.match(contestText4);
		String name = cne.getContestName();
		//System.out.printf("name: %s format: %d%n", name, format);
		assertEquals(2, format);
		assertEquals("Member Of Council\nAtglen Borough", name);
	}
	@Test
	void testContestMatch5() {
		int format = cne.match(contestText5);
		String name = cne.getContestName();
		//System.out.printf("name: %s format: %d%n", name, format);
		assertEquals(3, format);
		assertEquals("Member Of Council\nAtglen Borough", name);
	}
	@Test
	void testContestMatch6() {
		int format = cne.match(contestText6);
		String name = cne.getContestName();
		//System.out.printf("name: %s format: %d%n", name, format);
		assertEquals(-1, format);
		assertEquals("Unknown contest", name);
	}
	//@Disabled
	@Test
	void testContestMatch7() {
		int format = cne.match(contestText7);
		String name = cne.getContestName();
		//System.out.printf("name: %s format: %d%n", name, format);
		assertEquals(1, format);
		assertEquals("Inspector Of Elections\n759 W Whiteland 1", name);
	}
	@Test
	void testDumpPatterns() {
		ContestNameExtractor.dumpPatterns();
		assertTrue(true);
	}
}
