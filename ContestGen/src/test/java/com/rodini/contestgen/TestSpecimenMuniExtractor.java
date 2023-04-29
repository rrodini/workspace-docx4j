package com.rodini.contestgen;

import static java.util.stream.Collectors.joining;
import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

class TestSpecimenMuniExtractor {

	SpecimenMuniMarkers stm;
	SpecimenMuniExtractor sme;
	
	@BeforeEach
	void setUp() throws Exception {
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
	public void testExtract1() {
		// CHESTER CO. HERE
	    ContestGen.COUNTY="chester";
	    ContestGen.WRITE_IN="Write-in\n";
		SpecimenMuniMarkers.initialize("./src/test/java/Chester-General-2021.properties");
		List<MuniTextExtractor> muniExtracts = new ArrayList<>();
		String specText = readTextFile("./src/test/java/Chester-General-2021.txt");
		sme = new SpecimenMuniExtractor(specText);
		muniExtracts = sme.extract();
		assertEquals(232, muniExtracts.size());
		// first name in list
		assertEquals("005_Atglen", muniExtracts.get(0).getMuniName());
		// last name in list
		assertEquals("860_West_Chester_7", muniExtracts.get(muniExtracts.size()-1).getMuniName());;
	}
	@Disabled
	@Test
	public void testExtract2() {
		// BUCKS CO. HERE
	    ContestGen.COUNTY="bucks";
	    ContestGen.WRITE_IN="Write-In\n";
		SpecimenMuniMarkers.initialize("./src/test/java/Chester-General-2021.properties");
		List<MuniTextExtractor> muniExtracts = new ArrayList<>();
		String specText = readTextFile("./src/test/java/Bucks-Two-Precincts.txt");
		sme = new SpecimenMuniExtractor(specText);
		muniExtracts = sme.extract();
		assertEquals(2, muniExtracts.size());
		// first name in list
		assertEquals("347_Bedminster_Twp_East", muniExtracts.get(0).getMuniName());
		// last name in list
		assertEquals("347_Bedminster_Twp_West", muniExtracts.get(muniExtracts.size()-1).getMuniName());;
	}
	
}
