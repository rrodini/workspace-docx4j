package com.rodini.ballotgen.writein;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** 
 * Writein class represents a line from the write-in CSV file.
 * 
 * @author Bob Rodini
 *
 */
public class Writein {
	private static final Logger logger = LoggerFactory.getLogger(Writein.class);
	
	private final String candidateName;
	private final String contestName;
	private final int zoneNo; 	// zone # that endorses the candidate
	private final List<String> muniNos; // precincts with contest on ballot
	/**
	 * constructor assumes that all validation was performed by WriteinFactory.
	 * @param candName
	 * @param contestName
	 * @param zoneNo
	 * @param muniNos
	 */
	public Writein(String candName, String contestName, int zoneNo, List<String> muniNos) {
		this.candidateName = candName;
		this.contestName = contestName;
		this.zoneNo = zoneNo;
		this.muniNos = muniNos;
		int size = muniNos.size();
		logger.debug(String.format("Creating Writein %s, %s, %d, %s...%s", candName, contestName, zoneNo, muniNos.get(0), muniNos.get(size-1)));
	}
	// getter
	String getCandidateName() {
		return candidateName;
	}
	// getter
	String getContestName() {
		return contestName;
	}
	// getter
	int getZoneNo() {
		return zoneNo;
	}
	// getter
	List<String> getMuniNos() {
		return muniNos;
	}
	@Override
	public String toString(){
		StringBuffer sb = new StringBuffer();
		sb.append("Writein: ");
		sb.append(candidateName + ", ");
		sb.append(contestName + ", ");
		sb.append(Integer.toString(zoneNo) + ", ");
		sb.append(muniNos.get(0) + ",...,");
		int size = muniNos.size();		
		sb.append(muniNos.get(size-1));
		return sb.toString();
	}	
}
