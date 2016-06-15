package focusedCrawler.util.parser;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

import java.net.MalformedURLException;
import java.net.URL;

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
        PaginaURL pageParser = new PaginaURL(new URL("http://ex.com/index.html"),testString);
        URL[] extractedLinks = pageParser.links();
        LinkNeighborhood[] neighborhood = pageParser.getLinkNeighboor();

        // then
        assertThat(extractedLinks[0].toString(), is("http://ex.com/index.php?p1=asdf&p2=qwer"));
        assertThat(neighborhood[0].getLink().toString(), is("http://ex.com/index.php?p1=asdf&p2=qwer"));
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
        assertEquals("Extracted URL contains fragment.", false, hasFragments(extractedLinks));
    }

    
    @Test
    public void constructorsShouldWork() throws MalformedURLException {
        // given
        URL url = new URL("http://www.w3schools.com/html/tryit.asp?filename=tryhtml_basic_document");
        String testPage = createTestPage();
        // when
        PaginaURL paginaURL = new PaginaURL(url, testPage);
        // then
        assertEquals("Constructor not working properly !", false, paginaURL.getURL().equals(null));
    }

    private boolean hasFragments(URL[] urls) {
        for (URL url : urls) {
            if (url.getFile().toString().contains("#"))
                return true;
        }
        return false;
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
}
