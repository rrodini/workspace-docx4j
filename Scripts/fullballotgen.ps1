# fullballotgen.psq is a powershell script that drives the generation
# of municipal ballot docx files (approx. 232).

$VOTER_SERVICES_SPECIMEN="General-2021.pdf"
$VOTER_SERVICES_PAGES_PER_BALLOT=2
$PRECINCTS_ZONES_CSV="contests-zones.csv"
$JVM_LOG4J_LEVEL="-Dlog.level=ERROR"
$JVM_LOG4J_CONFIG="-Dlog4j.configurationFile=.\resources\log4j-file-config.xml"

$BALLOTGEN_VERSION=(Get-Item env:BALLOTGEN_VERSION).value
# Check that Ballot Gen Version is set
if ($BALLOTGEN_VERSION) {
    Write-Output "`r`nBALLOTGEN_VERSION: ${BALLOTGEN_VERSION}`r`n"
} else {
    Write-Output "`r`nBALLOTGEN_VERSION environment variable not defined -- Quitting.`r`n"
    Exit 1
}

# run PDFBOX to split voter service's PDF into municipal level PDFs
Write-Output "Splitting ${VOTER_SERVICES_SPECIMEN} into municipal PDFs."
Set-Location .\output
java -jar ..\PDFBOX\pdfbox-app-2.0.25.jar PDFSplit -split $VOTER_SERVICES_PAGES_PER_BALLOT -outputPrefix municipal "..\${VOTER_SERVICES_SPECIMEN}"
Set-Location ..

# run a script to extract the text from the municipal PDFs
Write-Output "Extracting text from municipal PDFs."
$FILE_PATH=Get-ChildItem -Path .\output
forEach ($f IN $FILE_PATH) {
    Write-Output "Extracting text: $f"
    java -jar .\PDFBOX\pdfbox-app-2.0.25.jar ExtractText .\output\$f
}

# run BallotNamer to rename files and do some pre-processing
Write-Output "Renaming municipal files."
Set-Location .\ballotnamer
java $JVM_LOG4J_LEVEL $JVM_LOG4J_CONFIG -jar "ballot-namer-$BALLOTGEN_VERSION-jar-with-dependencies.jar"  ..\output
Set-Location ..

# run BallotGen to generate .docx files for distribution
Write-Output "Generating municipal .docx files."
Set-Location .\ballotgen
java $JVM_LOG4J_LEVEL $JVM_LOG4J_CONFIG -jar "ballot-gen-$BALLOTGEN_VERSION-jar-with-dependencies.jar"  ..\output ..\contests
Set-Location ..

# run BallotZipper to generate .zip files for distribution
Write-Output "Generating zone .zip files."
Set-Location .\ballotzipper
java $JVM_LOG4J_LEVEL $JVM_LOG4J_CONFIG -jar "ballot-zipper-$BALLOTGEN_VERSION-jar-with-dependencies.jar" "..\${$PRECINCTS_ZONES_CSV}" ..\output ..\zip
Set-Location ..
