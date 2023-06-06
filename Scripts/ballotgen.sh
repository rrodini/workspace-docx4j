#!/bin/bash
#
# ballotgen.sh is the script that generates a single
# municipal sample ballot docx file. It can be used to
# switch from letter size to legal size paper, for instance.
#
JVM_LOG4J_LEVEL="-Dlog.level=ERROR"
JVM_LOG4J_CONFIG="-Dlog4j.configurationFile=./resources/log4j-console-config.xml"
# COUNTY determines input/output directories:  chester | bucks
COUNTY=${BALLOTGEN_COUNTY}
CONTESTS_FILE="contests-primary-2018.txt"
MUNICIPAL_TXT_SPECIMEN="Birmingham_1.txt"

if [ -n BALLOTGEN_VERSION ]; then
  echo -e "\nBALLOTGEN_VERSION: ${BALLOTGEN_VERSION}\n"
else 
  echo -e "\nBALLOTGEN_VERSION environment variable not defined -- Quitting.\n"
  exit 0
fi
if [ -n "${BALLOTGEN_COUNTY}" ]; then
  echo -e "\nBALLOTGEN_COUNTY: ${BALLOTGEN_COUNTY}"
else 
  echo -e "\nBALLOTGEN_COUNTY environment variable not defined -- Quitting.\n"
  exit 0
fi

# run BallotGen to generate .docx file
printf '%s \n' "Generating municipal docx file"
cd ballotgen
java ${JVM_LOG4J_LEVEL} ${JVM_LOG4J_CONFIG} -jar "ballot-gen-${BALLOTGEN_VERSION}-jar-with-dependencies.jar" ../${COUNTY}-output ../${COUNTY}-contests
cd ..
