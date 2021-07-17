package achecrawler.link.classifier.builder;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import achecrawler.util.parser.LinkNeighborhood;
import achecrawler.util.parser.PaginaURL;
import achecrawler.util.string.PorterStemmer;
import achecrawler.util.string.StopList;

public class LinkClassifierFeatureSelector {

    private final StopList stoplist;
    private final PorterStemmer stemmer;

    public LinkClassifierFeatureSelector(StopList stoplist) {
        this.stoplist = stoplist;
        this.stemmer = new PorterStemmer();
    }

    /**
     * This method selects the features to be used by the classifier.
     * 
     * @param allNeighbors
     * @param backlink
     * @return 
     * @return
     * @throws MalformedURLException
     */
    public Features selectBestFeatures(List<LinkNeighborhood> allNeighbors, boolean backlink)
            throws MalformedURLException {

        List<String> finalWords = new ArrayList<>();

        Set<String> usedURLTemp = new HashSet<>();

        Map<String, WordFrequency> urlWords = new HashMap<>();
        Map<String, WordFrequency> anchorWords = new HashMap<>();
        Map<String, WordFrequency> aroundWords = new HashMap<>();

        for (int l = 0; l < allNeighbors.size(); l++) {
            LinkNeighborhood element = allNeighbors.get(l);
            // anchor
            String[] anchorTemp = element.getAnchor();
            for (int j = 0; j < anchorTemp.length; j++) {
                String word = stemmer.stem(anchorTemp[j]);
                if (word == null || stoplist.isIrrelevant(word)) {
                    continue;
                }
                WordFrequency wf = anchorWords.get(word);
                if (wf != null) {
                    anchorWords.put(word, new WordFrequency(word, wf.getFrequency() + 1));
                } else {
                    anchorWords.put(word, new WordFrequency(word, 1));
                }
            }
            // around
            String[] aroundTemp = element.getAround();
            for (int j = 0; j < aroundTemp.length; j++) {
                String word = stemmer.stem(aroundTemp[j]);
                if (word == null || stoplist.isIrrelevant(word)) {
                    continue;
                }
                WordFrequency wf = aroundWords.get(word);
                if (wf != null) {
                    aroundWords.put(word, new WordFrequency(word, wf.getFrequency() + 1));
                } else {
                    aroundWords.put(word, new WordFrequency(word, 1));
                }
            }

            // url
            if (!usedURLTemp.contains(element.getLink().toString())) {
                usedURLTemp.add(element.getLink().toString());
                PaginaURL pageParser = new PaginaURL(new URL("http://"), element.getLink().getFile().toString(), stoplist);
                String[] urlTemp = pageParser.words();
                for (int j = 0; j < urlTemp.length; j++) {
                    // String word = stemmer.stem(urlTemp[j]);
                    String word = urlTemp[j];
                    if (stoplist.isIrrelevant(word)) {
                        continue;
                    }
                    WordFrequency wf = (WordFrequency) urlWords.get(word);
                    if (wf != null) {
                        urlWords.put(word, new WordFrequency(word, wf.getFrequency() + 1));
                    } else {
                        urlWords.put(word, new WordFrequency(word, 1));
                    }
                }
            }
        }

        String[][] fieldWords = new String[WordField.FIELD_NAMES.length][];

        List<WordFrequency> aroundVector = new ArrayList<>(aroundWords.values());
        Collections.sort(aroundVector, WordFrequency.WORD_FREQUENCY_DESC_COMPARATOR);
        FilterData filterData1 = new FilterData(100, 2);
        List<WordFrequency> aroundFinal = filterData1.filter(aroundVector, null);
        String[] aroundTemp = new String[aroundFinal.size()];

        for (int i = 0; i < aroundFinal.size(); i++) {
            WordFrequency wf = aroundFinal.get(i);
            finalWords.add("around_" + wf.getWord());
            aroundTemp[i] = wf.getWord();
        }
        fieldWords[WordField.AROUND] = aroundTemp;


        List<WordFrequency> urlVector = new ArrayList<>(urlWords.values());
        Collections.sort(urlVector, WordFrequency.WORD_FREQUENCY_DESC_COMPARATOR);
        FilterData filterData2 = new FilterData(150, 2);
        List<WordFrequency> urlFinal = filterData2.filter(urlVector, new ArrayList<>(aroundFinal));
        String[] urlTemp = new String[urlFinal.size()];

        for (int i = 0; i < urlTemp.length; i++) {
            WordFrequency wf = urlFinal.get(i);
            finalWords.add("url_" + wf.getWord());
            urlTemp[i] = wf.getWord();
        }
        fieldWords[WordField.URLFIELD] = urlTemp;

        if (!backlink) {
            List<WordFrequency> anchorVector = new ArrayList<>(anchorWords.values());
            Collections.sort(anchorVector, WordFrequency.WORD_FREQUENCY_DESC_COMPARATOR);
            FilterData filterData3 = new FilterData(150, 2);
            List<WordFrequency> anchorFinal = filterData3.filter(anchorVector, null);
            String[] anchorTemp = new String[anchorFinal.size()];

            for (int i = 0; i < anchorFinal.size(); i++) {
                WordFrequency wf = anchorFinal.get(i);
                finalWords.add("anchor_" + wf.getWord());
                anchorTemp[i] = wf.getWord();
            }
            fieldWords[WordField.ANCHOR] = anchorTemp;
        }

        String[] features = (String[]) finalWords.toArray(new String[finalWords.size()]);
        
        return new Features(fieldWords, features);
    }
    
    
    public class Features {
        
        public final String[][] fieldWords;
        public final String[] features;

        public Features(String[][] fieldWords, String[] features) {
            this.fieldWords = fieldWords;
            this.features = features;
        }

    }

}
