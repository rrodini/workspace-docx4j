package com.rodini.specimenextractor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.Logger;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Disabled;
import com.ginsberg.junit.exit.ExpectSystemExit;
import com.rodini.ballotutils.Utils;


class TestInitializeCLIArgs {

	private static MockedAppender mockedAppender;
	private static Logger logger;

	@BeforeAll
	static void setupClass() {
	    mockedAppender = new MockedAppender();
	    mockedAppender.start();
	    // ATTENTION: ERRORs are logged by the Utils class
	    // and not by the Initialize class.
	    logger = (Logger)LogManager.getLogger(Utils.class);
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
	@Disabled
	@Test
	@ExpectSystemExit
	void testInitializeArg0IsBad1 () {
		String [] args = {
		};
		String expected = "missing CLI arguments";
		Initialize.validateCommandLineArgs(args);
		assertEquals(1, mockedAppender.messages.size());
		assertTrue(mockedAppender.messages.get(0).startsWith(expected));
	}
	@Disabled
	@Test
	@ExpectSystemExit
	void testInitializeArg0IsBad2 () {
		String [] args = {
			"non-existent.pdf"
		};
		String expected = "invalid args[0] value, not a file:";
		Initialize.validateCommandLineArgs(args);
		assertEquals(1, mockedAppender.messages.size());
		assertTrue(mockedAppender.messages.get(0).startsWith(expected));
	}
	@Disabled
	@Test
	@ExpectSystemExit
	void testInitializeArg0IsBad3 () {
		String [] args = {
			".src/test/java/ATGLEN.txt"
		};
		String expected = "specimen file should end with \"pdf\"";
		Initialize.validateCommandLineArgs(args);
		assertEquals(1, mockedAppender.messages.size());
		assertTrue(mockedAppender.messages.get(0).startsWith(expected));
	}
	@Test
	@ExpectSystemExit
	void testInitializeArg0IsGood () {
		String [] args = {
			".src/test/java/ATGLEN.pdf"
		};
		Initialize.validateCommandLineArgs(args);
		assertEquals(0, mockedAppender.messages.size());
	}
}
