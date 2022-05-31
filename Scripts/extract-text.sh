#!/bin/bash
# Extract the text from each PDF file using PDFBOX
for FILE in ./output/*; do echo "Extracting: $FILE"; java -jar pdfbox-app-2.0.25.jar ExtractText $FILE ; done