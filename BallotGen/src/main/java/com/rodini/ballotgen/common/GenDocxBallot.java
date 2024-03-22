package com.rodini.ballotgen.common;

import static com.rodini.ballotgen.contest.ContestFileLevel.COMMON;
import static com.rodini.ballotgen.contest.ContestFileLevel.MUNICIPAL;
import static com.rodini.ballotgen.endorsement.EndorsementMode.ENDORSED;
import static com.rodini.ballotgen.endorsement.EndorsementMode.UNENDORSED;
import static com.rodini.ballotutils.Utils.ATTN;
import static org.apache.logging.log4j.Level.DEBUG;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.docx4j.Docx4J;
import org.docx4j.openpackaging.contenttype.ContentTypes;
import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.WordprocessingML.HeaderPart;
import org.docx4j.openpackaging.parts.WordprocessingML.MainDocumentPart;
import org.docx4j.wml.P;
import org.docx4j.wml.Style;
import org.docx4j.wml.Styles;

import com.rodini.ballotgen.contest.Candidate;
import com.rodini.ballotgen.contest.Contest;
import com.rodini.ballotgen.contest.ContestFactory;
import com.rodini.ballotgen.contest.ContestFileLevel;
import com.rodini.ballotgen.contest.ContestNameCounter;
import com.rodini.ballotgen.contest.GeneralCandidate;
import com.rodini.ballotgen.contest.PrimaryCandidate;
import com.rodini.ballotgen.endorsement.EndorsementMode;
import com.rodini.ballotgen.endorsement.EndorsementProcessor;
import com.rodini.ballotgen.generate.GenDocx;
import com.rodini.ballotgen.placeholder.Placeholder;
import com.rodini.ballotgen.placeholder.PlaceholderProcessor;
import com.rodini.ballotgen.writein.WriteinProcessor;
import com.rodini.ballotutils.Utils;
import com.rodini.zoneprocessor.Zone;
import com.rodini.zoneprocessor.ZoneProcessor;
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
	private static final Logger logger = LogManager.getLogger(GenDocxBallot.class);

	// from constructor
	private String dotxPath;  // path to DOTX template file
	private String ballotTextFilePath; // path to ballot text file
	private ContestFileLevel contestLevel; // COMMON or MUNICIPAL
	private String ballotText; // text of ballotTextFile
	private String contestsText; // text of contest names on this ballot
	private String referendumsText; // text of referendum questions on this ballot
	private String retentionsText; // text of retention questions on this ballot
	// local variables
	private WordprocessingMLPackage docx;  // document under construction
	private String precinctNoName; // e.g. "005_Atglen" or "750_East_Whiteland_4"
	private String precinctName; // e.g. "Atglen" or "East_Whiteland_4"
	private String precinctNo; // e.g. "005" or "750"
	private String zoneNo;		// # of zone that "owns" precinct
	private String zoneName;	// name of zone that "owns" precinct
	private static final String FILE_SUFFIX = "_VS";
	private String fileOutputPath; // generated DOCX file
	private String formatsText;   // formats (regexes) read from properties
	private EndorsementProcessor endorsementProcessor;
	private WriteinProcessor writeinProcessor;
	//          Candidate Candidate
	//          Name      Object
	public  static Map<String,   Integer> endorsedCandidates = new TreeMap<>();
	// Placeholders are predefined strings that may be embedded in the template file
	// so they can be located programmatically by this program.
	public static final String PLACEHOLDER_CONTESTS = "Contests";
	public static final String PLACEHOLDER_REFERENDUMS = "Referendums";
	public static final String PLACEHOLDER_RETENTIONS = "Retentions";
	public static final String PLACEHOLDER_PRECINCT_NO = "PrecinctNo";
	public static final String PLACEHOLDER_PRECINCT_NAME = "PrecinctName";
	public static final String PLACEHOLDER_PRECINCT_NO_NAME = "PrecinctNoName";
	public static final String PLACEHOLDER_ZONE_NO= "ZoneNo";	
	public static final String PLACEHOLDER_ZONE_NAME = "ZoneName";	
	public static final String PLACEHOLDER_ZONE_LOGO = "ZoneLogo";	
	public static List<String> placeholderNames = List.of (PLACEHOLDER_CONTESTS, PLACEHOLDER_REFERENDUMS, PLACEHOLDER_RETENTIONS,
			  PLACEHOLDER_PRECINCT_NO, PLACEHOLDER_PRECINCT_NAME, PLACEHOLDER_PRECINCT_NO_NAME,
			  PLACEHOLDER_ZONE_NO, PLACEHOLDER_ZONE_NAME, PLACEHOLDER_ZONE_LOGO);
	private static PlaceholderProcessor phProcessor;
	// Styles are pre-defined within the dotx template.
	// There is a chance that this program is out of sync with the template
	// so when the template is loaded the existence of the styles is checked.
	// If style is not found "Normal" style is used.
	public static String STYLEID_NORMAL = "Normal";
	public static String STYLEID_CONTEST_TITLE = "ContestTitle";
	public static String STYLEID_CONTEST_GENERIC_TITLE = "ContestGenericTitle";
	public static String STYLEID_CONTEST_INSTRUCTIONS = "ContestInstructions";
	public static String STYLEID_CONTEST_GENERIC_INSTRUCTIONS = "ContestGenericInstructions";
	public static String STYLEID_CANDIDATE_NAME = "CandidateName";
	public static String STYLEID_CANDIDATE_PARTY = "CandidateParty";
	public static String STYLEID_ENDORSED_CANDIDATE_NAME = "EndorsedCandidateName";
	public static String STYLEID_ENDORSED_CANDIDATE_PARTY = "EndorsedCandidateParty";
	public static String STYLEID_ANTI_ENDORSED_CANDIDATE_NAME = "AntiEndorsedCandidateName";
	public static String STYLEID_ANTI_ENDORSED_CANDIDATE_PARTY = "AntiEndorsedCandidateParty";
	public static String STYLEID_WRITE_IN_CANDIDATE_NAME = "WriteInCandidateName";
	public static String STYLEID_BOTTOM_BORDER = "BottomBorder";
	public static String STYLEID_COLUMN_BREAK_PARAGRAPH = "ColumnBreakParagraph";
	// Make separate enum if needed elsewhere.
	public enum TextStyle {BOLD, UNDERLINE}; // BOLD for retention questions, UNDERLINE for write-in candidates.
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
		Initialize.docxGenCount++;
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
	 * In version 1.4.1 of BallotGen the Contests and Candidates are inserted
	 * between 2. and 3. by use of "placeholder".
	 */
	public void generate() {
		// validate all inputs.
		logger.info("start new docx file");
		initialize();
		phProcessor = new PlaceholderProcessor(docx, placeholderNames);
		// below relies completely of placeholder functionality.
		genHeader();
		genBody();
		genFooter();
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
		initContestsFileText();
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
// OLD - doesn't work if name contains a period like Kennett Sq.
//		String[] fileElements = fileName.split("\\.");
//		precinctNoName = fileElements[0];
// NEW - next two lines.
		int lastDot = fileName.lastIndexOf(".");
		precinctNoName = fileName.substring(0, lastDot);
		// if suffix was added, then remove it.
		if (precinctNoName.endsWith(FILE_SUFFIX)) {
			precinctNoName = precinctNoName.substring(0, precinctNoName.length() - FILE_SUFFIX.length());
		}
		
		precinctNo = precinctNoName.substring(0, 3);
		precinctName = precinctNoName.substring(3);

		boolean precinctInMap = Initialize.precinctToZoneMap.keySet().contains(precinctNo);
		if (!precinctInMap) {
			logger.error(String.format("precinct # %s is not in precinctToZoneMap", precinctNo));
		}
		zoneNo = endorsementProcessor.getZoneNoForPrecinct(precinctNo);
		zoneName = endorsementProcessor.getZoneNameForPrecinct(precinctNo);
		logger.log(ATTN, precinctNoName);
		System.out.println("Generating: " + precinctNoName);
		try {
			Integer.parseInt(precinctNo);
		} catch (NumberFormatException e) {
			logger.error("Ballot Name doesn't start with precinct No. See: " + precinctNoName);
			precinctNo = "000";
		}
		fileOutputPath = pathName + File.separator + precinctNoName + ".docx";
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
	 * initContestsFileText reads in the contests file for this ballot.
	 */
	void initContestsFileText() {
		String contestsPath = Initialize.ballotContestsPath + File.separator;
		if (contestLevel == COMMON) {
			contestsPath = contestsPath + Initialize.COMMON_CONTESTS_FILE;
		} else if (contestLevel == MUNICIPAL) {
			contestsPath = contestsPath + precinctNoName + "_contests.txt";
		} else {
			Utils.logFatalError("contest file level not recognized. See: " + contestLevel.toString());
		}
		logger.debug("Loading contests file: " + contestsPath);
		String contestsFileText = Utils.readTextFile(contestsPath);
		// prepare logging message
		int len = contestsFileText.length();
		String msgText = contestsFileText;
		if (len > 100) {
			msgText = contestsFileText.substring(0, 50) + "..." + contestsFileText.substring(len - 50, len);
		}
		logger.debug("contestsFileText: " + msgText);
		initContestsSectionsText(contestsFileText);
	}
	/**
	 * initContestsSectionsText separates the contents of a municipal contests file into
	 * its sections: Contests (required), Referendums (optional), Retentions (optional).
	 * Note: ContestGen uses the order above.
	 * 
	 * @param contestsFileText
	 */
	void initContestsSectionsText(String contestsFileText) {
		int indexContests = contestsFileText.indexOf(PLACEHOLDER_CONTESTS);
		int indexReferendums = contestsFileText.indexOf(PLACEHOLDER_REFERENDUMS);
		int indexRetentions = contestsFileText.indexOf(PLACEHOLDER_RETENTIONS);
		int end = contestsFileText.length();
		// is "Retentions" present?
		if (indexRetentions >= 0) {
			retentionsText = contestsFileText.substring(indexRetentions + PLACEHOLDER_RETENTIONS.length()+1, end);
			end = indexRetentions;
		}
		// is "Referendums" present?
		if (indexReferendums >= 0) {
			referendumsText = contestsFileText.substring(indexReferendums + PLACEHOLDER_REFERENDUMS.length()+1, end);
			end = indexReferendums;
		}
		// is "Contests" present? Should be.
		if (indexContests >= 0) {
			contestsText = contestsFileText.substring(indexContests + PLACEHOLDER_CONTESTS.length()+1, end);
		}
		if (contestsText == null || contestsText.isBlank()) {
			logger.error("no contest names found in this municipality's contest file");
		}
	}
	/**
	 * initReadBallotText reads the contents of the ballot text file
	 * into a string.
	 */
	/* private */ 
	void initReadBallotText() {
		// Perform file existence check here.
		ballotText = Utils.readTextFile(ballotTextFilePath);
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
			STYLEID_CANDIDATE_NAME = STYLEID_NORMAL;
		}
		if (!templateIdStyles.contains(STYLEID_CANDIDATE_PARTY)) {
			logger.error("dotx template missing this styleId: " + STYLEID_CANDIDATE_PARTY);
			STYLEID_CANDIDATE_PARTY = STYLEID_NORMAL;
		}
		if (!templateIdStyles.contains(STYLEID_CONTEST_TITLE)) {
			logger.error("dotx template missing this styleId: " + STYLEID_CONTEST_TITLE);
			STYLEID_CONTEST_TITLE = STYLEID_NORMAL;
		}
		if (!templateIdStyles.contains(STYLEID_CONTEST_GENERIC_TITLE)) {
			logger.error("dotx template missing this styleId: " + STYLEID_CONTEST_GENERIC_TITLE);
			STYLEID_CONTEST_GENERIC_TITLE = STYLEID_NORMAL;
		}
		if (!templateIdStyles.contains(STYLEID_CONTEST_INSTRUCTIONS)) {
			logger.error("dotx template missing this styleId: " + STYLEID_CONTEST_INSTRUCTIONS);
			STYLEID_CONTEST_INSTRUCTIONS = STYLEID_NORMAL;
		}
		if (!templateIdStyles.contains(STYLEID_CONTEST_GENERIC_INSTRUCTIONS)) {
			logger.error("dotx template missing this styleId: " + STYLEID_CONTEST_GENERIC_INSTRUCTIONS);
			STYLEID_CONTEST_GENERIC_INSTRUCTIONS = STYLEID_NORMAL;
		}
		if (!templateIdStyles.contains(STYLEID_ENDORSED_CANDIDATE_NAME)) {
			logger.error("dotx template missing this styleId: " + STYLEID_ENDORSED_CANDIDATE_NAME);
			STYLEID_ENDORSED_CANDIDATE_NAME = STYLEID_NORMAL;
		}
		if (!templateIdStyles.contains(STYLEID_ENDORSED_CANDIDATE_PARTY)) {
			logger.error("dotx template missing this styleId: " + STYLEID_ENDORSED_CANDIDATE_PARTY);
			STYLEID_ENDORSED_CANDIDATE_PARTY = STYLEID_NORMAL;
		}
		if (!templateIdStyles.contains(STYLEID_ANTI_ENDORSED_CANDIDATE_NAME)) {
			logger.error("dotx template missing this styleId: " + STYLEID_ANTI_ENDORSED_CANDIDATE_NAME);
			STYLEID_ANTI_ENDORSED_CANDIDATE_NAME = STYLEID_NORMAL;
		}
		if (!templateIdStyles.contains(STYLEID_WRITE_IN_CANDIDATE_NAME)) {
			logger.error("dotx template missing this styleId: " + STYLEID_WRITE_IN_CANDIDATE_NAME);
			STYLEID_WRITE_IN_CANDIDATE_NAME = STYLEID_NORMAL;
		}
		if (!templateIdStyles.contains(STYLEID_BOTTOM_BORDER)) {
			logger.error("dotx template missing this styleId: " + STYLEID_BOTTOM_BORDER);
			STYLEID_BOTTOM_BORDER = STYLEID_NORMAL;
		}
		if (!templateIdStyles.contains(STYLEID_COLUMN_BREAK_PARAGRAPH)) {
			logger.error("dotx template missing this styleId: " + STYLEID_COLUMN_BREAK_PARAGRAPH);
			STYLEID_COLUMN_BREAK_PARAGRAPH = STYLEID_NORMAL;
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
	 * isPlaceholderNameMatch determines if there is a name match.
	 * 
	 * @param ph Placeholder object.mdp
	 * @param name placeholder name.
	 * @return true => match between the placeholder name and name.
	 */
	static boolean isPlaceholderNameMatch(Placeholder ph, String name) {
		return ph.getName().equals(name);
	}
	/**
	 * genHeader generates all values for placeholders in the Header part.
	 */
	void genHeader() {
		for (Placeholder ph: phProcessor.getHeaderPlaceholders()) {
			for (String name: placeholderNames) {
				if (isPlaceholderNameMatch(ph, name)) {
					P paragraph = genPlaceholderValue(name, ph, "Header", docx.getMainDocumentPart());
					phProcessor.replaceContent(ph, List.of(paragraph));
				}
			}
		}
	}
	/**
	 * genFooter generates all values for placeholders in the Footer part.
	 */
	void genFooter() {
		for (Placeholder ph: phProcessor.getFooterPlaceholders()) {
			for (String name: placeholderNames) {
				if (isPlaceholderNameMatch(ph, name)) {
					P paragraph = genPlaceholderValue(name, ph, "Footer", docx.getMainDocumentPart());
					phProcessor.replaceContent(ph, List.of(paragraph));
				}
			}
		}
	}
	/**
	 * genBody generates all values for placeholders in the main Body part.
	 * Note:  The main body MUST have a "Contests" placeholder.
	 */
	void genBody() {
		for (Placeholder ph: phProcessor.getBodyPlaceholders()) {
			for (String name: placeholderNames) {
				if (isPlaceholderNameMatch(ph, name)) {
					if (name.equals(PLACEHOLDER_CONTESTS)) {
						phProcessor.replaceContent(ph, genContests());
					} else if (name.equals(PLACEHOLDER_REFERENDUMS)) {
						// Are there referendums?
						phProcessor.replaceContent(ph, genReferendums());
					} else if (name.equals(PLACEHOLDER_RETENTIONS)) {
						// Are there retentions?
						phProcessor.replaceContent(ph, genRetentions());
					} else {
						P paragraph = genPlaceholderValue(name, ph, "Normal", docx.getMainDocumentPart());
						phProcessor.replaceContent(ph, List.of(paragraph));
					}
				}
			}
		}
	}
	/**
	 * genPlaceholderValue generates a paragraph that represents a simple placeholder value.
	 * The value is known at the time the ballot for the precinct is generated.
	 * Note:
	 * 1) This is called by genHeader and genFooter, but not by genBody.
	 * 
	 * @param name of placeholder
	 * @param ph a "found" placeholder.  Need to get paragraph (P) reference.
	 * @param style default MS Word style.
	 * @param mdp MainDocumentPart.
	 * @return single styled paragraph.
	 */
	P genPlaceholderValue(String name, Placeholder ph, String style, MainDocumentPart mdp) {
		P paragraph = null;
		switch (name) {
		case PLACEHOLDER_PRECINCT_NO:
			paragraph = GenDocx.genStyledParagraph(precinctNo, ph.getReplaceParagraph(), style, mdp);
			break;
		case PLACEHOLDER_PRECINCT_NAME:
			paragraph = GenDocx.genStyledParagraph(precinctName, ph.getReplaceParagraph(), style, mdp);
			break;
		case PLACEHOLDER_PRECINCT_NO_NAME:
			paragraph = GenDocx.genStyledParagraph(precinctNoName.replace("_", " "), ph.getReplaceParagraph(), style, mdp);
			break;
		case PLACEHOLDER_CONTESTS:
			logger.error("Cannot generate \"Contests\" content here");
			break;
		case PLACEHOLDER_REFERENDUMS:
			logger.error("Cannot generate \"Referendums\" content here");
			break;
		case PLACEHOLDER_RETENTIONS:
			logger.error("Cannot generate \"Retentions\" content here");
			break;
		case PLACEHOLDER_ZONE_NO:
			paragraph = GenDocx.genStyledParagraph(zoneNo, ph.getReplaceParagraph(), style, mdp);
			break;
		case PLACEHOLDER_ZONE_NAME:
			paragraph = GenDocx.genStyledParagraph(zoneName, ph.getReplaceParagraph(), style, mdp);
			break;
		case PLACEHOLDER_ZONE_LOGO:
			// Notes:
			// The precinct-zone CSV file must be correct. In particular,
			// the file path to the zone logo must be correct relative to 
			// the directory from which ballogen program is run.
			Zone zone = Initialize.precinctToZoneMap.get(precinctNo);
			String zoneLogoPath = zone.getZoneLogoPath();
			// pass sourcePart as last parameter
			paragraph = GenDocx.genImageParagraph(zoneLogoPath, docx, phProcessor.getLocPart(ph.getLoc()));
			break;
		}	
		return paragraph;
	}

	/**
	 * genBallotInstructions generates the ballot instructions provided by Voter Services.
	 * Note: Not used but a useful technique.
	 */
	/* private */
	void genBallotInstructions() {
		MainDocumentPart mdp = docx.getMainDocumentPart();
		GenDocx.genWmlChunk(mdp, "ballot_instructions.wml");
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
		Utils.logLines(logger, DEBUG, "contestLines:", contestLines);
		ContestNameCounter ballotFactory = new ContestNameCounter(ballotText);
		int i = 0;
		int j = 0;
		for (String line: contestLines) {
			String [] elements = line.split(",");
			String contestName = elements[0];
			contestName = Contest.processContestName(contestName);
			List<P> contestParagraphs = new ArrayList<>();
			// Insert column break before contest?
			if (Initialize.columnBreaks.indexOf(contestName) >= 0) {
				MainDocumentPart mdp = docx.getMainDocumentPart();
				P columnBreakParagraph = GenDocx.genColumnBreakParagraph(mdp);
				contestParagraphs.add(columnBreakParagraph);
			}
			String contestFormat = elements[1];
			// Is there a "page break" in the ballot?
			// Test if a pseudo contest box should be generated.
			contestParagraphs.addAll(contestName.equals(Initialize.PAGE_BREAK)?
					  GenDocx.genPageBreak(docx.getMainDocumentPart())
					: genContest(ballotFactory, contestName, contestFormat));
//			// Insert column break after contest?
//			if (i+1 == Initialize.columnBreaks[j]) {
//				MainDocumentPart mdp = docx.getMainDocumentPart();
//				P columnBreakParagraph = GenDocx.genColumnBreakParagraph(mdp);
//				contestParagraphs.add(columnBreakParagraph);
//				j++;
//			}
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
			contestParagraphs = genContestParagraphs(mdp, contest);
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
		String style = !contest.getName().equals(Contest.GENERIC_CONTEST.getName())?
				STYLEID_CONTEST_TITLE:
				STYLEID_CONTEST_GENERIC_TITLE;	
		newParagraph = mdp.createStyledParagraphOfText(style, contest.getName());
		headParagraphs.add(newParagraph);
		style = !contest.getTerm().equals(Contest.GENERIC_CONTEST.getTerm())?
				STYLEID_CONTEST_INSTRUCTIONS:
				STYLEID_CONTEST_GENERIC_INSTRUCTIONS;	
		if (!contest.getTerm().isEmpty()) {
			newParagraph = mdp.createStyledParagraphOfText(style, contest.getTerm());
			headParagraphs.add(newParagraph);
		}
		newParagraph = mdp.createStyledParagraphOfText(style, contest.getInstructions());
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
		if (mode == ENDORSED || mode == UNENDORSED ) {
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
			if (Initialize.writeInDisplay) {
				newParagraph = mdp.createStyledParagraphOfText(STYLEID_CANDIDATE_NAME, whiteEllipse + "   " + "_".repeat(16));
				writeinParagraphs.add(newParagraph);
				newParagraph = mdp.createStyledParagraphOfText(STYLEID_CANDIDATE_PARTY, "Write-in");
				writeinParagraphs.add(newParagraph);
			}
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
		// TODO: test this.
//		newParagraph = mdp.createStyledParagraphOfText(STYLEID_WRITE_IN_CANDIDATE_NAME, text);
		newParagraph = GenDocx.genStyledTextWithinParagraph(TextStyle.UNDERLINE, blackEllipse + " ", name, " ");
		candParagraphs.add(newParagraph);
		newParagraph = mdp.createStyledParagraphOfText(STYLEID_CANDIDATE_PARTY, "Write-in");
		candParagraphs.add(newParagraph);
		return candParagraphs;
	}
	/** 
	 * genReferendums generates the referendum boxes (like contest boxes) within the docx.
	 * 
	 * @return list of referendum paragraphs.
	 */
	List<P> genReferendums() {
		logger.debug("generating referendums for document");
		List<P> referendumParagraphs = new ArrayList<>();
		if (referendumsText != null) {
			String[] referendumLines = referendumsText.split("\n");
			Utils.logLines(logger, DEBUG, "referendumLines:", referendumLines);
			MainDocumentPart mdp = docx.getMainDocumentPart();
			for (String line: referendumLines) {
				// Each line is a referendum question
				String refQuestion = line;
				referendumParagraphs.addAll(genReferendum(mdp, refQuestion));
			}
		}
		return referendumParagraphs;
	}
	/**
	 * genReferendum generates a single referendum box within the docx.
	 * Notes: ballotgen.referendum.format regex used here.
	 * 
	 * @param mdp main document part.
	 * @param refQuestion referendum question.
	 * @return list of paragraphs.
	 */
	List<P> genReferendum(MainDocumentPart mdp, String refQuestion) {
		List<P> referendumParagraphs = new ArrayList<>();
		// retrofit the refQuestion to the referendumFormat (replace %question% )
		final String REF_QUESTION = "%question%";
		String regex = Initialize.referendumFormat.replace(REF_QUESTION, refQuestion);
		Pattern compiledRegex = Utils.compileRegex(regex);	
		Matcher m = compiledRegex.matcher(ballotText);
		if (m.find() ) {
			referendumParagraphs = genReferendumParagraphs(mdp, refQuestion, m.group("text"));
		} else {
			logger.error(String.format("no match for ref. question %s", refQuestion));
		}
		return referendumParagraphs;
	}
	/**
	 * genReferendumParagraphs generates the paragraphs of the referendum questions.
	 * 
	 * Notes:
	 * 1) conversion for printing and conversion for endorsement is tricky.
	 * 
	 */
	List<P> genReferendumParagraphs(MainDocumentPart mdp, String question, String text) {
		P newParagraph;
		List<P> refParagraphs = new ArrayList<>();
		String style = STYLEID_CONTEST_TITLE;
		// conversion for printing
		String question1 = Contest.processContestName(question);
		newParagraph = mdp.createStyledParagraphOfText(style, question1);
		refParagraphs.add(newParagraph);
		style = STYLEID_NORMAL;
		newParagraph = mdp.createStyledParagraphOfText(style, text);
		refParagraphs.add(newParagraph);
		// conversion for endorsement.
		String question2 = question1.replaceAll("\n", " ");
		refParagraphs.addAll(genYesNoParagraphs(mdp, question2));
		return refParagraphs;
	}
	/** 
	 * genRetentions generates the retention boxes (like contest boxes) within the docx.
	 * 
	 * @return list of retention paragraphs.
	 */
	List<P> genRetentions() {
		logger.debug("generating retentions for document");
		List<P> retentionParagraphs = new ArrayList<>();
		if (retentionsText != null) {
			String[] retentionLines = retentionsText.split("\n");
			Utils.logLines(logger, DEBUG, "retentionLines:", retentionLines);
			MainDocumentPart mdp = docx.getMainDocumentPart();
			for (String line: retentionLines) {
				// Each line is a referendum question
				String [] elements = line.split(",");
				String officeName = elements[0];
				String judgeName = elements[1];
				retentionParagraphs.addAll(genRetention(mdp, officeName, judgeName));
			}
		}
		return retentionParagraphs;
	}
	/**
	 * genRetention generates a single retention box within the docx.
	 * Notes: ballotgen.retention.format regex used here.
	 * 
	 * @param mdp main document part.
	 * @param officeName judicial office name.
	 * @param judgeName judge's name.
	 * @return list of paragraphs.
	 */
	List<P> genRetention(MainDocumentPart mdp, String officeName, String judgeName) {
		List<P> retentionParagraphs = new ArrayList<>();
		// retrofit both the office name and the judge name to retentionFormat (replace %office name% and %judge name%)
		final String RET_OFFICE_NAME = "%office name%";
		final String RET_JUDGE_NAME = "%judge name%";
		String regex = Initialize.retentionFormat.replace(RET_OFFICE_NAME, officeName);
		regex = regex.replace(RET_JUDGE_NAME, judgeName);
		Pattern compiledRegex = Utils.compileRegex(regex);
		Matcher m = compiledRegex.matcher(ballotText);
		if (m.find() ) {
			retentionParagraphs = genRetentionParagraphs(mdp, officeName, judgeName, m.group(1));
		} else {
			logger.error(String.format("no match for retention office: %s judge: %s", officeName, judgeName));
		}
		return retentionParagraphs;
	}
	/**
	 * genRetentionParagraphs generates the paragraphs of the retention questions.
	 * 
	 * @param mdp MainDocumentPart.
	 * @param officeName Judgeship office name.
	 * @param judgeName Judge up for retention.
	 * @param question Retention question.
	 * 
	 * @return list of paragraphs.
	 */
	List<P> genRetentionParagraphs(MainDocumentPart mdp, String officeName, String judgeName, String question) {
		P newParagraph;
		List<P> retParagraphs = new ArrayList<>();
		String style = STYLEID_CONTEST_TITLE;
		// massage the office name string.
		officeName = Contest.processContestName(officeName);
		newParagraph = mdp.createStyledParagraphOfText(style, officeName);
		retParagraphs.add(newParagraph);
		style = STYLEID_NORMAL;
		// TODO: find the judge name and use a BOLD font.
//		newParagraph = mdp.createStyledParagraphOfText(style, question);
		newParagraph = genRetentionQuestionParagraph(judgeName, question);
		retParagraphs.add(newParagraph);
		retParagraphs.addAll(genYesNoParagraphs(mdp, judgeName));
		return retParagraphs;
	}
	/**
	 * genRetentionQuestionParagraph generates the retention question with the judge's name bolded.
	 * Notes: The judge's name is assumed to be within the retention question.
	 * 
	 * @param mdp MainDocumment part.
	 * @param judgeName judge's name
	 * @param question retention question.
	 * 
	 * @return paragraph P.
	 */
	P genRetentionQuestionParagraph(String judgeName, String question) {
		P newParagraph = null;
		int index = question.indexOf(judgeName);
		if (index < 0) {
			logger.error(String.format("judge: %s not found in retention question: %s", judgeName, question));
			return newParagraph;
		}
		String questionStart = question.substring(0, index);
		String questionEnd = question.substring(index + judgeName.length());
		newParagraph = GenDocx.genStyledTextWithinParagraph(TextStyle.BOLD, questionStart, judgeName, questionEnd);
		return newParagraph;
	}
	/**
	 * genYesNoParagraphs generates the paragraphs for the YES/NO recommendations
	 *   for referendum questions and judge retentions.
	 *   Note:
	 *   1) The referendum question or retention judge is the endorsee here.
	 *   
	 * @param mdp MainDocument part.
	 * @param endorsee recipient of an Endorsement object.
	 * @return list of paragraphs.
	 */
	List<P> genYesNoParagraphs(MainDocumentPart mdp, String endorsee) {
		P newParagraph;
		// YES / NO recommendation here
		List<P> yesNoParagraphs = new ArrayList<>();
		String style = STYLEID_NORMAL;
		EndorsementMode mode = endorsementProcessor.getEndorsementMode(endorsee, precinctNo);
		endorsedCandidates.merge(endorsee.toUpperCase(), 1, (prev, inc) -> prev + inc);
		String yesStr = " YES";
		String noStr  = " NO";
		switch (mode) {
		case ENDORSED:
			yesStr = blackEllipse + yesStr;
			noStr  = whiteEllipse + noStr;
			break;
		case UNENDORSED:
			yesStr = whiteEllipse + yesStr;
			noStr  = whiteEllipse + noStr;
			break;
		case ANTIENDORSED:
			yesStr = whiteEllipse + yesStr;
			noStr  = blackEllipse + noStr;
			break;
		}
		newParagraph = mdp.createStyledParagraphOfText(style, yesStr);
		yesNoParagraphs.add(newParagraph);
		newParagraph = mdp.createStyledParagraphOfText(style, noStr);
		yesNoParagraphs.add(newParagraph);		
		// Draw a border line as a separator
		newParagraph = mdp.createStyledParagraphOfText(STYLEID_BOTTOM_BORDER,null);
		yesNoParagraphs.add(newParagraph);
		return yesNoParagraphs;
	}
}
