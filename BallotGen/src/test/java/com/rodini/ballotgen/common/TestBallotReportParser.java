package com.rodini.ballotgen.common;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class TestBallotReportParser {

	@BeforeEach
	void setUp() throws Exception {
	}

	@AfterEach
	void tearDown() throws Exception {
	}

	@Test
	void test() {
		BallotReportParser.parseBallotReport("./src/test/java/ballot-report-general-2024.txt");
		assertEquals(18, Initialize.uniqueFirstBallotFile.size());
		assertEquals("010_AVONDALE", Initialize.uniqueFirstBallotFile.get(0));
		assertEquals("010_AVONDALE", Initialize.uniqueFirstBallotFile.get(0));
		Set<Integer> keys = Initialize.uniqueBallotFiles.keySet();
		String expected0  = "[010_AVONDALE, 305_KENNETT_SQUARE_N, 310_KENNETT_SQUARE_S-1, 315_KENNETT_SQUARE_S-2, 320_KENNETT_TOWNSHIP_1, 325_KENNETT_TOWNSHIP_2, 328_KENNETT_TOWNSHIP_3, 329_KENNETT_TOWNSHIP_4, 355_E_MARLBOROUGH_E, 356_E_MARLBOROUGH_S_-_01, 356_E_MARLBOROUGH_S_-_02, 360_E_MARLBOROUGH_W, 530_POCOPSON]";
		String expected17 = "[035_E_BRANDYWINE_N-1, 036_E_BRANDYWINE_N-2, 038_E_BRANDYWINE_S, 040_W_BRANDYWINE_E-1, 041_W_BRANDYWINE_E-2, 043_W_BRANDYWINE_W, 054_CALN_4, 665_UPPER_UWCHLAN_1, 666_UPPER_UWCHLAN_2, 667_UPPER_UWCHLAN_3, 695_WALLACE]";
		assertEquals(expected0 , Initialize.uniqueBallotFiles.get( 0).toString());
		assertEquals(expected17, Initialize.uniqueBallotFiles.get(17).toString());
	}

}
