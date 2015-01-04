#!/bin/sh

while true
do
  x=$(ps aux | grep CrawlerManager | wc -l)
  if [ $x -eq 1 ]
  then
    sh script/runCrawler.sh
    sh script/runCrawler.sh
    sh script/runCrawler.sh
    echo "restart client"
  fi
  sleep 5m
done

