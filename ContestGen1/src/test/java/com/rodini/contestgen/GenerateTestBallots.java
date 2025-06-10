package com.rodini.contestgen;

import static org.apache.logging.log4j.Level.DEBUG;
import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.junit.jupiter.api.Test;

import com.rodini.ballotutils.Utils;
import com.rodini.contestgen.ContestGen1;
import com.rodini.contestgen.extract.ContestExtractor;
import com.rodini.contestgen.extract.ReferendumExtractor;
import com.rodini.contestgen.extract.RetentionExtractor;
import com.rodini.contestgen.model.Ballot;
import com.rodini.voteforprocessor.extract.Initialize;
import com.rodini.voteforprocessor.model.Contest;
import com.rodini.voteforprocessor.model.Referendum;
import com.rodini.voteforprocessor.model.Retention;

class GenerateTestBallots {

static String PRIMARY_2025_350_MALVERN_PAGE1 = 
"""
Judge of the Superior Court
Vote for One
Brandon Neuman
Washington County
Write-in
Judge of the Commonwealth Court
Vote for One
Stella Tsai
Philadelphia County
Write-in
Judge of the Court of Common Pleas
15th Judicial District
Vote for not more than Two
Mackenzie Smith
Willistown Township
Clay Cauley, Sr.
London Grove Township
Write-in
Write-in
Clerk of Courts
Vote for One
Caroline Bradley
Willistown Township
Write-in
Controller
Vote for One
Nick Cherubino
Uwchlan Township
Write-in
Coroner
Vote for One
Sophia Garcia-Jackson
West Whiteland Township
Write-in
Treasurer
Vote for One
Patricia A. Maisano
Kennett Township
Write-in
School Director
Great Valley Region 2
Vote for One
Lorie Sollenberger
Write-in
Member of Council
Malvern Borough
Vote for not more than Three
Angela Riccetti
Zoe Warner
Dan Kunze
Write-in
Write-in
Write-in
Mayor
Malvern Borough
Vote for One
Zeyn B. Uzman
Pete Papadopoulos
Write-in
Judge of Elections
350 Malvern
Vote for One
Hugo Schmitt
Write-in
Inspector of Elections
350 Malvern
Vote for One
Vicki Sharpless
Write-in
""";
static String PRIMARY_2025_350_MALVERN_PAGE2 = 
"""
""";
static String PRIMARY_2025_065_CHARLESTOWN_PAGE1 =
"""
Judge of the Superior Court
Vote for One
Brandon Neuman
Washington County
Write-in
Judge of the Commonwealth Court
Vote for One
Stella Tsai
Philadelphia County
Write-in
Judge of the Court of Common Pleas
15th Judicial District
Vote for not more than Two
Mackenzie Smith
Willistown Township
Clay Cauley, Sr.
London Grove Township
Write-in
Write-in
Clerk of Courts
Vote for One
Caroline Bradley
Willistown Township
Write-in
Controller
Vote for One
Nick Cherubino
Uwchlan Township
Write-in
Coroner
Vote for One
Sophia Garcia-Jackson
West Whiteland Township
Write-in
Treasurer
Vote for One
Patricia A. Maisano
Kennett Township
Write-in
Magisterial District Judge
District 15-2-01
Vote for One
James C. Kovaleski
Write-in
School Director
Great Valley Region 1
Vote for not more than Two
Andrea Rizzo
Stacey Kahan
Write-in
Write-in
Township Supervisor
Charlestown Township
Vote for One
Hugh D. Willig
Write-in
Township Supervisor
Unexpired 4 Year Term
Charlestown Township
Vote for One
Madeleine Carlson
Write-in
Township Supervisor
Unexpired 2 Year Term
Charlestown Township
Vote for One
Jill Green
Louis Rubinfield
Write-in
Auditor
Charlestown Township
Vote for One
Write-in
Auditor Unexpired 4 Year Term
Charlestown Township
Vote for One
Write-in
Tax Collector
Charlestown Township
Vote for One
Write-in 
""";
static String PRIMARY_2025_065_CHARLESTOWN_PAGE2 =
"""
Judge of Elections
065 Charlestown
Vote for One
Write-in
Inspector of Elections
065 Charlestown
Vote for One
Deborah Kuhn
Write-in
""";

static String GENERAL_2024_385_NEW_GARDEN_PAGE1 = 
"""
Presidential Electors
Vote for the candidates of one
party for President and
Vice-President, or insert the
names of candidates.
Kamala D. Harris
President, Democratic
Tim Walz
Vice-President, Democratic
Donald J. Trump
President, Republican
JD Vance
Vice-President, Republican
Chase Oliver
President, Libertarian
Mike ter Maat
Vice-President, Libertarian
Jill Stein
President, Green Party
Rudolph Ware
Vice-President, Green Party
Write-in
United States Senator
Vote for One
Robert P. Casey, Jr.
Democratic
Dave McCormick
Republican
John C. Thomas
Libertarian
Leila Hazou
Green Party
Marty Selker
Constitution Party
Write-in
Attorney General
Vote for One
Eugene DePasquale
Democratic
Dave Sunday
Republican
Robert Cowburn
Libertarian
Richard L. Weiss
Green Party
Justin L. Magill
Constitution Party
Eric L. Settle
Forward Party
Write-in
Auditor General
Vote for One
Malcolm Kenyatta
Democratic
Tim DeFoor
Republican
Reece Smith
Libertarian
Eric K. Anton
American Solidarity Party
Bob Goodrich
Constitution Party
Write-in
State Treasurer
Vote for One
Erin McClelland
Democratic
Stacy Garrity
Republican
Nickolas Ciesielski
Libertarian
Troy Bowman
Constitution Party
Chris Foster
Forward Party
Write-in
Representative in Congress
6th District
Vote for One
Chrissy Houlahan
Democratic
Neil Young
Republican
Write-in
Senator in the General
Assembly
9th District
Vote for One
John I. Kane
Democratic
Mike Woodin
Republican
Write-in
Representative in the General
Assembly
158th District
Vote for One
Christina D. Sappey
Democratic
Tina Ayala
Republican
Write-in
""";
static String GENERAL_2024_385_NEW_GARDEN_PAGE2 =
"""
New Garden Township:
Library Tax Referendum
Do you favor increasing New Garden
Township's existing Real Estate
Property Tax dedicated to Kennett
Library by 0.100 mills, for a total of
0.200 mills, with the revenue from
such increase to be used exclusively
to fund the operation of the Kennett
Library? The current real estate
property tax is 2.77 mills.
Yes
No
""";

static String GENERAL_2024_653_UWCHLAN_1_PAGE1=
"""
Presidential Electors
Vote for the candidates of one
party for President and
Vice-President, or insert the
names of candidates.
Kamala D. Harris
President, Democratic
Tim Walz
Vice-President, Democratic
Donald J. Trump
President, Republican
JD Vance
Vice-President, Republican
Chase Oliver
President, Libertarian
Mike ter Maat
Vice-President, Libertarian
Jill Stein
President, Green Party
Rudolph Ware
Vice-President, Green Party
Write-in
United States Senator
Vote for One
Robert P. Casey, Jr.
Democratic
Dave McCormick
Republican
John C. Thomas
Libertarian
Leila Hazou
Green Party
Marty Selker
Constitution Party
Write-in
Attorney General
Vote for One
Eugene DePasquale
Democratic
Dave Sunday
Republican
Robert Cowburn
Libertarian
Richard L. Weiss
Green Party
Justin L. Magill
Constitution Party
Eric L. Settle
Forward Party
Write-in
Auditor General
Vote for One
Malcolm Kenyatta
Democratic
Tim DeFoor
Republican
Reece Smith
Libertarian
Eric K. Anton
American Solidarity Party
Bob Goodrich
Constitution Party
Write-in
State Treasurer
Vote for One
Erin McClelland
Democratic
Stacy Garrity
Republican
Nickolas Ciesielski
Libertarian
Troy Bowman
Constitution Party
Chris Foster
Forward Party
Write-in
Representative in Congress
6th District
Vote for One
Chrissy Houlahan
Democratic
Neil Young
Republican
Write-in
Representative in the General
Assembly
155th District
Vote for One
Danielle Friel Otten
Democratic
Rodney Simon
Republican
Write-in
""";
static String GENERAL_2024_653_UWCHLAN_1_PAGE2=
"""
Uwchlan Township:
Open Space Referendum
Do you favor the imposition of an
increase in the earned income tax at
a rate not to exceed one quarter of
one percent by the Township of
Uwchlan to be used to purchase
interest in real property for purposes
of securing open space benefits and
for transactional fees incidental to
acquisitions of open space property;
retire indebtedness incurred in
acquiring open space; and the
expenditure of funds for any purpose
relating to the acquisition, planning for
acquisition, preservation,
improvement, and maintenance of
open space or for open space
benefits?
Yes
No
""";

	static Ballot genBallot1() {
		// one ballot - 2025 PRIMARY: 350_MALVERN 
		// use 2025 Primary Regexes
		com.rodini.contestgen.ContestGen1.COUNTY = Utils.getEnvVariable("BALLOTGEN_COUNTY", true);
		Properties contestGenProps = Utils.loadProperties("./src/test/java/primary-2025-contestgen.properties");
		com.rodini.contestgen.common.Initialize.validateProperties(contestGenProps);
		Ballot ballot = new Ballot("350_MALVERN", "");
		ballot.setPage1Text(PRIMARY_2025_350_MALVERN_PAGE1);
		ballot.setPage2Text(PRIMARY_2025_350_MALVERN_PAGE2);
		ContestExtractor.extractContests(ballot);
		return ballot;
	}
	
	static List<Ballot> genBallot2() {
		// two ballot - 2025 PRIMARY: 350_MALVERN, 065_CHARLESTOWN
		// use 2025 Primary Regexes
		List<Ballot> ballots = new ArrayList<Ballot> ();
		Ballot ballot = genBallot1();
		ballots.add(ballot);
		ballot = new Ballot("065_CHARLESTOWN", "");
		ballot.setPage1Text(PRIMARY_2025_065_CHARLESTOWN_PAGE1);
		ballot.setPage2Text(PRIMARY_2025_065_CHARLESTOWN_PAGE2);
		ContestExtractor.extractContests(ballot);
		ReferendumExtractor.extractPageReferendums(ballot);
		RetentionExtractor.extractPageRetentions(ballot);
		ballots.add(ballot);
		return ballots;
	}
	
	static Ballot genBallot3() {
		// one ballot - 2024 GENERAL: 385_NEW_GARDEN_PAGE1
		// use 2024 General Regexes
		com.rodini.contestgen.ContestGen1.COUNTY = Utils.getEnvVariable("BALLOTGEN_COUNTY", true);
		Properties contestGenProps = Utils.loadProperties("./src/test/java/general-2024-contestgen.properties");
		com.rodini.contestgen.common.Initialize.validateProperties(contestGenProps);
		Ballot ballot = new Ballot("385_NEW_GARDEN", "");
		ballot.setPage1Text(GENERAL_2024_385_NEW_GARDEN_PAGE1);
		ballot.setPage2Text(GENERAL_2024_385_NEW_GARDEN_PAGE2);
		ContestExtractor.extractContests(ballot);
		ReferendumExtractor.extractPageReferendums(ballot);
		RetentionExtractor.extractPageRetentions(ballot);
		return ballot;
	}

	static List<Ballot> genBallot4() {
		// one ballot - 2024 GENERAL: 385_NEW_GARDEN_PAGE1, 653_UWCHLAN_1
		// use 2024 General Regexes
		com.rodini.contestgen.ContestGen1.COUNTY = Utils.getEnvVariable("BALLOTGEN_COUNTY", true);
		Properties contestGenProps = Utils.loadProperties("./src/test/java/general-2024-contestgen.properties");
		com.rodini.contestgen.common.Initialize.validateProperties(contestGenProps);
		List<Ballot> ballots = new ArrayList<Ballot> ();
		Ballot ballot = genBallot3();
		ballots.add(ballot);
		ballot = new Ballot("653_UWCHLAN", "");
		ballot.setPage1Text(GENERAL_2024_653_UWCHLAN_1_PAGE1);
		ballot.setPage2Text(GENERAL_2024_653_UWCHLAN_1_PAGE2);
		ContestExtractor.extractContests(ballot);
		ReferendumExtractor.extractPageReferendums(ballot);
		RetentionExtractor.extractPageRetentions(ballot);
		ballots.add(ballot);
		return ballots;
	}
}
