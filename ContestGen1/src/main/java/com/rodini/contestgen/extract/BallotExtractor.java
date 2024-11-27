package com.rodini.contestgen.extract;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import static java.util.stream.Collectors.toList;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import static com.rodini.ballotutils.Utils.ATTN;
import com.rodini.contestgen.common.Initialize;
import com.rodini.contestgen.model.Ballot;
/**
 * BallotExtractor extracts the ballot text for each precinct-level ballot from
 * the specimen text.  First it extracts the application names (keys) for the ballot
 * then it extracts the raw text. At the end, a list of Ballot objects are created.
 * 
 * @author Bob Rodini
 *
 */
public class BallotExtractor {
	static final Logger logger = LogManager.getLogger(BallotExtractor.class);
	// prevent instantiation.
	private BallotExtractor() {}
	
	/**
	 * extract uses a regex that isolates the raw text for each precinct-level ballot.
	 * 
	 * @param specimenText text from Voter Services specimen. Includes 231 ballots.
	 */
	public static List<Ballot> extract(String specimenText) {
		List<Ballot> ballots = new ArrayList<>();
		List<String> precinctNoNames;
		// First extract the list of precinctNoNames
		precinctNoNames = extractPrecinctNoNames(specimenText);
		Pattern pat = Initialize.precinctNameRegex;
		Matcher match = pat.matcher(specimenText);
		match.find();
		boolean matching = true;
		int count = 0;
		String precinctNoName;
		int textSize = specimenText.length();
		int repeat = Initialize.precinctNameRepeatCount;
		while (matching) {
			precinctNoName = precinctNoNames.get(count);
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
				end = textSize;
			}
			logger.info(String.format("ballot extraction %s specimen start: %d end %d ", precinctNoName, start, end));
			Ballot ballot = new Ballot(precinctNoName, specimenText.substring(start, end));
			ballots.add(ballot);
			count++;
		}
		return ballots;
	}
	/**
	 * extractPrecinctNoNames extracts the names (keys) for the ballot objects.
	 * E.g. 005_ATGLEN
	 * 
	 * @param specimenText text w/ embedded precinct ballots.
	 * 
	 * @return List of precinctNoName.
	 */
	static List<String> extractPrecinctNoNames(String specimenText) {
		List<String> nameList = new ArrayList<>();
		Pattern pat = Initialize.precinctNameRegex;
		Matcher match = pat.matcher(specimenText);
		if (!match.find()) {
			String msg = String.format("no matches for precinctNoNames. Bad regex: %s", pat.pattern());
			// TODO: logFatalError here.
			logger.error(msg);
		} else {
			// must reset the matcher or loose the first match
			match.reset();
			nameList =  match.results()
					// chester county order here
					.map(mr -> mr.group(1) + " " + mr.group(2))
					.distinct()
					// avoid embedded spaces
					.map( name -> name.replace(" ", "_"))
					.collect(toList());	
		}
		logger.log(ATTN,String.format("PrecinctNoNames count: %d", nameList.size()));
		for (String name: nameList) {
			logger.debug(String.format("%s", name));
		}
		return nameList;
	}
}
