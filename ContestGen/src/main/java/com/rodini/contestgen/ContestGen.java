package com.rodini.contestgen;

import static com.rodini.contestgen.Environment.TEST;
import static java.nio.file.LinkOption.NOFOLLOW_LINKS;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.rodini.ballotutils.Utils;
/**
 * ContestGen is the program that analyzes the text of the Voter Services specimen
 * file and extracts the municipal level contests from it. At the end of the run
 * the contests folder should be populated as follows:
 *   005_Atglen_contests.txt
 *   010_Avondale_contests.txt
 *   ...
 *   common_contests.txt
 *   
 * CLI arguments: 
 * args[0] path Voter Services specimen text file.
 * args[1] path to directory for generated municipal level "NNN_XYZ_contests.txt" files.
 * 
 * @author Bob Rodini
 *
 */
public class ContestGen {
	
	static final Logger logger = LoggerFactory.getLogger(ContestGen.class);
	static final String ENV_BALLOTGEN_VERSION = "BALLOTGEN_VERSION";
	static final String PROPS_FILE = "contestgen.properties";
	static final String RESOURCE_PATH = "./resources/";
	static final String CONTESTS_FILE = "_contests.txt";

	static Environment env;		// TEST vs. PRODUCTION
	static String specimenText; // text of the Voter Services specimen.
	static String outPath;		// path to output directory
	static Properties props;
	static MuniContestNames muniContestNames;
	static MuniContestNames commonContestNames;
	
	
	/** 
	 * main entry point for program.
	 * @param args CLI arguments
	 */
	public static void main(String[] args){
		Utils.setLoggingLevel("com.rodini.contestgen");
		String version = System.getenv(ENV_BALLOTGEN_VERSION);
		String startMsg = String.format("Start of ContestGen app. Version: %s", version);
		System.out.println(startMsg);
		logger.info(startMsg);
		
		initialize(args);
		startMsg = String.format("Environment: %s", env.toString());
		System.out.println(startMsg);
		logger.info(startMsg);
		
		boolean first = true;
		// Use below during development, switch to resource loading afterwards.
		SpecimenMuniExtractor sme = new SpecimenMuniExtractor(specimenText);
		List<MuniTextExtractor> mteList = sme.extract();
		MuniContestNames muniContestNames;
		List<MuniContestNames> mcnList = new ArrayList<> ();
		for (MuniTextExtractor mte: mteList) {
			MuniContestsExtractor mce = mte.extract();
			muniContestNames = mce.extract();
			// debug
			String muniName = muniContestNames.getMuniName();
			if (first) {
				commonContestNames = muniContestNames;
				first = false;
			} else {
				commonContestNames = commonContestNames.intersect(muniContestNames);
			}
			// generate municipality contests
			genMuniContestsFile(muniName, muniContestNames.get());
			mcnList.add(muniContestNames);
		}
		// generate common contests.
		genMuniContestsFile("common", commonContestNames.get());
		// generate a summary report.
		genContestsSummary(mcnList);
		logger.info("End of ContestGen app.");
		System.out.println("End of ContestGen app.");
	}
	/**
	 * initialize the application attempting to FAIL EARLY if possible.
	 * @param args CLI arguments
	 */
	static void initialize(String [] args) {
		// check the # of command line args
		if (args.length < 2) {
			Utils.logFatalError("missing command line arguments:\n" +
					"args[0]: path Voter Services specimen text file.\n" +
					"args[1]: path to directory for generated municipal level \"NNN_XYZ_contests.txt\" files.");
		} else {
			String msg0 = String.format("path to Voter Services specimen text   : %s", args[0]);
			String msg1 = String.format("path to directory for generate contests: %s", args[1]);
			System.out.println(msg0);
			System.out.println(msg1);
		}
		// check args[0] is present and is a TXT file
		String specimenFilePath = args[0];
		if (!Files.exists(Path.of(specimenFilePath), NOFOLLOW_LINKS)) {
			Utils.logFatalError("can't find \"" + specimenFilePath + "\" file.");
		}
		if (!specimenFilePath.endsWith("txt")) {
			Utils.logFatalError("file \"" + specimenFilePath + "\" doesn't end with TXT extension.");
		}
		specimenText = Utils.readTextFile(specimenFilePath);
		// check args[1] is present and a directory
		outPath = args[1];
		File directory1 = new File(outPath);
		try {
			if (!directory1.isDirectory()) {
				Utils.logFatalError("command line arg[1] is not a directory: " + outPath);
			}
		} catch (SecurityException e) {
			Utils.logFatalError("can't access this directory" + outPath);
		}
		// read in program's properties
		String propsFilePath = RESOURCE_PATH + PROPS_FILE;
		props = Utils.loadProperties(propsFilePath);
		String envStr = props.getProperty("environment");
		env = Environment.valueOf(envStr);
		// initialize marker classes
		initMarkers(propsFilePath);
	}
	/**
	 * initMarkers initialize the "marker" values.  If env == TEST
	 * then use the values hard-coded in each class. Otherwise,
	 * read SAME values from resource file.
	 * 
	 * @param propsFilePath path to properties file.
	 */
	static void initMarkers(String propsFilePath) {
		if (env == TEST) {
			ContestNameMarkers.initialize(false, null);
			MuniTextMarkers.initialize(false, null);
			SpecimenMuniMarkers.initialize(false, null);
		} else {
			// PRODUCTION
			// validate SpecimenMuniMarkers values
			SpecimenMuniMarkers.initialize(true, propsFilePath);
			// validate MuniTextMarkers values
			MuniTextMarkers.initialize(true, propsFilePath);
			// validate ContestNameMarkers values
			ContestNameMarkers.initialize(true, propsFilePath);
		}
	}
	/**
	 * genMuniContestsFile generates the contest(s) file for the municipality.
	 * 
	 * @param muniName municipality name
	 * @param cnList list of contest names w/ formats.
	 */
	static void genMuniContestsFile(String muniName, List<ContestName> cnList) {
		if (env == TEST) {
			for (ContestName mcn: cnList) {
				String contestName = mcn.getName();
				contestName = contestName.replaceAll("\n", "\\\\\\n");
				System.out.printf("%s: %s, %d%n", muniName, contestName, mcn.getFormat());
			}
		} else {
			// PRODUCTION
			String contestFilePath = outPath + File.separator + muniName + CONTESTS_FILE;
			String msg = String.format("writing file: %s", contestFilePath);
			System.out.println(msg);
			logger.info(msg);
			try (FileWriter contestsFile = new FileWriter(contestFilePath, false);) {
				for (ContestName mcn: cnList) {
					String contestName = mcn.getName();
					contestName = contestName.replaceAll("\n", "\\\\\\n");
					contestsFile.write(String.format("%s,%d%n", contestName, mcn.getFormat()));
				}
	
			} catch (IOException e) {
				logger.error(e.getMessage());
			}
		}
	}
	/**
	 * genContestsSummary - generate a summary report and determine how many
	 * unique ballots there are.
	 */
	static void genContestsSummary(List<MuniContestNames> mcnList) {
		String line = "Summary";
		System.out.println(line);
		logger.info(line);
		line = String.format("Precinct count: %s", mcnList.size());
		System.out.println(line);
		logger.info(line);
		// determine how many unique ballots
		//   ContestsText  Municipalities w/ same ContestsText
		//       |                 |
		Map  <String,      List<String>> uniqueBallots = new HashMap<>();
		for (MuniContestNames mcn: mcnList) {
			String muniContestsText = mcn.getMuniContestsText();
			String muniName = mcn.getMuniName();
			if (!uniqueBallots.containsKey(muniContestsText)) {
				uniqueBallots.put(muniContestsText, new ArrayList<String>());
			}
			List<String> muniNames = uniqueBallots.get(muniContestsText);
			muniNames.add(muniName);
		}
		line = String.format("Unique ballot count: %s", uniqueBallots.keySet().size());
		System.out.println(line);
		logger.info(line);
		line = String.format("Precincts with identical ballots:");
		System.out.println(line);
		logger.info(line);
		Set<String> ballotKeys = uniqueBallots.keySet();
		int i = 0;
		for (String ballotKey: ballotKeys) {
			String muniNameList = String.join(",", uniqueBallots.get(ballotKey));
			line = String.format("Ballot %2d: %s", i, muniNameList);
			System.out.println(line);
			logger.info(line);
			i++;
		}
	}

}
