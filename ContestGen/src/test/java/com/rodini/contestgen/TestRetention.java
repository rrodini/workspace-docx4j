package com.rodini.contestgen;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class TestRetention {
	Retention ret;
	String retOffice = "Superior Court Retention\nElection Question";
	String retJudgeName = "Jack Panella";
	@BeforeEach
	void setUp() throws Exception {
		ret = new Retention(retOffice, retJudgeName);
	}

	@AfterEach
	void tearDown() throws Exception {
	}
	@Test
	void testGetRetQuestion() {
		assertEquals(retOffice,ret.getOfficeName());
	}
	@Test
	void testGetRefText() {
		assertEquals(retJudgeName, ret.getJudgeName());
	}
	@Test
	void testEquals() {
		Retention ret2 = new Retention(retOffice, retJudgeName);
		assertEquals(ret, ret2);
	}
	@Test
	void testToString() {
		String expectedValue = String.format("Retention office: %s judge: %s", retOffice, retJudgeName);
		assertEquals(expectedValue, ret.toString());
	}
}
