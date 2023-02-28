package com.rodini.ballotgen;

import static java.util.stream.Collectors.joining;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.rodini.ballotgen.ContestFileLevel.*;
import static com.rodini.ballotgen.ElectionType.*;
import com.rodini.ballotutils.Utils;
import com.rodini.zoneprocessor.Zone;

import jakarta.xml.bind.JAXBElement;
import jakarta.xml.bind.JAXBException;

import org.docx4j.Docx4J;
import org.docx4j.XmlUtils;
import org.docx4j.openpackaging.contenttype.ContentTypes;
import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.WordprocessingML.FooterPart;
import org.docx4j.openpackaging.parts.WordprocessingML.HeaderPart;
import org.docx4j.openpackaging.parts.WordprocessingML.MainDocumentPart;
import org.docx4j.TraversalUtil;
import org.docx4j.TraversalUtil.CallbackImpl;
import org.docx4j.wml.P;
import org.docx4j.wml.R;
import org.docx4j.wml.Style;
import org.docx4j.wml.Styles;
import org.docx4j.wml.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
/**
 * GenDocxBallot is the workhorse class that produces a new Word file
 * based on inputs.  Those inputs are:
 * 1. The ballot (text) file for the municipality.
 * 2. The contests (text) file for the municipality.
 * 3. The dotx template file which is referenced by the program's
 *    properties file.
 * Notes:
 * 1. The class is designed to FAIL FAST if initialization fails.
 * 2. The class is designed are THREAD SAFE.  It may not be.
 * 3. private visibility is NOT used so that unit tests can be written. 
 * @author Bob Rodini
 *
 */
public class GenDocxBallot {
	private static final Logger logger = LoggerFactory.getLogger(GenDocxBallot.class);

	// from constructor
	private String dotxPath;  // path to DOTX template file
	private String ballotTextFilePath; // path to ballot text file
	private ContestFileLevel contestLevel; // COMMON or MUNICIPAL
	private String contestsText; // text of contests on this ballot
	// local variables
	private WordprocessingMLPackage docx;  // document under construction
	private String ballotName; // e.g. "005_Atglen" or "750_East_Whiteland_4"
	private String precinctNo; // precinct No. - first three digits of ballotName
	private String contestFilePath; // path to contest file e.g. XYZ_contests.txt
	private static final String FILE_SUFFIX = "_VS";
	private String ballotText; // text of ballotTextFile
	private String fileOutputPath; // generated DOCX file
	private String formatsText;   // formats (regexes) read from properties
	private EndorsementProcessor endorsementProcessor;
	// Placeholders are predefined strings that may be embedded in the template file
	// so they can be located programmatically by this program.
	private static final String PLACEHOLDER_CONTESTS = "Contests";
	private static final String PLACEHOLDER_BALLOTNAME = "BallotName";
	// Styles are pre-defined within the dotx template.
	// There is a chance that this program is out of sync with the template
	// so when the template is loaded the existence of the styles is checked.
	// If style is not found "Normal" style is used.
	private static String STYLEID_CONTEST_TITLE = "ContestTitle";
	private static String STYLEID_CONTEST_INSTRUCTIONS = "ContestInstructions";
	private static String STYLEID_CANDIDATE_NAME = "CandidateName";
	private static String STYLEID_CANDIDATE_PARTY = "CandidateParty";
	private static String STYLEID_ENDORSED_CANDIDATE_NAME = "EndorsedCandidateName";
	private static String STYLEID_BOTTOM_BORDER = "BottomBorder";
	// These are unicode characters (also Segoe UI Symbol font)
	private static final String whiteEllipse = "⬭";
	private static final String blackEllipse = "⬬";
	/** 
	 * GenDocxBallot constructor just saves input references.
	 * @param dotxPath path to the Word template.
	 * @param textFilePath path to the ballot text file.
	 * @param contestsLevel which contests text file to use.
	 * @param formatsText formats (regexes) to use.
	 */
	public GenDocxBallot(String dotxPath, String textFilePath, ContestFileLevel contestLevel,
			String formatsText, EndorsementProcessor ep) {
		this.dotxPath = dotxPath;
		this.ballotTextFilePath = textFilePath;
		this.contestLevel = contestLevel;
		this.formatsText = formatsText;
		this.endorsementProcessor = ep;
	}
	/**
	 * generate generates the contents of the docx file.
	 * Its implementation is subject to change.  In the first few versions
	 * it did not get any content from the Word template.  In later versions
	 * it gets significant content from the template such as:
	 * 1. CCDC Header with graphics.
	 * 2. CCDC voter instructions and candidate info.
	 * 3. CCDC info with graphics,
	 * 
	 * In version 1.3 of BallotGen the Contests and Candidates are inserted
	 * between 2. and 3. above.
	 */
	public void generate() {
		// validate all inputs.
		logger.info("start new docx file");
		initialize();
		// Header
		// If next line is commented out, then template header is used.
		// genHeader(getHeaderContents(), "New Header");
		// Body
		// generate the contests on the ballot as DOCX4J paragraphs.
		List<P> insertParagraphs = genContests();
		P placeholder = findContestsPlaceholder();
		MainDocumentPart mdp = docx.getMainDocumentPart();
		List<Object> contentList = mdp.getContent();
		int removeIndex = contentList.indexOf(placeholder);
		// Insert new paragraphs
		contentList.addAll(removeIndex, insertParagraphs);
		// Remove the placeholder paragraph
		contentList.remove(placeholder);
		// Footer
		genFooter(getFooterContents(), ballotName.replace("_", " "));
		// shutdown cleanly
		terminate();
		logger.info("end new docx file");
	}
	/**
	 * initialize the class for generation of the Word docx ballot. Notes: 1. Try to
	 * FAIL EARLY.
	 */
	/* private */ 
	void initialize() {
		initDocxFile();
		initContestsText();
		initReadBallotText();
		initStyles();
	}
	/**
	 * initDocxFile creates the output docx file from the Word template. A sample
	 * from DOCX4J showed how to do this so I copied it.
	 */
	/* private */ 
	void initDocxFile() {
		File dotxFile = new File(dotxPath);
		logger.info(String.format("dotxFile: %s", dotxPath));
		File textFile = new File(ballotTextFilePath);
		String fileName = textFile.getName();
		String pathName = textFile.getAbsolutePath();
		// pathName includes fileName so strip out fileName
		int lastSeparator = pathName.lastIndexOf(File.separator);
		pathName = pathName.substring(0, lastSeparator);
		logger.info(String.format("pathName: %s fileName: %s", pathName, fileName));
		String[] fileElements = fileName.split("\\.");
		ballotName = fileElements[0];
		// if suffix was added, then remove it.
		if (ballotName.endsWith(FILE_SUFFIX)) {
			ballotName = ballotName.substring(0, ballotName.length() - FILE_SUFFIX.length());
		}
		precinctNo = ballotName.substring(0, 3);
		try {
			int number = Integer.parseInt(precinctNo);
		} catch (NumberFormatException e) {
			logger.error("Ballot Name doesn't start with precinct No. See: " + ballotName);
			precinctNo = "000";
		}
		fileOutputPath = pathName + File.separator + ballotName + ".docx";
		System.out.printf("Generating %s%n", ballotName + ".docx");
		logger.info(String.format("fileOutputPath: %s", fileOutputPath));
		WordprocessingMLPackage dotx = null;
		try {
			dotx = Docx4J.load(dotxFile);
			docx = (WordprocessingMLPackage) dotx.cloneAs(ContentTypes.WORDPROCESSINGML_DOCUMENT);
			// The dotx should be unchanged
			docx.attachTemplate(dotxPath);
			// sample shows a save (with no changes) back to the .dotx file
			// this should be unnecessary unless it loses a system file handle
			// TODO: investigate loss of file handles.
			dotx.save(dotxFile);
		} catch (Docx4JException e) {
			Utils.logFatalError("critical DOCX4J load/clone/attach operation failed.");
		}
	}
	
	void initContestsText() {
		String contestsPath = Initialize.ballotContestsPath + File.separator;
		if (contestLevel == COMMON) {
			contestsPath = contestsPath + Initialize.COMMON_CONTESTS_FILE;
		} else if (contestLevel == MUNICIPAL) {
			contestsPath = contestsPath + ballotName + "_contests.txt";
		} else {
			Utils.logFatalError("contest file level not recognized. See: " + contestLevel.toString());
		}
		logger.debug("Loading contests file: " + contestsPath);
		List<String> contestsLines = null;
		try {
			contestsLines = Files.readAllLines(Path.of(contestsPath));
		} catch (IOException e) {
			Utils.logFatalError("can't read: " + contestsPath);
		}
		contestsText = contestsLines.stream().collect(joining("\n"));
		// prepare logging message
		int len = contestsText.length();
		String msgText = contestsText;
		if (len > 100) {
			msgText = contestsText.substring(0, 50) + "..." + contestsText.substring(len - 50, len);
		}
		logger.debug("contestsText: " + msgText);
	}
	/**
	 * initReadBallotText reads the contents of the ballot text file
	 * into a string.
	 */
	/* private */ 
	void initReadBallotText() {
		ballotText = Utils.readTextFile(ballotTextFilePath);
	}
	/**
	 * initStyles checks that the expected StyleIds (just strings) exist
	 * in the dotx file.  If not, it substitutes "Normal"
	 */
	/* private */ 	
	void initStyles() {
//		List<String> neededIdStyles = List.of(
//				STYLEID_CANDIDATE_NAME,
//				STYLEID_ENDORSED_CANDIDATE_NAME,
//				STYLEID_CANDIDATE_PARTY,
//				STYLEID_CONTEST_INSTRUCTIONS,
//				STYLEID_CONTEST_TITLE
//				);
		List<String> templateIdStyles = new ArrayList<>();
		Styles styles = docx.getMainDocumentPart().getStyleDefinitionsPart().getJaxbElement();
		for (Style style : styles.getStyle()) {
		  templateIdStyles.add(style.getStyleId());
		}
		// Can't refactor below.  Doing so would lose String reference to specific variable.
		if (!templateIdStyles.contains(STYLEID_CANDIDATE_NAME)) {
			logger.error("dotx template missing this styleId: " + STYLEID_CANDIDATE_NAME);
			STYLEID_CANDIDATE_NAME = "Normal";
		}
		if (!templateIdStyles.contains(STYLEID_CANDIDATE_PARTY)) {
			logger.error("dotx template missing this styleId: " + STYLEID_CANDIDATE_PARTY);
			STYLEID_CANDIDATE_PARTY = "Normal";
		}
		if (!templateIdStyles.contains(STYLEID_CONTEST_INSTRUCTIONS)) {
			logger.error("dotx template missing this styleId: " + STYLEID_CONTEST_INSTRUCTIONS);
			STYLEID_CONTEST_INSTRUCTIONS = "Normal";
		}
		if (!templateIdStyles.contains(STYLEID_CONTEST_TITLE)) {
			logger.error("dotx template missing this styleId: " + STYLEID_CONTEST_TITLE);
			STYLEID_CONTEST_TITLE = "Normal";
		}
		if (!templateIdStyles.contains(STYLEID_ENDORSED_CANDIDATE_NAME)) {
			logger.error("dotx template missing this styleId: " + STYLEID_ENDORSED_CANDIDATE_NAME);
			STYLEID_ENDORSED_CANDIDATE_NAME = "Normal";
		}
		if (!templateIdStyles.contains(STYLEID_BOTTOM_BORDER)) {
			logger.error("dotx template missing this styleId: " + STYLEID_BOTTOM_BORDER);
			STYLEID_BOTTOM_BORDER = "Normal";
		}
	}
	/**
	 * terminate performs all cleanup following document generation.
	 */
	/* private */ 
	void terminate() {
		termDocxFile();
	}
	/**
	 * termDocxFile saves the generated docx file.
	 */
	/* private */ 
	void termDocxFile() {
		try {
			Docx4J.save(docx, new File(fileOutputPath));
		} catch (Docx4JException e) {
			Utils.logFatalError("critical DOCX4J save operation failed.");
		}
	}
	/**
	 * getHeaderContents gets a reference to the docx header part.
	 * @return
	 */
	/* private */
	List<Object> getHeaderContents() {
		HeaderPart headerPart = docx.getDocumentModel().getSections().get(0).getHeaderFooterPolicy().getDefaultHeader();
		List<Object> headerContents = headerPart.getContent();
		return headerContents;
	}
	/**
	 * getFooterContents gets a reference to the docx footer part.
	 * @return
	 */
	/* private */
	List<Object> getFooterContents() {
		FooterPart footerPart = docx.getDocumentModel().getSections().get(0).getHeaderFooterPolicy().getDefaultFooter();
		List<Object> headerContents = footerPart.getContent();
		return headerContents;
	}
	/** 
	 * genHeader updates the default header with the headerText.
	 * Code was generated by DOCX4J Word Add-in.
	 * @param headerContents reference to docx4j part.
	 * @param headerText new header text.
	 */
	/* private */
	void genHeader(List<Object> headerContents, String headerText) {
		logger.debug("generating header for document");
		org.docx4j.wml.ObjectFactory wmlObjectFactory = new org.docx4j.wml.ObjectFactory();
		// Create object for p
		P p = wmlObjectFactory.createP();
		// Create object for r
		R r = wmlObjectFactory.createR();
		p.getContent().add(r);
		// Create object for t (wrapped in JAXBElement)
		Text text = wmlObjectFactory.createText();
		JAXBElement<org.docx4j.wml.Text> textWrapped = wmlObjectFactory.createRT(text);
		r.getContent().add(textWrapped);
		text.setValue(headerText);
		// Line below replaces what was in the dotx templatel
		headerContents.set(0, p);
	}
	/** 
	 * genFooter updates the default footer with the footerText.
	 * Code was generated by DOCX4J Word Add-in.
	 * @param footerContents reference to docx4j part.
	 * @param footText new header text.
	 */
	/* private */
	void genFooter(List<Object> footerContents, String footText) {
		logger.debug("generating header for document");
		org.docx4j.wml.ObjectFactory wmlObjectFactory = new org.docx4j.wml.ObjectFactory();
		// Create object for p
		P p = wmlObjectFactory.createP();
		// Create object for r
		R r = wmlObjectFactory.createR();
		p.getContent().add(r);
		// Create object for t (wrapped in JAXBElement)
		Text text = wmlObjectFactory.createText();
		JAXBElement<org.docx4j.wml.Text> textWrapped = wmlObjectFactory.createRT(text);
		r.getContent().add(textWrapped);
		text.setValue(footText);
		// Line below replaces what was in the dotx templatel
		footerContents.set(0, p);
	}
	/**
	 * genWmlChunk demonstrates the technique of generating
	 * a big chunk of content using Word, then exporting the .xml
	 * using the DOCX4J word add-in, then importing the .xml file.
	 * 
	 * WARNING: Export/import does not work with graphics, hyperlinks, etc.
	 */
	void genWmlChunk(String wmlFileName) {
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
		MainDocumentPart mdp = docx.getMainDocumentPart();
		for (Object content: contents) {
			mdp.addObject(content);
		}
	}
	/**
	 * genBallotInstructions generates the ballot instructions provided by Voter Services.
	 */
	/* private */
	void genBallotInstructions() {
		genWmlChunk("ballot_instructions.wml");
	}
	/**
	 * genContests uses the contestsText to generate each "contest group"
	 * It assumes that the format (regex) is correct for each contest.
	 */
	/* private */
	List<P> genContests() {
		logger.debug("generating contests for document");
		// contestsParagraph is the list of all of the contest paragraphs.
		List<P> contestsParagraphs = new ArrayList<>();		
		String[] contestLines = contestsText.split("\n");
		for (String line: contestLines) {
			String [] elements = line.split(",");
			String contestName = elements[0];
			contestName = Contest.processContestName(contestName);
			String contestFormat = elements[1];
			List<P> contestParagraphs = genContest(contestName, contestFormat);
			contestsParagraphs.addAll(contestParagraphs);
		}
		return contestsParagraphs;
	}
	/**
	 * genContest generates a particular contest group.
	 * @param name e.g. "Justice of the Supreme Court"
	 * @param format number # that references "contest.format.#"
	 */
	/* private */
	List<P> genContest(String name, String format) {
		logger.debug(String.format("generating contest name: %s format: %s%n", name, format));
		List<P> contestParagraphs = new ArrayList<>();
		MainDocumentPart mdp = docx.getMainDocumentPart();
		ContestFactory cf = new ContestFactory(ballotText, formatsText, Initialize.elecType, Initialize.endorsedParty);
		String contestText = cf.findContestText(name);
		if (!contestText.isBlank()) {
			Contest contest = cf.parseContestText(name, contestText, format);
			// TODO: configuration item?
//			if (contest.getCandidates().size() > 0) {
				contestParagraphs = genContestParagraphs(mdp, contest);
//			}
		}
		return contestParagraphs;
	}
	/**
	 * genContestParagraphs generates the paragraphs of a contest group
	 * @param mdp MainDocumentPart from DOCX4J API.
	 * @param contest contest group
	 */
	List<P> genContestParagraphs(MainDocumentPart mdp, Contest contest) {
		List<P> contestParagraphs = new ArrayList<>();
		P newParagraph;
		newParagraph = mdp.createStyledParagraphOfText(STYLEID_CONTEST_TITLE, contest.getName());
		contestParagraphs.add(newParagraph);
		if (!contest.getTerm().isEmpty()) {
			newParagraph = mdp.createStyledParagraphOfText(STYLEID_CONTEST_INSTRUCTIONS, contest.getTerm());
			contestParagraphs.add(newParagraph);
		}
		newParagraph = mdp.createStyledParagraphOfText(STYLEID_CONTEST_INSTRUCTIONS, contest.getInstructions());
		contestParagraphs.add(newParagraph);
		newParagraph = mdp.createParagraphOfText(null);  // Paragraph separator
		contestParagraphs.add(newParagraph);
		List<Candidate> cands = contest.getCandidates();
		// Endorsements here. See the classes Endorsement, EndorsementFactory, EndorsementProcessor
		for (Candidate cand: cands) {
			// Note: there is no ellipse (black or white) when the party is null.
			// This is part of the kludge for "tickets" but also useful for other
			// situations.
			
			String oval = "";
			String candName = cand.getName();
			boolean endorsed = endorsementProcessor.isEndorsed(candName, contest.getName(), cand.getParty(), precinctNo);
			oval = endorsed? blackEllipse : whiteEllipse;
//			OLD kludge for ticket (e.g. US President & Vice-President, Governor and Lieutenant Governor
//			if (Initialize.elecType == GENERAL && ((GeneralCandidate) cand).getParty() != null) {
//				oval = (cand.getEndorsement())? blackEllipse : whiteEllipse;
//			} else {
//				// TODO - endoresement logic
//				// PRIMARY
//				oval = whiteEllipse;
//			}
			String partyOrResidence = "";
			boolean bottomOfTicket = false;
			if (Initialize.elecType == ElectionType.GENERAL) {
				partyOrResidence = ((GeneralCandidate) cand).getTextBeneathName();
				bottomOfTicket = ((GeneralCandidate) cand).getBottomOfTicket();
				if (bottomOfTicket) {
					oval = "   ";  // Need 3 spaces for 2nd name on ticket
				}
			} else {
				// PRIMARY
				partyOrResidence = ((PrimaryCandidate) cand).getResidence();
			}
			if (endorsed) {
				newParagraph = mdp.createStyledParagraphOfText(STYLEID_ENDORSED_CANDIDATE_NAME, oval + " " + candName);
			} else {
				newParagraph = mdp.createStyledParagraphOfText(STYLEID_CANDIDATE_NAME, oval + " " + candName);
			}
			contestParagraphs.add(newParagraph);
			newParagraph = mdp.createStyledParagraphOfText(STYLEID_CANDIDATE_PARTY, partyOrResidence);
			contestParagraphs.add(newParagraph);
		}
		// Display (or not) a write-in line
		if (Initialize.writeInDisplay) {
			newParagraph = mdp.createStyledParagraphOfText(STYLEID_CANDIDATE_NAME, whiteEllipse + "   " + "_".repeat(16));
			contestParagraphs.add(newParagraph);
			newParagraph = mdp.createStyledParagraphOfText(STYLEID_CANDIDATE_PARTY, "Write-in");
			contestParagraphs.add(newParagraph);
		}
		// Draw a border line as a separator
		newParagraph = mdp.createStyledParagraphOfText(STYLEID_BOTTOM_BORDER,null);
		contestParagraphs.add(newParagraph);  // Paragraph separator
		return contestParagraphs;
	}
	/**
	 * Finder is a DOCX utility method for finding types of parts within a Word document.
	 * Note that it is a call back function which the Traversal utility calls when it
	 * finds an part of the specific type, and that the results are accessed as a member
	 * variable.
	 */
	static class Finder extends CallbackImpl {
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
	 * findContestsPlaceholder traverses the paragraphs of the DOCX4J parts list
	 * looking for the paragraph that contains the PLACEHOLDER_CONTESTS text.
	 * This acts as the insertion point for the paragraphs to be generated.
	 * 
	 * @return P paragraph which is the placeholder.
	 */
	P findContestsPlaceholder() {
		P placeholder = null;
		MainDocumentPart mdp = docx.getMainDocumentPart();
		Finder pFinder = new Finder(P.class);
		new TraversalUtil (mdp, pFinder);
		List<Object> paragraphs  = pFinder.results;
		for (Object p: paragraphs) {
			Finder textFinder = new Finder(Text.class);
			new TraversalUtil (p, textFinder);
			List<Object> texts = textFinder.results;
			for (Object text: texts) {
				Text content = (Text) text;
				if (content.getValue().equals(PLACEHOLDER_CONTESTS)) {
					logger.info("found Contests placeholder.");
					placeholder = (P) p;
					break;
				}
			}
		}
		return placeholder;
	}

}
