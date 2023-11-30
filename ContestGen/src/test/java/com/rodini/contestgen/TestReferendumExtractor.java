package com.rodini.contestgen;

import static org.junit.jupiter.api.Assertions.*;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.Logger;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class TestReferendumExtractor {

	private static MockedAppender mockedAppender;
	private static Logger logger;
	
	MuniReferendums muniReferendums;
	ReferendumExtractor rfe;
	String muniName = "479 PHOENIXVILLE W-3";
	// No referendums
	String ballotText0 =
"""
Member of Council
Phoenixville W Ward
Vote for ONE
Member of Council
Phoenixville W Ward
Vote for ONE
Brian C. Moore
Democratic
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
	// Two referendums
	String ballotText2 =
"""
Member of Council
Phoenixville W Ward
Vote for ONE
Member of Council
Phoenixville W Ward
Vote for ONE
Brian C. Moore
Democratic
Write-in
Phoenixville Area School District
Occupational Tax Referendum
Do you favor eliminating the
Phoenixville Area School District
Occupation tax, effective July 1,
2024, which would require increasing
the earned income tax rate from 0.5%
to a maximum of 0.6%, beginning
January 1, 2025?
YES
NO
Honey Brook Township Board of
Supervisors Referendum
Do you favor the addition of two
supervisors to serve Honey Brook
Township on the Board of
Supervisors to ensure the residents
of Honey Brook Township are more
adequately represented?
YES
NO
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
	// One referendum
	String ballotText1 =
"""
Member of Council
Phoenixville W Ward
Vote for ONE
Member of Council
Phoenixville W Ward
Vote for ONE
Brian C. Moore
Democratic
Write-in
Phoenixville Area School District
Occupational Tax Referendum
Do you favor eliminating the
Phoenixville Area School District
Occupation tax, effective July 1,
2024, which would require increasing
the earned income tax rate from 0.5%
to a maximum of 0.6%, beginning
January 1, 2025?
YES
NO
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
	    logger = (Logger)LogManager.getLogger(ReferendumExtractor.class);
	    logger.addAppender(mockedAppender);
	    logger.setLevel(Level.ERROR);
	    ContestGen.COUNTY = "chester";
		ReferendumMarkers.initialize("./src/test/java/Chester-General-2023.properties");
	}

	
	@BeforeEach
	void setUp() throws Exception {
		muniReferendums = new MuniReferendums(muniName);
	}

	@AfterEach
	void tearDown() throws Exception {
	}
	@Test
	void testExtractNone() {
		rfe = new ReferendumExtractor(muniReferendums, muniName);
		rfe.match(ballotText0);
		assertEquals(0, muniReferendums.get().size());
	}
	@Test
	void testExtractOne() {
		rfe = new ReferendumExtractor(muniReferendums, muniName);
		rfe.match(ballotText1);
		assertEquals(1, muniReferendums.get().size());
	}
	@Test
	void testExtractTwo() {
		rfe = new ReferendumExtractor(muniReferendums, muniName);
		rfe.match(ballotText2);
		assertEquals(2, muniReferendums.get().size());
	}
}
