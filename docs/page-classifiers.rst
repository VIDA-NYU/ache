Target Page Classifiers
#######################

.. highlight :: yaml

ACHE uses target page classifiers to distinguish between relevant and irrelevant pages.
To configure a page classifier, you will need to create a new folder containing a file named `pageclassifier.yml` specifying the type of classifier that should be used.
ACHE contains several `page classifier implementations <https://github.com/ViDA-NYU/ache/tree/master/src/main/java/focusedCrawler/target/classifier>`_ available.
The following subsections describe how to configure them.

title_regex
-----------

Classifies a page as relevant if the HTML tag `title` matches a given pattern defined by a provided regular expression.
You can provide this regular expression using the `pageclassifier.yml` file. Pages that match this expression are considered relevant. For example::

  type: title_regex
  parameters:
    regular_expression: ".*sometext.*"


url_regex
-----------

Classifies a page as relevant if the **URL** of the page matches any of the regular expression patterns provided.
You can provide a list of regular expressions using the `pageclassifier.yml` file as follows::

  type: url_regex
  parameters:
    regular_expressions: [
      "https?://www\\.somedomain\\.com/forum/.*"
      ".*/thread/.*",
      ".*/archive/index.php/t.*",
    ]


body_regex
-----------

Classifies a page as relevant if the HTML content of the page matches any of the regular expression patterns provided.
You can provide a list of regular expressions using the `pageclassifier.yml` file as follows::

  type: body_regex
  parameters:
    regular_expressions:
    - pattern1
    - pattern2

weka
-----------

Classifies pages using a machine-learning based text classifier (SVM, Random Forest) trained using ACHE's `buildModel` command. Current classifier implementation uses the library Weka.

You need to provide the path for a *features_file*, a *model_file*, and a *stopwords_file* file containing the stop-words used during the training process:

.. code-block:: yaml

  type: weka
  parameters:
    features_file: pageclassifier.features
    model_file: pageclassifier.model
    stopwords_file: stoplist.txt

You can build these files by training a model, as detailed in the next sub-section.

Alternatively, you can use the `Domain Discovery Tool (DDT) <https://github.com/ViDA-NYU/domain_discovery_tool>`_ to gather training data and build automatically these files.
DDT is a interactive web-based application that helps the user with the process of training a page classifier for ACHE.

Building a model for the weka page classifier
*********************************************

To create the files ``pageclassifier.features`` and ``pageclassifier.model``, you
can use ACHE's command line.
You will need positive (relevant) and negative (irrelevant) examples of web pages to train the page classifier.
You should store the HTML content of each web page in a plain text file. These files should be placed in two directories, named `positive` and `negative`, which reside in another empty directory. You can see an example at `config/sample_training_data <https://github.com/ViDA-NYU/ache/tree/master/config/sample_training_data>`_.

Here is how you build a model from these examples using ACHE's commmand line::

  ache buildModel -t <training data path> -o <output path for model> -c <stopwords file path>

where,

* ``<training data path>`` is the path to the directory containing positive and negative examples.
* ``<output path>`` is the new directory that you want to save the generated model that consists of two files: ``pageclassifier.model`` and ``pageclassifier.features``.
* ``<stopwords file path>`` is a file with list of words that the classifier should ignore. You can see an example at `config/sample_config/stoplist.txt <https://github.com/ViDA-NYU/ache/blob/master/config/sample_config/stoplist.txt>`_.

Example of building a page classifier using our test data::

  ache buildModel -c config/sample_config/stoplist.txt -o model_output -t config/sample_training_data
