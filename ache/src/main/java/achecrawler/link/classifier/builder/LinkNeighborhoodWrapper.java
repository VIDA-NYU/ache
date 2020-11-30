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
import achecrawler.util.string.StopList;

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

    private StopList stoplist = null;
    private String[][] fieldWords;
    private PorterStemmer stemmer = new PorterStemmer();

    public LinkNeighborhoodWrapper(StopList stoplist) {
        this.stoplist = stoplist;
    }
    
    public LinkNeighborhoodWrapper(String[] features) {
        this(features, null);
    }

    public LinkNeighborhoodWrapper(String[] features, StopList stoplist) {
        this.stoplist = stoplist;
        this.setFeatures(features);
    }

    public LinkNeighborhoodWrapper() {}

    public void setFeatures(String[][] fieldWords) {
        this.fieldWords = fieldWords;
    }

    private void setFeatures(String[] features) {
        String[][] fieldWords = new String[WordField.FIELD_NAMES.length][];

        List<String> aroundTemp = new ArrayList<String>();
        List<String> altTemp = new ArrayList<String>();
        List<String> srcTemp = new ArrayList<String>();
        List<String> urlTemp = new ArrayList<String>();
        List<String> anchorTemp = new ArrayList<String>();

        for (int i = 0; i < features.length; i++) {
            if (features[i].startsWith("around_")) {
                String[] parts = features[i].split("_");
                aroundTemp.add(parts[1]);
            }
            if (features[i].startsWith("alt_")) {
                String[] parts = features[i].split("_");
                altTemp.add(parts[1]);
            }
            if (features[i].startsWith("src_")) {
                String[] parts = features[i].split("_");
                srcTemp.add(parts[1]);
            }
            if (features[i].startsWith("url_")) {
                String[] parts = features[i].split("_");
                urlTemp.add(parts[1]);
            }
            if (features[i].startsWith("anchor_")) {
                String[] parts = features[i].split("_");
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
     * @throws MalformedURLException
     */
    public Map<String, Instance> extractLinks(LinkNeighborhood[] linkNeighboors, String[] features)
            throws MalformedURLException {
        Map<String, WordField[]> linkFields = extractLinks(linkNeighboors);
        return mapFeatures(linkFields, features);
    }

    public Instance extractToInstance(LinkNeighborhood linkNeighboor, String[] features)
            throws MalformedURLException {
        boolean useImageFeatures = false;
        String url = linkNeighboor.getLink().toString();
        WordField[] linkFields = extract(linkNeighboor, url, useImageFeatures);
        return mapFeaturesToInstance(features, linkFields);
    }

    public Instance extractToInstanceWithImageFeatures(LinkNeighborhood linkNeighboor,
            String[] features) throws MalformedURLException {
        boolean useImageFeatures = true;
        String url = linkNeighboor.getLink().toString();
        WordField[] linkFields = extract(linkNeighboor, url, useImageFeatures);
        return mapFeaturesToInstance(features, linkFields);
    }

    private Map<String, Instance> mapFeatures(Map<String, WordField[]> linkFields,
            String[] features) {
        Map<String, Instance> result = new HashMap<String, Instance>();
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
        for (int j = 0; j < words.length; j++) {
            WordField wordField = words[j];
            String field = (WordField.FIELD_NAMES[wordField.getField()]).toLowerCase();
            String word = wordField.getWord();
            if (wordField.getField() == WordField.URLFIELD || wordField.getField() == WordField.SRC) {
                List<String> wordsTemp = searchSubstring(wordField.getWord(), wordField.getField());
                for (int i = 0; i < wordsTemp.size(); i++) {
                    word = wordsTemp.get(i);
                    word = field + "_" + word;
                    instance.setValue(word, new Double(1));
                }
            } else {
                if (word != null) {
                    word = field + "_" + word;
                    instance.setValue(word, new Double(1));
                }
            }
        }
        return instance;
    }

    private String stemming(String word) {
        String new_word = "";
        try {
            new_word = stemmer.stem(word);
            if (new_word.indexOf("No term") != -1 || new_word.indexOf("Invalid term") != -1) {
                new_word = word;
            }
        } catch (Exception e) {
            new_word = word;
        }
        return new_word;
    }

    private List<String> searchSubstring(String word, int field) {
        List<String> result = new ArrayList<String>();
        String[] words = fieldWords[field];
        for (int i = 0; i < words.length; i++) {
            String tempWord = words[i];
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
        Map<String, WordField[]> result = new HashMap<String, WordField[]>();
        for (int i = 0; i < linkNeighboors.length; i++) {
            extractToMap(result, linkNeighboors[i]);
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
        List<WordField> words = new ArrayList<WordField>();
        if (useImageFeatures) {
            if (ln.getImgSrc() != null) {
                PaginaURL pageParser = new PaginaURL(new URL("http://"), ln.getImgSrc(), stoplist);
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
     * @throws MalformedURLException
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
        PaginaURL pageParser = new PaginaURL(url, url.getFile(), stoplist);
        String[] terms = pageParser.words();
        for (int i = 0; i < terms.length; i++) {
            wordsFields.add(new WordField(WordField.URLFIELD, stemming(terms[i])));
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
        for (int j = 0; j < textTerms.length; j++) {
            WordField wf = new WordField(type, stemming(textTerms[j]));
            words.add(wf);
        }
    }

}

