package com.rodini.contestgen;

import static java.util.stream.Collectors.toList;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * SpecimenMuniExtractor "rips" the specimen text and 
 * generates MuniTextExtractor objects.
 * 
 * Input:  Complete text from the Voter Services specimen ballot.
 *         Markers object that delimits beginning / end of municipal text.
 * Output: List<MuniTextExtractor> ordered list of municipal text objects.
 * 
 * ATTENTION: This class is highly sensitive to the patterns within the
 * master pdf.  Check that the "markers" are working before proceeding.
 * 
 * @author Bob Rodini
 *
 */
public class SpecimenMuniExtractor {
	private static final Logger logger = LoggerFactory.getLogger(SpecimenMuniExtractor.class);

	private final String specimenText;
	
	private List<MuniTextExtractor> muniExtracts;
	/**
	 * Constructor
	 * 
	 * @param specimenText complete text from Voter Services pdf.
	 * @param stm Markers object.
	 */
	SpecimenMuniExtractor(String specimenText) {
		this.specimenText = specimenText;
		muniExtracts = new ArrayList<MuniTextExtractor>();
	}
	/**
	 * extractMuniNames extracts the municipality names which appear
	 * at the boundaries of the municipality text.
	 * 
	 * Note: The regex that extracts the name also delimits the text boundaries.
	 * 
	 * @return List of muni names.
	 */
	List<String> extractMuniNames() {
		List<String> muniNames = new ArrayList<>();
		Pattern pattern = SpecimenMuniMarkers.getMuniNamePattern();
		Matcher m = pattern.matcher(specimenText);
		if (!m.find()) {
			String msg = String.format("no match for municipal names. Bad regex? %s", pattern.pattern());
			logger.error(msg);
		} else {
			try {
				muniNames = m.results()
						.map(mr -> mr.group(2))
						.distinct()
						// avoid embedded spaces
						.map( name -> name.replace(" ", "_"))
						.collect(toList());
				
			} catch (Exception e) {
				String msg = e.getMessage();
				logger.error(msg);
			}
		}
		return muniNames;
	}
	
	/**
	 * extract extracts the municipal text from the text of the Voter Services pdf.
	 * It relies on a critical format (regular expression) passed to the constructor.
	 * 
	 * Note: The regex that extracts the name also delimits the text boundaries.
	 * 
	 * @return Ordered list of MunicipalTextExtractor objects.
	 */
	List<MuniTextExtractor> extract() {
		List<String> muniNames = extractMuniNames();
		String [] muniStringExtracts = specimenText.split(SpecimenMuniMarkers.getMuniNamePattern().pattern(), 0);
		int repeat = SpecimenMuniMarkers.getRepeatCount();
		for (int i = 0; i < muniNames.size(); i++) {
			int j = (i + 1) * repeat;
			// log below
			muniExtracts.add(new MuniTextExtractor(muniNames.get(i), muniStringExtracts[j]));
		}
		return muniExtracts;
	}

}
