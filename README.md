ACHE Focused Crawler
============

Download with Conda
---------------------------------------------

You can download `ache` from Binstar [2] with Conda [3] by running:

```bash
  conda install -c memex ache
```
NOTE: we haven't released the current developing version of the crawler, so 'ache' from Binstart is outdated.
If you want to try the developing version, please compile the code using below instruction then start crawler using 'ache' in build/install/ache/bin

Build ACHE
--------------------------------------------
If you want to compile ACHE from source code, use compile_crawler.sh:

        $./script/compile_crawler.sh
  
  
Build a model for ACHE's page classifier. If you already have the model, skip this step.
--------------------------------------------
To focus on a certain topic ACHE needs to have access to a model of its content. This model is then 
used by a classifier to decide, given a new crawled page, whether it is on-topic or not. Assume that you store positive and negative examples in two directories, `positive` and `negative`. Also, assume these directories are placed in the `training_data` directory. Here is how you build a model from these examples:
    
        $./script/build_model.sh <training data path> <output path>

`<training data path>` is the path to the directory containing positive and negative examples.

`<output path>` is the new directory that you want to save the generated model that consists of two files: `pageclassifier.model` and `pageclassifier.features`. 
  

Start ACHE
--------------------------------------------
After you generate a model, you need to prepare the seed file, where each line is a URL. To start the crawler, run:

        $./build/install/bin/ache startCrawl -o <data output path> -c <config path> -s <seed path> -m <model path> -l <lang detect profile path>


`<configuration path>` is the path to the config directory.

`<seed path>` is the seed file.

`<model path>` is the path to the model directory (containing pageclassifier.model and pageclassifier.features).

`<data output path>` is the path to the data output directory.

 `<lang detect profile path>` is the path to the language detection profile: "libs/langdetect-03-03-2014.jar"
 
 Example of running ACHE:
 
       $./build/install/bin/ache startCrawl -o output -c config/sample_config -s config/sample.seeds -m config/sample_model -l libs/langdetect-03-03-2014.jar
        

More information?
-----------------------------------------------
[ACHE Wiki](https://github.com/ViDA-NYU/ache/wiki)

What is inside the data output directory?
-----------------------------------------------
`data_target` contains relevant pages.

`data_negative` contains irrelevant pages. In default setting, the crawler does not save the irrelevant pages.

`data_monitor` contains current status of the crawler.

`data_url` and `data_backlinks` are where persistent storages keep information of frontier and crawled graph.

When to stop the crawler?
----------------------------------------------
Unless you stop it, the crawler exists when the number of crawled pages exeeds the limit in the setting, which is 9M at default. You can look at this file `data_monitor/harvestinfo.csv` to know how many pages has been downloaded to decide whether you want to stop the crawler. The 1st, 2nd, 3rd columns are number of relevant pages, number of visited pages, timestamp.

Where to report bugs?
---------------------------------------------

We welcome user feedback. Please submit any suggestions or bug reports using the Github tracker (https://github.com/ViDA-NYU/ache/issues)



