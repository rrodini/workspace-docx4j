package com.rodini.contestgen.common;

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
//import org.junit.jupiter.api.extension.ExtendWith;
//import com.ginsberg.junit.exit.agent.AgentSystemExitHandlerStrategy;
import com.ginsberg.junit.exit.ExpectSystemExit;
//import com.ginsberg.junit.exit.FailOnSystemExit;
//import com.ginsberg.junit.exit.SystemExitExtension;

import com.rodini.contestgen.ContestGen1;
import com.rodini.ballotutils.Utils;
//@ExtendWith(SystemExitExtension.class)
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
	    ContestGen1.COUNTY = "chester";
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
//	@Test
//	@Disabled
//	@ExpectSystemExit
//	void testInitializeArgsCount() {
//		String [] args1 = {
//				"bogus"
//		};
//		String expected = "missing command line arguments";
//		Initialize.validateCommandLineArgs(args1);
//		assertEquals(1, mockedAppender.messages.size());
//		assertTrue(mockedAppender.messages.get(0).startsWith(expected));
//	}
	@Test
//	@Disabled
	@ExpectSystemExit
	void testInitializeArg0IsBad1() {
System.out.println("testInitializeArg0IsBad1");
		String [] args = {
				"./non-existent.txt",		
				"./src/test/java/contests",
				"./src/test/java/ballots"
		};
		String expected = "file";
		Initialize.validateCommandLineArgs(args);
		assertEquals(1, mockedAppender.messages.size());
		assertTrue(mockedAppender.messages.get(0).startsWith(expected));
	}
	@Test
//	@Disabled
	@ExpectSystemExit
	void testInitializeArg0IsBad2() {
System.out.println("testInitializeArg0IsBad2");
		String [] args = {
				"./src/test/java/Test.xyz",
				"./src/test/java/contests",
				"./src/test/java/ballots"
		};
		String expected = "file";
		Initialize.validateCommandLineArgs(args);
//		assertEquals(1, mockedAppender.messages.size());
System.out.println("*******");
for (int i =0; i < mockedAppender.messages.size(); i++ ) {
	System.out.println(mockedAppender.messages.get(0));
}
System.out.println("*******");	
		assertTrue(mockedAppender.messages.get(0).startsWith(expected));
	}
	@Test
//	@Disabled
	void testInitializeArg1IsBad1() {
System.out.println("testInitializeArg1IsBad1");
		String [] args = {
				"./src/test/java/Chester-General-2021.txt",
				"./non-existent-folder",
				"./src/test/java/ballots"
		};
		String expected = "directory";
		Initialize.validateCommandLineArgs(args);
//		assertEquals(1, mockedAppender.messages.size());
System.out.println("*******");
for (int i =0; i < mockedAppender.messages.size(); i++ ) {
	System.out.println(mockedAppender.messages.get(i));
}
System.out.println("*******");	
		assertTrue(mockedAppender.messages.get(0).startsWith(expected));
	}
	@Test
//	@Disabled
	void testInitializeArg2IsBad1() {
System.out.println("testInitializeArg2IsBad1");
		String [] args = {
				"./src/test/java/Chester-General-2021.txt",
				"./src/test/java/contests",
				"./non-existent-folder",
		};
		String expected = "directory";
		Initialize.validateCommandLineArgs(args);
//		assertEquals(1, mockedAppender.messages.size());
System.out.println("*******");	
for (int i =0; i < mockedAppender.messages.size(); i++ ) {
	System.out.println(mockedAppender.messages.get(i));
}
System.out.println("*******");	
		assertTrue(mockedAppender.messages.get(0).startsWith(expected));
	}
	@Test
	void testInitializeArgsGood() {
		String [] args = {
				"./src/test/java/Chester-General-2021.txt",
				"./src/test/java/contests",
				"./src/test/java/ballots"
		};
		Initialize.validateCommandLineArgs(args);
	}
	


}
