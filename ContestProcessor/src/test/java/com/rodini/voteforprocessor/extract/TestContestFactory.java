package com.rodini.voteforprocessor.extract;

import static java.util.stream.Collectors.joining;
import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Pattern;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.Logger;
import org.apache.logging.log4j.core.appender.AbstractAppender;

import com.rodini.ballotutils.ElectionType;
import com.rodini.ballotutils.Party;
//import com.rodini.ballotgen.common.Initialize;
//import com.rodini.ballotgen.common.MockedAppender;
//import com.rodini.ballotgen.contest.Contest;
//import com.rodini.ballotgen.contest.ContestFactory;
//import com.rodini.ballotgen.contest.ContestNameCounter;
import com.rodini.ballotutils.Utils;
import com.rodini.voteforprocessor.extract.ContestFactory;
import com.rodini.voteforprocessor.extract.Initialize;
import com.rodini.voteforprocessor.model.Contest;

// TODO: add more test cases
class TestContestFactory {
	
	ContestFactory cf;
	String ballotTextGeneral =
		"Justice of the Supreme Court\n" +
		"Vote for ONE\n" +
		"Maria McLaughlin\n" +
		"Democratic\n" +
		"Kevin Brobson\n" +
		"Republican\n" +
		"Write-in\n" +
		"Judge of the Superior Court\n" +
		"Vote for ONE\n" +
		"Timika Lane\n" +
		"Democratic\n" +
		"Megan Sullivan\n" +
		"Republican\n" +
		"Write-in\n" +
		"Judge of the Commonwealth Court\n" +
		"Vote for no more than TWO\n" +
		"Lori A. Dumas\n" +
		"Democratic\n" +
		"David Lee Spurgeon\n" +
		"Democratic\n" +
		"Stacy Marie Wallace\n" +
		"Republican\n" +
		"Drew Crompton\n" +
		"Republican\n" +
		"Write-in\n" +
		"Write-in\n" +
		// use format 1 here
		"Judge of the\n" +
		"Court of Common Pleas\n" +
		"Vote for no more than TWO\n" +
		"Alita Rovito\n" +
		"Democratic\n" +
		"Tony Verwey\n" +
		"Democratic\n" +
		"Lou Mincarelli\n" +
		"Republican\n" +
		"PJ Redmond\n" +
		"Republican\n" +
		"Write-in\n" +
		"Write-in\n" +
		"Treasurer\n" +
		"4 Year Term\n" +
		"Vote for ONE\n" +
		"Patricia A. Maisano\n" +
		"Democratic\n" +
		"Jennifer Nicolas\n" +
		"Republican\n" +
		"Write-in";
	String ballotTextBad =  // Missing "Write-in"
		"Justice of the Supreme Court\n" +
		"Vote for ONE\n" +
		"Maria McLaughlin\n" +
		"Democratic\n" +
		"Kevin Brobson\n" +
		"Republican\n" +
		"Judge of the Superior Court\n" +
		"Vote for ONE\n" +
		"Timika Lane\n" +
		"Democratic\n" +
		"Megan Sullivan\n" +
		"Republican\n" +
		"Judge of the Commonwealth Court\n";
	String ballotTextPrimary =
		"DEMOCRATIC STATE COMMITTEE\n" +
		"Vote for not more than EIGHT\n" +
		"Electing FOUR Females and FOUR Males\n" +
		"BILL SCOTT\n" +
		"Male - West Chester Borough\n" +
		"ALEX TEPLYAKOV\n" +
		"Male - Schuylkill\n" +
		"LISA LONGO\n" +
		"Female - Phoenixville\n" +
		"CHRIS PIELLI\n" +
		"Male - West Goshen Township\n" +
		"BARBARA E COOPER\n" +
		"Female - West Goshen Township\n" +
		"DIANE O'DWYER\n" +
		"Female - Uwchlan Township\n" +
		"ELVA BANKINS BAXTER\n" +
		"Female - Tredyffrin Township\n" +
		"JOHN HELLMANN III\n" +
		"Male - West Goshen\n" +
		"DENNIS MCANDREWS\n" +
		"Male - Tredyffrin\n" +
		"CHRISTOPHER KOWERDOVICH\n" +
		"Male - East Brandywine\n" +
		"KEVIN HOUGHTON\n" +
		"Male - West Bradford\n" +
		"DAVE MCLIMANS\n" +
		"Male - Sadsbury Township\n" +
		"STEPHANNIE MCLIMANS\n" +
		"Female - Sadsbury Township\n" +
		"LANI FRANK\n" +
		"Female - Willistown Township\n" +
		"DEBRA A SULENSKI\n" +
		"Female - Warwick\n" +
		"MICHELE VAUGHN\n" +
		"Female - East Whiteland Township\n" +
		"MARY R LASOTA\n" +
		"Female - West Goshen\n" +
		"HANS VAN MOL\n" +
		"Male - Tredyffrin\n" +
		"Write-in\n" +
		"Write-in\n" +
		"Write-in\n" +
		"Write-in\n" +
		"Write-in\n" +
		"Write-in\n" +
		"Write-in\n" +
		"Write-in\n" +
		"MEMBER OF DEMOCRATIC COUNTY\n" +
		"COMMITTEE\n" +
		"Vote for not more than TWO\n" +
		"THERESA M SCHATZ\n" +
		"SAMANTHA JOUIN\n" +
		"Write-in\n";
	
	String contestFormat1 = "/^%contest name%\\n(?<instructions>.*)\\n(?<candidates>((.*\\n){1})*)^Write-in$/";
	String contestFormat2 = "^%contest name%\\n(?<term>^(\\d Year |Unexpired ).*)\\n(?<instructions>^Vote.*)\\n(?<candidates>((.*\\n){1})*?)^Write-in$";
	// Below is needed for primary ballot, specifically CCDC.
	String contestFormat3 = "/^%contest name%\\n(?<instructions>.*)\\n(?<candidates>((.*\\n){1})*?)^Write-in$/";
	//String formatsText = contestFormat1 + "\n" + contestFormat2;
	String formatsText = 
		"""
		^%contest name%\\n(?<instructions>^Vote.*)\\n(?<candidates>((.*\\n){1})*?)^Write-in$
		^%contest name%\\n(?<term>^(\\d Year |Unexpired ).*)\\n(?<instructions>^Vote.*)\\n(?<candidates>((.*\\n){1})*?)^Write-in$
		^%contest name%\\n(?<region>^Region [A-Z].*)\\n(?<term>^(\\d |Unexpired ).*)\\n(?<instructions>^Vote.*)\\n(?<candidates>((.*\\n){1})*?)^Write-in$
		^%contest name%\\n(?<instructions>^Vote(.*\\n)*?Vote for ONE)\\n(?<candidates>((.*\\n){1})*?)^Write-in$
		""";
			
	
	private static MockedAppender mockedAppender;
	private static Logger logger;

	@BeforeAll
	static void setupClass() {
	    mockedAppender = new MockedAppender();
	    mockedAppender.start();
	    logger = (Logger)LogManager.getLogger(ContestFactory.class);
	    logger.addAppender(mockedAppender);
	    logger.setLevel(Level.ERROR);
	}

	@AfterAll
	public static void teardown() {
		logger.removeAppender(mockedAppender);
		mockedAppender.stop();
	}
	
	/**
	 * Bias is toward General election since this was the first implementation.
	 * @throws Exception
	 */
	@BeforeEach
	void setUp() throws Exception {
	    mockedAppender.messages.clear();
		Initialize.contestGenProps = Utils.loadProperties("src/test/java/contestgen.props");

		Initialize.COUNTY = "chester";
		Initialize.WRITE_IN = "Write-in";
	}

	@AfterEach
	void tearDown() throws Exception {
	}


}
