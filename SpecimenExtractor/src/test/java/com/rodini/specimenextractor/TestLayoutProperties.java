package com.rodini.specimenextractor;

import static com.rodini.specimenextractor.Initialize.PROP_PAGE1_COL2_RECT;
import static com.rodini.specimenextractor.Initialize.specimenLayoutPage1Props;
import static com.rodini.specimenextractor.Initialize.specimenLayoutPage2Props;
import static com.rodini.specimenextractor.Initialize.validateLayoutProperties;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.awt.Rectangle;
import java.util.Map;
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

class TestLayoutProperties {

	private static MockedAppender mockedAppender;
	private static Logger logger;

	@BeforeAll
	static void setupClass() {
	    mockedAppender = new MockedAppender();
	    mockedAppender.start();
	    logger = (Logger)LogManager.getLogger(Initialize.class);
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
	void testValidateLayoutPropertiesGood() {
		String propsFile = "./src/test/java/test-props-00.properties";
		Properties testProps = Utils.loadProperties(propsFile);
		Map<String, Rectangle> layoutPage1Rects;
		Map<String, Rectangle> layoutPage2Rects;
		layoutPage1Rects = validateLayoutProperties(testProps, specimenLayoutPage1Props);
		layoutPage2Rects = validateLayoutProperties(testProps, specimenLayoutPage2Props);
		int errors = mockedAppender.messages.size();
		assertEquals(0, errors, String.format("Should be no errors with good layout properties, but had %d errors", errors));
//		System.out.print(layoutRects);
		// just do this for Page 1 Column 2
		Rectangle rect = layoutPage1Rects.get(PROP_PAGE1_COL2_RECT);
		assertEquals(216, rect.getX(), String.format("Expected x=216, but got %f", rect.getX()));
		assertEquals(261, rect.getY(), String.format("Expected y=261, but got %f", rect.getY()));
		assertEquals(180, rect.getWidth(),  String.format("Expected width= 180, but got %f", rect.getWidth()));
		assertEquals(705, rect.getHeight(), String.format("Expected height=705, but got %f", rect.getHeight()));
	}
	@Test
	void testValidateLayoutPropertiesBad01() {
		String propsFile = "./src/test/java/test-props-01.properties";
		Properties testProps = Utils.loadProperties(propsFile);
		Map<String, Rectangle> layoutRects;
		layoutRects = validateLayoutProperties(testProps, specimenLayoutPage1Props);
		int errors = mockedAppender.messages.size();
		assertEquals(1, errors, String.format("Should have 1 error, but had %d errors", errors));
	}
	// more bad properties tests to come...
	
	
	
}
