Frequently Asked Questions
==========================

What is inside the output directory?
------------------------------------

Depending on the configuration settings, ACHE may creates different folders under the data output directory:

  * **data_pages**: contains raw-data of crawled pages (including relevant, irrelevant and non-HTML content). The sub-directories and file formats depends on the configured *Data Format* being using. See :ref:`Data Formats <dataformat-filesystem>` for more information.
  * **data_monitor**: contains TSV-formatted log files with information about the status of the crawl, including relevant and irrelevant pages along with their scores, download requests and its metadata, etc.
  * **data_url**, **data_backlinks**, **data_hosts**: are where the persistent storages keep data needed for crawler operation such as the frontier, the links graph, and metadata about crawled hosts.

When will the crawler stop?
---------------------------
The crawler will run until it downloads all links discovered during the crawling process, or until it hits maximum number of visited pages as configured in the ``ache.yml`` file.
You can also look at ``<data-output>/data_monitor/harvestinfo.csv`` to check how many pages have been downloaded and decide whether you want to stop the crawler manually.

How to limit the number of visited pages?
-----------------------------------------
By default, the maximum number of visited pages is set to the maximum integer value (*Integer.MAX_VALUE*).
You can modify it by setting a value for the key ``target_storage.visited_page_limit`` in the ``ache.yml`` configuration file.

What format is used to store crawled data?
------------------------------------------
ACHE supports multiple types of the data formats. Take a look at the :ref:`Data Formats <dataformat-filesystem>` page for more information.
ACHE also supports indexing web pages directly into :ref:`Elasticsearch <dataformat-elasticsearch>`.

How can I save irrelevant pages?
--------------------------------
By default, this is off so you will need to set the value of ``target_storage.store_negative_pages`` to true in the configuration file.

Does ACHE crawl webpages in languages other than English?
---------------------------------------------------------
ACHE does language detection and can be configured to ignore pages with non-English content.
You can enable or disable language detection in the configuration file by changing ``target_storage.english_language_detection_enabled``.
Detection of other languages are currently not available, but could easily be supported in the future.

Is there any limit on number of crawled webpages per website?
-------------------------------------------------------------
There is no limit by default, but you can set a hard limit in the configuration file using the key ``link_storage.max_pages_per_domain``.
You can enable this so that the crawler doesn't get trapped by particular domains, and to favor crawling a larger number of domains as opposed to focusing on a few domains.

Why am I getting a *SSL Handshake Exception* for some sites?
----------------------------------------------------------------------------------------------
A ``javax.net.ssl.SSLHandshakeException : handshake_failure`` usually occurs when the server and ache can't decide on which Cipher to use. This most probably happens when the JVM is using a limited security cipher suite. The easiest work around for this is to use OpenJDK 8+ because it comes with Java Cryptography Extension (JCE) Unlimited Strength Jurisdiction Policy out of the box. To install this JCE on Oracle, follow the instructions `here <https://github.com/ViDA-NYU/ache/issues/95>`_.

Why am I getting a *SSL Protocol Exception* for some sites?
---------------------------------------------------------------------------------------------
A ``javax.net.ssl.SSLProtocolException : unrecognized_name`` is a server misconfiguration issue. Most probably, this website is hosted on a virtual server. A simple solution is to disable SNI extension by adding ``-Djsse.enableSNIExtension=false`` as VM options when running ACHE. However, keep in mind that disabling SNI will cause certificate validation failures for some sites which use mutiple host-names behind a shared IP.


Where to report bugs?
---------------------
We welcome feedback. Please submit any suggestions or bug reports using the GitHub issue tracker (https://github.com/ViDA-NYU/ache/issues)
