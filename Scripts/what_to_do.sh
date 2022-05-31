#!/bin/bash
#
# what_to_do.sh is the interactive script that
# the ballotgen superuser uses when a new ballot
# specimen is received from Chester Co. Voter Services.

# Global Variables
PDF_FILE=""
JVM_LOG4J_LEVEL="-Dlog.level=INFO"
JVM_LOG4J_CONFIG="-Dlog4j.configurationFile=./resources/log4j-console-config.xml"
#BALLOTGEN_VERSION is now a mandated ENVIRONMENT variable

# Main menu - Option 5 to quit or CTRL-C
menu() {
echo "Make a selection:"
MENU="
1) Extract text from PDF
2) Split Master PDF
3) Run ContestGen
4) Run BallotNamer
5) Run BallotGen
6) Clear output folder
7) Quit"

echo "${MENU}"
read OPTION
  case $OPTION in
    1)
      extractPdfText; menu
      ;;
    2)
      splitMasterPdf; menu
      ;;
    3)
      runContestGen; menu
      ;;
    4)
      runBallotNamer; menu
      ;;
    5)
      runBallotGen; menu
      ;;
    6)
      clearOutputFolder; menu
      ;;
    7)
      exit 0
      ;;
    *) 
      echo "Invalid option $REPLY"
      ;;
  esac
}
# extractPdfText - get PDF file from user. Then extract it to ./output folder
#   Use at beginning for entire VS PDF
#   then later for first municipal PDF
extractPdfText() {
  echo "Enter path to PDF to extract:"
  read  PDF_FILE
  if [ -f "${PDF_FILE}" ]; then
    echo "Extracting text from ${PDF_FILE}"
    java -jar ./PDFBOX/pdfbox-app-2.0.25.jar ExtractText "${PDF_FILE}"
  else
    echo "${PDF_FILE} does not exist."
  fi
}
# splitMasterPdf - split off one or all of the municipal PDFs
#    within the Voter Services master PDF.
splitMasterPdf() {
  echo "About to split ${PDF_FILE}"
  echo "How to split: all or one?"
  read HOW
  if [ $HOW == "one" ] || [ $HOW == "all" ]; then
    :
  else
    echo "Sorry, wrong reply"
    return
  fi
  echo "How many pages per municipality: 1 or 2?"
  read MANY
  if [ $MANY == 1 ] || [ $MANY == 2 ]; then
    :
  else
    echo "Sorry, wrong reply"
    return
  fi
  cd ./output
  case $HOW in
  one)
    echo "Splitting first $MANY page(s) of ${PDF_FILE} into ONE PDF"
    java -jar ../PDFBOX/pdfbox-app-2.0.25.jar PDFSplit -startPage 1 -endPage $MANY -split $MANY -outputPrefix municipal "../${PDF_FILE}"
    ;;
  all)
    echo "Splitting all pages of ${PDF_FILE} into MANY PDFs"
    java -jar ../PDFBOX/pdfbox-app-2.0.25.jar PDFSplit -split $MANY -outputPrefix municipal "../${PDF_FILE}"
    ;;
  *) 
    echo "Invalid option $REPLY"
    ;;
  esac
  cd ..
}
# runContestGen - run the contest generating program.  This tests the regexes
#   therein and produces named municipal XYZ_contests.txt file.
runContestGen() {
  echo "Running ContestGen."
  LEN=${#PDF_FILE}
  PDF_TXT_FILE="${PDF_FILE:0:LEN-4}.txt" 
  if [ -f "${PDF_TXT_FILE}" ]; then
    cd ./contestgen
    # Note period stuck in front ( ./ -> ../ )
    java ${JVM_LOG4J_LEVEL} ${JVM_LOG4J_CONFIG} -jar "contest-gen-${BALLOTGEN_VERSION}-jar-with-dependencies.jar" ".${PDF_TXT_FILE}" ../contests
    cd ..
  else
    echo "${PDF_TXT_FILE} does not exist."
  fi
} 
# runBallotNamer - run the ballot naming program.  This tests the regexes
#   therein and produces the precontests.txt file. There should be just
#   one pdf/txt file in ./output folder, and each will be renamed.
runBallotNamer() {
  echo "Renaming municipal PDF & TXT files."
  cd ./ballotnamer
  java ${JVM_LOG4J_LEVEL} ${JVM_LOG4J_CONFIG} -jar "ballot-namer-${BALLOTGEN_VERSION}-jar-with-dependencies.jar" ../output
  cd ..
} 
# runBallotGen - run the ballot generation program.  This tests the regexes
#   therein and produces the DOCX file. There should be just
#   one pdf/txt file in ./output folder and a new docx file.
runBallotGen() {
  echo "Enter name of contests file:"
  read  CONTESTS_FILE
  if [ -f "./contests/${CONTESTS_FILE}" ]; then
    :
  else
    echo "./contests/${CONTESTS_FILE} does not exist."
    return
  fi
  echo "Generating municipal DOCX file."
  cd ./ballotgen
  java ${JVM_LOG4J_LEVEL} ${JVM_LOG4J_CONFIG} -jar "ballot-gen-${BALLOTGEN_VERSION}-jar-with-dependencies.jar" ../output ../contests
  cd ..
} 
# clearOutputFolder - remove all files in ./output folder
#   in preparation for running the fullballotgen script
clearOutputFolder() {
  echo "Removing all files in ./output"
  rm -rf ./output/*
}
# Main logic

if [ -n BALLOTGEN_VERSION ]; then
  echo -e "\nBALLOTGEN_VERSION: ${BALLOTGEN_VERSION}\n"
else 
  echo -e "\nBALLOTGEN_VERSION environment variable not defined -- Quitting.\n"
  exit 0
fi
menu;
