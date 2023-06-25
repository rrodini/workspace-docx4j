#!/bin/bash
# updatebinaries.sh refresh the jar files for ContestGen, BallotNamer, BallotGen, BallotZipper, TabReplacer
# Note: These jars are embedded as dependencies BallotUtils, ZoneProcessor

if [ -n "$BALLOTGEN_VERSION" ]; then
  echo -e "\nBALLOTGEN_VERSION: ${BALLOTGEN_VERSION}\n"
else 
  echo -e "\nBALLOTGEN_VERSION environment variable not defined -- Quitting.\n"
  exit 0
fi

RELEASE_FOLDER=/Users/robert/Documents/"Sample Ballot Production"/SampleBallotGen-${BALLOTGEN_VERSION}
CONTESTGEN_FOLDER=/Users/robert/git/workspace-docx4j/ContestGen
BALLOTGEN_FOLDER=/Users/robert/git/workspace-docx4j/BallotGen
BALLOTNAMER_FOLDER=/Users/robert/git/workspace-docx4j/BallotNamer
BALLOTZIPPER_FOLDER=/Users/robert/git/workspace-docx4j/BallotZipper
TABREPLACER_FOLDER=/Users/robert/git/workspace-docx4j/TabReplacer

# do all work in the release folder
cd "$RELEASE_FOLDER" || exit

echo "copying contestgen binary"
cp -v "${CONTESTGEN_FOLDER}/target/contest-gen-${BALLOTGEN_VERSION}-jar-with-dependencies.jar" contestgen/

echo "copying ballotgen binary"
cp -v "${BALLOTGEN_FOLDER}/target/ballot-gen-${BALLOTGEN_VERSION}-jar-with-dependencies.jar" ballotgen/

echo "copying ballotnamer binary"
cp -v "${BALLOTNAMER_FOLDER}/target/ballot-namer-${BALLOTGEN_VERSION}-jar-with-dependencies.jar" ballotnamer/

echo "copying ballotzipper binary"
cp -v "${BALLOTZIPPER_FOLDER}/target/ballot-zipper-${BALLOTGEN_VERSION}-jar-with-dependencies.jar" ballotzipper/

echo "copying tabreplacer binary"
cp -v "${TABREPLACER_FOLDER}/target/tab-replacer-${BALLOTGEN_VERSION}-jar-with-dependencies.jar" tabreplacer/

echo "DONE."
