package com.rodini.ballotzipper;

import static org.junit.jupiter.api.Assertions.*;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.Logger;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.rodini.ballotutils.Utils;
import com.rodini.zoneprocessor.ZoneProcessor;
import com.rodini.zoneprocessor.ZoneFactory;
/**
/**
 * Uses: test-dir-01.csv   test-dir-01
 *       test-dir-02.csv   test-dir-02
 *       test-dir-03.csv   test-dir-03
 * 
 * @author Bob Rodini
 *
 */
class TestGenZipFiles {

	private static MockedAppender mockedAppender;
	private static Logger logger;

	@BeforeAll
	static void setupClass() {
	    mockedAppender = new MockedAppender();
	    mockedAppender.start();
	    logger = (Logger)LogManager.getLogger(GenZipFiles.class);
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
		ZoneFactory.clearZones();
		ZoneProcessor.clearMuniNoMap();
		GenDocxMap.clearDocxNoMap();
	}

	@AfterEach
	void tearDown() throws Exception {
	}

	@Test
	void testDir01() {
		String [] args = {"./src/test/java/test-dir-01.csv", "./src/test/java/test-dir-01", "./src/test/java/test-dir-01"};
		Initialize.initialize(args);
		String cvsText = Utils.readTextFile("./src/test/java/test-dir-01.csv");
		ZoneProcessor.processCSVText(cvsText);
		GenDocxMap.processInDir();
		GenZipFiles.genZips();
	}
	@Test
	void testDir02() {
		String [] args = {"./src/test/java/test-dir-02.csv", "./src/test/java/test-dir-02", "./src/test/java/test-dir-02"};
		Initialize.initialize(args);
		String cvsText = Utils.readTextFile("./src/test/java/test-dir-02.csv");
		ZoneProcessor.processCSVText(cvsText);
		GenDocxMap.processInDir();
		GenZipFiles.genZips();
		// ERROR (GenMuniMap) - duplicate precinct no. 010
		// ERROR (GenZipFiles) - DOCX precinct 014 lacks CSV precinct
//		assertTrue(mockedAppender.messages.contains("duplicate precinct no. 010"));
		assertTrue(mockedAppender.messages.contains("DOCX precinct 014 lacks CSV precinct"));
	}
	@Test
	void testDir03() {
		String [] args = {"./src/test/java/test-dir-03.csv", "./src/test/java/test-dir-03", "./src/test/java/test-dir-03"};
		Initialize.initialize(args);
		String cvsText = Utils.readTextFile("./src/test/java/test-dir-03.csv");
		ZoneProcessor.processCSVText(cvsText);
		GenDocxMap.processInDir();
		GenZipFiles.genZips();
		// ERROR (GenZipFiles) - CSV precinct 020 lacks DOCX files
		// ERROR (GenZipFiles) - DOCX precinct 004 lacks CSV precinct
		assertTrue(mockedAppender.messages.contains("CSV precinct 020 lacks DOCX files"));
		assertTrue(mockedAppender.messages.contains("DOCX precinct 004 lacks CSV precinct"));
	}
}
