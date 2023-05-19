package com.rodini.ballotgen;

public enum EndorsementMode {
	/**
	 * There are three modes of endorsement: endorsed, unendorsed, antiendorsed.
	 * (default)
	 * 
	 * @author Bob Rodini
	 *
	 */
	ENDORSED, 		// won an endorsement convention
	UNENDORSED, 	// did not win an endorsement convention
	ANTIENDORSED;	// default - used for non-Democrats
}
