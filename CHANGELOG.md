# ACHE Crawer Change Log

## Version 0.13.0 (2020-xx-xx)

- Upgrade gradle-node-plugin to version 2.2.4 
- Upgrade gradle wrapper to version 6.6.1
- Upgrade `crawler-commons` to version 1.1
- Reorganized gradle module directory structure
- Rename root package to 'achecrawler'
- Use multi-stage build to reduce Docker image size
- Refactor Elasticsearch repository and make it wait until the server ready


## Version 0.12.0 (2020-01-18)

- Upgrade `crawler-commons` dependency to version 0.9
- Removed Elasticsearch transport-client-based repository
- Removed Elasticsearch 1.4.4 binaries dependency
- Added DumpDataFromElasticsearch tool for dumping documents from Elasticsearch
  repositories
- Added configuration for minimum relevance in link selectors
- Added configuration for selecting whether should re-crawl sitemaps and
  robots.txt links
- Added documentaion about `relevance_threshold` parameters to the target page
  classifiers documentation page
- Added support for crawling via HTTP proxy in okhttp3 fetcher (by @maqzi)
- Added tracking of more HTTP error messages (301, 302, 3xx, 402) (by @maqzi)
- Upgrade `crawler-commons` library to version 1.0
- Upgrade `commons-validator` library to version 1.6
- Upgrade `okhttp3` library to version 3.14.0
- Fix issue #177: Links from recent TLDs are considered invalid
- Upgrade RocksDB dependency (rocksdbjni) to version 6.2.2
- Added error code details to RocksDB exception logs
- Upgrade gradle-node-plugin to version 1.3.1
- Upgrade npm version to 6.10.2
- Upgrade ache-dashboard npm dependencies
- Upgrade gradle wrapper to version 5.6.1
- Update Dockerfile to use openjdk:11-jdk (Java 11)
- Added content_type field to RegexTargetClassifier
- Change default link classifier to LinkClassifierBreadthSearch
- Update io.airlift:airline dependency to version 0.8
- Update gradle build script to use new plugins DSL
- Update coverals gradle plugin to version 2.9.0
- Update searchkit to version ^2.4.0

## Version 0.11.0 (2018-06-01)

- Removed dependency on Weka and reimplemented all machine-learning code using SMILE.
- Added option to skip cross-validation on `ache buildModel` command
- Added option to configure max number of features on `ache buildModel` command
- Changed license from GNU GPL to Apache 2.0
- Added tool (ache run ReplayCrawl) to replay old crawls using a new configuration file
- Added near-duplicate page detection using min-hashing and LSH
- Support ELASTIC format in Kafka data format (issue #155)
- Upgrade react-scripts to get rid of vulnerable transitive dependency (hoek:4.2.0)
- Upgrade npm version to 5.8.0 on gradle build script
- Changed `smile` target page classifier to use Platt's scaling only when the
  parameter 'relevance_threshold' is provided in the `pageclassifier.yml` file.
- Added Ansible scripts for automatic deployment
- Added RocksDB-based target repository (RocksDBTargetRepository)
- Fixed bug in ache-dashboard that prevented reloading search page on browser
  page refresh (issue #163)
- Support Elasticsearch 6.x (issue #158)


## Version 0.10.0 (2018-01-16)

We are pleased to announce version 0.10.0 of ACHE Crawler! This release contains very important changes, which include support for running multiple crawlers in a single server (multi-tenancy), and the start of our migration to Apache License 2 (APLv2).

Following is a detailed log of the major changes since last version:

- Upgraded gradle-node plugin to version 1.2.0
- Removed BerkeleyDB dependency (issue #143)
- Allow for running multiple crawlers in a single server (issue #103)
- REST API endpoints modified to support multiple crawlers (issue #103)
- Web interface modified to support multiple crawlers (issue #103)
- Display more metrics in crawler monitoring page
- Upgrade RocksDB (org.rocksdb:rocksdbjni) to version 5.8.7 (issue #142)
- Upgraded build script plugin "gradle-node" to version 1.2.0
- Upgraded javascript dependencies from crawler web-interface:
  - `react` to version 16.2.0
  - `react-vis` to version 1.7.9
  - `searchkit` to version 2.3.0
  - `npm` to version 5.6.0
- Allow cookies be modified dynamically via REST API endpoint (issue #114)
- Added `crawlerId` field to JSON output of target repositories to track provenance of crawled pages


## Version 0.9.0 (2017-11-07)

We are pleased to announce version 0.9.0 of ACHE Focused Crawler! We also recently reached the milestone of 100+ starts on GitHub, 55+ forks, and 1000+ commits in the current git repository.  We would like to thanks all users for the feedback we have received in the past year.

This is a large release and it brings many improvements to the documentation and several new features. Following is a detailed log of major changes since last version:

- Fixed multiple bugs and handling of exceptions
- Several improvements made to ACHE documentation
- Allow use of multiple data formats simultaneously (issue #92)
- Added new data storage format using the standard WARC format (issue #64)
- Added new data storage format using Apache Kafka (issue #123)
- Re-crawling of *sitemaps.xml* files using fixed time intervals (issue #73)
- Allow configuration of cookies in *ache.yml* (issue #81)
- Allow configuration of full User-Agent string
- Fixed memory issues that would cause OutOfMemoryError (issue #63)
- Support for robots exclusion protocol a.k.a. *robots.txt* (issue #46)
- Added new HTTP fetcher implementation using okhttp3 library with support to multiple SSL cipher suites
- Non-HTML pages are no longer parsed as HTML
- Training of new link classifiers (Online Learning) in a background thread (issue #76)
- Added REST API endpoint to stop crawler
- Added REST API endpoint to add new seeds to the crawl
- Added documentation for the REST API
- Persist run-time crawl metrics across crawler restarts (issue #101)
- Added support to per-domain wildcard link filters (issue #121)
- Add more detailed metrics for HTTP response codes (issue #120)
- Changed referrer policies in the search dashboard for better security
- Added various configuration options for timeouts in both fetcher implementations (issue #122)
- Added support for Basic HTTP authentication in the web interface (issue #129)
- Added REST API endpoints to supporting monitoring using Prometheus.io (issue #128)
- Add page relevance metrics for better monitoring (issue #119)
- Add parameters for elasticsearch index and type names through the `/startCrawl` REST API (issue #107)
- Support for serving web interface from non-root path (issue #137)
- Added button to stop crawler in web user interface (issue #139)
- Upgraded searchkit library to 2.2.0 which supports Elasticsearch 5.x
- Upgrade crawler-commons library to version 0.8

**Notice**: that there were breaking changes in some data formats:

- Repositories for relevant and irrelevant pages are now stored in the same folder (or same Elasticsearch index) and page entries include new properties to identify pages as relevant or irrelevant according to the target page classifier output. Double check the data formats documentation page and make sure you make appropriate changes if needed.


## Version 0.8.0 (2017-04-27)

We are pleased to announce version 0.8.0 of ACHE Focused Crawler.

This release includes a more complete and reorganized documentation (available at http://ache.readthedocs.io/en/latest/) and a new REST API for real-time crawler monitoring.

Following is the detailed log of major changes since last version.

- Added frontier load time metrics (issue #59)
- Update some library versions on build.gradle
- Update gradle wrapper to version 3.2.1
- Added Dockerfile
- Added connection timeouts to BingSearchAzureAPI
- Changed seed finder to use SimpleHttpFetcher
- Added option to configure a custom user agent string
- Added option of not starting console reporter in MetricsManager
- Change set_version script to work on MacOS
- Updated test dependency (Jetty) to version 9.3.6
- Rewrite all CLI programs using only airline library
- Shutdown crawler and log errors on any error (any Throwable)
- Simple WekaTargetClassifier refactoring
- Added argument --seedsPath to specify the directory to store the seed file in SeedFinder command
- Replaced the deprecated installApp by installDist gradle command in conda.recipe
- Fixed type of links extracted from sitemaps
- REST API for real-time metrics monitoring (issue #67)
- Remove dependency on linkclassifier.features file from LinkClassifierBreadthSearch (issue #65)
- Create an initial version of web-based crawler dashboard for visualization of system metrics (issue #68)
- Avoid creating empty files when not necessary in FilesTargetRepository
- Added Memex CDRv3 support
- Added Elasticsearch indexer to AcheToCdrFileExporter and rename it to AcheToCdrExporter
- Capture exceptions and retry on failures during ElasticSearch bulk indexing
- Refactoring of TargetClassifierFactory
- Added command annotation to MigrateToFilesTargetRepository tool
- Added a simple in-memory duplicate detection tool
- Added a new regex-based target classifier that matches multiple fields (issue #69)
- Created an initial version of documentation using the documentation generation system Sphinx and published documentation online at http://ache.readthedocs.io/ (issue #66)
- Added additional system descriptions and a scaffold for missing documentation (issue #66)
- Added badge with link to documentation in README.md (issue #66)
- Added an index to page-classifiers documentation page
- Improved documentation on page classifiers
- Added a tool to run a classifier over a file content
- Adjusted regex matcher to use DOTALL mode (issue #69)
- Rename test file correctly
- Write a CSV with queries, classification result, and URLs (issue #71)
- Moved SeedFinder documentation from wiki to Sphinx documentation


## Version 0.7.0 (2016-11-27)

There were more than 100 commits since the last release 0.6.0 in July 8. Following are some of the improvements.

ACHE is now simpler to use and to configure:
- Added more specific configuration samples for focused crawling and in-depth website crawling
- Stopwords are now an optional parameter, and a embedded stopword list is used by default
- Added utility tools for working with CDR (Common Data Repository) files
- Added utility to print frontier links along with relevance scores
- Added configuration for HTTP connection pool size

ACHE is faster: we fixed synchronization and parallelism issues that led to improvements of crawler efficiency of 980% (a simple benchmark available at https://github.com/ViDA-NYU/ache/issues/56).

ACHE is more resilient due fix of bugs related to:
- Extraction of malformed URLs during HTML parsing
- Failures due to handling of URLs with IPv4 addresses
- Failure to train the linking classifier for certain configuration values
- Corruption of binary data improperly stored in strings

URL normalization added for links extracted from web pages, so less duplicate content will be fetched

Cleaned log messages and added logging of structured data in CSV files regarding:
- Download requests
- Links selected to be downloaded

Added detailed software metrics that allows better monitoring and detection of problems. Added metrics include shows counts, 1, 5 and 15-minute rates, mean, median, and 75%, 95%, 98% and 99% percentiles for
- URL fetch time
- Download page processing time
- Current download queue size
- Current processing and pending downloads in queue

ACHE has an improved data management:
- Added new page repository that stores multiple pages in rolling compressed files
- Added a new alternative database backend based on Facebook's RocksDB key-value store that improves efficiency and JVM memory management.

Some stability problems were solved, such as:
- Limiting size of downloader thread-pool queue sizes
- Properly close repository files during crawler shutdown
- Avoid start crawler shutdown multiple times

Other minor improvement such as:
- Migrated code base to Java 8
- More refactoring, code cleaning, and tests (coverage 44%)


## Version 0.6.0 (2016-07-08)

We are pleased to announce version 0.6.0 of ACHE Focused Crawler. Here we list the major changes since last version.

#### New features, improvements and bug fixes:
- Implementation of [SeedFinder algorithm](http://link.springer.com/article/10.1007%2Fs11280-015-0331-7), which leverages search engine's APIs to automatically create a large and diverse seed URL set to start to bootstrap the crawler.
- Added flexible way to different handlers for different types of links, which will allow to have different extractors for each content type such as HTML, media files, XML sitemaps, etc.
- Support for sitemap.xml protocol, which allows the crawler automatically discover all links along with some metadata specified by webmasters.
- More bug fixes and code refactoring.
- More unit tests and integration tests (coverage raised to 42%)


## Version 0.5.0 (2016-04-20)

We are pleased to announce version 0.5.0 of ACHE Focused Crawler. Here we list the major changes since last version.

#### New features, improvements and bug fixes:
- New simplified configuration based on a single YML file (ache.yml)
- Fixed "backlink crawling" using Mozcape API to get backlinks
- Complete rewrite of Crawler Manager module with some threading bug fixes and new thread managing model
- Allow HTTP Fetcher cancel download of undesired mime-types (valid mime-types configuration)
- Added ability to crawl .onion links from TOR network using HTTP proxies such as Privoxy
- Added more unit tests for several components (test coverage raised to 31% of codebase)
- More code cleaning and refactorings


## Version 0.4.0 (2016-01-28)

We are pleased to announce version 0.4.0 of ACHE Crawler. Here we list the major changes since last version.

### New features, improvements and bug fixes:
- Improved and updated ACHE documentation
- Added configuration to disable English language detection
- Configured a service to measure test code coverage (https://coveralls.io/github/ViDA-NYU/ache)
- Added more unit tests for several components (test coverage raised to 24% of codebase)
- Refactor RegexBasedDetector into a new type of Page Classifier that uses regular expressions
- Refactor Link Storage to abtract a new component called LinkSelector
- Extract headers from HTTP responses
- Add support for relative redirect URLs
- Add support for redirected urls, mime type, reorganize code
- Fixed a number of small issues and minor bugs
- Removed legacy code and more code formatting
- Fixed of some memory leaks and memory usage waste
- Removed LinkMonitor and ability to print frontier pages that caused memory leaks
- Added better caching policy with limited memory usage in Frontier
- Added link selector with politeness restrictions (access URLs from the same domain only after minimum time interval)
- Added link selector that maximizes number of websites downloaded
- Added link selector to allow only crawl web pages within a max depth from the seed URLs
- Changed default JVM garbage collector used in ACHE
- Added command line option to train a Random Forest page classifier
- Refactoring of page repositories to reuse code and allow improvements
- Added configuration to hash file name when using FILESYTEM data formats
- Added new JSON data format
- Store fetch time of downloaded pages in JSON data format
- Store HTTP request headers in JSON data format
- Added deflate compression for pages repositories
- Improved command line help messages
- Updated Gradle wrapper version to 2.8
- Updated Weka version to 3.6.13
- Fixed other minor bugs
- Removed lots of unused code and code cleaning


## Version 0.3.1 (2015-07-22)

We are pleased to announce version 0.3.1 of ACHE Crawler. This is a minor release with some changes:
- Added config files to final package distribution
- Added version to command line interface
- Some code refactorings


## Version 0.3.0 (2015-07-14)

We are pleased to announce version 0.3.0 of ACHE Crawler. Here we list the major changes since version 0.2.0 (note that some changes break compatibility with previous releases).

### New features:
- New command-line interface using named parameters
- Integration with ElasticSearch with configurable index names
- Added new way to configure different types of classifiers using YAML files (this will allow new types of classifiers be added later as well as "meta classifiers" which can combine any type of classifier, using votting or machine learning ensembles for example)
- Implemented a new type of page classifier based on simple URL regular expressions
- Added filtering for extracted links using "white" and "black" lists of regular expressions
- Added tool for compression of CBOR files in GZIP using CCA format
- Added tool for off-line indexing data into ElasticSearch from crawler files in disk

### Improvements:
- Improved documentation in GitHub
- Started writing automated unit tests for new features
- Configuration of a continuous integration pipeline using TravisCI (compiles and runs the tests for each new commit in the repository)
- Embedded language detection into crawler package to ease configuration for end user (before, the user needed to download external language profiles files and specify them in command line)
- Converted bash scripts to build SVM model to a single command written in cross-platform Java code
- Don't automatically remove data from existing crawl, just resume previous crawls.

### Bug fixes:
- Escaping HTML entities from extracted links (this was causing wrong links to be extracted and the crawler waste resources trying to download unexisting pages)
- Checking for empty strings in frontier and seed file
- Fixed computation of CCA key
- Insert URLs from the seed file only when they are not already inserted
- Added shutdown hook to close LinkStorage database properly
- Removed URL fragment (#) from extracted links (this was causing duplicated URLs to be downloaded)

### Refactorings:
- Refactored tens of classes in the crawler


## Version 0.2.0 (2015-04-01)

First version release on GitHub.
