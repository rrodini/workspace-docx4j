# ballotgen.ps1 is the script that generates a single
# municipal sample ballot docx file.  It can be used to
# switch from letter size to legal size paper, for instance.

$JVM_LOG4J_LEVEL="-Dlog.level=INFO"
$JVM_LOG4J_CONFIG="-Dlog4j.configurationFile=.\resources\log4j-file-config.xml"
$MUNICIPAL_TXT_SPECIMEN="Birmingham_1.txt"
$CONTESTS_FILE="contests-primary-2018.txt"
$BALLOTGEN_VERSION=(Get-Item env:BALLOTGEN_VERSION).value
# Check that Ballot Gen Version is set
Write-Output "`r`nBALLOTGEN_VERSION: ${BALLOTGEN_VERSION}`r`n"
if ($BALLOTGEN_VERSION) {
    Write-Output "`r`nBALLOTGEN_VERSION: ${BALLOTGEN_VERSION}`r`n"
} else {
    Write-Output "`r`nBALLOTGEN_VERSION environment variable not defined -- Quitting.`r`n"
    Exit 1
}
# run BallotGen to generate .docx file
Write-Output "Generating municipal docx file"
Set-Location ballotgen
java $JVM_LOG4J_LEVEL $JVM_LOG4J_CONFIG -jar "ballot-gen-${BALLOTGEN_VERSION}-jar-with-dependencies.jar"  ..\output\$MUNICIPAL_TXT_SPECIMEN ..\contests\$CONTESTS_FILE
Set-Location ..
