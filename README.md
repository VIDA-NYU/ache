[![Build Status](https://travis-ci.org/ViDA-NYU/ache.svg?branch=master)]
(https://travis-ci.org/ViDA-NYU/ache)

# ACHE Focused Crawler

## Introduction

ACHE is an implementation of a focused crawler. A focused crawler is a web crawler that collects Web pages that satisfy some specific property, by carefully prioritizing the crawl frontier and managing the hyperlink exploration process [[1]].

## Installation

### Download with Conda

You can download `ache` from Binstar [[2]] with Conda [[3]] by running:

```bash
conda install -c memex ache
```
NOTE: Only tagged versions are published to Binstar, so `ache` from Binstart may be outdated.
If you want to try the most recent version, please clone the repository, compile the code using instructions below and then start the crawler using `ache` located in `build/install/ache/bin`.

### Build from source with Gradle

To build `ache` from source, you can run the following commands in your terminal:

```bash
git clone https://github.com/ViDA-NYU/ache.git
cd ache
./gradlew clean installApp
```

which will generate an installation package under `/build/install/`.

Alternatively, you can build a zip archive:

```bash
./gradlew clean distZip
```
or a tar archive:

```bash
./gradlew clean distTar
```

which will generate a zip/tar file of your project under `/build/distributions/`.

Learn more about Gradle: [http://www.gradle.org/documentation](http://www.gradle.org/documentation).
  
  
## Build a model for ACHE's page classifier
(If you already have the model, skip this step.)

To focus on a certain topic ACHE needs to have access to a model of its content. This model is then 
used by a classifier to decide, given a new crawled page, whether it is on-topic or not. Assume that you store positive and negative examples in two directories, `positive` and `negative`. Also, assume these directories are placed in the `training_data` directory. Here is how you build a model from these examples:

    ./build/install/ache/bin/ache buildModel -t <training data path> -o <output path for model> -c <stopwords file path>

`<training data path>` is the path to the directory containing positive and negative examples.

`<output path>` is the new directory that you want to save the generated model that consists of two files: `pageclassifier.model` and `pageclassifier.features`. 
  
`<stopwords file path>` is a file with list of words that the classifier should ignore. Example: https://github.com/ViDA-NYU/ache/blob/master/config/sample_config/stoplist.txt

## Start ACHE

After you generate a model, you need to prepare the seed file, where each line is a URL. To start the crawler, run:

    ./build/install/ache/bin/ache startCrawl -o <data output path> -c <config path> -s <seed path> -m <model path> [-e <elastic search index name>]


`<configuration path>` is the path to the config directory.

`<seed path>` is the seed file.

`<model path>` is the path to the model directory (containing pageclassifier.model and pageclassifier.features).

`<data output path>` is the path to the data output directory.
 
 Example of running ACHE:
 
    ./build/install/ache/bin/ache startCrawl -o output -c config/sample_config -s config/sample.seeds -m config/sample_model -e achecrawler

## Data Formats

ACHE can store data in different data formats. The data format can be configured by changing the key `DATA_FORMAT` in the [target storage configuration file] (https://github.com/ViDA-NYU/ache/blob/master/config/sample_config/target_storage/target_storage.cfg). The data formats available now are:

- FILE (default) -- only raw content is stored in plain text files.
- CBOR -- raw content and some metadata is stored using [CBOR](http://cbor.io) format in files.
- ELATICSEARCH -- raw content and metadata is indexed in an ElasticSearch index. See [ElasticSearch Integration](https://github.com/ViDA-NYU/ache/wiki/ElasticSearch-Integration) for details about configuration.

## More information?

More documentation is availabe in the project's [Wiki](https://github.com/ViDA-NYU/ache/wiki).

## Where to report bugs?

We welcome user feedback. Please submit any suggestions or bug reports using the Github tracker (https://github.com/ViDA-NYU/ache/issues)

[1]: http://en.wikipedia.org/wiki/Focused_crawler
[2]: https://binstar.org/
[3]: http://conda.pydata.org/
