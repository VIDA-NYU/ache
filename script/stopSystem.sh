kill -9 `ps awwux | grep CrawlerManager | grep -v perp | grep -v "grep" | awk '{print $2}' | xargs`
kill -9 `ps awwux | grep Storage | grep -v perp | grep -v "grep" | awk '{print $2}' | xargs`

sleep 10

mv /n/ferric_srini/LargeLM/news_crawler/data/data_target /n/ferric_srini/LargeLM/news_crawler/data_`date +"%m%d%y"`
rm -r /n/ferric_srini/LargeLM/news_crawler/data
mkdir /n/ferric_srini/LargeLM/news_crawler/data 
sh /home/lbarbosa/news_crawler/focused_crawler/script/createLM.sh

rm -r /n/ferric_srini/LargeLM/news_crawler/data_`date +"%m%d%y"`

sleep 10

killall -9 java
