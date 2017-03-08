Link Filters
############

ACHE allows one to restrict what web domains and paths within a domain that should be crawled through regular expressions.
There are two types of link filters:

* **whitelists** - URLS that should be downloaded;
* **blacklists** - URLS that should not be downloaded.

To configure a link filter, you will need to create a text file containing one regular expression per line.
All regular expressions loaded will be evaluated against the links found on the web pages crawled in other to determine whether the crawler should accept or reject them.

For whitelist filters, the file should be named ``link_whitelist.txt``, whereas for blacklist filters it should be ``link_blacklist.txt``. These files should be placed under the config directory (same folder as ``ache.yml``). These files will be loaded during crawler start-up, and the filters loaded will be printed out to the crawler log.

A link filter file looks like this::

  https?://www\.example.com/some_path/*
  https?://www\.another-example.com/*
