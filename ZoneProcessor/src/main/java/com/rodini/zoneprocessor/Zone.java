package com.rodini.zoneprocessor;

/**
 * Zone class stores information regarding a zone (id, name, path to logo image).
 * 
 * @author Bob Rodini
 *
 */
public final class Zone {

	private final String zoneNo; // normalized to 2 digits
	private final String zoneName;
	private final String zoneLogoPath;
	private final String zoneUrl;
	private final String zoneChunkPath;

	// Constructor
	public Zone(String zoneNo, String zoneName, String zoneLogoPath, String zoneUrl, String zoneChunkPath) {
		this.zoneNo = zoneNo;
		this.zoneName = zoneName;
		this.zoneLogoPath = zoneLogoPath;
		this.zoneUrl = zoneUrl;
		this.zoneChunkPath = zoneChunkPath;
	}

	public String getZoneNo() {
		return zoneNo;
	}

	public String getZoneName() {
		return zoneName;
	}

	public String getZoneLogoPath() {
		return zoneLogoPath;
	}
	
	public String getZoneUrl() {
		return zoneUrl;
	}

	public String getZoneChunkPath() {
		return zoneChunkPath;
	}
}
