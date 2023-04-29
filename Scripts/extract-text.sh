#!/bin/bash
# Extract the text from Specimen PDF file using PDFBOX
FILE="Primary-Dems-2021.pdf"
"Extracting: $FILE"; java -jar pdfbox-app-2.0.25.jar ExtractText $FILE