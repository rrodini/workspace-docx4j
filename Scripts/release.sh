#!/bin/bash
# release.sh creates the folders and moves files for a full release of SampleBallotGen. 
#
# Script must be run in "Sample Ballot Production" folder.

if [ -n BALLOTGEN_VERSION ]; then
  echo -e "\nBALLOTGEN_VERSION: ${BALLOTGEN_VERSION}\n"
else 
  echo -e "\nBALLOTGEN_VERSION environment variable not defined -- Quitting.\n"
  exit 0
fi


PARENT_FOLDER=/Users/robert/Documents/"Sample Ballot Production"
RELEASE_FOLDER="SampleBallotGen-${BALLOTGEN_VERSION}"
CONTESTGEN_FOLDER=/Users/robert/git/workspace-docx4j/ContestGen
BALLOTGEN_FOLDER=/Users/robert/git/workspace-docx4j/BallotGen
BALLOTNAMER_FOLDER=/Users/robert/git/workspace-docx4j/BallotNamer
BALLOTZIPPER_FOLDER=/Users/robert/Documents/"Sample Ballot Production"/DOCX4J/workspace-docx4j/BallotZipper
PDFBOX_FOLDER=/Users/robert/Documents/"Sample Ballot Production"/PDFBox
SCRIPTS_FOLDER=/Users/robert/git/workspace-docx4j/Scripts
SPECIMENS_FOLDER=/Users/robert/Documents/"Sample Ballot Production"/Specimens


# do all work in the parent folder
cd "${PARENT_FOLDER}"

# don't clobber an existing folder
echo "Generating release for ${RELEASE_FOLDER}"
if [ -d ${RELEASE_FOLDER} ]
then
  echo "${RELEASE_FOLDER} exits, So quitting."; exit;
fi

# Create release folder
echo "Creating ${RELEASE_FOLDER}"
mkdir "${RELEASE_FOLDER}"
cd "${RELEASE_FOLDER}"
# sub-folder structure
# /ballotgen
#   /resources
# /ballotnamer
#   /resources
# /ballotzipper
#   /resources
# /contestgen
#  /resources
# /contests
# /logs
# /output
# /PDFBOX
# /specimens
# /zip
  
echo "Creating contestgen folder"
mkdir contestgen
cd contestgen

echo "Creating contestgen/resources folder"
mkdir resources
cd ..

echo "Creating ballotgen folder"
mkdir ballotgen
cd ballotgen

echo "Creating ballotgen/resources folder"
mkdir resources
cd ..

echo "Creating ballotnamer folder"
mkdir ballotnamer
cd ballotnamer

echo "Creating ballotnamer/resources folder"
mkdir resources
cd ..

echo "Creating ballotzipper"
mkdir ballotzipper
cd ballotzipper

echo "Creating ballotzipper/resources folder"
mkdir resources
cd ..

echo "Creating contests folder"
mkdir contests


echo "Creating logs folder"
mkdir logs

echo "Creating output folder"
mkdir output

echo "Creating PDFBOX folder"
mkdir PDFBOX

echo "Creating specimens folder"
mkdir specimens

echo "Creating zip folder"
mkdir zip

echo "folder structure creating...Now moving files..."

echo "copying contestgen files and resources"

cp "${CONTESTGEN_FOLDER}/target/contest-gen-${BALLOTGEN_VERSION}-jar-with-dependencies.jar" contestgen/
cp -a "${CONTESTGEN_FOLDER}"/resources/. contestgen/resources/

echo "copying ballotgen files and resources"
cp "${BALLOTGEN_FOLDER}/target/ballot-gen-${BALLOTGEN_VERSION}-jar-with-dependencies.jar" ballotgen/
cp -a "${BALLOTGEN_FOLDER}"/resources/. ballotgen/resources/

echo "copying ballotnamer files and resources"
cp "${BALLOTNAMER_FOLDER}/target/ballot-namer-${BALLOTGEN_VERSION}-jar-with-dependencies.jar" ballotnamer/
cp -a "${BALLOTNAMER_FOLDER}"/resources/. ballotnamer/resources/

echo "copying zipper files and resources"
cp "${BALLOTZIPPER_FOLDER}/target/ballot-zipper-${BALLOTGEN_VERSION}-jar-with-dependencies.jar" ballotzipper/
cp "${BALLOTZIPPER_FOLDER}"/precincts-zones.csv ballotzipper/
cp -a "${BALLOTZIPPER_FOLDER}"/resources/. ballotzipper/resources/

echo "copying PDFBox files and resources"
cp "${PDFBOX_FOLDER}/pdfbox-app-2.0.25.jar" PDFBOX/

echo "copying specimens files"
cp -a "${SPECIMENS_FOLDER}"/. specimens/

echo "copying script files"
cp -a "${SCRIPTS_FOLDER}"/. .

cd ..
echo "DONE."

