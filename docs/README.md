# ACHE Documentation

This directory contains ACHE's documentation.
It is writen using `reStructuredText` format and is compiled to multiple media formats using Sphinx.
The final documentation is automatically compiled and published after every commit to http://ache.readthedocs.io/en/latest/

### Requirements

This documentation uses the following extra Sphinx packages:
 * sphinx_rtd_theme
 * httpdomain

If you use `conda`, these packages can be installed using:

```
conda install sphinx
conda install sphinx_rtd_theme
conda install -c bokeh sphinxcontrib-httpdomain
```
or using `pip`:

```
pip install sphinx sphinx_rtd_theme sphinxcontrib-httpdomain
```

### Editing the Documentation

You can edit any `.rst` file, and then use the make to command to compile the docs:

    make html

Finally, open the generated HTML file using your web browser. The entry page of
the documentation can be found at: ``_build/html/index.html``.
