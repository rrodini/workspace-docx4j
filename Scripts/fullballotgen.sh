#!/bin/bash
#
# fullballotgen.sh is the script that drives the generation of
# municipal sample ballot docx files (approx. 232).
#

VOTER_SERVICES_SPECIMEN="General-2021.pdf"
VOTER_SERVICES_PAGES_PER_BALLOT=2
#CONTESTS_FILE="../contests"
JVM_LOG4J_LEVEL="-Dlog.level=ERROR"
JVM_LOG4J_CONFIG="-Dlog4j.configurationFile=./resources/log4j-console-config.xml"

if [ -n BALLOTGEN_VERSION ]; then
  echo -e "\nBALLOTGEN_VERSION: ${BALLOTGEN_VERSION}\n"
else 
  echo -e "\nBALLOTGEN_VERSION environment variable not defined -- Quitting.\n"
  exit 0
fi

# run PDFBOX to split voter service's PDF into municipal level PDFs.
printf '%s\n' "Splitting ${VOTER_SERVICES_SPECIMEN} into municipal PDFs"
cd ./output
#java -jar ../PDFBOX/pdfbox-app-2.0.25.jar PDFSplit -split $VOTER_SERVICES_PAGES_PER_BALLOT -outputPrefix municipal "../${VOTER_SERVICES_SPECIMEN}"
cd ..

# run a script PDFBOX to get the text extracted from the municipal PDFs.
printf '%s\n' "Extracting text from municipal PDFs"
#for FILE in ./output/*; do echo "Extracting: $FILE"; java -jar ./PDFBOX/pdfbox-app-2.0.25.jar ExtractText $FILE ; done

# run BallotNamer to rename files and do some pre-processing.
printf '%s \n' "Renaming municipal files"
cd ./ballotnamer
#java ${JVM_LOG4J_LEVEL} ${JVM_LOG4J_CONFIG} -jar "ballot-namer-${BALLOTGEN_VERSION}-jar-with-dependencies.jar" ../output
cd ..

# run BallotGen to generate .docx files for distribution
printf '%s \n' "Generating municipal docx files"
cd ballotgen
java ${JVM_LOG4J_LEVEL} ${JVM_LOG4J_CONFIG} -jar "ballot-gen-${BALLOTGEN_VERSION}-jar-with-dependencies.jar" ../output ../contests
cd ..
