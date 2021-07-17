package achecrawler.seedfinder;

import static java.util.Arrays.asList;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;

import java.io.IOException;
import java.util.List;

import org.junit.Test;

import com.sun.net.httpserver.HttpServer;

import achecrawler.crawler.async.TestWebServerBuilder;
import achecrawler.seedfinder.QueryProcessor.QueryResult;
import achecrawler.target.classifier.BodyRegexTargetClassifier;
import achecrawler.target.classifier.TargetClassifier;
import achecrawler.util.parser.BackLinkNeighborhood;

public class QueryProcessorTest {
    
    private SearchEngineApi searchEngineMock = new SearchEngineApi() {
        @Override
        public List<BackLinkNeighborhood> submitQuery(String query, int page) throws IOException {
            if (page == 0) {
                return asList(
                    new BackLinkNeighborhood("http://localhost:1234/1-pos.html", "ex1"),
                    new BackLinkNeighborhood("http://localhost:1234/1-neg.html", "ex1")
                );
            } else if (page == 1) {
                return asList(
                    new BackLinkNeighborhood("http://localhost:1234/2-pos.html", "ex2")
                );
            } else if (page == 2) {
                return asList(
                    new BackLinkNeighborhood("http://localhost:1234/3-neg.html", "ex3") 
                );
            }else {
                return null;
            }
        }
    };
    
    @Test
    public void shouldExecuteQuery() throws Exception {
        // given
        HttpServer httpServer = new TestWebServerBuilder("localhost", 1234)
                .with200OK("/1-pos.html", "Example page 1!")
                .with200OK("/1-neg.html", "Negative page 1!")
                .with200OK("/2-pos.html", "Example page 2!")
                .with200OK("/3-neg.html", "Page number 3!")
                .start();
        
        List<String> patterns = asList(".*example.*");
        TargetClassifier classifier = new BodyRegexTargetClassifier(patterns);
        
        QueryProcessor qp = new QueryProcessor(2, 0.25, classifier, searchEngineMock);
        Query query = new Query("example");
        
        // when
        QueryResult result = qp.processQuery(query);
        
        // then
        assertThat(result, is(notNullValue()));
        assertThat(result.positivePages.size(), is(2));
        assertThat(result.positivePages.get(0).getURL().toString(), is("http://localhost:1234/1-pos.html"));
        assertThat(result.positivePages.get(1).getURL().toString(), is("http://localhost:1234/2-pos.html"));
        
        assertThat(result.negativePages.size(), is(1));
        assertThat(result.negativePages.get(0).getURL().toString(), is("http://localhost:1234/1-neg.html"));
        
        // finally
        httpServer.stop(0);
    }
    
}
