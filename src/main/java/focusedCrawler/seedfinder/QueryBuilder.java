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
        System.out.println("Building next query...");
        System.out.println("Initial query: "+query.asString());

        // keep track of all terms used
        queryTermsUsed.addAll(query.termsSet());
        
        //
        // 1. compute term scores for terms contained in all positive and negative documents
        //
        // (this 'if' exists in the original implementation, but is not described in the paper)
        if(queryResult.precision() > minimumPrecision) {
            for (Page page : queryResult.positivePages) {
                relevanceModel.addPage(true, page);
            }
            for (Page page : queryResult.negativePages) {
                relevanceModel.addPage(false, page);
            }
        }
        
        //
        // 2. re-weight term scores of all terms of the query
        //
        for (QueryTerm t : query.getTerms()) {
            relevanceModel.reweightScore(t.term, queryResult.precision());
        }
        
        //
        // 3. create new query
        //
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
        System.out.println("New query: "+newQuery.asString());
        return newQuery;
    }

}
