package com.rodini.contestgen;

import static java.util.stream.Collectors.joining;
import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.Logger;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import com.ginsberg.junit.exit.ExpectSystemExit;

/**
 * Tests based on General 2021 election
 *
 */
class TestMuniTextExtractor {

	private static MockedAppender mockedAppender;
	private static Logger logger;

	private int pageCount; // 1 => contests on page 1 only

	@BeforeAll
	static void setupClass() {
	    mockedAppender = new MockedAppender();
	    mockedAppender.start();
	    logger = (Logger)LogManager.getLogger(MuniTextExtractor.class);
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
		ContestGen.COUNTY = "chester";
		MuniTextMarkers.initialize("./src/test/java/Chester-General-2021.properties");
		pageCount = MuniTextMarkers.getPageCount();
	}

	@AfterEach
	void tearDown() throws Exception {
	}
	
	public static String readTextFile(String textFilePath) {
 		List<String> textLines = null;
		try {
			textLines = Files.readAllLines(Path.of(textFilePath));
		} catch (IOException e) {
			System.out.println("cannot read file: " + textFilePath);
			System.exit(1);
		}
 		String text = textLines.stream().collect(joining("\n"));
 		return text;
	}

	@Test
	void testMuniExtract1() {
		String muniRawText = readTextFile("./src/test/java/Muni-2021-Raw-Text1.txt");
		String [] page1ContestName = {
				"Justice of the Supreme Court",
				"Judge of the\nSuperior Court"
			};
		String [] page2ContestName = {
					"Judge Of Elections\n005 Atglen",
					"Inspector Of Elections\n005 Atglen"
				};
		MuniTextExtractor mte = new MuniTextExtractor("050 Atglen", muniRawText);
		MuniContestsExtractor mce = mte.extract();
		String page1Text = mce.getMuniPage1Text();
		String page2Text = mce.getMuniPage2Text();
		
//		System.out.println("\ntestMuniExtract1()");
//		System.out.println();
//		System.out.println(page1Text);
//		System.out.println();
//		System.out.println("");

		// assert existence of first and last contest on page 1.
		assertTrue(page1Text.indexOf(page1ContestName[0]) >= 0);
		assertTrue(page1Text.indexOf(page1ContestName[1]) >= 0);
		if (pageCount == 2) {
			// assert existence of first and last contest on page 2.
			assertTrue(page2Text.indexOf(page2ContestName[0]) >= 0);
			assertTrue(page2Text.indexOf(page2ContestName[1]) >= 0);
		}
	}
	@Test
	void testMuniExtract2() {
		String muniRawText = readTextFile("./src/test/java/Muni-2021-Raw-Text2.txt");
		String [] page1ContestName = {
			"Magisterial District Judge\nDistrict 15-4-04",
			"School Director\nAvon Grove Region 3"
		};
		String [] page2ContestName = {
				"Constable\nAvondale Borough",
				"Inspector Of Elections\n010 Avondale"
			};
		MuniTextExtractor mte = new MuniTextExtractor("010 Avondale", muniRawText);
		mte.extract();
		MuniContestsExtractor mce = mte.extract();
		String page1Text = mce.getMuniPage1Text();
		String page2Text = mce.getMuniPage2Text();
		// assert existence of first and last contest on page 1.
		assertTrue(page1Text.indexOf(page1ContestName[0]) >= 0);
		assertTrue(page1Text.indexOf(page1ContestName[1]) >= 0);
		if (pageCount == 2) {
		// assert existence of first and last contest on page 2.
			assertTrue(page2Text.indexOf(page2ContestName[0]) >= 0);
			assertTrue(page2Text.indexOf(page2ContestName[1]) >= 0);
		}
	}
	@Test
	@ExpectSystemExit
	void testMuniExtractError1() {
		String muniRawText = "Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do\n" +
					"eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut\n" +
					"enim ad minim veniam, quis nostrud exercitation ullamco laboris\n" +
					"nisi ut aliquip ex ea commodo consequat. Duis aute irure dolor in\n" +
					"reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla\n" +
					"pariatur. Excepteur sint occaecat cupidatat non proident, sunt in\n" +
					"culpa qui officia deserunt mollit anim id est laborum.";
		String expected = "no match for municipal page.";
		MuniTextExtractor mte = new MuniTextExtractor("010 Avondale", muniRawText);
		mte.extract();
		// fatal error should be detected.
	}
}
