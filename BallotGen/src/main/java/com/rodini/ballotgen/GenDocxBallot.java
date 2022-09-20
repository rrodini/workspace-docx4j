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

import static com.rodini.ballotgen.ContestFileLevel.*;
import static com.rodini.ballotgen.Utils.logFatalError;
import static com.rodini.ballotgen.ElectionType.*;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;

import org.docx4j.Docx4J;
import org.docx4j.XmlUtils;
import org.docx4j.openpackaging.contenttype.ContentTypes;
import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.WordprocessingML.FooterPart;
import org.docx4j.openpackaging.parts.WordprocessingML.HeaderPart;
import org.docx4j.openpackaging.parts.WordprocessingML.MainDocumentPart;
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
	String dotxPath;  // path to DOTX template file
	String ballotTextFilePath; // path to ballot text file
	ContestFileLevel contestLevel; // COMMON or MUNICIPAL
	String contestsText; // text of contests on this ballot
	// local variables
	WordprocessingMLPackage docx;  // document under construction
	String ballotName; // e.g. "Atglen" or "East Whiteland 4"
	String contestFilePath; // path to contest file e.g. XYZ_contests.txt
	static final String FILE_SUFFIX = "_VS";
	String ballotText; // text of ballotTextFile
	String fileOutputPath; // generated DOCX file
	String formatsText;   // formats (regexes) read from properties
	// Sty[es
	// Styles are pre-defined within the dotx template.
	// There is a chance that this program is out of sync with the template
	// so when the template is loaded the existence of the styles is checked.
	// If style is not found "Normal" style is used.
	String STYLEID_CONTEST_TITLE = "ContestTitle";
	String STYLEID_CONTEST_INSTRUCTIONS = "ContestInstructions";
	String STYLEID_CANDIDATE_NAME = "CandidateName";
	String STYLEID_CANDIDATE_PARTY = "CandidateParty";
	// These are unicode characters (also Segoe UI Symbol font)
	final String whiteEllipse = "⬭";
	final String blackEllipse = "⬬";
	/** 
	 * GenDocxBallot constructor just saves input references.
	 * @param dotxPath path to the Word template.
	 * @param textFilePath path to the ballot text file.
	 * @param contestsLevel which contests text file to use.
	 * @param formatsText formats (regexes) to use.
	 */
	public GenDocxBallot(String dotxPath, String textFilePath, ContestFileLevel contestLevel, String formatsText) {
		this.dotxPath = dotxPath;
		this.ballotTextFilePath = textFilePath;
		this.contestLevel = contestLevel;
		this.formatsText = formatsText;
	}
	/**
	 * generate is line main() in most programs.
	 */
	public void generate() {
		// validate all inputs.
		logger.info("start new docx file");
		initialize();
		// generate the header
		genHeader(getHeaderContents(), ballotName.replace("_", " "));
		// generate the voter instructions
		genBallotInstructions();	
		// generate the contests on the ballot 
		genContests();	
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
		fileOutputPath = pathName + File.separator + ballotName + ".docx";
		System.out.printf("Generating %s%n", ballotName + ".docx");
		logger.info(String.format("fileOutputPath: %s", fileOutputPath));
		// If the file base name has underscores, replace them with blanks
//		ballotName = ballotName.replace("_", " ");
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
			logFatalError("can't read: " + contestsPath);
		}
		contestsText = contestsLines.stream().collect(joining("\n"));
		// prepare logging message
//		int len = contestsText.length();
//		if (len > 400) {
//			contestsText = contestsText.substring(0, 200) + "..." + 
//		                     contestsText.substring(len - 200, len);
//		}
		logger.debug("contestsText: " + contestsText);
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
		} else
		if (!templateIdStyles.contains(STYLEID_CONTEST_INSTRUCTIONS)) {
			logger.error("dotx template missing this styleId: " + STYLEID_CONTEST_INSTRUCTIONS);
			STYLEID_CONTEST_INSTRUCTIONS = "Normal";
		}
		if (!templateIdStyles.contains(STYLEID_CONTEST_TITLE)) {
			logger.error("dotx template missing this styleId: " + STYLEID_CONTEST_TITLE);
			STYLEID_CONTEST_TITLE = "Normal";
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
	/* private */
	/** 
	 * updateHeader updates the default header with the headerText.
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
	 * genBallotInstructions demonstrates the technique of generating
	 * a big chunk of content using Word, then exporting the .xml
	 * using the DOCX4J word add-in, then importing the .xml file.
	 */
	/* private */
	void genBallotInstructions() {
		// load stream from resources
		//InputStream inStream = ClassLoader.getSystemResourceAsStream("ballot_instructions.wml");
		String text = "";
		try (FileInputStream inStream = new FileInputStream(Initialize.RESOURCE_PATH + "ballot_instructions.wml");) {
			text = new String(inStream.readAllBytes(), StandardCharsets.UTF_8);
		} catch (IOException e) {
			logger.error("error reading xml chunk.");
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
	 * genContests uses the contestsText to generate each "contest group"
	 * It assumes that the format (regex) is correct for each contest.
	 */
	/* private */
	void genContests() {
		logger.debug("generating contests for document");
		String[] contestLines = contestsText.split("\n");
		for (String line: contestLines) {
			String [] elements = line.split(",");
			String contestName = elements[0];
			contestName = Utils.processContestName(contestName);
			String contestFormat = elements[1];
			genContest(contestName, contestFormat);
		}
	}
	/**
	 * genContest generates a particular contest group.
	 * @param name e.g. "Justice of the Supreme Court"
	 * @param format number # that references "contest.format.#"
	 */
	/* private */
	void genContest(String name, String format) {
		logger.debug(String.format("generating contest name: %s format: %s%n", name, format));
		MainDocumentPart mdp = docx.getMainDocumentPart();
		ContestFactory cf = new ContestFactory(ballotText, formatsText, Initialize.elecType);
		String contestText = cf.findContestText(name);
		if (!contestText.isEmpty()) {
			Contest contest = cf.parseContestText(name, contestText, format);
			genContestParagraphs(mdp, contest);
		}
	}
	/**
	 * genContestParagraphs generates the paragraphs of a contest group
	 * @param mdp MainDocumentPart from DOCX4J API.
	 * @param contest contest group
	 */
	void genContestParagraphs(MainDocumentPart mdp, Contest contest) {
		mdp.addStyledParagraphOfText(STYLEID_CONTEST_TITLE, contest.getName());
		if (!contest.getTerm().isEmpty()) {
			mdp.addStyledParagraphOfText(STYLEID_CONTEST_INSTRUCTIONS, contest.getTerm());
		}
		mdp.addStyledParagraphOfText(STYLEID_CONTEST_INSTRUCTIONS, contest.getInstructions());
		mdp.addParagraphOfText(null);  // Paragraph separator
		List<Candidate> cands = contest.getCandidates();
		// Endorsements here.
		for (Candidate cand: cands) {
			// Note: there is no ellipse (black or white) when the party is null.
			// This is part of the kludge for "tickets" but also useful for other
			// situations
			String endorse = "   ";  // Need 3 spaces for 2nd name on ticket
			if (Initialize.elecType == GENERAL && ((GeneralCandidate) cand).getParty() != null) {
				endorse = (cand.getEndorsement())? blackEllipse : whiteEllipse;
			}
			String partyOrResidence = (Initialize.elecType == ElectionType.GENERAL) ?
					((GeneralCandidate) cand).getTextBeneathName():
					((PrimaryCandidate) cand).getResidence();
			mdp.addStyledParagraphOfText(STYLEID_CANDIDATE_NAME, endorse + " " + cand.getName());
			mdp.addStyledParagraphOfText(STYLEID_CANDIDATE_PARTY, partyOrResidence);
		}
		mdp.addParagraphOfText(null);  // Paragraph separator
	}
	
}
