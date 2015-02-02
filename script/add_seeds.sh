#!/bin/sh
if [ $# -eq 3 ]
then
    DATA_PATH=$1
    CONFIG_PATH=$2
    SEED_FILE=$3
else
    #default parameters
    DATA_PATH='./data'
    CONFIG_PATH='config/sample_config'
    SEED_FILE='config/sample.seeds'
fi

./build/install/ache/bin/ache addSeeds $DATA_PATH $CONFIG_PATH $SEED_FILE
