if [ $# -eq 1 ]
then
    CRAWLER_NAME=$1
else
    #default parameters
    CRAWLER_NAME='default-achecrawler'
fi
echo "Shut down the crawler "$1" ...."
kill $(ps aux | grep $CRAWLER_NAME | grep -v "grep" | awk '{print $2}')
