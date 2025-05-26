/**
 * Initialize class reads in and validates the properties used  by the VoteForProcessor.
 * These properties are stored in the contestgen.properties file.
 * Notes:
 * 1. ContestGen1 and BallotGen use this component as of v1.7.0.
 * 1. The location of contestgen.properties is assumed ../contestgen/resources/contestgen.properties
 * 2. start() method must be called by program using component.
 */
package com.rodini.voteforprocessor.extract;

import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.regex.Pattern;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.rodini.ballotutils.ElectionType;
import com.rodini.ballotutils.Party;
import com.rodini.ballotutils.Utils;

public class Initialize {
	private static final Logger logger = LogManager.getLogger(Initialize.class);
	// Property names. Note: FORMAT == REGEX
	private static final String ENV_BALLOTGEN_COUNTY = "BALLOTGEN_COUNTY";
	private static final String PROP_ELECTION_TYPE = "election.type";
	private static final String PROP_ENDORSED_PARTY = "endorsed.party";
	private static final String PROP_WRITE_IN = ".write.in";
	private static final String PROP_CONTEST_FORMAT_PREFIX = ".contest.format";
	private static final String PROP_REFERENDUM_FORMAT = ".referendum.format";
	private static final String PROP_RETENTION_QUESTION_FORMAT = ".retention.question.format";
	private static final String PROP_RETENTION_NAME_FORMAT = ".retention.name.format";
	public  static final String PROP_TICKET_CONTEST_NAMES = ".ticket.contest.names";
	public  static final String PROP_LOCAL_CONTEST_NAMES = ".local.contest.names";
	public  static final String PROP_LOCAL_CONTEST_EXCEPTION_NAMES = ".local.contest.exception.names";
	// Global variables below. These reflect values of properties.
	public	static       String COUNTY;
	public  static       String WRITE_IN;
	public  static Properties   contestGenProps;
	public  static ElectionType elecType;	// e.g. PRIMARY or GENERAL
	public  static Party        endorsedParty;		// e.g. Democratic (or NULL)
	public  static List<String> namesOfTicketContests;
	public  static List<String> namesOfLocalContests;
	public  static List<String> namesOfLocalContestsExceptions;
	public  static Pattern []   contestRegexes;
	public  static Pattern      referendumRegex;
	public  static Pattern      retentionQuestionRegex;
	public  static Pattern      retentionNameRegex;

	/**
	 * validateElectionType get/display election type.
	 */
	static void validateElectionType() {
		String type = Utils.getPropValue(contestGenProps, PROP_ELECTION_TYPE);
		logger.info(String.format("election.type: %s", type));
		elecType = ElectionType.toEnum(type);
	}
	/**
	 * validateEndorsedParty get/display the endorsed party.
	 */
	static void validateEndorsedParty() {
		String endorsedPartyString = Utils.getPropValue(contestGenProps, PROP_ENDORSED_PARTY);
		logger.info(String.format("endorsed.party: %s", endorsedPartyString));
		endorsedParty = endorsedPartyString.isEmpty()? null : Party.toEnum(endorsedPartyString);
	}
	/**
	 * validateWritein property. This is critical to contest delineation.
	 */
	static void validateWritein() {
		WRITE_IN = Utils.getPropValue(contestGenProps, COUNTY + PROP_WRITE_IN);
		if (WRITE_IN == null || WRITE_IN.isBlank()) {
			logger.error("write.in property is null or blank");
		} else {
			logger.info(String.format("write.in: %s", WRITE_IN));
		}
	}
	/**
	 * validateOrderedRegexProperties matches Utils.getPropOrderedValues logic
	 * which finds a sequence of property names that start with the same prefix.
	 * there is not a whole lot of validation, but the regexes must compile.
	 * 
	 * @param props Properties object
	 * @param propPrefix String prefix for sequence of related regex properties
	 * @return List of compile Pattern objects.
	 */
	static Pattern [] validateOrderedRegexProperties(Properties props, String propPrefix) {
		List<String> regexList = Utils.getPropOrderedValues(props, propPrefix);
		Pattern [] patList = new Pattern [regexList.size()];
		for (int i = 0; i < regexList.size(); i++) {
			patList[i] = Utils.compileRegex(regexList.get(i));
		}
		return patList;
	}
	/**
	 * validateRegexProperty looks for the given property by name and
	 * validates that the string value can be compiled as a regex Pattern.
	 * 
	 * @param props Properties object
	 * @param propName property name
	 * @return property value compiled as Pattern.
	 */
	static Pattern validateRegexProperty(Properties props, String propName) {
		Pattern pat = null;
		String propVal = Utils.getPropValue(props, propName);
		if (propVal == null || propVal.isBlank()) {
			logger.error(String.format("property %s is missing.", propName));
		} else {
			pat = Utils.compileRegex(propVal);
		}
		return pat;
	}
	/**
	 * validateContestFormats reads the contest formats (regexes) from the properties
	 * file and displays them.
	 */
	static void validateContestFormats() {
		String format;
		int count = 1;
		do {
			// Don't know how many there will be
			String key = Integer.toString(count);
			String propName =  COUNTY + PROP_CONTEST_FORMAT_PREFIX + key;
			format = Utils.getPropValue(contestGenProps, propName);
			logger.info(String.format("%s: %s", propName, format));
			if (format != null) {
				count++;				
			}
		} while (format != null);
		logger.info(String.format("there are %d contest formats in the contestgen properties file", count-1));
		// Now validate them by compiling them
		contestRegexes = validateOrderedRegexProperties(contestGenProps, COUNTY + PROP_CONTEST_FORMAT_PREFIX);
	}
	
	/**
	 * Used by CandidateFactory (of all things).
	 */
	static void validateTicketAndLocalContestNames() {
		String 	contestNames;
		contestNames = Utils.getPropValue(contestGenProps, COUNTY + PROP_TICKET_CONTEST_NAMES);
		logger.info(String.format("%s:%n %s", PROP_TICKET_CONTEST_NAMES, contestNames));
		namesOfTicketContests = Arrays.asList(contestNames.split(","));
		contestNames = Utils.getPropValue(contestGenProps, COUNTY + PROP_LOCAL_CONTEST_NAMES);
		logger.info(String.format("%s:%n %s", PROP_LOCAL_CONTEST_NAMES, contestNames));
		namesOfLocalContests = Arrays.asList(contestNames.split(","));
		contestNames = Utils.getPropValue(contestGenProps, COUNTY + PROP_LOCAL_CONTEST_EXCEPTION_NAMES);
		logger.info(String.format("%s:%n %s", PROP_LOCAL_CONTEST_EXCEPTION_NAMES, contestNames));
		namesOfLocalContestsExceptions = Arrays.asList(contestNames.split(","));
	}
	/**
	 * start() - validates the voteforprocessor's properties which are read in
	 * by the program that is dependent on it.
	 * 
	 * @param props Properties object containing VOTEFORPROCESSOR properties.
	 */
	public static void start(Properties props) {
		COUNTY = Utils.getEnvVariable(ENV_BALLOTGEN_COUNTY, true);
		contestGenProps = props;
		// very little validation here.
		validateElectionType();
		validateEndorsedParty();
		validateWritein();
		validateContestFormats();  // ordered regexes
		validateTicketAndLocalContestNames();
	    referendumRegex = validateRegexProperty(contestGenProps, COUNTY + PROP_REFERENDUM_FORMAT);
	    retentionQuestionRegex = validateRegexProperty(contestGenProps, COUNTY + PROP_RETENTION_QUESTION_FORMAT);
	    retentionNameRegex = validateRegexProperty(contestGenProps, COUNTY + PROP_RETENTION_NAME_FORMAT);
	}


}
