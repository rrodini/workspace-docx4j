package com.rodini.contestgen;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class TestReferendum {
	Referendum ref;
	String refQuestion = "Honey Brook Township Board of\nSupervisors Referendum";
	String refText = 
			"""
			Do you favor the addition of two
			supervisors to serve Honey Brook
			Township on the Board of
			Supervisors to ensure the residents
			of Honey Brook Township are more
			adequately represented?		
			""";
	
	@BeforeEach
	void setUp() throws Exception {
		ref = new Referendum(refQuestion, refText);
	}

	@AfterEach
	void tearDown() throws Exception {
	}
	@Test
	void testGetRefQuestion() {
		assertEquals(refQuestion,ref.getRefQuestion());
	}
	@Test
	void testGetRefText() {
		assertEquals(refText, ref.getRefText());
	}
	@Test
	void testEquals() {
		Referendum ref2 = new Referendum(refQuestion, refText);
		assertEquals(ref, ref2);
	}
	@Test
	void testAddMuniNo() {
		ref.addMuniNo("301");
		ref.addMuniNo("302");
		List<String> muniNoList = ref.getMuniNoList();
		assertEquals(2, muniNoList.size());
	}
	@Test
	void testToString() {
		String expectedValue = "Referendum: " + refQuestion;
		assertEquals(expectedValue, ref.toString());
	}
}
