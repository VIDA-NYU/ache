<img src="https://raw.githubusercontent.com/ViDA-NYU/ache/master/ache-logo.png" align="right" height="90px"/>

[![Build Status](https://travis-ci.org/ViDA-NYU/ache.svg?branch=master)](https://travis-ci.org/ViDA-NYU/ache)
[![Documentation Status](https://readthedocs.org/projects/ache/badge/?version=latest)](http://ache.readthedocs.io/en/latest/?badge=latest)
[![Coverage Status](https://coveralls.io/repos/ViDA-NYU/ache/badge.svg?branch=master&service=github)](https://coveralls.io/github/ViDA-NYU/ache?branch=master)

# ACHE Focused Crawler

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
./gradlew installDist
```

which will generate an installation package under `ache/build/install/`. You can then make ACHE command line available in the terminal by adding ACHE to the PATH:

```bash
export ACHE_HOME="{path-to-cloned-ache-repository}/build/install/ache"
export PATH="$ACHE_HOME/bin:$PATH"
```

### Download with Conda

If you use the Conda package manager [[2]], you can install `ache` from Anaconda Cloud [[3]] by running:

```
conda install -c vida-nyu ache
```
NOTE: Only tagged versions are published to Anaconda Cloud, so the version available through Conda may not be up-to-date.
If you want to try the most recent version, please clone the repository and build from source.

## Target Page Classifiers

ACHE uses target page classifiers to distinguish between relevant and irrelevant pages. Page classifiers are flexible and can be as simple as a simple regular expression, or a sophisticated machine-learning based classification model.
ACHE contains several page classifier implementations available. See [http://ache.readthedocs.io/en/latest/page-classifiers.html](http://ache.readthedocs.io/en/latest/page-classifiers.html) for details on how to configure them.

## Running ACHE

After you generate a model, you need to prepare the seed file, where each line is a URL. Then to start the crawler, run:
```
ache startCrawl -o <data output path> -c <config path> -s <seed path> -m <model path>
```
where,
- `<configuration path>` is the path to the config directory.
- `<seed path>` is the seed file.
- `<model path>` is the path to the model directory (containing files `pageclassifier.yml`, `pageclassifier.model`, and `pageclassifier.features`).
- `<data output path>` is the path to the data output directory.

Example of running ACHE using our sample data:
```
ache startCrawl -o output -c config/sample_config -s config/sample.seeds -m config/sample_model
```

## Data Formats

ACHE can store data in different data formats. The data format can be configured by changing the key `target_storage.data_format.type` in the [configuration file](https://github.com/ViDA-NYU/ache/blob/master/config/sample_config/ache.yml). The data formats available now are:

- FILESYSTEM_HTML (default) - only raw content is stored in plain text files.
- FILESYSTEM_JSON - raw content and some metadata is stored using JSON format in files.
- FILESYSTEM_CBOR - raw content and some metadata is stored using [CBOR](http://cbor.io) format in files.
- FILES - raw content and metadata is stored in rolling compressed files of fixed size.
- ELATICSEARCH - raw content and metadata is indexed in an ElasticSearch index.

For more details on data format configurations, see [http://ache.readthedocs.io/en/latest/data-formats.html](http://ache.readthedocs.io/en/latest/data-formats.html).


## Link Filters

ACHE allows one to restrict what web domains and paths within a domain that should be crawled through regular expresions. There are two types of link filters: **whitelists** (links that should be downloaded) and **blacklists** (links that should not be downloaded). To configure a link filter, you will need to create a text file containing one regular expression per line. All regular expressions loaded will be evaluated against the links found on the web pages crawled in other to accept or reject them. For whitelist filters, the file should be named `link_whitelist.txt`, whereas for blacklist filters it should be `link_blacklist.txt`. These files should be placed under the config directory (same folder as `ache.yml`). These files will be loaded during crawler start-up, and the filters loaded will be printed out to the crawler log. A link filter file looks like this:

```
https?://www\.example.com/some_path/*
https?://www\.another-example.com/*
```

## More information?

More documentation is available on the project's [Documentation](http://ache.readthedocs.io/en/latest/) and on the [Wiki](https://github.com/ViDA-NYU/ache/wiki).

## Where to report bugs?

We welcome user feedback. Please submit any suggestions or bug reports using the Github tracker (https://github.com/ViDA-NYU/ache/issues)

## Contact?

- Aécio Santos [aecio.santos@nyu.edu]
- Kien Pham [kien.pham@nyu.edu]
