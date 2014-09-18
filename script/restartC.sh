#!/bin/sh

echo "killing crawlers...";
for a in `ps awwux | grep CrawlerManager | grep -v "grep" | grep -v perp | awk '{print $2}'`; do kill -9 $a; sh /home/lbarbosa/news_crawler/focused_crawler/script/runCrawler.sh; done;

