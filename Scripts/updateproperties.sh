#!/bin/bash
# updateproperties.sh refreshes the properties files for ContestGen, BallotNamer, BallotGen, BallotZipper, TabReplacer
# Usage: ./updateproperties.sh [work2git | git2work]
#

if [ -n "$BALLOTGEN_VERSION" ]; then
  echo -e "\nBALLOTGEN_VERSION: ${BALLOTGEN_VERSION}\n"
else 
  echo -e "\nBALLOTGEN_VERSION environment variable not defined -- Quitting.\n"
  exit 0
fi

WORK_FOLDER=/Users/robert/Documents/"Sample Ballot Production"/SampleBallotGen-${BALLOTGEN_VERSION}
GIT_FOLDER=/Users/robert/git/workspace-docx4j
# CONTESTGEN_FOLDER=/ContestGen
# BALLOTGEN_FOLDER=/BallotGen
# BALLOTNAMER_FOLDER=/BallotNamer
# BALLOTZIPPER_FOLDER=/BallotZipper
# TABREPLACER_FOLDER=/TabReplacer
DIRECTION=$1
TO_DIR=
FROM_DIR=


# check existence
if [ ! -d "${WORK_FOLDER}" ]
then
  echo "${WORK_FOLDER} doesn't exit, So quitting."; exit;
fi
if [ ! -d "${GIT_FOLDER}" ]
then
  echo "${GIT_FOLDER} doesn't exit, So quitting."; exit;
fi
# check parameter
if [ "$DIRECTION" = "work2git" ]
then
  FROM_DIR="${WORK_FOLDER}"
  TO_DIR="${GIT_FOLDER}"
elif [ "$DIRECTION" = "git2work" ]
then
  FROM_DIR="${GIT_FOLDER}"
  TO_DIR="${WORK_FOLDER}"
else
  echo "Bad parameter, So quitting."; exit;
fi

echo "FROM_DIR: $FROM_DIR"
echo "TO_DIR: $TO_DIR"

# echo "copying contestgen properties\n"
cp "${FROM_DIR}/contestgen/resources/contestgen.properties" "${TO_DIR}/contestgen/resources/contestgen.properties"

# echo "copying ballotgen properties\n"
cp "${FROM_DIR}/ballotgen/resources/ballotgen.properties" "${TO_DIR}/ballotgen/resources/ballotgen.properties"

# echo "copying ballotnamer properties\n"
cp "${FROM_DIR}/ballotnamer/resources/ballotnamer.properties" "${TO_DIR}/ballotnamer/resources/ballotnamer.properties"

# echo "copying ballotzipper properties\n"
cp "${FROM_DIR}/ballotzipper/resources/ballotzipper.properties" "${TO_DIR}/ballotzipper/resources/ballotzipper.properties"

# echo "copying tabreplacer properties\n"
cp "${FROM_DIR}/tabreplacer/resources/tabreplacer.properties" "${TO_DIR}/tabreplacer/resources/tabreplacer.properties"


echo "DONE."
