package com.rodini.contestgen;

import static java.util.stream.Collectors.toList;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.rodini.ballotutils.Utils;
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
			String msg = String.format("no matches for municipal names. Bad regex? %s", pattern.pattern());
			Utils.logFatalError(msg);
		} else {
			try {
				// After first call to m.find() need to reset the matcher.
				m.reset();
				// Now call m.find() again so as not to lose the first find.
				m.find();
				muniNames = m.results()
						.map(ContestGen.COUNTY.equals("chester")?
								// CHESTER 1=>id               2=>name
								mr -> mr.group(1) + " " + mr.group(2)
						     :
						    	// BUCKS   2=>id               1=name
						        mr -> mr.group(2) + " " + mr.group(1))
						.distinct()
						// avoid embedded spaces
						.map( name -> name.replace(" ", "_"))
						.collect(toList());
//				NEW - below was used to find the bug for specimen's that list municipality once.
//				m.reset();
//				while (m.find()) {
//					String id = m.group(1);
//					String name = m.group(2);
//					String muniName = id + "_" + name.replace(" ", "_");
//					if (!muniNames.contains(muniName)) {
//						muniNames.add(muniName);
//					}
//				}
			} catch (Exception e) {
				String msg = e.getMessage();
				logger.error(msg);
			}
			logger.debug(String.format("Specimen Muni Names: %d%n", muniNames.size()));
			for (String name: muniNames) {
				logger.debug(String.format("%s%n", name));
			}
		}
		return muniNames;
	}
	
	/**
	 * extract extracts the municipal text from the text of the Voter Services text.
	 * It relies on a critical format (regular expression) passed to the constructor.
	 * 
	 * Note: The regex that extracts the name also delimits the text boundaries.
	 * 
	 * @return Ordered list of MunicipalTextExtractor objects.
	 */
	List<MuniTextExtractor> extract() {
		List<String> muniNames = extractMuniNames();
		logger.info(String.format("SpecimenMuniExtractor: there are %d municipalities", muniNames.size()));
//logger.debug(String.format("muniNameRegex: %s%n", SpecimenMuniMarkers.getMuniNamePattern().pattern().toString()));
//		String [] muniStringExtracts = specimenText.split(SpecimenMuniMarkers.getMuniNamePattern().pattern().toString(), 0);
		Pattern muniNamePattern = SpecimenMuniMarkers.getMuniNamePattern();
		String [] muniStringExtracts = muniNamePattern.split(specimenText, 0);
logger.debug(String.format("muniStringExtracts.length=%d%n", muniStringExtracts.length));
for (int i=0; i <muniStringExtracts.length; i++) {
	logger.debug(String.format("extract[%d]: %s%n",i, muniStringExtracts[i]));
}

		if (ContestGen.COUNTY.equals("chester")) {
			// structure of Chester Co. ballot below
			int repeat = SpecimenMuniMarkers.getRepeatCount();
			// Loop below uses the results of specimenText extract to create the muniText extracts for each municipality.
			for (int i = 0; i < muniNames.size(); i++) {
				int j = (i + 1) * repeat;
				muniExtracts.add(new MuniTextExtractor(muniNames.get(i), muniStringExtracts[j]));
			}
		} else if (ContestGen.COUNTY.equals("bucks")) {
			// structure of Bucks Co. ballot below
			//int repeat = SpecimenMuniMarkers.getRepeatCount();
			int j = 0;
			// Loop below uses the results of specimenText extract to create the muniText extracts for each municipality.
			for (int i = 0; i < muniNames.size(); i++) {
				j++;
				muniExtracts.add(new MuniTextExtractor(muniNames.get(i), muniStringExtracts[j] + muniStringExtracts[j + 1]));
				j++;
			}
		}
		return muniExtracts;
	}

}
