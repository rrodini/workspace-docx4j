package com.rodini.contestgen;

import static org.junit.jupiter.api.Assertions.*;

import java.util.regex.Pattern;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class TestSpecimenMuniMarkers {
	
//	private int testMuniNameRepeatCount;
//	private Pattern testMuniNamePattern;

	private static final String [] testSpecimenMuniMarker = {
			  "(?m)^OFFICIAL MUNICIPAL PRIMARY ELECTION BALLOT$\n(?<id>\\d+)[\\s]*(?<name>.*)\n"
			};

	@BeforeEach
	void setUp() throws Exception {
	}

	@AfterEach
	void tearDown() throws Exception {
	}

	@Test
	void testLoadFromResources() {
		SpecimenMuniMarkers.initialize("./src/test/java/Primary-Dems-2021.properties");
		assertEquals(2, SpecimenMuniMarkers.getRepeatCount());
		String regex = testSpecimenMuniMarker[0];
		String patternRegex = SpecimenMuniMarkers.getMuniNamePattern().toString();
		//System.out.println(patternRegex);
		assertEquals(regex, patternRegex);
	}

}
