package focusedCrawler.seedfinder;

import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;

import java.net.URL;

import org.junit.Test;

import focusedCrawler.seedfinder.Query;
import focusedCrawler.seedfinder.QueryBuilder;
import focusedCrawler.seedfinder.QueryProcessor.QueryResult;
import focusedCrawler.target.model.Page;

public class QueryBuilderTest {
    
    @Test
    public void shouldBuildNextQuery() throws Exception {
        // given
        double minimumPrecision = 0.25;
        QueryBuilder qb = new QueryBuilder(minimumPrecision);
        
        Query initialQuery = new Query("ebola");
        
        QueryResult queryResult = new QueryResult(0d);
        queryResult.positivePages.add(new Page(new URL("http://foo.com/p1"), "a page about ebola disease outbreak "));
        queryResult.positivePages.add(new Page(new URL("http://foo.com/p2"), "another page about ebola outreak "));
        queryResult.positivePages.add(new Page(new URL("http://foo.com/p2"), "some page about the african ebola outreak "));
        queryResult.negativePages.add(new Page(new URL("http://foo.com/n1"), "a page about something else"));
        queryResult.negativePages.add(new Page(new URL("http://foo.com/n2"), "another page about something else"));
        
        // when
        Query nextQuery = qb.buildNextQuery(initialQuery, queryResult);
        
        // then
        assertThat(nextQuery, is(notNullValue()));
        assertThat(nextQuery.getTerms().size(), is(greaterThan(0)));
    }

}
