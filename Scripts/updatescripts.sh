#!/bin/bash
# updatescripts.sh refreshes the script files in the Git direcory
# Usage: ./updatescripts.sh
#
WORK_FOLDER=/Users/robert/Documents/"Sample Ballot Production"/SampleBallotGen-${BALLOTGEN_VERSION}
GIT_FOLDER=/Users/robert/git/workspace-docx4j/Scripts

cp -v "${WORK_FOLDER}/"*.sh "${GIT_FOLDER}"
cp -v "${WORK_FOLDER}/"*.ps1 "${GIT_FOLDER}"