#!/bin/bash
# release.sh creates the folders and moves files
# for a full release of SampleBallotGen.

if [ -n BALLOTGEN_VERSION ]; then
  echo -e "\nBALLOTGEN_VERSION: ${BALLOTGEN_VERSION}\n"
else 
  echo -e "\nBALLOTGEN_VERSION environment variable not defined -- Quitting.\n"
  exit 0
fi


PARENT_FOLDER=/Users/robert/Documents/"Sample Ballot Production"
CONTESTGEN_FOLDER=/Users/robertgit/workspace-docx4j/ContestGen
BALLOTGEN_FOLDER=/Users/robert/git/workspace-docx4j/BallotGen
BALLOTNAMER_FOLDER=/Users/robert/git/workspace-docx4j/BallotNamer
PDFBOX_FOLDER=/Users/robert/Documents/"Sample Ballot Production"/PDFBOX
SAMPLES_FOLDER=/Users/robert/Documents/"Sample Ballot Production"/samples
SCRIPTS_FOLDER=/Users/robert/Documents/"Sample Ballot Production"/DOCX4J/workspace-docx4j/scripts


# do all work in the parent folder
cd "${PARENT_FOLDER}"

# don't clobber an existing folder
RELEASE_FOLDER="SampleBallotGen-${BALLOTGEN_VERSION}"
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
# /contestgen
#  /resources
# /contests
# /logs
# /output
# /PDFBOX
# /samples
  
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

echo "Creating contests folder"
mkdir contests

echo "Creating logs folder"
mkdir logs

echo "Creating output folder"
mkdir output

echo "Creating PDFBOX folder"
mkdir PDFBOX

echo "Creating samples folder"
mkdir samples

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

echo "copying PDFBox files and resources"
cp "${PDFBOX_FOLDER}/pdfbox-app-2.0.25.jar" PDFBOX/

echo "copying samples files"
cp -a "${SAMPLES_FOLDER}"/. samples/

echo "copying script files"
cp -a "${SCRIPTS_FOLDER}"/. .

cd ..
#echo "zipping files in {$RELEASE_FOLDER}"
#zip -r "${RELEASE_FOLDER}.zip" "${RELEASE_FOLDER}"

