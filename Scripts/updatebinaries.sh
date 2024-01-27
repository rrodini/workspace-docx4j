#!/bin/bash
# updatebinaries.sh refresh the jar files for ContestGen, BallotNamer, BallotGen, BallotZipper, TabReplacer
# Note: These jars are embedded as dependencies BallotUtils, ZoneProcessor

if [ -n "$BALLOTGEN_VERSION" ]; then
  echo -e "\nBALLOTGEN_VERSION: ${BALLOTGEN_VERSION}\n"
else 
  echo -e "\nBALLOTGEN_VERSION environment variable not defined -- Quitting.\n"
  exit 0
fi
# List of binaries
RELEASE_FOLDER=/Users/robert/Documents/"Sample Ballot Production"/SampleBallotGen-${BALLOTGEN_VERSION}
CONTESTGEN_FOLDER=/Users/robert/git/workspace-docx4j/ContestGen/target
CONTESTGEN="contest-gen-${BALLOTGEN_VERSION}-jar-with-dependencies.jar"
BALLOTGEN_FOLDER=/Users/robert/git/workspace-docx4j/BallotGen/target
BALLOTGEN="ballot-gen-${BALLOTGEN_VERSION}-jar-with-dependencies.jar"
BALLOTNAMER_FOLDER=/Users/robert/git/workspace-docx4j/BallotNamer/target
BALLOTNAMER="ballot-namer-${BALLOTGEN_VERSION}-jar-with-dependencies.jar"
BALLOTZIPPER_FOLDER=/Users/robert/git/workspace-docx4j/BallotZipper/target
BALLOTZIPPER="ballot-zipper-${BALLOTGEN_VERSION}-jar-with-dependencies.jar"
# TABREPLACER_FOLDER=/Users/robert/git/workspace-docx4j/TabReplacer/target
# TABREPLACER="tab-replacer-${BALLOTGEN_VERSION}-jar-with-dependencies.jar"
TEXTCLEANER_FOLDER=/Users/robert/git/workspace-docx4j/TextCleaner/target
TEXTCLEANER="text-cleaner-${BALLOTGEN_VERSION}-jar-with-dependencies.jar"

# copy_file - copy FILE1 to FILE2 only if it is newer.
copy_file() {
  if [ $1 -nt $2 ]
  then
  cp -v $1 $2
  fi
}

# do all work in the release folder
cd "$RELEASE_FOLDER" || exit

# CONTESTGEN
FILE1="${CONTESTGEN_FOLDER}/$CONTESTGEN"
FILE2="contestgen/$CONTESTGEN"
copy_file $FILE1 $FILE2

# BALLOTGEN
FILE1="${BALLOTGEN_FOLDER}/$BALLOTGEN"
FILE2="ballotgen/$BALLOTGEN"
copy_file $FILE1 $FILE2

# BALLOTNAMER
FILE1="${BALLOTNAMER_FOLDER}/$BALLOTNAMER"
FILE2="ballotnamer/$BALLOTNAMER"
copy_file $FILE1 $FILE2

# BALLOTZIPPER
FILE1="${BALLOTZIPPER_FOLDER}/$BALLOTZIPPER"
FILE2="ballotzipper/$BALLOTZIPPER"
copy_file $FILE1 $FILE2

# TABREPLACER
# FILE1="${TABREPLACER_FOLDER}/$TABREPLACER"
# FILE2="tabreplacer/$TABREPLACER"
# copy_file $FILE1 $FILE2


#echo "copying textcleaner binary"
#cp -v "${TEXTCLEANER_FOLDER}/target/text-cleaner-${BALLOTGEN_VERSION}-jar-with-dependencies.jar" textcleaner/
# TEXTCLEANER
FILE1="${TEXTCLEANER_FOLDER}/$TEXTCLEANER"
FILE2="textcleaner/$TEXTCLEANER"
copy_file $FILE1 $FILE2

echo "DONE."
