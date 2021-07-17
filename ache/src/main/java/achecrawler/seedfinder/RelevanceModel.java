package achecrawler.seedfinder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import achecrawler.target.model.Page;
import achecrawler.target.model.ParsedData;
import achecrawler.util.parser.PaginaURL;
import achecrawler.util.string.StopListFile;

public class RelevanceModel {
    
    
    private Map<String, Double> termScores = new HashMap<>();
    
    public void addPage(boolean isRelevant, Page page) {
        PaginaURL pageParser = new PaginaURL(page.getURL(), page.getContentAsString(), StopListFile.DEFAULT);
        page.setParsedData(new ParsedData(pageParser));
        String[] terms = page.getParsedData().getWords();
        List<String> words = new ArrayList<String>();
        for (int i = 0; i < terms.length; i++) {
            if(terms[i] == null || terms[i].trim().length() < 2) {
                continue;
            }
            words.add(terms[i]);
        }
        this.addPage(isRelevant, (String[]) words.toArray(new String[words.size()]));
    }
    
    public void addPage(boolean isRelevant, String[] docTerms) {
        if(docTerms.length == 0) {
            return;
        }
        
        Map<String, Integer> termFrequencies = countTerms(docTerms);
        
        for(String term : docTerms) {
            
            if(term == null || term.trim().length() < 2) {
                continue;
            }
            
            int tf = termFrequencies.get(term);
            
            Double termScore = termScores.get(term);
            if(termScore == null) {
                termScore = 0d;
            }
            
            double newScore;
            if(isRelevant) {
                newScore = termScore + ( ((double) tf) / docTerms.length);
            } else {
                newScore = termScore - ( ((double) tf) / docTerms.length);
            }
            
            termScores.put(term, newScore);
        }
    }
    
    private Map<String, Integer> countTerms(String[] docTerms) {
        Map<String, Integer> counts = new HashMap<String, Integer>();
        for(int i = 0;  i < docTerms.length; i++) {
            if(docTerms[i] == null || docTerms[i].trim().length() < 2) {
                continue;
            }
            Integer count = counts.get(docTerms[i]);
            if(count == null) {
                count = 1;
            } else {
                count++;
            }
            counts.put(docTerms[i], count);
        }
        return counts;
    }
    
    double getTermScore(String term) {
        return termScores.get(term);
    }
    
    public double reweightScore(String term, double queryPrecision) {
        if(!termScores.containsKey(term)) {
            termScores.put(term, 1d/100);
        }
        double oldScore = termScores.get(term);
        double newScore = oldScore * queryPrecision;
        termScores.put(term, newScore);
        return newScore;
    }
    
    public List<QueryTerm> getTermsWithBestScores(int size) {
        this.termScores = sortByValueInDescendingOrder(this.termScores);
        List<QueryTerm> terms = new ArrayList<QueryTerm>();
        for (Entry<String, Double> termScore : this.termScores.entrySet()) {
            terms.add(new QueryTerm(termScore.getKey(), termScore.getValue()));
            if(terms.size() > size) {
                break;
            }
        }
        return terms;
    }

    public QueryTerm getTermsWithBestScore() {
        termScores = sortByValueInDescendingOrder(termScores);
        printTermScoresMap();
        Entry<String, Double> first = termScores.entrySet().iterator().next();
        QueryTerm queryTerm = new QueryTerm(first.getKey(), first.getValue());
        return queryTerm;
    }
    
    private void printTermScoresMap() {
        for(Entry<String, Double> entry : termScores.entrySet()) {
            System.out.println(entry.getKey() + " - " + entry.getValue());   
        }
        
    }
    public QueryTerm getTermWithBestScoreExcept(Set<String> exceptions) {
        termScores = sortByValueInDescendingOrder(termScores);
        for(Entry<String, Double> ts : termScores.entrySet()) {
            if(!exceptions.contains(ts.getKey())) {
                return new QueryTerm(ts.getKey(), ts.getValue());
            }
//            else {
//                System.out.println(ts.getKey() + " is an exception.");
//            }
        }
        return null;
    }
    
    public static <K, V extends Comparable<? super V>> Map<K, V> sortByValueInDescendingOrder(Map<K, V> map) {
        List<Map.Entry<K, V>> list = new LinkedList<Map.Entry<K, V>>(map.entrySet());
        Collections.sort(list, new Comparator<Map.Entry<K, V>>() {
            public int compare(Map.Entry<K, V> o1, Map.Entry<K, V> o2) {
                return (o2.getValue()).compareTo(o1.getValue());
            }
        });

        Map<K, V> result = new LinkedHashMap<K, V>();
        for (Map.Entry<K, V> entry : list) {
            result.put(entry.getKey(), entry.getValue());
        }
        return result;
    }
    
}