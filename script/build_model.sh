DIRECTORY=$1 #Directory that contain training examples. it should have two subdirectories: positive and negative
OUTPUT=$2 #output directory
mkdir -p $OUTPUT
java -cp "class/:libs/weka-stable-3.6.10.jar" focusedCrawler.target.CreateWekaInput conf/conf_default/target_storage/target_storage.cfg ${DIRECTORY} ${DIRECTORY}/weka.arff
java -cp libs/weka-stable-3.6.10.jar weka.classifiers.functions.SMO -M -d ${OUTPUT}/pageclassifier.model -t ${DIRECTORY}/weka.arff
echo "CLASS_VALUES  S NS" > ${OUTPUT}/pageclassifier.features
echo -n "ATTRIBUTES " >> ${OUTPUT}/pageclassifier.features
cat ${DIRECTORY}/weka.arff | awk '{FS=" "; if (($1=="@ATTRIBUTE") && ($3=="REAL")) {print $2}}' | sed -e ':a' -e 'N' -e '$!ba' -e 's/\n/ /g' >> ${OUTPUT}/pageclassifier.features
