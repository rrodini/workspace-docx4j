package com.rodini.contestgen;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class TestMuniContestNames {

	MuniContestNames mcn;
	
	@BeforeEach
	void setUp() throws Exception {
		mcn = new MuniContestNames("Municipality1");
	}

	@AfterEach
	void tearDown() throws Exception {
	}

	@Test
	void testConstructor() {
		//setUp has created an object.
		assertEquals("Municipality1", mcn.getMuniName());
		assertEquals(0, mcn.get().size());
	}
	
	@Test
	void testContests2() {
		mcn.add(new ContestName("Justice of the Supreme Court", 1));
		mcn.add(new ContestName("Judge of the\nCourt of Common Pleas", 1));
		List<ContestName> cnList = mcn.get();
		assertEquals(2, cnList.size());
		assertEquals("Justice of the Supreme Court", cnList.get(0).getName());
		assertEquals(1, cnList.get(0).getFormat());
	}
	
	@Test
	void testIntersect() {
		mcn.add(new ContestName("Justice of the Supreme Court", 1));
		mcn.add(new ContestName("Judge of the\nCourt of Common Pleas", 1));
		mcn.add(new ContestName("Supreme Being", 1));
		MuniContestNames mcn1 = new MuniContestNames("Municipality2");
		mcn1.add(new ContestName("Justice of the Supreme Court", 1));
		mcn1.add(new ContestName("Judge of the\nCourt of Common Pleas", 1));
		mcn1.add(new ContestName("Non-Supreme Being", 1));
		MuniContestNames common = mcn.intersect(mcn1);
		List<ContestName> commonList = common.get();
		assertEquals(2, commonList.size());
		assertEquals("Justice of the Supreme Court", commonList.get(0).getName());
		assertEquals("Judge of the\nCourt of Common Pleas", commonList.get(1).getName());
	}

}
