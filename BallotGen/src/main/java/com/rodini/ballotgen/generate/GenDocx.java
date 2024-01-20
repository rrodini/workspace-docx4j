package com.rodini.ballotgen.generate;

import static com.rodini.ballotgen.common.GenDocxBallot.STYLEID_BOTTOM_BORDER;
import static com.rodini.ballotgen.common.GenDocxBallot.STYLEID_COLUMN_BREAK_PARAGRAPH;
import static com.rodini.ballotgen.common.GenDocxBallot.STYLEID_CONTEST_TITLE;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.docx4j.XmlUtils;
import org.docx4j.dml.wordprocessingDrawing.Inline;
import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.Part;
import org.docx4j.openpackaging.parts.WordprocessingML.BinaryPartAbstractImage;
import org.docx4j.openpackaging.parts.WordprocessingML.MainDocumentPart;
import org.docx4j.wml.BooleanDefaultTrue;
import org.docx4j.wml.Br;
import org.docx4j.wml.Drawing;
import org.docx4j.wml.ObjectFactory;
import org.docx4j.wml.P;
import org.docx4j.wml.PPr;
import org.docx4j.wml.PPrBase;
import org.docx4j.wml.R;
import org.docx4j.wml.RPr;
import org.docx4j.wml.Text;
import org.docx4j.wml.U;

import com.rodini.ballotgen.common.GenDocxBallot.TextStyle;
import com.rodini.ballotgen.common.Initialize;

import jakarta.xml.bind.JAXBElement;
import jakarta.xml.bind.JAXBException;

/**
 * GenDocx class contains static methods for generating low-level Docx4J stuff.
 * Low-level here means that it has no dependencies except for Docx4J itself.
 * 
 * @author Bob Rodini
 *
 */
public class GenDocx {
	private static final Logger logger = LogManager.getLogger(GenDocx.class);
	// Prevent instantiation
	private GenDocx() {
	}
	// Cache for previously loaded zone image logos.
	private static Map<String, byte[]> imageFileMap = new HashMap<>();
	
	/**
	 * genWmlChunk demonstrates the technique of generating
	 * a big chunk of content using Word, then exporting the .xml
	 * using the DOCX4J word add-in, then importing the .xml file.
	 * 
	 * WARNING: Export/import does not work with graphics, hyperlinks, etc.
	 */
	public static void genWmlChunk(MainDocumentPart mdp, String wmlFileName) {
		String text = "";
		try (FileInputStream inStream = new FileInputStream(Initialize.RESOURCE_PATH + wmlFileName);) {
			text = new String(inStream.readAllBytes(), StandardCharsets.UTF_8);
		} catch (IOException e) {
			logger.error("error reading xml chunk: " + wmlFileName);
		}
		Object o = null;
		try {
			o = XmlUtils.unmarshalString(text);
		} catch (JAXBException e) {
			logger.error("can't convert xml to part");
		}
		org.docx4j.wml.Body body = ((org.docx4j.wml.Document) o).getBody();
		List<Object> contents = body.getContent();
		for (Object content: contents) {
			mdp.addObject(content);
		}
	}
	/**
	 * genPageBreak generates the pseudo contest name "PAGE BREAK" using
	 * the wording in the property PAGE_BREAK_WORDING (e.g. "See other side of ballot")
	 * @param mdp MainDocumentPart from DOCX4J API.
	 * @return list of new paragraphs.
	 */
	public static List<P> genPageBreak(MainDocumentPart mdp) {
		List<P> pageBreakParagraphs = new ArrayList<>();
		// Test the property value here
		if (Initialize.PAGE_BREAK_DISPLAY) {
			logger.info("generating page break");
			P newParagraph;
			// first paragraph
			newParagraph = mdp.createStyledParagraphOfText(STYLEID_CONTEST_TITLE, Initialize.PAGE_BREAK_WORDING);
			pageBreakParagraphs.add(newParagraph);
			// Draw a border line as a separator
			newParagraph = mdp.createStyledParagraphOfText(STYLEID_BOTTOM_BORDER,null);
			// last paragraph
			pageBreakParagraphs.add(newParagraph);  // Paragraph separator
		}
		return pageBreakParagraphs;
	}
	/** 
	 * genColumnBreakParagraph generates a column break object tree.
	 * This can be used to "post-format" the columns after a human evaluation as to where
	 * a column break should be injected.
	 * @return paragraph effecting a column break
	 */
	public static P genColumnBreakParagraph(MainDocumentPart mdp) {
		logger.info("generating column break");
		org.docx4j.wml.ObjectFactory wmlObjectFactory = new ObjectFactory();
        // Create object for p
        P p = mdp.createStyledParagraphOfText(STYLEID_COLUMN_BREAK_PARAGRAPH,null);
        // Create object for r
        R r = wmlObjectFactory.createR(); 
        p.getContent().add(r); 
        // Create object for br
        Br br = wmlObjectFactory.createBr(); 
        r.getContent().add(br); 
        br.setType(org.docx4j.wml.STBrType.COLUMN);
        return p;
	}
	/**
	 * genStyledParagraph generates a single paragraph containing the text.
	 * 
	 * @param text of paragraph.
	 * @param oldParagraph placeholder paragraph (P).
	 * @param defaultStyle style to use is oldParagraph has no style.
	 * @param mdp MainDocumentPart.
	 * @return new paragraph.
	 */
	public static P genStyledParagraph(String text, P oldParagraph, String defaultStyle, MainDocumentPart mdp) {		
		PPr pPr = oldParagraph.getPPr();
		String style = defaultStyle;
		if (pPr != null) {
		    PPrBase.PStyle pStyle = pPr.getPStyle();
		    if (pStyle != null) {
		        style = pStyle.getVal();
		    } else {
				logger.info("pStyle value is null - Normal style used.");
		    }
		} else {
			logger.info("pPr value is null - Normal style used.");
		}
		P newParagraph = mdp.createStyledParagraphOfText(style, text);
		return newParagraph;
	}
	/**
	 * genStyledTextWithinParagraph generates a paragraph of text wherein just some of the
	 * text has one of TextStyle (e.g. BOLD) styles.
	 * Notes: Java code generated by DOCX4J Word plugin. Modified by RAR.
	 * 
	 * @param textStyle see TextStyle.
	 * @param unstyledText1 Normal text.
	 * @param styledText styled text.
	 * @param unstyledText2 More Normal text.
	 * @return P object.
	 */
	public static P genStyledTextWithinParagraph(TextStyle textStyle, String unstyledText1, String styledText,
			String unstyledText2) {
		org.docx4j.wml.ObjectFactory wmlObjectFactory = new ObjectFactory();
		P p = wmlObjectFactory.createP();
		// Create object for r
		R r = wmlObjectFactory.createR();
		p.getContent().add(r);
		// Create object for t (wrapped in JAXBElement)
		Text text = wmlObjectFactory.createText();
		JAXBElement<org.docx4j.wml.Text> textWrapped = wmlObjectFactory.createRT(text);
		r.getContent().add(textWrapped);
		text.setValue(unstyledText1);
		text.setSpace("preserve");
		// Create object for r
		R r2 = wmlObjectFactory.createR();
		p.getContent().add(r2);
		// Create object for t (wrapped in JAXBElement)
		Text text2 = wmlObjectFactory.createText();
		JAXBElement<org.docx4j.wml.Text> textWrapped2 = wmlObjectFactory.createRT(text2);
		r2.getContent().add(textWrapped2);
		text2.setValue(styledText);
		// Create object for rPr
		RPr rpr = wmlObjectFactory.createRPr();
		r2.setRPr(rpr);
		if (textStyle == TextStyle.BOLD) {	
			// Create object for b
			BooleanDefaultTrue booleandefaulttrue = wmlObjectFactory.createBooleanDefaultTrue();
			rpr.setB(booleandefaulttrue);
			// Create object for bCs
			BooleanDefaultTrue booleandefaulttrue2 = wmlObjectFactory.createBooleanDefaultTrue();
			rpr.setBCs(booleandefaulttrue2);
		} else if (textStyle == TextStyle.UNDERLINE) {
            // Create object for u
            U u = wmlObjectFactory.createU(); 
            rpr.setU(u); 
            u.setVal(org.docx4j.wml.UnderlineEnumeration.SINGLE);
		}
		// Create object for r
		R r3 = wmlObjectFactory.createR();
		p.getContent().add(r3);
		// Create object for t (wrapped in JAXBElement)
		Text text3 = wmlObjectFactory.createText();
		JAXBElement<org.docx4j.wml.Text> textWrapped3 = wmlObjectFactory.createRT(text3);
		r3.getContent().add(textWrapped3);
		text3.setValue(unstyledText2);
		text3.setSpace("preserve");
		return p;
	}
	/**
	 * readImageFile reads the bytes of an image file.
	 * 
	 * @param filePath path to image file.
	 * @return byte array of the image.
	 */
	private static byte[] readImageFile(String filePath) {
		byte [] bytes = null;
		if (imageFileMap.containsKey(filePath)) {
			bytes = imageFileMap.get(filePath);
		} else {
			try {
				bytes = Files.readAllBytes(Paths.get(filePath));
			} catch (IOException e) {
				logger.error("IOException reading image file: " + filePath + "message: " + e.getMessage());			
			}
		}
		return bytes;
	}
	/**
	 * genImageInline generates an Inline object.
	 * 
	 * @param bytes image file contents as bytes.
	 * Notes: see https://javadoc.io/doc/org.docx4j/docx4j/3.3.2/org/docx4j/openpackaging/parts/WordprocessingML/BinaryPartAbstractImage.html
	 * 
	 * @return org.docx4j.dml.wordprocessingDrawing.InLine object.
	 */
	private static Inline genImageInline(String zoneLogoPath, WordprocessingMLPackage docx, Part sourcePart)  {
		byte [] bytes = readImageFile(zoneLogoPath);
		BinaryPartAbstractImage imagePart = null;
		Inline inline = null;
		try {
			imagePart = BinaryPartAbstractImage.createImagePart(docx, sourcePart, bytes);
			inline = imagePart.createImageInline("ZoneLogo", "ZoneLogo", 
					 docx.getDrawingPropsIdTracker().generateId(),
					 1, false);
			logger.info("generated imagePart using sourcePart: " + sourcePart);
		} catch (Docx4JException e) {
			logger.error("Docx4JException generating imagePart message: " + e.getMessage());
		} catch (Exception e) {
			logger.error("Generic Exception generating imagePart message: " + e.getMessage());
		}
		return inline;
	}
	/** 
	 * genImageParagraph generates the wml objects needed for an image insertion.
	 *  
	 * @param zoneLogoPath file path to image file.
	 * @param docx WordprocessingMLPackage object.
	 * @param sourcePart Header/Main/Footer part
	 * 
	 * @return paragraph object containing image.
	 */
	public static P genImageParagraph(String zoneLogoPath, WordprocessingMLPackage docx, Part sourcePart) {
		P paragraph = null;
		Inline inline = null;
		inline = genImageInline(zoneLogoPath, docx, sourcePart);
		if (inline != null) {
			org.docx4j.wml.ObjectFactory wmlObjectFactory = new ObjectFactory();
			paragraph = wmlObjectFactory.createP();
			R run = wmlObjectFactory.createR();
			paragraph.getContent().add(run);
			Drawing drawing = wmlObjectFactory.createDrawing();
			run.getContent().add(drawing);
			drawing.getAnchorOrInline().add(inline);
		}
		return paragraph;
	}
	
}
