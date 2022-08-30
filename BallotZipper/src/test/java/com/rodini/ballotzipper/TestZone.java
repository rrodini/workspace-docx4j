package com.rodini.ballotzipper;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class TestZone {

	@BeforeEach
	void setUp() throws Exception {
	}

	@AfterEach
	void tearDown() throws Exception {
	}

	@Test
	void testConstlructor() {
		Zone zone = new Zone("02", "Generic zone");
		assertEquals("02", zone.getZoneNo());
		assertEquals("Generic zone", zone.getZoneName());
		assertEquals(0, zone.getZoneBallotFiles().size());
	}
	@Test
	void testZoneWithFiles() {
		Zone zone = new Zone("03", "Generic zone");
		List<String> files = List.of("014_Birmingham_1_VS.txt", "014_Birmingham_1_VS.pdf", "014_Birmingham_1.docx");
		MuniFiles muniFiles = new MuniFiles(files);
		zone.addFiles(muniFiles);
		List<MuniFiles> zoneFiles = zone.getZoneBallotFiles();
		assertEquals(1, zoneFiles.size());
	}
	
}
