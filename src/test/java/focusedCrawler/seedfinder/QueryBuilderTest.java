package focusedCrawler.seedfinder;

import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;

import org.junit.Test;

import focusedCrawler.seedfinder.Query;
import focusedCrawler.seedfinder.QueryBuilder;
import focusedCrawler.seedfinder.QueryProcessor.QueryResult;

public class QueryBuilderTest {
    
    @Test
    public void shouldBuildNextQuery() throws Exception {
        // given
        double minimumPrecision = 0.25;
        QueryBuilder qb = new QueryBuilder(minimumPrecision);
        
        String[] queryTerms = new String[]{"ebola", "outbreak"};
        double[] queryWeights = new double[]{0.5, 0.5};
        
        Query query = new Query(queryTerms, queryWeights);
        QueryResult queryResult = new QueryResult(0d);
        
        // when
        Query nextQuery = qb.buildNextQuery(query, queryResult);
        
        // then
        assertThat(nextQuery, is(notNullValue()));
        assertThat(nextQuery.getTerms().size(), is(greaterThan(0)));
    }

}
