#!/bin/bash
# Extract the text from Specimen PDF file using PDFBOX
FILE="./chester-prep/2024_Presidential_General_Election_Specimen_Review.pdf"
echo "Extracting: $FILE"
java -jar PDFBOX/pdfbox-app-3.0.2.jar export:text  -encoding UTF-8 -i="$FILE"