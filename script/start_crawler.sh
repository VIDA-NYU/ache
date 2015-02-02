#!/bin/sh

mkdir -p log
if [ $# -eq 5 ]
then
    CRAWLER_NAME=$1'-achecrawler'
    CONFIG_PATH=$2
    SEED_FILE=$3
    MODEL_PATH=$4
    DATA_PATH=$5
else
    #sample parameters
    CRAWLER_NAME='sample-achecrawler'
    CONFIG_PATH='config/sample_config'
    SEED_FILE='config/sample.seeds'
    MODEL_PATH='config/sample_model'
    DATA_PATH='./data'
fi

if [ $# -eq 1 ]
then
    CRAWLER_NAME=$1
fi

echo 'Starting crawler '$1
sh script/add_seeds.sh $DATA_PATH $CONFIG_PATH $SEED_FILE
sh script/run_link_storage.sh $CONFIG_PATH $SEED_FILE $DATA_PATH $CRAWLER_NAME
sh script/run_target_storage.sh $CONFIG_PATH $MODEL_PATH $DATA_PATH $CRAWLER_NAME
sh script/run_client.sh $CONFIG_PATH $CRAWLER_NAME
sh script/run_client.sh $CONFIG_PATH $CRAWLER_NAME
sh script/run_client.sh $CONFIG_PATH $CRAWLER_NAME
sh script/run_client.sh $CONFIG_PATH $CRAWLER_NAME
