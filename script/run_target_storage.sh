#!/bin/sh
if [ $# -eq 4 ]
then
    CONFIG_PATH=$1
    MODEL_PATH=$2
    DATA_PATH=$3
    CRAWLER_NAME=$4
else
    #default parameters
    CONFIG_PATH='conf/conf_default'
    MODEL_PATH='conf/models/ht'
    DATA_PATH='./data'
    CRAWLER_NAME="default-achecrawler"
fi
java -Xmx32g -cp "class/:libs/weka-3.6.2.jar:libs/langdetect-03-03-2014.jar:libs/xerces-2.9.1.jar:libs/nekohtml-0.9.5.jar:libs/jsonic-1.2.0.jar:libs/boilerpipe-1.2.0.jar" focusedCrawler.target.TargetStorage $CONFIG_PATH $MODEL_PATH $DATA_PATH $CRAWLER_NAME > log/target_storage.log 2>&1 &
