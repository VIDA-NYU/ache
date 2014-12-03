mkdir -p log
sh script/clean_data.sh .
sh script/insert_seeds.sh
sh script/run_link_storage.sh
sh script/run_target_storage.sh conf/ conf/models/ht
sh script/run_client.sh
sh script/run_client.sh
sh script/run_client.sh
sh script/run_client.sh
