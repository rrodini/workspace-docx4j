#!/bin/bash
# Clean the text from Specimen PDF file using PDFBOX
FILE="../chester-prep/2024_Primary_Democratic_OCR-Abby.txt"
echo "Cleaning: $FILE"
cd ./textcleaner || exit
java -jar text-cleaner-1.5.0-jar-with-dependencies.jar $FILE
cd .. || exit