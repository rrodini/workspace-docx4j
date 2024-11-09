package com.rodini.ballotgen.common;

import java.io.File;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


/** 
 * BallotUtils are utility routines local to the BallotGen program.
 * Note: com.rodini.ballotgen.Utils are shared across multiple programs,
 * but these are not.
 * @author Bob Rodini
 *
 */
public class BallotUtils {
	
	private static final Logger logger = LogManager.getLogger(BallotUtils.class);
	private static final String FILE_SUFFIX = "_VS";

	/**
	 * getPrecinctNoName extracts the PrecinctNoName value from a file path
	 * that is shared between ballot generation programs (ContestGen, BallotNamer, BallotGen).
	 * Example:  "./chester-output/750_East_Whiteland_4_VS.txt" => "750_East_Whiteland_4"
	 * 
	 * @param ballotTextFilePath absolute or relative path to extracted text files.
	 * @return "750_East_Whiteland_4"
	 */
	public static String getPrecinctNoName(String ballotTextFilePath) {
		File textFile = new File(ballotTextFilePath);
		String fileName = textFile.getName();
		String pathName = textFile.getPath();
		logger.debug(String.format("pathName: %s", pathName));
		String precinctNoName;
		int lastDot = fileName.lastIndexOf(".");
		precinctNoName = fileName.substring(0, lastDot);
		// if suffix was added, then remove it.
		if (precinctNoName.endsWith(FILE_SUFFIX)) {
			precinctNoName = precinctNoName.substring(0, precinctNoName.length() - FILE_SUFFIX.length());
		}
		logger.debug(String.format("precinctNoName: %s", precinctNoName));
		return precinctNoName;
	}
	/**
	 * getPathNameOnly extracts the path to the ballot output folder.
	 * Example:  "./chester-output/750_East_Whiteland_4_VS.txt" => "./chester-output"
	 * 
	 * @param ballotTextFilePath absolute or relative path to extracted text files.
	 * @return "./chester-output"
	 */
	public static String getPathNameOnly(String ballotTextFilePath) {
		File textFile = new File(ballotTextFilePath);
		String fileName = textFile.getName();
		String pathName = textFile.getPath();
		int lastSeparator = pathName.lastIndexOf(File.separator);
		pathName = pathName.substring(0, lastSeparator);
		return pathName;
	}
	/**
	 * getPrecinctNo returns the 3 digit precinct # that Voter Services uses.
	 * 
	 * @param precinctNoName Example: 750_East_Whiteland_4
	 * @return 750
	 */
	public static String getPrecinctNo(String precinctNoName) {
		String precinctNo;
		precinctNo = precinctNoName.substring(0, 3);
		return precinctNo;
	}
	/**
	 * getPrecinctName returns the string following the 3 digit precinct # that Voter Services uses.
	 * 
	 * @param precinctNoName Example: 750_East_Whiteland_4
	 * @return East_Whiteland_4
	 */
	public static String getPrecinctName(String precinctNoName) {
		String precinctName;
		precinctName = precinctNoName.substring(4);
		return precinctName;
	}
}
