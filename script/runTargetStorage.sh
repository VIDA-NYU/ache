#!/bin/sh
cd .;
java -Xmx1g -cp "class/:libs/weka.jar:libs/langdetect.jar:libs/xerces-2.9.1.jar:libs/nekohtml.jar:libs/jsonic-1.2.0.jar:libs/boilerpipe-1.2.0.jar" focusedCrawler.target.TargetStorage conf/target_storage/target_storage.cfg > log/target_storage.log 2>&1 &
