DIRECTORY=$1
mkdir -p model
java -cp "class/:libs/weka.jar" focusedCrawler.target.CreateWekaInput conf/target_storage/target_storage.cfg ${DIRECTORY} ${DIRECTORY}/weka.arff
java -cp libs/weka.jar weka.classifiers.functions.SMO -M -d ${DIRECTORY}/weka.model -t ${DIRECTORY}/weka.arff
echo -n "ATTRIBUTES " > ${DIRECTORY}/features
cat ${DIRECTORY}/weka.arff | awk '{FS=" "; if (($1=="@ATTRIBUTE") && ($3=="REAL")) {print $2}}' | sed ':a;N;$!ba;s/\n/ /g' >> ${DIRECTORY}/features
