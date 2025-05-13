package com.rodini.contestgen.common;

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

import static com.rodini.contestgen.common.ContestGenOutput.*;
class TestContestGenOutput {

	private static MockedAppender mockedAppender;
	private static Logger logger;

	@BeforeAll
	static void setUpBeforeClass() throws Exception {
	}

	@AfterAll
	static void tearDownAfterClass() throws Exception {
	}

	@BeforeEach
	void setUp() throws Exception {
	}

	@AfterEach
	void tearDown() throws Exception {
	}

	@Test
	void testEnum() {
		for (ContestGenOutput cgo: ContestGenOutput.values()) {
			switch (cgo) {
				case CONTESTS: 
					break;
				case BALLOTS:
					break;
				case BOTH:
					break;
				default:
					fail("invalid ContestGenOutput enum value: " + cgo.toString());			
			}
		}
	}
	@Test
	void testToEnum1() {
		ContestGenOutput cgo = toEnum("CONTESTS");
		assertEquals(CONTESTS, cgo);
		cgo = toEnum("contests");
		assertEquals(CONTESTS, cgo);
	}
	@Test
	void testToEnum2() {
		ContestGenOutput cgo = toEnum("BALLOTS");
		assertEquals(BALLOTS, cgo);
		cgo = toEnum("ballots");
		assertEquals(BALLOTS, cgo);
	}
	@Test
	void testToEnum3() {
		ContestGenOutput cgo = toEnum("BOTH");
		assertEquals(BOTH, cgo);
		cgo = toEnum("both");
		assertEquals(BOTH, cgo);
	}
	@Test
	void testToEnumBad() {
	    mockedAppender = new MockedAppender();
	    mockedAppender.start();
	    // ATTENTION: ERRORs are logged by the Utils class
	    // and not by the Initialize class.
	    logger = (Logger)LogManager.getLogger(ContestGenOutput.class);
	    logger.addAppender(mockedAppender);
	    logger.setLevel(Level.ERROR);
		// Mixed case is not accepted
		ContestGenOutput cgo = toEnum("Both");
		assertEquals(1, mockedAppender.messages.size());
		assertTrue(mockedAppender.messages.get(0).startsWith("can't convert property to ContestGenOutput:"));
	}
	@Test
	void testDisplay() {
		assertEquals("both", BOTH.toString());
		assertEquals("contests", CONTESTS.toString());
		assertEquals("ballots", BALLOTS.toString());
	}
}
