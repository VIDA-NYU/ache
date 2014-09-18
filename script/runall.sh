mkdir -p log
sh script/runCleanDirs.sh .
sh script/runInsertLinks.sh
sh script/runLinkStorage.sh
sleep 5
sh script/runTargetStorage.sh
sleep 5
sh script/runCrawler.sh
sh script/runCrawler.sh
sh script/runCrawler.sh
sh script/runCrawler.sh
