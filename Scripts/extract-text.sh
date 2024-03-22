#!/bin/bash
# Extract the text from Specimen PDF file using PDFBOX
FILE="./chester-prep/Chester_2024Primary_Democratic_Specimens-1.pdf"
echo "Extracting: $FILE"
java -jar PDFBOX/pdfbox-app-2.0.25.jar ExtractText -encoding UTF-8 $FILE