package com.rodini.specimenextractor;

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.Logger;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.rodini.ballotutils.Utils;


class TestSpecimenExtractor {

	private static Logger logger;

	@BeforeAll
	static void setupClass() {
//	    logger = (Logger)LogManager.getLogger(Initialize.class);
	    logger = (Logger)LogManager.getRootLogger();
	    logger.setLevel(Level.ERROR);
	}

	
	@BeforeEach
	void setUp() throws Exception {
	}

	@AfterEach
	void tearDown() throws Exception {
	}
	
	boolean compareTexts(String expectedText, String extractedText) {
		boolean same = true;
		String [] expectedLines = expectedText.split("\n");
		String [] extractedLines = extractedText.split("\n");
		int i;
		for ( i = 0; i < expectedLines.length; i++ ) {
			
			if (!expectedLines[i].equals(extractedLines[i])) {
				System.out.printf("Expected:  %s%n", expectedLines[i]);
				System.out.printf("Extracted: %s%n", extractedLines[i]);
				System.out.printf("Lines different at #%s%n.", i);
				return false;
			}
		}
		if (i < extractedLines.length) {
			same = false;
		}
		return same;
	}
	
	@Test
	void testExtractSpecimenText_ATGLEN() throws IOException {
		// Must set layout properties BEFORE extraction.
		Properties props = Utils.loadProperties("./src/test/java/test-props-00.properties");
		Initialize.specimenLayoutPage1Rects = Initialize.validateLayoutProperties(props, Initialize.specimenLayoutPage1Props);
		Initialize.specimenLayoutPage2Rects = Initialize.validateLayoutProperties(props, Initialize.specimenLayoutPage2Props);
		String expectedText = Files.readString(Path.of("./src/test/java/ATGLEN-good-text-order.txt"));
		String extractedText = SpecimenExtractor.extractSpecimenText("./src/test/java/ATGLEN.pdf");
		
		if (!compareTexts(expectedText, extractedText)) {
			fail("Extracted text doesn't match. See line that differs.");
		}		
	}

	@Test
	void testExtractSpecimenText_W_PIKELAND() throws IOException {
		// Must set layout properties BEFORE extraction.
		Properties props = Utils.loadProperties("./src/test/java/test-props-00.properties");
		Initialize.specimenLayoutPage1Rects = Initialize.validateLayoutProperties(props, Initialize.specimenLayoutPage1Props);
		Initialize.specimenLayoutPage2Rects = Initialize.validateLayoutProperties(props, Initialize.specimenLayoutPage2Props);
		String expectedText = Files.readString(Path.of("./src/test/java/W_PIKELAND-good.txt"));
		String extractedText = SpecimenExtractor.extractSpecimenText("./src/test/java/W_PIKELAND.pdf");
		
//		System.out.println();
//		System.out.print(extractedText);		
//		System.out.println();
		
		if (!compareTexts(expectedText, extractedText)) {
			fail("Extracted text doesn't match. See line that differs.");
		}		
	}

	@Test
	void testExtractSpecimenText_2026_Primary_Dems() throws IOException {
		// Must set layout properties BEFORE extraction.
		Properties props = Utils.loadProperties("./src/test/java/test-props-00.properties");
		Initialize.specimenLayoutPage1Rects = Initialize.validateLayoutProperties(props, Initialize.specimenLayoutPage1Props);
		Initialize.specimenLayoutPage2Rects = Initialize.validateLayoutProperties(props, Initialize.specimenLayoutPage2Props);
		String expectedText = Files.readString(Path.of("./src/test/java/2026_Primary_Dems.txt"));
		String extractedText = SpecimenExtractor.extractSpecimenText("./src/test/java/2026_Primary_Dems.pdf");
		
//		System.out.println();
//		System.out.print(extractedText);		
//		System.out.println();
		
		
		if (!compareTexts(expectedText, extractedText)) {
			fail("Extracted text doesn't match. See line that differs.");
		}		
	}

	
}
