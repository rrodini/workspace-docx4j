package com.rodini.zoneprocessor;


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
	// Constructor
	public Zone(String zoneNo, String zoneName) {
		this.zoneNo = zoneNo;
		this.zoneName = zoneName;
	}
 	public String getZoneNo() {
		return zoneNo;
	}
	public String getZoneName() {
		return zoneName;
	}
}
