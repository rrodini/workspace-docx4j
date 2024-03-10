package com.rodini.textcleaner;

import static org.junit.jupiter.api.Assertions.*;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.Logger;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.rodini.textcleaner.TextCleaner;
import com.rodini.ballotutils.Utils;
import com.rodini.textcleaner.MockedAppender;

class TestPrecinctNameInsertion {

	private static MockedAppender mockedAppender;
	private static Logger logger;

	@BeforeAll
	static void setupClass() {
	}

	@AfterAll
	public static void teardown() {
	}
	@BeforeEach
	void setUp() throws Exception {
	    mockedAppender = new MockedAppender();
	    mockedAppender.start();
	    logger = (Logger)LogManager.getLogger(TextCleaner.class);
	    logger.addAppender(mockedAppender);
	    logger.setLevel(Level.ERROR);		
	}

	@AfterEach
	void tearDown() throws Exception {
		logger.removeAppender(mockedAppender);
		mockedAppender.stop();
	}

	@Test
	void testNoPrecinctNameInsertion() {
		String [] args = {"./src/test/java/2024-Dem-Primary-Precinct-1.txt"};
		String startContents = Utils.readTextFile("./src/test/java/2024-Dem-Primary-Precinct-1.txt");
		TextCleaner.initialize(args);
		TextCleaner.initProperties("./src/test/java/cleaner-no-insertion.properties");
		assertFalse(TextCleaner.insertPrecinctNames);
	}

	@Test
	void testPrecinctNameInsertionGood() {
		String [] args = {"./src/test/java/2024-Dem-Primary-Precinct-1.txt"};
		String startContents = Utils.readTextFile("./src/test/java/2024-Dem-Primary-Precinct-1.txt");
		TextCleaner.initialize(args);
		TextCleaner.initProperties("./src/test/java/cleaner-insertion.properties");
		assertTrue(TextCleaner.insertPrecinctNames);
		String endContents = TextCleaner.insertPrecinctNames(startContents);
		//System.out.println(endContents);
		String expected = "OFFICIAL DEMOCRATIC GENERAL PRIMARY BALLOT\n005 ATGLEN\n";
		assertEquals(expected, endContents.substring(0, expected.length()));
	}

	@Test
	void testPrecinctNameInsertionBad() {
		String [] args = {"./src/test/java/2024-Dem-Primary-Precinct-2.txt"};
		String startContents = Utils.readTextFile("./src/test/java/2024-Dem-Primary-Precinct-2.txt");
		TextCleaner.initialize(args);
		TextCleaner.initProperties("./src/test/java/cleaner-insertion.properties");
		assertTrue(TextCleaner.insertPrecinctNames);
		String endContents = TextCleaner.insertPrecinctNames(startContents);
		//System.out.println(endContents);
		
		for (String msg: mockedAppender.messages) {
			System.out.println(msg);
		}
		
		// expect 3 ANY messages and 2 ERROR messages.
		assertEquals(5, mockedAppender.messages.size());
		assertTrue(mockedAppender.messages.get(3).startsWith("Zero precinct names inserted."));
		assertTrue(mockedAppender.messages.get(4).startsWith("count mismatch. count: 0 precinctCount: 1"));
	}

}
