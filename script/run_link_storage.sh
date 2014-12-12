#!/bin/sh
if [ $# -eq 4 ]
then
    CONFIG_PATH=$1
    SEED_PATH=$2
    DATA_PATH=$3
    CRAWLER_NAME=$4
else
    #default parameters
    CONFIG_PATH='conf/conf_default'
    SEED_PATH='conf/seeds/ht.seeds'
    DATA_PATH='./data'
    CRAWLER_NAME='defaut-achecrawler'
fi
java  -Xmx32g -cp "libs/guava-18.0.jar:libs/je-3.3.75.jar:libs/lucene2.4.0.jar:libs/xercesImpl-2.5.0.jar:libs/nekohtml-0.9.5.jar:libs/weka-3.6.2.jar:class" focusedCrawler.link.LinkStorage $CONFIG_PATH $SEED_PATH $DATA_PATH $CRAWLER_NAME > log/link_storage.log 2>&1 &
