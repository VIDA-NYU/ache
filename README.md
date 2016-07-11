<img src="https://raw.githubusercontent.com/ViDA-NYU/ache/master/ache-logo.png" align="right" height="100px"/>
[![Build Status](https://travis-ci.org/ViDA-NYU/ache.svg?branch=master)](https://travis-ci.org/ViDA-NYU/ache)
[![Coverage Status](https://coveralls.io/repos/ViDA-NYU/ache/badge.svg?branch=master&service=github)](https://coveralls.io/github/ViDA-NYU/ache?branch=master)

# ACHE Focused Crawler

## Introduction

ACHE is an implementation of a focused crawler. A focused crawler is a web crawler that collects Web pages that satisfy some specific property.
ACHE differs from other crawlers in the sense that it includes **page classifiers** that allows it to distinguish between relevant and irrelevant pages in a given domain. The page classifier can be from a simple regular expression (that matches every page that contains a specific word, for example), to a sophisticated machine-learned classification model.
ACHE also includes **link classifiers**, which allows it decide the best order in which the links should be downloaded in order to find the relevant content on the web as fast as possible, at the same time it doesn't waste resources downloading irrelevant content.

## Installation
You can either build ACHE from the source code or download the executable binary using `conda`.
### Build from source with Gradle

To build `ache` from source, you can run the following commands in your terminal:

```
git clone https://github.com/ViDA-NYU/ache.git
cd ache
./gradlew clean installApp
```

which will generate an installation package under `ache/build/install/`. You can then make ACHE command line available in the terminal by adding ACHE to the PATH:

```bash
export ACHE_HOME="{path-to-cloned-ache-repository}/build/install/ache"
export PATH="$ACHE_HOME/bin:$PATH"
```

### Download with Conda

If you use the Conda package manager [[2]], you can install `ache` from Anaconda Cloud [[3]] by running:

```
conda install -c memex ache
```
NOTE: Only tagged versions are published to Anaconda Cloud, so the version available through Conda may not be up-to-date.
If you want to try the most recent version, please clone the repository and build from source.

## Page Classifiers

ACHE uses page classifiers to distinguish between relevant and irrelevant pages. Currently, the following page classifiers are available:
- `url_regex` - classify a page as relevant if the **URL** of the page matches a given pattern defined by a provided regular expression.
- `title_regex` - classify a page as relevant if the HTML's tag **title** matches a given pattern defined by a provided regular expression.
- `weka` - uses a machine-learning classifier (SVM, Random Forest) trained using ACHE's `buildModel` command. Current classifier implementation relies on library Weka.

To configure a page classifier, you will need to create a new folder containing a file named `pageclassifier.yml` specifying the type of classifier that should be used, as follows.

#### title_regex

You can provide a regular expression to match the title of the page. Pages that match this expression are considered relevant. Example:

```yml
type: title_regex
parameters:
  regular_expression: "sometext"
```

#### url_regex
You can provide a list of regular expressions to match the URLs. Pages that match any of the regular expressions are considered relevant.

```yml
type: url_regex
parameters:
  regular_expressions: [
    "https?://www\\.somedomain\\.com/forum/.*"
    ".*/thread/.*",
    ".*/archive/index.php/t.*",
  ]
```

#### weka

You need provide the path for a *features_file*, a *model_file*, and a *stopwords_file* file containing the stop-words used during the training process:

```yml
type: weka
parameters:
  features_file: pageclassifier.features
  model_file: pageclassifier.model
  stopwords_file: stoplist.txt
```

You can build these files by training a model, as detailed in the next sub-section.
Alternatively, you can use the [Domain Discovery Tool (DDT)](https://github.com/ViDA-NYU/domain_discovery_tool) to gather training data and build automatically these files. DDT is a interactive web-based app to help the user with the process of training a page classifier for ACHE.

**Building a model for the weka page classifier**

To create the files `pageclassifier.features` and `pageclassifier.model`, you
can use ACHE's command line.
You will need positive (relevant) and negative (irrelevant) examples of web pages to train the page classifier.
You should store the HTML content of each web page in a plain text file. These files should be placed in two directories, named `positive` and `negative`, which reside in another empty directory. You can see an example at [config/sample_training_data](https://github.com/ViDA-NYU/ache/tree/master/config/sample_training_data).

Here is how you build a model from these examples using ACHE's commmand line:

```
ache buildModel -t <training data path> -o <output path for model> -c <stopwords file path>
```
where,
- `<training data path>` is the path to the directory containing positive and negative examples.
- `<output path>` is the new directory that you want to save the generated model that consists of two files: `pageclassifier.model` and `pageclassifier.features`.
- `<stopwords file path>` is a file with list of words that the classifier should ignore. You can see an example at [config/sample_config/stoplist.txt](https://github.com/ViDA-NYU/ache/blob/master/config/sample_config/stoplist.txt).

Example of building a page classifier using our test data:
```
ache buildModel -c config/sample_config/stoplist.txt -o model_output -t config/sample_training_data
```

## Running ACHE

After you generate a model, you need to prepare the seed file, where each line is a URL. Then to start the crawler, run:
```
ache startCrawl -o <data output path> -c <config path> -s <seed path> -m <model path> [-e <elastic search index name>]
```
where,
- `<configuration path>` is the path to the config directory.
- `<seed path>` is the seed file.
- `<model path>` is the path to the model directory (containing files `pageclassifier.yml`, `pageclassifier.model`, and `pageclassifier.features`).
- `<data output path>` is the path to the data output directory.

Example of running ACHE using our sample data:
```
ache startCrawl -o output -c config/sample_config -s config/sample.seeds -m config/sample_model -e achecrawler
```

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

## Link Filters

ACHE allows one to restrict what web domains and paths within a domain that should be crawled through regular expresions. There are two types of link filters: **whitelists** (links that should be downloaded) and **blacklists** (links that should not be downloaded). To configure a link filter, you will need to create a text file containing one regular expression per line. All regular expressions loaded will be evaluated against the links found on the web pages crawled in other to accept or reject them. For whitelist filters, the file should be named `link_whitelist.txt`, whereas for blacklist filters it should be `link_blacklist.txt`. These files should be placed under the config directory (same folder as `ache.yml`). These files will be loaded during crawler start-up, and the filters loaded will be printed out to the crawler log. A link filter file looks like this:

```
https?://www\.example.com/some_path/*
https?://www\.another-example.com/*
```

## More information?

More documentation is available in the project's [Wiki](https://github.com/ViDA-NYU/ache/wiki).

## Where to report bugs?

We welcome user feedback. Please submit any suggestions or bug reports using the Github tracker (https://github.com/ViDA-NYU/ache/issues)

[1]: http://en.wikipedia.org/wiki/Focused_crawler
[2]: http://conda.pydata.org/
[3]: https://anaconda.org/

## Contact?

- Aécio Santos [aecio.santos@nyu.edu]
- Kien Pham [kien.pham@nyu.edu]
