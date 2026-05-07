package com.rodini.specimenextractor;

import java.awt.Rectangle;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.text.PDFTextStripperByArea;

import com.rodini.ballotutils.Utils;
import static com.rodini.specimenextractor.Initialize.*;

public class SpecimenExtractor {
	private static final Logger logger = LogManager.getRootLogger();

	private SpecimenExtractor() {}
	static final String ENV_BALLOTGEN_VERSION = "BALLOTGEN_VERSION";
	// specimenPageCount is the # pages in the VS specimen.
	public static int specimenPageCount;
	// specimenText is the extracted text from VS specimen.
	public static String specimenText;
	// Below are used in the Specimen_Summary.txt report.
	public static String page1Row2Line = "";
	public static String page1Col1FirstLine = "";
	public static String page1Col2FirstLine = "";
	public static String page1Col3FirstLine = "";
	public static String page2Col1FirstLine = "";
	public static String page2Col2FirstLine = "";
	public static String page2Col3FirstLine = "";
	// VARIOUS string indicates that first line varies over the specimen columns.
	public static final String VARIOUS = "<various>";
	
	
	public static void main(String[] args){
		// Get the logging level from JVM parameter on command line.
		Utils.setLoggingLevel(LogManager.getRootLogger().getName());
		String version = Utils.getEnvVariable(ENV_BALLOTGEN_VERSION, true);
		String msg = String.format("Start of SpecimenExtractor app. Version: %s", version);
		Utils.logAppMessage(logger, msg, true);
		Initialize.start(args);	
		specimenText = extractSpecimenText(Initialize.specimenFilePath);		
		Terminate.start();
		Utils.logAppErrorCount(logger);
		msg = "End of SpecimenExtractor app.";
		Utils.logAppMessage(logger, msg, true);
	}
	
	static String extractSpecimenText(String filePath) {
		String specimenText = "";
		List<String> pageLayoutProps;
		Map<String, Rectangle> pageLayoutRects;
		try (PDDocument document = Loader.loadPDF(new File(filePath)))
        {
			specimenPageCount = document.getNumberOfPages();
			logger.info(String.format("specimen has %d pages.", specimenPageCount));
			for (int pageNo = 0; pageNo < specimenPageCount; pageNo++) {
				logger.info(String.format("extracting page %d.", pageNo));
				if (pageNo % 2 == 0) {
					logger.debug("Using page 1 layout.");
					pageLayoutProps = Initialize.specimenLayoutPage1Props;
					pageLayoutRects = Initialize.specimenLayoutPage1Rects;
				} else {
					logger.debug("Using page 2 layout.");
					pageLayoutProps = Initialize.specimenLayoutPage2Props;
					pageLayoutRects = Initialize.specimenLayoutPage2Rects;
				}
				String pageText = extractPageText(pageNo, document.getPage(pageNo), pageLayoutProps, pageLayoutRects);
				logger.debug(String.format("page %d text:", pageNo));
				logger.debug(pageText);
	        	if (!pageText.isBlank()) {
	        		specimenText += pageText;
	        	}
			}
        } catch (Exception ex) {
        	logger.error(String.format("extractSpecimenText: pdfbox message: %s.", ex.getMessage()));
        }
		return specimenText;
	}
	
	static String extractPageText(int pageNo, PDPage page, List<String> pageLayoutProps, Map<String, Rectangle> pageLayoutRects) {
		String pageText = "";
		// Extract text in BallotGen order.
		try {
	        PDFTextStripperByArea stripper = new PDFTextStripperByArea();
	        stripper.setSortByPosition( true );
	        for (String layoutProp: pageLayoutProps) {
	        	Rectangle layoutRect = pageLayoutRects.get(layoutProp);
	        	logger.info(String.format("Extracting region: %s with rect: %s", layoutProp, layoutRect));
	        	stripper.addRegion(layoutProp, layoutRect);
	        	stripper.extractRegions(page);
	        	String regionText = stripper.getTextForRegion(layoutProp);
	        	regionText = stripTrailingText(regionText);      	
	        	logger.debug(String.format("Region text: %s", regionText));
	        	if (pageNo == 0 && layoutProp.equals(Initialize.PROP_PAGE1_ROW2_RECT)) {
	        		// reformat so as to print on one line
	        		page1Row2Line = regionText.replace("\n", "\\n");
	        	} else if (pageNo == 0) {
	        		String firstLine = extractFirstLine(regionText);
	        		assignSpecimenFirstLine(layoutProp, firstLine);
	        	} else if (pageNo > 0 ) {
	        		String anotherFirstLine = extractFirstLine(regionText);
	        		updateSpecimenFirstLine(layoutProp, anotherFirstLine);
	        	}
	        	if (!regionText.isBlank()) {
	        		pageText += regionText;
	        	}
	        }
		
        } catch (Exception ex) {
        	logger.error(String.format("extractPageText: pdfbox message: %s.", ex.getMessage()));
        }
		return pageText;
	}
	
	static String stripTrailingText(String text) {
		String [] lines =  text.split("\n");
		String strippedText = Arrays.stream(lines).map(line -> line.stripTrailing()).collect(Collectors.joining("\n")) + "\n";
		return strippedText;
	}
	
	static String extractFirstLine(String regionText) {
		String [] lines = regionText.split("\n");
		String firstLine = "";
		if (lines.length > 0) {
			firstLine = lines[0];
		}
		return firstLine;
	}
	
	static void assignSpecimenFirstLine(String layoutProp, String firstLine) {
		logger.debug(String.format("First line of %s: %s", layoutProp, firstLine));
		switch (layoutProp) {
		case PROP_PAGE1_COL1_RECT -> page1Col1FirstLine = firstLine;
		case PROP_PAGE1_COL2_RECT -> page1Col2FirstLine = firstLine;
		case PROP_PAGE1_COL3_RECT -> page1Col3FirstLine = firstLine;
		case PROP_PAGE2_COL1_RECT -> page2Col1FirstLine = firstLine;
		case PROP_PAGE2_COL2_RECT -> page2Col2FirstLine = firstLine;
		case PROP_PAGE2_COL3_RECT -> page2Col3FirstLine = firstLine;
		}		
	}
	
	static void updateSpecimenFirstLine(String layoutProp, String anotherFirstLine) {
		logger.debug(String.format("Update first line of %s: %s", layoutProp, anotherFirstLine));
 		switch (layoutProp) {
		case PROP_PAGE1_COL1_RECT -> {if (!page1Col1FirstLine.equals(anotherFirstLine)) page1Col1FirstLine = VARIOUS;}
		case PROP_PAGE1_COL2_RECT -> {if (!page1Col2FirstLine.equals(anotherFirstLine)) page1Col2FirstLine = VARIOUS;}
		case PROP_PAGE1_COL3_RECT -> {if (!page1Col3FirstLine.equals(anotherFirstLine)) page1Col3FirstLine = VARIOUS;}
		case PROP_PAGE2_COL1_RECT -> {if (!page2Col1FirstLine.equals(anotherFirstLine)) page2Col1FirstLine = VARIOUS;}
		case PROP_PAGE2_COL2_RECT -> {if (!page2Col2FirstLine.equals(anotherFirstLine)) page2Col2FirstLine = VARIOUS;}
		case PROP_PAGE2_COL3_RECT -> {if (!page2Col3FirstLine.equals(anotherFirstLine)) page2Col3FirstLine = VARIOUS;}
    	}
		
	}
}
