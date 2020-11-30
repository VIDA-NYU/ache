package achecrawler.seedfinder;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Query {

    private List<QueryTerm> terms = new ArrayList<QueryTerm>();
    
    public Query(String[] terms, double[] weights) {
        if(terms.length != weights.length) {
            throw new IllegalArgumentException("terms and weights vectors should have same size.");
        }
        for (int i = 0; i < weights.length; i++) {
            this.terms.add(new QueryTerm(terms[i], weights[i]));
        }
    }

    public Query(List<QueryTerm> terms) {
        this.terms = terms;
    }
    
    public Query(String query) {
        this(query.split("\\s"));
    }

    public Query(String... terms) {
        for (int i = 0; i < terms.length; i++) {
            this.terms.add(new QueryTerm(terms[i], 0));
        }
    }
    
    public void addTerm(String term, double score) {
        addTerm(new QueryTerm(term, score));
    }
    
    public void addTerm(QueryTerm term) {
        this.terms.add(term);
    }
    
    public void addTerms(List<QueryTerm> terms) {
        this.terms.addAll(terms);
    }
    
    public List<QueryTerm> getTerms() {
        return this.terms;
    }
    
    public String asString() {
        if(terms == null || terms.isEmpty()) {
            return "";
        }
        StringBuilder q = new StringBuilder();
        for(QueryTerm term : terms) {
            q.append(term.term);
            q.append(" ");
        }
        try {
            return URLEncoder.encode(q.toString().trim(), "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException("Oops! Unsupported enconding.");
        }
    }
    
    public String explain() {
        if(terms == null || terms.isEmpty()) {
            return "";
        }
        StringBuilder q = new StringBuilder();
        for(QueryTerm term : terms) {
            q.append(term.score);
            q.append(" ");
            q.append(term.term);
            q.append("\n");
        }
        return q.toString();
    }

    public void sortByScore() {
        Collections.sort(this.terms, QueryTerm.COMPARATOR);
    }

    public void dropLast() {
        terms.remove(terms.size()-1);
    }

    public Set<String> termsSet() {
        Set<String> termsSet = new HashSet<String>();
        for (QueryTerm t : this.terms) {
            termsSet.add(t.term);
        }
        return termsSet;
    }

}
