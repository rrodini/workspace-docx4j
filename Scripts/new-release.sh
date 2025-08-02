#!/bin/bash
# release.sh creates the folders and moves files for a new release of SampleBallotGen. 
# A new release creates a new DEV environment, e.g. SampleBallotGen-1.6.0
# - Build BallotUtils, ZoneProcessor, ContestGen, etc. with new version
# - Set env variable BALLOTGEN_VERSION to new value, e.g. "1.6.0"
# - Update the OLD_BALLOTGEN_VERSION variable
# - Run this script. Script runs in "Sample Ballot Production" folder.

if [ -n BALLOTGEN_VERSION ]; then
  echo -e "\nBALLOTGEN_VERSION: ${BALLOTGEN_VERSION}\n"
else 
  echo -e "\nBALLOTGEN_VERSION environment variable not defined -- Quitting.\n"
  exit 0
fi

OLD_BALLOTGEN_VERSION="1.6.0"
PARENT_FOLDER=/Users/robert/Documents/"Sample Ballot Production"
RELEASE_FOLDER=SampleBallotGen-${BALLOTGEN_VERSION}
CONTESTGEN_FOLDER=../SampleBallotGen-${OLD_BALLOTGEN_VERSION}/contestgen
BALLOTGEN_FOLDER=../SampleBallotGen-${OLD_BALLOTGEN_VERSION}/ballotgen
BALLOTNAMER_FOLDER=../SampleBallotGen-${OLD_BALLOTGEN_VERSION}/ballotnamer
BALLOTZIPPER_FOLDER=../SampleBallotGen-${OLD_BALLOTGEN_VERSION}/ballotzipper
PDFBOX_FOLDER=/Users/robert/Documents/"Sample Ballot Production"/PDFBox
SCRIPTS_FOLDER=../SampleBallotGen-${OLD_BALLOTGEN_VERSION}


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
# /chester-contests
# /chester-input
# /chester-output
# /chester-prep
# /chester-post
# /chester-zip
# /contestgen
#  /resources
# /logs
# /PDFBOX
  
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

# echo "Creating Bucks Co. folders"
# mkdir bucks-contests
# mkdir bucks-input
# mkdir bucks-output
# mkdir bucks-prep
# mkdir bucks-zip

echo "Creating Chester Co. folders"
mkdir chester-contests
mkdir chester-input
mkdir chester-output
mkdir chester-prep
mkdir chester-post
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

echo "folder structure creating...Now moving files..."
# Now in new release folder
echo "copying contestgen resources"

# no need to copy the old binary
#cp "${CONTESTGEN_FOLDER}/contest-gen-${OLD_BALLOTGEN_VERSION}-jar-with-dependencies.jar" contestgen/
cp -a "${CONTESTGEN_FOLDER}"/resources/. contestgen/resources/


echo "copying ballotgen resources"
# no need to copy the old binary
cp "${BALLOTGEN_FOLDER}/ballot-gen-${OLD_BALLOTGEN_VERSION}-jar-with-dependencies.jar" ballotgen/
cp -a "${BALLOTGEN_FOLDER}"/resources/. ballotgen/resources/

echo "copying ballotnamer resources"
# no need to copy the old binary
#cp "${BALLOTNAMER_FOLDER}/ballot-namer-${OLD_BALLOTGEN_VERSION}-jar-with-dependencies.jar" ballotnamer/
cp -a "${BALLOTNAMER_FOLDER}"/resources/. ballotnamer/resources/

echo "copying ballotzipper resources"
# no need to copy the old binary
#cp "${BALLOTZIPPER_FOLDER}/ballot-zipper-${OLD_BALLOTGEN_VERSION}-jar-with-dependencies.jar" ballotzipper/
cp -a "${BALLOTZIPPER_FOLDER}"/resources/. ballotzipper/resources/

echo "copying PDFBox files and resources"
cp "${PDFBOX_FOLDER}/pdfbox-app-3.0.2.jar" PDFBOX/

echo "copying script files"
cp -a "${SCRIPTS_FOLDER}"/. .

cd ..
echo "DONE."

