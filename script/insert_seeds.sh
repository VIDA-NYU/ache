#!/bin/sh
if [ $# -eq 2 ]
then
    CONFIG_PATH=$1
    SEED_PATH=$2
else
    #default parameters
    CONFIG_PATH='conf/'
    SEED_PATH='conf/seeds/ht.seeds'
fi

java -cp class:libs/je-3.3.75.jar focusedCrawler.link.frontier.AddSeeds $CONFIG_PATH $SEED_PATH
