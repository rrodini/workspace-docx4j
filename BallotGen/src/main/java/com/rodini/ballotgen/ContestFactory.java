package com.rodini.ballotgen;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.rodini.ballotgen.ElectionType.*;

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
	
	private static final Logger logger = LoggerFactory.getLogger(ContestFactory.class);
	private final String CONTEST_END = "Write-in";
	private final String CONTEST_NAME = "%contest name%";
	private final String ballotText;	// text of the entire ballot
	private final ElectionType elecType; // GENERAL or PRIMARY
	// (key, entry) example:  "1", "/^%contest name%\n(?<instruction>.*)\n(?<candidates>((.*\n){2})*)^Write-in$/"
	private final Map<String, String> contestFormats;
	/**
	 * ContestFactory receives two key parameters for its methods.
	 * @param ballotText text of the entire ballot for municipality.
	 * @param formatsText strings of key,value pairs for parsing contest text.
	 */
	ContestFactory(String ballotText, String formatsText, ElectionType type) {
		this.ballotText = ballotText;
		this.elecType = type;
		contestFormats = new HashMap<>();
		String [] formats = formatsText.split("\\n");
		// Note: trailing / removed for all except last by split().
		int index=1;
		for (String f: formats) {
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
		int start = ballotText.indexOf(contestName);
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
		return ballotText.substring(start, end + CONTEST_END.length());
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
			String msg = String.format("no match for contest name: %s and format: %s%n"
										,contestName, format);
			logger.error(msg);
		} else {
			try {
				// "term" is optional
				String term = getMatchGroup(m, "term");
				String instructions = getMatchGroup(m, "instructions");
				String candidatesText = getMatchGroup(m, "candidates");
				List<Candidate> candidates = createCandidates(candidatesText, getNumCandidateLines(format));
				contest = new Contest(contestName, term, instructions, candidates);
			} catch (Exception e) {
				String msg = e.getMessage();
				logger.error(msg);
			}
		}
		if (contest == Contest.GENERIC_CONTEST) {
			System.out.printf("COULD NOT GENERATE CONTEST FOR: %s%n", contestName);
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
		logger.debug(String.format("match: %s groupName: %s%n", m.group(), groupName));
		String value = "";
		try {
			value = m.group(groupName);
		} catch (Exception e) {
			String msg = e.getMessage();
			if (msg.contains("<term>")) {
				// this is expected for some formats (regexs).
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
	/**
	 * getNumCandidateLines is a kludge to compensate for the fact that
	 * 95% of the time when candidates are listed the pattern is:
	 * name
	 * party / residence
	 * and 5% of the time it is"
	 * name
	 * @param format index of regex used for contest group
	 * @return 1 or 2.
	 */
	int getNumCandidateLines(String format) {
		int numLines = 0;
		String startRegex = contestFormats.get(format);
		if (startRegex.contains("{1}")) {
			numLines = 1;
		} else if (startRegex.contains("{2}")) {
			numLines = 2;
// for the future
//		} else if (startRegex.contains("{3}")) {
//			numLines = 3;
		}
		return numLines;
	}
	/** createCandidates takes lines of candidate name/party pairs
	 * and converts them to Candidate objects.
	 * @param candidatesText lines of candidate name/party pairs
	 * @return Candidate list
	 */
	/* private */ 
	List<Candidate> createCandidates(String candidatesText, int numLines) {
		// format for general:  "Name1"
		//                      "Party1"
		//                      "Name2"
		//                      "Party2"
		//                      etc.
		// format for primary:  "Name1"
		//                      "Residence1"
		//                      "Name2"
		//                      "Residence2"
		//                      etc.
		// format for CCDC:     "Name1"
		//                      "Name2"
		//                      etc.
		logger.info(String.format("candidatesText: %s", candidatesText));
		List<Candidate> candidates = new ArrayList<>();
		if (candidatesText.isBlank()) {
			return candidates;
		}
		final String [] elements = candidatesText.split("\\n");
		for (int i = 0; i < elements.length; i = i+numLines) {
			String display1 = "";
			String display2 = "";
			if (i < elements.length) {
				display1 = elements[i];
			}
			if (numLines == 2 && i + 1 < elements.length) {
				display2 = elements[i+1];
			}
			if (elecType == GENERAL) {
				candidates.add(new GeneralCandidate(display1, Party.toEnum(display2)));
			} else if (elecType == PRIMARY) {
				candidates.add(new PrimaryCandidate(display1, display2));
			} else {
				Utils.logFatalError("unknown election type.  Can't create candidate.");
			}
		}	
		return candidates;
	}
}
