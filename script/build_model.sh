#!/bin/bash
DIRECTORY=$1 #Directory that contain training examples. it should have two subdirectories: positive and negative
OUTPUT=$2 #output directory
mkdir -p $OUTPUT
./build/install/ache/bin/ache buildModel $DIERECTORY $OUTPUT
echo "CLASS_VALUES  S NS" > ${OUTPUT}/pageclassifier.features
echo -n "ATTRIBUTES " >> ${OUTPUT}/pageclassifier.features
cat ${DIRECTORY}/weka.arff | awk '{FS=" "; if (($1=="@ATTRIBUTE") && ($3=="REAL")) {print $2}}' | sed -e ':a' -e 'N' -e '$!ba' -e 's/\n/ /g' >> ${OUTPUT}/pageclassifier.features
