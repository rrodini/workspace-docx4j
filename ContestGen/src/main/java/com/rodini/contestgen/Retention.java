package com.rodini.contestgen;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
/**
 * Retention is a simple container for a retention object.
 * Note: This is a candidate for a Java 'record'.
 * @author Bob Rodini
 *
 */
public class Retention {
	private static final Logger logger = LogManager.getLogger(Retention.class);
	private String officeName;
	private String judgeName; 

	public Retention(String officeName, String judgeName) {
		this.officeName = officeName;
		this.judgeName = judgeName;
	}
	public String getOfficeName() {
		return officeName;
	}
	public String getJudgeName() {
		return judgeName;
	}
	/**
	 * Just compare the judge name for uniqueness.
	 */
	public boolean equals(Object o) {
		boolean equal = false;
		if (o instanceof Retention) {
			equal = judgeName.equals(((Retention) o).judgeName);
		}
		return equal;
	}
	
	public int hashCode() {
		return judgeName.hashCode();
	}
	
	public String toString() {
		return String.format("Retention office: %s judge: %s", officeName, judgeName);
	}
}
