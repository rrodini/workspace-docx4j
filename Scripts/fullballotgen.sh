#!/bin/bash
#
# fullballotgen.sh is the script that drives the generation of
# municipal sample ballot docx files. Activate one step at a time.
# STEP0  - MANUAL (see BallotGen SuperUser's Guide)
# STEP1  - CONTEST GENERATE
# STEP2  - PDF SPLIT
# STEP3  - BALLOT NAME
# STEP4  - BALLOT GENERATE  <= For Democratic Committee experts
# STEP4a - BALLOT CUSTOMIZE <= OPTIONAL
# STEP5  - BALLOT ZIP       <= For Democratic Committee experts
# TO activate a STEP, set STEPx value to 1
STEP1=0
STEP2=0
STEP3=0
STEP4=0
STEP5=1

# GLOBAL VARIABLES
COUNTY_INPUT=""
COUNTY_OUTPUT=""
COUNTY_CONTESTS=""
COUNTY_ZIP=""
VOTER_SERVICES_SPECIMEN_PDF=""
VOTER_SERVICES_SPECIMEN_TXT=""
VOTER_SERVICES_PAGES_PER_BALLOT=2
PRECINCTS_ZONES_CSV=""
# Log.levels: ALL, ERROR, WARN, INFO, DEBUG, TRACE
JVM_LOG4J_LEVEL="-Dlog.level=ERROR -XX:+ShowCodeDetailsInExceptionMessages"
JVM_LOG4J_CONFIG="-Dlog4j.configurationFile=./resources/log4j-file-config.xml"

# GLOBAL FUNCTIONS
# check that the environment variables are defined.
run_check_env_variables() {
    if [ -n "${BALLOTGEN_VERSION}" ]; then
    echo -e "BALLOTGEN_VERSION: ${BALLOTGEN_VERSION}"
    else 
    echo -e "BALLOTGEN_VERSION environment variable not defined -- Quitting.\n"
    exit 1
    fi
    if [ -n "${BALLOTGEN_COUNTY}" ]; then
    echo -e "BALLOTGEN_COUNTY: ${BALLOTGEN_COUNTY}"
    else 
    echo -e "BALLOTGEN_COUNTY environment variable not defined -- Quitting.\n"
    exit 1
    fi 
}
# populate global variables as per BALLOTGEN_COUNTY.
run_populate_globals() {
    if [ $BALLOTGEN_COUNTY = "chester" ];
    then
        COUNTY_INPUT="./chester-input"
        COUNTY_OUTPUT="./chester-output"
        COUNTY_CONTESTS="./chester-contests"
        COUNTY_ZIP="./chester-zip"
        VOTER_SERVICES_SPECIMEN_PDF="2020_PRIMARY_DEMOCRATIC_SPECIMEN.pdf"
        VOTER_SERVICES_SPECIMEN_TXT="2020_PRIMARY_DEMOCRATIC_SPECIMEN.txt"
        VOTER_SERVICES_PAGES_PER_BALLOT=1
        PRECINCTS_ZONES_CSV="chester-2024-precincts-zones.csv"
    elif [ $BALLOTGEN_COUNTY = "bucks" ];
    then
        COUNTY_INPUT="./bucks-input"
        COUNTY_OUTPUT="./bucks-output"
        COUNTY_CONTESTS="./bucks-contests"
        COUNTY_ZIP="./bucks-zip"
        # No county-wide PDF file
        VOTER_SERVICES_SPECIMEN_TXT="Bucks-Primary-Dems-2023.txt"
        VOTER_SERVICES_PAGES_PER_BALLOT=2
        PRECINCTS_ZONES_CSV="bucks-precincts-zones.csv"
    else
        echo -e "Bad BALLOTGEN_COUNTY -- Quitting.\n"
        exit 2
    fi
}
function run_echo_county() {
    echo "${BALLOTGEN_COUNTY} co." 
}
# extract the text from a single PDF file.
function run_PDF_extract() {
    printf '%s\n' "Extracting text from $1"
    java -jar ./PDFBOX/pdfbox-app-2.0.25.jar ExtractText -encoding UTF-8  "$1"
}
# extract text from all files in folder.
function run_PDF_extract_in_folder() {
    for FILE in ./$1/*; do 
        echo "Extracting: $FILE"; 
        java -jar ./PDFBOX/pdfbox-app-2.0.25.jar ExtractText -encoding UTF-8  "$FILE"
    done
}
# split the large PDF into municipal PDFs.
function run_PDF_split() {
    printf '%s\n' "Splitting ${VOTER_SERVICES_SPECIMEN_PDF} into municipal PDFs"
    cd "./${COUNTY_OUTPUT}" || exit
    java -jar ../PDFBOX/pdfbox-app-2.0.25.jar PDFSplit -split $VOTER_SERVICES_PAGES_PER_BALLOT -outputPrefix municipal "../${COUNTY_INPUT}/${VOTER_SERVICES_SPECIMEN_PDF}"
    cd .. || exit
}
# replace the tabs on all text files in folder (parameter).
function run_tabreplacer_in_folder() {
    for FILE in ./$1/*.txt; do
        cd ./tabreplacer
        echo "tab replacing: $FILE"; 
        # Note the dot before $FILE!
        java ${JVM_LOG4J_LEVEL} ${JVM_LOG4J_CONFIG} -jar "./tab-replacer-${BALLOTGEN_VERSION}-jar-with-dependencies.jar" ".$FILE"
        cd ..
    done
}
# rename the files in the folder (parameter).
function run_ballotnamer() {
    printf '%s \n' "Renaming municipal files"
    cd ./ballotnamer || exit
    java ${JVM_LOG4J_LEVEL} ${JVM_LOG4J_CONFIG} -jar "ballot-namer-${BALLOTGEN_VERSION}-jar-with-dependencies.jar" ../$1
    cd ..|| exit
}
# generate the contest files from specimen (parameter) to folder (parameter).
function run_contestgen() {
    printf '%s \n' "Extracting contest/ballot files"
    cd ./contestgen || exit
    java ${JVM_LOG4J_LEVEL} ${JVM_LOG4J_CONFIG} -jar "contest-gen-${BALLOTGEN_VERSION}-jar-with-dependencies.jar" ../$1 ../$2 ../$3
    cd ..|| exit
}
# generate the docx files for txt files in folder (parameter) using contests in folder (parameter).
function run_ballotgen() {
    printf '%s \n' "Generating municipal docx files"
    cd ballotgen
    java ${JVM_LOG4J_LEVEL} ${JVM_LOG4J_CONFIG} -jar "ballot-gen-${BALLOTGEN_VERSION}-jar-with-dependencies.jar" ../$1 ../$2
    cd .. || exit
}
# zip the docx (and others) for a zone into a zip file in folder.
function run_ballotzipper() {
    cd ballotzipper
    java ${JVM_LOG4J_LEVEL} ${JVM_LOG4J_CONFIG} -jar "ballot-zipper-${BALLOTGEN_VERSION}-jar-with-dependencies.jar" ../$1 ../$2 ../$3
    cd .. || exit
}
# STEP0 - MANUAL/GET INPUTS
# chester - get VS specimen pdf and csv files to ./chester-input
# bucks -   get precinct pdfs and csv files to   ./bucks-input
run_check_env_variables
run_populate_globals


# STEP1 - CONTEST GENERATE
# chester/bucks - run ContestGen to generate contest files in ./county-contests
# Notes:
# 1. Uses all regexs from contestgen.properties
if (( $STEP1 ))
then
    echo "STEP1 - CONTEST GENERATE";
    run_contestgen "${COUNTY_INPUT}/${VOTER_SERVICES_SPECIMEN_TXT}" "${COUNTY_CONTESTS}" "${COUNTY_OUTPUT}"
fi

# STEP2 - BALLOT SPLIT
# chester - split VS specimen pdf into precinct pdfs in ./chester-output
# chester - extract text from precinct pdfs
# bucks - copy precinct pdfs from ./bucks-input to ./bucks-output
# bucks - extract text from precinct pdfs
# bucks - run tab replacer to change \t to space
# bucks - concat txt files into .\bucks-input\$VOTER_SERVICES_SPECIMEN_TXT
if (( $STEP2 ))
then
    echo "STEP2 - BALLOT SPLIT";
    if [ $BALLOTGEN_COUNTY = "chester" ]; then
        run_PDF_split
        #run_PDF_extract_in_folder "${COUNTY_OUTPUT}"
        # extract
    elif [ $BALLOTGEN_COUNTY = "bucks" ]; then
        cp "${COUNTY_INPUT}"/*.pdf "${COUNTY_OUTPUT}"/
        run_PDF_extract_in_folder "${COUNTY_OUTPUT}"
        run_tabreplacer_in_folder "${COUNTY_OUTPUT}"
        cat ${COUNTY_OUTPUT}/*.txt > ${COUNTY_INPUT}/${VOTER_SERVICES_SPECIMEN_TXT}
    else
        echo -e "Bad BALLOTGEN_COUNTY -- Quitting.\n"
        exit 2
    fi
fi

# STEP3 - BALLOT NAME
# chester/bucks - run BallotNamer program on files in ./county-output
# Notes:
# 1. Uses regex from contestgen.properties
# 2. New name pattern:  nnn_precinct_name.pdf, nnn_precinct_name.txt
if (( $STEP3 ))
then
    echo "STEP3 - BALLOT NAME";
    run_ballotnamer "${COUNTY_OUTPUT}"
fi

# STEP4 - BALLOT GENERATE
# chester/bucks - run BallotGen to generate docx files in ./county-output
# Notes:
# 1. Uses some regexs from contestgen.properties
if (( $STEP4 ))
then
    echo "STEP4 - BALLOT GENERATE";
    run_ballotgen "${COUNTY_OUTPUT}" "${COUNTY_CONTESTS}"
fi

# STEP5 - BALLOT ZIP
# chester/bucks - run BallotZipper to generate zip files in ./county-zip
if (( $STEP5 ))
then
    echo "STEP5 - BALLOT ZIP";
    run_ballotzipper "${COUNTY_INPUT}/${PRECINCTS_ZONES_CSV}" "${COUNTY_OUTPUT}" "${COUNTY_ZIP}"
fi
