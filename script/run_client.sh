#!/bin/sh
if [ $# -eq 2 ]
then
    CONFIG_PATH=$1
    CRAWLER_NAME=$2
else
    #default parameters
    CONFIG_PATH='conf/conf-default'
    CRAWLER_NAME="default-achecrawler"
fi

java -Xmx1g -cp class focusedCrawler.crawler.CrawlerManager $CONFIG_PATH $CRAWLER_NAME > log/crawler.log 2>&1 &
