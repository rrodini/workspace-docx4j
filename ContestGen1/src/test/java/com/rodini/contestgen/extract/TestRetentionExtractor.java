package com.rodini.contestgen.extract;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.rodini.ballotutils.Utils;
import com.rodini.contestgen.common.Initialize;
import com.rodini.contestgen.model.Retention;

class TestRetentionExtractor {
	String page1Text =
	"""
	JUSTICE OF THE SUPREME COURT
	Vote for ONE
	DWAYNE WOODRUFF
	DEMOCRATIC
	SALLIE MUNDY
	REPUBLICAN
	Write-in
	JUDGE OF THE SUPERIOR COURT
	Vote for not more than FOUR
	MARIA MCLAUGHLIN
	DEMOCRATIC
	CAROLYN H NICHOLS
	DEMOCRATIC
	DEBBIE KUNSELMAN
	DEMOCRATIC
	GEOFF MOULTON
	DEMOCRATIC
	CRAIG STEDMAN
	REPUBLICAN
	EMIL GIORDANO
	REPUBLICAN
	WADE A KAGARISE
	REPUBLICAN
	MARY MURRAY
	REPUBLICAN
	JULES MERMELSTEIN
	GREEN
	Write-in
	Write-in
	Write-in
	Write-in
	JUDGE OF THE COMMONWEALTH
	COURT
	Vote for not more than TWO
	ELLEN CEISLER
	DEMOCRATIC
	IRENE M CLARK
	DEMOCRATIC
	PAUL LALLEY
	REPUBLICAN
	CHRISTINE FIZZANO CANNON
	REPUBLICAN
	Write-in
	Write-in
	TREASURER
	Vote for ONE
	PATRICIA MAISANO
	DEMOCRATIC
	JACK LONDON
	REPUBLICAN
	Write-in
	CONTROLLER
	Vote for ONE
	MARGARET REIF
	DEMOCRATIC
	NORMAN MACQUEEN
	REPUBLICAN
	Write-in
	CLERK OF COURTS
	Vote for ONE
	YOLANDA VAN DE KROL
	DEMOCRATIC
	ROBIN MARCELLO
	REPUBLICAN
	Write-in
	CORONER
	Vote for ONE
	CHRISTINA VANDEPOL
	DEMOCRATIC
	GORDON R ECK
	REPUBLICAN
	Write-in
	MAGISTERIAL DISTRICT JUDGE
	DISTRICT 15-1-01
	Vote for ONE
	BRET BINDER
	DEMOCRATIC
	MARK A BRUNO
	REPUBLICAN
	Write-in
	TOWNSHIP SUPERVISOR
	WEST BRADFORD TOWNSHIP
	Vote for ONE
	BRUCE DURNAN
	DEMOCRATIC
	LAURIE ABELE
	REPUBLICAN
	Write-in
	TAX COLLECTOR
	WEST BRADFORD TOWNSHIP
	Vote for ONE
	JOHN J BOSSONG III
	DEMOCRATIC/REPUBLICAN
	Write-in
	AUDITOR
	WEST BRADFORD TOWNSHIP
	Vote for ONE
	TRACY L CHRISTMAN
	REPUBLICAN
	Write-in
	""";

	
	String page2Text =
	"""
	025 WEST BRADFORD 1
	OFFICIAL RETENTION QUESTIONS
	INSTRUCTIONS TO VOTER
	To vote in FAVOR of the retention, blacken
	the oval to the left of the word YES.
	To vote AGAINST the retention, blacken the
	oval to the left of the word NO.
	VOTE ON EACH OF THE FOLLOWING
	JUDICIAL QUESTIONS
	JUDGE OF ELECTIONS
	Vote for ONE
	Write-in
	INSPECTOR OF ELECTIONS
	Vote for ONE
	Write-in
	PROPOSED CONSTITUTIONAL
	AMENDMENT
	AMENDING THE HOMESTEAD
	PROPERTY TAX ASSESSMENT
	EXCLUSION
	"Shall the Pennsylvania
	Constitution be amended to
	permit the General Assembly to
	enact legislation authorizing local
	taxing authorities to exclude from
	taxation up to 100 percent of the
	assessed value of each
	homestead property within a local
	taxing jurisdiction, rather than
	limit the exclusion to one-half of
	the median assessed value of all
	homestead property, which is the
	existing law?"
	YES
	NO
	WEST BRADFORD TOWNSHIP
	OPEN SPACE REFERENDUM
	"Do you favor the imposition of a
	tax on earned income of
	Township residents by West
	Bradford Township at the rate of
	one-quarter of one percent
	annually to be used to preserve,
	conserve, and acquire open
	space property interest and
	benefits?"
	YES
	NO
	JUSTICE OF THE SUPREME
	COURT RETENTION ELECTION
	"Shall THOMAS G SAYLOR be
	retained for an additional term as
	Justice of the Supreme Court of
	the Commonwealth of
	Pennsylvania?"
	YES
	NO
	JUSTICE OF THE SUPREME
	COURT RETENTION ELECTION
	"Shall DEBRA TODD be retained
	for an additional term as Justice
	of the Supreme Court of the
	Commonwealth of
	Pennsylvania?"
	YES
	NO
	JUDGE OF THE SUPERIOR
	COURT RETENTION ELECTION
	"Shall JACQUELINE O SHOGAN
	be retained for an additional term
	as Judge of the Superior Court of
	the Commonwealth of
	Pennsylvania?"
	YES
	NO
	JUDGE OF THE COURT OF
	COMMON PLEAS RETENTION ELECTION
	"Shall DAVID F BORTNER be
	retained for an additional term as
	Judge of the Court of Common
	Pleas, 15th Judicial District,
	Chester County?"
	YES
	NO
	JUDGE OF THE COURT OF
	COMMON PLEAS RETENTION ELECTION
	"Shall KATHERINE B L PLATT
	be retained for an additional term
	as Judge of the Court of Common
	Pleas, 15th Judicial District,
	Chester County?"
	YES
	NO
	""";
	
	@BeforeAll
	static void setUpBeforeClass() throws Exception {
		Initialize.retentionQuestionRegex = Utils.compileRegex("(?m)^(?<question>(?<office>(.*)\n.*RETENTION ELECTION)((.*)\n)*?^YES\nNO)");
		Initialize.retentionNameRegex = Utils.compileRegex("(?m)((.*)\n)*^\"Shall (?<name>(.*))(\\ |\n)be(\\ |\n)retained.*\n((.*)\n)*^YES\nNO");
	}

	@AfterAll
	static void tearDownAfterClass() throws Exception {
	}

	@BeforeEach
	void setUp() throws Exception {
	}

	@AfterEach
	void tearDown() throws Exception {
	}

	@Test
	void testExtractRetentions1() {
		List<Retention> rets = RetentionExtractor.extractRetentions("025", "WEST_BRADFORD_1", page2Text);
		assertEquals(5, rets.size());
		// check first retention
		Retention first = rets.get(0);
		assertEquals("JUSTICE OF THE SUPREME\nCOURT RETENTION ELECTION", first.getOfficeName());
		assertEquals("THOMAS G SAYLOR", first.getJudgeName());
		// check last retention
		Retention last = rets.get(rets.size()-1);
		assertEquals("JUDGE OF THE COURT OF\nCOMMON PLEAS RETENTION ELECTION", last.getOfficeName());
		assertEquals("KATHERINE B L PLATT", last.getJudgeName());
	}

	@Test
	void testExtractRetentions2() {
		List<Retention> rets = RetentionExtractor.extractRetentions("025", "WEST_BRADFORD_1", "");
		assertEquals(0, rets.size());
	}

}
