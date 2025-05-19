package com.rodini.contestprocessor;

import static org.junit.jupiter.api.Assertions.*;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.Logger;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class TestInitialize {

	private static MockedAppender mockedAppender;
	private static Logger logger;

	@BeforeAll
	static void setUpBeforeClass() throws Exception {
	    mockedAppender = new MockedAppender();
	    mockedAppender.start();
	    logger = (Logger)LogManager.getLogger(Initialize.class);
	    logger.addAppender(mockedAppender);
	    logger.setLevel(Level.INFO);
	}

	@AfterAll
	static void tearDownAfterClass() throws Exception {
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
	void testStart() {
		Initialize.start();
		fail("Not yet implemented");
	}

}
