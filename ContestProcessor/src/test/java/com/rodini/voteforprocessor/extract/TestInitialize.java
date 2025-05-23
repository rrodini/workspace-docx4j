package com.rodini.voteforprocessor.extract;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Properties;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.Logger;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.rodini.ballotutils.Utils;
import com.rodini.voteforprocessor.extract.Initialize;

class TestInitialize {

	private static MockedAppender mockedAppender;
	private static Logger logger;

	@BeforeAll
	static void setUpClass() {
	    mockedAppender = new MockedAppender();
	    mockedAppender.start();
	    logger = (Logger)LogManager.getLogger(Initialize.class);
	    logger.addAppender(mockedAppender);
	}

	@AfterAll
	static void tearDownClass() {
		logger.removeAppender(mockedAppender);
		mockedAppender.stop();
	}

	@BeforeEach
	void setUp() throws Exception {
	    mockedAppender.messages.clear();
	    logger.setLevel(Level.ERROR);
	}

	@AfterEach
	void tearDown() throws Exception {
	}

	@Test
	void testStart1() {
		Properties props = Utils.loadProperties("./src/test/java/contestgen_2024_Primary_Dems.properties");
		// Logging INFO messages for examination.
	    logger.setLevel(Level.INFO);
		Initialize.start(props);
		// Should be 8 INFO messages
		assertEquals(8, mockedAppender.messages.size());
	}
	@Test
	void testStart2() {
		// Same as above, but now logging ERRORS.
		Properties props = Utils.loadProperties("./src/test/java/contestgen_2024_Primary_Dems.properties");
		Initialize.start(props);
		// Should be 0 ERROR messages
		assertEquals(0, mockedAppender.messages.size());
	}
// TBD - more unit test for errors in properties file...
	
	
}
