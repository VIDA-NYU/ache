echo "Shut down the crawler..."
kill $(ps aux | grep Crawler | grep -v "grep" | awk '{print $2}')
