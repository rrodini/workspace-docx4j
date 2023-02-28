package com.rodini.ballotgen;
/**
 * There are three kinds of endorsement:
 * State, County (CCDC), Zone.
 * 
 * Note: Assumption is that State dominates County, County dominates Zone.
 * This accounts for the order shown below.
 * 
 * @author Bob Rodini
 *
 */
public enum EndorsementType {
	ZONE,	// 0
	COUNTY,	// 1
	STATE;	// 2
}
