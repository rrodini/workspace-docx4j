package com.rodini.contestgen;

import static org.junit.jupiter.api.Assertions.*;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.Logger;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class TestRetentionExtractor {

	private static MockedAppender mockedAppender;
	private static Logger logger;
	MuniRetentions muniRetentions;	
	RetentionExtractor rte;
	String muniName = "479 PHOENIXVILLE W-3";
	// No retentions
	String ballotText0 =
"""
Magisterial District Judge
District 15-3-06
Vote for ONE
Joseph Hutton
Democratic
Tim Arndt
Republican
Write-in
School Director
Twin Valley Region 3
Vote for no more than TWO
William Wray
Democratic
Nick DiGiacomo
Democratic/Republican
John Burdy
Republican
Write-in
""";
	// Two retentions
	String ballotText2 =
"""
Magisterial District Judge
District 15-3-06
Vote for ONE
Joseph Hutton
Democratic
Tim Arndt
Republican
Write-in
School Director
Twin Valley Region 3
Vote for no more than TWO
William Wray
Democratic
Nick DiGiacomo
Democratic/Republican
John Burdy
Republican
Write-in
OFFICIAL JUDICIAL RETENTION
QUESTIONS INSTRUCTIONS TO
VOTER
To vote in FAVOR of the retention,
blacken the oval ( •) to the left of the
word YES.
To vote AGAINST the retention,
blacken the oval ( •) to the left of the
word NO.
VOTE ON EACH OF THE
FOLLOWING JUDICIAL RETENTION
QUESTIONS
Superior Court Retention
Election Question
Shall Jack Panella be retained for an
additional term as Judge of the
Superior Court of the Commonwealth
of Pennsylvania?
YES
NO
Superior Court Retention
Election Question
Shall Victor P. Stabile be retained for
an additional term as Judge of the
Superior Court of the Commonwealth
of Pennsylvania?
YES
NO
""";
	// One retention
	String ballotText1 =
"""
Magisterial District Judge
District 15-3-06
Vote for ONE
Joseph Hutton
Democratic
Tim Arndt
Republican
Write-in
School Director
Twin Valley Region 3
Vote for no more than TWO
William Wray
Democratic
Nick DiGiacomo
Democratic/Republican
John Burdy
Republican
Write-in
OFFICIAL JUDICIAL RETENTION
QUESTIONS INSTRUCTIONS TO
VOTER
To vote in FAVOR of the retention,
blacken the oval ( •) to the left of the
word YES.
To vote AGAINST the retention,
blacken the oval ( •) to the left of the
word NO.
VOTE ON EACH OF THE
FOLLOWING JUDICIAL RETENTION
QUESTIONS
Superior Court Retention
Election Question
Shall Jack Panella be retained for an
additional term as Judge of the
Superior Court of the Commonwealth
of Pennsylvania?
YES
NO
""";
	
	@BeforeAll
	static void setupClass() {
	    mockedAppender = new MockedAppender();
	    mockedAppender.start();
	    logger = (Logger)LogManager.getLogger(RetentionExtractor.class);
	    logger.addAppender(mockedAppender);
	    logger.setLevel(Level.ERROR);
	    ContestGen.COUNTY = "chester";
		RetentionMarkers.initialize("./src/test/java/Chester-General-2023.properties");
	}
	@BeforeEach
	void setUp() throws Exception {
		muniRetentions = new MuniRetentions(muniName);
	}

	@AfterEach
	void tearDown() throws Exception {
	}

	@Test
	void testExtractNone() {
		rte = new RetentionExtractor(muniRetentions);
		rte.match(ballotText0);
		assertEquals(0, muniRetentions.get().size());
	}
	@Test
	void testExtractOne() {
		rte = new RetentionExtractor(muniRetentions);
		rte.match(ballotText1);
		assertEquals(1, muniRetentions.get().size());
	}
	@Test
	void testExtractTwo() {
		rte = new RetentionExtractor(muniRetentions);
		rte.match(ballotText2);
		assertEquals(2, muniRetentions.get().size());
	}
}
