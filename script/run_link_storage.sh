#!/bin/sh
cd .;
if [ $# -eq 2 ]
then
    CONFIG_PATH=$1
    SEED_PATH=$2
else
    #default parameters
    CONFIG_PATH='conf/'
    SEED_PATH='conf/seeds/ht.seeds'
fi
java  -Xmx32g -cp "libs/guava-18.0.jar:libs/je-3.3.75.jar:libs/lucene2.4.0.jar:libs/xercesImpl.jar:libs/xmlParserAPIs.jar:libs/xml-apis.jar:libs/nekohtml.jar:libs/weka.jar:class" focusedCrawler.link.LinkStorage $CONFIG_PATH $SEED_PATH > log/link_storage.log 2>&1 &
