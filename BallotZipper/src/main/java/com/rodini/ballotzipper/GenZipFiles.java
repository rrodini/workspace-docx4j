package com.rodini.ballotzipper;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.rodini.zoneprocessor.ZoneProcessor;
import com.rodini.zoneprocessor.Zone;
import com.rodini.zoneprocessor.ZoneFactory;
/**
 * GenZipFile class generates the zip files for each zone.
 * @author Bob Rodini
 *
 */
public class GenZipFiles {
	static final Logger logger = LoggerFactory.getLogger(GenZipFiles.class);
	
	static final String ZIP = ".zip";
	
	// Prevent instantiation.
	private GenZipFiles() {
	}
	
/** 
 * Logic:
 * 
 * for each munikey value
 *   find matching docxkey
 *   if !found
 *     report error
 *   end if
 * end for
 * for each docxkey value
 *   find matching muniKey
 *   if !found
 *      report error
 *   end if
 * end for
 * for each zone
 *  generate zone zip file
 * end for
 *  
 */
	
	// Perform mapping checks ensuring that the two maps are in sync.
	// Then generate the zip files for each zone.
	static void genZips() {
		Map<String, Zone> zoneMap = ZoneFactory.getZones();
		Map<String, Zone> muniNoMap = ZoneProcessor.getPrecinctZoneMap();
		Map<String, MuniFiles> docxNoMap = GenDocxMap.getDocxNoMap();
		Map<Zone, List<MuniFiles>> zoneMuniFiles = new HashMap<>();

		Set<String> docxKeys = docxNoMap.keySet();
		// Check docxNo against muniNo. Error if CSV file doesn't contain docxNo.
		for (String muniNo: muniNoMap.keySet()) {
			if (!docxKeys.contains(muniNo)) {
				logger.error(String.format("CSV precinct %s lacks DOCX files", muniNo));
			} else {
				// Transfer the muniFiles to the zone that owns them.
				Zone zone = muniNoMap.get(muniNo);
				MuniFiles muniFiles = docxNoMap.get(muniNo);
				List<MuniFiles> currentMuniFiles = zoneMuniFiles.get(zone);
				if (currentMuniFiles == null) {
					currentMuniFiles = new ArrayList<MuniFiles>();
				}
				currentMuniFiles.add(muniFiles);
				zoneMuniFiles.put(zone, currentMuniFiles);
			}
		}
		Set<String> muniKeys = muniNoMap.keySet();
		for (String docxNo: docxNoMap.keySet()) {
			// Check muniNo against docxNo. Error if docx entry is not "owned" by a zone.
			if (!muniKeys.contains(docxNo)) {
				logger.error(String.format("DOCX precinct %s lacks CSV precinct", docxNo));
			}
		}
		// Generate the zip files.
		for (String zoneNo: zoneMap.keySet()) {
			Zone zone = zoneMap.get(zoneNo);
			String message = String.format("%nZone no: %s name %s", zone.getZoneNo(), zone.getZoneName());
			logger.info(message);
			System.out.println(message);
			// OLD
			//List<MuniFiles> zoneFiles = zone.getZoneBallotFiles();
			// NEW
			List<MuniFiles> zoneFiles = zoneMuniFiles.get(zone);
			File zipFilePath = new File(Initialize.outDirPath + File.separator + "zone" + zoneNo + ZIP);
			message = "Zip file: " + zipFilePath;
			logger.info(message);
			System.out.println(message);
			try (ZipOutputStream zipOut = new ZipOutputStream(new FileOutputStream(zipFilePath));)
			{
				zipOut.setComment(String.format("Sample ballot files for zone #%s.", zoneNo));
				for (MuniFiles muniFiles: zoneFiles) {
					addToZipOut(zipOut, muniFiles.getDocxFile());
					addToZipOut(zipOut, muniFiles.getPdfFile());
					addToZipOut(zipOut, muniFiles.getTxtFile());
					// precinct 356 workaround
					addToZipOut(zipOut, muniFiles.getDocxFile2());
					addToZipOut(zipOut, muniFiles.getPdfFile2());
					addToZipOut(zipOut, muniFiles.getTxtFile2());
				}
				
			} catch (Exception ex) {
				logger.error("Zip error: " + ex.getMessage());
			}
		}
	}
	// Get a path to the file in inDir.
	static String pathForZipFile(String fileName) {
		return Initialize.inDirPath + File.separator + fileName;
	}
	// Add a file to the zip file.
	static void addToZipOut(ZipOutputStream zipOut, String fileName) {
		if (fileName == null || fileName.isBlank()) {
			return;
		}
		String message = "Zipping file: " + fileName;
		logger.info(message);
		System.out.println(message);
		String filePath = pathForZipFile(fileName);
		File file = new File(filePath);
		try (FileInputStream fis = new FileInputStream(file);) {
	        ZipEntry zipEntry = new ZipEntry(fileName);
	        zipOut.putNextEntry(zipEntry);
	
	        byte[] bytes = new byte[1024];
	        int length;
	        while((length = fis.read(bytes)) >= 0) {
	            zipOut.write(bytes, 0, length);
	        }
	    } catch (Exception ex) {
	    	logger.error("Zipping file error: " + ex.getMessage());
	    }
	}

}
