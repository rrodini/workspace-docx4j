#!/bin/bash
# Extract the text from Specimen PDF file using PDFBOX
FILE="/Users/robert/Documents/Sample Ballot Production/Specimens/Primary-Dems-2024/Chester_2024Primary_Democratic_Specimens_LOCKED.pdf"
echo "Extracting: $FILE"
java -jar PDFBOX/pdfbox-app-3.0.2.jar export:text  -encoding UTF-8 -i="$FILE"