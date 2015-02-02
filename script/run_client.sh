#!/bin/sh
mkdir -p log
if [ $# -eq 2 ]
then
    CONFIG_PATH=$1
    CRAWLER_NAME=$2
else
    #sample parameters
    CONFIG_PATH='config/sample_config'
    CRAWLER_NAME="sample-achecrawler"
fi

./build/install/ache/bin/ache startCrawlManager $CONFIG_PATH > log/crawler.log 2>&1 &
