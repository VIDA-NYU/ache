#!/bin/sh
if [ $# -eq 3 ]
then
    CONFIG_PATH=$1
    SEED_PATH=$2
    DATA_PATH=$3
else
    #default parameters
    CONFIG_PATH='conf/conf_default'
    SEED_PATH='conf/seeds/ht.seeds'
    DATA_PATH='./data'
fi

java -cp class:libs/je-3.3.75.jar focusedCrawler.link.frontier.AddSeeds $CONFIG_PATH $SEED_PATH $DATA_PATH
