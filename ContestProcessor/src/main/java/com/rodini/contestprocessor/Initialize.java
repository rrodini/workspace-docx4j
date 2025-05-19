package com.rodini.contestprocessor;

import static java.util.stream.Collectors.joining;

import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.rodini.ballotutils.ElectionType;
import com.rodini.ballotutils.Party;
import com.rodini.ballotutils.Utils;

public class Initialize {
	private static final Logger logger = LogManager.getLogger(Initialize.class);

	private static final String ENV_BALLOTGEN_COUNTY = "COUNTY";
	public  static final String CONTESTGEN_RESOURCE_PATH = "../contestgen/resources/";
	public  static final String CONTESTGEN_PROPS_FILE = "contestgen.properties";
	private static final String PROP_ELECTION_TYPE = "election.type";
	private static final String PROP_ENDORSED_PARTY = "endorsed.party";
	private static final String PROP_CONTEST_FORMAT_PREFIX = "ballotgen.contest.format";
	public  static final String TICKET_CONTEST_NAMES = "ticket.contest.names";
	public  static final String LOCAL_CONTEST_NAMES = "local.contest.names";
	public  static final String LOCAL_CONTEST_EXCEPTION_NAMES = "local.contest.exception.names";
	// Global variables below
	public	static       String COUNTY;
	public  static       String WRITE_IN;
	public  static Properties contestGenProps;
	public  static ElectionType elecType;	// e.g. PRIMARY or GENERAL
	public  static Party endorsedParty;		// e.g. Democratic (or NULL)
	public  static       List<String> namesOfTicketContests;
	public  static       List<String> namesOfLocalContests;
	public  static       List<String> namesOfLocalContestsExceptions;
	public  static String formatsText;		// ballotgen contest name formats (regexes)

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
	 * validateFormatsText reads the formats (regexes) from the properties
	 * file and gets them into a long string that ContestFactory
	 * expects.
	 */
	static void validateFormatsText() {
		// This is a good place to read the Write-in string value.
		WRITE_IN = Utils.getPropValue(contestGenProps, COUNTY + ".write.in");
		List<String> formatLines = Utils.getPropOrderedValues(contestGenProps, COUNTY + ".ballotgen.contest.format");
		formatsText = formatLines.stream()
				.collect(joining("\n"));
		logger.info(String.format("formatsText:%n%s%n", formatsText));
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
			String propName =  COUNTY + PROP_CONTEST_FORMAT_PREFIX + "." + key;
			format = Utils.getPropValue(contestGenProps, propName);
			logger.info(String.format("%s.%d: %s", propName, count, format));
			if (format != null) {
				count++;				
			}
		} while (format != null);
		logger.info(String.format("there are %d contest formats in the contestgen properties file", count-1));
	}
	
	/**
	 * Used by CandidateFactory (of all things).
	 */
	static void validateTicketAndLocalContestNames() {
		String 	contestNames;
		contestNames = Utils.getPropValue(contestGenProps, TICKET_CONTEST_NAMES);
		namesOfTicketContests = Arrays.asList(contestNames.split(","));
		contestNames = Utils.getPropValue(contestGenProps, LOCAL_CONTEST_NAMES);
		namesOfLocalContests = Arrays.asList(contestNames.split(","));
		contestNames = Utils.getPropValue(contestGenProps, LOCAL_CONTEST_EXCEPTION_NAMES);
		namesOfLocalContestsExceptions = Arrays.asList(contestNames.split(","));
	}
	public static void start() {
		COUNTY = Utils.getEnvVariable(ENV_BALLOTGEN_COUNTY, true);
		contestGenProps = Utils.loadProperties(CONTESTGEN_RESOURCE_PATH + CONTESTGEN_PROPS_FILE);
		// very little validation here.
		validateElectionType();
		validateEndorsedParty();
		validateFormatsText();
		validateContestFormats();
		validateTicketAndLocalContestNames();
	}
}
