package com.rodini.ballotzipper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.Logger;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class TestMuniFiles {

	private static MockedAppender mockedAppender;
	private static Logger logger;

	@BeforeAll
	static void setupClass() {
	    mockedAppender = new MockedAppender();
	    mockedAppender.start();
	    logger = (Logger)LogManager.getLogger(MuniFiles.class);
	    logger.addAppender(mockedAppender);
	    logger.setLevel(Level.ERROR);
	}

	@AfterAll
	public static void teardown() {
		logger.removeAppender(mockedAppender);
		mockedAppender.stop();
	}

	
	@BeforeEach
	void setUp() throws Exception {
	    mockedAppender.messages.clear();
	}

	@AfterEach
	void tearDown() throws Exception {
	}

	@Test
	// Happy path
	void testValidFiles() {
		//                                        0                        1                         2
		List<String> files = List.of("014_Birmingham_1_VS.txt", "014_Birmingham_1_VS.pdf", "014_Birmingham_1.docx");
		MuniFiles muniFiles = new MuniFiles(files);
		assertEquals(files.get(2), muniFiles.getDocxFile());
		assertEquals(files.get(1), muniFiles.getPdfFile());
		assertEquals(files.get(0), muniFiles.getTxtFile());
		assertEquals(0, mockedAppender.messages.size());
 	}
	@Test
	// Null file 
	void testNullFiles() {
		MuniFiles muniFiles = new MuniFiles(null);
		assertEquals(1, mockedAppender.messages.size());
		assertTrue(mockedAppender.messages.get(0).startsWith("fileNames list is null."));
	}
	@Test
	// File list too short
	void testShortFiles() {
		List<String> files = List.of("014_Birmingham_1.docx", "014_Birmingham_1_VS.pdf");
		MuniFiles muniFiles = new MuniFiles(files);
		assertEquals(files.get(0), muniFiles.getDocxFile());
		assertEquals(files.get(1), muniFiles.getPdfFile());
		assertEquals(1, mockedAppender.messages.size());
		assertTrue(mockedAppender.messages.get(0).startsWith("fileNames should have size 3 but has size 2."));
	}
	@Test
	// File list too long
	void testLongFiles() {
		List<String> files = List.of("014_Birmingham_1.docx", "014_Birmingham_1_VS.pdf", "014_Birmingham_1.docx", "014_Birmingham_1_VS.pdf");
		MuniFiles muniFiles = new MuniFiles(files);
		assertEquals(files.get(0), muniFiles.getDocxFile());
		assertEquals(files.get(1), muniFiles.getPdfFile());
		assertEquals(3, mockedAppender.messages.size());
		assertTrue(mockedAppender.messages.get(0).startsWith("fileNames should have size 3 but has size 4."));
		String error1 = mockedAppender.messages.get(1);
		assertEquals(error1, "duplicate " + ".docx" + " file: " + files.get(0));
		String error2 = mockedAppender.messages.get(2);
		assertEquals(error2, "duplicate " + ".pdf" + " file: "  + files.get(1));
	}
	@Test
	// Test toString()
	void testToString() {
		List<String> files = List.of("014_Birmingham_1_VS.txt", "014_Birmingham_1_VS.pdf", "014_Birmingham_1.docx");
		MuniFiles muniFiles = new MuniFiles(files);
		String muniFilesString = muniFiles.toString();
		String expected = "MuniFiles: 014_Birmingham_1.docx, 014_Birmingham_1_VS.pdf, 014_Birmingham_1_VS.txt";
		assertEquals(expected, muniFilesString);
	}
}
