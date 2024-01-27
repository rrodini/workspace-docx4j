#!/bin/bash
# Extract the text from Specimen PDF file using PDFBOX
FILE="./chester-input/2020_PRIMARY_DEMOCRATIC_SPECIMEN.pdf"
echo "Extracting: $FILE"
java -jar PDFBOX/pdfbox-app-2.0.25.jar ExtractText -encoding UTF-8 $FILE