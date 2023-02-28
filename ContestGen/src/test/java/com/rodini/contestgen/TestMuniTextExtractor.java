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
@Disabled
class TestMuniTextExtractor {

	private static MockedAppender mockedAppender;
	private static Logger logger;

	private MuniTextMarkers mtm;
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
		MuniTextMarkers.initialize(false, null);;
		
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
		String muniRawText = readTextFile("./src/test/java/Muni-2022-Raw-Text1.txt");
		String [] page1ContestName = {
				"United States Senator",
				"Governor and Lieutenant\nGovernor"
			};
		String [] page2ContestName = {
					"Judge Of Elections\n005 Atglen",
					"Inspector Of Elections\n005 Atglen"
				};
		MuniTextExtractor mte = new MuniTextExtractor("Atglen", muniRawText);
		MuniContestsExtractor mce = mte.extract();
		String contestsText = mce.getMuniContestsText();
		// assert existence of first and last contest on page 1.
		assertTrue(contestsText.indexOf(page1ContestName[0]) >= 0);
		assertTrue(contestsText.indexOf(page1ContestName[1]) >= 0);
		if (pageCount == 2) {
			// assert existence of first and last contest on page 2.
			assertTrue(contestsText.indexOf(page2ContestName[0]) >= 0);
			assertTrue(contestsText.indexOf(page2ContestName[1]) >= 0);
		}
	}
	@Test
	void testMuniExtract2() {
		String muniRawText = readTextFile("./src/test/java/Muni-2022-Raw-Text2.txt");
		String [] page1ContestName = {
			"Representative in Congress\n6th District",
			"Assembly\n158th District"
		};
		String [] page2ContestName = {
				"Judge Of Elections\n010 Avondale",
				"Inspector Of Elections\n010 Avondale"
			};
		MuniTextExtractor mte = new MuniTextExtractor("Avondale", muniRawText);
		mte.extract();
		MuniContestsExtractor mce = mte.extract();
		String contestsText = mce.getMuniContestsText();
		// assert existence of first and last contest on page 1.
		assertTrue(contestsText.indexOf(page1ContestName[0]) >= 0);
		assertTrue(contestsText.indexOf(page1ContestName[1]) >= 0);
		if (pageCount == 2) {
		// assert existence of first and last contest on page 2.
			assertTrue(contestsText.indexOf(page2ContestName[0]) >= 0);
			assertTrue(contestsText.indexOf(page2ContestName[1]) >= 0);
		}
	}
	@Test
	void testMuniExtractError1() {
		String muniRawText = "Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do\n" +
					"eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut\n" +
					"enim ad minim veniam, quis nostrud exercitation ullamco laboris\n" +
					"nisi ut aliquip ex ea commodo consequat. Duis aute irure dolor in\n" +
					"reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla\n" +
					"pariatur. Excepteur sint occaecat cupidatat non proident, sunt in\n" +
					"culpa qui officia deserunt mollit anim id est laborum.";
		String [] page1ContestName = {
			"Representative in Congress\n6th District",
			"Assembly\n158th District"
		};
		String [] page2ContestName = {
				"Judge Of Elections\n010 Avondale",
				"Inspector Of Elections\n010 Avondale"
			};
		String expected = "no match for municipal page.";
		MuniTextExtractor mte = new MuniTextExtractor("Avondale", muniRawText);
		mte.extract();
		MuniContestsExtractor mce = mte.extract();
		String contestsText = mce.getMuniContestsText();
		// 2 error - one for each contest name listed above
		assertEquals(2,  mockedAppender.messages.size());
		assertTrue(mockedAppender.messages.get(0).startsWith(expected));
	}
}
