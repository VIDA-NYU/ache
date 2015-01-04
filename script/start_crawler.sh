#!/bin/sh

mkdir -p log
if [ $# -eq 5 ]
then
    CRAWLER_NAME=$1'-achecrawler'
    CONFIG_PATH=$2
    SEED_PATH=$3
    MODEL_PATH=$4
    DATA_PATH=$5
else
    #default parameters
    CRAWLER_NAME='default-achecrawler'
    CONFIG_PATH='conf/conf_default'
    SEED_PATH='conf/seeds/ht.seeds'
    MODEL_PATH='conf/models/ht'
    DATA_PATH='./data'
fi

if [ $# -eq 1 ]
then
    CRAWLER_NAME=$1
fi

echo 'Starting crawler '$1
sh script/clean_data.sh $DATA_PATH
sh script/insert_seeds.sh $CONFIG_PATH $SEED_PATH $DATA_PATH
sh script/run_link_storage.sh $CONFIG_PATH $SEED_PATH $DATA_PATH $CRAWLER_NAME
sh script/run_target_storage.sh $CONFIG_PATH $MODEL_PATH $DATA_PATH $CRAWLER_NAME
sh script/run_client.sh $CONFIG_PATH $CRAWLER_NAME
sh script/run_client.sh $CONFIG_PATH $CRAWLER_NAME
sh script/run_client.sh $CONFIG_PATH $CRAWLER_NAME
sh script/run_client.sh $CONFIG_PATH $CRAWLER_NAME
