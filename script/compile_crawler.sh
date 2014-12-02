mkdir -p class
find ./src/ -name "*.java" > sources.txt
javac -d class -encoding ISO-8859-1 -cp .:libs/guava-18.0.jar:libs/jsonic-1.2.0.jar:libs/xerces-2.9.1.jar:libs/langdetect.jar:libs/juniversalchardet-1.0.3.jar:libs/org.w3c.css.sac-1.3.0.jar:libs/htmlunit-2.8.jar:libs/commons-codec-1.9.jar:libs/juniversalchardet-1.0.3.jar:libs/sac-1.3.jar:libs/htmlunit-2.8.jar:libs/xml-apis.jar:libs/xmlParserAPIs.jar:libs/lucene-core-3.5.0.jar:libs/weka.jar:libs/je-3.3.75.jar:libs/nekohtml.jar:libs/xercesImpl.jar:libs/boilerpipe-1.2.0.jar @sources.txt
rm -rf sources.txt
