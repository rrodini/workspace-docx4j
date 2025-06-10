package com.rodini.contestgen;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import static com.rodini.contestgen.ContestGen1.PAGE_BREAK;

import com.rodini.ballotutils.Utils;
import com.rodini.contestgen.common.Initialize;
import com.rodini.contestgen.model.Ballot;
import com.rodini.voteforprocessor.model.Contest;
import com.rodini.voteforprocessor.model.Candidate;
import com.rodini.zoneprocessor.Zone;
import com.rodini.zoneprocessor.ZoneFactory;
import com.rodini.zoneprocessor.ZoneProcessor;

/**
 * GenerateEndorsementsFile generates the file endorsements-all.csv whereby every
 * candidate on the ballot is endorsed. This file must be reduced to only party
 * endorsed candidates, but it is a convenience for sample ballot generation for a
 * Primary election where some primaries have over 500 endorsements.
 * 
 * @author Bob Rodini
 *
 */
public class GenerateEndorsementsFile {
    // prevent instances.
	private GenerateEndorsementsFile() {}
	
	static final Logger logger = LogManager.getLogger(GenerateEndorsementsFile.class);
// @formatter: off
	/**
	 * generate the endorsements-all.csv file. The order of generation goes:
	 *  1. Candidates common to all ballots
	 *  2. Zone 1 candidates
	 *     ...
	 * 18. Zone 18 candidates
	 * 
	 * Basic algorithm:
	 * 1. Identify common contests across all ballots (commonContests)
	 * 2. Create the zone to contests map (zoneContestsMap)
	 * 3. Generate the common candidate endorsements
	 * 4. Generate the zone candidate endorsements by zone.
	 */
// @formatter: on
	public static void generate(List<Ballot> ballots) {
		logger.info("generating endorsements file.");
		initializeZoneProcessor(Initialize.contestGenProps);
		// these contests appear on every ballot.
		List<Contest> commonContests = processCommonContests(ballots);
		// each map entry has zone specific contests
		//   zoneNo  contests
		Map<String, List<Contest>> zoneContestsMap;
		zoneContestsMap = processZoneBallots(ballots, commonContests, ZoneProcessor.getPrecinctZoneMap());
		logZoneContestsMap(zoneContestsMap);
		String endorsmentsFilePath = Initialize.outContestPath + File.separator + Initialize.endorsementsAllFileName;
		try (PrintWriter pw = new PrintWriter(new File(endorsmentsFilePath))) {
			// Generate common candidate endorsements
			generateCommonCandidatesEndorsements(pw, commonContests);
			// Get the zone ids (zoneNos)
			Set<String> zoneNos = zoneContestsMap.keySet();
			for (String zoneNo: zoneNos) {
				// Generate candidate endorsements by zone
				Zone zone = ZoneFactory.getZones().get(zoneNo);
				generateZoneContestsEndorsements(pw, zone, zoneContestsMap.get(zoneNo));
			}
		} catch (IOException ex) {
			String msg = String.format("IOException writing endorsements-all.csv file: %s", ex.getMessage());			
			logger.error(msg);
			System.out.println(msg);
		}

	}
	/**
	 * initializeZoneProcessor - read in the precinct to zone mapping file
	 * and initialize the ZoneProcessor.
	 * 
	 * @param props Properties object with file name.
	 */
	static void initializeZoneProcessor(Properties props) {
		logger.info("initializing the ZoneProcessor.");
		String csvFilePath = Utils.getPropValue(props, ContestGen1.COUNTY + ".precinct.to.zone.file");
		String csvText = Utils.readTextFile(csvFilePath);
		// after this call the ZoneProcessor is functional.
		ZoneProcessor.processCSVText(csvText);
	}
	/**
	 * processCommonContests processes all specimen ballots and identify the contests common
	 * to all.
	 * 
	 * @param ballots list of specimen ballots.
	 * @return list of common Contest objects.
	 */
	static List<Contest> processCommonContests(List <Ballot> ballots) {
		//   key     value
		//   contest count
		//   name    across
		//           ballots
		Map <String, Integer> contestCountsMap = new HashMap<>();
		logger.info("processing common contests.");
		int ballotCount = ballots.size();
		List<Contest> commonContests = new ArrayList<>();
		for (Ballot ballot: ballots) {
			List<Contest> contests = ballot.getContests();
			// filter out the pesky pseudo contest PAGE_BREAK
			contests = contests.stream().filter(c -> !c.getName().equals(ContestGen1.PAGE_BREAK))
					.collect(Collectors.toList());
			// the next line takes advantage of the Map class's merge() method
			contests.forEach(contest ->
			contestCountsMap.merge(contest.getName(), 1, (prev, one) -> Integer.sum(prev, one)));
			// code below was replace by two-liner above.
			//	for (Contest contest: contests) {
			//		int count = 0;
			//		if (contestCounts.get(contest.getName()) != null) {
			//			count = contestCounts.get(contest.getName());
			//		}
			//		contestCounts.put(contest.getName(), count + 1);
			//	}
		}
		// now examine just one ballot which will contains common contests (by definition).
		Ballot ballot = ballots.get(0);
		for (Contest contest: ballot.getContests()) {
			// ignore the pesky pseudo contest PAGE_BREAK
			if (contest.getName().equals(PAGE_BREAK)) {
				continue;
			}
			if (contestCountsMap.get(contest.getName()) == ballotCount) {
				commonContests.add(contest);
			}
		}
		return commonContests;
	}
// @formatter: off
   /**
     * processZoneBallots processes all of the ballots into the zoneContestsMap structure.
     * This will make it easy to generate candidates by zone.
     * 
     * Algorithm;
     * for each ballot in ballots
	 * 	 if contest NOT in common contests
	 * 	   get zone contest list
	 *     add contest to zone's contest list
	 *	 end if
     * end for
     * 
     * @param ballots all ballots
     * @param commonContests Contests common across all ballots
     * @param precinctZoneMap precinct to zone map
     * @return zoneContestsMap zone to contests map
     */
// @formatter: on
	static Map<String, List<Contest>> processZoneBallots(List<Ballot> ballots,
			List<Contest> commonContests, Map<String,  Zone> precinctZoneMap) {
		logger.info("processing zone ballots.");
		Set<String> zoneNoSet = ZoneFactory.getZones().keySet();
		//  zoneNo  zone
		//          contest list
		Map<String, List<Contest>> zoneContestsMap = new TreeMap<>();
		for (String zoneNo: zoneNoSet) {
			zoneContestsMap.put(zoneNo, new ArrayList<Contest>());
		}
		for (Ballot ballot: ballots) {
			List<Contest> contests = ballot.getContests();
			for (Contest contest: contests) {
				if (!isCommonContest(contest, commonContests)) {;
					Zone zone = precinctZoneMap.get(contest.getPrecinctNo());
					List<Contest> zoneContests = zoneContestsMap.get(zone.getZoneNo());
					zoneContests.add(contest);
					zoneContestsMap.put(zone.getZoneNo(), zoneContests);
				}
			}
		}
		// Have to sort on contest names for generateZoneCandidates algorithm.
		// You must understand this to understand generateZoneCandidates.
		for (String zoneNo: zoneNoSet) {
			List<Contest> zoneContests = zoneContestsMap.get(zoneNo);
			zoneContests.sort((contest1, contest2) -> contest1.getName().compareTo(contest2.getName()));
		}
		return zoneContestsMap;
	}
	/**
	 * logZoneContestsMap logs the zone to contests map to the log file.
	 * @param zoneContestMap map to be logged.
	 */
	static void logZoneContestsMap(Map<String, List<Contest>> zoneContestMap) {
		logger.debug("logging zone contests map.");
		Set<String> zoneNos = zoneContestMap.keySet();
		for (String zoneNo: zoneNos) {
			logger.debug("Zone: " + zoneNo);
			List<Contest> zoneContests = zoneContestMap.get(zoneNo);
			for (Contest contest: zoneContests) {
				logger.debug("Contest: " + contest.getName());
			}
		}
	}		
	/**
	 * isCommonContest identifies contests that are in the commonContests list 
	 * or have the pesky PAGE_BREAK name.
	 * 
	 * @param contest to be tested.
	 * @param commonContests list of common Contest objects.
	 * @return true => contest in "common" list. false => contest is a "zone" contest.
	 */
	static boolean isCommonContest(Contest contest, List<Contest> commonContests) {
		boolean common = false;
		List<String> commonContestNames = commonContests.stream().map(Contest::getName).toList();
		if (contest.getName().equals(PAGE_BREAK)) {
			common = true;
		} else {
			common = commonContestNames.contains(contest.getName());
		}
		return common;
	}
	/**
	 * generateCommonCandidatesEndorsements writes lines for the "common" candidates.
	 * 
	 * @param pw Writer object.
	 * @param commonContests list of common contests.
	 * @throws IOException
	 */
	static void generateCommonCandidatesEndorsements(Writer pw, List<Contest> commonContests) throws IOException {
		logger.info("writing common candidates endorsements.");
		pw.write("# Common contest candidates\n");
		for (Contest contest: commonContests) {
			for (Candidate candidate: contest.getCandidates()) {
				pw.write(String.format("%s,endorsed,county%n", candidate.getName()));
			}
		}
	}
// @formatter: off
	/**
	 * generateZoneContestsEndorsement generates endorsements by zone. Must avoid listing candidates
	 * multiple times.
	 * 
	 * Algorithm:
	 *
	 * Set up for filtering in for zoneContests loop
	 * lastContestName = ""
	 * lastCandidateList = null
	 * for each contest in zone contests
	 *   contestName = contest.getName()
	 *   if contestName != lastContestName
	 *     if lastCandidateList != null
	 *       write endorsement for lastCandicateList, zone, lastPrecinctList
	 *     end if
	 *     lastCandiateList = contest candidates
	 *   end if
	 * end for
	 * if lastCandidateList != null
	 *   write endorsement for: lastCandidateList, zone, lastPrecinctList
	 * end if
	 * 
	 * @param pw Writer object.
	 * @param zone Zone with contests.
	 * @param zoneContests list of zone contests.
	 * @throws IOException
	 */
// @formatter: on
	static void generateZoneContestsEndorsements(Writer pw, Zone zone, List<Contest> zoneContests) throws IOException {
		String zoneNo;
		String zoneName;
		logger.info("writing zone contests endorsements.");
		zoneNo = zone.getZoneNo();
		zoneName = zone.getZoneName();
		pw.write(String.format("# Zone %s %s%n", zoneNo, zoneName));
		String lastContestName = "";
		List<Candidate> lastContestCandidates = null;
		for (Contest contest: zoneContests) {
			String contestName = contest.getName();
			if (!contestName.equals(lastContestName)) {
				if (lastContestCandidates != null) {
					generateZoneCandidatesEndorsement(pw, lastContestCandidates, zone);
				}
				lastContestName = contestName;
				lastContestCandidates = contest.getCandidates();
			}
		}
		if (lastContestCandidates != null) {
			generateZoneCandidatesEndorsement(pw, lastContestCandidates, zone);
		}		
	}
	/**
	 * generateZoneCandidatesEndorsements writes lines for the zone's candidates.
	 * 
	 * @param pw Writer object.
	 * @param candidates list of zone candidates.
	 * @param zone Zone object.
	 * @throws IOException
	 */
	static void generateZoneCandidatesEndorsement(Writer pw, List<Candidate> candidates,
			Zone zone) throws IOException {
		logger.info("writing zone candidates endorsements.");
		String zoneNo = zone.getZoneNo();
		// line below is not needed, but it ever is needed, it's here.
		//  String precinctNosString = precinctNos.stream().collect(Collectors.joining(","));
		for (Candidate cand: candidates) {
			pw.write(String.format("%s,endorsed,zone,%s%n",cand.getName(), zoneNo));
		}
	}
}