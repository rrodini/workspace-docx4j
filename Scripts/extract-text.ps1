# Extract text 
$FILE="./chester-input/2020_PRIMARY_DEMOCRATIC_SPECIMEN.pdf"
Write-Output "Extracting Text: $FILE"
java -jar .\PDFBOX\pdfbox-app-2.0.25.jar ExtractText -encoding UTF-8 $FILE
