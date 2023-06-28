#!/bin/bash
# prod-release.sh creates the folders and moves files for a PROD release of SampleBallotGen. 
#
# Script must be run in "Sample Ballot Production" folder.

if [ -n BALLOTGEN_VERSION ]; then
  echo -e "\nBALLOTGEN_VERSION: ${BALLOTGEN_VERSION}\n"
else 
  echo -e "\nBALLOTGEN_VERSION environment variable not defined -- Quitting.\n"
  exit 0
fi


PARENT_FOLDER=~/Library/CloudStorage/GoogleDrive-rrodini@chescodems.org/"My Drive"/
RELEASE_FOLDER="SampleBallotGen-${BALLOTGEN_VERSION}"
DEV_RELEASE_FOLDER=~/Documents/"Sample Ballot Production/SampleBallotGen-${BALLOTGEN_VERSION}"
CONTESTGEN_FOLDER=${DEV_RELEASE_FOLDER}/contestgen
BALLOTGEN_FOLDER=${DEV_RELEASE_FOLDER}/ballotgen
BALLOTNAMER_FOLDER=${DEV_RELEASE_FOLDER}/ballotnamer
BALLOTZIPPER_FOLDER=${DEV_RELEASE_FOLDER}/ballotzipper
TABREPLACER_FOLDER=${DEV_RELEASE_FOLDER}/tabreplacer
PDFBOX_FOLDER=/Users/robert/Documents/"Sample Ballot Production"/PDFBox

# do all work in the parent folder
cd "${PARENT_FOLDER}"

# don't clobber an existing folder
# echo "Generating PROD release for ${RELEASE_FOLDER}"
# if [ -d ${RELEASE_FOLDER} ]
# then
#   echo "${RELEASE_FOLDER} exits, So quitting."; exit;
# fi

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
# /bucks-contests
# /bucks-input
# /bucks-output
# /bucks-zip
# /chester-contests
# /chester-input
# /chester-output
# /chester-zip
# /contestgen
#  /resources
# /logs
# /PDFBOX
# /tabreplacer
#  /resources
  
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

echo "Creating Bucks Co. folders"
mkdir bucks-contests
mkdir bucks-input
mkdir bucks-output
mkdir bucks-zip

echo "Creating Chester Co. folders"
mkdir chester-contests
mkdir chester-input
mkdir chester-output
mkdir chester-zip

echo "Creating contestgen folder"
mkdir contestgen
cd contestgen

echo "Creating contestgen/resources folder"
mkdir resources
cd ..

echo "Creating logs folder"
mkdir logs

echo "Creating PDFBOX folder"
mkdir PDFBOX

echo "Creating tabreplacer folder"
mkdir tabreplacer
cd tabreplacer

echo "Creating tabreplacer/resources folder"
mkdir resources
cd ..
 
echo "folder structure creating...Now moving files..."

echo "copying contestgen files and resources"

cp "${CONTESTGEN_FOLDER}/contest-gen-${BALLOTGEN_VERSION}-jar-with-dependencies.jar" contestgen/
cp -a "${CONTESTGEN_FOLDER}"/resources/. contestgen/resources/

echo "copying ballotgen files and resources"
cp "${BALLOTGEN_FOLDER}/ballot-gen-${BALLOTGEN_VERSION}-jar-with-dependencies.jar" ballotgen/
cp -a "${BALLOTGEN_FOLDER}"/resources/. ballotgen/resources/

echo "copying ballotnamer files and resources"
cp "${BALLOTNAMER_FOLDER}/ballot-namer-${BALLOTGEN_VERSION}-jar-with-dependencies.jar" ballotnamer/
cp -a "${BALLOTNAMER_FOLDER}"/resources/. ballotnamer/resources/

echo "copying ballotzipper files and resources"
cp "${BALLOTZIPPER_FOLDER}/ballot-zipper-${BALLOTGEN_VERSION}-jar-with-dependencies.jar" ballotzipper/
cp -a "${BALLOTZIPPER_FOLDER}"/resources/. ballotzipper/resources/

echo "copying tabreplacer files and resources"
cp "${TABREPLACER_FOLDER}/tab-replacer-${BALLOTGEN_VERSION}-jar-with-dependencies.jar" ballotzipper/
cp -a "${TABREPLACER_FOLDER}"/resources/. tabreplacer/resources/

echo "copying PDFBox files and resources"
cp "${PDFBOX_FOLDER}/pdfbox-app-2.0.25.jar" PDFBOX/

echo "copying scripts and other files"
cp -a "${DEV_RELEASE_FOLDER}"/*.sh .
cp -a "${DEV_RELEASE_FOLDER}"/*.ps1 .

cd ..
echo "DONE."


