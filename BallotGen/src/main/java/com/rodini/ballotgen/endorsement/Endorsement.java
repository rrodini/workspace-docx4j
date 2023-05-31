package com.rodini.ballotgen.endorsement;

import static com.rodini.ballotgen.endorsement.EndorsementMode.UNENDORSED;
import static com.rodini.ballotgen.endorsement.EndorsementScope.ZONE;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Endorsement is a simple class. It represents a line of the endorsement CSV file.
 * a candidate.
 * 
 * @author Bob Rodini
 */
public class Endorsement {
	private static final Logger logger = LoggerFactory.getLogger(Endorsement.class);

	private final String name;	// Endorsed candidate name
	private final EndorsementMode mode;	// Mode of endorsement
	private final EndorsementScope scope;	// Scope of endorsement
	private final int zoneNo; 	// for scope == ZONE, otherwise 0
	
	public Endorsement (String name, EndorsementMode mode, EndorsementScope scope, int zoneNo) {
		if (name == null) {
			logger.error("Candidate name cannot be null");
			name = "Donald Duck";
		}
		if (mode == null) {
			logger.error("Endorsement mode cannot be null");
			mode  = UNENDORSED;
		}
		if (scope == null) {
			logger.error("Endorsement scope cannot be null");
			scope  = ZONE;
			zoneNo = 0;
		}
		if (scope == ZONE && zoneNo < 0) {
			logger.error("Zone # cannot be < 0");
			zoneNo = 0;
		}
		this.name = name;
		this.mode = mode;
		this.scope = scope;
		this.zoneNo = zoneNo;
		logger.debug(String.format("Creating Endorsement %s, %s, %s, %d", name, mode.toString(), scope.toString(), zoneNo));
	}
	// getter
	public String getName() {
		return name;
	}
	// getter
	public EndorsementMode getMode() {
		return mode;
	}
	// getter
	public EndorsementScope getScope() {
		return scope;
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
		sb.append(mode.toString() + ", ");
		if (scope == ZONE) {
			sb.append(scope.toString() + ", " + Integer.toString(zoneNo));
		} else {
			sb.append(scope.toString());
		}
		return sb.toString();
	}
}
