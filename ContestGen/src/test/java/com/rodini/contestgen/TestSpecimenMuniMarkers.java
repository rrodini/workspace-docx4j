package com.rodini.contestgen;

import static org.junit.jupiter.api.Assertions.*;

import java.util.regex.Pattern;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class TestSpecimenMuniMarkers {
	
	private int testMuniNameRepeatCount;
	private Pattern testMuniNamePattern;

	@BeforeEach
	void setUp() throws Exception {
		SpecimenMuniMarkers.initialize(false, null);
		// save the values immediately.
		testMuniNameRepeatCount = SpecimenMuniMarkers.getRepeatCount();
		testMuniNamePattern = SpecimenMuniMarkers.getMuniNamePattern();
	}

	@AfterEach
	void tearDown() throws Exception {
	}

	@Test
	void testLoadFromResources() {
//		SpecimenMuniMarkers.initialize(true, "./src/test/java/test-props1.properties");
		SpecimenMuniMarkers.initialize(true, "./resources/contestgen.properties");
		assertEquals(testMuniNameRepeatCount, SpecimenMuniMarkers.getRepeatCount());
		String regex = testMuniNamePattern.toString();
		String patternRegex = SpecimenMuniMarkers.getMuniNamePattern().toString();
		//System.out.println(patternRegex);
//		assertEquals(regex, patternRegex);
	}

}
