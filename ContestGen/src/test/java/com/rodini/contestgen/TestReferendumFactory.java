package com.rodini.contestgen;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Disabled;

class TestReferendumFactory {
	String ref1Question = "Honey Brook Township Board of\nSupervisors Referendum";
	String ref1Text = 
			"""
			Do you favor the addition of two
			supervisors to serve Honey Brook
			Township on the Board of
			Supervisors to ensure the residents
			of Honey Brook Township are more
			adequately represented?		
			""";
	String ref2Question = "Phoenixville Area School District\nOccupational Tax Referendum";
	String ref2Text =
			"""
			Do you favor eliminating the
			Phoenixville Area School District
			Occupation tax, effective July 1,
			2024, which would require increasing
			the earned income tax rate from 0.5%
			to a maximum of 0.6%, beginning
			January 1, 2025?			
			""";
	@BeforeEach
	void setUp() throws Exception {
	}

	@AfterEach
	void tearDown() throws Exception {
		ReferendumFactory.clearReferendums();
	}
	@Test
	void testCreateUniqueReferendums() {
		Referendum ref1 = ReferendumFactory.create(ref1Question,ref1Text, "301");
		Referendum ref2 = ReferendumFactory.create(ref2Question,ref2Text, "467");
		assertTrue(ref1 != ref2);
		assertEquals(2, ReferendumFactory.getReferendums().size());
	}
	@Test
	void testCreateDuplicateReferendums() {
		Referendum ref1 = ReferendumFactory.create(ref2Question,ref2Text, "467");
		Referendum ref2 = ReferendumFactory.create(ref2Question,ref2Text, "468");
		assertTrue(ref1 == ref2);
		assertEquals(1, ReferendumFactory.getReferendums().size());
	}
}
