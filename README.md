[![Build Status](https://travis-ci.org/ViDA-NYU/ache.svg?branch=master)]
(https://travis-ci.org/ViDA-NYU/ache)
[![Coverage Status](https://coveralls.io/repos/ViDA-NYU/ache/badge.svg?branch=master&service=github)](https://coveralls.io/github/ViDA-NYU/ache?branch=master)

# ACHE Focused Crawler

## Introduction

ACHE is an implementation of a focused crawler. A focused crawler is a web crawler that collects Web pages that satisfy some specific property.
ACHE differs from other crawlers in the sense the it includes **page classifiers** that allows it to distinguish between relevant and irrelevant pages in a given domain. The page classifier can be from a simple regular expression (that matches every page that contains a specific word, for example), to a sophisticated machine-learned classification model.
ACHE also includes **link classifiers**, which allows it decide the best order in which the links should be downloaded in order to find the relevant content on the web as fast as possible, at the same time it doesn't waste resources downloading irrelevant content.

## Installation
You can either build `ache` from the source code or download the execution using `conda`
### Build from source with Gradle

To build `ache` from source, you can run the following commands in your terminal:

```bash
git clone https://github.com/ViDA-NYU/ache.git
cd ache
./gradlew clean installApp
```

which will generate an installation package under `/build/install/`.


Learn more about Gradle: [http://www.gradle.org/documentation](http://www.gradle.org/documentation).

### Download with Conda

You can download `ache` from Binstar [[2]] with Conda [[3]] by running:

```bash
conda install -c memex ache
```
NOTE: Only tagged versions are published to Binstar, so `ache` from Binstart may be outdated.
If you want to try the most recent version, please clone the repository, compile the code using instructions below and then start the crawler using `ache` located in `build/install/ache/bin`.
  
## Build page classifier for ACHE

To focus on a certain topic ACHE needs to have a page classifier to decide, given a new crawled page, whether it is on-topic or not. A page classifier can be created with `ache` given positive and negative examples. Each training example corresponds to a web page whose HTML content needs to be stored in a plain text file. Assume that you store positive and negative examples in two directories, `positive` and `negative`, which reside in `training_data` directory. Here is how you build a model from these examples:

    ./build/install/ache/bin/ache buildModel -t <training data path> -o <output path for model> -c <stopwords file path>

`<training data path>` is the path to the directory containing positive and negative examples.

`<output path>` is the new directory that you want to save the generated model that consists of two files: `pageclassifier.model` and `pageclassifier.features`. 
  
`<stopwords file path>` is a file with list of words that the classifier should ignore. Example: https://github.com/ViDA-NYU/ache/blob/master/config/sample_config/stoplist.txt

Example of building a page classifier using test data:

    ./build/install/ache/bin/ache buildModel -c config/sample_config/stoplist.txt -o model_output -t config/sample_training_data

## Start ACHE

After you generate a model, you need to prepare the seed file, where each line is a URL. Then to start the crawler, run:

    ./build/install/ache/bin/ache startCrawl -o <data output path> -c <config path> -s <seed path> -m <model path> [-e <elastic search index name>]


`<configuration path>` is the path to the config directory.

`<seed path>` is the seed file.

`<model path>` is the path to the model directory (containing pageclassifier.model and pageclassifier.features).

`<data output path>` is the path to the data output directory.
 
 Example of running ACHE:
 
    ./build/install/ache/bin/ache startCrawl -o output -c config/sample_config -s config/sample.seeds -m config/sample_model -e achecrawler

## Data Formats

ACHE can store data in different data formats. The data format can be configured by changing the key `target_storage.data_format.type` in the [configuration file] (https://github.com/ViDA-NYU/ache/blob/master/config/sample_config/ache.yml). The data formats available now are:

- FILESYSTEM_HTML (default) - only raw content is stored in plain text files.
- FILESYSTEM_JSON - raw content and some metadata is stored using JSON format in files.
- FILESYSTEM_CBOR - raw content and some metadata is stored using [CBOR](http://cbor.io) format in files.
- ELATICSEARCH - raw content and metadata is indexed in an ElasticSearch index. See [ElasticSearch Integration](https://github.com/ViDA-NYU/ache/wiki/ElasticSearch-Integration) for details about configuration.
 
When using any FILESYSTEM_* data format, you can enable compression of the data stored in the files enabling the following line in the config file:
```yaml
target_storage.data_format.filesystem.compress_data: true
```

## More information?

More documentation is available in the project's [Wiki](https://github.com/ViDA-NYU/ache/wiki).

## Where to report bugs?

We welcome user feedback. Please submit any suggestions or bug reports using the Github tracker (https://github.com/ViDA-NYU/ache/issues)

[1]: http://en.wikipedia.org/wiki/Focused_crawler
[2]: https://binstar.org/
[3]: http://conda.pydata.org/

## Contact?

- Aécio Santos [aecio.santos@nyu.edu]
- Kien Pham [kien.pham@nyu.edu]
