package com.rodini.ballotgen.contest;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.rodini.ballotgen.common.ElectionType;
import com.rodini.ballotgen.common.Initialize;
import com.rodini.ballotgen.common.Party;

/**
 * ContestFactory has methods that find ballot text relating
 * to a named contest.  Once the text is isolated, it can be
 * parsed into Candidate and Contest objects.
 * 
 * Note: This class is highly sensitive to changes in the 
 * spelling and format of the ballot text from voter services.
 * 
 * @author Bob Rodini
 *
 */
public class ContestFactory {
	
	private static final Logger logger = LogManager.getLogger(ContestFactory.class);
	// CHESTER
	//private final String CONTEST_END = "Write-in";
	// BUCKS
	private final String CONTEST_END = Initialize.WRITE_IN.trim();
	private final String CONTEST_NAME = "%contest name%";
	private final ContestNameCounter ballotFactory;
	private final String ballotText;	// text of the entire ballot
	private final ElectionType elecType; // GENERAL or PRIMARY
	private final Party endorsedParty;
	// (key, entry) example:  "1", "/^%contest name%\n(?<instruction>.*)\n(?<candidates>((.*\n){2})*)^Write-in$/"
	private final Map<String, String> contestFormats;
	// contestNameIndexes handles a special case when the contest name is duplicated on a ballot
	// Example: Auditor - 4 year term, Auditor - 6 year term
	// (key, entry) example:  "Auditor", 0
	/**
	 * ContestFactory receives two key parameters for its methods.
	 * @param ballotText text of the entire ballot for municipality.
	 * @param formatsText strings of key,value pairs for parsing contest text.
	 */
	public ContestFactory(ContestNameCounter bf, String formatsText, ElectionType type, Party endorsedParty) {
		this.ballotFactory = bf;
		this.ballotText = bf.getBallotText();
		this.elecType = type;
		this.endorsedParty = endorsedParty;
		contestFormats = new HashMap<>();
		// TODO: "\n" or "\\n"  ?
		String [] formats = formatsText.split("\\n");
		// Note: trailing / removed for all except last by split().
		int index=1;
		logger.trace("contestFormats by key:");		
		for (String f: formats) {
			logger.trace(String.format("key: %d format: %s%n", index, f));
			contestFormats.put(Integer.toString(index), f);
			index++;
		}
	}
    public String getBallotText() {
		return ballotText;
	}
	public Map<String, String> getContestFormats() {
		return contestFormats;
	}
	/**
     * findContestText searches the ballotText for the start/end
     * of the text that pertains to a particular contest.
     * 
     * Notes: 
     * 1. The contest name may not pertain to this ballot.
     *    So, the contest will be skipped.
     * 2. If contest name is found it returns
     *    the string up to and including
     *    the first "Write-in" sequence.
     * 
     * @param contestName e.g. "Justice of the Supreme Court"
     * @returns text snippet (string) that pertains to the contest
     */
	public String findContestText(String contestName) {
		String contestText = "";
		int start = ballotText.indexOf(contestName, ballotFactory.getStartIndex(contestName));
		if (start == -1) {
			String msg = "can't find this contestName: " + contestName;
			logger.error(msg);
			return contestText;
		}
		int end = ballotText.indexOf(CONTEST_END, start+contestName.length());
		if (end == -1) {
			String msg = "can't find this contest end text for: " + contestName;
			logger.error(msg);
			return contestText;
		}
		contestText = ballotText.substring(start, end + CONTEST_END.length());
		ballotFactory.setEndIndex(contestName, end + CONTEST_END.length());
		return contestText;
	}
    /**
     * parseContestText takes the text "found" by findContestText and
     * parses it as per the format given to the constructor. Lots of
     * things can go wrong here, but if successful a Contest object
     * is returned.
     * @param contestName e.g. "Justice of the Supreme Court"
     * @param contestText text snippet that pertains to contest
     * @param format format (regex) to use for contest
     * @returns Contest object
     */
	public Contest parseContestText(String contestName, String contestText, String format) {
		Pattern pattern = getContestPattern(contestName, format);
		Matcher m = pattern.matcher(contestText);
		Contest contest = Contest.GENERIC_CONTEST;
		if (!m.find()) {
			String msg = String.format("no match for contest name: %s and format: %s"
										,contestName, format);
			logger.error(msg);
		} else {
			logger.info("contestName: " + contestName);
			try {
				// "term" is optional
				String term = getMatchGroup(m, "term");
				String instructions = getMatchGroup(m, "instructions");
				String candidatesText = getMatchGroup(m, "candidates");
				CandidateFactory cf = new CandidateFactory(contestName, candidatesText, elecType, endorsedParty);
				List<Candidate> candidates = cf.getCandidates();
				contest = new Contest(contestName, term, instructions, candidates);
			} catch (Exception e) {
				String msg = e.getMessage();
				logger.error(msg);
			}
		}
		if (contest == Contest.GENERIC_CONTEST) {
			String msg = String.format("COULD NOT GENERATE CONTEST FOR: %s", contestName);
			System.out.println(msg);
			logger.error(msg);
		}
		return contest;
	}
	/** 
	 * getMatchGroup attempts to get the value of the named match
	 * group.  There are some formats (regexes) that will not match.
	 * 
	 * @param m Matcher object
	 * @param groupName name for group
	 * @return value for group
	 */
	/* private */
	String getMatchGroup(Matcher m, String groupName) {
		String value = "";
		try {
			value = m.group(groupName);
		} catch (Exception e) {
			String msg = e.getMessage();
			if (msg.contains("<term>")) {
				// this is expected for some formats (regexes).
				logger.info(msg);
			} else {
				logger.error(msg);
			}
		}
		return value;
	}
    /**
     * getContestFormat gets the format associated with the contest
     * name, manipulates it a little and transforms it into a Pattern object.
     * Note: the returned Pattern object is always multi-line.
     * @param contestName e.g. "Justice of the Supreme Court"
     * @param format that identifies the regex for the ballot text.
     * @returns Pattern object for parsing.
     */
	/* private */ 
	Pattern getContestPattern(String contestName, String format) {
		String startRegex = contestFormats.get(format);
		logger.debug(String.format("format: %s regex: %s%n", format, startRegex));
		Pattern compiledRegex = null;
		if (startRegex == null) {
			String msg = "format key for contest cannot be found: " + format;
			logger.error(msg);
			return compiledRegex;
		}
		String endRegex = startRegex.replace(CONTEST_NAME, contestName);
		if (endRegex.startsWith("/")) {
			// strip off leading "/"
			endRegex = endRegex.substring(1);
		}
		if (endRegex.endsWith("/")) {
			// strip off trailing "/"
			endRegex = endRegex.substring(0, endRegex.length() - 1);
		}
		try {
			compiledRegex = Pattern.compile(endRegex, Pattern.MULTILINE);
		} catch (Exception e) {
			String msg = String.format("can't compile regex: %s msg: %s%n", endRegex , e.getMessage());
			logger.error(msg);
		}
		return compiledRegex;
	}
}
