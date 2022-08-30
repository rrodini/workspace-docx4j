#!/bin/bash
#
# savepropertyfiles.sh is the script that saves the various properties
# and configuration files that were used in previous sample ballot gen
# releases.
#
WORK_FOLDER="/Users/robert/Documents/Sample Ballot Production"
SPECIMEN_FILE="General-2021.pdf"
SAVE_FOLDER="/Users/robert/Documents/Sample Ballot Production/Specimens"

if [ -n BALLOTGEN_VERSION ]; then
  echo -e "\nBALLOTGEN_VERSION: ${BALLOTGEN_VERSION}\n"
else 
  echo -e "\nBALLOTGEN_VERSION environment variable not defined -- Quitting.\n"
  exit 0
fi
WORK_FOLDER="${WORK_FOLDER}/SampleBallotGen-${BALLOTGEN_VERSION}"
SAVE_FOLDER="${SAVE_FOLDER}/${SPECIMEN_FILE/.pdf//}"

echo "WORK_FOLDER"
echo ${WORK_FOLDER}
echo "SAVE_FOLDER"
echo ${SAVE_FOLDER}

mkdir "${SAVE_FOLDER}"

echo "SAVING VOTER SERVICES FILE"
cp "${WORK_FOLDER}/${SPECIMEN_FILE}" "${SAVE_FOLDER}"
echo "SAVING PROPERTY FILES"
cp -v "${WORK_FOLDER}/contestgen/resources/contestgen.properties"   "${SAVE_FOLDER}"
cp -v "${WORK_FOLDER}/ballotnamer/resources/ballotnamer.properties"  "${SAVE_FOLDER}"
cp -v "${WORK_FOLDER}/ballotgen/resources/ballotgen.properties"    "${SAVE_FOLDER}"
cp -v "${WORK_FOLDER}/ballotzipper/resources/ballotzipper.properties" "${SAVE_FOLDER}"

