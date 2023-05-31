package com.rodini.ballotgen;

import static com.rodini.ballotgen.contest.ContestFileLevel.COMMON;
import static com.rodini.ballotgen.contest.ContestFileLevel.MUNICIPAL;
import static com.rodini.ballotgen.endorsement.EndorsementMode.*;
import static java.util.stream.Collectors.joining;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.docx4j.Docx4J;
import org.docx4j.TraversalUtil;
import org.docx4j.TraversalUtil.CallbackImpl;
import org.docx4j.XmlUtils;
import org.docx4j.openpackaging.contenttype.ContentTypes;
import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.WordprocessingML.FooterPart;
import org.docx4j.openpackaging.parts.WordprocessingML.HeaderPart;
import org.docx4j.openpackaging.parts.WordprocessingML.MainDocumentPart;
import org.docx4j.wml.Br;
import org.docx4j.wml.ObjectFactory;
import org.docx4j.wml.P;
import org.docx4j.wml.R;
import org.docx4j.wml.Style;
import org.docx4j.wml.Styles;
import org.docx4j.wml.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.rodini.ballotgen.common.ElectionType;
import com.rodini.ballotgen.common.Initialize;
import com.rodini.ballotgen.contest.Candidate;
import com.rodini.ballotgen.contest.Contest;
import com.rodini.ballotgen.contest.ContestFactory;
import com.rodini.ballotgen.contest.ContestFileLevel;
import com.rodini.ballotgen.contest.ContestNameCounter;
import com.rodini.ballotgen.contest.GeneralCandidate;
import com.rodini.ballotgen.contest.PrimaryCandidate;
import com.rodini.ballotgen.endorsement.EndorsementMode;
import com.rodini.ballotgen.endorsement.EndorsementProcessor;
import com.rodini.ballotgen.writein.WriteinProcessor;
import com.rodini.ballotutils.Utils;

import jakarta.xml.bind.JAXBElement;
import jakarta.xml.bind.JAXBException;
/**
 * GenDocxBallot is the workhorse class that produces a new Word file
 * based on inputs.  Those inputs are:
 * 1. The ballot (text) file for the municipality.
 * 2. The contests (text) file for the municipality.
 * 3. The dotx template file which is referenced by the program's
 *    properties file.
 * 
 *  DOCX4J
 *  Website: https://www.docx4java.org/trac/docx4j
 *  API: https://javadoc.io/doc/org.docx4j/docx4j/latest/index.html
 *    
 *    
 * Notes:
 * 1. The class is designed to FAIL FAST if initialization fails.
 * 2. The class is designed to be THREAD SAFE.  It may not be.
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
	private String precinctNoName; // e.g. "005_Atglen" or "750_East_Whiteland_4"
	private String precinctName; // e.g. "Atglen" or "East_Whiteland_4"
	private String precinctNo; // e.g. "005" or "750"
	private static final String FILE_SUFFIX = "_VS";
//	private String ballotText; // text of ballotTextFile
	private String fileOutputPath; // generated DOCX file
	private String formatsText;   // formats (regexes) read from properties
	private EndorsementProcessor endorsementProcessor;
	private WriteinProcessor writeinProcessor;
	//          Candidate Candidate
	//          Name      Object
	public  static Map<String,   Integer> endorsedCandidates = new TreeMap<>();
	// Placeholders are predefined strings that may be embedded in the template file
	// so they can be located programmatically by this program.
	private static final String PLACEHOLDER_CONTESTS = "Contests";
	private static final String PLACEHOLDER_PRECINCT_NO = "Precinct#";
	private static final String PLACEHOLDER_PRECINCT_NAME = "PrecinctName";
	private static final String PLACEHOLDER_PRECINCT_NO_NAME = "Precinct#Name";
	// Styles are pre-defined within the dotx template.
	// There is a chance that this program is out of sync with the template
	// so when the template is loaded the existence of the styles is checked.
	// If style is not found "Normal" style is used.
	private static String STYLEID_CONTEST_TITLE = "ContestTitle";
	private static String STYLEID_CONTEST_INSTRUCTIONS = "ContestInstructions";
	private static String STYLEID_CANDIDATE_NAME = "CandidateName";
	private static String STYLEID_CANDIDATE_PARTY = "CandidateParty";
	private static String STYLEID_ENDORSED_CANDIDATE_NAME = "EndorsedCandidateName";
	private static String STYLEID_ENDORSED_CANDIDATE_PARTY = "EndorsedCandidateParty";
	private static String STYLEID_ANTI_ENDORSED_CANDIDATE_NAME = "AntiEndorsedCandidateName";
	private static String STYLEID_ANTI_ENDORSED_CANDIDATE_PARTY = "AntiEndorsedCandidateParty";
	private static String STYLEID_WRITE_IN_CANDIDATE_NAME = "WriteInCandidateName";
	private static String STYLEID_BOTTOM_BORDER = "BottomBorder";
	private static String STYLEID_COLUMN_BREAK_PARAGRAPH = "ColumnBreakParagraph";
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
			String formatsText, EndorsementProcessor ep, WriteinProcessor wp) {
		this.dotxPath = dotxPath;
		this.ballotTextFilePath = textFilePath;
		this.contestLevel = contestLevel;
		this.formatsText = formatsText;
		this.endorsementProcessor = ep;
		this.writeinProcessor = wp;
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
	 * between 2. and 3. above by use of "placeholder."
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
		P contestsPlace = findContestsPlaceholder(PLACEHOLDER_CONTESTS);
		MainDocumentPart mdp = docx.getMainDocumentPart();
		List<Object> contentList = mdp.getContent();
		int removeIndex = contentList.indexOf(contestsPlace);
		// Insert new paragraphs
		contentList.addAll(removeIndex, insertParagraphs);
		// Remove the placeholder paragraph
		contentList.remove(contestsPlace);
		// Footer
		genFooter(getFooterContents(), precinctNoName.replace("_", " "));
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
		File textFile = new File(ballotTextFilePath);
		String fileName = textFile.getName();
		String pathName = textFile.getAbsolutePath();
		// pathName includes fileName so strip out fileName
		int lastSeparator = pathName.lastIndexOf(File.separator);
		pathName = pathName.substring(0, lastSeparator);
		logger.info(String.format("pathName: %s fileName: %s", pathName, fileName));
		String[] fileElements = fileName.split("\\.");
		precinctNoName = fileElements[0];
		// if suffix was added, then remove it.
		if (precinctNoName.endsWith(FILE_SUFFIX)) {
			precinctNoName = precinctNoName.substring(0, precinctNoName.length() - FILE_SUFFIX.length());
		}
		precinctNo = precinctNoName.substring(0, 3);
		precinctName = precinctName.substring(3);
		try {
			Integer.parseInt(precinctNo);
		} catch (NumberFormatException e) {
			logger.error("Ballot Name doesn't start with precinct No. See: " + precinctNoName);
			precinctNo = "000";
		}
		fileOutputPath = pathName + File.separator + precinctNoName + ".docx";
		System.out.printf("Generating %s%n", precinctNoName + ".docx");
		logger.info(String.format("fileOutputPath: %s", fileOutputPath));
		WordprocessingMLPackage dotx = null;
		try {
			dotx = Docx4J.load(dotxFile);
			docx = (WordprocessingMLPackage) dotx.cloneAs(ContentTypes.WORDPROCESSINGML_DOCUMENT);
			// The dotx should be unchanged
			docx.attachTemplate(dotxPath);
			// sample shows a save (with no changes) back to the .dotx file
			// this should be unnecessary unless it loses a system file handle
			dotx.save(dotxFile);
		} catch (Docx4JException e) {
			Utils.logFatalError("critical DOCX4J load/clone/attach operation failed.");
		}
	}
	/**
	 * initContestsText reads in the contests file for this ballot.
	 */
	void initContestsText() {
		String contestsPath = Initialize.ballotContestsPath + File.separator;
		if (contestLevel == COMMON) {
			contestsPath = contestsPath + Initialize.COMMON_CONTESTS_FILE;
		} else if (contestLevel == MUNICIPAL) {
			contestsPath = contestsPath + precinctNoName + "_contests.txt";
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
		// Perform file existence check here.
		String ballotText = Utils.readTextFile(ballotTextFilePath);
	}
	/**
	 * initStyles checks that the expected StyleIds (just strings) exist
	 * in the dotx file.  If not, it substitutes style "Normal"
	 */
	/* private */ 	
	void initStyles() {
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
		if (!templateIdStyles.contains(STYLEID_ENDORSED_CANDIDATE_PARTY)) {
			logger.error("dotx template missing this styleId: " + STYLEID_ENDORSED_CANDIDATE_PARTY);
			STYLEID_ENDORSED_CANDIDATE_PARTY = "Normal";
		}
		if (!templateIdStyles.contains(STYLEID_ANTI_ENDORSED_CANDIDATE_NAME)) {
			logger.error("dotx template missing this styleId: " + STYLEID_ANTI_ENDORSED_CANDIDATE_NAME);
			STYLEID_ANTI_ENDORSED_CANDIDATE_NAME = "Normal";
		}
		if (!templateIdStyles.contains(STYLEID_WRITE_IN_CANDIDATE_NAME)) {
			logger.error("dotx template missing this styleId: " + STYLEID_WRITE_IN_CANDIDATE_NAME);
			STYLEID_WRITE_IN_CANDIDATE_NAME = "Normal";
		}
		if (!templateIdStyles.contains(STYLEID_BOTTOM_BORDER)) {
			logger.error("dotx template missing this styleId: " + STYLEID_BOTTOM_BORDER);
			STYLEID_BOTTOM_BORDER = "Normal";
		}
		if (!templateIdStyles.contains(STYLEID_COLUMN_BREAK_PARAGRAPH)) {
			logger.error("dotx template missing this styleId: " + STYLEID_COLUMN_BREAK_PARAGRAPH);
			STYLEID_COLUMN_BREAK_PARAGRAPH = "Normal";
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
		List<Object> footerContents = footerPart.getContent();
		return footerContents;
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
		logger.debug("generating footer for document");
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
		// Line below replaces what was in the dotx template.
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
	 * 
	 * Notes:
	 * The property column.break.contest.count is a crude way of formatting
	 * the ballot layout. A human counts (starting at 1) the contests
	 * after which a column break should be generated.  This is crude since
	 * a contest names and # of candidates can vary from municipality to
	 * municipality.
	 */
	/* private */
	List<P> genContests() {
		logger.debug("generating contests for document");
		// contestsParagraphs is the list of all of the contest paragraphs.
		List<P> contestsParagraphs = new ArrayList<>();		
		String[] contestLines = contestsText.split("\n");
		ContestNameCounter ballotFactory = new ContestNameCounter(Utils.readTextFile(ballotTextFilePath));
		int i = 0;
		int j = 0;
		for (String line: contestLines) {
			String [] elements = line.split(",");
			String contestName = elements[0];
			contestName = Contest.processContestName(contestName);
			String contestFormat = elements[1];
			List<P> contestParagraphs = contestName.equals(Initialize.PAGE_BREAK)?
					  genPageBreak(docx.getMainDocumentPart())
					: genContest(ballotFactory, contestName, contestFormat);
			// Insert column break after contest?
			if (i+1 == Initialize.columnBreaks[j]) {
				P columnBreakParagraph = genColumnBreakParagraph();
				contestParagraphs.add(columnBreakParagraph);
				j++;
			}
			contestsParagraphs.addAll(contestParagraphs);
			i++;
		}
		return contestsParagraphs;
	}
	/**
	 * genContest generates a particular contest group. This consists 
	 * of the contest name, contest instructions, candidates, etc.
	 *  
	 * @param bf ballotFactory object
	 * @param name e.g. "Justice of the Supreme Court"
	 * @param format number # that references "contest.format.#"
	 */
	/* private */
	List<P> genContest(ContestNameCounter bf, String name, String format) {
		logger.debug(String.format("generating contest name: %s format: %s%n", name, format));
		List<P> contestParagraphs = new ArrayList<>();
		MainDocumentPart mdp = docx.getMainDocumentPart();
		ContestFactory cf = new ContestFactory(bf, formatsText, Initialize.elecType, Initialize.endorsedParty);
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
	 * genContestParagraphs generates the paragraphs of a contest.
	 * 
	 * @param mdp MainDocumentPart from DOCX4J API.
	 * @param contest contest group
	 * @return list of new paragraphs.
	 */
	List<P> genContestParagraphs(MainDocumentPart mdp, Contest contest) {	
		List<P> contestParagraphs = new ArrayList<>();
		// contest header: name, term, and instructions
		List<P> headParagraphs = genContestHeader(mdp, contest);
		contestParagraphs.addAll(headParagraphs);	
		// list of candidates (including write-ins)	
		List<P> candParagraphs = genContestCandidates(mdp, contest);
		contestParagraphs.addAll(candParagraphs);
		return contestParagraphs;
	}
	/**
	 * genContestHeader generates and formats the contest name, term, and instructions.
	 * 
	 * @param mdp MainDocumentPart from DOCX4J API.
	 * @param contest Contest object.
	 * @return list of contest header paragraphs.
	 */
	List<P> genContestHeader(MainDocumentPart mdp, Contest contest) {
		P newParagraph;
		List<P> headParagraphs = new ArrayList<>();
		newParagraph = mdp.createStyledParagraphOfText(STYLEID_CONTEST_TITLE, contest.getName());
		headParagraphs.add(newParagraph);
		if (!contest.getTerm().isEmpty()) {
			newParagraph = mdp.createStyledParagraphOfText(STYLEID_CONTEST_INSTRUCTIONS, contest.getTerm());
			headParagraphs.add(newParagraph);
		}
		newParagraph = mdp.createStyledParagraphOfText(STYLEID_CONTEST_INSTRUCTIONS, contest.getInstructions());
		headParagraphs.add(newParagraph);
		newParagraph = mdp.createParagraphOfText(null);  // Paragraph separator
		headParagraphs.add(newParagraph);
		return headParagraphs;
	}
	/**
	 * genContestCandidates generates and formats the list of candidates for the contest.
	 * This is complex because each candidate may be endorsed, unendorsed, or anti-endorsed.
	 * Also, there may be write-in candidates for the contest.
	 * 
	 * @param mdp MainDocumentPart from DOCX4J API.
	 * @param contest Contest object.
	 * @return list of candidate paragraphs.
	 */
	List<P> genContestCandidates(MainDocumentPart mdp, Contest contest) {
		String contestName = contest.getName();
		List<Candidate> cands = contest.getCandidates();
		List<P> candsParagraphs = new ArrayList<>();
		P newParagraph;
		for (Candidate cand: cands) {
			candsParagraphs.addAll(genContestCandidate(mdp, contestName, cand));	
		}
		// generate Write-in candidates (if any)
		candsParagraphs.addAll(genWriteins(mdp, contest));
		// Draw a border line as a separator
		newParagraph = mdp.createStyledParagraphOfText(STYLEID_BOTTOM_BORDER,null);
		candsParagraphs.add(newParagraph);
		return candsParagraphs;
	}
	/**
	 * genContestCandidate generates and formats a single candidate.
	 * This is tricky because the candidate may be endorsed, unendorsed, or anti-endorsed.
	 * Notes:
	 * - Endorsements are processed here. See the classes Endorsement, EndorsementFactory, EndorsementProcessor.
	 * 
	 * @param mdp MainDocumentPart from DOCX4J API.
	 * @param contestName contest name.
	 * @param cand Candidate object.
	 * @return
	 */
	List<P> genContestCandidate(MainDocumentPart mdp, String contestName, Candidate cand) {
		List<P> candParagraphs = new ArrayList<>();
		P newParagraph = null;
		String oval = "";
		String candName = cand.getName();
		EndorsementMode mode = endorsementProcessor.getEndorsementMode(candName, contestName, cand.getParty(), precinctNo);
		if (mode == ENDORSED) {
			endorsedCandidates.merge(candName.toUpperCase(), 1, (prev, inc) -> prev + inc);
		}
		oval = mode == ENDORSED ? blackEllipse : whiteEllipse;
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
		// paragraph for candidate name
		newParagraph = genParagraphByEndorsementMode(mdp, oval + " " + candName, true, mode);
		candParagraphs.add(newParagraph);
		// paragraph for candidate party
		newParagraph = genParagraphByEndorsementMode(mdp, partyOrResidence, false, mode);
		candParagraphs.add(newParagraph);
		return candParagraphs;
	}
	/** 
	 * genParagraphByEndorsementMode generates a styled candidate line based on endorsement mode.
	 * @param mdp MainDocumentPart from DOCX4J API.
	 * @param text candidate name or line below name.
	 * @param mode EndorsementMode value.
	 * @return list of paragraphs.
	 */
	P genParagraphByEndorsementMode(MainDocumentPart mdp, String text, boolean textIsName, EndorsementMode mode) {
		P newParagraph;
		String style = "";
		switch (mode) {
		case ENDORSED:
			style = textIsName? STYLEID_ENDORSED_CANDIDATE_NAME : STYLEID_ENDORSED_CANDIDATE_PARTY;
			newParagraph = mdp.createStyledParagraphOfText(style, text);
			break;
		case UNENDORSED:
			style = textIsName? STYLEID_CANDIDATE_NAME : STYLEID_CANDIDATE_PARTY;
			newParagraph = mdp.createStyledParagraphOfText(style, text);
			break;
		default:
		/* case ANTIENDORSED: */
			style = textIsName? STYLEID_ANTI_ENDORSED_CANDIDATE_NAME : STYLEID_ANTI_ENDORSED_CANDIDATE_PARTY;
			newParagraph = mdp.createStyledParagraphOfText(style, text);
			break;
		}
		return newParagraph;
	}
	/**
	 * genWriteins generate the list of Write-ins name.
	 * Notes: In most cases this will be a blank line.
	 * 
	 * @param mdp MainDocumentPart from DOCX4J API.
	 * @param contest object.
	 * @return list of paragraphs.
	 */
	List<P> genWriteins(MainDocumentPart mdp, Contest contest) {
		List<P> writeinParagraphs = new ArrayList<>();
		P newParagraph;
		// list of write-in names.
		List<String> names = new ArrayList<>();
		if (writeinProcessor.precinctHasWriteins(precinctNo)) {
			// Important: map \n to a space for match!
			String contestName = contest.getName().replace("\n", " ");
			names = writeinProcessor.findCandidatesForContest(precinctNo, contestName);
		}
		if (names.size() > 0) {
			for (String name: names) {
				writeinParagraphs.addAll(genWriteinCandidate(mdp, name));
			}
		} else {
			// Display a blank write-in line
		//	if (Initialize.writeInDisplay) {
			newParagraph = mdp.createStyledParagraphOfText(STYLEID_CANDIDATE_NAME, whiteEllipse + "   " + "_".repeat(16));
			writeinParagraphs.add(newParagraph);
			newParagraph = mdp.createStyledParagraphOfText(STYLEID_CANDIDATE_PARTY, "Write-in");
			writeinParagraphs.add(newParagraph);
		//	}
		}
		return writeinParagraphs;
	}
	/**
	 * Generate the styled paragraph for a write-in candidate.
	 * @param name of write-in candidate
	 * @return list of paragraphs
	 */
	List<P> genWriteinCandidate(MainDocumentPart mdp, String name) {
		List<P> candParagraphs = new ArrayList<>();
		P newParagraph = null;
		String oval =  blackEllipse;
		String text = oval + " " + name;
		// TODO: underline just the name, not the oval
		// Explore Word style type CHARACTER or LINKED
		newParagraph = mdp.createStyledParagraphOfText(STYLEID_WRITE_IN_CANDIDATE_NAME, text);
		candParagraphs.add(newParagraph);
		newParagraph = mdp.createStyledParagraphOfText(STYLEID_CANDIDATE_PARTY, "Write-in");
		candParagraphs.add(newParagraph);
		return candParagraphs;
	}
	
	/**
	 * genPageBreak generates the pseudo contest name "PAGE BREAK" using
	 * the wording in the property PAGE_BREAK_WORDING (e.g. "See other side of ballot")
	 * @param mdp MainDocumentPart from DOCX4J API.
	 * @return list of new paragraphs.
	 */
	List<P> genPageBreak(MainDocumentPart mdp) {
		logger.info("generating page break");
		List<P> pageBreakParagraphs = new ArrayList<>();
		P newParagraph;
		// first paragraph
		newParagraph = mdp.createStyledParagraphOfText(STYLEID_CONTEST_TITLE, Initialize.PAGE_BREAK_WORDING);
		pageBreakParagraphs.add(newParagraph);
		// Draw a border line as a separator
		newParagraph = mdp.createStyledParagraphOfText(STYLEID_BOTTOM_BORDER,null);
		// last paragraph
		pageBreakParagraphs.add(newParagraph);  // Paragraph separator
		return pageBreakParagraphs;
	}
	/** 
	 * genColumnBreakParagraph generates a column break object tree.
	 * This can be used to "post-format" the columns after a human evaluation as to where
	 * a column break should be injected.
	 * @return paragraph effecting a column break
	 */
	P genColumnBreakParagraph() {
		logger.info("generating column break");
		org.docx4j.wml.ObjectFactory wmlObjectFactory = new ObjectFactory();
		MainDocumentPart mdp = docx.getMainDocumentPart();
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
	P findContestsPlaceholder(String placeHolder) {
		P placeParagraph = null;
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
				if (content.getValue().equals(placeHolder)) {
					logger.info("found Contests placeholder.");
					placeParagraph = (P) p;
					break;
				}
			}
		}
		return placeParagraph;
	}

}
