memexcrawler
============
Collaboration between NYU and Continuum in Memex Project

--------------------------------------------
* About ACHE

  The focused crawler in this package, ACHE (Adaptative Crawler for Hidden-Web Entries), was created by Luciano Barbosa and Juliana Freire. 
  It uses the contents of pages to focus the crawl on a topic and, additionally, is able to learn link patterns that indicate which links are 
  more promising to follow [Barbosa and Freire; WWW 2007].

--------------------------------------------
* Building a Model for ACHE's Page Classifier

  To focus on a certain topic, say HIV, ACHE needs to have access to a model of its content. This model is then 
  used by a classifier to decide, given a new crawled page, whether it is on-topic or not. To build a model for HIV, 
  assuming that you are on the focused_crawler directory, create directories HIV, HIV/page_classifier, HIV/page_classifier/positive 
  and HIV/page_classifier/negative. Afterwards, copy positive examples of HTML pages on HIV to HIV/page_classifier/positive, and 
  negative ones to HIV/page_classifier/negative. Assuming you are on a UNIX operational system, you can run the following commands:

  $mkdir -p HIV && mkdir -p HIV/page_classifier && mkdir -p HIV/page_classifier/positive && mkdir -p HIV/page_classifier/negative
  $cp POSITIVE_HTML_PAGES HIV/page_classifier/positive
  $cp NEGATIVE_HTML_PAGES HIV/page_classifier/negative

  Now you can create the model for a page classifier on HIV. ACHE currently uses the implementation of SVM provided by weka. 
  Assuming a UNIX environment again, run:

  $sh script/compile.sh
  $sh script/createWekamodel.sh HIV/page_classifier

  This script will output files weka.arff, weka.model, and features on directory HIV/page_classifier. 

--------------------------------------------
* Configuring ACHE to Use a Page Classifier Model

  Let us assume that you want to run ACHE on HIV, and you have just created a model to it by following the 
  instructions above. Assuming you are on the focused_crawler directory, the first step is to create a directory 
  named HIV/crawler, and create a file inside it containing seeds for the topic HIV, namely HIV/crawler/hiv_seeds.cfg. 
  On a UNIX environment, you can run the following commands:

  $mkdir -p HIV/crawler && touch HIV/crawler/hiv_seeds.cfg

  hiv_seeds.cfg needs to specify hosts and seeds for the topic HIV. An example for variables HOSTS and SEEDS, mandatory 
  in this file, is:

  HOSTS www.aids.gov
  SEEDS en.wikipedia.org/wiki/HIV www.cdc.gov/HIV aidsinfo.nih.gov www.webmd.com/hiv-aids

  Afterwards, copy hiv_seeds.cfg to conf/link_storage. Right now this is redundant and will be fixed later because it involves some 
  refactoring on the code.  After copying it, open file conf/link_storage/link_storage.cfg and replace whatever is indicated as the seeds file 
  -- i.e.  . oldtopic_seeds.cfg -- by . hiv_seeds.cfg

  Create a file HIV/crawler/hiv.cfg with the following content:
    
  STOPLIST_FILES PATH_TO_FOCUSED_CRAWLER/conf/stoplist.txt
  FILE_CLASSIFIER  PATH_TO_FOCUSED_CRAWLER/HIV/page_classifier/weka.model
  CLASS_VALUES  S NS

  and append the content of PATH_TO_FOCUSED_CRAWLER/HIV/page_classifier/features to it


  Finally, copy hiv.cfg to conf/target_storage (this redundancy will also be fixed soon), open file conf/target_storage/target_storage.cfg 
  and replace whatever is indicated as the topic configuration file -- i.e. . oldtopic.cfg -- by . hiv.cfg

  Important: If you want to use ACHE to crawl pages regardless of topic, set USE_CLASSIFIER as FALSE in conf/target_storage/target_storage.cfg 
  and GRAB_LINKS as FALSE in conf/link_storage/link_storage.cfg

  After running ACHE, you can remove hiv_seeds.cfg and hiv.cfg from conf.

--------------------------------------------
* Configuring ACHE to Use its Link Classifier

  ACHE has a link classifier that, via exploitation and exploration, predicts which links are promising to follow. It operates under 
  an online learning fashion. To use ACHE's link classifier:

  - Set parameter ONLINE_LEARNING to TRUE in file conf/link_storage/link_storage.cfg
  - Set parameter HARD_FOCUS to TRUE in file conf/target_storage/target_storage.cfg


--------------------------------------------
* Running ACHE

  There are two ways of running ACHE: single-command and step by step. In both cases, you will have to compile the code. In a UNIX 
  environment, run:

  $sh script/compile.sh

  If you want to run ACHE in the single-command way, type:

  $sh script/runall.sh

  To run it step by step, do:
 
  $sh script/runCleanDirs.sh .
  $sh script/runInsertLinks.sh
  $sh script/runLinkStorage.sh
  $sh script/runTargetStorage.sh
  $sh script/runCrawler.sh

  Important: you need to wait a few seconds after running runLinkStorage.sh before going on.

  When you fire the crawler, it is going to collect all the data it is grabbing inside a directory named focused_crawler/data. You may save 
  this directory inside focused_crawler/hiv/crawler once you have stopped running it.


--------------------------------------------
* Killing ACHE

  If you want to stop ACHE's execution, just type:

  $sh script/killall.sh

--------------------------------------------
* Analysing ACHE's Execution

  To analyse ACHE's execution, you can either check its logs or the data that is being crawled. 

  The log files are log/crawler.log, log/link_storage.log, and log/target_storage.log. Some exceptions in 
  log files are ok, as long as they're not in the link_storage file. Also in the logs, PROB indicates the
  probability that a certain page is on-topic. 

  The data that is being crawled is under the directory named data. In particular, one can check the HTML 
  pages under data/data_target.

