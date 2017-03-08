############
Data Formats
############

ACHE can store data in different data formats. The data format can be configured by changing the key ``target_storage.data_format.type`` in the `configuration file <https://github.com/ViDA-NYU/ache/blob/master/config/sample_config/ache.yml>`_.

The data formats currently available are:

* :ref:`FILESYSTEM_HTML, FILESYSTEM_JSON, FILESYSTEM_CBOR <dataformat-filesystem>`
* :ref:`FILES <dataformat-files>`
* :ref:`ELATICSEARCH <dataformat-elasticsearch>`


.. _dataformat-filesystem:

------------
FILESYSTEM_*
------------

Each page is stored in a single file, and files are organized in directories (one for each domain).
The suffix in the data format name determines how content of each file is formatted:

* ``FILESYSTEM_HTML`` - only raw content (HTML, or binary data) is stored in files. Useful for testing and opening the files HTML using the browser.
* ``FILESYSTEM_JSON`` - raw content and some metadata is stored using JSON format in files.
* ``FILESYSTEM_CBOR`` - raw content and some metadata is stored using `CBOR <http://cbor.io>`_ format in files.


When using any ``FILESYSTEM_*`` data format, you can enable compression (DEFLATE)
of the data stored in the files enabling the following line in the config file::

  target_storage.data_format.filesystem.compress_data: true

By default, the name of each file will be an encoded URL.
Unfortunately, this can cause problems in some cases where the URL is very long.
To fix this you can configure the file format to use a fixed size hash of the URL, instead of URL itself as a file name::

  target_storage.data_format.filesystem.hash_file_name: true


.. Warning ::

  All FILESYSTEM_* formats are not recommended for large crawls, since they can create millions files quickly and cause file system problems.


.. _dataformat-files:

-----
FILES
-----

Raw content and metadata is stored in rolling compressed files of fixed size (256MB).
Each file is a JSON lines file (each line contains one JSON object) compressed using the DEFLATE algorithm.
Each JSON object has the following fields:

* ``url`` - The requested URL
* ``redirected_url`` - The URL of final redirection if it applies
* ``content`` - A Base64 encoded string containing the page content
* ``content_type`` - The mime-type returned in the HTTP response
* ``response_headers`` - An array containing the HTTP response headers
* ``fetch_time`` - A integer containing the time when the page was fetched (epoch)

.. _dataformat-elasticsearch:

-------------
ELASTICSEARCH
-------------

The ELASTICSEARCH data format stores raw content and metadata as documents in an Elasticsearch index.

Types and fields
************************

Currently, ACHE indexes documents into two ElasticSearch types:

* ``target``, for pages classified as on-topic by the page classifier
* ``negative``, for pages classified as off-topic by the page classifier

These two types use the same schema, which has the following fields:

* ``domain`` - domain of the url
* ``topPrivateDomain`` -  top private domain of the url
* ``url`` - complete URL
* ``title`` - title of the page extracted from the html tag ``<title>``
* ``text`` - clean text extract from html using Boilerpipe's DefaultExtractor
* ``retrieved`` - date when the time was fetched using ISO-8601 representation Ex: "2015-04-16T07:03:50.257+0000"
* ``words`` - array of strings with tokens extracted from the text content
* ``wordsMeta`` - array of strings with tokens extracted from tags ``<meta>`` of the html content
* ``html`` - raw html content


Configuration
*************

To use ElasticSearch, you need to enable the following lines in the configuration file ``ache.yml``::

  target_storage.data_format.type: ELASTICSEARCH
  target_storage.data_format.elasticsearch.host: localhost
  target_storage.data_format.elasticsearch.port: 9300
  target_storage.data_format.elasticsearch.cluster_name: elasticsearch


.. warning ::

  ``target_storage.data_format.elasticsearch.port`` should point to the transport client port (which defaults to 9300), not the JSON API port.


Command line parameters
****************************************

When running ACHE using ElasticSearch, you should provide the name of the ElasticSearch index that should be used in the command line using the following arguments::

  -e <arg>

or::

  --elasticIndex <arg>
