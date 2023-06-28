# fullballotgen.ps1 is a powershell script that drives the generation
# municipal sample ballot docx files. Activate one step at a time.
# STEP0  - MANUAL (see BallotGen SuperUser's Guide)
# STEP1  - BALLOT PREP
# STEP2  - BALLOT NAME
# STEP3  - CONTEST GENERATE
# STEP4  - BALLOT GENERATE  <= For Democratic Committee experts
# STEP4a - BALLOT CUSTOMIZE <= MANUAL (see BallotGen SuperUser's Guide)
# STEP5  - BALLOT ZIP       <= For Democratic Committee experts
# TO activate a STEP, set STEPx value to 1
$STEP1=0
$STEP2=0
$STEP3=0
$STEP4=0
$STEP5=0

# if ($STEP1 -eq $true) {
#     Write-Output "Step1 is true"
# } else {
#     Write-Output "Step1 is false"
# }
# function Show-Params {
#     param ($Param1, $Param2)
#     Write-Output "Param1: $Param1"
#     Write-Output "Param2: $Param2"
# }
# Show-Params "hello" "goodbye"
# Exit 0
# GLOBAL VARIABLES
$global:COUNTY_INPUT = ""
$global:COUNTY_OUTPUT = ""
$global:COUNTY_CONTESTS=""
$global:COUNTY_ZIP=""
$global:VOTER_SERVICES_SPECIMEN=""
$global:VOTER_SERVICES_PAGES_PER_BALLOT=2
$global:PRECINCTS_ZONES_CSV=""
# Log.levels: ERROR, WARN, INFO, DEBUG, TRACE
$JVM_LOG4J_LEVEL="-Dlog.level=ERROR -XX:+ShowCodeDetailsInExceptionMessages"
$JVM_LOG4J_CONFIG="-Dlog4j.configurationFile=.\resources\log4j-file-config.xml"
$BALLOTGEN_VERSION=(Get-Item env:BALLOTGEN_VERSION).value
$BALLOTGEN_COUNTY=(Get-Item env:BALLOTGEN_COUNTY).value
# GLOBAL FUNCTIONS
# set the HOME drive and directory
function set_home {
    G:
    Set-Location "\My Drive\SampleBallotGen-$BALLOTGEN_VERSION"
}
# check that the environment variables are defined.
function run_check_env_variables {
    if ($BALLOTGEN_VERSION) {
        Write-Output "BALLOTGEN_VERSION: ${BALLOTGEN_VERSION}"
    } else {
        Write-Output "`r`nBALLOTGEN_VERSION environment variable not defined -- Quitting.`r`n"
        Exit 1
    }
    
    # Check that Ballot Gen County is set
    if ($BALLOTGEN_COUNTY) {
        Write-Output "BALLOTGEN_COUNTY: ${BALLOTGEN_COUNTY}"
    } else {
        Write-Output "BALLOTGEN_COUNTY environment variable not defined -- Quitting."
        Exit 1
    }
}
function run_populate_globals {
    if ( $BALLOTGEN_COUNTY -eq "chester" ) {
Write-Output "Initializing Chester Co. values"
        $global:COUNTY_INPUT = ".\chester-input"
        $global:COUNTY_OUTPUT = ".\chester-output"
        $global:COUNTY_CONTESTS=".\chester-contests"
        $global:COUNTY_ZIP=".\chester-zip"
        $global:VOTER_SERVICES_SPECIMEN="Chester-Primary-Dems-2023.pdf"
        $global:VOTER_SERVICES_PAGES_PER_BALLOT=2
        $global:PRECINCTS_ZONES_CSV="chester-2023-precincts-zones.csv"
    } elseif ($BALLOTGEN_COUNTY -eq "bucks" ) {
        $global:COUNTY_INPUT=".\bucks-input"
        $global:COUNTY_OUTPUT=".\bucks-output"
        $global:COUNTY_CONTESTS=".\bucks-contests"
        $global:COUNTY_ZIP=".\bucks-zip"
        $global:VOTER_SERVICES_SPECIMEN="Bucks-Primary-Dems-2023.txt"
        $global:VOTER_SERVICES_PAGES_PER_BALLOT=2
        $global:PRECINCTS_ZONES_CSV="bucks-precincts-zones.csv"
    } else {
        Write-Output "Bad BALLOTGEN_COUNTY -- Quitting.\n"
        Exit 2
    }
}
function run_echo_county {
    Write-Output "${BALLOTGEN_COUNTY} co."
}
# extract text from a single PDF file.
function run_PDF_extract {
    param (
        $SPECIMEN
    )
    Write-Output "Extracting text from ${SPECIMEN}"
    java -jar .\PDFBOX\pdfbox-app-2.0.25.jar ExtractText "${SPECIMEN}"
}
# extract text from all files in a folder.
function run_PDF_extract_in_folder {
    param (
        $FOLDER
    )
    $FILE_PATH=Get-ChildItem -Path .\${FOLDER}
    forEach ($F IN $FILE_PATH) {
        Write-Output "Extracting text: $F"
        java -jar .\PDFBOX\pdfbox-app-2.0.25.jar ExtractText ".\${FOLDER}\${F}"
    }
}
# split the large PDF into municipal level PDFs
function run_PDF_split {
    Write-Output "Splitting ${VOTER_SERVICES_SPECIMEN} into municipal PDFs."
    Set-Location ".\${COUNTY_OUTPUT}"
    java -jar ..\PDFBOX\pdfbox-app-2.0.25.jar PDFSplit -split $VOTER_SERVICES_PAGES_PER_BALLOT -outputPrefix municipal "..\$COUNTY_INPUT\$VOTER_SERVICES_SPECIMEN"
    Set-Location ..
}
# replace the tabs in all text files in folder (parameter).
function run_tabreplacer_in_folder {
    param (
        $FOLDER
    )
    $FILE_PATH=Get-ChildItem -Path .\${FOLDER}
    forEach ($F IN $FILE_PATH) {
        Set-Locacation .\tabreplacer
        Write-Output "tab replacing: $F"
        # Note the dot before $F!
        java ${JVM_LOG4J_LEVEL} ${JVM_LOG4J_CONFIG} -jar ".\tab-replacer-${BALLOTGEN_VERSION}-jar-with-dependencies.jar" ".$F"
    }
}
# rename the files in the folder (parameter).
function run_ballotnamer {
    param (
        $FOLDER
    )
    Write-Output "Renaming municipal files."
    Set-Location .\ballotnamer
    java $JVM_LOG4J_LEVEL $JVM_LOG4J_CONFIG -jar "ballot-namer-$BALLOTGEN_VERSION-jar-with-dependencies.jar"  ..\${FOLDER}
    Set-Location ..  
}
# generate the contest files from specimen (parameter) to folder (parameter).
function run_contestgen {
    param (
        $SPECIMEN,
        $CONTESTS_FOLDER
    )
    Write-Output "Running contest generation."
    Set-Location .\contestgen
    java ${JVM_LOG4J_LEVEL} ${JVM_LOG4J_CONFIG} -jar "contest-gen-${BALLOTGEN_VERSION}-jar-with-dependencies.jar" ..\${SPECIMEN} ..\${CONTESTS_FOLDER}
    Set-Location ..
    }
# generate the docx files for txt files in folder (parameter) using contests in folder (parameter).
function run_ballotgen {
    param (
        $OUTPUT_FOLDER,
        $CONTESTS_FOLDER
    )
    Write-Output "Generating municipal .docx files."
    Set-Location .\ballotgen
    java $JVM_LOG4J_LEVEL $JVM_LOG4J_CONFIG -jar "ballot-gen-$BALLOTGEN_VERSION-jar-with-dependencies.jar"  ..\${OUTPUT_FOLDER} ..\${CONTESTS_FOLDER}
    Set-Location ..
}
# zip the docx (and others) for a zone into a zip file in folder.
function run_ballotzipper {
    param (
        $PRECINCTS_ZONES_CSV_PATH,
        $OUTPUT_FOLDER,
        $ZIP_FOLDER
    )
    Write-Output "Generating zone .zip files."
    Set-Location .\ballotzipper
    java $JVM_LOG4J_LEVEL $JVM_LOG4J_CONFIG -jar "ballot-zipper-$BALLOTGEN_VERSION-jar-with-dependencies.jar" "..\${PRECINCTS_ZONES_CSV_PATH}" ..\${OUTPUT_FOLDER} ..\${ZIP_FOLDER}
    Set-Location ..
    }
# STEP0 - MANUAL/GET INPUTS
# chester - get VS specimen pdf and csv files to .\chester-input
# bucks -   get precinct pdfs and csv files to   .\bucks-input
run_check_env_variables
run_populate_globals
# STEP1 - BALLOT PREP
# chester - split VS specimen pdf into precinct pdfs in .\chester-output
# chester - extract text from precinct pdfs
# bucks - copy precinct pdfs from .\bucks-input to .\bucks-output
# bucks - extract text from precinct pdfs
# bucks - run tab replacer to change \t to space
# bucks - concat txt files into .\bucks-input\$VOTER_SERVICES_SPECIMEN.txt
if ($STEP1 -eq $true) {
    Write-Output "STEP1 - BALLOT PREP";
    if ($BALLOTGEN_COUNTY -eq "chester") {
        run_PDF_split
        run_PDF_extract_in_folder "${COUNTY_OUTPUT}"
    } elseif ( $BALLOTGEN_COUNTY -eq "bucks" ) {
        Copy-Item -Filter *.txt -Path ${COUNTY_INPUT} -Destination "${COUNTY_OUTPUT}"
        run_PDF_extract_in_folder "${COUNTY_OUTPUT}"
        run_tabreplacer_in_folder "${COUNTY_OUTPUT}"
        Get-Content ${COUNTY_OUTPUT}\*.txt | Set-Content ${COUNTY_INPUT}\${VOTER_SERVICES_SPECIMEN}
    } else {
        Write-Output -e "Bad BALLOTGEN_COUNTY -- Quitting.\n"
        exit 2
    }
}
# STEP2 - BALLOT NAME
# chester\bucks - run BallotNamer program on files in .\county-output
# Notes:
# 1. Uses regex from contestgen.properties
# 2. New name pattern:  nnn_precinct_name.pdf, nnn_precinct_name.txt
if ( $STEP2 -eq $true ) {
    Write-Output "STEP2 - BALLOT NAME";
    run_ballotnamer "${COUNTY_OUTPUT}"
}
# STEP3 - CONTEST GENERATE
# chester\bucks - run ContestGen to generate contest files in .\county-contests
# Notes:
# 1. Uses all regexs from contestgen.properties
if ( $STEP3 -eq $true ) {
    Write-Output "STEP3 - CONTEST GENERATE";
    $SPECIMEN_TXT = $VOTER_SERVICES_SPECIMEN.Replace('pdf','txt')
    run_contestgen "${COUNTY_INPUT}\$SPECIMEN_TXT" "${COUNTY_CONTESTS}"
}
# STEP4 - BALLOT GENERATE
# chester/bucks - run BallotGen to generate docx files in .\county-output
# Notes:
# 1. Uses some regexs from contestgen.properties
if ( $STEP4 -eq $true) {
    Write-Output "STEP4 - BALLOT GENERATE";
    run_ballotgen "${COUNTY_OUTPUT}" "${COUNTY_CONTESTS}"
}
# STEP5 - BALLOT ZIP
# chester\bucks - run BallotZipper to generate zip files in .\county-zip
if ( $STEP5 -eq $true) {
    Write-Output "STEP5 - BALLOT ZIP";
    run_ballotzipper "${COUNTY_INPUT}\${PRECINCTS_ZONES_CSV}" "${COUNTY_OUTPUT}" "${COUNTY_ZIP}"
}
