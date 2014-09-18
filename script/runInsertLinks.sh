#!/bin/sh
cd .;

java -cp class:libs/je-3.3.75.jar focusedCrawler.link.frontier.AddSeeds conf/link_storage/link_storage.cfg
