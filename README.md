MemexCrawler
============
Collaboration between NYU and Continnum Analytics in Memex Project

About ACHE
--------------------------------------------
The focused crawler in this package, ACHE (Adaptative Crawler for Hidden-Web Entries), was created by Luciano Barbosa and Juliana Freire. 
It uses the contents of pages to focus the crawl on a topic and, additionally, is able to learn link patterns that indicate which links are more promising to follow [Barbosa and Freire; WWW 2007].

Build ACHE
--------------------------------------------
If you want to compile ACHE from source code, use compile_crawler.sh:

        $./script/compile_crawler.sh
  
  
Build a model for ACHE's page classifier
--------------------------------------------
To focus on a certain topic ACHE needs to have accddess to a model of its content. This model is then 
used by a classifier to decide, given a new crawled page, whether it is on-topic or not. Assume that you store positive and negative examples in two directories postive and negative respectively. Also, these directories are placed in training_data directory. Here is how you build a model from these examples:
    
        $./script/build_model.sh training_data conf/models/new_model

- **First parameter** is path to the directory containing positive and negative examples.
- **Second parameter** is the new directory that you want to save the generated model. We recommend to save the models in conf directory for easy management.
  
This script will generate two files in conf/models/new_model: **pageclassifier.model** and **pageclassifier.features**.

Start ACHE
--------------------------------------------
After you generated a model and saved it to conf/models/new_model. You need to prepare the seed list for the crawler. We recommend to save the seed list in conf/seeds for easy management. Assume that you have a seed file: conf/seeds/topic.seeds, in which each url is placed in one line. Here is an example of seed file:
  
        http://www.seed1.com
        http://www.seed2.com
        http://www.seed3.com
  
Now you are ready to run the crawler:

        $./script/start_crawler.sh crawler_name conf/conf_default conf/seeds/topics.seeds conf/models/new_model/ data

- **First parameter** is a string to name the crawler.
- **Second parameter** is path to the config directory.
- **Third parameter** is the seed file.
- **Fourth parameter** is path to model directory.
- **Fifth parameter** is path to data output directory.

Stop ACHE
--------------------------------------------
If you want to stop ACHE's execution, just type:

        $./script/stop_crawler.sh crawler_name

Monitor ACHE's execution
---------------------------
To analyse ACHE's execution, you can either check its logs or the data that is being crawled. 
The log files are **log/crawler.log**, **log/link_storage.log**, and **log/target_storage.log**. Some exceptions in 
log files are ok, as long as they're not in the link_storage file. Also in the logs, PROB indicates the
probability that a certain page is on-topic. 
The data that is being crawled is under the directory named data. In particular, one can check the HTML 
pages under **data/data_target**.
Besides log files, crawler's status is also updated in runtime at **data/data_monitor**. This information
could be used to visualize the crawler's status while it is running.
