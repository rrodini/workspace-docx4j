package com.rodini.contestgen;

import static org.junit.jupiter.api.Assertions.*;

import java.util.regex.Pattern;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.rodini.ballotutils.Utils;

class TestContestNameMarkers {

	private static final String [] testContestNameFormats = {
	  "^(?<name>(.*\n){1,3})(?<instructions>^Vote.*)\n(?<candidates>((.*\n){1})*)^Write-in$",
	  "^(?<name>(.*\n){1,3})(?<term>^(\\d Year |Unexpired ).*)\n(?<instructions>^Vote.*)\n(?<candidates>((.*\n){1})*)^Write-in$",
	  "^(?<name>(.*\n){1,3})(?<region>^Region [A-Z].*)\n(?<term>^(\\d |Unexpired ).*)\n(?<instructions>^Vote.*)\n(?<candidates>((.*\n){1})*)^Write-in$"	
	};

	private static Pattern [] testContestNamePatterns = new Pattern[testContestNameFormats.length];; 
			
	@BeforeEach
	void setUp() throws Exception {
		int index = 0;
		for (String format: testContestNameFormats) {
			testContestNamePatterns[index] = Utils.compileRegex(format);
			index++;
		}
	}

	@AfterEach
	void tearDown() throws Exception {
	}

	private void testRegexValue(Pattern testPat, Pattern pat) {
		String testPatRegex = testPat.toString();
		String patRegex = pat.toString();
		//testPatRegex = "\"" + testPatRegex.replaceAll("\n", "\\\\\\n") + "\"";
		System.out.printf("%s%n%n", testPatRegex);
		System.out.printf("%s%n%n", patRegex);
		assertEquals(testPatRegex, patRegex);
	}
	
	@Test
	void testLoadFromResources() {
		ContestNameMarkers.initialize("./src/test/java/General-2021.properties");
		Pattern [] patterns = ContestNameMarkers.getContestNamePatterns();
		assertEquals(testContestNamePatterns.length, patterns.length);
		for (int i = 0; i < patterns.length; i++) {
			testRegexValue(testContestNamePatterns[i], patterns[i]);
		}
	}

}
