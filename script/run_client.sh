#!/bin/sh
mkdir -p log
if [ $# -eq 1 ]
then
    CONFIG_PATH=$1
else
    #sample parameters
    CONFIG_PATH='config/sample_config'
fi

./build/install/ache/bin/ache startCrawlManager $CONFIG_PATH > log/crawler.log 2>&1 &
