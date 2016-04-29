package focusedCrawler.seedfinder;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import focusedCrawler.seedfinder.QueryProcessor.QueryResult;
import focusedCrawler.target.model.Page;

public class QueryBuilder {
    
    private double minimumPrecision = 0.5d;
    Set<String> queryTermsUsed = new HashSet<>();
    
    RelevanceModel relevanceModel = new RelevanceModel();
    
    public QueryBuilder(double minimumPrecision) {
        this.minimumPrecision = minimumPrecision;
    }

    public Query buildNextQuery(Query query, QueryResult queryResult) {
        // keep track of all terms used
        queryTermsUsed.addAll(query.termsSet());
        
        // 1. compute term scores for terms contained in all positive and negative documents
        System.out.println("Precision:"+queryResult.precision());
        if(queryResult.precision() > minimumPrecision) {
            System.out.println("QueryBuilder.buildNextQuery() positive:"+queryResult.positivePages.size());
            for (Page page : queryResult.positivePages) {
                relevanceModel.addPage(true, page);
            }
            System.out.println("QueryBuilder.buildNextQuery() negative:"+queryResult.negativePages.size());
            for (Page page : queryResult.negativePages) {
                relevanceModel.addPage(false, page);
            }
        }
        
        // 2. re-weight query scores of all terms of the query
        for (QueryTerm t : query.getTerms()) {
            relevanceModel.updateScore(t.term, queryResult.precision());
        }
        
        // 3. create new query
        int querySize = query.getTerms().size();
        if(queryResult.precision() < minimumPrecision) {
            querySize++;   
        } else {
            if(queryResult.percentNewResults < minimumPrecision) {
                querySize--;
            }
        }
        
        Query newQuery = new Query();
        
        List<QueryTerm> bestTerms = relevanceModel.getTermsWithBestScores(querySize-1);
        newQuery.addTerms(bestTerms);
        for (QueryTerm term : bestTerms) System.out.println("bestTerm: "+term.toString());
        queryTermsUsed.addAll(newQuery.termsSet());
        
        QueryTerm unusedTerm = relevanceModel.getTermWithBestScoreExcept(queryTermsUsed);
        System.out.println("unusedTerm: "+unusedTerm.toString());
        newQuery.addTerm(unusedTerm);
        
        return newQuery;
    }

}
