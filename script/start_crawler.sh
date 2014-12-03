mkdir -p log
if [ $# -eq 3 ]
then
    CONFIG_PATH=$1
    SEED_PATH=$2
    MODEL_PATH=$3
else
    CONFIG_PATH='conf/'
    SEED_PATH='conf/seeds/ht.seeds'
    MODEL_PATH='conf/models/ht'
fi
sh script/clean_data.sh .
sh script/insert_seeds.sh $CONFIG_PATH $SEED_PATH
sh script/run_link_storage.sh $CONFIG_PATH $SEED_PATH
sh script/run_target_storage.sh $CONFIG_PATH $MODEL_PATH
sh script/run_client.sh 
sh script/run_client.sh
sh script/run_client.sh
sh script/run_client.sh
