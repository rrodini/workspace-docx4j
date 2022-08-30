package com.rodini.ballotzipper;
/**
 * BallotZipper is the program that runs after the municipal docx files are generated.
 * One of the input files (args[0]) identifies which zones "own" which municipalities by precinct number.
 * It uses this information to create a zip file that can be send to zone leaders for the completion
 * of the sample ballots.
 * 
 * Notes:
 * 1) The program should really use an SQL database with zone information
 * 2) Voter Services is responsible for assigning precinct numbers to municipalities.
 *    Therefore, this program must chase VS in case new precincts appear in a new election cycle.
 * 
 * CLI arguments:
 * args[0] CSV file with municipality to zone mapping. 
 * args[1] path to input directory w/ docx, PDF and text files
 * args[2] path to output directory w/ zip files (can be same as args[1]
 * 
 * Program Logic:
 * 
 * Process CSV file into two data structures
 *  muniNoMap (TreeMap) key: MuniNo (String) value: zone (Zone object)
 *  zoneMap (TreeMap) key: zone no. (String) value: zone (Zone Object)
 * Produce zone report to console & log file
 * Process input directory into data structure
 *  docxNoMap (TreeMap) key: DocxNo (String) value: muniFiles (MuniFiles object)
 * Match the key set of muniNameMap to docxNameMap
 *  see matching algorithm in GenZipFile
 * Process zoneMap to produce zip files
 * 
 * @author Bob Rodini
 *
 */

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BallotZipper {

	private BallotZipper() {
	}
	static final Logger logger = LoggerFactory.getLogger(BallotZipper.class);
	static final String ENV_BALLOTGEN_VERSION = "BALLOTGEN_VERSION";

	public static void main(String[] args) {
		Utils.setLoggingLevel();
		String version = System.getenv(ENV_BALLOTGEN_VERSION);
		String message = String.format("Start of BallotZipper app. Version: %s", version);
		System.out.println(message);
		logger.info(message);
		Initialize.initialize(args);
		GenMuniMap.processCSVFile();
		Map<String, Zone> zoneMap = ZoneFactory.getZones();
		message = "Zone report:";
		System.out.println(message);
		logger.info(message);
		for (String zoneNo: zoneMap.keySet()) {
			Zone zone = zoneMap.get(zoneNo);
			message = String.format("Zone no: %s name %s", zone.getZoneNo(), zone.getZoneName());
			System.out.println(message);
			logger.info(message);
		}
//		Map<String, Zone> muniNoMap = GenMuniMap.getMuniNoMap();
//		for (String muniNo: muniNoMap.keySet()) {
//			Zone zone = muniNoMap.get(muniNo);
//			System.out.printf("Precinct no: %s Zone no: %s%n", muniNo, zone.getZoneNo());
//		}
		GenDocxMap.processInDir();
		Map<String, MuniFiles> docxNoMap = GenDocxMap.getDocxNoMap();
		message = "docxMap:";
		System.out.println(message);
		logger.info(message);
		for (String docxNo: docxNoMap.keySet()) {
			message = String.format("%s: %s", docxNo, docxNoMap.get(docxNo).toString());
			System.out.println(message);
			logger.info(message);
		}
		GenZipFiles.genZips();
	}
}
