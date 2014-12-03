DIRECTORY=$1 #Directory that contain training examples. it should have two subdirectories: positive and negative
OUTPUT=$2 #output directory
java -cp "class/:libs/weka.jar" focusedCrawler.target.CreateWekaInput conf/target_storage/target_storage.cfg ${DIRECTORY} ${DIRECTORY}/weka.arff
java -cp libs/weka.jar weka.classifiers.functions.SMO -M -d ${OUTPUT}/pageclassifier.model -t ${DIRECTORY}/weka.arff
echo "CLASS_VALUES  S NS\n" > ${OUTPUT}/pageclassifier.features
echo -n "ATTRIBUTES " >> ${OUTPUT}/pageclassifier.features
cat ${DIRECTORY}/weka.arff | awk '{FS=" "; if (($1=="@ATTRIBUTE") && ($3=="REAL")) {print $2}}' | sed ':a;N;$!ba;s/\n/ /g' >> ${OUTPUT}/pageclassifier.features
