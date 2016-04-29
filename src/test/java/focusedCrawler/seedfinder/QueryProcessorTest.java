package focusedCrawler.seedfinder;

import static java.util.Arrays.asList;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;

import java.util.List;

import org.junit.Test;

import focusedCrawler.seedfinder.Query;
import focusedCrawler.seedfinder.QueryProcessor;
import focusedCrawler.seedfinder.QueryProcessor.QueryResult;
import focusedCrawler.target.classifier.BodyRegexTargetClassifier;
import focusedCrawler.target.classifier.TargetClassifier;

public class QueryProcessorTest {
    
    @Test
    public void shouldExecuteQuery() throws Exception {
        // given
        List<String> patterns = asList(".*ebola.*");
        TargetClassifier classifier = new BodyRegexTargetClassifier(patterns);
        
        QueryProcessor qp = new QueryProcessor(3, 0.25, classifier);
        Query query = new Query("ebola");
        
        // when
        QueryResult result = qp.processQuery(query);
        
        // then
        assertThat(result, is(notNullValue()));
        assertThat(result.positivePages.size(), is(greaterThan(0)));
    }
    
}
