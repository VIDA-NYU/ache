package focusedCrawler.util.parser;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Scanner;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import focusedCrawler.util.Page;

public class PaginaURLTest {

    public static final Logger logger = LoggerFactory.getLogger(PaginaURLTest.class);

    @Test
    public void linksShouldNotContainFragments() throws UnsupportedEncodingException {
        try {
            String testString = createTestPage();
            PaginaURL pageParser = new PaginaURL(new URL(
                    "http://www.w3schools.com/html/tryit.asp?filename=tryhtml_basic_document"),testString);

            Object[] extractedLinks = pageParser.links();
            assertEquals("Fragment detector test failed. ", false, hasFragments(extractedLinks));

        } catch (MalformedURLException e) {
            logger.error("URL of input file not in proper format.", e);
        }
    }
    
    @Test
    public void constructorsShouldWork() throws MalformedURLException {
        PaginaURL paginaURL = new PaginaURL(new URL(
                                "http://www.w3schools.com/html/tryit.asp?filename=tryhtml_basic_document"),
                                createTestPage());
                        assertEquals("Constructor not working properly !", false, paginaURL.getURL().equals(null));
    }

    private boolean hasFragments(Object[] urls) {
        for (Object url : urls) {
            if (url.toString().contains("#"))
                return true;
        }
        return false;
    }

    private String createTestPage() {
        StringBuffer testPage = new StringBuffer();

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
