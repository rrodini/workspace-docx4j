package com.rodini.contestgen;

import static java.util.stream.Collectors.joining;
import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class TestMuniTextExtractor {

	private MuniTextMarkers mtm;

	
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
		String muniRawText = readTextFile("./src/test/java/Muni-Raw-Text1.txt");
		String [] page1ContestName = {
				"Justice of the Supreme Court",
				"Tax Collector\nAtglen Borough"
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
		// assert existence of first and last contest on page 2.
		assertTrue(contestsText.indexOf(page2ContestName[0]) >= 0);
		assertTrue(contestsText.indexOf(page2ContestName[1]) >= 0);
	}
	@Test
	void testMuniExtract2() {
		String muniRawText = readTextFile("./src/test/java/Muni-Raw-Text2.txt");
		String [] page1ContestName = {
			"Justice of the Supreme Court",
			"Tax Collector\nAvondale Borough"
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
		// assert existence of first and last contest on page 2.
		assertTrue(contestsText.indexOf(page2ContestName[0]) >= 0);
		assertTrue(contestsText.indexOf(page2ContestName[1]) >= 0);
	}
}
