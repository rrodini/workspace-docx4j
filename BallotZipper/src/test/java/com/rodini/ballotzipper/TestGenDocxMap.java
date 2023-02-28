package com.rodini.ballotzipper;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Map;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.Logger;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.rodini.zoneprocessor.ZoneFactory;
/**
 * uses: test-dir-01
 * 
 * @author Bob Rodini
 *
 */
class TestGenDocxMap {

	private static MockedAppender mockedAppender;
	private static Logger logger;

	@BeforeAll
	static void setupClass() {
	    mockedAppender = new MockedAppender();
	    mockedAppender.start();
	    logger = (Logger)LogManager.getLogger(GenDocxMap.class);
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
		GenDocxMap.clearDocxNoMap();
	}

	@AfterEach
	void tearDown() throws Exception {
	}
	@Test
	void testGoodDir() {
		String [] args = {"./src/test/java/test-dir-01.csv", "./src/test/java/test-dir-01", "./src/test/java/test-dir-01"};
		Initialize.initialize(args);
		GenDocxMap.processInDir();
		Map<String, MuniFiles> docxNoMap = GenDocxMap.getDocxNoMap();
		assertEquals(3, docxNoMap.size());
	}

}
