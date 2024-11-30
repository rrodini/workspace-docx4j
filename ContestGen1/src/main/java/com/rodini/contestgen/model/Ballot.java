package com.rodini.contestgen.model;

import java.util.ArrayList;
import java.util.List;

import com.rodini.ballotutils.Utils;

/** 
 * Ballot represents a precinct-level ballot. There is not much logic here as
 * most of the work is done by the extractors.
 * 
 * @author Bob Rodini
 *
 */
public class Ballot {
	private String precinctNoName; // see BallotExtractor
	private String precinctNo;
	private String precinctName;
	private String rawText;     // ballot text (see BallotExtractor)
	private String page1Text;	// page1 ballot text (see PageExtractor)
	private String page2Text;	// page2 ballot text (see PageExtractor)
	private List<Contest> contests;
	private List<Referendum> referendums;
	private List<Retention> retentions;
	/**
	 * Constructor.
	 * 
	 * @param precinctNoName name (key) for ballot
	 * @param rawText extracted text from specimen.
	 */
	public Ballot(String precinctNoName, String rawText) {
		this.precinctNoName = precinctNoName;
		this.rawText = rawText;
		this.precinctNo = Utils.getPrecinctNo(precinctNoName);
		this.precinctName = Utils.getPrecinctName(precinctNoName);
		// remaining fields filled by further extractions,
		// but provide defaults.
		this.page1Text = "";
		this.page2Text = "";
		contests = new ArrayList<>();
		referendums = new ArrayList<>();
		retentions = new ArrayList<>();
	}
	/**
	 * get the precinct number and name.
	 * @return precinctNoName
	 */
	public String getPrecinctNoName() {
		return precinctNoName;
	}
	/**
	 * get the precinct number.
	 * @return precinctNo
	 */
	public String getPrecinctNo() {
		return precinctNo;
	}
	/**
	 * get the precinct name.
	 * @return precinctName
	 */
	public String getPrecinctName() {
		return precinctName;
	}
	/** 
	 * getRawText returns refined text of the ballot.
	 * @return refined text.
	 */
	public String getRawText() {
		return rawText;
	}
	/** 
	 * getPage1Text returns page1 text of the ballot.
	 * @return page1 text.
	 */
	public String getPage1Text() {
		return page1Text;
	}
	/**
	 * setPage1Text sets page 1 text as a result of extraction.
	 * @param page1Text page1 text.
	 */
	public void setPage1Text(String page1Text) {
		this.page1Text = page1Text;
	}
	/** 
	 * getPage1Text returns page2 text of the ballot.
	 * @return page2 text.
	 */
	public String getPage2Text() {
		return page2Text;
	}
	/**
	 * setPage2Text sets page 2 text as a result of extraction.
	 * @param page2Text page2 text.
	 */
	public void setPage2Text(String page2Text) {
		this.page2Text = page2Text;
	}
	
	public List<Contest> getContests() {
		return contests;
	}
	
	public void setContests(List<Contest> contests) {
		this.contests = contests;
	}

	public List<Referendum> getReferendums() {
		return referendums;
	}
	
	public void setReferendums(List<Referendum> referendums) {
		this.referendums = referendums;
	}
	
	public List<Retention> getRetentions() {
		return retentions;
	}
	
	public void setRetentions(List<Retention> retentions) {
		this.retentions = retentions;
	}

	@Override
	// TODO: Consider enhancing with additional field information.
	public String toString() {
		return "Ballot: " + precinctNoName;
	}
}
