# what_to_do.ps1 is the interactive script that
# the ballotgen superuser uses when a new ballot
# specimen is received from Chester Co. Voter Services.

# Global Variables
$PDF_FILE=""
$JVM_LOG4J_LEVEL="-Dlog.level=INFO"
$JVM_LOG4J_CONFIG="-Dlog4j.configurationFile=./resources/log4j-console-config.xml"
# Main menu - Option 5 to quit or CTRL-C
function menu {
    Write-Host "Make a selection:"
    $MENU = "
    1) Extract text from PDF
    2) Split Master PDF
    3) Run ContestGen
    4) Run BallotNamer
    5) Run BallotGen
    6) Clear output folder
    7) Quit"
    
    Write-Host $MENU
    $OPTION = Read-Host
    switch ($OPTION) {
        1 {
            extractPdfText
            menu
        }
        2 {
            splitMasterPdf
            menu
        }
        3 {
            runContestGen
            menu
        }
        4 {
            runBallotNamer
            menu
        }
        5 {
            runBallotGen
            menu
        }
        6 {
            clearOutputFolder
            menu
        }
        7 {
            exit 1
        }
        default {
            Write-Host "Invalid option: $OPTION"
        }     
    }
}
# extractPdfText - get PDF file from user. Then extract it to ./output folder
#   Use at beginning for entire VS PDF
#   then later for first municipal PDF
function extractPdfText {
    Write-Host "Enter path to PDF to extract:"
    $global:PDF_FILE = Read-Host
    if ( Test-Path -Path "${global:PDF_FILE}" ) {
      Write-Host "Extracting text from $global:PDF_FILE"
      java -jar ./PDFBOX/pdfbox-app-2.0.25.jar ExtractText "${global:PDF_FILE}"
    } else {
      Write-Host "$global:PDF_FILE does not exist."
    }
}
# splitMasterPdf - split off one or all of the municipal PDFs
#    within the Voter Services master PDF.
function splitMasterPdf {
    Write-Host "About to split ${global:PDF_FILE}"
    Write-Host "How to split: all or one?"
    $HOW = Read-Host
    if (( $HOW -eq "one" ) -or ( $HOW -eq "all")) {
    }
    else {
        Write-Host "Sorry, wrong reply"
        return
    }
    Write-Host "How many pages per municipality: 1 or 2?"
    $MANY = Read-Host
    if (($MANY -eq 1) -or ($MANY -eq 2 )) {
    }
    else {
        Write-Host "Sorry, wrong reply"
        return
    }
    Set-Location ./output
    switch ($HOW) {
        "one" {
            Write-Host "Splitting first $MANY page(s) of ${PDF_FILE} into ONE PDF"
            java -jar ../PDFBOX/pdfbox-app-2.0.25.jar PDFSplit -startPage 1 -endPage $MANY -split $MANY -outputPrefix municipal "../${global:PDF_FILE}"
        }
        "all" {
            Write-Host "Splitting all pages of ${PDF_FILE} into MANY PDFs"
            java -jar ../PDFBOX/pdfbox-app-2.0.25.jar PDFSplit -split $MANY -outputPrefix municipal "../${global:PDF_FILE}"
        }
        default {
            Write-Host "Invalid option $REPLY"
        }
    }
    Set-Location ..
}
# runContestGen - run the contest generating program.  This tests the regexes
#   therein and produces named municipal XYZ_contests.txt file.
function runContestGen {
    Write-Host "Running ContestGen."
    $LEN=$PDF_FILE.Length
    PDF_TXT_FILE="${PDF_FILE}.Substring(0, $LEN-4).txt"
    if ( Test-Path -Path "${PDF_TXT_FILE}" ) {
        Set-Location ./contestgen
        # Note period stuck in front ( ./ -> ../ )
        java ${JVM_LOG4J_LEVEL} ${JVM_LOG4J_CONFIG} -jar "contest-gen-${BALLOTGEN_VERSION}-jar-with-dependencies.jar" ".${PDF_TXT_FILE}" ../contests
        Set-Location ..
    } else {
        Write-Host "$global:PDF_FILE does not exist."
      }



    
    Set-Location ..
} 
# runBallotNamer - run the ballot naming program.  This tests the regexes
#   therein and produces the precontests.txt file. There should be just
#   one pdf/txt file in ./output folder, and each will be renamed.
function runBallotNamer {
    Write-Host "Renaming municipal PDF & TXT files."
    Set-Location ./ballotnamer
    java ${JVM_LOG4J_LEVEL} ${JVM_LOG4J_CONFIG} -jar "ballot-namer-${BALLOTGEN_VERSION}-jar-with-dependencies.jar" ../output
    Set-Location ..
} 
# runBallotGen - run the ballot generation program.  This tests the regexes
#   therein and produces the DOCX file. There should be just
#   one pdf/txt file in ./output folder and a new docx file.
function runBallotGen {
    Write-Host "Enter name of contests file:"
    $CONTESTS_FILE  = Read-Host
    if ( Test-Path -Path "./contests/${CONTESTS_FILE}" ) {

    } else {
      Write-Host "./contests/${CONTESTS_FILE} does not exist."
      return
    }
    Write-Host "Generating municipal DOCX file."
    Set-Location ./ballotgen
    java ${JVM_LOG4J_LEVEL} ${JVM_LOG4J_CONFIG} -jar "ballot-gen-${BALLOTGEN_VERSION}-jar-with-dependencies.jar" ../output ../contests
    Set-Location ..
}
# clearOutputFolder - remove all files in ./output folder
#   in preparation for running the fullballotgen script
function clearOutputFolder {
    Write-Host "Removing all files in ./output"
    Remove-Item ./output/*
}

# Main logic
# Check that environment variable is set
$BALLOTGEN_VERSION=(Get-Item env:BALLOTGEN_VERSION).value
if ($BALLOTGEN_VERSION) {
    Write-Output "`r`nBALLOTGEN_VERSION: ${BALLOTGEN_VERSION}`r`n"
} else {
    Write-Output "`r`nBALLOTGEN_VERSION environment variable not defined -- Quitting.`r`n"
    Exit 1
}
menu