while true
do
  x=$(ps aux | grep ache | grep kienpham | wc -l) #kienpham should be changed to username of crawler's owner
                                                  #also, we should change the client so that there is no need to rerun it
  if [ $x != 7 ]
  then
    sh script/run_client.sh
    echo "rerun"
  fi
  sleep 5m
done
