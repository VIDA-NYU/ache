echo "Shut down the crawler "$1" ...."
kill $(ps aux | grep Crawler | grep -v "grep" | awk '{print $2}')
