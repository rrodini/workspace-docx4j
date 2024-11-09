package com.rodini.contestgen;

import static org.junit.jupiter.api.Assertions.*;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.Logger;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class TestUniqueBallotReport {

	private static MockedAppender mockedAppender;
	private static Logger logger;
	MuniReferendums muniReferendums1;
	MuniRetentions muniRetentions1;	
	MuniReferendums muniReferendums2;
	MuniRetentions muniRetentions2;	
	ReferendumExtractor rfe;
	RetentionExtractor rte;
	String muniName1 = "479 PHOENIXVILLE W-3";
	String muniName2 = "480 PHOENIXVILLE W-3"; // Muni names must differ
	
	String ballotPage1Text =
"""
Justice of the Supreme Court
Vote for ONE
Daniel McCaffery
Democratic
Carolyn Carluccio
Republican
Write-in
Judge of the Superior Court
Vote for no more than TWO
Jill Beck
Democratic
Timika Lane
Democratic
Maria Battista
Republican
Harry F. Smail Jr.
Republican
Write-in
Write-in
Judge of the Commonwealth Court
Vote for ONE
Matt Wolf
Democratic
Megan Martin
Republican
Write-in
Judge of the Court of Common Pleas
Vote for no more than FIVE
Sarah B. Black
Democratic
Deb Ryan
Democratic
Fredda D. Maddox
Democratic
Nicole Forzato
Democratic
Thomas McCabe
Democratic
Lou Mincarelli
Republican
PJ Redmond
Republican
Andy Rongaus
Republican
Don Kohler
Republican
Dave Black
Republican
Write-in
Write-in
Write-in
Write-in
Write-in
County Commissioner
Vote for no more than TWO
Josh Maxwell
Democratic
Marian Moskowitz
Democratic
David C. Sommers
Republican
Eric Roe
Republican
Write-in
Write-in
District Attorney
Vote for ONE
Christopher de Barrena-Sarobe
Democratic
Ryan L. Hyde
Republican
Write-in
Sheriff
Vote for ONE
Kevin Dykes
Democratic
Roy Kofroth
Republican
Write-in
Prothonotary
Vote for ONE
Debbie Bookman
Democratic
Michael Taylor
Republican
Write-in
Register of Wills
Vote for ONE
Michele Vaughn
Democratic
Terri Clark
Republican
Write-in
Recorder of Deeds
Vote for ONE
Diane O'Dwyer
Democratic
Brian D. Yanoviak
Republican
Write-in
""";
	String ballotPage2Text =   // referendum question and retention questions
"""
School Director At Large
Phoenixville Area
Vote for no more than FIVE
Stephanie Allen
Democratic
Sean Halloran
Democratic
Graham Perry
Democratic
Frank Venezia
Democratic
Daniel S. Wiser
Democratic
Megan Valencia
Republican
Emily Shanley
Republican
David Golberg
Republican
KurtSiso
Republican
Prince Denson
Republican
Write-in
Write-in
Write-in
Write-in
Write-in
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
Superior Court Retention
Election Question
Shall Victor P. Stabile be retained for
an additional term as Judge of the
Superior Court of the Commonwealth
of Pennsylvania?
YES
NO
Court of Common Pleas Retention
Election Question
Shall Patrick Carmody be retained
for an additional term as Judge of the
Court of Common Pleas, 15th Judicial
District, Chester County?
YES
NO
Court of Common Pleas Retention
Election Question
Shall John L. Hall be retained for an
additional term as Judge of the Court
of Common Pleas, 15th Judicial
District, Chester County?
YES
NO
""";	

	String ballotPage3Text =   // just retention questions
"""
School Director At Large
Phoenixville Area
Vote for no more than FIVE
Stephanie Allen
Democratic
Sean Halloran
Democratic
Graham Perry
Democratic
Frank Venezia
Democratic
Daniel S. Wiser
Democratic
Megan Valencia
Republican
Emily Shanley
Republican
David Golberg
Republican
KurtSiso
Republican
Prince Denson
Republican
Write-in
Write-in
Write-in
Write-in
Write-in
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
Superior Court Retention
Election Question
Shall Victor P. Stabile be retained for
an additional term as Judge of the
Superior Court of the Commonwealth
of Pennsylvania?
YES
NO
Court of Common Pleas Retention
Election Question
Shall Patrick Carmody be retained
for an additional term as Judge of the
Court of Common Pleas, 15th Judicial
District, Chester County?
YES
NO
Court of Common Pleas Retention
Election Question
Shall John L. Hall be retained for an
additional term as Judge of the Court
of Common Pleas, 15th Judicial
District, Chester County?
YES
NO
""";	

	@BeforeAll
	static void setupClass() {
	    mockedAppender = new MockedAppender();
	    mockedAppender.start();
	    logger = (Logger)LogManager.getLogger(MuniContestsQuestionsExtractor.class);
	    logger.addAppender(mockedAppender);
	    logger.setLevel(Level.ERROR);
	    ContestGen.COUNTY = "chester";
		ContestNameMarkers.initialize("./src/test/java/Chester-General-2023.properties");
		ReferendumMarkers.initialize("./src/test/java/Chester-General-2023.properties");
		RetentionMarkers.initialize("./src/test/java/Chester-General-2023.properties");
		// Don't forget this guy.
		ContestGen.WRITE_IN = "Write-in\n";		
	}

	
	@BeforeEach
	void setUp() throws Exception {
		muniReferendums1 = new MuniReferendums(muniName1);
		muniRetentions1  = new MuniRetentions(muniName1);
		muniReferendums2 = new MuniReferendums(muniName2);
		muniRetentions2  = new MuniRetentions(muniName2);
	}

	@AfterEach
	void tearDown() throws Exception {
	}

	@Test
	void testExtract() {
		List<MuniContestsQuestionsExtractor> mcqeList = new ArrayList<>();
		// Perform 2 extracts - one for each precinct
		// Precinct 1 extract
		MuniContestsQuestionsExtractor mcqe1 = new MuniContestsQuestionsExtractor(muniName1, ballotPage1Text, ballotPage2Text);
		mcqe1.extract();
		assertEquals(muniName1, mcqe1.getMuniName());
		MuniContestNames cns = mcqe1.getMuniContestNames(); // 12 + PAGE_BREAK
		assertEquals(13, cns.get().size());
//		for (ContestName cn: cns.get()) {
//			System.out.printf("Contest name: %s%n", cn.getName());
//		}
		MuniReferendums refs = mcqe1.getMuniReferendums();  //  1
		assertEquals( 1, refs.get().size());
		MuniRetentions  rets = mcqe1.getMuniRetentions();   //  4
		assertEquals( 4, rets.get().size());
		mcqeList.add(mcqe1);
		// Precinct 2 extract
		MuniContestsQuestionsExtractor mcqe2 = new MuniContestsQuestionsExtractor(muniName2, ballotPage1Text, ballotPage3Text);
		mcqe2.extract();
		assertEquals(muniName2, mcqe2.getMuniName());
		cns = mcqe2.getMuniContestNames(); // 12 + PAGE_BREAK
		assertEquals(13, cns.get().size());
//		for (ContestName cn: cns.get()) {
//			System.out.printf("Contest name: %s%n", cn.getName());
//		}
		refs = mcqe2.getMuniReferendums();  //  0
		assertEquals( 0, refs.get().size());
		rets = mcqe2.getMuniRetentions();   //  4
		assertEquals( 4, rets.get().size());
		mcqeList.add(mcqe2);
		// Now generated the ballot report.
        StringWriter stringWriter = new StringWriter();
        PrintWriter printWriter = new PrintWriter(stringWriter);
        ContestGen.genBallotReport(printWriter, mcqeList);
        printWriter.close(); // Important to close the PrintWriter
        String result = stringWriter.toString();
        int ballotCountIndex = result.indexOf("Unique ballot count: ");
        // Report should have a line like below.
        // Unique ballot count: 2
        String strCount = result.substring(ballotCountIndex+21, ballotCountIndex+22);
 //       System.out.println(result);
//        Ballot Summary
//        Precinct count: 2
//        Unique ballot count: 2
//        Precincts with identical ballots:
//        Ballot  0: 480 PHOENIXVILLE W-3
//        Ballot  1: 479 PHOENIXVILLE W-3    
        assertEquals("2", strCount);
 	}

}
