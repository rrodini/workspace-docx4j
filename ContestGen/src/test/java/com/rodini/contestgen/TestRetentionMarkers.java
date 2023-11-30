package com.rodini.contestgen;

import static org.junit.jupiter.api.Assertions.*;

import java.util.regex.Pattern;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.rodini.ballotutils.Utils;

class TestRetentionMarkers {

	private static final String testRetentionQuestionFormat =
			"^(?<question>(?<office>.*Retention\nElection Question$)((.*)\n)*?^YES\nNO)";
	private static final String testRetentionNameFormat =
			"((.*)\n)*^Shall (?<name>(.*)?) be retained.*\n((.*)\n)*^YES\nNO";
	private static Pattern testRetentionQuestionPattern;
	private static Pattern testRetentionNamePattern;
	
	@BeforeEach
	void setUp() throws Exception {
		testRetentionQuestionPattern = Utils.compileRegex(testRetentionQuestionFormat);
		testRetentionNamePattern = Utils.compileRegex(testRetentionNameFormat);
	}

	@AfterEach
	void tearDown() throws Exception {
	}

	@Test
	void testLoadFromResources() {
	    ContestGen.COUNTY = "chester";
		RetentionMarkers.initialize("./src/test/java/Chester-General-2023.properties");
		Pattern pattern = RetentionMarkers.getRetQuestionPattern();
		String testPatRegex = testRetentionQuestionPattern.toString();
		String patRegex = pattern.toString();
		assertEquals(testPatRegex, patRegex);
		pattern = RetentionMarkers.getRetNamePattern();
		testPatRegex = testRetentionNamePattern.toString();
		patRegex = pattern.toString();
		assertEquals(testPatRegex, patRegex);
	}

}
