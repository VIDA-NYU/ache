package achecrawler.seedfinder;

import java.net.URL;

import achecrawler.seedfinder.QueryProcessor.QueryResult;

import org.junit.jupiter.api.Test;
import achecrawler.target.model.Page;

import static org.assertj.core.api.Assertions.assertThat;

class QueryGeneratorTest {

    @Test
    void shouldBuildNextQuery() throws Exception {
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
        assertThat(nextQuery).isNotNull();
        assertThat(nextQuery.getTerms().size()).isGreaterThan(0);
    }

}
