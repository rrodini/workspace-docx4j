package com.rodini.contestgen;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

import org.junit.jupiter.api.Test;

import com.rodini.contestgen.model.Ballot;
import com.rodini.voteforprocessor.model.Contest;

class TestGenBallots {

	@Test
	void test1() {
		Ballot ballot = GenerateTestBallots.genBallot1();
//		System.out.printf("Malvern contests:%n");
//		for (Contest contest: ballot.getContests()) {
//			System.out.printf("  Contest: %s%n", contest.getName());
//		}
//		System.out.printf("Referendum count %d%n", ballot.getReferendums().size());
//		System.out.printf("Retention count: %d%n", ballot.getRetentions().size());
		assertEquals(12, ballot.getContests().size());
		assertEquals( 0, ballot.getReferendums().size());
		assertEquals( 0, ballot.getRetentions().size());
	}
	@Test
	void test2() {	
		List <Ballot> ballots = GenerateTestBallots.genBallot2();
//		ballots.stream().forEach(b -> 
//			System.out.printf("%s%n", b));
		assertEquals(2, ballots.size());
		assertEquals("350_MALVERN", ballots.get(0).getPrecinctNoName());
		assertEquals("065_CHARLESTOWN", ballots.get(1).getPrecinctNoName());
	}
	@Test
	void test3() {
		Ballot ballot = GenerateTestBallots.genBallot3();
//		System.out.printf("New Garden contests:%n");
//		for (Contest contest: ballot.getContests()) {
//			System.out.printf("  Contest: %s%n", contest.getName());
//		}
//		System.out.printf("Referendum count %d%n", ballot.getReferendums().size());
//		System.out.printf("Retention count: %d%n", ballot.getRetentions().size());
		assertEquals( 8, ballot.getContests().size());
		assertEquals( 1, ballot.getReferendums().size());
		assertEquals( 0, ballot.getRetentions().size());
	}
	@Test
	void test4() {	
		List <Ballot> ballots = GenerateTestBallots.genBallot4();
//		ballots.stream().forEach(b -> 
//			System.out.printf("%s%n", b));
		assertEquals(2, ballots.size());
		assertEquals("385_NEW_GARDEN", ballots.get(0).getPrecinctNoName());
		assertEquals("653_UWCHLAN", ballots.get(1).getPrecinctNoName());
	}
}
