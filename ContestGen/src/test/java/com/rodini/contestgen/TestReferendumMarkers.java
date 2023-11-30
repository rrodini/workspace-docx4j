package com.rodini.contestgen;

import static org.junit.jupiter.api.Assertions.*;

import java.util.regex.Pattern;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.rodini.ballotutils.Utils;

class TestReferendumMarkers {

	private static final String testReferendumFormat = "^(?<question>((.*\n).*Referendum.*\n))(?<text>(.*\n)*?)^YES$\nNO$";
	private static Pattern testReferendumPattern;
	
	@BeforeEach
	void setUp() throws Exception {
		testReferendumPattern = Utils.compileRegex(testReferendumFormat);
	}

	@AfterEach
	void tearDown() throws Exception {
	}

	@Test
	void testLoadFromResources() {
	    ContestGen.COUNTY = "chester";
		ReferendumMarkers.initialize("./src/test/java/Chester-General-2023.properties");
		Pattern pattern = ReferendumMarkers.getReferendumPattern();
		String testPatRegex = testReferendumPattern.toString();
		String patRegex = pattern.toString();
		assertEquals(testPatRegex, patRegex);
	}

}
