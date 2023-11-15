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
 * args[2] path to output directory w/ zip files
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

//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.rodini.ballotutils.Utils;
import static com.rodini.ballotutils.Utils.ATTN;
import com.rodini.zoneprocessor.ZoneProcessor;
import com.rodini.zoneprocessor.Zone;
import com.rodini.zoneprocessor.ZoneFactory;

public class BallotZipper {

	private BallotZipper() {
	}
	static final Logger logger = LogManager.getRootLogger();
	static final String ENV_BALLOTGEN_VERSION = "BALLOTGEN_VERSION";
	static final String ENV_BALLOTGEN_COUNTY = "BALLOTGEN_COUNTY";
	static String COUNTY;		// chester vs. bucks

	/** 
	 * main implements the algorithm described above.
	 * @param args CLI arguments.
	 */
	public static void main(String[] args) {
		Utils.setLoggingLevel(LogManager.getRootLogger().getName());
		String version = Utils.getEnvVariable(ENV_BALLOTGEN_COUNTY, true);
		COUNTY = Utils.getEnvVariable(ENV_BALLOTGEN_COUNTY, true);
		String message = String.format("Start of BallotZipper app. Version: %s", version);
		Utils.logAppMessage(logger, message, true);		
		message = String.format("Zip files for: %s Co.", COUNTY);
		Utils.logAppMessage(logger, message, false);
		Initialize.initialize(args);
		// Process CSV file into two data structures
		//  muniNoMap (TreeMap) key: MuniNo (String) value: zone (Zone object)
		//  zoneMap (TreeMap) key: zone no. (String) value: zone (Zone Object)		
		String csvText = Utils.readTextFile(Initialize.csvFilePath);
		ZoneProcessor.processCSVText(csvText);
		Map<String, Zone> zoneMap = ZoneFactory.getZones();
		// Produce zone report to console & log file
		message = "Zone report:";
		System.out.println(message);
		logger.log(ATTN, message);
		for (String zoneNo: zoneMap.keySet()) {
			Zone zone = zoneMap.get(zoneNo);
			message = String.format("Zone no: %s name %s", zone.getZoneNo(), zone.getZoneName());
			System.out.println(message);
			logger.log(ATTN, message);
		}
//		Map<String, Zone> muniNoMap = GenMuniMap.getMuniNoMap();
//		for (String muniNo: muniNoMap.keySet()) {
//			Zone zone = muniNoMap.get(muniNo);
//			System.out.printf("Precinct no: %s Zone no: %s%n", muniNo, zone.getZoneNo());
//		}
		// Process input directory into data structure
		//  docxNoMap (TreeMap) key: DocxNo (String) value: muniFiles (MuniFiles object)
		GenDocxMap.processInDir();
		Map<String, MuniFiles> docxNoMap = GenDocxMap.getDocxNoMap();
		message = "docxMap:";
		System.out.println(message);
		logger.log(ATTN, message);
		for (String docxNo: docxNoMap.keySet()) {
			message = String.format("%s: %s", docxNo, docxNoMap.get(docxNo).toString());
			Utils.logAppMessage(logger, message, false);
		}
		GenZipFiles.genZips();
		message = "End of BallotZipper app";
		Utils.logAppErrorCount(logger);
		Utils.logAppMessage(logger, message, true);
	}
}
