#!/bin/bash
#
# fullballotgen.sh is the script that drives the generation of
# municipal sample ballot docx files (approx. 232).
#
# COUNTY determines input/output directories:  chester | bucks
COUNTY=${BALLOTGEN_COUNTY}
#VOTER_SERVICES_SPECIMEN="Chester-Primary-Dems-2023.pdf"
VOTER_SERVICES_SPECIMEN="Bucks-Primary-Dems-2023.pdf"
VOTER_SERVICES_PAGES_PER_BALLOT=2
PRECINCTS_ZONES_CSV="../chester-2023-precincts-zones.csv"
JVM_LOG4J_LEVEL="-Dlog.level=DEBUG -XX:+ShowCodeDetailsInExceptionMessages"
JVM_LOG4J_CONFIG="-Dlog4j.configurationFile=./resources/log4j-file-config.xml"

if [ -n "${BALLOTGEN_VERSION}" ]; then
  echo -e "\nBALLOTGEN_VERSION: ${BALLOTGEN_VERSION}"
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


# run PDFBOX to merge Bucks Co. Voter Service's PDFs into one county PDF.
# printf '%s\n' "Merging Bucks municipal PDFs into county PDF"
# cd "./${COUNTY}-input" || exit
# java -jar ../PDFBOX/pdfbox-app-2.0.25.jar PDFMerger 347-BEDMINSTER_TWP_EAST--OFF-DEM-EN.pdf \
# 347-BEDMINSTER_TWP_WEST--OFF-DEM-EN.pdf \
# 349-BENSALEM_TWP_LOWER_EAST_1--OFF-DEM-EN.pdf \
# 349-BENSALEM_TWP_LOWER_EAST_2--OFF-DEM-EN.pdf \
#   "../Bucks-Primary-Dems-2023.pdf"
# cd .. || exit


## ATTENTION - Voter Services has a flaw in the patterns within General-2021.txt. 
##             This flaw must be fixed by  manually!
## extract text from voter service's PDF file.
printf '%s\n' "Extracting text from ${VOTER_SERVICES_SPECIMEN}"
#java -jar ./PDFBOX/pdfbox-app-2.0.25.jar ExtractText "${VOTER_SERVICES_SPECIMEN}"

if [ $COUNTY = "bucks" ] 
then
 printf '%s\n' "Replacing tabs by spaces."
 cd ./tabreplacer
# java ${JVM_LOG4J_LEVEL} ${JVM_LOG4J_CONFIG} -jar "tab-replacer-${BALLOTGEN_VERSION}-jar-with-dependencies.jar" "../${VOTER_SERVICES_SPECIMEN/.pdf/.txt}"
 cd ..
fi

# run ContestGen to populate the contests directory.
cd ./contestgen || exit
#java ${JVM_LOG4J_LEVEL} ${JVM_LOG4J_CONFIG} -jar "contest-gen-${BALLOTGEN_VERSION}-jar-with-dependencies.jar" "../${VOTER_SERVICES_SPECIMEN/.pdf/.txt}" ../${COUNTY}-contests
cd .. || exit


# run PDFBOX to split voter service's PDF into municipal level PDFs.
printf '%s\n' "Splitting ${VOTER_SERVICES_SPECIMEN} into municipal PDFs"
cd "./${COUNTY}-output" || exit
#java -jar ../PDFBOX/pdfbox-app-2.0.25.jar PDFSplit -split $VOTER_SERVICES_PAGES_PER_BALLOT -outputPrefix municipal "../${VOTER_SERVICES_SPECIMEN}"
cd .. || exit

# run PDFBOX to get the text extracted from the municipal PDFs.
# printf '%s\n' "Extracting text from municipal PDFs"
# for FILE in ./"${COUNTY}-output"/*; do 
#   echo "Extracting: $FILE"; 
#   java -jar ./PDFBOX/pdfbox-app-2.0.25.jar ExtractText $FILE
#   if [ $COUNTY = "bucks" ] 
#   then
#     cd ./tabreplacer
#     printf '%s\n' "Replacing tabs by spaces."
#     java ${JVM_LOG4J_LEVEL} ${JVM_LOG4J_CONFIG} -jar "tab-replacer-${BALLOTGEN_VERSION}-jar-with-dependencies.jar" "../${FILE/.pdf/.txt}"
#     cd ..
#   fi
# done

# run BallotNamer to rename files and do some pre-processing.
printf '%s \n' "Renaming municipal files"
cd ./ballotnamer || exit
#java ${JVM_LOG4J_LEVEL} ${JVM_LOG4J_CONFIG} -jar "ballot-namer-${BALLOTGEN_VERSION}-jar-with-dependencies.jar" ../${COUNTY}-output
cd ..|| exit

# run BallotGen to generate .docx files for distribution
printf '%s \n' "Generating municipal docx files"
cd ballotgen
java ${JVM_LOG4J_LEVEL} ${JVM_LOG4J_CONFIG} -jar "ballot-gen-${BALLOTGEN_VERSION}-jar-with-dependencies.jar" ../${COUNTY}-output ../${COUNTY}-contests
cd .. || exit

# run BallotZipper to generate .zip files for distribution
printf '%s \n' "Generating zone .zip files"
cd ballotzipper || exit
#java ${JVM_LOG4J_LEVEL} ${JVM_LOG4J_CONFIG} -jar "ballot-zipper-${BALLOTGEN_VERSION}-jar-with-dependencies.jar" "${COUNTY}-${PRECINCTS_ZONES_CSV}" ../${COUNTY}-output ../${COUNTY}-zip
cd .. || exit

echo "DONE."