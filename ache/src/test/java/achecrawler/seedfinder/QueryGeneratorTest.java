package achecrawler.seedfinder;

import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;

import java.net.URL;

import org.junit.Test;

import achecrawler.seedfinder.QueryProcessor.QueryResult;
import achecrawler.target.model.Page;

public class QueryGeneratorTest {
    
    @Test
    public void shouldBuildNextQuery() throws Exception {
        // given
        double minimumPrecision = 0.25;
        QueryGenerator generator = new QueryGenerator(minimumPrecision);
        
        Query initialQuery = new Query("ebola");
        
        QueryResult queryResult = new QueryResult(0d);
        queryResult.positivePages.add(new Page(new URL("http://foo.com/p1"), "a page about ebola disease outbreak "));
        queryResult.positivePages.add(new Page(new URL("http://foo.com/p2"), "another page about ebola outreak "));
        queryResult.positivePages.add(new Page(new URL("http://foo.com/p2"), "some page about the african ebola outreak "));
        queryResult.negativePages.add(new Page(new URL("http://foo.com/n1"), "a page about something else"));
        queryResult.negativePages.add(new Page(new URL("http://foo.com/n2"), "another page about something else"));
        
        // when
        Query nextQuery = generator.buildNextQuery(initialQuery, queryResult);
        
        // then
        assertThat(nextQuery, is(notNullValue()));
        assertThat(nextQuery.getTerms().size(), is(greaterThan(0)));
    }

}
