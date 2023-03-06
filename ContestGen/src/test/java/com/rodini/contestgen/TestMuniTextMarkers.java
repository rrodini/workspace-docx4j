package com.rodini.contestgen;

import static org.junit.jupiter.api.Assertions.*;

import java.util.regex.Pattern;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class TestMuniTextMarkers {

	private static final String [] testMuniPageMarkers = {
			  "(?m)(.*?)(ADDITIONAL CONTESTS AND\nQUESTIONS.\n)(?<page>((.*)\n)*?)^PROPOSED CONSTITUTIONAL$(.*)",
			  "(?m)(.*?)(^PROPOSED CONSTITUTIONAL$\n)(?<page>((.*)\n)*)(^YES\nNO$)*"
			};

	
	@BeforeEach
	void setUp() throws Exception {
	}

	@AfterEach
	void tearDown() throws Exception {
	}

	@Test
	void testLoadFromResources() {
		MuniTextMarkers.initialize("./src/test/java/Primary-Dems-2021.properties");
		assertEquals(2, MuniTextMarkers.getPageCount());
		String regex = testMuniPageMarkers[0];
		//System.out.println(regex);
		String patternRegex = MuniTextMarkers.getPage1Pattern().toString();
		//System.out.println(patternRegex);
		assertEquals(regex, patternRegex);	
		regex = testMuniPageMarkers[1];		
		patternRegex = MuniTextMarkers.getPage2Pattern().toString();
		//System.out.println(patternRegex);
		assertEquals(regex, patternRegex);	

	}

}
