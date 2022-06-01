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
import org.junit.jupiter.api.Test;

class TestSpecimenMuniExtractor {

	SpecimenMuniMarkers stm;
	SpecimenMuniExtractor sme;
	
	@BeforeEach
	void setUp() throws Exception {
		SpecimenMuniMarkers.initialize(false, null);
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
		List<MuniTextExtractor> muniExtracts = new ArrayList<>();
		String specText = readTextFile("./src/test/java/General-2021.txt");
		sme = new SpecimenMuniExtractor(specText);
		muniExtracts = sme.extract();
//		System.out.printf("size: %d%n", muniExtracts.size());
//		System.out.printf("first: %s%n", muniExtracts.get(0).getMuniName());
//		System.out.printf("last: %s%n", muniExtracts.get(muniExtracts.size()-1).getMuniName());
		assertEquals(232, muniExtracts.size());
		// first name in list
		assertEquals("Atglen", muniExtracts.get(0).getMuniName());
		// last name in list
		assertEquals("West_Chester_7", muniExtracts.get(muniExtracts.size()-1).getMuniName());;
	}

}
