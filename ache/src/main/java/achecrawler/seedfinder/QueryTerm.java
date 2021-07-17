package achecrawler.seedfinder;

import java.util.Comparator;

public class QueryTerm {
    
    public static Comparator<QueryTerm> COMPARATOR = new Comparator<QueryTerm>() {
        @Override
        public int compare(QueryTerm o1, QueryTerm o2) {
            return Double.compare(o2.score, o1.score);
        }
    };
    
    final String term;
    final double score;
    
    public QueryTerm(String term, double score) {
        this.term = term;
        this.score = score;
    }
    
    @Override
    public String toString() {
        return String.format("{%s,%.5f}", term, score);
    }
}