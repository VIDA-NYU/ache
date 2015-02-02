#!/bin/sh
mkdir -p log
if [ $# -eq 4 ]
then
    CONFIG_PATH=$1
    MODEL_PATH=$2
    DATA_PATH=$3
    CRAWLER_NAME=$4
else
    #sample parameters
    CONFIG_PATH='config/sample_config'
    MODEL_PATH='config/sample_model'
    DATA_PATH='./data'
    CRAWLER_NAME="sample-achecrawler"
fi

./build/install/ache/bin/ache startTargetStorage $DATA_PATH $CONFIG_PATH $MODEL_PATH libs/profiles/ > log/target_storage.log 2>&1 &
