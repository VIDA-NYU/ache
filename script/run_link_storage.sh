#!/bin/sh
mkdir -p log
if [ $# -eq 4 ]
then
    CONFIG_PATH=$1
    SEED_FILE=$2
    DATA_PATH=$3
    CRAWLER_NAME=$4
else
    #sample parameters
    CONFIG_PATH='config/sample_config'
    SEED_FILE='config/sample.seeds'
    DATA_PATH='./data'
    CRAWLER_NAME='sample-achecrawler'
fi

./build/install/ache/bin/ache startLinkStorage $DATA_PATH $CONFIG_PATH $SEED_FILE > log/link_storage.log 2>&1 &
