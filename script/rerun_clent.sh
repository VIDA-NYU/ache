while true
do
  x=$(ps aux | grep CrawlerManager | grep kienpham | wc -l)
  if [ $x != 7 ]
  then
    sh script/run_client.sh
    echo "rerun"
  fi
  sleep 5m
done
