package com.rodini.ballotnamer;

import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.rodini.ballotutils.Utils;
import static com.rodini.ballotutils.Utils.ATTN;

/**
 * BallotNamer is the program that runs after the Voter Services master sample ballot
 * is broken into precinct-level files and before the the BallotGen program runs.
 * 
 * It's main purpose is to re-name all of the files from data in within the text
 * of the file. Example: municipal-1.txt -> 005_Atglen_VS.txt
 *                       municipal-1.pdf -> 005_Atglen_VS.pdf
 *                       ...
 * 
 * CLI arguments: 
 * args[0] path to directory w/ PDF and text files
 * 
 * ENV variables:
 * BALLOTGEN_VERSION version # of Ballot Gen Software (e.g. "1.4.0")
 * BALLOTGEN_COUNTY  county for Ballot Gen (e.g. "chester")
 * 
 * @author Bob Rodini
 *
 */
public class BallotNamer {
	
	static final Logger logger = LogManager.getRootLogger();
	static final String ENV_BALLOTGEN_VERSION = "BALLOTGEN_VERSION";
	static final String ENV_BALLOTGEN_COUNTY = "BALLOTGEN_COUNTY";
	static String COUNTY;
	static String dirPath; // path to directory w/ pdf and text files
	static final String PROPS_FILE = "ballotnamer.properties";
	static Properties props;
	static final String CONTESTGEN_PROPS_FILE = "contestgen.properties";
	static Properties contestGenProps;
	static final String GENERIC_NAME = "%ballot title%";
	static Pattern fileBallotPattern;
	static Pattern ballotContestPattern;
	static final String JVM_LOG_LEVEL = "log.level";
	static final String RESOURCE_PATH = "./resources/";
	static final String CONTESTGEN_RESOURCE_PATH = "../contestgen/resources/";
	static Map<String, List<String>> preContests = new TreeMap<String, List<String>>();
	static final String FILE_SUFFIX = "_VS";	// suffix for renamed files
	static ENVIRONMENT env;
	/**
	 * initialize the program and perform validation.
	 * Will fail early if something is wrong.
	 * @param args
	 */
	/* private */
	static void initialize(String[] args) {
		// check the # of command line args
		if (args.length < 1) {
			Utils.logFatalError("initialize: missing command line argument:\n" +
					"args[0]: path to directory w/ PDF and text files");
		} else {
			String msg0 = String.format("path to dir w/ PDF and text files: %s", args[0]);
			System.out.println(msg0);
			logger.info(msg0);
		}
		// validate the CLI args
		dirPath = args[0];
		if (!Utils.checkDirExists(dirPath)) {
				Utils.logFatalError("initialize: command line args[0] is not a directory: " + dirPath);
		}
		logger.info("args[0]: " + dirPath);
		String propsFilePath = RESOURCE_PATH + PROPS_FILE;
		// get ballotnamer properties
		props = Utils.loadProperties(propsFilePath);
		logger.info("initialize: properties file loaded: " + propsFilePath);
		// get contestgen properties
		propsFilePath = CONTESTGEN_RESOURCE_PATH + CONTESTGEN_PROPS_FILE;
		contestGenProps = Utils.loadProperties(propsFilePath);
		logger.info("initialize: properties file loaded: " + propsFilePath);
		// build compile matching pattern
		fileBallotPattern = getFileBallotPattern();
		String envStr = props.getProperty("environment");
		env = ENVIRONMENT.valueOf(envStr);	
	}
    /**
     * getFileBallotPattern gets the format (regex) associated with the municipality ballot
     * name, manipulates it a little and transforms it into a Pattern object.
     * Note: the returned Pattern object is always multi-line.
     * 
     * ballot.format.name: "OFFICIAL MUNICIPAL ELECTION BALLOT"
     * 
     * @returns Pattern object for parsing text later on.
     */
	/* private */ 
	static Pattern getFileBallotPattern() {
		String startRegex = contestGenProps.getProperty(COUNTY + ".ballotnamer.ballot.heading.format");
		logger.debug(String.format("getFileBallotPattern: startRegex: %s", startRegex));
		Pattern compiledRegex = null;
		if (startRegex == null) {
			String msg = String.format("property \"$s.ballotnamer.ballot.heading.format\" cannot be found: ", COUNTY);
			Utils.logFatalError(msg);
		}
		String endRegex = startRegex.replace(GENERIC_NAME, contestGenProps.getProperty(COUNTY + ".ballotnamer.ballot.title"));
		logger.debug(String.format("getFileBallotPattern: endRegex: %s", endRegex));
		try {
			compiledRegex = Pattern.compile(endRegex, Pattern.MULTILINE);
		} catch (Exception e) {
			String msg = String.format("can't compile regex: %s msg: %s", endRegex , e.getMessage());
			Utils.logFatalError(msg);
		}
		logger.debug(String.format("getFileBallotPattern: compiledRegex: %s", compiledRegex.toString()));
		return compiledRegex;
	}
	
	/** 
	 * main entry point for program.
	 * @param args CLI arguments
	 */
	public static void main(String[] args) {
		Utils.setLoggingLevel(LogManager.getRootLogger().getName());
		String version = Utils.getEnvVariable(ENV_BALLOTGEN_VERSION, true);
		String startMsg = String.format("Start of BallotNamer app. Version: %s", version);
		Utils.logAppMessage(logger, startMsg, true);
		COUNTY = Utils.getEnvVariable(ENV_BALLOTGEN_COUNTY, true);
		startMsg = String.format("Names for: %s Co.", COUNTY);
		Utils.logAppMessage(logger, startMsg, false);
		initialize(args);
        processFiles();
        Utils.logAppErrorCount(logger);
		Utils.logAppMessage(logger, "End of BallotNamer app.", true);
	}
	/**
	 * processFiles builds lists of files in the directory where the "split'
	 * files reside.  These files should be the untouched output from PDFBOX
	 * after it has split the master sample ballot into municipality level files.
	 */
	/* private */
	static void processFiles() {
		List<String> txtFileNameList = collectFilesByExtension(".txt");
		// sorting here is absolutely necessary
		Collections.sort(txtFileNameList);
		List<String> pdfFileNameList = collectFilesByExtension(".pdf");
		// sorting here is absolutely necessary
		Collections.sort(pdfFileNameList);
		int txtFileCount = txtFileNameList.size();
		int pdfFileCount = pdfFileNameList.size();
		String msg = String.format("txtFileCount: %d pdfFileCount: %d", txtFileCount, pdfFileCount);
		System.out.println(msg);
		logger.info(msg);
		for (int i = 0; i < txtFileNameList.size(); i++) {
			// names should be in a one-to-one correspondence.
			processFile(txtFileNameList.get(i), pdfFileNameList.get(i));
		}
		msg = String.format("Renamed %s files", txtFileNameList.size());
		Utils.logAppMessage(logger, msg, false);
	}
	/**
	 * collectFilesByExtension returns a list of files in a directory that have 
	 * the given extension e.g. ".txt".
	 * @param ext e.g. ".txt"
	 * @return list of file names.
	 */
	/* private */
	static List<String> collectFilesByExtension(String ext) {
		File directory = new File(dirPath);
		List<String> fileList = Stream.of(directory.listFiles())
				.filter(file -> !file.isDirectory() && file.getName().endsWith(ext))
				.map(File::getName)
				.collect(toList());
		return fileList;
	}
	/**
	 * processFile process a pair of files assumed to match, e.g. split-1.pdf, split-1.txt
	 * @param txtFileName e.g. split-1.txt
	 * @param pdfFileName e.g. split-1.pdf
	 */
	/* private */ 
	static void processFile(String txtFileName, String pdfFileName) {
		String msg = String.format("processing files txt: %s pdf: %s", txtFileName, pdfFileName);
		System.out.println(msg);
		logger.info(msg);
		// get the file text for searching
		String fileText = getFileText(txtFileName);
		String ballotFileName = getBallotFileName(txtFileName, fileText);	
		// now rename the files
		String newTxtFileName = ballotFileName + FILE_SUFFIX + ".txt";
		String newPdfFileName = ballotFileName + FILE_SUFFIX + ".pdf";
		msg = String.format("renaming %s to %s", txtFileName, newTxtFileName );
		System.out.println(msg);
		logger.info(msg);
		msg = String.format("renaming %s to %s", pdfFileName, newPdfFileName );
		System.out.println(msg);
		logger.info(msg);
		renameFile(txtFileName, newTxtFileName);
		renameFile(pdfFileName, newPdfFileName);
	}
	/**
	 * renameFile renames a file within the same directory
	 * @param oldName the old name
	 * @param newName the new name
	 */
	static void renameFile(String oldName, String newName) {
		Path oldFilePath = Path.of(dirPath + File.separator + oldName);
		try {
			Files.move(oldFilePath, oldFilePath.resolveSibling(newName));
		} catch (IOException e) {
			logger.error(String.format("renameFile: couldn't rename %s to %s", oldName, newName));
		}

	}
	/**
	 * getBallotFileName assumes that the introductory text of the Voter Services doesn't
	 * change much from election to elections.  The text looks something like this:
	 *    OFFICIAL MUNICIPAL ELECTION BALLOT
	 *    010 Avondale
	 *    County of Chester, Commonwealth of Pennsylvania
	 *    ...
	 * The method uses a regular expression to parse out "Avondale"
	 * @param txtFileName text file name
	 * @param fileText contents of text file
	 * @return
	 */
	static String getBallotFileName(String txtFileName, String fileText) {
		// parse the municipality name
		Matcher m = fileBallotPattern.matcher(fileText);
		String ballotFileName = "";
		if (!m.find()) {
			String msg = String.format("parseFile: no match for file name: %s", txtFileName);
			Utils.logFatalError(msg);
		} else {
			try {
				ballotFileName = m.group("id") + "_" + m.group("name");
			} catch (Exception e) {
				String msg = e.getMessage();
				Utils.logFatalError(msg);
			}
		}
		if (props.get("replace.space.with.underscore").equals("true")) {
			ballotFileName = ballotFileName.replace(" ", "_");
		}
		return ballotFileName;
	}
	/**
	 * getFileText reads the contents of the text File.
	 * @param fileName name of text file.
	 * @return contents of text file.
	 */
	static String getFileText(String fileName) {
 		String fileText = null;
 		String filePath = dirPath + File.separator + fileName;
 		logger.debug(String.format("reading lines of file: %s", filePath));
		fileText = Utils.readTextFile(filePath);
		logger.debug(String.format("first 100 characters of fileText: %s", fileText.substring(0, Math.min(fileText.length(), 100))));
 		return fileText;
	}
	
}
