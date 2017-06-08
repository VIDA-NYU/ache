<img src="https://raw.githubusercontent.com/ViDA-NYU/ache/master/ache-logo.png" align="right" height="90px"/>

[![Build Status](https://travis-ci.org/ViDA-NYU/ache.svg?branch=master)](https://travis-ci.org/ViDA-NYU/ache)
[![Documentation Status](https://readthedocs.org/projects/ache/badge/?version=latest)](http://ache.readthedocs.io/en/latest/?badge=latest)
[![Coverage Status](https://coveralls.io/repos/ViDA-NYU/ache/badge.svg?branch=master&service=github)](https://coveralls.io/github/ViDA-NYU/ache?branch=master)

# ACHE Focused Crawler

ACHE is a focused web crawler. It collects web pages that satisfy some specific criteria, e.g., pages that belong to a given domain or that contain a user-specified pattern.
ACHE differs from generic crawlers in sense that it uses *page classifiers* to distinguish between relevant and irrelevant pages in a given domain. A page classifier can be from a simple regular expression (that matches every page that contains a specific word, for example), to a machine-learning based classification model.
ACHE can also automatically learn how to prioritize links in order to efficiently locate relevant content while avoiding the retrieval of irrelevant content.

ACHE supports many features, such as:
- Regular crawling of a fixed list of web sites
- Discovery and crawling of new relevant web sites through automatic link prioritization
- Configuration of different types of pages classifiers (machine-learning, regex, etc)
- Continuous re-crawling of sitemaps to discover new pages
- Indexing of crawled pages using Elasticsearch
- Web interface for searching crawled pages in real-time
- REST API and web-based user interface for crawler monitoring
- Crawling of hidden services using TOR proxies


## Installation

You can either build ACHE from the source code, download the executable binary using `conda`, or use Docker to build an image and run ACHE in a container.

### Build from source with Gradle

**Prerequisite:** You will need to install recent version of Java (JDK 8 or latest).

To build ACHE from source, you can run the following commands in your terminal:

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

### Running using Docker

**Prerequisite:** You will need to install a recent version of Docker. See https://docs.docker.com/engine/installation/ for details on how to install Docker for your platform.

We publish pre-built docker images on [Docker Hub](https://hub.docker.com/r/vidanyu/ache/) for each released version.
You can run the latest image using:

    docker run -p 8080:8080 vidanyu/ache:latest

Alternatively, you can build the image yourself and run it:

```
git clone https://github.com/ViDA-NYU/ache.git
cd ache
docker build -t ache .
docker run -p 8080:8080 ache
```

The [Dockerfile](https://github.com/ViDA-NYU/ache/blob/master/Dockerfile) exposes two data volumes so that you can mount a directory with your configuration files (at `/config`) and preserve the crawler stored data (at `/data`) after the container stops.

### Download with Conda

**Prerequisite:** You need to have Conda package manager installed in your system.

If you use Conda, you can install `ache` from Anaconda Cloud by running:

```
conda install -c vida-nyu ache
```

*NOTE: Only tagged versions are published to Anaconda Cloud, so the version available through Conda may not be up-to-date.
If you want to try the most recent version, please clone the repository and build from source.*

## Running ACHE

Before starting a crawl, you need to create a configuration file named `ache.yml`.
We provide some configuration samples in the repository's [config](https://github.com/ViDA-NYU/ache/tree/master/config) directory that can help you to get started.

You will also need a page classifier configuration file named `pageclassifier.yml`.
For details on how configure a page classifier, refer to the [page classifiers documentation](http://ache.readthedocs.io/en/latest/page-classifiers.html).

After you have configured a classifier, the last thing you will need is a seed file, i.e, a plain text containing one URL per line. The crawler will use these URLs to bootstrap the crawl.

Finally, you can start the crawler using the following command:

```
ache startCrawl -o <data-output-path> -c <config-path> -s <seed-file> -m <model-path>
```
where,
- `<configuration-path>` is the path to the config directory that contains `ache.yml`.
- `<seed-file>` is the seed file that contains the seed URLs.
- `<model-path>` is the path to the model directory that contains the file `pageclassifier.yml`.
- `<data-output-path>` is the path to the data output directory.

Example of running ACHE using our sample data:

```
ache startCrawl -o output -c config/sample_config -s config/sample.seeds -m config/sample_model
```


### Data Formats

ACHE can store data in different data formats. The data format can be configured by changing the key `target_storage.data_format.type` in the [configuration file](https://github.com/ViDA-NYU/ache/blob/master/config/sample_config/ache.yml). The data formats available now are:

- FILESYSTEM_HTML (default) - only raw content is stored in plain text files.
- FILESYSTEM_JSON - raw content and some metadata is stored using JSON format in files.
- FILESYSTEM_CBOR - raw content and some metadata is stored using [CBOR](http://cbor.io) format in files.
- FILES - raw content and metadata is stored in rolling compressed files of fixed size.
- ELATICSEARCH - raw content and metadata is indexed in an ElasticSearch index.

For more details on data format configurations, see the [data formats documentation](http://ache.readthedocs.io/en/latest/data-formats.html) page.


## Documentation

More info is available on the project's [documentation](http://ache.readthedocs.io/en/latest/) and on the [wiki](https://github.com/ViDA-NYU/ache/wiki).


## Bug Reports and Questions

We welcome user feedback. Please submit any suggestions, questions or bug reports using the [Github issue tracker](https://github.com/ViDA-NYU/ache/issues).

## Contributing

Code contributions are welcome. We use a code style derived from the [Google Style Guide](https://google.github.io/styleguide/javaguide.html), but with 4 spaces for tabs. A Eclipse Formatter configuration file is available in the [repository](https://github.com/ViDA-NYU/ache/blob/master/eclipse-code-style.xml).

## Contact 

- Aécio Santos [aecio.santos@nyu.edu]
- Kien Pham [kien.pham@nyu.edu]
