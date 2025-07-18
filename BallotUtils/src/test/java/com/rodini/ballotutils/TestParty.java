package com.rodini.ballotutils;

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

class TestParty {

	@Test
	void testToString() {

		for (Party p : Party.values()) {
			switch (p) {
			case DEMOCRATIC:
				assertEquals("Democratic", p.toString());
				break;
			case REPUBLICAN:
				assertEquals("Republican", p.toString());
				break;
			case INDEPENDENT:
				assertEquals("Independent", p.toString());
				break;
			case DEM_REP:
				assertEquals("Democratic/Republican", p.toString());
				break;
			case REP_DEM:
				assertEquals("Republican/Democratic", p.toString());
				break;
			case LIBERTARIAN:
				assertEquals("Libertarian", p.toString());
				break;
			case NOAFFILIATION:
				assertEquals("No Affiliation", p.toString());
				break;
			case LEADERSHIPFORALL:
				assertEquals("Leadership for All", p.toString());
				break;
			case GREEN_PARTY:
				assertEquals("Green Party", p.toString());
				break;
			case KEYSTONE:
				assertEquals("Keystone", p.toString());
				break;
			}
		}
	}
	@Test
	void testToEnum() {
		List<String> names = List.of("Democratic", "Republican", "Independent", "Democratic/Republican",
				"Republican/Democratic","Libertarian","No Affiliation","Leadership for All","Green Party",
				"Keystone");
		for (String name : names) {
			switch (name) {
			case "Democratic":
				assertEquals(Party.DEMOCRATIC, Party.toEnum(name));
				break;
			case "Republican":
				assertEquals(Party.REPUBLICAN, Party.toEnum(name));
				break;
			case "Independent":
				assertEquals(Party.INDEPENDENT, Party.toEnum(name));
				break;
			case "Democratic/Republican":
				assertEquals(Party.DEM_REP, Party.toEnum(name));
				break;
			case "Republican/Democratic":
				assertEquals(Party.REP_DEM, Party.toEnum(name));
				break;
			case "Libertarian":
				assertEquals(Party.LIBERTARIAN, Party.toEnum(name));
				break;
			case "Leadership for All":
				assertEquals(Party.LEADERSHIPFORALL, Party.toEnum(name));
				break;
			case "No Affiliation":
				assertEquals(Party.NOAFFILIATION, Party.toEnum(name));
				break;
			case "Green Party":
				assertEquals(Party.GREEN_PARTY, Party.toEnum(name));
				break;
			case "Keystone":
				assertEquals(Party.KEYSTONE, Party.toEnum(name));
				break;
			}
		}
	}
	@Test
	void testToEnumBadInput() {
		String name = "Nonexistent";
		assertTrue(null == Party.toEnum(name));
	}

}
