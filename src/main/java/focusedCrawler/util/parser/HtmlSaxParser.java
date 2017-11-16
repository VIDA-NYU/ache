package focusedCrawler.util.parser;

import java.io.IOException;
import java.io.StringReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.cyberneko.html.parsers.SAXParser;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.InputSource;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;

public class HtmlSaxParser extends SAXParser implements ContentHandler {

    public static void main(String[] args) throws Exception {

        String url = "http://example.com";
        String html =
                "<html><meta /><body><h1><!-- my comment --><a HREF=\"index.html\">My heading 1!</a></h1><div><p>My Paragraph.</p></body></html>";
        html = "Hello World!";
        html = new String(Files.readAllBytes(Paths.get(
                "src/test/resources/focusedCrawler/memex/cdr/http%3A%2F%2Fwww.darpa.mil%2Fprogram%2Fmemex")));
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

    private TextType textState = TextType.TEXT;
    private List<Anchor> anchors = new ArrayList<>();
    private List<String> images;
    private String baseUrl;
    private StringBuilder title = new StringBuilder();
    private StringBuilder text = new StringBuilder();
    private StringBuilder anchorText = new StringBuilder();
    private String currentHref = null;
    private int currentHrefTextStart = 0;

    public HtmlSaxParser(String url, String html) throws SAXException, IOException {
        this.baseUrl = url;
        // super.setContentHandler(new BoilerpipeHTMLContentHandler());
        setContentHandler(this);
        InputSource input = new InputSource(new StringReader(html));
        this.parse(input);
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
                String href = atts.getValue("href");
                if (href != null && !href.isEmpty()) {
                    // All extracted links should be relative to the href of <base> tag
                    this.baseUrl = href;
                }
                break;
            }
            case "A": {
                this.textState = TextType.ANCHOR_TEXT;
                String href = atts.getValue("href");
                if (href != null && !href.isEmpty()) {
                    this.currentHref = href;
                    this.currentHrefTextStart = text.length();
                }
                break;
            }
            case "IMG": {
                String href = atts.getValue("href");
                if (href != null && !href.isEmpty()) {
                    images.add(href);
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

    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException {
        String tagName = localName;
        System.out.println(")" + localName);
        switch (tagName) {
            case "A":
                if (currentHref != null && !currentHref.isEmpty()) {
                    // TODO: validate href? unescape?
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
