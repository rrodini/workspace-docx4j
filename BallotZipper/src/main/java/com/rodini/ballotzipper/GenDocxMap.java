package com.rodini.ballotzipper;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * GenDocxMap is the complement to GenMuniMap. It's job is to group the sample ballot files
 * together by precinct no. embedded in the file title.
 * 
 * @author Bob Rodini
 *
 */
public class GenDocxMap {
	static final Logger logger = LogManager.getLogger(GenDocxMap.class);
	
	private static final Map<String, MuniFiles> docxNoMap = new TreeMap<>();
	// Docx No (key)         MuniFiles
	// 020                   020_E_Bradford_N-1.docx
	//                       020_E_Bradford_N-1_VS.txt
	//                       020_E_Bradford_N-1_VS.pdf
	static List<String> fileList = new ArrayList<>();
	// Prevent instantiation.
	private GenDocxMap() {
	}
	// Process file titles found in inDir into docxNoMap.
	static void processInDir() {
		fileList = Stream.of(new File(Initialize.inDirPath).listFiles())
					.filter(file -> !file.isDirectory())
					.map(File::getName)
					.collect(Collectors.toList());
		// This sort is absolutely necessary.
		fileList.sort(null);
		logger.debug(String.format("Files in \"%s\"", Initialize.inDirPath));
		for (String fileName: fileList) {
			logger.debug(fileName);
		}
		// Initialize first prefix.
		String prevMuniNo = fileList.get(0).substring(0, 3);
		for (int i = 0; i < fileList.size(); i++) {
			String fileName = fileList.get(i);
			// Get the precinct no. embedded in file name.
			String muniNo = fileName.substring(0, 3);
			List<String> muniFileList = new ArrayList<>();
			while (prevMuniNo.equals(muniNo) && i < fileList.size()) {
//System.out.printf("prefix: %s fileName: %s%n", muniNo, fileName);
				muniFileList.add(fileName);
				i++;
				if (i < fileList.size()) {
					fileName = fileList.get(i);
					muniNo = fileName.substring(0, 3);
				}
			}
			// Since the above loop put the counter ahead by one.
			i--;
			MuniFiles muniFiles = new MuniFiles(muniFileList);
//System.out.printf("map entry for %s%n", muniNo);
			docxNoMap.put(prevMuniNo, muniFiles);
			prevMuniNo = muniNo;
		}
	}
	// Return docxNoMap to clients.
	public static Map<String, MuniFiles> getDocxNoMap() {
		return docxNoMap;
	}
	// Used only for testing.
	static void clearDocxNoMap() {
		docxNoMap.clear();
	}
}
