package com.rodini.contestgen;

import static org.junit.jupiter.api.Assertions.*;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.Logger;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.ginsberg.junit.exit.ExpectSystemExit;

class TestContestGen {

	private static MockedAppender mockedAppender;
	private static Logger logger;

	@BeforeAll
	static void setupClass() {
	    mockedAppender = new MockedAppender();
	    mockedAppender.start();
	    logger = (Logger)LogManager.getLogger(ContestGen.class);
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
		ContestGen.COUNTY="chester";
	}

	@AfterEach
	void tearDown() throws Exception {
	}
	@Test
	@ExpectSystemExit
	void testInitializeArgsCount() {
		String [] args1 = {
				"bogus"
		};
		String expected = "missing command line arguments";
		ContestGen.initialize(args1);
		assertEquals(1, mockedAppender.messages.size());
		assertTrue(mockedAppender.messages.get(0).startsWith(expected));
	}
	@Test
	@ExpectSystemExit
	void testInitializeArg0IsBad1() {
		String [] args = {
				"non-existent.txt",
				"./contests"
		};
		String expected = "can't find ...";
		ContestGen.initialize(args);
		assertEquals(1, mockedAppender.messages.size());
		assertTrue(mockedAppender.messages.get(0).startsWith(expected));
	}
	@Test
	@ExpectSystemExit
	void testInitializeArg0IsBad2() {
		String [] args = {
				"./src/test/java/Test.xyz",
				"./contests"
		};
		String expected = "file ... doesn't end with TXT";
		ContestGen.initialize(args);
		assertEquals(1, mockedAppender.messages.size());
		assertTrue(mockedAppender.messages.get(0).startsWith(expected));
	}
	@Test
	@ExpectSystemExit
	void testInitializeArg1IsBad1() {
		String [] args = {
				"./src/test/java/Chester-General-2021.txt",
				"./non-existent-folder"
		};
		String expected = "command line arg[1] is not a folder";
		ContestGen.initialize(args);
		assertEquals(1, mockedAppender.messages.size());
		assertTrue(mockedAppender.messages.get(0).startsWith(expected));
	}
	@Test
	void testInitializeArgsGood() {
		String [] args = {
				"./src/test/java/Chester-General-2021.txt",
				"./contests"
		};
		String expected = "command line arg[1] is not a folder";
		ContestGen.initialize(args);
		assertTrue(ContestGen.props != null);
	}
	
}
