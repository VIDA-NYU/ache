package achecrawler.util.parser;

import achecrawler.util.Urls;
import okhttp3.HttpUrl;
import org.cyberneko.html.parsers.SAXParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.*;

import java.io.IOException;
import java.io.StringReader;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class HtmlSaxParser extends SAXParser implements ContentHandler {


    public static void main(String[] args) throws Exception {

        String url = "http://example.com";
        String html =
                "<html><meta /><body><h1><!-- my comment --><a HREF=\"index.html\">My heading 1!</a></h1><div><p>My Paragraph.</p></body></html>";
        html = "Hello World!";
        html = new String(Files.readAllBytes(Paths.get(
                "ache-tools/src/test/resources/achecrawler/memex/cdr/http%3A%2F%2Fwww.darpa.mil%2Fprogram%2Fmemex")));
        HtmlSaxParser parser = new HtmlSaxParser(url, html);
        parser.print();
    }

    static class Anchor {

        private String href;
        private int textStart;
        private int textEnd;
        private String anchorText;

        Anchor(String href, int textStart, int textEnd, String anchorText) {
            this.href = href;
            this.textStart = textStart;
            this.textEnd = textEnd;
            this.anchorText = anchorText;
        }

        @Override
        public String toString() {
            return "Anchor[href=" + href + ", textStart=" + textStart + ", textEnd=" + textEnd
                    + ", text=" + anchorText + "]";
        }

    }

    enum TextType {
        TITLE, TEXT, ANCHOR_TEXT, IGNORE
    }

    public static final Logger logger = LoggerFactory.getLogger(HtmlSaxParser.class);


    private TextType textState = TextType.TEXT;
    private List<Anchor> anchors = new ArrayList<>();
    private List<String> images;
    private HttpUrl base;

    private StringBuilder title = new StringBuilder();
    private StringBuilder text = new StringBuilder();
    private StringBuilder anchorText = new StringBuilder();
    private String currentHref = null;
    private int currentHrefTextStart = 0;

    public HtmlSaxParser(URL url, String html) {
        this(url.toString(), html);
    }

    public HtmlSaxParser(String url, String html) {
        this.base = HttpUrl.parse(url);
        // super.setContentHandler(new BoilerpipeHTMLContentHandler());
        setContentHandler(this);
        InputSource input = new InputSource(new StringReader(html));
        try {
            this.parse(input);
        } catch (SAXException | IOException e) {
            throw new RuntimeException("Failed to parse page: " + url, e);
        }
    }

    private void print() {
        System.out.println("---");
        System.out.println("TEXT: " + text.toString());
        System.out.println("ANCHORS: ");
        for (Anchor anchor : anchors) {
            System.out.println("> " + anchor);
        }
    }

    @Override
    public void startElement(String uri, String localName, String qName, Attributes atts)
            throws SAXException {
        String tagName = localName;
        System.out.println("(" + localName + " " + qName + " " + uri);
        switch (tagName) {
            case "BASE": {
                //
                // Handles the BASE tag which sets the URL that should be used for resolving
                // relative links
                //
                String href = atts.getValue("href");
                if (href != null && !href.isEmpty()) {
                    // All extracted links should be relative to the href of <base> tag
                    try {
                        HttpUrl newBase = Urls.resolveHttpLink(this.base, href);
                        if (newBase != null) {
                            this.base = newBase;
                        }
                    } catch (Exception e) {
                        // ignore invalid URLs
                    }
                }
                break;
            }
            case "A": {
                this.textState = TextType.ANCHOR_TEXT;
                String href = atts.getValue("href");
                String link = createLink(this.base, href);
                if (link != null) {
                    this.currentHref = link;
                    this.currentHrefTextStart = text.length();
                }
                break;
            }
            case "IMG": {
                String href = atts.getValue("href");
                if (href != null && !href.isEmpty()) {
                    images.add(createLink(this.base, href));
                }
                break;
            }
            case "NOSCRIPT":
            case "SCRIPT":
            case "STYLE":
                this.textState = TextType.IGNORE;
                break;
            case "TITLE":
                this.textState = TextType.TITLE;
                break;
            // default:
            // this.textState = TextType.TEXT;
        }
    }

    private String createLink(HttpUrl base, String href) {
        if (href == null || href.isEmpty()) {
            return null;
        }

        String url = href;

        if (url.startsWith(" ") || url.endsWith(" ")) {
            url = href.trim();
        }

        if (url.startsWith("javascript:")) {
            return null;
        }

        if (url.startsWith("mailto:")) {
            return null;
        }

        if (url.startsWith("tel:")) {
            return null;
        }

        if (url.startsWith("data:")) {
            return null;
        }

        String absoluteUrl = Urls.resolveHttpLinkAsString(base, href);
        if (absoluteUrl == null || absoluteUrl.isEmpty()) {
            return null;
        }

        if (!Urls.isValid(absoluteUrl)) {
            return null;
        }

        return Urls.normalize(absoluteUrl);
    }

    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException {
        String tagName = localName;
        System.out.println(")" + localName);
        switch (tagName) {
            case "A":
                if (currentHref != null && !currentHref.isEmpty()) {
                    anchors.add(new Anchor(currentHref, currentHrefTextStart, text.length(),
                            anchorText.toString().trim()));
                    currentHref = null;
                }
                anchorText = new StringBuilder();
                textState = TextType.TEXT;
                break;
            case "TITLE":
                break;
            case "P":
            case "H1":
            case "H2":
            case "H3":
            case "H4":
            case "H5":
            case "H6":
                text.append("\n\n");
                break;
            case "BR":
                text.append('\n');
                break;
            default:
                text.append(' ');
        }
    }

    @Override
    public void characters(char[] ch, int start, int length) throws SAXException {
        System.out.println(new String(ch, start, length));
        switch (textState) {
            case IGNORE:
                break;
            case TEXT:
                text.append(ch, start, length);
                break;
            case ANCHOR_TEXT:
                text.append(ch, start, length);
                anchorText.append(ch, start, length);
                break;
            case TITLE:
                title.append(ch, start, length);
                break;
        }
    }

    public URL[] links() {
        List<URL> links = new ArrayList<>();
        for (Anchor anchor : anchors) {
            URL absoluteUrl = Urls.toJavaURL(anchor.href);
            if (absoluteUrl != null) {
                links.add(absoluteUrl);
            }
        }
        return links.toArray(new URL[links.size()]);
    }

    public LinkNeighborhood[] getLinkNeighboor() {
        List<LinkNeighborhood> links = new ArrayList<>();
        for (Anchor anchor : anchors) {
            URL absoluteUrl = Urls.toJavaURL(anchor.href);
            LinkNeighborhood ln = new LinkNeighborhood(absoluteUrl);
            // TODO:
//            ln.setAround();
//            ln.setAnchor();
            links.add(ln);
        }
        return links.toArray(new LinkNeighborhood[links.size()]);
    }

    public URL getURL() {
        return base != null ? base.url() : null;
    }

    @Override
    public void setDocumentLocator(Locator locator) {
        // System.out.println("NekoHtmlSaxParser.setDocumentLocator()");
    }

    @Override
    public void startDocument() throws SAXException {
        // System.out.println("NekoHtmlSaxParser.startDocument()");
    }

    @Override
    public void endDocument() throws SAXException {
        // System.out.println("NekoHtmlSaxParser.startElement()");
    }

    @Override
    public void ignorableWhitespace(char[] ch, int start, int length) throws SAXException {
        System.out.println("NekoHtmlSaxParser.ignorableWhitespace()");
    }

    @Override
    public void processingInstruction(String target, String data) throws SAXException {
        System.out.println("NekoHtmlSaxParser.processingInstruction()");
    }

    @Override
    public void skippedEntity(String name) throws SAXException {
        System.out.println("NekoHtmlSaxParser.skippedEntity()");
    }

    @Override
    public void startPrefixMapping(String prefix, String uri) throws SAXException {
        System.out.println("NekoHtmlSaxParser.startPrefixMapping()");
    }

    @Override
    public void endPrefixMapping(String prefix) throws SAXException {
        System.out.println("NekoHtmlSaxParser.endPrefixMapping()");
    }

}
