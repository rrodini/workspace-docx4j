package com.rodini.voteforprocessor.extract;

//mport static org.junit.jupiter.api.Assertions.*;

//import org.junit.jupiter.api.Test;

class TestBallotText {
	// 2024 Presidential Primary, No referendums, No retentions
	public static String ATGLEN_PRIMARY_2024_BALLOT =
	"""
	President of the United States
	Vote for One
	Joseph R. Biden Jr.
	Dean Phillips
	Write-in
	United States Senator
	Vote for One
	William Parker
	Allegheny County
	Robert P. Casey Jr.
	Lackawanna County
	Write-in
	Attorney General
	Vote for One
	Jack Stollsteimer
	Delaware County
	Eugene DePasquale
	Allegheny County
	Joe Khan
	Bucks County
	Keir Bradford-Grey
	Philadelphia County
	Jared Solomon
	Philadelphia County
	Write-in
	Auditor General
	Vote for One
	Malcolm Kenyatta
	Philadelphia County
	Mark Pinsley
	Lehigh County
	Write-in
	State Treasurer
	Vote for One
	Ryan Bizzarro
	Erie County
	Erin McClelland
	Allegheny County
	Write-in
	Representative in Congress
	6th District
	Vote for One
	Chrissy Houlahan
	Chester County
	Write-in
	Representative in the General
	Assembly
	74th District
	Vote for One
	Dan Williams
	Chester County
	Write-in
	Delegate to the National
	Convention
	6th District
	Vote for no more than Seven
	Tom Herman
	Committed to Joseph R. Biden Jr.
	Marian Moskowitz
	Committed to Joseph R. Biden Jr.
	George Piasecki III
	Committed to Joseph R. Biden Jr.
	Ashley Gagne
	Committed to Joseph R. Biden Jr.
	Hans van Mol
	Committed to Joseph R. Biden Jr.
	Kieran Francke
	Committed to Joseph R. Biden Jr.
	Johanny Cepeda Freytiz
	Committed to Joseph R. Biden Jr.
	Write-in
	Write-in
	Write-in
	Write-in
	Write-in
	Write-in
	Write-in
	""";
	// 2024 Presidential Election,
	public static String ATGLEN_GENERAL_2024_BALLOT =
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
	74th District
	Vote for One
	Dan Williams
	Democratic
	Dale Hensel
	Republican
	Write-in
	""";
	// 2023 Democratic Primary, 1 referendum, 0 retentions
	public static String MALVERN_PRIMARY_2023_BALLOT =
	"""
	Justice of the Supreme Court
	Vote for One
	Daniel McCaffery
	Philadelphia County
	Debbie Kunselman
	Beaver County
	Write-in
	Judge of the Superior Court
	Vote for no more than Two
	Pat Dugan
	Philadelphia County
	Timika Lane
	Philadelphia County
	Jill Beck
	Allegheny County
	Write-in
	Write-in
	Judge of the Commonwealth Court
	Vote for One
	Matt Wolf
	Philadelphia County
	Bryan Neft
	Allegheny County
	Write-in
	Judge of the Court of Common Pleas
	10 Year Term
	Vote for no more than Five
	Dave Black
	Thornbury Township
	Andy Rongaus
	West Goshen Township
	Deb Ryan
	Birmingham Township
	PJ Redmond
	West Goshen Township
	Don Kohler
	New Garden Township
	Nicole Forzato
	Easttown Township
	Fredda D. Maddox
	East Goshen Township
	Kristine C. Howard
	East Whiteland Township
	Thomas McCabe
	Newlin Township
	Lou Mincarelli
	East Brandywine Township
	Sarah B. Black
	West Chester Borough
	Paige Simmons
	West Pikeland Township
	Write-in
	Write-in
	Write-in
	Write-in
	Write-in
	County Commissioner
	Vote for no more than Two
	Josh Maxwell
	Downingtown Borough
	Marian Moskowitz
	Tredyffrin Township
	Write-in
	Write-in
	District Attorney
	Vote for One
	Christopher de Barrena-Sarobe
	Willistown Township
	Write-in
	Sheriff
	Vote for One
	Kevin Dykes
	East Marlborough Township
	Write-in
	Prothonotary
	Vote for One
	Debbie Bookman
	City of Coatesville
	Write-in
	Register of Wills
	Vote for One
	Michele Vaughn
	East Whiteland Township
	Write-in
	Recorder of Deeds
	Vote for One
	Diane O'Dwyer
	Uwchlan Township
	Write-in
	School Director
	Great Valley Region 2
	Vote for no more than Two
	Wendy Litzke
	Neha Mehta
	Write-in
	Write-in
	Member of Council
	Malvern Borough
	Vote for no more than Four
	Mark Niemiec
	Brendan Phillips
	Joe Bones
	Lynne Frederick
	Write-in
	Write-in
	Write-in
	Write-in
	Malvern Borough Referendum
	Shall the ordinance entitled "An
	Ordinance Offering a Conservation
	Easement and Declaration of
	Restrictive Covenants to the
	Willistown Conservation Trust, Inc.,
	for Certain Borough-owned Property
	in the Ruthland Avenue - Randolph
	Woods Tract" be adopted granting a
	conservation easement to the
	Willistown Conservation Trust, Inc.,
	for the purpose of preserving a
	0.87-acre tract of land owned by the
	Borough, off of Ruthland Avenue and
	adjacent to the Malvern Fire
	Department property?
	YES
	NO
	""";
	// 2023 General Election, 1 referendum, 4 retentions
	// No test since no regex as of 5/22/2025
	public static String HONEYBROOK_GENERAL_2023_BALLOT =
	"""
	Justice of the Supreme Court
	Vote for ONE
	Daniel McCaffery
	Democratic
	Carolyn Carluccio
	Republican
	Write-in
	Judge of the Superior Court
	Vote for no more than TWO
	Jill Beck
	Democratic
	Timika Lane
	Democratic
	Maria Battista
	Republican
	Harry F. Smail Jr.
	Republican
	Write-in
	Write-in
	Judge of the Commonwealth Court
	Vote for ONE
	Matt Wolf
	Democratic
	Megan Martin
	Republican
	Write-in
	Judge of the Court of Common Pleas
	15th Judicial District
	Vote for no more than FIVE
	Sarah B. Black
	Democratic
	Deb Ryan
	Democratic
	Fredda D. Maddox
	Democratic
	Nicole Forzato
	Democratic
	Thomas McCabe
	Democratic
	Lou Mincarelli
	Republican
	PJ Redmond
	Republican
	Andy Rongaus
	Republican
	Don Kohler
	Republican
	Dave Black
	Republican
	Write-in
	Write-in
	Write-in
	Write-in
	Write-in
	County Commissioner
	Vote for no more than TWO
	Josh Maxwell
	Democratic
	Marian Moskowitz
	Democratic
	David C. Sommers
	Republican
	Eric Roe
	Republican
	Write-in
	Write-in
	District Attorney
	Vote for ONE
	Christopher de Barrena-Sarobe
	Democratic
	Ryan L. Hyde
	Republican
	Write-in
	Sheriff
	Vote for ONE
	Kevin Dykes
	Democratic
	Roy Kofroth
	Republican
	Write-in
	Prothonotary
	Vote for ONE
	Debbie Bookman
	Democratic
	Michael Taylor
	Republican
	Write-in
	Register of Wills
	Vote for ONE
	Michele Vaughn
	Democratic
	Terri Clark
	Republican
	Write-in
	Recorder of Deeds
	Vote for ONE
	Diane O'Dwyer
	Democratic
	Brian D. Yanoviak
	Republican
	Write-in
	Magisterial District Judge
	District 15-3-06
	Vote for ONE
	Joseph Hutton
	Democratic
	Tim Arndt
	Republican
	Write-in
	School Director
	Twin Valley Region 3
	Vote for no more than TWO
	William Wray
	Democratic
	Nick DiGiacomo
	Democratic/Republican
	John Burdy
	Republican
	Write-in
	Write-in
	Township Supervisor
	Honey Brook Township
	Vote for ONE
	Salvatore DiGiacomo
	Republican
	Write-in
	Tax Collector Unexpired 2 Year Term
	Honey Brook Township
	Vote for ONE
	Write-in
	Auditor
	Honey Brook Township
	Vote for ONE
	Valerie Shultz
	Republican
	Write-in
	Honey Brook Township:
	Referendum for Additional
	Township Supervisors
	Should two additional supervisors be
	elected to serve in this township?
	YES
	NO
	Superior Court Retention
	Election Question
	Shall Jack Panella be retained for an
	additional term as Judge of the
	Superior Court of the Commonwealth
	of Pennsylvania?
	YES
	NO
	Superior Court Retention
	Election Question
	Shall Victor P. Stabile be retained for
	an additional term as Judge of the
	Superior Court of the Commonwealth
	of Pennsylvania?
	YES
	NO
	Court of Common Pleas Retention
	Election Question
	Shall Patrick Carmody be retained
	for an additional term as Judge of the
	Court of Common Pleas, 15th Judicial
	District, Chester County?
	YES
	NO
	Court of Common Pleas Retention
	Election Question
	Shall John L. Hall be retained for an
	additional term as Judge of the Court
	of Common Pleas, 15th Judicial
	District, Chester County?
	YES
	NO
	""";
}
