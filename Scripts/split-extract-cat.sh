#!/bin/bash
# Script is a workaround to the password protection scheme used
# by Chester Co. Voter Services using PDF box utitilies.
# Steps:
# 1) Split the master PDF into two PDFs (breaks protection)
# 2) Extract the text from each PDF
# 3) Concatenate the two text files into one.
# Notes:
# - Script assumes there are TWO PAGES per precinct ballot in all elections.
PREP_DIR="./chester-prep"  # no final / please
SPECIMEN_PDF="2024_General_Election-VS.pdf"
SPECIMEN_NAME="${SPECIMEN_PDF%.*}"
cd "./$PREP_DIR" || exit
echo "$SPECIMEN_NAME"
echo "Splitting: $SPECIMEN_PDF"
java -jar ../PDFBOX/pdfbox-app-3.0.2.jar split -split 231 -outputPrefix "${SPECIMEN_NAME}" -i="${SPECIMEN_PDF}"
SPLIT_PDF1="${SPECIMEN_NAME}-1.PDF"
echo "Extracting: ${SPLIT_PDF1}"
java -jar ../PDFBOX/pdfbox-app-3.0.2.jar export:text -encoding UTF-8  -i="$SPLIT_PDF1"
SPLIT_PDF2="${SPECIMEN_NAME}-2.PDF"
echo "Extracting: ${SPLIT_PDF2}"
java -jar ../PDFBOX/pdfbox-app-3.0.2.jar export:text -encoding UTF-8  -i="$SPLIT_PDF2"
echo "Concatenating: ${SPECIMEN_NAME}.txt"
cat "${SPECIMEN_NAME}-1.txt" "${SPECIMEN_NAME}-2.txt" > "${SPECIMEN_NAME}.txt"
cd .. || exit
