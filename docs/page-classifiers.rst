.. _pageclassifiers:

Target Page Classifiers
#######################

.. highlight :: yaml

ACHE uses target page classifiers to distinguish between relevant and irrelevant pages.
Page classifiers are flexible and can be as simple as a simple regular expression,
or a sophisticated machine-learning based classification model.



Configuring Page Classifiers
============================

To configure a page classifier, you will need to create a new directory
containing a file named  ``pageclassifier.yml`` specifying the type of
classifier that should be used and its parameters.
ACHE contains several `page classifier implementations
<https://github.com/ViDA-NYU/ache/tree/master/ache/src/main/java/achecrawler/target/classifier>`_
available. The following subsections describe how to configure them:

* :ref:`title_regex <pageclassifier_title_regex>`
* :ref:`url_regex <pageclassifier_url_regex>`
* :ref:`body_regex <pageclassifier_body_regex>`
* :ref:`regex <pageclassifier_regex>`
* :ref:`smile <pageclassifier_smile>` (a.k.a "weka" before version 0.11.0)


.. _pageclassifier_title_regex:

title_regex
-----------

Classifies a page as relevant if the HTML tag `title` matches a given pattern defined by a provided regular expression.
You can provide this regular expression using the  ``pageclassifier.yml`` file. Pages that match this expression are considered relevant. For example::

  type: title_regex
  parameters:
    regular_expression: ".*sometext.*"


.. _pageclassifier_url_regex:

url_regex
----------

Classifies a page as relevant if the **URL** of the page matches any of the regular expression patterns provided.
You can provide a list of regular expressions using the  ``pageclassifier.yml`` file as follows::

  type: url_regex
  parameters:
    regular_expressions: [
      "https?://www\\.somedomain\\.com/forum/.*"
      ".*/thread/.*",
      ".*/archive/index.php/t.*",
    ]


.. _pageclassifier_body_regex:

body_regex
-----------

Classifies a page as relevant if the HTML content of the page matches any of the regular expression patterns provided.
You can provide a list of regular expressions using the  ``pageclassifier.yml`` file as follows::

  type: body_regex
  parameters:
    regular_expressions:
    - pattern1
    - pattern2

.. _pageclassifier_regex:

regex
-----------

Classifies a page as relevant by matching the lists of regular expressions
provided against multiple fields: `url`, `title`, `content`, and `content_type`.
You can provide a list of regular expressions for each of these fields,
and also the type of boolean operation to combine the results:

* **AND** (default): All regular expressions must match
* **OR**: At least one regular expression must match

Besides the combination method for each regular expression within a list,
you cab also specify how the final result for each field should be combined.
The file  ``pageclassifier.yml`` should be organized as follows:

.. code-block:: yaml

  type: regex
  parameters:
      boolean_operator: AND|OR
      url:
        boolean_operator: AND|OR
        regexes:
          - pattern1-for-url
          - pattern2-for-url
      title:
        boolean_operator: AND|OR
        regexes:
          - pattern1-for-title
          - pattern2-for-title
      content:
        boolean_operator: AND|OR
        regexes:
          - pattern1-for-content
      content_type:
        boolean_operator: AND|OR
        regexes:
          - pattern1-for-content-type

For example, in order to be classified as relevant using the following
configuration, a page would have to:

* match regexes ``.*category=1.*`` OR ``.*post\.php.*`` in the URL
* AND
* it would have to match ``.*bar.*`` OR ``.*foo.*`` in the title.

.. code-block:: yaml

  type: regex
  parameters:
      boolean_operator: "AND"
      url:
        boolean_operator: "OR"
        regexes:
          - .*category=1.*
          - .*post\.php.*
      title:
        boolean_operator: "OR"
        regexes:
          - .*bar.*
          - .*foo.*

.. _pageclassifier_smile:

smile (a.k.a "weka" before version 0.11.0)
------------------------------------------

.. Warning ::

  This classifier was previously known as ``weka`` before version 0.11.0, and has
  been re-implemented using `SMILE library <http://haifengl.github.io/smile/>`_
  which uses a more permissive open-source license (Apache 2.0).
  If you have models built using a previous ACHE version, you will need to
  re-build your model before upgrading ACHE to a version equal or greater
  than 0.11.0.

Classifies pages using a machine-learning based text classifier (SVM, Random Forest)
trained using ACHE's ``buildModel`` command. 
Smile page classifiers can be built automatically by training a model using the command
``ache buildModel``, as detailed in the next sub-section. You can also run
``ache help buildModel`` to see more options available.

Alternatively, you can use the `Domain Discovery Tool (DDT) <https://github.com/ViDA-NYU/domain_discovery_tool>`_ to gather training data and build automatically these files.
DDT is an interactive web-based application that helps the user with the process of training a page classifier for ACHE.

A `smile` classifier supports the following parameters in the ``pageclassifier.yml``:

* ``features_file``, ``model_file``: files containing the list of features used by the classifier and the serialized learned model respectively.
* ``stopwords_file``: a file containing stop-words (words ignored) used during the training process;
* ``relevance_threshold``: a number between 0.0 and 1.0 indicating the minimum relevance probability threshold for a page to be considered relevant.
  Higher values indicate that only pages which the classifier is highly confident are considered relevant.

Following is a sample ``pageclassifier.yml`` file for a smile classifier:

.. code-block:: yaml

  type: smile
  parameters:
    features_file: pageclassifier.features
    model_file: pageclassifier.model
    stopwords_file: stoplist.txt
    relevance_threshold: 0.6


Building a model for the smile page classifier
**********************************************

To create the necessary configuration files, you will need to gather
positive (relevant) and negative (irrelevant) examples of web pages to train
the page classifier.
You should store the HTML content of each web page in a plain text file.
These files should be placed in two directories, named ``positive` and
``negative``, which reside in another empty directory.
See an example at `config/sample_training_data <https://github.com/ViDA-NYU/ache/tree/master/config/sample_training_data>`_.

Here is how you build a model from these examples using ACHE's command line::

  ache buildModel -t <training data path> -o <output path for model> -c <stopwords file path>

where,

* ``<training data path>`` is the path to the directory containing positive and negative examples.
* ``<output path>`` is the new directory that you want to save the generated model that consists of two files: ``pageclassifier.model`` and ``pageclassifier.features``.
* ``<stopwords file path>`` is a file with list of words that the classifier should ignore. You can see an example at `config/sample_config/stoplist.txt <https://github.com/ViDA-NYU/ache/blob/master/config/sample_config/stoplist.txt>`_.

Example of building a page classifier using our test data::

  ache buildModel -c config/sample_config/stoplist.txt -o model_output -t config/sample_training_data


.. _testing_page_classifiers:

Testing Page Classifiers
========================

Once you have configured your classifier, you can verify whether it is working
properly to classify a specific web page by running the following command::

    ache run TargetClassifierTester --input-file {html-file} --model {model-config-directory}

where,

* ``{html-file}`` is the path to a file containing the page's HTML content and
* ``{model-config-directory}`` is a path to the configuration directory containing your page classifier configuration.
