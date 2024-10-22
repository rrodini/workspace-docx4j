#!/bin/bash
# Split the Specimen PDF file using PDFBOX
FILE="./chester-prep/2024_Primary_Democratic_Specimens.pdf"
echo "Splitting: $FILE"
#java -jar PDFBOX/pdfbox-app-2.0.25.jar PDFSplit -split 80 -outputPrefix Chester_2024Primary_Democratic_Specimens $FILE
java -jar PDFBOX/pdfbox-app-3.0.2.jar split -split 232 -outputPrefix 2024_Primary_Democratic_Ballots -i="$FILE"