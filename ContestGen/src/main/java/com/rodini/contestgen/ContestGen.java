package com.rodini.contestgen;

import static com.rodini.contestgen.Environment.TEST;
import static java.nio.file.LinkOption.NOFOLLOW_LINKS;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.stream.Collectors;

//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.rodini.ballotutils.Utils;
import static com.rodini.ballotutils.Utils.ATTN;
/**
 * ContestGen is the program that analyzes the text of the Voter Services specimen
 * file and extracts the ballot text and municipal level contests from it. 
 * 
 * At the end of the run the contests folder (args[1]) should be populated as follows:
 *   005_Atglen_contests.txt
 *   010_Avondale_contests.txt
 *   ...
 *   common_contests.txt     <= not currently used 
 *   Ballot_Summary.txt      <= summary report
 * And the text ballots folder (args[2]) should be populated as follows;
 *   municipal0.txt (to be changed to 005_Atglen)
 *   municipal1.txt (to be changed to 010_Avondale)
 *   
 * CLI arguments: 
 * args[0] path Voter Services specimen text file.
 * args[1] path to directory for generated municipal level "NNN_XYZ_contests.txt" files.
 * args[2] path to directory for generated municipal level "municipalNNN.txt" files.
 * 
 * ENV variables:
 * BALLOTGEN_VERSION version # of Ballot Gen Software (e.g. "1.4.0")
 * BALLOTGEN_COUNTY  county for Ballot Gen (e.g. "chester")
 * 
 * @author Bob Rodini
 *
 */
public class ContestGen {
	
	static final Logger logger = LogManager.getRootLogger();
	static final String ENV_BALLOTGEN_VERSION = "BALLOTGEN_VERSION";
	static final String ENV_BALLOTGEN_COUNTY = "BALLOTGEN_COUNTY";
	static final String PROPS_FILE = "contestgen.properties";
	static final String RESOURCE_PATH = "./resources/";
	static final String CONTESTS_FILE = "_contests.txt";
	static final String MUNICIPAL_FILE = "municipal";
	static final String TEXT_EXT = ".txt";
	static final String PAGE_BREAK = "PAGE_BREAK"; // pseudo contest name
	static final String SUMMARY_FILE_NAME = "Ballot_Summary.txt";

	static String COUNTY;		// chester vs. bucks
	static String WRITE_IN; 	// Write-in vs. Write-In
	static Environment env;		// TEST vs. PRODUCTION
	static String specimenText; // text of the Voter Services specimen.
	static String outContestPath;		// path to contest output directory
	static String outBallotPath;		// path to ballot output directory
	static Properties props;
	static MuniContestNames muniContestNames;
	static MuniContestNames commonContestNames;
	static MuniReferendums muniReferendums;
	static MuniRetentions muniRetentions;
	
	/** 
	 * main entry point for program. Check for necessary ENV variables.
	 * @param args CLI arguments
	 */
	public static void main(String[] args){
		Utils.setLoggingLevel(LogManager.getRootLogger().getName());
		String version = Utils.getEnvVariable(ENV_BALLOTGEN_VERSION, true);
		COUNTY = Utils.getEnvVariable(ENV_BALLOTGEN_COUNTY, true);
		String startMsg = String.format("Start of ContestGen app. Version: %s", version);
		Utils.logAppMessage(logger, startMsg, true);
		startMsg = String.format("Contests for: %s Co.", COUNTY);
		Utils.logAppMessage(logger, startMsg, false);		
		initialize(args);
		
		boolean first = true;
		// use muniNameRegex to split specimenText MunitTextExtractor objects.
		SpecimenMuniExtractor sme = new SpecimenMuniExtractor(specimenText);
		List<MuniTextExtractor> mteList = sme.extract();
		// Take the MuniTextExtractor objects and generate municipal ballots.
		genMuniBallots(mteList);
		
		
		List<MuniContestNames> mcnList = new ArrayList<> ();
		for (MuniTextExtractor mte: mteList) {
			MuniContestsQuestionsExtractor mce = mte.extract();
			mce.extract();
			muniContestNames = mce.getMuniContestNames();
			muniReferendums  = mce.getMuniReferendums();
			muniRetentions   = mce.getMuniRetentions();
			// TODO: test if name is unique (as it should be, except for "356 E Marlborough S"
			// 
			String muniName = muniContestNames.getMuniName();
			if (first) {
				commonContestNames = muniContestNames;
				first = false;
			} else {
				commonContestNames = commonContestNames.intersect(muniContestNames);
			}
			// generate municipality contests
			genMuniContestsAndQuestions(muniName, muniContestNames.get());
			mcnList.add(muniContestNames);
		}
		// TODO: Death of common_contests.txt ?
		// generate common contests.
		//genMuniCommonContests("common", commonContestNames.get());
		// generate a summary report.
		genSummaryReport(mcnList);
		Utils.logAppErrorCount(logger);
		Utils.logAppMessage(logger, "End of ContestGen app.", true);
	}
	/**
	 * initialize the application attempting to FAIL EARLY if possible.
	 * @param args CLI arguments
	 */
	static void initialize(String [] args) {
		// check the # of command line args
		if (args.length < 3) {
			Utils.logFatalError("missing command line arguments:\n" +
					"args[0]: path Voter Services specimen text file.\n" +
					"args[1]: path to directory for generated municipal level \"NNN_XYZ_contests.txt\" files." +
					"args[2]: path to directory for generated municipal level \"municipalNNN.txt\" files.");
		} else {
			String msg0 = String.format("path to Voter Services specimen text: %s", args[0]);
			String msg1 = String.format("path to directory for contests:       %s", args[1]);
			String msg2 = String.format("path to directory for ballots :       %s", args[2]);
			System.out.println(msg0);
			logger.log(ATTN, msg0);
			System.out.println(msg1);
			logger.log(ATTN, msg1);
			System.out.println(msg2);
			logger.log(ATTN, msg2);
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
		outContestPath = args[1];
		File directory1 = new File(outContestPath);
		try {
			if (!directory1.isDirectory()) {
				Utils.logFatalError("command line arg[1] is not a directory: " + outContestPath);
			}
		} catch (SecurityException e) {
			Utils.logFatalError("can't access this directory" + outContestPath);
		}
		// check args[2] is present and a directory
		outBallotPath = args[2];
		File directory2 = new File(outBallotPath);
		try {
			if (!directory2.isDirectory()) {
				Utils.logFatalError("command line arg[2] is not a directory: " + outBallotPath);
			}
		} catch (SecurityException e) {
			Utils.logFatalError("can't access this directory" + outBallotPath);
		}
		// read in program's properties
		String propsFilePath = RESOURCE_PATH + PROPS_FILE;
		props = Utils.loadProperties(propsFilePath);
		WRITE_IN = props.getProperty(COUNTY + ".write.in");		
		logger.info(String.format("WRITE_IN: %s%n", WRITE_IN));		
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
		// validate SpecimenMuniMarkers values
		SpecimenMuniMarkers.initialize(propsFilePath);
		// validate MuniTextMarkers values
		MuniTextMarkers.initialize(propsFilePath);
		// validate ContestNameMarkers values
		ContestNameMarkers.initialize(propsFilePath);
		// validate ReferendumMarkers values
		ReferendumMarkers.initialize(propsFilePath);
		// validate RetentionMarkers values
		RetentionMarkers.initialize(propsFilePath);
	}
	/**
	 * GenMuniBallots generates the ballot file for each municipality.
	 */
	static void genMuniBallots(List<MuniTextExtractor> mteList) {
		int i = 1;
		for (MuniTextExtractor mte: mteList) {
			String muniName = mte.getMuniName();
			String muniBallotText = mte.getMuniText();
			String ballotFilePath = outBallotPath + File.separator + MUNICIPAL_FILE + Integer.toString(i) + TEXT_EXT;
			String msg = String.format("writing file: %s", ballotFilePath);
			System.out.println(msg);
			logger.info(msg);
			try (FileWriter ballotFile = new FileWriter(ballotFilePath, false);) {
				ballotFile.write(muniBallotText);
				
			} catch (IOException e) {
				logger.error(e.getMessage());
			}
			i++;
		}
	}
	
	static void genMuniContests(FileWriter cf, String muniName) throws IOException{
		cf.write("Contests\n");
		for (ContestName mcn: muniContestNames.get()) {
			String contestName = mcn.getName();
			contestName = contestName.replaceAll("\n", "\\\\\\n");
			cf.write(String.format("%s,%d%n", contestName, mcn.getFormat()));
		}	
	}
	static void genMuniReferendums(FileWriter cf, String muniName) throws IOException {
		if (muniReferendums.get().size() == 0) {
			return;
		}
		cf.write("Referendums\n");
		for (Referendum ref: muniReferendums.get()) {
			String refQuestion = ref.getRefQuestion();
			refQuestion = refQuestion.replaceAll("\n", "\\\\\\n");
			cf.write(String.format("%s%n", refQuestion));
		}	
	}
	static void genMuniRetentions(FileWriter cf, String muniName) throws IOException {
		if (muniRetentions.get().size() == 0) {
			return;
		}
		cf.write("Retentions\n");
		for (Retention ret: muniRetentions.get()) {
			String officeName = ret.getOfficeName();
			String judgeName = ret.getJudgeName();
			officeName = officeName.replaceAll("\n", "\\\\\\n");
			judgeName = judgeName.replaceAll("\n", "\\\\\\n");
			cf.write(String.format("%s,%s%n", officeName, judgeName));
		}	
	}
	/**
	 * genMuniContests generates the contests file for the municipality.
	 * 
	 * @param muniName municipality name
	 * @param cnList list of contest names w/ formats.
	 */
	static void genMuniContestsAndQuestions(String muniName, List<ContestName> cnList) {
		String contestFilePath = outContestPath + File.separator + muniName + CONTESTS_FILE;
		String msg = String.format("writing file: %s", contestFilePath);
		System.out.println(msg);
		logger.info(msg);
		try (FileWriter contestsFile = new FileWriter(contestFilePath, false);) {
			genMuniContests(contestsFile, muniName);
			genMuniReferendums(contestsFile, muniName);
			genMuniRetentions(contestsFile, muniName);
		} catch (IOException e) {
			logger.error(e.getMessage());
		}
	}
	/**
	 * genBallotReport - generate a ballot summary report and determine how many
	 * unique ballots there are.
	 * 
	 * 
	 */
	static void genSummaryReport(List<MuniContestNames> mcnList) {
		// try with resources will close the output file.
		try (PrintWriter pw = new PrintWriter(new File(outContestPath + File.separator + SUMMARY_FILE_NAME))) {
			String line = "Summary Report (Ballot/Referendum/Retention)";
			System.out.println(line);
			pw.println(line);
			genBallotReport(pw, mcnList);
			genReferendumReport(pw, ReferendumFactory.getReferendums());
			genRetentionReport(pw, RetentionFactory.getRetentions());
			pw.close();
		} catch (IOException ex) {
			String msg = String.format("IOException writing ballot summary report: %s", ex.getMessage());			
			logger.error(msg);
			System.out.println(msg);
		}
		
	}
	static void genBallotReport(PrintWriter pw, List<MuniContestNames> mcnList) {
		String line = "Ballot Summary";
		System.out.println(line);
		pw.println(line);
		line = String.format("Precinct count: %s", mcnList.size());
		System.out.println(line);
		logger.log(ATTN, line);
		pw.println(line);
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
		pw.println(line);
		line = String.format("Precincts with identical ballots:");
//		System.out.println(line);
		pw.println(line);
		Set<String> ballotKeys = uniqueBallots.keySet();
		int i = 0;
		for (String ballotKey: ballotKeys) {
			String muniNameList = String.join(",", uniqueBallots.get(ballotKey));
			line = String.format("Ballot %2d: %s", i, muniNameList);
//			System.out.println(line);
			pw.println(line);
			i++;
		}
	}
	
	static void genReferendumReport(PrintWriter pw, List<Referendum> refList) {
		String line = "Referendum Summary";
		System.out.println(line);
		pw.println(line);
		line = String.format("Unique referendum questions: %d", refList.size());
		System.out.println(line);
		logger.log(ATTN, line);
		pw.println(line);
		for (int i = 0; i < refList.size(); i++) {
			Referendum ref = refList.get(i);
			line = String.format("Referendum %d:", i);
			pw.println(line);
			// refQuestion text seems to end w. \n
			pw.print(ref.getRefQuestion());
			List<String> muniNoList = ref.getMuniNoList();
			String listAsString = muniNoList.stream().collect(Collectors.joining(","));
			pw.println(line + " precincts: " + listAsString);
		}
	}
	
	static void genRetentionReport(PrintWriter pw, List<Retention> retList) {
		String line = "Retention Summary";
		System.out.println(line);
		pw.println(line);
		line = String.format("Unique retention questions: %d", retList.size());
		System.out.println(line);
		logger.log(ATTN, line);
		pw.println(line);
		for (int i = 0; i < retList.size(); i++) {
			Retention ret = retList.get(i);
			line = String.format("Retention %d: %s", i, ret.getJudgeName());
			pw.println(line);
		}
	}

}
