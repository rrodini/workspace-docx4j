package com.rodini.specimenextractor;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
/**
 * Terminate class writes all outputs to the file system.
 * @author Bob Rodini
 *
 */
public class Terminate {
	private static final Logger logger = LogManager.getLogger(Terminate.class);
	private static String specimenAbsFilePath;
	private static final String EXTRACTION_REPORT = "Extraction_Summary.txt";
	private Terminate() {}
	/** 
	 * start the termination process.
	 */
	public static void start() {
		specimenAbsFilePath = new File(Initialize.specimenFilePath).getAbsolutePath();
		generateSpecimenText();		
		generateExtractionReport();		
	}
	/**
	 * generateSpecimenText writes the extracted text to the same folder where
	 * the specimen file is found. Of course, it has the extension "txt".
	 */
	static void generateSpecimenText() {
		int index = specimenAbsFilePath.lastIndexOf(".");
		// Chop off .pdf and replace with .txt
		String specimenTextFilePath = specimenAbsFilePath.substring(0, index) + ".txt";
		try {
			Files.writeString(Path.of(specimenTextFilePath), SpecimenExtractor.specimenText);
		} catch (IOException e) {
			logger.error(String.format("genSpecimenText IOException error: %s"), e.getMessage());
		}
	}
	/**
	 * generateExtractionReport writes a short report that summarizes the extraction process.
	 */
	static void generateExtractionReport() {
		String reportPath = Path.of(specimenAbsFilePath).getParent().toString();
		reportPath += File.separator + EXTRACTION_REPORT;
		try (PrintWriter pw = new PrintWriter(new File(reportPath))) {
			String electionInfo = SpecimenExtractor.page1Row2Line;
			pw.write(String.format("specimen file:  %s%n", specimenAbsFilePath));
			pw.write(String.format("specimen pages: %d%n", SpecimenExtractor.specimenPageCount));
			pw.write(String.format("election info:  %s%n", electionInfo));
			pw.write(String.format("page1 col1 first line: %s%n", SpecimenExtractor.page1Col1FirstLine));
			pw.write(String.format("page1 col2 first line: %s%n", SpecimenExtractor.page1Col2FirstLine));
			pw.write(String.format("page1 col3 first line: %s%n", SpecimenExtractor.page1Col3FirstLine));
			pw.write(String.format("page2 col1 first line: %s%n", SpecimenExtractor.page2Col1FirstLine));
			pw.write(String.format("page2 col2 first line: %s%n", SpecimenExtractor.page2Col2FirstLine));
			pw.write(String.format("page2 col3 first line: %s%n", SpecimenExtractor.page2Col3FirstLine));

		} catch (IOException ex) {
			String msg = String.format("IOException writing extraction summary report: %s", ex.getMessage());			
			logger.error(msg);
			System.out.println(msg);
		}
		
	}
	
	
}
