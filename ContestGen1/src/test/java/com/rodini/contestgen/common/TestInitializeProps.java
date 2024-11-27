package com.rodini.contestgen.common;

import static org.junit.jupiter.api.Assertions.*;

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
import org.junit.jupiter.api.Disabled;

import com.rodini.ballotutils.Utils;
import com.rodini.contestgen.ContestGen1;

class TestInitializeProps {

	private static MockedAppender mockedAppender;
	private static Logger logger;

	@BeforeAll
	static void setupClass() {
	    mockedAppender = new MockedAppender();
	    mockedAppender.start();
	    // ATTENTION: These ERRORs are logged by the Initialize class.
	    logger = (Logger)LogManager.getLogger(Initialize.class);
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
	@Test
	void testValidateIntPropertyGood() {
		Properties props = Utils.loadProperties("./src/test/java/Bad-Properties.properties");
		int val = Initialize.validateIntProperty(props, "int.prop.good", 0);
		int expected = 3;
		assertEquals(expected, val);
	}
	@Test
	void testValidateIntPropertyMissing() {
		Properties props = Utils.loadProperties("./src/test/java/Bad-Properties.properties");
		int val = Initialize.validateIntProperty(props, "int.prop.missing", 7);
		int expected = 7;
		assertEquals(expected, val);
		assertEquals(1, mockedAppender.messages.size());
		assertTrue(mockedAppender.messages.get(0).startsWith("property int.prop.missing is missing."));
	}
	@Test
	void testValidateIntPropertyBad() {
		Properties props = Utils.loadProperties("./src/test/java/Bad-Properties.properties");
		int val = Initialize.validateIntProperty(props, "int.prop.bad", 7);
		int expected = 7;
		assertEquals(expected, val);
		assertEquals(1, mockedAppender.messages.size());
		assertTrue(mockedAppender.messages.get(0).startsWith("property int.prop.bad is not an integer. See: 1a2"));
	}
	@Test
	void testValidateRegexPropertyGood() {
		Properties props = Utils.loadProperties("./src/test/java/Bad-Properties.properties");
		Pattern patGood = Pattern.compile("\\d");
		Pattern patGoodProperty = Initialize.validateRegexProperty(props, "regex.prop.good");
		assertEquals(patGood.toString(), patGoodProperty.toString());
	}
	@Test
	void testValidateRegexPropertyMissing() {
		Properties props = Utils.loadProperties("./src/test/java/Bad-Properties.properties");
		Pattern patMissingProperty = Initialize.validateRegexProperty(props, "regex.prop.missing");
		assertEquals(null, patMissingProperty);
		assertEquals(1, mockedAppender.messages.size());
		assertTrue(mockedAppender.messages.get(0).startsWith("property regex.prop.missing is missing."));
	}
	@Test
	@Disabled // Implement when System.exit() issue is fixed.
	void testValidateRegexPropertyBad() {		
	}
	@Test
	void testValidateOrderedRegexProperties() {
		Properties props = Utils.loadProperties("./src/test/java/Bad-Properties.properties");
		Pattern [] patterns = Initialize.validateOrderedRegexProperties(props, "contest.format");
		assertEquals(3, patterns.length);
	}

}
