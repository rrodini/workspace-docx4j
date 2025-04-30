package com.rodini.contestgen;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.rodini.contestgen.common.Initialize;
import com.rodini.contestgen.model.Ballot;
/**
 * GenerateBallotFiles generates the precinct-level files in the ./output folder.
 * Example: ./chester-output/
 *             municipal-0.txt
 *             municipal-1.txt
 *             municipal-2.txt
 *             ...                 
 * 
 * Note: 
 * The BallotNamer program will subsequently change the names.
 * Example:
 *             municipal-0.txt => 005_ATGLEN_VS.txt
 *             municipal-1.txt => 010_AVONDALE_VS.txt
 *             municipal-2.txt => 014_BIRMINGHAM_1_VS.txt
 *             ...                 
 * @author Bob Rodini
 *
 */
public class GenerateBallotFiles {

	static final Logger logger = LogManager.getLogger(GenerateBallotFiles.class);
	// prevent instantiation
	private GenerateBallotFiles() {}
    /**
     * generate a ballot for each Ballot object.
     * 
     * @param ballots List of Ballot objects
     */
	public static void generate(List<Ballot> ballots) {
		int count = 1;
		for (Ballot ballot: ballots) {
			String precinctNoName = ballot.getPrecinctNoName();
			String ballotFilePath = "";
			if (Initialize.precinctNoNameFileName) {
				// use specific name (350_MALVERN.txt)
				ballotFilePath = Initialize.outBallotPath + File.separator + 
						          precinctNoName + ContestGen1.TEXT_EXT;
			} else {
				// use generic name (municipal-nn.txt)
				ballotFilePath = Initialize.outBallotPath + File.separator + 
						         ContestGen1.BALLOT_FILE + "-"  + count + ContestGen1.TEXT_EXT;
			}
			String msg = String.format("writing file: %s", ballotFilePath);
			logger.info(msg);
			try (FileWriter ballotFile = new FileWriter(ballotFilePath, StandardCharsets.UTF_8, false);) {
				// Just write the raw text as this file isn't really needed.
				ballotFile.write(ballot.getRawText());				
			} catch (IOException e) {
				logger.error(e.getMessage());
			}
			count++;
		}
	}
}
