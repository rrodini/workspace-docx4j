package com.rodini.ballotzipper;

import java.util.ArrayList;
import java.util.List;

/**
 * Zone class stores information regarding a zone.  Some of the data doesn't change
 * (e.g. no, name) and some is dynamic (ballotList).
 * 
 * Actually, anything can change over time.
 * 
 * @author Bob Rodini
 *
 */
public class Zone {
	
	private String zoneNo;  // normalized to 2 digits
	private String zoneName;
	private List<MuniFiles> zoneBallotFiles;
	// Constructor
	public Zone(String zoneNo, String zoneName) {
		this.zoneNo = zoneNo;
		this.zoneName = zoneName;
		this.zoneBallotFiles = new ArrayList<>();
	}
 	String getZoneNo() {
		return zoneNo;
	}
	String getZoneName() {
		return zoneName;
	}
	// Add these files to the zone's ownership.
	void addFiles(MuniFiles files) {
		zoneBallotFiles.add(files);
	}
	// Get the list of files for zipping.
	List<MuniFiles> getZoneBallotFiles() {
		return zoneBallotFiles;
	}
}
