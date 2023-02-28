package com.rodini.contestgen;

import static org.junit.jupiter.api.Assertions.*;

import java.util.regex.Pattern;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class TestContestNameMarkers {

//	private static final String [] testContestNameFormats = {
//			"^(?<name>(.*\n){1,2})(?<instructions>^Vote.*)\n(?<candidates>((.*\n){1})*)^Write-in$",
//			"^(?<name>(.*\n){1,2})(?<term>^(\\d Year |Unexpired ).*)\n(?<instructions>^Vote.*)\n(?<candidates>((.*\n){1})*)^Write-in$",
//			"^(?<name>(.*\n){1,2})(?<region>^Region [A-Z].*)\n(?<term>^(\\d |Unexpired ).*)\n(?<instructions>^Vote.*)\n(?<candidates>((.*\n){1})*)^Write-in$"
//	};

	private static Pattern [] testContestNamePatterns; 
			
	@BeforeEach
	void setUp() throws Exception {
		ContestNameMarkers.initialize(false, null);
		testContestNamePatterns = ContestNameMarkers.getContestNamePatterns();
	}

	@AfterEach
	void tearDown() throws Exception {
	}

	private boolean testRegexValue(Pattern testPat, Pattern pat) {
		String testPatRegex = testPat.toString();
		String patRegex = pat.toString();
		//testPatRegex = "\"" + testPatRegex.replaceAll("\n", "\\\\\\n") + "\"";
		System.out.printf("%s%n", testPatRegex);
		System.out.printf("%s%n", patRegex);
		return testPatRegex.equals(patRegex);
	}
	
	@Test
	void testLoadFromResources() {
		ContestNameMarkers.initialize(true, "./resources/contestgen.properties");
		Pattern [] patterns = ContestNameMarkers.getContestNamePatterns();
		assertEquals(testContestNamePatterns.length, patterns.length);
		for (int i = 0; i < patterns.length; i++) {
			assertTrue(testRegexValue(testContestNamePatterns[i], patterns[i]));
		}
	}

}
