#!/bin/bash
# Split the Specimen PDF file using PDFBOX
FILE="./chester-prep/Chester_2024Primary_Democratic_Specimens_2-29-24-google.pdf"
echo "Splitting: $FILE"
java -jar PDFBOX/pdfbox-app-2.0.25.jar PDFSplit -split 80 -outputPrefix Chester_2024Primary_Democratic_Specimens $FILE