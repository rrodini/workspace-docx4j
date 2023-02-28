package com.rodini.contestgen;

import static org.junit.jupiter.api.Assertions.*;

import java.util.regex.Pattern;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class TestMuniTextMarkers {

	private static int testPageCount;
	private static Pattern testPage1Pattern;
	private static Pattern testPage2Pattern;

	
	@BeforeEach
	void setUp() throws Exception {
		MuniTextMarkers.initialize(false, null);
		// save the values immediately.
		testPageCount = MuniTextMarkers.getPageCount();
		testPage1Pattern = MuniTextMarkers.getPage1Pattern();
		testPage2Pattern = MuniTextMarkers.getPage2Pattern();
	}

	@AfterEach
	void tearDown() throws Exception {
	}

	@Test
	void testLoadFromResources() {
		MuniTextMarkers.initialize(true, "./resources/contestgen.properties");
		assertEquals(testPageCount, MuniTextMarkers.getPageCount());
		String regex = testPage1Pattern.toString();
		System.out.println(regex);
		String patternRegex = MuniTextMarkers.getPage1Pattern().toString();
		System.out.println(patternRegex);
//		assertEquals(regex, patternRegex);
		
	}

}
