#!/bin/bash
# updatebinaries.sh refresh the jar files for ContestGen, BallotNamer, BallotGen, BallotZipper.

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
#SCRIPTS_FOLDER=/Users/robert/git/workspace-docx4j/Scripts

# do all work in the release folder
cd "$RELEASE_FOLDER" || exit

echo "copying contestgen binary"
cp "${CONTESTGEN_FOLDER}/target/contest-gen-${BALLOTGEN_VERSION}-jar-with-dependencies.jar" contestgen/
#cp -a "${CONTESTGEN_FOLDER}"/resources/. contestgen/resources/

echo "copying ballotgen binary"
cp "${BALLOTGEN_FOLDER}/target/ballot-gen-${BALLOTGEN_VERSION}-jar-with-dependencies.jar" ballotgen/
#cp -a "${BALLOTGEN_FOLDER}"/resources/. ballotgen/resources/

echo "copying ballotnamer binary"
cp "${BALLOTNAMER_FOLDER}/target/ballot-namer-${BALLOTGEN_VERSION}-jar-with-dependencies.jar" ballotnamer/
#cp -a "${BALLOTNAMER_FOLDER}"/resources/. ballotnamer/resources/

echo "copying ballotzipper binary"
cp "${BALLOTZIPPER_FOLDER}/target/ballot-zipper-${BALLOTGEN_VERSION}-jar-with-dependencies.jar" ballotzipper/
#cp "${BALLOTZIPPER_FOLDER}"/precincts-zones.csv ballotzipper/
#cp -a "${BALLOTZIPPER_FOLDER}"/resources/. ballotzipper/resources/

echo "DONE."
