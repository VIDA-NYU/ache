package achecrawler.link.classifier.builder;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import achecrawler.util.parser.LinkNeighborhood;
import achecrawler.util.parser.PaginaURL;
import achecrawler.util.string.PorterStemmer;
import achecrawler.util.string.Stopwords;

/**
 * <p>
 * Description: This class from a predefined set of words extracts for each link the frequency of
 * these words given a page. These words are the features used by the Link Classifier.
 * </p>
 *
 * <p>
 * Copyright: Copyright (c) 2004
 * </p>
 *
 *
 * @author Luciano Barbosa
 * @version 1.0
 */
public class LinkNeighborhoodWrapper {

    private Stopwords stopwords = null;
    private String[][] fieldWords;
    private final PorterStemmer stemmer = new PorterStemmer();

    public LinkNeighborhoodWrapper(Stopwords stopwords) {
        this.stopwords = stopwords;
    }
    
    public LinkNeighborhoodWrapper(String[] features, Stopwords stopwords) {
        this.stopwords = stopwords;
        this.setFeatures(features);
    }

    public LinkNeighborhoodWrapper() {}

    public void setFeatures(String[][] fieldWords) {
        this.fieldWords = fieldWords;
    }

    private void setFeatures(String[] features) {
        String[][] fieldWords = new String[WordField.FIELD_NAMES.length][];

        List<String> aroundTemp = new ArrayList<>();
        List<String> altTemp = new ArrayList<>();
        List<String> srcTemp = new ArrayList<>();
        List<String> urlTemp = new ArrayList<>();
        List<String> anchorTemp = new ArrayList<>();

        for (String feature : features) {
            if (feature.startsWith("around_")) {
                String[] parts = feature.split("_");
                aroundTemp.add(parts[1]);
            }
            if (feature.startsWith("alt_")) {
                String[] parts = feature.split("_");
                altTemp.add(parts[1]);
            }
            if (feature.startsWith("src_")) {
                String[] parts = feature.split("_");
                srcTemp.add(parts[1]);
            }
            if (feature.startsWith("url_")) {
                String[] parts = feature.split("_");
                urlTemp.add(parts[1]);
            }
            if (feature.startsWith("anchor_")) {
                String[] parts = feature.split("_");
                anchorTemp.add(parts[1]);
            }
        }

        String[] around = new String[aroundTemp.size()];
        aroundTemp.toArray(around);
        fieldWords[WordField.AROUND] = around;

        String[] alt = new String[altTemp.size()];
        altTemp.toArray(alt);
        fieldWords[WordField.ALT] = alt;

        String[] src = new String[srcTemp.size()];
        srcTemp.toArray(src);
        fieldWords[WordField.SRC] = src;

        String[] url = new String[urlTemp.size()];
        urlTemp.toArray(url);
        fieldWords[WordField.URLFIELD] = url;

        String[] anchor = new String[anchorTemp.size()];
        anchorTemp.toArray(anchor);
        fieldWords[WordField.ANCHOR] = anchor;

        this.fieldWords = fieldWords;
    }

    /**
     * Extract the information from links as word in the URL, anchor and around links, and converts
     * it to an Instance object
     * 
     * @param linkNeighboors The LinkNeighborhood instances to be converted to Instance
     * @param features String[] pre-defined words
     * @return HashMap mapping url -> instance
     */
    public Map<String, Instance> extractLinks(LinkNeighborhood[] linkNeighboors, String[] features)
            throws MalformedURLException {
        Map<String, WordField[]> linkFields = extractLinks(linkNeighboors);
        return mapFeatures(linkFields, features);
    }

    public Instance extractToInstance(LinkNeighborhood linkNeighboor, String[] features)
            throws MalformedURLException {
        final boolean useImageFeatures = false;
        String url = linkNeighboor.getLink().toString();
        WordField[] linkFields = extract(linkNeighboor, url, useImageFeatures);
        return mapFeaturesToInstance(features, linkFields);
    }

    public Instance extractToInstanceWithImageFeatures(LinkNeighborhood linkNeighboor,
            String[] features) throws MalformedURLException {
        final boolean useImageFeatures = true;
        String url = linkNeighboor.getLink().toString();
        WordField[] linkFields = extract(linkNeighboor, url, useImageFeatures);
        return mapFeaturesToInstance(features, linkFields);
    }

    private Map<String, Instance> mapFeatures(Map<String, WordField[]> linkFields,
            String[] features) {
        Map<String, Instance> result = new HashMap<>();
        for (Entry<String, WordField[]> kv : linkFields.entrySet()) {
            String url = kv.getKey();
            WordField[] words = kv.getValue();
            Instance instance = mapFeaturesToInstance(features, words);
            result.put(url, instance);
        }
        return result;
    }

    private Instance mapFeaturesToInstance(String[] features, WordField[] words) {
        Instance instance = new Instance(features);
        for (WordField wordField : words) {
            String field = (WordField.FIELD_NAMES[wordField.getField()]).toLowerCase();
            String word = wordField.getWord();
            if (wordField.getField() == WordField.URLFIELD || wordField.getField() == WordField.SRC) {
                List<String> wordsTemp = searchSubstring(wordField.getWord(), wordField.getField());
                for (String s : wordsTemp) {
                    word = s;
                    word = field + "_" + word;
                    instance.setValue(word, 1.0);
                }
            } else {
                if (word != null) {
                    word = field + "_" + word;
                    instance.setValue(word, 1.0);
                }
            }
        }
        return instance;
    }

    private String stemming(String word) {
        String new_word;
        try {
            new_word = stemmer.stem(word);
            if (new_word.contains("No term") || new_word.contains("Invalid term")) {
                new_word = word;
            }
        } catch (Exception e) {
            new_word = word;
        }
        return new_word;
    }

    private List<String> searchSubstring(String word, int field) {
        List<String> result = new ArrayList<>();
        String[] words = fieldWords[field];
        for (String s : words) {
            String tempWord = s;
            int index = tempWord.indexOf("_");
            if (index != -1) {
                tempWord = tempWord.substring(index + 1);
            }
            if (word != null && word.toLowerCase().equals(tempWord)) {
                result.add(tempWord);
            }
        }
        return result;
    }

    private Map<String, WordField[]> extractLinks(LinkNeighborhood[] linkNeighboors)
            throws MalformedURLException {
        Map<String, WordField[]> result = new HashMap<>();
        for (LinkNeighborhood linkNeighboor : linkNeighboors) {
            extractToMap(result, linkNeighboor);
        }
        return result;
    }

    private void extractToMap(Map<String, WordField[]> result, LinkNeighborhood ln)
            throws MalformedURLException {
        String urlStr = ln.getLink().toString();
        WordField[] wordsArray = extract(ln, urlStr, false);
        if (wordsArray != null) {
            result.put(urlStr, wordsArray);
        }
    }

    private WordField[] extract(LinkNeighborhood ln, String urlStr, boolean useImageFeatures)
            throws MalformedURLException {
        List<WordField> words = new ArrayList<>();
        if (useImageFeatures) {
            if (ln.getImgSrc() != null) {
                PaginaURL pageParser = new PaginaURL(new URL("http://"), ln.getImgSrc(), stopwords);
                addFeaturesToWordFieldList(words, pageParser.words(), WordField.SRC);
            }
            addFeaturesToWordFieldList(words, ln.getImgAlt(), WordField.ALT);
        }
        addUrlWordsToWordField(urlStr, words);
        addFeaturesToWordFieldList(words, ln.getAnchor(), WordField.ANCHOR);
        addFeaturesToWordFieldList(words, ln.getAround(), WordField.AROUND);
        return asNonEmptyArray(words);
    }

    /**
     * Put the words in URL into bag of words
     *
     * @param urlStr String
     * @param wordsFields Vector list of word
     */
    private void addUrlWordsToWordField(String urlStr, List<WordField> wordsFields)
            throws MalformedURLException {
        URL url = new URL(urlStr);
        String host = url.getHost();
        int index = host.lastIndexOf(".");
        if (index != -1) {
            host = "host_" + host.substring(index + 1);
            wordsFields.add(new WordField(WordField.URLFIELD, host));
        }
        PaginaURL pageParser = new PaginaURL(url, url.getFile(), stopwords);
        String[] terms = pageParser.words();
        for (String term : terms) {
            wordsFields.add(new WordField(WordField.URLFIELD, stemming(term)));
        }
    }

    private WordField[] asNonEmptyArray(List<WordField> words) {
        if (words != null && !words.isEmpty()) {
            WordField[] wordsArray = new WordField[words.size()];
            words.toArray(wordsArray);
            return wordsArray;
        } else {
            return null;
        }
    }

    private void addFeaturesToWordFieldList(List<WordField> words, String[] textTerms, int type) {
        if(textTerms == null) return;
        for (String textTerm : textTerms) {
            WordField wf = new WordField(type, stemming(textTerm));
            words.add(wf);
        }
    }

}

