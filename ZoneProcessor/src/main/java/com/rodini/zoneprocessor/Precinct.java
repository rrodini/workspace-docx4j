package com.rodini.zoneprocessor;
/**
 * Precinct class stores information regarding a precinct (id, name, precinct id).
 * Note: There are currently no use cases for Precinct objects.
 * 
 * @author Bob Rodini
 *
 */
public class Precinct {
	
	private final String precinctNo;  // normalized to 3 digits
	private final String precinctName;
	private final String zoneNo;	  // normalized to 2 digits
	
	// Constructor
	public Precinct(String precinctNo, String precinctName, String zoneNo) {
		this.precinctNo = precinctNo;
		this.precinctName = precinctName;
		this.zoneNo = zoneNo;
	}
	
	public String getPrecinctNo() {
		return precinctNo;
	}
	
	public String getPrecinctName() {
		return precinctName;
	}
	
	public String getZoneNo() {
		return zoneNo;
	}
	
	
}
