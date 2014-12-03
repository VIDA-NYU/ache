memexcrawler
============
Collaboration between NYU and Continnum Analytics in Memex Project

--------------------------------------------
* About ACHE

  The focused crawler in this package, ACHE (Adaptative Crawler for Hidden-Web Entries), was created by Luciano Barbosa and Juliana Freire. 
  It uses the contents of pages to focus the crawl on a topic and, additionally, is able to learn link patterns that indicate which links are 
  more promising to follow [Barbosa and Freire; WWW 2007].

--------------------------------------------
* Building a Model for ACHE's Page Classifier

To focus on a certain topic, say HIV, ACHE needs to have accddess to a model of its content. This model is then 
used by a classifier to decide, given a new crawled page, whether it is on-topic or not.
Assume that you store positive and negative examples in two directories postive and negative respectively. Also, these directories are placed
training_data directory. Here is how you build a model from these examples:
    
        $./script/build_model.sh training_data conf/models/new_model

- First parameter is path to the directory containing positive and negative examples.
- Second parameter is the new directory that you want to save the generated model. We recommend to save the models in conf directory for easy management.
  
This script will generate two files in conf/models/new_model: pageclassifier.model and pageclassifier.features
--------------------------------------------
* Start ACHE with the an exisiting model
  
After you generated a model and saved it to conf/models/new_model. You need to prepare the seed list for the crawler. We recommend 
to save the seed list in conf/seeds for easy management. Assume that you have a seed file: conf/seeds/topic.seeds. Here is an example of
seed file:
  
    SEEDS http://www.seed1.com http://www.seed2.com
  
Note that all urls are placed in single line. Now you are ready to run the crawler:

        $./script/start_crawler.sh conf/ conf/seeds/topics.seeds conf/models/new_model/

- First parameter is path to the config directory.
- Second parameter is the seed file.
- Third parameter is path to model directory

--------------------------------------------
* Configuring ACHE to Use its Link Classifier

ACHE has a link classifier that, via exploitation and exploration, predicts which links are promising to follow. It operates under 
an online learning fashion. To use ACHE's link classifier:

- Set parameter ONLINE_LEARNING to TRUE in file conf/link_storage/link_storage.cfg
- Set parameter HARD_FOCUS to TRUE in file conf/target_storage/target_storage.cfg

--------------------------------------------
* Compile ACHE

If you want to compile ACHE from source code, use compile_crawler.sh:

    $./script/compile_crawler.sh
  
--------------------------------------------
* Stop ACHE

If you want to stop ACHE's execution, just type:

        $./script/stop_crawler.sh

--------------------------------------------
* Analysing ACHE's Execution

  To analyse ACHE's execution, you can either check its logs or the data that is being crawled. 

  The log files are log/crawler.log, log/link_storage.log, and log/target_storage.log. Some exceptions in 
  log files are ok, as long as they're not in the link_storage file. Also in the logs, PROB indicates the
  probability that a certain page is on-topic. 

  The data that is being crawled is under the directory named data. In particular, one can check the HTML 
  pages under data/data_target.

  Besides log files, crawler's status is also updated in runtime at data/data_monitor. This information
  could be used to visualize the crawler's status while it is running
