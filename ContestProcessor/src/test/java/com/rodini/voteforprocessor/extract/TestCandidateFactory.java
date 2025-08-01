package com.rodini.voteforprocessor.extract;

import static com.rodini.ballotutils.ElectionType.*;
import static com.rodini.ballotutils.Party.*;
import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.Logger;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
//import org.mockito.Mock;
//import org.mockito.MockitoAnnotations.*;

import com.rodini.voteforprocessor.extract.CandidateFactory;
import com.rodini.voteforprocessor.extract.ContestFactory;
import com.rodini.voteforprocessor.extract.Initialize;
import com.rodini.voteforprocessor.model.Candidate;
import com.rodini.voteforprocessor.model.GeneralCandidate;
import com.rodini.voteforprocessor.model.PrimaryCandidate;

//import com.rodini.ballotgen.common.MockedAppender;
//import com.rodini.ballotgen.common.Initialize;
//import com.rodini.ballotgen.contest.Candidate;
//import com.rodini.ballotgen.contest.CandidateFactory;
//import com.rodini.ballotgen.contest.ContestFactory;
//import com.rodini.ballotgen.contest.GeneralCandidate;
//import com.rodini.ballotgen.contest.PrimaryCandidate;

// Primary test data taken from Primary-Dems-2021.txt
// General test data taken from General-2022.txt

class TestCandidateFactory {

	private static MockedAppender mockedAppender;
	private static Logger logger;
    private static List<String> namesOfTicketContests = List.of(
    		"GOVERNOR AND LIEUTENANT GOVERNOR",
			"PRESIDENT AND VICE-PRESIDENT");

    private static List<String> namesOfLocalContests = List.of(
		"AUDITOR",
		"CONSTABLE",
		"DEMOCRATIC COUNTY COMMISSIONER",
		"INSPECTOR OF ELECTIONS",
		"JUDGE OF ELECTIONS",
		"MAGISTERIAL DISTRICT JUDGE",
		"MAYOR",
		"MEMBER OF COUNCIL",
		"SCHOOL DIRECTOR",
		"TOWNSHIP COMMISSIONER",
		"TOWNSHIP SUPERVISOR",
		"TOWNSHIP SUPERVISOR AT LARGE",
		"TOWNSHIP DISTRICT SUPERVISOR",
		"TAX COLLECTOR",
		"DISTRICT SUPERVISOR",
		"COUNCIL",
		"SUPERVISOR",
		"BOROUGH COUNCIL");
	
	private static final List<String> namesOfLocalContestsExceptions = List.of(
		"SCHOOL DIRECTOR OCTORARA REGION 1",
		"SCHOOL DIRECTOR UNIONVILLE CHADDS FORD REGION C",
		"SCHOOL DIRECTOR TWIN VALLEY REGION 2",
		"SCHOOL DIRECTOR SPRING FORD REGION 3");


	@BeforeAll
	static void setupClass() {
	    mockedAppender = new MockedAppender();
	    mockedAppender.start();
	    logger = (Logger)LogManager.getLogger(ContestFactory.class);
	    logger.addAppender(mockedAppender);
	    logger.setLevel(Level.ERROR);
	    Initialize.namesOfTicketContests = namesOfTicketContests;
	    Initialize.namesOfLocalContests = namesOfLocalContests;
	    Initialize.namesOfLocalContestsExceptions = namesOfLocalContestsExceptions;
	}

	@AfterAll
	public static void teardown() {
		logger.removeAppender(mockedAppender);
		mockedAppender.stop();
	}

	@BeforeEach
	void setUp() throws Exception {
	}

	@AfterEach
	void tearDown() throws Exception {
	    mockedAppender.messages.clear();
	}
	@Test
	void testPrimaryCandidates01() {
		String contestName = "JUDGE OF THE SUPERIOR COURT";
		String candidatesText = "";
		CandidateFactory cf = new CandidateFactory(contestName, candidatesText, PRIMARY, DEMOCRATIC);
		List<Candidate> candidates = cf.getCandidates();
		cf.clearCandidates();
		assertEquals(0, candidates.size());
	}
	@Test
	void testPrimaryCandidates02() {
		// first line name, second line region
		String contestName = "JUDGE OF THE SUPERIOR COURT";
		String candidatesText =
			"JILL BECK\n" + 
			"ALLEGHENY COUNTY\n" +
			"TIMIKA LANE\n" +
			"PHILADELPHIA COUNTY\n" +
			"BRYAN NEFT\n" +
			"ALLEGHENY COUNTY";
		CandidateFactory cf = new CandidateFactory(contestName, candidatesText, PRIMARY, DEMOCRATIC);
		List<Candidate> candidates = cf.getCandidates();
		cf.clearCandidates();
		assertEquals(3, candidates.size());
		for (Candidate c: candidates) {
			assertEquals(PrimaryCandidate.class, c.getClass());
		}
	}
	@Test
	void testPrimaryCandidates03() {
		// each line is a name.
		String contestName = "MEMBER OF COUNCIL\nAVONDALE BOROUGH";
		String candidatesText =
			"MICHAEL W ESSMAKER\n" + 
			"JANET A WATTS" ;
		CandidateFactory cf = new CandidateFactory(contestName, candidatesText, PRIMARY, DEMOCRATIC);
		List<Candidate> candidates = cf.getCandidates();
		cf.clearCandidates();
		assertEquals(2, candidates.size());
		for (Candidate c: candidates) {
			assertEquals(PrimaryCandidate.class, c.getClass());
		}		
	}
	@Test
	void testGeneralCandidate01() {
		String contestName = "United States Senator";
		String candidatesText = "\n";
		CandidateFactory cf = new CandidateFactory(contestName, candidatesText, GENERAL, DEMOCRATIC);
		List<Candidate> candidates = cf.getCandidates();
		cf.clearCandidates();
		assertEquals(0, candidates.size());
	}
	@Test
	void testGeneralCandidate02() {		
		String contestName = "United States Senator";
		String candidatesText = 
			"John Fetterman\n" +
			"Democratic\n" +
			"Mehmet Oz\n" +
			"Republican\n" +
			"Erik Gerhardt\n" +
			"Libertarian\n" +
			"Richard L. Weiss\n" +
			"Green Party\n" +
			"Daniel Wassmer\n" +
			"Keystone";
		CandidateFactory cf = new CandidateFactory(contestName, candidatesText, GENERAL, DEMOCRATIC);
		List<Candidate> candidates = cf.getCandidates();
		cf.clearCandidates();
		assertEquals(5, candidates.size());
	}
	@Test
	void testGeneralCandidate03() {
		String contestName = "Governor and Lieutenant\nGovernor";
		String candidatesText =
				"Josh Shapiro\n" +
				"Democratic\n" +
				"Austin Davis\n" +
				"Lieutenant Governor, Democratic\n" +
				"Douglas V. Mastriano\n" +
				"Republican\n" +
				"Carrie Lewis DelRosso\n" +
				"Lieutenant Governor, Republican\n" +
				"Matt Hackenburg\n" +
				"Libertarian\n" +
				"Tim McMaster\n" +
				"Lieutenant Governor, Libertarian\n" +
				"Christina DiGiulio\n" +
				"Green Party\n" +
				"Michael Bagdes-Canning\n" +
				"Lieutenant Governor, Green Party\n" +
				"Joe Soloski\n" +
				"Keystone\n" +
				"Nicole Shultz\n" +
				"Lieutenant Governor, Keystone";
		CandidateFactory cf = new CandidateFactory(contestName, candidatesText, GENERAL, DEMOCRATIC);
		List<Candidate> candidates = cf.getCandidates();
		cf.clearCandidates();
		assertEquals(10, candidates.size());
		for (Candidate c: candidates) {
			assertEquals(GeneralCandidate.class, c.getClass());
		}
		for (int i = 0; i < candidates.size(); i++) {
			boolean bottomOfTicket = ((GeneralCandidate)candidates.get(i)) .getBottomOfTicket();
			if (i % 2 == 0) {
				assertFalse(bottomOfTicket);
			} else {
				assertTrue(bottomOfTicket);
			}
		}
	}
}
