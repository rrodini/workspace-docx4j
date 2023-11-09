package com.rodini.ballotutils;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import java.util.Properties;
import java.util.regex.Pattern;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.Logger;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.ginsberg.junit.exit.ExpectSystemExit;

import static com.rodini.ballotutils.Utils.*;

class TestUtils {
	private static MockedAppender mockedAppender;
	private static Logger logger;

	/**
	 * For some reason mvn test will not work if this is @Before, but in eclipse it works! As a
	 * result, we use @BeforeClass.
	 */
	@BeforeAll
	static void setupClass() {
	    mockedAppender = new MockedAppender();
	    mockedAppender.start();
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
	@Test
	void testSetLoggingLevel() {
		String strLevel = System.setProperty(JVM_LOG_LEVEL, "ATTN").toUpperCase();
		setLoggingLevel(logger.getName());
		Level level = logger.getLevel();
		assertEquals(ATTN, level);
	}
	@Test
	@ExpectSystemExit
	void testLogFatalError() {
		Utils.logFatalError("A fatal error has occurred");
		assertEquals(1, mockedAppender.messages.size());
		assertTrue(mockedAppender.messages.get(0).startsWith("A fatal error has occurred"));
	}
	@Test
	void testLoggingLevelError() {
		// default level is ERROR
		// see setupClass()
		logger.trace("trace message");
		assertEquals(0, mockedAppender.messages.size());
		logger.debug("debug message");
		assertEquals(0, mockedAppender.messages.size());
		logger.info("info message");
		assertEquals(0, mockedAppender.messages.size());
		logger.warn("warn message");
		assertEquals(0, mockedAppender.messages.size());
		logger.error("error message");
		assertEquals(1, mockedAppender.messages.size());
		assertTrue(mockedAppender.messages.get(0).startsWith("error message"));
	}
	@Test
	void testLoggingLevelWarn() {
	    System.setProperty(JVM_LOG_LEVEL, "warn");
	    logger.setLevel(Level.WARN);
		logger.trace("trace message");
		assertEquals(0, mockedAppender.messages.size());
		logger.debug("debug message");
		assertEquals(0, mockedAppender.messages.size());
		logger.info("info message");
		assertEquals(0, mockedAppender.messages.size());
		logger.warn("warn message");
		assertEquals(1, mockedAppender.messages.size());
		assertTrue(mockedAppender.messages.get(0).startsWith("warn message"));
	}
	@Test
	void testLogAppMessageWithDateTime() {
		String message = "Start ballot generation app.";
		logAppMessage(logger, message, true);
		assertEquals(1, mockedAppender.messages.size());
		String logMessage = mockedAppender.messages.get(0);
System.out.println("logMessage: " + logMessage);
		assertTrue(logMessage.startsWith(message));
		// TODO: Statement below not true. Consider two digit dates!
		// The date/time value should have the same length no matter what.
		message += " Nov 7, 2023, 3:03:25 PM";
		//           Nov 9, 2023, 11:20:42 AM
		int lengthDiff = logMessage.length() - message.length();
		assertTrue(lengthDiff >= 0 && lengthDiff <= 3);
	}
	@Test
	void testLogAppMessageWithoutDateTime() {
		String message = "Start ballot generation app.";
		logAppMessage(logger, message, false);
		assertEquals(1, mockedAppender.messages.size());
		String logMessage = mockedAppender.messages.get(0);
		assertTrue(logMessage.startsWith(message));
		assertEquals(message.length(), logMessage.length());
	}
	@Test
	void testGetErrorCount() {
		String logFilePath = "./src/test/java/test-log-file.log";
		assertEquals(6, getErrorCount(logFilePath));
	}
	@Test
	void testLoadGoodPropertiesFile() {
		String propsPath = "./src/test/java/test-props1.properties";
		Properties props = Utils.loadProperties(propsPath);
		// new Properties object always returned
		assertTrue(props.size() > 0);
		assertEquals(0, mockedAppender.messages.size());
	}
	@Test
	void testLoadBadPropertiesFile() {
		String propsPath = "./src/test/java/bogus.properties";
		String expected = "cannot load properties file: " + propsPath;
		Properties props = Utils.loadProperties(propsPath);
		// new Properties object always returned
		assertTrue(props.size() == 0);
		assertTrue(mockedAppender.messages.get(0).startsWith(expected));
	}
	@Test
	void testGetGoodPropValue() {		
		String propsPath = "./src/test/java/test-props1.properties";
		Properties props = Utils.loadProperties(propsPath);
		String muniNameRegex = Utils.getPropValue(props, "muniNameRegex");
		//System.out.printf("%s: %s%n", "muniNameRegex", muniNameRegex);
		assertTrue(muniNameRegex != null);
	}
	@Test
	void testGetBadPropValue() {
	    logger.setLevel(Level.INFO);
		String propsPath = "./src/test/java/test-props1.properties";
		String expected = "property value not found for property name: bogusRegex";
		//                 property value not found for property name: bogusRegex
		Properties props = Utils.loadProperties(propsPath);
		String bogusRegex = Utils.getPropValue(props, "bogusRegex");
		assertTrue(bogusRegex == null);
		// messages.get(0) is "properties file loaded from: ./src/test/java/test-props1.properties"
		assertTrue(mockedAppender.messages.get(1).startsWith(expected));
	}
	@Test
	void testGetGoodPropOrderedValues() {
		String propsPath = "./src/test/java/test-props1.properties";
		Properties props = Utils.loadProperties(propsPath);
		List<String> propsList;
		propsList = Utils.getPropOrderedValues(props, "ballotgen.contest.format");
		assertEquals(2, propsList.size());
	}
	@Test
	void testGetBadPropOrderedValues() {
		String propsPath = "./src/test/java/test-props1.properties";
		Properties props = Utils.loadProperties(propsPath);
		String expected1 = "propery value not found for property name: " + "muniNameRegex.1";
		String expected2 = "no property names starting with: " + "muniNameRegex";
		List<String> propsList;
		propsList = Utils.getPropOrderedValues(props, "muniNameRegex");
		// No longer an error.
//		assertEquals(0, propsList.size());
//		System.out.printf("%s%n", mockedAppender.messages.get(0));
//		assertTrue(mockedAppender.messages.get(0).startsWith(expected1));
//		assertTrue(mockedAppender.messages.get(1).startsWith(expected2));
	}
	@Test
	void textCheckFileExistsTrue() {
		boolean exists = Utils.checkFileExists("./src/test/java/gettysburg.txt");
		assertTrue(exists);
	}
	@Test
	void textCheckFileExistsFalse() {
		boolean exists = Utils.checkFileExists("./src/test/java/non-existant.txt");
		assertFalse(exists);
	}
	@Test
	void textCheckDirExistsTrue() {
		boolean exists = Utils.checkDirExists("./src/test/java/DirExists");
		assertTrue(exists);
	}
	@Test
	void textCheckDirExistsFalse() {
		boolean exists = Utils.checkDirExists("./src/test/java/non-existant.txt");
		assertFalse(exists);
		exists = Utils.checkDirExists("./src/test/java/gettysburg.txt");
		assertFalse(exists);
	}
	@Test
	void testReadTextFile() {
		String expected = 
				"Four score and seven years ago our fathers brought forth\n" +
				"on this continent a new nation, conceived in liberty,\n" +
				"and dedicated to the proposition that all men are created equal.\n" +
				"Now we are engaged in a great civil war, testing whether that nation, \n" +
				"or any nation so conceived and so dedicated, can long endure.";
		String text = Utils.readTextFile("./src/test/java/gettysburg.txt");
		assertEquals(expected, text);
	}
	@Test
	@ExpectSystemExit
	void testReadTextFileException() {
		Utils.readTextFile("./non-existant.txt");
		assertTrue( true);
	}
	@Test
	void testCompileRegex1() {
		String goodRegex = "\\d/\\d/\\dddd";  // birthdate
		Pattern pattern = Utils.compileRegex(goodRegex);
		assertTrue( pattern != null);
	}
	@Test
	@ExpectSystemExit
	void testCompileRegex2() {
		String badRegex = "\\x/\\x/\\xxxx";
		assertThrows(
				Exception.class,
				() -> { Utils.compileRegex(badRegex);});
	}
	@Test
	void testEnvVariable1() {
		// expect HOME variable to always exist
		assertTrue(null != Utils.getEnvVariable("HOME", true));
	}
	@Test
	@ExpectSystemExit
	void testEnvVariable2() {
		// expect DOES_NOT_EXIST variable to never exist
		Utils.getEnvVariable("DOES_NOT_EXIST", true);
	}
	@Test
	void testEnvVariable3() {
		// don't expect OPTIONAL variable to always exist
		assertTrue(null== Utils.getEnvVariable("OPTIONAL", false));
	}
	@Test
	void testNormalizeMuniNoGood() {
		String expected = "047";
		String normalized = normalizeMuniNo(47);
		assertEquals(expected, normalized);
	}
	@Test
	@ExpectSystemExit
	void testNormalizeMuniNoBad() {
		String normalized = normalizeMuniNo(-1);
		assertEquals(1, mockedAppender.messages.size());
		assertTrue(mockedAppender.messages.get(0).startsWith("can't normalize #:"));
	}
	@Test
	void testNormalizeZoneNoGood() {
		String expected = "01";
		String normalized = normalizeZoneNo(1);
		assertEquals(expected, normalized);
	}
	@Test
	@ExpectSystemExit
	void testNormalizeMZoneNoBad() {
		String normalized = normalizeZoneNo(100);
		assertEquals(1, mockedAppender.messages.size());
		assertTrue(mockedAppender.messages.get(0).startsWith("can't normalize #:"));
	}

}
