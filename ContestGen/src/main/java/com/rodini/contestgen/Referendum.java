package com.rodini.contestgen;

import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Referendum is a simple container for a referendum object.
 * Note: This is a candidate for a Java 'record'.
 * @author Bob Rodini
 *
 */
public class Referendum {
	private static final Logger logger = LogManager.getLogger(Referendum.class);
	
	private String refQuestion;
	private String refText;
	List<String> muniNoList;

	public Referendum(String refQuestion, String refText) {
		this.refQuestion = refQuestion;
		this.refText = refText;
		muniNoList = new ArrayList<>();
	}
	public void addMuniNo(String muniNo) {
		muniNoList.add(muniNo);
	}
	public String getRefQuestion() {
		return refQuestion;
	}
	public String getRefText() {
		return refText;
	}
	public List<String> getMuniNoList() {
		return muniNoList;
	}
	/**
	 * Just compare the question text for uniqueness.
	 */
	public boolean equals(Object o) {
		boolean equal = false;
		if (o instanceof Referendum) {
			equal = refQuestion.equals( ((Referendum) o).refQuestion);
		}
		return equal;
	}
	
	public int hashCode() {
		return refQuestion.hashCode();
	}
	
	public String toString() {
		return String.format("Referendum: %s", refQuestion);
	}

}
