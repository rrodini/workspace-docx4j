package com.rodini.ballotgen.writein;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.rodini.zoneprocessor.ZoneProcessor;
import com.rodini.zoneprocessor.Zone;
/**
 * WriteinProcessor processes the write-in candidates for a contest.
 * A contest (particularly in a general election) may not have any write-in
 * candidate names. However, a write-in CSV file is allowed for any election.
 * 
 * Notes:
 * - A contest may have multiple write-in names (no checking against limit)
 * - A precinct ballot may have multiple contests with write-ins.
 * - There is no cross-checking write-in names against non-write-in names.
 * 
 * @author Bob Rodini
 * 
 */
public class WriteinProcessor {
	private static final Logger logger = LogManager.getLogger(WriteinProcessor.class);
	// Precinct no. (key)  List of Writeins (value)
	// 754                 Writein1, Writein2, ...
	private Map <String, List<Writein>> precinctWriteins;
	/**
	 * Constructor for production
	 * 
	 * @param precinctWriteins map of ballots to their write-in candidates
	 */
	public WriteinProcessor(Map<String, List<Writein>> precinctWriteins) {
		this.precinctWriteins = precinctWriteins;
	}
	/**
	 * Constructor for testing
	 * 
	 * @param writeinsCSVText test CSV write-in file.
	 * @param precinctZoneCSVText test zone file.
	 */
	public WriteinProcessor(String writeinsCSVText, String precinctZoneCSVText) {
		Map<String, Zone> precinctToZone;
		// tell ZoneProcessor to process the zoneText (CSV lines) into the muniNoToZone map.
		ZoneProcessor.processCSVText(precinctZoneCSVText);
		precinctToZone = ZoneProcessor.getPrecinctZoneMap();
		WriteinFactory.setPrecinctToZones(precinctToZone);
		WriteinFactory.processCSVText(writeinsCSVText);
		this.precinctWriteins = WriteinFactory.getPrecinctWriteins();
	}
	/**
	 * precinctHasWriteins determines if a ballot (identified by its precinct
	 * number) has any write-in candidates.
	 * 
	 * @param precinctNo ballot identifier.
	 * @return true => ballot has at least one write-in candidate.
	 */
	public boolean precinctHasWriteins(String precinctNo) {
		Set<String> precincts = precinctWriteins.keySet();
		return precincts.contains(precinctNo);
	}
	/**
	 * findCandidatesForContest returns the write-in candidate names for 
	 * a contest on the precinct's ballot.
	 * 
	 * @param precinctNo ballot identifier.
	 * @param contestName contest name on ballot.
	 * @return list of write-in names.
	 */
	public List<String> findCandidatesForContest(String precinctNo, String contestName) {
		List<String> names = new ArrayList<>();
		List<Writein> writeins = precinctWriteins.get(precinctNo);
		if (writeins != null) {
			for (Writein writein: writeins) {
				// Watch out for embedded newline characters (\n)
				if (contestName.equals(writein.getContestName())) {
					names.add(writein.getCandidateName());
				}
			}
		}
		if (names.size() > 0 ) {
			logger.info(String.format("precinct: %s contest: %s has writeins starting: %s" , 
					precinctNo, contestName, names.get(0)));
		}
 		return names;
	}

}
