package com.rodini.ballotgen.contest;

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

import com.rodini.ballotgen.common.ElectionType;
import com.rodini.ballotgen.common.Initialize;
import com.rodini.ballotgen.common.MockedAppender;
import com.rodini.ballotgen.common.Party;
import com.rodini.ballotgen.contest.Contest;
import com.rodini.ballotgen.contest.ContestFactory;
import com.rodini.ballotgen.contest.ContestNameCounter;
import com.rodini.ballotutils.Utils;

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
	String contestFormat2 = "^%contest name%\\n(?<term>^(\\d Year |Unexpired ).*)\\n(?<instructions>^Vote.*)\\n(?<candidates>((.*\\n){1})*)^Write-in$";
	// Below is needed for primary ballot, specifically CCDC.
	String contestFormat3 = "/^%contest name%\\n(?<instructions>.*)\\n(?<candidates>((.*\\n){1})*)^Write-in$/";
	//String formatsText = contestFormat1 + "\n" + contestFormat2;
	String formatsText;
	
	private static MockedAppender mockedAppender;
	private static Logger logger;
	private static Properties ballotGenProps;
	private static Properties contestGenProps;


	/**
	 * For some reason mvn test will not work if this is @Before, but in eclipse it works! As a
	 * result, we use @BeforeClass.
	 */
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
		Initialize.ballotGenProps = Utils.loadProperties(Initialize.RESOURCE_PATH + Initialize.PROPS_FILE);
//if (ballotGenProps == null) {
//	System.out.println("ballotGenProps == null");
//	System.exit(0);
//}
		Initialize.contestGenProps = Utils.loadProperties(Initialize.CONTESTGEN_RESOURCE_PATH + Initialize.CONTESTGEN_PROPS_FILE);
//if (contestGenProps == null) {
//	System.out.println("contestGenProps == null");
//	System.exit(0);
//}
		Initialize.COUNTY = "chester";
		Initialize.WRITE_IN = "Write-in";
		List<String> formatLines = Utils.getPropOrderedValues(Initialize.contestGenProps, Initialize.COUNTY + ".ballotgen.contest.format");
		formatsText = formatLines.stream()
				.collect(joining("\n"));
		ContestNameCounter bf = new ContestNameCounter(ballotTextGeneral);
		cf = new ContestFactory(bf, formatsText, ElectionType.GENERAL, Party.DEMOCRATIC);
	}

	@AfterEach
	void tearDown() throws Exception {
	}

	@Test
	void testContructor() {
		assertTrue(cf.getBallotText().startsWith(ballotTextGeneral.substring(0, 10)));
		assertTrue(cf.getBallotText().endsWith(ballotTextGeneral.substring(ballotTextGeneral.length()-10, ballotTextGeneral.length())));
		
		Map<String, String> contestFormats = cf.getContestFormats();
		System.out.println("contestFormats:");
		for (String k: contestFormats.keySet()) {
			System.out.printf("  %s: %s%n", k, contestFormats.get(k));
		}
		assertEquals(contestFormat2.substring(0, contestFormat2.length()), cf.getContestFormats().get("2"));
	}
	@Test
	void testFindContestText() {
		String contestName = "Judge of the\nCourt of Common Pleas";
		String expected = 
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
			"Write-in";
		String contestText = cf.findContestText(contestName);
		assertEquals(expected, contestText);
	}
	@Test
	void testFindContestText1() {
		ContestNameCounter bf = new ContestNameCounter(ballotTextPrimary);
//		Initialize.WRITE_IN = "Write-in";
		cf = new ContestFactory(bf, formatsText, ElectionType.PRIMARY, Party.DEMOCRATIC);
		String contestName = "DEMOCRATIC STATE COMMITTEE";
		String expected = 
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
			"Write-in";
		String contestText = cf.findContestText(contestName);
		assertEquals(expected, contestText);
	}

	@Test
	void testFindContestTextError1() {
		String contestName = "Nonexistent";
		String expected = "can't find this contestName: " + contestName;
		String text = cf.findContestText(contestName);
		// No longer an error
//		assertEquals(1, mockedAppender.messages.size());
//		assertEquals(expected, mockedAppender.messages.get(0));
		assertTrue(text.isEmpty());
	}
	@Test
	void testFindContestTextError2() {
		ContestNameCounter bf = new ContestNameCounter(ballotTextBad);
		cf = new ContestFactory(bf, formatsText, ElectionType.GENERAL, Party.DEMOCRATIC);
		String contestName = "Justice of the Supreme Court";
		String expected = "can't find this contest end text for: " + contestName;
		String text = cf.findContestText(contestName);
		// No longer an error
//		assertEquals(1, mockedAppender.messages.size());
//		assertEquals(expected, mockedAppender.messages.get(0));
		assertTrue(text.isEmpty());
	}
	//@Disabled
	@Test
	void testFindContestTextDynamic() {
 		List<String> ballotFileLines = null;
 		List<String> contestsLines = null;
		Initialize.COUNTY = "Chester";
		Initialize.WRITE_IN = "Write-in";
		String contestLine = "";
		String expected = 
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
			"Write-in";
		try {
			ballotFileLines = Files.readAllLines(Path.of("./src/test/java/test-ballot-text.txt"));
			contestsLines = Files.readAllLines(Path.of("./src/test/java/test-contests.txt"));
		} catch (IOException e) {
			e.printStackTrace();
		}
 		String ballotFileText = ballotFileLines.stream().collect(joining("\n"));
 		// ATTENTION: There is only one contest!
 		String contestText = contestsLines.stream().collect(joining("\n"));
 		String [] elements1 = contestText.split(",");
 		System.out.println(Arrays.toString(elements1));
 		String [] elements2 = elements1[0].split("\\\\n");
 		System.out.println(Arrays.toString(elements2));
 		// This value must be built dynamically.  Don't know why.
 		String contestName = elements2[0].concat("\n").concat(elements2[1]);
 		assertEquals(ballotTextGeneral, ballotFileText);
		ContestNameCounter bf = new ContestNameCounter(ballotFileText);
		cf = new ContestFactory(bf, formatsText, ElectionType.GENERAL, Party.DEMOCRATIC);
		contestText = cf.findContestText(contestName);
		assertEquals(expected, contestText);
	}
	@Test
	void testGetContestPattern() {
		String contestName = "Judge of the\nCourt of Common Pleas";
		String format = "2";
		String expected =
			"^Judge of the\n" +
			"Court of Common Pleas\\n" +
			"(?<term>^(\\d Year |Unexpired ).*)\\n" +
			"(?<instructions>^Vote.*)\\n" +
			"(?<candidates>((.*\\n){1})*)" +
			"^Write-in$";
		Pattern p = cf.getContestPattern(Contest.processContestName(contestName), format);
		assertEquals(expected, p.toString());
	}
	@Test
	void testGetContestPattern1() {
		String contestName = "Judge of the\nCourt of Common Pleas";
		String expected = "format key for contest cannot be found: 300";
		contestName = Contest.processContestName(contestName);
		cf.getContestPattern(contestName,"300");
		assertEquals(1, mockedAppender.messages.size());
		assertEquals(expected, mockedAppender.messages.get(0));
	}
	@Test
	void testGetContestPattern2() {
		// first section of regex should not compile.
		String formatsText = "/^@#(\\($%^\n(?<instructions>.*)\n(?<candidates>((.*\n){2})*)^Write-in$/";
		ContestNameCounter bf = new ContestNameCounter(ballotTextGeneral);
		cf = new ContestFactory(bf, formatsText, ElectionType.GENERAL, Party.DEMOCRATIC);
		String contestName = "Judge of the\nCourt of Common Pleas";
		String expected = "can't compile regex";
		cf.getContestPattern(contestName,"1");
		assertEquals(1, mockedAppender.messages.size());
		assertTrue(mockedAppender.messages.get(0).startsWith(expected));
	}

}
