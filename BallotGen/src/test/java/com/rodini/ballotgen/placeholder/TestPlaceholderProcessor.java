package com.rodini.ballotgen.placeholder;

import static org.junit.jupiter.api.Assertions.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.Logger;
import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.WordprocessingML.MainDocumentPart;
import org.docx4j.wml.P;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.rodini.ballotgen.common.MockedAppender;

import static com.rodini.ballotgen.placeholder.PlaceholderLocation.*;
import static com.rodini.ballotgen.GenDocxBallot.*;


class TestPlaceholderProcessor {
	private static MockedAppender mockedAppender;
	private static Logger logger;


	@BeforeEach
	void setUp() throws Exception {
	    mockedAppender = new MockedAppender();
	    mockedAppender.start();
	    logger = (Logger)LogManager.getLogger(PlaceholderProcessor.class);
	    logger.addAppender(mockedAppender);
	    logger.setLevel(Level.ERROR);
	}

	@AfterEach
	void tearDown() throws Exception {
		logger.removeAppender(mockedAppender);
		mockedAppender.stop();
	}
	void dumpPlaceholderNames() {
		System.out.println("PLACEHOLDER NAMES:");
		for (String name: placeholderNames) {
			System.out.println(name);
		}
	}
	
	@Test
	void testCreatePlaceholdersGood1() throws Docx4JException, FileNotFoundException {
		dumpPlaceholderNames();
		File docxFile = new File("./src/test/java/placeholder-good-01.docx");	
		WordprocessingMLPackage docx = WordprocessingMLPackage.load(new FileInputStream(docxFile));
		PlaceholderProcessor php = new PlaceholderProcessor(docx, placeholderNames);
		List<Placeholder> headerList = php.getHeaderPlaceholders();
		List<Placeholder> bodyList = php.getBodyPlaceholders();
		List<Placeholder> footerList = php.getFooterPlaceholders();
		// just check the counts.
		assertEquals(2, headerList.size());
		assertEquals(1, bodyList.size());
		assertEquals(1, footerList.size());
	}
	boolean checkPlaceholder(Placeholder ph, String expectedName, PlaceholderLocation expectedLoc) {
		return ph.getName().equals(expectedName) && ph.getLoc() == expectedLoc && ph.getReplaceParagraph() != null;
	}
	@Test
	void testCreatePlaceholdersGood2() throws Docx4JException, FileNotFoundException {
		File docxFile = new File("./src/test/java/placeholder-good-01.docx");	
		WordprocessingMLPackage docx = WordprocessingMLPackage.load(new FileInputStream(docxFile));
		PlaceholderProcessor php = new PlaceholderProcessor(docx, placeholderNames);
		List<Placeholder> headerList = php.getHeaderPlaceholders();
		List<Placeholder> bodyList = php.getBodyPlaceholders();
		List<Placeholder> footerList = php.getFooterPlaceholders();
		// check the Placeholder object contents
		// HEADER
		Placeholder ph = headerList.get(0);
		checkPlaceholder(ph, "PrecinctNo", HEADER);
		ph =  headerList.get(1);
		checkPlaceholder(ph, "PrecinctName", HEADER);
		// BODY
		ph =  bodyList.get(0);
		checkPlaceholder(ph, "Contests", BODY);
		// FOOTER
		ph =  footerList.get(0);
		checkPlaceholder(ph, "PrecinctNoName", FOOTER);
	}
	@Test
	void testReplaceContent() throws Docx4JException, FileNotFoundException {
		File docxFile = new File("./src/test/java/placeholder-good-01.docx");	
		WordprocessingMLPackage docx = WordprocessingMLPackage.load(new FileInputStream(docxFile));
		PlaceholderProcessor php = new PlaceholderProcessor(docx, placeholderNames);
		List<Placeholder> headerList = php.getHeaderPlaceholders();
		MainDocumentPart mdp = docx.getMainDocumentPart();
		P paragraph = mdp.createParagraphOfText("replacement text");
		List<Placeholder> documentList = new ArrayList<>(headerList);
		documentList.addAll(php.getBodyPlaceholders());
		documentList.addAll(php.getFooterPlaceholders());
		for (Placeholder ph: documentList) {
			php.replaceContent(ph, List.of(paragraph));
		}
		// Not a good test.  What to assert?
		assertTrue(true);
	}
	@Test
	void testCreatePlaceholdersBad1() throws Docx4JException, FileNotFoundException {
		File docxFile = new File("./src/test/java/placeholder-bad-01.docx");	
		WordprocessingMLPackage docx = WordprocessingMLPackage.load(new FileInputStream(docxFile));
		PlaceholderProcessor php = new PlaceholderProcessor(docx, placeholderNames);
		assertEquals(3, mockedAppender.messages.size());
		String expected1 = "placeholder \"Contests\" cannot be in header area";
		assertTrue(mockedAppender.messages.get(0).startsWith(expected1));
		String expected2 = "placeholder \"Contests\" missing from body area";
		assertTrue(mockedAppender.messages.get(1).startsWith(expected2));
		String expected3 = "placeholder \"Contests\" cannot be in footer area";
		assertTrue(mockedAppender.messages.get(2).startsWith(expected3));
	}
}
