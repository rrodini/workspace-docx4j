package com.rodini.ballotgen;

import static com.rodini.ballotgen.EndorsementType.ZONE;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Endorser is a simple class. It represents the entity that is endorsing
 * a candidate.
 * 
 * @author Bob Rodini
 */
public class Endorsement {
	private static final Logger logger = LoggerFactory.getLogger(Endorsement.class);

	String name;	// Endorsed candidate name
	EndorsementType type;	// Level of endorsement
	int zoneNo; 	// for type == ZONE, otherwise 0
	
	public Endorsement (String name, EndorsementType type, int zoneNo) {
		logger.debug(String.format("Creating Endorsement %s, %s, %d", name, type.toString(), zoneNo));
		if (name == null) {
			logger.error("Candidate name cannot be null");
			name = "Donald Duck";
		}
		if (type == null) {
			logger.error("Endorsement type cannot be null");
			type  = ZONE;
			zoneNo = 0;
		}
		if (type == ZONE && zoneNo < 0) {
			logger.error("Zone # cannot be < 0");
			zoneNo = 0;
		}
		this.name = name;
		this.type = type;
		this.zoneNo = zoneNo;
	}
	// getter
	public String getName() {
		return name;
	}
	// getter
	public EndorsementType getType() {
		return type;
	}
	// getter
	public int getZoneNo() {
		return zoneNo;
	}
	@Override
	public String toString(){
		StringBuffer sb = new StringBuffer();
		sb.append("Endorsement: ");
		sb.append(name + ", ");
		if (type == ZONE) {
			sb.append(type.toString() + ", " + Integer.toString(zoneNo));
		} else {
			sb.append(type.toString());
		}
		return sb.toString();
	}
}
