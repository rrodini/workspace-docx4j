package com.rodini.contestgen.model;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.rodini.voteforprocessor.model.Contest;
import com.rodini.voteforprocessor.model.Referendum;
import com.rodini.voteforprocessor.model.Retention;

class TestBallot {

	private String precinctNoName = "305_KENNETT_SQUARE_N";
	private String precinctNo = "305";
	private String precinctName= "KENNETT_SQUARE_N";;
	private String rawText =     // ballot text (see BallotExtractor)
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
	15th Judicial District
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
	Magisterial District Judge
	District 15-3-04
	Vote for ONE
	Albert M. Iacocca
	Democratic
	Peter George Mylonas
	Republican
	Write-in
	School Director
	Kennett Consolidated Region A
	Vote for no more than TWO
	Lenda Carrillo
	Democratic
	LaToya M. Myers
	Democratic
	Christopher T. Lafferty
	Republican
	Write-in
	Write-in
	Member of Council
	Kennett Square Borough
	Vote for no more than THREE
	Julie Hamilton
	Democratic
	Eric H. King
	Democratic
	Joel Sprick
	Democratic
	James P. Miller
	Republican
	Write-in
	Write-in
	Write-in	
	Borough of Kennett Square:
	Library Tax Referendum
	Do you favor increasing the Borough
	of Kennett Square's real estate tax by
	0.2000 mills, the revenue from such
	tax to be used exclusively to fund the
	operation of the Kennett Library within
	the Borough?
	YES
	NO
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
	Review
	""";
	
	private String page1Text =
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
	15th Judicial District
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
	Magisterial District Judge
	District 15-3-04
	Vote for ONE
	Albert M. Iacocca
	Democratic
	Peter George Mylonas
	Republican
	Write-in
	School Director
	Kennett Consolidated Region A
	Vote for no more than TWO
	Lenda Carrillo
	Democratic
	LaToya M. Myers
	Democratic
	Christopher T. Lafferty
	Republican
	Write-in
	Write-in
	Member of Council
	Kennett Square Borough
	Vote for no more than THREE
	Julie Hamilton
	Democratic
	Eric H. King
	Democratic
	Joel Sprick
	Democratic
	James P. Miller
	Republican
	Write-in
	Write-in
	Write-in			
	""";
	private String page2Text =
	"""
	Borough of Kennett Square:
	Library Tax Referendum
	Do you favor increasing the Borough
	of Kennett Square's real estate tax by
	0.2000 mills, the revenue from such
	tax to be used exclusively to fund the
	operation of the Kennett Library within
	the Borough?
	YES
	NO
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
	Review			
	""";
	
	private Ballot ballot;
	
	@BeforeAll
	static void setUpBeforeClass() throws Exception {
	}

	@AfterAll
	static void tearDownAfterClass() throws Exception {
	}

	@BeforeEach
	void setUp() throws Exception {
		ballot = new Ballot(precinctNoName, rawText);
	}

	@AfterEach
	void tearDown() throws Exception {
	}
// 
	@Test
	void testNewContest() {
		// ballot state after constructor call.
		assertEquals(precinctNoName, ballot.getPrecinctNoName());
		assertEquals(precinctNo, ballot.getPrecinctNo());
		assertEquals(precinctName, ballot.getPrecinctName());
		assertEquals(rawText, ballot.getRawText());
	}
	@Test
	void testAfterPageExtraction() {
		// ballot state after page1 & page2 extraction
		ballot.setPage1Text(page1Text);
		ballot.setPage2Text(page2Text);
		assertEquals(page1Text, ballot.getPage1Text());
		assertEquals(page2Text, ballot.getPage2Text());
	}
	@Test
	void testAfterVoteForExtraction() {
		// ballot state after contest, referendum, and retention extraction.
		// Not all of these extracted!
		Contest contest = new Contest("305", "KENNETT_SQUARE_N", "Justice of the Supreme Court", "", "", null, 1);
		List<Contest> contests = new ArrayList<>();
		assertEquals(0, ballot.getContests().size());
		contests.add(contest);
		ballot.setContests(contests);
		assertEquals(1, ballot.getContests().size());
		List<Referendum> referendums = new ArrayList<>();
		Referendum ref = new Referendum(precinctNo, precinctName,
				"Borough of Kennett Square:\nLibrary Tax Referendum",
				"Not important");
		assertEquals(0, ballot.getReferendums().size());
		referendums.add(ref);
		ballot.setReferendums(referendums);
		assertEquals(1, ballot.getReferendums().size());
		List<Retention> retentions = new ArrayList<>();
		Retention ret = new Retention(precinctNo, precinctName,
				"Superior Court Retention\nElection Question",
				"",
				"Jack Panella");
		assertEquals(0, ballot.getRetentions().size());
		retentions.add(ret);
		ballot.setRetentions(retentions);
		assertEquals(1, ballot.getRetentions().size());
	}
	@Test
	void testToString() {
		assertEquals("Ballot: 305_KENNETT_SQUARE_N", ballot.toString());
	}
}
