package com.rodini.ballotgen.generate;

import static com.rodini.ballotgen.common.GenDocxBallot.STYLEID_BOTTOM_BORDER;
import static com.rodini.ballotgen.common.GenDocxBallot.STYLEID_COLUMN_BREAK_PARAGRAPH;
import static com.rodini.ballotgen.common.GenDocxBallot.STYLEID_VOTE_BOTH_SIDES;

import java.io.File;
import java.io.IOException;
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
import org.docx4j.jaxb.Context;
import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.Part;
import org.docx4j.openpackaging.parts.WordprocessingML.BinaryPartAbstractImage;
import org.docx4j.openpackaging.parts.WordprocessingML.MainDocumentPart;
import org.docx4j.openpackaging.parts.relationships.Namespaces;
import org.docx4j.wml.BooleanDefaultTrue;
import org.docx4j.wml.Br;
import org.docx4j.wml.Drawing;
import org.docx4j.wml.ObjectFactory;
import org.docx4j.wml.P;
import org.docx4j.wml.P.Hyperlink;
import org.docx4j.wml.PPr;
import org.docx4j.wml.PPrBase;
import org.docx4j.wml.R;
import org.docx4j.wml.RPr;
import org.docx4j.wml.Text;
import org.docx4j.wml.U;

import com.rodini.ballotgen.common.GenDocxBallot.TextStyle;
import com.rodini.ballotgen.common.Initialize;

import jakarta.xml.bind.JAXBElement;

/**
 * GenDocx class contains static methods for generating low-level Docx4J stuff.
 * Low-level here means that it has no dependencies except for Docx4J itself.
 * Most code taken from DOCX4J sample code.
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
	 * genChunk demonstrates the technique of generating
	 * a big chunk of content using Word
	 * 
	 * WARNING: Import does not work with graphics, hyperlinks, etc.
	 */
	public static List<P> genChunk(MainDocumentPart mdp, String chunkFileName) {
		String message = String.format("genChunk: %s%n", chunkFileName);
		logger.debug(message);
		WordprocessingMLPackage docSource = null;
		try {
			docSource=WordprocessingMLPackage.load(new File(chunkFileName));
		} catch (Docx4JException e) {
			logger.error(message);
			logger.error("Docx4JException: %s%n", e.getMessage());
		}
		List<Object> objects = docSource.getMainDocumentPart().getContent();
		List<P> paragraphs = new ArrayList<>();
		// Convert to P objects by downcasting.
		for (Object o: objects) {
			paragraphs.add((P) o);
		}
		return paragraphs;
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
		if (Initialize.pageBreakDisplay) {
			logger.info("generating page break");
			P newParagraph;
			// first paragraph at bottom
			newParagraph = mdp.createStyledParagraphOfText(STYLEID_VOTE_BOTH_SIDES, Initialize.PAGE_BREAK_WORDING);
			pageBreakParagraphs.add(newParagraph);
			// Draw a border line as a separator
			newParagraph = mdp.createStyledParagraphOfText(STYLEID_BOTTOM_BORDER,null);
			// last paragraph
			pageBreakParagraphs.add(newParagraph);  // Paragraph separator
			// 9/7/2024 - generate a column break too
			// For one page ballots don't do this!
//			newParagraph = genColumnBreakParagraph(mdp);
//			pageBreakParagraphs.add(newParagraph);
//			// new paragraph at top
//			newParagraph = mdp.createStyledParagraphOfText(STYLEID_CONTEST_TITLE, Initialize.PAGE_BREAK_WORDING);
//			pageBreakParagraphs.add(newParagraph);
//			// Draw a border line as a separator
//			newParagraph = mdp.createStyledParagraphOfText(STYLEID_BOTTOM_BORDER,null);
//			pageBreakParagraphs.add(newParagraph);
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
			// 09/20/2024 added 6th parameter to size zone logo.
			inline = imagePart.createImageInline("ZoneLogo", "ZoneLogo", 
					 docx.getDrawingPropsIdTracker().generateId(),
					 1, false, 1000);
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
		logger.info(String.format("genImageParagraph: %s%n", zoneLogoPath));
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
	/**
	 * genHyperlink generates the wml objects needed for a hyperlink.
	 * 
	 * @param text seen by user.
	 * @param url address for link.
	 * @param part DocumentPart.
	 * 
	 * @return paragraph of text with underlying hyperlink.
	 */
	public static P genHyperlink(Part part, String text, String url) {
		logger.info(String.format("genHyperlink: \"%s\" %s%n", text, url));
		P paragraph = Context.getWmlObjectFactory().createP();
		Hyperlink link = createHyperlink(part, text, url);
		if (link != null) {
			paragraph.getContent().add(link);
		}
		return paragraph;
	}
	/**
	 * createHyperlink generates objects/wml needed for a hyperlink.
	 * Taken from DOCX4J examples on the internet.
	 * Notes: 
	 * 1) Namespace urls must NOT start httpS!!!
	 * 2) Must have Hyperlink style within document.
	 * 
	 * @param part DocumentPart.
	 * @param url address for link.
	 * 
	 * @return Hyperlink object.
	 */
	public static Hyperlink createHyperlink(Part part, String text, String url) {
		try {
			// We need to add a relationship to word/_rels/document.xml.rels
			// but since its external, we don't use the
			// usual wordMLPackage.getMainDocumentPart().addTargetPart
			// mechanism
			org.docx4j.relationships.ObjectFactory factory =
				new org.docx4j.relationships.ObjectFactory();
			org.docx4j.relationships.Relationship rel = factory.createRelationship();
			rel.setType( Namespaces.HYPERLINK  );
			rel.setTarget(url);
			rel.setTargetMode("External");
			part.getRelationshipsPart().addRelationship(rel);
			// addRelationship sets the rel's @Id
			String hpl = "<w:hyperlink r:id=\"" + rel.getId() + "\" xmlns:w=\"http://schemas.openxmlformats.org/wordprocessingml/2006/main\" " +
					"xmlns:r=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships\" >" +
					"<w:r>" +
					"<w:rPr>" +
					"<w:rStyle w:val=\"Hyperlink\" />" +  // TODO: enable this style in the document!
					"</w:rPr>" +
					"<w:t>" + text + "</w:t>" +
					"</w:r>" +
					"</w:hyperlink>";
			Hyperlink link = (Hyperlink)XmlUtils.unmarshalString(hpl);
			return link;
		} catch (Exception e) {
			logger.error(String.format("createHyperlink: %s%n", e.getMessage()));
			return null;
		}
	}
}
