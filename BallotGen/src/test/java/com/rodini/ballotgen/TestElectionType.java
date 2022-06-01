package com.rodini.ballotgen;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

import org.junit.jupiter.api.Test;
/**
 * Party should be a simple enum except for the fact
 * that the sample ballot uses string "Democratic" instead of
 * string "DEMOCRATIC". Hence the need for methods toEnum()
 * and toString().
 * @author Bob Rodini
 *
 */

class TestElectionType {

	@Test
	void testToString() {
		for (ElectionType type: ElectionType.values()) {
			switch (type) {
			case GENERAL: 
				assertEquals("General", type.toString());
				break;
			case PRIMARY:
				assertEquals("Primary", type.toString());
				break;
			}
		}
	}
	@Test
	void testToEnum() {
		List<String> names = List.of("Primary", "General");
		for (String name : names) {
			switch (name) {
			case "Primary":
				assertEquals(ElectionType.PRIMARY, ElectionType.toEnum(name));
				break;
			case "General":
				assertEquals(ElectionType.GENERAL, ElectionType.toEnum(name));
				break;
			}
		}
	}
	@Test
	void testToEnumBadInput() {
		String name = "Nonexistent";
		assertTrue(null == ElectionType.toEnum(name));
	}

}
