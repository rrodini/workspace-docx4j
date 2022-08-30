package com.rodini.ballotnamer;

import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;
import static org.apache.logging.log4j.Level.DEBUG;
import static org.apache.logging.log4j.Level.ERROR;
import static org.apache.logging.log4j.Level.INFO;
import static org.apache.logging.log4j.Level.TRACE;
import static org.apache.logging.log4j.Level.WARN;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * BallotNamer is the program that runs after the Voter Services master sample ballot
 * is broken into precinct-level files and before the the BallotGen program runs.
 * 
 * It's main purpose is to re-name all of the files from data in within the text
 * of the file. Example: municipal-1.txt -> 005_Atglen(005)_VS.txt
 *                       municipal-1.pdf -> 005_Atglen(005)_VS.pdf
 *                       ...
 * 
 * CLI arguments: 
 * arg[0] path to directory w/ PDF and text files
 * 
 * 
 * @author Bob Rodini
 *
 */
public class BallotNamer {
	
	static final Logger logger = LoggerFactory.getLogger(BallotNamer.class);
	static final String ENV_BALLOTGEN_VERSION = "BALLOTGEN_VERSION";
	static String dirPath; // path to directory w/ pdf and text files
//	static String outPath; // path to output directory
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
//	static final String PRE_CONTESTS_FILE = "pre-contests.txt";
	static Map<String, List<String>> preContests = new TreeMap<String, List<String>>();
	static final String FILE_SUFFIX = "_VS";	// suffix for renamed files
	static ENVIRONMENT env;
	/** 
	 * Log a fatal error and stop program.
	 * @param msg message to display / log.
	 */
	public static void logFatalError(String msg) {
		System.out.println(msg);
		logger.error(msg);
		System.exit(1); // non-zero exit code == fatal error
	}
	/**
	 * initialize the program and perform validation.
	 * Will fail early if something is wrong.
	 * @param args
	 */
	/* private */
	static void initialize(String[] args) {
		// check the # of command line args
		if (args.length < 1) {
			logFatalError("initialize: missing command line argument:\n" +
					"args[0]: path to directory w/ PDF and text files");
		} else {
			String msg0 = String.format("path to dir w/ PDF and text files: %s", args[0]);
			System.out.println(msg0);
			logger.info(msg0);
		}
		// validate the CLI args
		dirPath = args[0];
		File directory1 = new File(dirPath);
		try {
			if (!directory1.isDirectory()) {
				logFatalError("initialize: command line arg[0] is not a directory: " + dirPath);
			}
		} catch (SecurityException e) {
			logFatalError("initialize: can't access this directory" + dirPath);
		}
		String propsFilePath = RESOURCE_PATH + PROPS_FILE;
		// get ballotname properties
		props = getPropsFromFile(propsFilePath);
		logger.info("initialize: properties file loaded: " + propsFilePath);
		// get contestgen properties
		propsFilePath = CONTESTGEN_RESOURCE_PATH + CONTESTGEN_PROPS_FILE;
		contestGenProps = getPropsFromFile(propsFilePath);
		logger.info("initialize: properties file loaded: " + propsFilePath);
		// build compile matching pattern
		fileBallotPattern = getFileBallotPattern();
		String envStr = props.getProperty("environment");
		env = ENVIRONMENT.valueOf(envStr);	
	}
	/**
	 * setLoggingLevel sets the logging level to the value of the
	 * JVM parameter "log.level".  Default is ERROR
	 */
	public static void setLoggingLevel() {
		Map<String, org.apache.logging.log4j.Level> logLevels = Map.of(
				"ERROR", ERROR,
				"WARN", WARN,
				"INFO", INFO,
				"DEBUG", DEBUG,
				"TRACE", TRACE
				);
		// get logging level from JVM argument
		String level = System.getProperty(JVM_LOG_LEVEL, "ERROR").toUpperCase();
		org.apache.logging.log4j.Level log4jLevel = logLevels.get(level);
		if (log4jLevel == null) {
			log4jLevel = ERROR;
		}
		org.apache.logging.log4j.core.config.Configurator.setLevel("com.rodini.ballotnamer",log4jLevel);
	}
	/**
	 * Get the program properties.
	 * @param filePath to properties file.
	 * @return Properties object.
	 */
	static Properties getPropsFromFile(String filePath) {
		Properties props = new Properties();
		try (FileInputStream resourceStream = new FileInputStream(filePath);) {
			props.load(resourceStream);
		} catch (Exception e) {
			e.printStackTrace();
			logFatalError("getPropsFromFile: cannot load properties file: " + filePath);
		}
		return props;
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
//		String startRegex = props.getProperty("ballot.heading.format");
		String startRegex = contestGenProps.getProperty("ballotnamer.ballot.heading.format");
		logger.debug(String.format("getFileBallotPattern: startRegex: %s", startRegex));
		Pattern compiledRegex = null;
		if (startRegex == null) {
			String msg = "property \"ballotnamer.ballot.heading.format\" cannot be found: ";
			logFatalError(msg);
		}
		String endRegex = startRegex.replace(GENERIC_NAME, contestGenProps.getProperty("ballotnamer.ballot.title"));
		if (endRegex.startsWith("/")) {
			// strip off leading "/"
			endRegex = endRegex.substring(1);
		}
		if (endRegex.endsWith("/")) {
			// strip off trailing "/"
			endRegex = endRegex.substring(0, endRegex.length() - 1);
		}
		logger.debug(String.format("getFileBallotPattern: endRegex: %s", endRegex));
		try {
			compiledRegex = Pattern.compile(endRegex, Pattern.MULTILINE);
		} catch (Exception e) {
			String msg = String.format("can't compile regex: %s msg: %s", endRegex , e.getMessage());
			logFatalError(msg);
		}
		logger.debug(String.format("getFileBallotPattern: compiledRegex: %s", compiledRegex.toString()));
		return compiledRegex;
	}
	
	/** 
	 * main entry point for program.
	 * @param args CLI arguments
	 */
	public static void main(String[] args) {
		setLoggingLevel();
		String version = System.getenv(ENV_BALLOTGEN_VERSION);
		String startMsg = String.format("Start of BallotNamer app. Version: %s", version);
		System.out.println(startMsg);
		logger.info(startMsg);
		initialize(args);
        processFiles();
 		logger.info("End of BallotNamer app.");
		System.out.println("End of BallotNamer app.");
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
			logFatalError(msg);
		} else {
			try {
				ballotFileName = m.group("id") + "_" + m.group("name");
			} catch (Exception e) {
				String msg = e.getMessage();
				logFatalError(msg);
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
		logger.debug(String.format("reading lines of file: %s%n", dirPath + File.separator + fileName));
 		List<String> fileLines = null;
		try {
			fileLines = Files.readAllLines(Path.of(dirPath + File.separator + fileName));
			logger.debug(String.format("successfully read %d lines", fileLines.size()));
		} catch (IOException e) {
			String msg = "getFileText: can't read file: " + fileName;
			logger.error(msg);
		}
 		String fileText = fileLines.stream().collect(joining("\n"));
		logger.debug(String.format("first 100 characters of fileText: %n", fileText.substring(0, Math.min(fileText.length(), 100))));
 		return fileText;
	}
	/**
	 * getBallotContests tries to extract the contests from the text of the file.
	 * This is hard.  First, there is no marker for the first contest (so it will be missed).
	 * Second, some contests are spread over two lines.  There is no way of knowing this in advance.
	 * @param ballotFileName
	 * @param fileText
	 */
	static void getBallotContests(String ballotFileName, String fileText) {
		// parse the municipality name
		String contestTitle = "";
		Matcher m = ballotContestPattern.matcher(fileText);
		if (!m.find()) {
			String msg = String.format("parseFile: no match for any contests in: %s", ballotFileName);
			logFatalError(msg);
		} else {
			List<String> contestTitles = new ArrayList<>();
			// must give index or 1st match is lost to if test.
			int i = 0;
			while (m.find(i)) {
				try {
					i++;
					contestTitle = m.group("contest");
					if (!contestTitle.startsWith("Typ:")) {
						contestTitles.add(contestTitle);						
					}
				} catch (Exception e) {
					String msg = e.getMessage();
					logFatalError(msg);
				}
			}
			preContests.put(ballotFileName, contestTitles);

		}
	}
	
}
