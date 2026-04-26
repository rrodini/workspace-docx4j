package com.rodini.specimenextractor;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class TestSpecimenSize {

	@BeforeEach
	void setUp() throws Exception {
	}

	@AfterEach
	void tearDown() throws Exception {
	}

	@Test
	void testSizeGood() {
		String specimenPath = "./src/test/java/ATGLEN.pdf";
		Initialize.validatePDFSize(specimenPath);
		assertEquals(Initialize.EXPECTED_PDF_PAGE_WIDTH, Initialize.specimenPageWidth);
		assertEquals(Initialize.EXPECTED_PDF_PAGE_HEIGHT, Initialize.specimenPageHeight);
	}

	
	
}
