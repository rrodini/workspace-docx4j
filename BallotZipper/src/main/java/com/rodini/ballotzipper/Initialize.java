package com.rodini.ballotzipper;

import static java.nio.file.LinkOption.NOFOLLOW_LINKS;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;

//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.rodini.ballotutils.Utils;

/**
 * Initialize class performs all initializations needed trying
 * to detect misconfigurations as soon as possible (i.e. fail-fast).
 * 
 * @author Bob Rodini
 *
 */
public class Initialize {
	static final Logger logger = LogManager.getLogger(Initialize.class);
	// Global variables
	static String csvFilePath;
	static String inDirPath;
	static String outDirPath;
	// Prevent instantiation.
	private Initialize() {
	}
	// Perform main initialization.
	static void initialize(String [] args) {
		// check the # command line arguments
		if (args.length != 3) {
			Utils.logFatalError("incorrect CLI arguments:\n" +
					"args[0]: path to precincts to zones CSV file.\n" +
					"args[1]: path to directory with municipal level \"NNN_name.docx\" files." +
					"args[2]: path to directory for zoneNN.zip files.");
		} else {
			String msg0 = String.format("CSV file: %s", args[0]);
			String msg1 = String.format("DOCX dir: %s", args[1]);
			String msg2 = String.format("ZIP dir:  %s", args[2]);
			System.out.println(msg0);
			System.out.println(msg1);
			System.out.println(msg2);
			logger.info(msg0);
			logger.info(msg1);
			logger.info(msg2);
		}
		// Check that args[0] exists and is a CSV file.
		csvFilePath = args[0];
		if (!Files.exists(Path.of(csvFilePath), NOFOLLOW_LINKS)) {
			Utils.logFatalError("can't find \"" + csvFilePath + "\" file.");
		}
		if (!csvFilePath.endsWith("csv")) {
			Utils.logFatalError("file \"" + csvFilePath + "\" doesn't end with CSV extension.");
		}
		// Check that args[1] is a directory and has files.
		inDirPath = args[1];
		if (!Files.isDirectory(Path.of(inDirPath))) {
			Utils.logFatalError("invalid args[1] value, DOCX dir doesn't exist: " + inDirPath);
		}		
		// Check that args[2] is a directory.
		outDirPath = args[2];
		if (!Files.isDirectory(Path.of(outDirPath))) {
			Utils.logFatalError("invalid args[2] value, ZIP dir doesn't exist: " + inDirPath);
		}
		// Clear out args[2] directory of ZIP files since inDir may equal outDir.
		File outDir = new File(outDirPath);
		File [] zipFiles = outDir.listFiles(file -> file.getName().endsWith(".zip"));
		for (File file: zipFiles) {
			file.delete();
		}
		
	}

}
