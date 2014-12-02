#!/bin/sh
cd .;
java  -Xmx32g -cp "libs/guava-18.0.jar:libs/je-3.3.75.jar:libs/lucene2.4.0.jar:libs/xercesImpl.jar:libs/xmlParserAPIs.jar:libs/xml-apis.jar:libs/nekohtml.jar:libs/weka.jar:class" focusedCrawler.link.LinkStorage conf/link_storage/link_storage.cfg > log/link_storage.log 2>&1 &
