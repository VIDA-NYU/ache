package achecrawler.util.parser;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;

import org.junit.Test;

public class PaginaURLTest {

    @Test
    public void htmlEncodedLinksShouldBeEscaped() throws Exception {
        // given
        StringBuilder testPage = new StringBuilder();
        testPage.append("<!DOCTYPE html>");
        testPage.append("<html>");
        testPage.append("<body>");
        testPage.append("<a href = \"http://ex.com/index.php?p1=asdf&amp;p2=qwer\">Anchor text.</a>");
        testPage.append("</body>");
        testPage.append("</html>");
        String testString = testPage.toString();
        
        // when
        PaginaURL pageParser = new PaginaURL(null, testString);
        URL[] extractedLinks = pageParser.links();
        LinkNeighborhood[] neighborhood = pageParser.getLinkNeighboor();

        // then
        assertThat(extractedLinks.length, is(1));
        assertThat(extractedLinks[0].toString(), is("http://ex.com/index.php?p1=asdf&p2=qwer"));
        assertThat(neighborhood[0].getLink().toString(),
                is("http://ex.com/index.php?p1=asdf&p2=qwer"));
    }

    /*
     * Test for issue #141 (https://github.com/ViDA-NYU/ache/issues/141), in which the string "%2F"
     * is appended in the end of the URL.
     */
    @Test
    public void testIssue141() throws Exception {
        // given
        StringBuilder testPage = new StringBuilder();
        testPage.append("<!DOCTYPE html>");
        testPage.append("<html>");
        testPage.append("<body>");
        testPage.append("<a href = \"?widget=sidebar-shared\">Anchor text.</a>");
        testPage.append("</body>");
        testPage.append("</html>");
        String testString = testPage.toString();

        // when
        PaginaURL pageParser = new PaginaURL(
                new URL("http://www.example.com/most-shared/?widget=sidebar-primary-most_shared"),
                testString);
        URL[] extractedLinks = pageParser.links();
        LinkNeighborhood[] neighborhood = pageParser.getLinkNeighboor();

        // then
        assertThat(extractedLinks.length, is(1));
        assertThat(extractedLinks[0].toString(),
                is("http://www.example.com/most-shared/?widget=sidebar-shared"));
        assertThat(neighborhood[0].getLink().toString(),
                is("http://www.example.com/most-shared/?widget=sidebar-shared"));
    }

    @Test
    public void shouldIgnoreNonHttpLinks() throws Exception {
        // given
        StringBuilder testPage = new StringBuilder();
        testPage.append("<!DOCTYPE html>");
        testPage.append("<html>");
        testPage.append("<body>");
        testPage.append("<a href = \"mailto:someone@example.com\">Mail me!</a>");
        testPage.append("<a href = \"ftp:example.com/file.txt\">Some File!</a>");
        testPage.append("</body>");
        testPage.append("</html>");
        String testString = testPage.toString();

        // when
        PaginaURL pageParser = new PaginaURL(new URL("http://ex.com/index.html"), testString);
        URL[] extractedLinks = pageParser.links();
        LinkNeighborhood[] neighborhood = pageParser.getLinkNeighboor();

        // then
        assertThat(extractedLinks.length, is(0));
        assertThat(neighborhood.length, is(0));
    }

    @Test
    public void linksShouldNotContainFragments() throws Exception {
        // given
        String testString = createTestPage();
        URL url = new URL("http://www.w3schools.com/html/tryit.asp?filename=tryhtml_basic_document");
        // when
        PaginaURL pageParser = new PaginaURL(url,testString);
        URL[] extractedLinks = pageParser.links();
        // then
        for(URL extractedUrl : Arrays.asList(extractedLinks)) {
        	assertThat(extractedUrl.getFile().toString(), not(containsString("#")));
        }
    }
    
    @Test
    public void constructorsShouldWork() throws MalformedURLException {
        // given
        URL url = new URL("http://www.w3schools.com/html/tryit.asp?filename=tryhtml_basic_document");
        String testPage = createTestPage();
        // when
        PaginaURL paginaURL = new PaginaURL(url, testPage);
        // then
        assertThat(paginaURL.getURL(), is(notNullValue()));
    }
    
    @Test
    public void shouldNotExtractInvalidLinks() throws MalformedURLException {
        // given
        URL url = new URL("http://example.com/test.html");
        StringBuilder testPage = new StringBuilder();
        testPage.append("<!DOCTYPE html>");
        testPage.append("<html>");
        testPage.append("<body>");
        testPage.append("<h1>My First Heading</h1>");
        testPage.append("<a href = \"http://None/\">link 0</a>");
        testPage.append("<a href = \"http://12324/\">link 1</a>");
        testPage.append("<a href = \"/asdf.html\">link 2</a>");
        testPage.append("</body>");
        testPage.append("</html>");
        
        // when
        PaginaURL paginaURL = new PaginaURL(url, testPage.toString());
        URL[] links = paginaURL.links();
        LinkNeighborhood[] lns  = paginaURL.getLinkNeighboor();
        
        // then
        assertThat(links.length, is(1));
        assertThat(links[0].toString(), is("http://example.com/asdf.html"));

        assertThat(lns.length, is(1));
        assertThat(lns[0].getLink().toString(), is("http://example.com/asdf.html"));
    }
    
    @Test
    public void shouldExtractAnchoTextAndTextAroundLink() throws MalformedURLException {
        // given
        URL url = new URL("http://www.w3schools.com/html/tryit.asp?filename=tryhtml_basic_document");
        String testPage = createTestPage();
        // when
        PaginaURL paginaURL = new PaginaURL(url, testPage);
        LinkNeighborhood[] neighborhoods = paginaURL.getLinkNeighboor();
        // then
        assertThat(neighborhoods.length, is(1));
        
        assertThat(neighborhoods[0].getAroundString().trim(), is("my first heading"));
        assertThat(neighborhoods[0].getAround()[0], is("my"));
        assertThat(neighborhoods[0].getAround()[1], is("first"));
        assertThat(neighborhoods[0].getAround()[2], is("heading"));
        
        assertThat(neighborhoods[0].getAnchorString().trim(), is("my first paragraph"));
        assertThat(neighborhoods[0].getAnchor()[0], is("my"));
        assertThat(neighborhoods[0].getAnchor()[1], is("first"));
        assertThat(neighborhoods[0].getAnchor()[2], is("paragraph"));
    }
    
    @Test
    public void shouldNormalizeLinks() throws MalformedURLException {
        // given
        URL url = new URL("http://www.w3schools.com/html/tryit.asp?filename=tryhtml_basic_document");
        String testPage = createTestPageUnormalizedLinks();
        // when
        PaginaURL paginaURL = new PaginaURL(url, testPage);
        LinkNeighborhood[] neighborhoods = paginaURL.getLinkNeighboor();
        URL[] links = paginaURL.links();

        // then
        assertThat(neighborhoods.length, is(3));
        assertThat(links.length, is(3));
        
        assertThat(neighborhoods[0].getLink().toString(), is("http://example.com/post.php?"));
        assertThat(links[0].toString(), is("http://example.com/post.php?"));
        
        assertThat(neighborhoods[1].getLink().toString(), is("http://example.com/post.php?a=1&b=2"));
        assertThat(links[1].toString(), is("http://example.com/post.php?a=1&b=2"));

        assertThat(neighborhoods[2].getLink().toString(), is("http://example.com/"));
        assertThat(links[2].toString(), is("http://example.com/"));
    }

    @Test
    public void shouldExtractLinksWithRecentAndOnion() throws MalformedURLException {
        // given
        URL url = new URL("http://example.com/test.html");
        StringBuilder testPage = new StringBuilder();
        testPage.append("<!DOCTYPE html>");
        testPage.append("<html>");
        testPage.append("<body>");
        testPage.append("<h1>My First Heading</h1>");
        testPage.append("<a href = \"http://registry.africa/\">Link with recent TLDs</a>");
        testPage.append("<a href = \"http://3g2upl4pq6kufc4m.onion/\">Onion Link</a>");
        testPage.append("</body>");
        testPage.append("</html>");

        // when
        PaginaURL paginaURL = new PaginaURL(url, testPage.toString());
        URL[] links = paginaURL.links();

        // then
        assertThat(links.length, is(2));
        assertThat(links[0].toString(), is("http://registry.africa/"));
        assertThat(links[1].toString(), is("http://3g2upl4pq6kufc4m.onion/"));
    }

    private String createTestPage() {
        StringBuilder testPage = new StringBuilder();
        testPage.append("<!DOCTYPE html>");
        testPage.append("<html>");
        testPage.append("<body>");
        testPage.append("<h1>My First Heading</h1>");
        testPage.append("<a href = \"https://en.wikipedia.org/wiki/Mouse_(computing)#Mechanical_mice\">My first paragraph.</a>");
        testPage.append("</body>");
        testPage.append("</html>");
        return testPage.toString();
    }
    
    private String createTestPageUnormalizedLinks() {
        StringBuilder testPage = new StringBuilder();
        testPage.append("<!DOCTYPE html>");
        testPage.append("<html>");
        testPage.append("<body>");
        testPage.append("<h1>My First Heading</h1>");
        testPage.append("<a href = \"http://Example.com:80/post.php?\">Link 1.</a>");
        testPage.append("<a href = \"HTTP://EXAMPLE.com/post.php?b=2&a=1\">Link 2.</a>");
        testPage.append("<a href = \"HTTP://EXAMPLE.com\">Link 3.</a>");
        testPage.append("</body>");
        testPage.append("</html>");
        return testPage.toString();
    }
}
