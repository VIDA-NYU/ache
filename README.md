ACHE Focused Crawler
============

Download with Conda
---------------------------------------------

You can download `ache` from Binstar [2] with Conda [3] by running:

```bash
  conda install -c memex ache
```

Build ACHE
--------------------------------------------
If you want to compile ACHE from source code, use compile_crawler.sh:

        $./script/compile_crawler.sh
  
  
Build a model for ACHE's page classifier
--------------------------------------------
To focus on a certain topic ACHE needs to have accddess to a model of its content. This model is then 
used by a classifier to decide, given a new crawled page, whether it is on-topic or not. Assume that you store positive and negative examples in two directories postive and negative respectively. Also, these directories are placed in training_data directory. Here is how you build a model from these examples:
    
        $./script/build_model.sh <training data path> <output path>

    `<training data path>` is path to the directory containing positive and negative examples.
    `<output path>` is the new directory that you want to save the generated model that consists of two files: `pageclassifier.model` and `pageclassifier.features`. 
  

Start ACHE
--------------------------------------------
After you generated a model, you need to prepare the seed file, that each line is an url. To start the crawler, run:

        $./script/start_crawler.sh <crawler name> <configuration path> <seed file> <model path> <data output path>

    `<crawler name>` is a string to name the crawler.
    `<configuration path>` is path to the config directory.
    `<seed path>` is the path to the seed list file.
    `<model path>` is the path to the model directory (containing pageclassifier.model and pageclassifier.features).
    `<data output path>` is path to data output directory.

Stop ACHE
--------------------------------------------
If you want to stop ACHE's execution, run:

        $./script/stop_crawler.sh crawler_name
        

Monitor ACHE's execution
---------------------------
To analyse ACHE's execution, you can either check its logs or the data that is being crawled. 
The log files are **log/crawler.log**, **log/link_storage.log**, and **log/target_storage.log**. 
Additionally, crawler's status is also updated in runtime at **data/data_monitor**. This information
could be used to visualize the crawler's status while it is running.
