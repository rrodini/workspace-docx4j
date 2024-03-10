package com.rodini.contestgen;

import static java.util.stream.Collectors.toList;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.rodini.ballotutils.Utils;
import static com.rodini.ballotutils.Utils.ATTN;;

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
	private static final Logger logger = LogManager.getLogger(SpecimenMuniExtractor.class);

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
				// And don't call m.find() again or you will lose the first match.
				m.reset();
				muniNames = m.results()
						.map(ContestGen.COUNTY.equals("chester")?
								// 2024 PRIMARY
								// CHESTER 2=>id               3=>name
								mr -> mr.group(2) + " " + mr.group(3)
						     :
						    	// BUCKS   2=>id               1=name
						        mr -> mr.group(2) + " " + mr.group(1))
						.distinct()
						// avoid embedded spaces
						.map( name -> name.replace(" ", "_"))
						.collect(toList());
			} catch (Exception e) {
				String msg = e.getMessage();
				logger.error(msg);
			}
			logger.log(ATTN,String.format("Specimen Muni Names: %d", muniNames.size()));
			for (String name: muniNames) {
				logger.debug(String.format("%s", name));
			}
		}
		return muniNames;
	}
	
	/**
	 * extract extracts the municipal text from the text of the Voter Services text.
	 * It relies on a critical format (regular expression) passed to the constructor.
	 * 
	 * Notes: 
	 * 1) The regex that extracts the name is reused to delimit the ballot text boundaries.
	 * 2) The ballot text that is extracted should be very similar to the result of PDFBox Extract utility.
	 *    The advantage of doing it in this program is that the Specimen Text may have been
	 *    manually repaired, so each municipal ballot is also repaired. 
	 * 
	 * @return Ordered list of MunicipalTextExtractor objects.
	 */
// NEW CODE
	List<MuniTextExtractor> extract() {
		List<String> muniNames = extractMuniNames();
		Pattern pat = SpecimenMuniMarkers.getMuniNamePattern();
		Matcher match = pat.matcher(specimenText);

		match.find();
		boolean matching = true;
		int count = 0;
		String muniName;
		int fileSize = specimenText.length();
		int repeat = SpecimenMuniMarkers.getRepeatCount();
		while (matching) {
			muniName = muniNames.get(count);
			int start = match.start();
			for (int r = 1; r < repeat; r++) {
				// skip next match
				match.find();
			}
			int end = 0;
			if (match.find()) {
				end = match.start() - 1;
			} else {
				matching = false;
				end = fileSize;
			}
			logger.info(String.format("%s start: %d end %d ", muniName, start, end));
			muniExtracts.add(new MuniTextExtractor(muniName,specimenText.substring(start, end)));
			count++;
		}
		return muniExtracts;
	}
//OLD CODE	
//	List<MuniTextExtractor> extract1() {
//		List<String> muniNames = extractMuniNames();
//		logger.info(String.format("SpecimenMuniExtractor: there are %d municipalities", muniNames.size()));
//		Pattern muniNamePattern = SpecimenMuniMarkers.getMuniNamePattern();
//		String [] muniStringExtracts = muniNamePattern.split(specimenText, 0);
//		logger.debug(String.format("muniStringExtracts.length=%d%n", muniStringExtracts.length));
//		for (int i=0; i <muniStringExtracts.length; i++) {
//			logger.debug(String.format("extract[%d]: %s%n",i, muniStringExtracts[i]));
//		}
//
//		if (ContestGen.COUNTY.equals("chester")) {
//			// structure of Chester Co. ballot below
//			int repeat = SpecimenMuniMarkers.getRepeatCount();
//			// Loop below uses the results of specimenText extract to create the muniText extracts for each municipality.
//			for (int i = 0; i < muniNames.size(); i++) {
//				int j = (i + 1) * repeat;
//				muniExtracts.add(new MuniTextExtractor(muniNames.get(i), muniStringExtracts[j]));
//			}
//		} else if (ContestGen.COUNTY.equals("bucks")) {
//			// structure of Bucks Co. ballot below
//			//int repeat = SpecimenMuniMarkers.getRepeatCount();
//			int j = 0;
//			// Loop below uses the results of specimenText extract to create the muniText extracts for each municipality.
//			for (int i = 0; i < muniNames.size(); i++) {
//				j++;
//				muniExtracts.add(new MuniTextExtractor(muniNames.get(i), muniStringExtracts[j] + muniStringExtracts[j + 1]));
//				j++;
//			}
//		}
//		return muniExtracts;
//	}

}
