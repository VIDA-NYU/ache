package achecrawler.util.parser;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;

import java.net.MalformedURLException;
import java.net.URL;

import org.junit.Test;

public class HtmlSaxParserTest {

    @Test
    public void shouldExtractTitle() {
        // given
        String testString = new HtmlBuilder()
                .withHeader("<title>ACHE Crawler \n \t</title>")
                .withBody("<p>My text</p>")
                .build();

        // when
        HtmlSaxParser pageParser = new HtmlSaxParser("http://ex.com/index.html", testString);

        // then
        assertThat(pageParser.title().trim(), is("ACHE Crawler"));
    }

    @Test
    public void shouldCleanHtmlEntities() {
        // given
        String testString = new HtmlBuilder()
                .withHeader("<title>ACHE &gt; domain specific search &#169;</title>")
                .withBody("<p>My&nbsp;text &amp; me. &euro;</p>")
                .build();

        // when
        HtmlSaxParser pageParser = new HtmlSaxParser("http://ex.com/index.html", testString);

        // then
        assertThat(pageParser.title(), is("ACHE > domain specific search ©"));
        assertThat(pageParser.text().trim(), is("My\u00A0text & me. €"));
    }

    @Test
    public void htmlEncodedLinksShouldBeEscaped() {
        // given
        String testString = new HtmlBuilder()
                .withBody("<a href=\"http://ex.com/index.php?p1=asdf&amp;p2=qwer\">Anchor text.</a>")
                .build();

        // when
        HtmlSaxParser pageParser = new HtmlSaxParser("http://ex.com/index.html", testString);
        URL[] extractedLinks = pageParser.links();
        LinkNeighborhood[] neighborhood = pageParser.getLinkNeighborhood();

        // then
        assertThat(extractedLinks[0].toString(), is("http://ex.com/index.php?p1=asdf&p2=qwer"));
        assertThat(neighborhood[0].getLink().toString(), is("http://ex.com/index.php?p1=asdf&p2=qwer"));
    }

    @Test
    public void linksShouldNotContainFragments() throws MalformedURLException {
        // given
        String testString = new HtmlBuilder()
                .appendToBody("<h1>My First Heading</h1>")
                .appendToBody("<a href=\"https://en.wikipedia.org/wiki/Mouse_(computing)#Mechanical_mice\">Mouse</a>")
                .build();
        URL url = new URL("http://www.w3schools.com/html/tryit.asp?filename=tryhtml_basic_document");

        // when
        HtmlSaxParser pageParser = new HtmlSaxParser(url, testString);
        URL[] extractedLinks = pageParser.links();

        // then
        assertThat(extractedLinks.length, is(1));
        assertThat(extractedLinks[0].toString(), is("https://en.wikipedia.org/wiki/Mouse_(computing)"));
    }

    @Test
    public void constructorsShouldWork() throws MalformedURLException {
        // given
        URL url = new URL("http://www.w3schools.com/html/tryit.asp?filename=tryhtml_basic_document");
        String testPage = createTestPage();
        // when
        HtmlSaxParser pageParser = new HtmlSaxParser(url, testPage);
        // then
        assertThat(pageParser.getURL(), is(notNullValue()));
    }

    @Test
    public void shouldExtractOnionLinks() throws MalformedURLException {
        // given
        URL url = new URL("http://example.com/test.html");
        String testPage = new HtmlBuilder()
                .appendToBody("<a href = \"http://3g2asl4qw6kufc5m.onion/\">link 1</a>")
                .appendToBody("<a href = \"http://3g2asl4qw6kufc5m.onion/test.html\">link 1</a>")
                .build();
        // when
        HtmlSaxParser pageParser = new HtmlSaxParser(url, testPage.toString());
        URL[] links = pageParser.links();

        // then
        assertThat(links.length, is(2));
        assertThat(links[0].toString(), is("http://3g2asl4qw6kufc5m.onion/"));
        assertThat(links[1].toString(), is("http://3g2asl4qw6kufc5m.onion/test.html"));
    }

//    @Test
//    public void shouldParseText() throws MalformedURLException {
//        // given
//        URL url = new URL("http://example.com/");
//        StringBuilder testPage = new StringBuilder();
//        testPage.append("<!DOCTYPE html>");
//        testPage.append("<html>");
//        testPage.append("<body>");
//        testPage.append("<p>My First paragraph. My second second paragraph.</p>");
//        testPage.append("</body>");
//        testPage.append("</html>");
//
//        // when
//        HtmlSaxParser pageParser = new HtmlSaxParser(url, testPage.toString());
//        String[] links = pageParser.palavras();
//        int[] ocorrencias = pageParser.ocorrencias();
//        System.out.println(testPage.toString());
//        System.out.println(Arrays.deepToString(links));
//        System.out.println(Ints.asList(ocorrencias));
////        // then
////        assertThat(links.length, is(1));
////        assertThat(links[0].toString(), is("http://example.com/asdf.html"));
////
////        assertThat(lns.length, is(1));
////        assertThat(lns[0].getLink().toString(), is("http://example.com/asdf.html"));
//    }

    @Test
    public void shouldExtractAnchorTextAndTextAroundLink() throws MalformedURLException {
        // given
        String url = "http://www.example.com";
        String testPage = HtmlBuilder.newBuilder()
                .appendToBody("<p>My First Heading</p>")
                .appendToBody("<a href=\"http://example.com/about.html\">My first anchor text.</a>")
//                .appendToBody("<a href=\"http://example.com/about.html\">my second anchor text.</a>")
                .appendToBody("<p>my paragraph.</p>")
                .appendToBody("free text")
                .build();
        // when
        HtmlSaxParser pageParser = new HtmlSaxParser(url, testPage);
        LinkNeighborhood[] neighborhoods = pageParser.getLinkNeighborhood();
//        PaginaURL pageParser = new PaginaURL(new URL(url), testPage);
//        LinkNeighborhood[] neighborhoods = pageParser.getLinkNeighboor();
//        System.out.println("tokens = " + pageParser.getTokens());

        // then
        assertThat(neighborhoods.length, is(1));

        assertThat(neighborhoods[0].getAroundString().trim(), is("my first heading my paragraph free text"));
        assertThat(neighborhoods[0].getAround()[0], is("my"));
        assertThat(neighborhoods[0].getAround()[1], is("first"));
        assertThat(neighborhoods[0].getAround()[2], is("heading"));

        assertThat(neighborhoods[0].getAnchorString().trim(), is("my first anchor text"));
        assertThat(neighborhoods[0].getAnchor()[0], is("my"));
        assertThat(neighborhoods[0].getAnchor()[1], is("first"));
        assertThat(neighborhoods[0].getAnchor()[2], is("anchor"));
        assertThat(neighborhoods[0].getAnchor()[3], is("text"));
    }

    @Test
    public void shouldNotExtractInvalidLinks() throws MalformedURLException {
        // given
        URL url = new URL("http://example.com/test.html");
        String testPage = new HtmlBuilder()
                .withBody(
                        "<h1>My First Heading</h1>"
                                + "<a href = \"http://None/\">link 0</a>"
                                + "<a href = \"http://12324/\">link 1</a>"
                                + "<a href = \"/asdf.html\">link 2</a>"
                )
                .build();
        // when
        HtmlSaxParser pageParser = new HtmlSaxParser(url, testPage.toString());
        URL[] links = pageParser.links();
        LinkNeighborhood[] lns = pageParser.getLinkNeighborhood();

        // then
        assertThat(links.length, is(1));
        assertThat(links[0].toString(), is("http://example.com/asdf.html"));

        assertThat(lns.length, is(1));
        assertThat(lns[0].getLink().toString(), is("http://example.com/asdf.html"));
    }

    @Test
    public void shouldNormalizeLinks() throws MalformedURLException {
        // given
        URL url = new URL("http://www.w3schools.com/html/tryit.asp?filename=tryhtml_basic_document");
        String testPage = HtmlBuilder.newBuilder()
                .appendToBody("<h1>My First Heading</h1>")
                .appendToBody("<a href = \"http://Example.com:80/post.php?\">Link 1.</a>")
                .appendToBody("<a href = \"HTTP://EXAMPLE.com/post.php?b=2&a=1\">Link 2.</a>")
                .appendToBody("<a href = \"HTTP://EXAMPLE.com\">Link 3.</a>")
                .build();
        // when
        HtmlSaxParser pageParser = new HtmlSaxParser(url, testPage);
        LinkNeighborhood[] neighborhoods = pageParser.getLinkNeighborhood();
        URL[] links = pageParser.links();

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

    private String createTestPage() {
        return HtmlBuilder.newBuilder()
                .appendToBody("<h1>My First Heading</h1>")
                .appendToBody("<a href=\"https://en.wikipedia.org/wiki/Mouse_(computing)#Mechanical_mice\">My first paragraph.</a>")
                .build();
    }

    public static class HtmlBuilder {

        private String header = "";
        private String body = "";

        public static HtmlBuilder newBuilder() {
            return new HtmlBuilder();
        }

        public HtmlBuilder appendToBody(String body) {
            this.body += body;
            return this;
        }

        public HtmlBuilder withHeader(String header) {
            this.header = header;
            return this;
        }

        public HtmlBuilder withBody(String body) {
            this.body = body;
            return this;
        }

        public String build() {
            StringBuilder html = new StringBuilder();
            html.append("<!DOCTYPE html>");
            html.append("<html>");
            if (header != null && !header.isEmpty()) {
                html.append(header);
            }
            html.append("<body>");
            if (body != null && !body.isEmpty()) {
                html.append(body);
            }
            html.append("</body>");
            html.append("</html>");
            return html.toString();
        }

    }


}
