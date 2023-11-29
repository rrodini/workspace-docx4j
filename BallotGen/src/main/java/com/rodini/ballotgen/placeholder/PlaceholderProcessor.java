package com.rodini.ballotgen.placeholder;

import static com.rodini.ballotgen.placeholder.PlaceholderLocation.*;

import java.util.ArrayList;
import java.util.List;

import org.docx4j.TraversalUtil;
import org.docx4j.TraversalUtil.CallbackImpl;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.JaxbXmlPart;
import org.docx4j.openpackaging.parts.WordprocessingML.FooterPart;
import org.docx4j.openpackaging.parts.WordprocessingML.HeaderPart;
import org.docx4j.openpackaging.parts.WordprocessingML.MainDocumentPart;
import org.docx4j.wml.P;
import org.docx4j.wml.Text;

//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import static com.rodini.ballotgen.GenDocxBallot.PLACEHOLDER_CONTESTS;
import static com.rodini.ballotgen.GenDocxBallot.PLACEHOLDER_REFERENDUMS;
import static com.rodini.ballotgen.GenDocxBallot.PLACEHOLDER_RETENTIONS;

/**
 * PlaceholderProcessor classes does these functions
 * 1. Scans document parts looking for references to placeholder strings.
 * 2. Creates Placeholder objects for actual references to placeholder strings.
 * 3. Provides and API to replace placeholder references with actual values.
 * 
 * Notes:
 * - This is the only class that knows how to find and replace using DOCX4J.
 * 
 * @author Bob Rodini
 *
 */
public class PlaceholderProcessor {
	private static final Logger logger = LogManager.getLogger(PlaceholderProcessor.class);

	private final MainDocumentPart bodyPart;
	private final HeaderPart headerPart;
	private final FooterPart footerPart;
	// below are populated by initial scan.
	private List<Placeholder> headerPlaceholders;
	private List<Placeholder> bodyPlaceholders;
	private List<Placeholder> footerPlaceholders;
	/**
	 * constructor populates the document parts then scans them for placeholder references.
	 * 
	 * @param docx DOCX4J document
	 * @param placeholderNames names of supported placeholders.
	 */
	public PlaceholderProcessor(WordprocessingMLPackage docx, List<String> placeholderNames) {
		headerPart = docx.getDocumentModel().getSections().get(0).getHeaderFooterPolicy().getDefaultHeader();
		bodyPart = docx.getMainDocumentPart();
		footerPart = docx.getDocumentModel().getSections().get(0).getHeaderFooterPolicy().getDefaultFooter();
		// create placeholder lists
		createPlaceholders(placeholderNames);
		validatePlaceholders();
	}
	/**
	 * createPlaceholders scans the document parts and creates Placeholder objects for supported placeholders.
	 * 
	 * @param placeholderNames list of supported placeholder names.
	 */
	private void createPlaceholders(List<String> placeholderNames) {
		headerPlaceholders = new ArrayList<>();
		bodyPlaceholders = new ArrayList<>();
		footerPlaceholders = new ArrayList<>();
		Placeholder ph;
		for (String name: placeholderNames) {
			//logger.info("Searching HEADER for placeholder: " + name);
			ph = createPlaceholder(HEADER, headerPart, name);
			if (ph != null) {
				headerPlaceholders.add(ph);
			}
			//logger.info("Searching BODY for placeholder: " + name);
			ph = createPlaceholder(BODY, bodyPart, name);
			if (ph != null) {
				bodyPlaceholders.add(ph);
			}
			//logger.info("Searching FOOTER for placeholder: " + name);
			ph = createPlaceholder(FOOTER, footerPart, name);
			if (ph != null) {
				footerPlaceholders.add(ph);
			}
		}
	}
	/**
	 * createPlaceholder creates a new Placeholder object.
	 * 
	 * @param loc location of object.
	 * @param part document part containing placeholder.
	 * @param placeholderName symbolic name of placeholder.
	 * @return new Placeholder object.
	 */
	private Placeholder createPlaceholder(PlaceholderLocation loc, JaxbXmlPart part, String placeholderName) {
		Placeholder ph = null;
		P paragraph = findParagraphByPlaceholder(part, placeholderName);
		if (paragraph != null) {
			//logger.info("Creating placeholder: " + placeholderName);
			ph = new Placeholder(placeholderName, loc, paragraph);
		}
		return ph;
	}
	/**
	 * validatePlaceholders makes sure that "Contests" doesn't appear in header / footer
	 * and that is does appear in body.
	 */
	private void validatePlaceholders() {
		boolean bodyHasContests = false;
		for (Placeholder ph: headerPlaceholders) {
			String phName = ph.getName();
			if (phName.equals(PLACEHOLDER_CONTESTS) ||
				phName.equals(PLACEHOLDER_REFERENDUMS) ||
				phName.equals(PLACEHOLDER_RETENTIONS)) {
				logger.error("placeholder \"" + phName + "\" cannot be in header area");
				// remove Placeholder object since generation will fail.
				headerPlaceholders.remove(ph);
			}
		}
		for (Placeholder ph: bodyPlaceholders) {
			if (ph.getName().equals(PLACEHOLDER_CONTESTS)) {
				bodyHasContests = true;
			}
		}
		if (!bodyHasContests) {
			logger.error("placeholder \"" + PLACEHOLDER_CONTESTS + "\" missing from body area");
		}
		for (Placeholder ph: footerPlaceholders) {
			String phName = ph.getName();
			if (phName.equals(PLACEHOLDER_CONTESTS) ||
				phName.equals(PLACEHOLDER_REFERENDUMS) ||
				phName.equals(PLACEHOLDER_RETENTIONS)) {
				logger.error("placeholder \"" + phName + "\" cannot be in footer area");
				// remove Placeholder object since generation will fail.
				footerPlaceholders.remove(ph);
			}
		}
	}
	/** 
	 * getHeaderPlaceholders returns the list of Placeholder objects in the Header.
	 * @return list of Placeholder objects.
	 */
	public List<Placeholder> getHeaderPlaceholders() {
		return headerPlaceholders;
	}
	/** 
	 * getHeaderPlaceholders returns the list of Placeholder objects in the Body.
	 * @return list of Placeholder objects.
	 */
	public List<Placeholder> getBodyPlaceholders() {
		return bodyPlaceholders;
	}
	/** 
	 * getHeaderPlaceholders returns the list of Placeholder objects in the Footer.
	 * @return list of Placeholder objects.
	 */
	public List<Placeholder> getFooterPlaceholders() {
		return footerPlaceholders;
	}
	/** 
	 * findParagraphByPlaceholder - traverses the DOCX object model for P objects
	 * and then returns the first P whose text matches the placeholder name.
	 * 
	 * @param part document part with object model.
	 * @param placeholderName symbolic name.
	 * 
	 * @return null or "found" paragraph.
	 */
	private P findParagraphByPlaceholder(JaxbXmlPart<?> part, String placeholderName) {
		Finder pFinder = new Finder(P.class);
		new TraversalUtil (part, pFinder);
		List<Object> paragraphs  = pFinder.results;
		P paragraph = null;
		for (Object p: paragraphs) {
			Finder textFinder = new Finder(Text.class);
			new TraversalUtil (p, textFinder);
			List<Object> texts = textFinder.results;
			for (Object text: texts) {
				Text t = (Text) text;
				String s = t.getValue();
				//logger.info("Found text: " + s);
				if (s.equals(placeholderName)) {
					logger.info("Found placeholder: " + placeholderName);
					paragraph = (P) p;
					break;
				}
			}
		}
		return paragraph;
	}
	/**
	 * Finder static class as dictated by the DOCX4J API.
	 *
	 */
	private static class Finder extends CallbackImpl {
		Class<?> typeToFind;
		public List<Object> results = new ArrayList<Object>();
		Finder (Class<?> typeToFind) {
			this.typeToFind = typeToFind;
		}
		@Override
		public List<Object> apply(Object obj) {
			if (obj.getClass().equals(typeToFind)) {
				results.add(obj);
			}
			return null;
		}
	}
	/**
	 * getContent get the content of a document part.
	 * Note:
	 * - need this to do replacement.
	 * 
	 * @param loc which document part.
	 * @return content of that part.
	 */
	private List<Object> getContent(PlaceholderLocation loc) {
		List<Object> content = null;
		switch (loc) {
		case HEADER:
			content = headerPart.getContent();
			break;
		case BODY:
			content = bodyPart.getContent();
			break;
		case FOOTER:
			content = footerPart.getContent();
			break;
		}
		return content;
	}
	/**
	 * replaceContent replaces the placeholder paragraph with a new "value" paragraph.
	 * 
	 * @param ph Placeholder object.
	 * @param contentList new content to replace placeholder paragraph (P).
	 */
	public void replaceContent(Placeholder ph, List<P> contentList) {
		PlaceholderLocation loc = ph.getLoc();		
		P replaceParagraph = ph.getReplaceParagraph();
		List<Object> oldContentList = getContent(loc);
		int replaceIndex = oldContentList.indexOf(replaceParagraph);
		if (replaceIndex < 0) {
			logger.error(String.format("can't insert new content for placeholder %s - replace paragraph not found", ph.getName()));
		}
		// Insert new content
		oldContentList.addAll(replaceIndex, contentList);
		// Remove the placeholder paragraph
		oldContentList.remove(replaceParagraph);
	}
	
	

	
}
