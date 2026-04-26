package com.rodini.specimenextractor;

import java.awt.Rectangle;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.common.PDRectangle;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.rodini.ballotutils.Utils;
/** 
 * Initialize class gets the program ready to extract text from VS sample PDF file
 * It attempts to validate critical inputs and FAIL EARLY if things are amiss.
 * See Word document SpecimenExtractor.docx for more details.
 * 
 * Note: Many global variables valued here.
 * 
 * @author Bob Rodini
 *
 */
public class Initialize {
	private static final Logger logger = LogManager.getLogger(Initialize.class);
	public  static final String PROP_RESOURCE_PATH = "./resources/";
	public  static final String PROP_PROPS_FILE = "./specimenextractor.properties";
	
	public  static final String PROP_PAGE1_ROW1_RECT = "page1.row1.rect";
	public  static final String PROP_PAGE1_ROW2_RECT = "page1.row2.rect";
	public  static final String PROP_PAGE1_ROW3_RECT = "page1.row3.rect";
	public  static final String PROP_PAGE1_COL1_RECT = "page1.col1.rect";
	public  static final String PROP_PAGE1_COL2_RECT = "page1.col2.rect";
	public  static final String PROP_PAGE1_COL3_RECT = "page1.col3.rect";
	public  static final String PROP_PAGE2_COL1_RECT = "page2.col1.rect";
	public  static final String PROP_PAGE2_COL2_RECT = "page2.col2.rect";
	public  static final String PROP_PAGE2_COL3_RECT = "page2.col3.rect";
	
	public  static Rectangle page1Row1Rect;
	public  static Rectangle page1Row2Rect;
	public  static Rectangle page1Row3Rect;
	public  static Rectangle page1Col1Rect;
	public  static Rectangle page1Col2Rect;
	public  static Rectangle page1Col3Rect;
	public  static Rectangle page2Col1Rect;
	public  static Rectangle page2Col2Rect;
	public  static Rectangle page2Col3Rect;
	
	// Order is important here. Text extraction follows this order.
	public static List<String> specimenLayoutProps = List.of(
			PROP_PAGE1_ROW1_RECT,
			PROP_PAGE1_ROW2_RECT,
			PROP_PAGE1_ROW3_RECT,
			PROP_PAGE1_COL1_RECT,
			PROP_PAGE1_COL2_RECT,
			PROP_PAGE1_COL3_RECT,
			PROP_PAGE2_COL1_RECT,
			PROP_PAGE2_COL2_RECT,
			PROP_PAGE2_COL3_RECT
			);
	
	public static Map<String, Rectangle> specimenLayoutRects = new HashMap<>();
	// Had to use static initializer since Map.of creates immutable map.
	static {
		specimenLayoutRects.put(PROP_PAGE1_ROW1_RECT, page1Row1Rect);
		specimenLayoutRects.put(PROP_PAGE1_ROW2_RECT, page1Row2Rect);
		specimenLayoutRects.put(PROP_PAGE1_ROW3_RECT, page1Row3Rect);
		specimenLayoutRects.put(PROP_PAGE1_COL1_RECT, page1Col1Rect);
		specimenLayoutRects.put(PROP_PAGE1_COL2_RECT, page1Col2Rect);
		specimenLayoutRects.put(PROP_PAGE1_COL3_RECT, page1Col3Rect);
		specimenLayoutRects.put(PROP_PAGE2_COL1_RECT, page2Col1Rect);
		specimenLayoutRects.put(PROP_PAGE2_COL2_RECT, page2Col2Rect);
		specimenLayoutRects.put(PROP_PAGE2_COL3_RECT, page2Col3Rect);
	}
	
	public  static String specimenFilePath;
	private static Properties specimenExtractorProps;
	// Expect legal size paper.
	public static float EXPECTED_PDF_PAGE_WIDTH =   612.0f; // 8.5"
	public static float EXPECTED_PDF_PAGE_HEIGHT = 1008.0f; //  14"
	public static float specimenPageWidth;
	public static float specimenPageHeight;
	
	/**
	 * validateCommandLineArgs checks that there are at least 1 CLI arg
	 * args[0] - specimen file path.
	 * 
	 * @param args command line arguments
	 */
	/* private */
	static void validateCommandLineArgs(String [] args) {
		// check for 1 command arg
		if (args == null || args.length < 1) {
			Utils.logFatalError("missing CLI arguments:\n" +
					"args[0]: specimen PDF file.");
		} else {
			String msg0 = String.format("specimen PDF: %s", args[0]);
			System.out.println(msg0);
			logger.info(msg0);
		}
		specimenFilePath = args[0];
		processSpecimenFilePath(specimenFilePath);
	}
	/**
	 * processSpecimenFilePath validates the that filePath leads to
	 * a PDF file.
	 * 
	 * @param filePath specimen file path.
	 */
	static void processSpecimenFilePath(String filePath) {
		if (!Files.exists(Path.of(filePath))) {				
			Utils.logFatalError("invalid args[0] value, not a file: " + filePath);
		}
		if (!filePath.endsWith(".pdf")) {
			Utils.logFatalError(String.format("specimen file should end with \"pdf\": %s", filePath));
		}
	}
	/**
	 * validateLayoutProperties reads the Rectangle values for areas of the
	 * standard layout of the specimen file.
	 * 
	 * Notes: 
	 * 1. The layout is subject to change. Each specimen published by
	 * Voter Services should be checked to ensure that the properties values
	 * are still correct.
	 * 2. Only syntactic validation here.
	 */
	static Map<String, Rectangle> validateLayoutProperties(Properties props, List<String> layoutProps) {
		Map<String, Rectangle> layoutRects = new HashMap<String, Rectangle>();
		for (String layoutProp: layoutProps) {
			String propValue = Utils.getPropValue(props, layoutProp);
			if (propValue == null) {
				logger.error(String.format ("property: %s must be defined", layoutProp));
			} else {
				Rectangle rect = layoutRects.get(layoutProp);
				if (rect != null) {
					logger.error(String.format ("property: %s is defined twice", layoutProp));
				} else {
					// Now store some Rectangle object.
					layoutRects.put(layoutProp, validateLayoutProperty(layoutProp, propValue));
				}
			}
		}
		// Echo values to the log;
		for (String layoutProp: layoutProps) {
			logger.info(String.format("%s=%s", layoutProp, layoutRects.get(layoutProp)));
		}
		return layoutRects;
	}
	
	static Rectangle validateLayoutProperty(String propName, String propValue) {
		Rectangle rect = null;
		if (propValue != null) {
			String [] propElements = propValue.split(",");
			int len = propElements.length;
			if (len != 4) {
				logger.error(String.format ("property: %s should have 4 values. Value: %s", propName, propValue));
			} else {
				rect = validateRectValues(propName, propElements);
			}
		}
		return rect;
	}
	
	static Rectangle validateRectValues(String propName, String [] rectStrValues) {
		Rectangle rect;
		int [] rectValues = {0, 0, 0, 0};
		for (int i=0; i < 4; i++) {
			String strValue = rectStrValues[i].trim();
			try {
				int val = Integer.parseInt(strValue);
				if (val < 0) {
					logger.error(String.format ("property: %s has negative value: %s", propName, strValue));
					break;
				}
				rectValues[i] = val;
			} catch (NumberFormatException ex) {
				logger.error(String.format ("property: %s has non-numeric value: %s", propName, strValue));
				break;
			}
		}
		rect = new Rectangle(rectValues[0], rectValues[1], rectValues[2], rectValues[3]);
		return rect;
	}
	
	static void validatePDFSize(String filePath) {
		try (PDDocument doc = Loader.loadPDF(new File(filePath))) {
			PDPage page = doc.getPage(0);
			PDRectangle mediaBox = page.getMediaBox();

			specimenPageWidth  = mediaBox.getWidth();
			specimenPageHeight = mediaBox.getHeight();
			if (specimenPageWidth != EXPECTED_PDF_PAGE_WIDTH || specimenPageHeight != EXPECTED_PDF_PAGE_HEIGHT) {
				logger.error(String.format ("Expected page size of 612x1008 but got %4.1fx%4.1f",specimenPageWidth, specimenPageHeight));
			}	
			doc.close();
		} catch (IOException e) {
			// already checked for missing file
			logger.error(String.format ("error on PDF size check: %s", e.getMessage()));
		}
	}
	
	public static void start(String [] args) {
		specimenExtractorProps = Utils.loadProperties(PROP_RESOURCE_PATH + PROP_PROPS_FILE);
		validateCommandLineArgs(args);
		specimenLayoutRects = validateLayoutProperties(specimenExtractorProps, specimenLayoutProps);
		validatePDFSize(specimenFilePath);
	}
}
