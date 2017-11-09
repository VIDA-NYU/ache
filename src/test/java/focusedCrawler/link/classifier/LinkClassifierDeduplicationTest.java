package focusedCrawler.link.classifier;

import static org.junit.Assert.assertTrue;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;

import org.junit.Test;

import focusedCrawler.link.frontier.LinkRelevance;
import focusedCrawler.target.model.Page;
import focusedCrawler.target.model.ParsedData;
import focusedCrawler.util.parser.PaginaURL;

public class LinkClassifierDeduplicationTest {

    @Test
    public void shouldTrainClassifierOnline() throws Exception {
        LinkClassifierDeduplication dedup = new LinkClassifierDeduplication();

        Page page1 = createPage("http://example.com", createTestPage("example"));
        Page pageL1 = createPage("http://example.com/comment/1", createTestPage("login"));
        Page pageL2 = createPage("http://example.com/comment/2", createTestPage("login"));
        Page pageL3 = createPage("http://example.com/comment/3", createTestPage("login"));
        Page page2 = createPage("http://example2.com", pageWithLink("http://example.com/comment/4"));
        Page page3 = createPage("http://example3.com", pageWithLink("http://example.com/asdf"));

        dedup.classify(page1);
        dedup.classify(pageL1);
        dedup.classify(pageL2);
        dedup.classify(pageL3);

        LinkRelevance[] ln2 = dedup.classify(page2);
        System.err.println(Arrays.deepToString(ln2));

        LinkRelevance[] ln3 = dedup.classify(page3);
        System.err.println(Arrays.deepToString(ln3));

        assertTrue(ln3[0].getRelevance() > ln2[0].getRelevance());
    }


    private Page createPage(String url, String p1) throws MalformedURLException {
        Page page = new Page(new URL(url), p1);
        page.setParsedData(new ParsedData(new PaginaURL(page)));
        return page;
    }


    private String createTestPage(String content) {
        StringBuilder testPage = new StringBuilder();
        testPage.append("<!DOCTYPE html>");
        testPage.append("<html>");
        testPage.append("<body>");
        testPage.append("<h1>");
        testPage.append(content);
        testPage.append("</h1>");
        testPage.append("</body>");
        testPage.append("</html>");
        return testPage.toString();
    }

    private String pageWithLink(String link) {
        StringBuilder testPage = new StringBuilder();
        testPage.append("<!DOCTYPE html>");
        testPage.append("<html>");
        testPage.append("<body>");
        testPage.append("<h1>My First Heading</h1>");
        testPage.append("<a href = \"");
        testPage.append(link);
        testPage.append("\">My first paragraph.</a>");
        testPage.append("</body>");
        testPage.append("</html>");
        return testPage.toString();
    }

}
