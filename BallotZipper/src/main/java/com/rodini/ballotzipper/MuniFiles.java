package com.rodini.ballotzipper;
/**
 * MuniFiles class is a holder for the 3 files expected from the ballot generation process.
 * E.g. 014_Birmingham_1.docx, 014_Birmingham_1_VS.pdf, 014_Birmingham_1_VS.txt
 * 
 * Notes:
 * - workaround for Chester Co. precinct 356 special case (precinct w/ two ballots)
 *   requires 6 files.
 * 
 */
import java.util.List;

//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class MuniFiles {
	static final Logger logger = LogManager.getLogger(MuniFiles.class);
	
	final String  DOCX = ".docx";
	final String  TXT  = ".txt";
	final String  PDF  = ".pdf";
	final int LISTSIZE = 3;
	
	private String docxFileName;
	private String txtFileName;
	private String pdfFileName;
	private String docxFileName2;
	private String txtFileName2;
	private String pdfFileName2;
	// Constructor
	public MuniFiles(List<String> fileNames) {
		if (fileNames == null) {
			logger.error("fileNames list is null.");
			return;
		}
		checkSize(fileNames);
		checkExtensions(fileNames);
	}
	// Check that there are exactly 3 files.
	void checkSize(List<String> fileNames) {
		int size = fileNames.size();
		if (size < 3 || size > 3) {
			logger.error(String.format("fileNames should have size %d but has size %d.", LISTSIZE, size));
		}
	}
	// Check that the files have the correct extension
	void checkExtensions(List<String> fileNames) {
		boolean haveDocx = false;
		boolean haveTxt = false; 
		boolean havePdf = false;
		for (int i = 0; i < fileNames.size(); i++) {
			String fileName = fileNames.get(i);
			String ext = fileName.substring(fileName.indexOf("."), fileName.length());
			switch (ext) {
			case DOCX:
				if (haveDocx) {
					logDuplicateExt(DOCX, fileName);
					docxFileName2 = fileName;
				} else {
					docxFileName = fileName;
					haveDocx = true;
				}
				break;
			case PDF: 
				if (havePdf) {
					logDuplicateExt(PDF, fileName);
					pdfFileName2 = fileName;
				} else {
					pdfFileName = fileName;
					havePdf = true;
				}
				break;
			case TXT: 
				if (haveTxt) {
					logDuplicateExt(TXT, fileName);
					txtFileName2 = fileName;
				} else {
					txtFileName = fileName;
					haveTxt = true;
				}
				break;
			default:
				logger.error("bad extension for file: " + fileName);
			}
		}
	}
	// Log an error if there's a file with a duplicated extension.
	void logDuplicateExt(String ext, String fileName) {
		logger.error(String.format("duplicate %s file: %s", ext, fileName));
	}
	// Standard toString() method
	public String toString() {
		StringBuffer sb = new StringBuffer("MuniFiles: ");
		sb.append(docxFileName + ", ");
		sb.append(pdfFileName  + ", ");
		sb.append(txtFileName);		
		return sb.toString();
	}
	// Get file ending with .docx
	String getDocxFile() {
		return docxFileName;
	}
	// Get file ending with .pdf
	String getPdfFile() {
		return pdfFileName;
	}
	// Get file ending with .txt
	String getTxtFile() {
		return txtFileName;
	}
	// Get second file ending with .docx
	String getDocxFile2() {
		return docxFileName2;
	}
	// Get second file ending with .pdf
	String getPdfFile2() {
		return pdfFileName2;
	}
	// Get second file ending with .txt
	String getTxtFile2() {
		return txtFileName2;
	}

}
