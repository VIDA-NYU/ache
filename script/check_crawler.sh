
while true
do
  x=$(ps aux | grep CrawlerManager | wc -l)
  if [ $x -eq 1 ]
  then
    sh script/runCrawler.sh
    sh script/runCrawler.sh
    echo "rerun"
  fi
  sleep 5m
done

