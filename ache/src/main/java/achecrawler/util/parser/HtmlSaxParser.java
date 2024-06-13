package achecrawler.util.parser;

import achecrawler.util.Urls;
import okhttp3.HttpUrl;
import org.apache.commons.io.input.CharSequenceReader;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.core.SimpleAnalyzer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
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

    public static final Logger logger = LoggerFactory.getLogger(HtmlSaxParser.class);

    public static final int AROUND_WORDS = 10;

    private final List<Anchor> anchors = new ArrayList<>();
    private final List<String> images = new ArrayList<>();
    private final List<String> tokens = new ArrayList<>();

    private final StringBuilder title = new StringBuilder();
    private final StringBuilder text = new StringBuilder();
    private final SimpleTokenizer tokenizer = new SimpleTokenizer(new CharSequenceReader(text));

    private HttpUrl base;
    private TextType textState = TextType.TEXT;
    private String currentHref = null;
    private int currentHrefTextStart = 0;
    private int currentHrefTokenStart = 0;
    private StringBuilder anchorText = new StringBuilder();

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

    @Override
    public void startElement(String uri, String tagName, String qName, Attributes atts) {
        switch (tagName) {
            case "BASE": {
                handleBaseTag(atts);
                break;
            }
            case "A": {
                this.textState = TextType.ANCHOR_TEXT;
                String href = atts.getValue("href");
                String link = createLink(this.base, href);
                if (link != null) {
                    this.currentHref = link;
                    this.currentHrefTextStart = text.length();

                    this.tokenizer.tokenize();
                    this.currentHrefTokenStart = this.tokens.size();
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

    @Override
    public void endElement(String uri, String tagName, String qName) {
        // TODO: extract data from <meta> tags (e.g., description, keywords, noindex, nofollow)
        switch (tagName) {
            case "A":
                if (currentHref != null && !currentHref.isEmpty()) {
                    tokenizer.tokenize();
                    anchors.add(new Anchor(currentHref, currentHrefTextStart, text.length(),
                            anchorText.toString().trim(), currentHrefTokenStart, tokens.size()));
                    currentHref = null;
                }
                anchorText = new StringBuilder();
                textState = TextType.TEXT;
                break;
            case "TITLE":
                textState = TextType.TEXT;
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
            case "NOSCRIPT":
            case "SCRIPT":
            case "STYLE":
                this.textState = TextType.TEXT;
                break;
            default:
                text.append(' ');
        }
    }

    /*
     * Handles the BASE tag which sets the URL that should be used for resolving
     */
    private void handleBaseTag(Attributes attributes) {
        String href = attributes.getValue("href");
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
    }

    @Override
    public void characters(char[] ch, int start, int length) {
        switch (textState) {
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
            case IGNORE:
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

    public LinkNeighborhood[] getLinkNeighborhood() {
        List<LinkNeighborhood> links = new ArrayList<>();
        for (Anchor anchor : anchors) {
            URL absoluteUrl = Urls.toJavaURL(anchor.href);
            LinkNeighborhood ln = new LinkNeighborhood(absoluteUrl);
            ln.setAround(createAroundText(anchor));
            ln.setAnchor(createAnchorText(anchor));
            links.add(ln);
        }
        return links.toArray(new LinkNeighborhood[links.size()]);
    }

    private String[] createAnchorText(Anchor anchor) {
        List<String> aroundTemp = new ArrayList<>();
        for (int i = anchor.tokenStart; i < anchor.tokenEnd; i++) {
            aroundTemp.add(tokens.get(i));
        }
        return aroundTemp.toArray(new String[aroundTemp.size()]);
    }

    private String[] createAroundText(Anchor anchor) {
        List<String> aroundTemp = new ArrayList<String>();
        final int begin = Math.max(0, anchor.tokenStart - AROUND_WORDS);
        for (int i = begin; i < anchor.tokenStart; i++) {
            aroundTemp.add(tokens.get(i));
        }
        int end = Math.min(tokens.size(), anchor.tokenEnd + AROUND_WORDS);
        for (int i = anchor.tokenEnd; i < end; i++) {
            aroundTemp.add(tokens.get(i));
        }
        return aroundTemp.toArray(new String[aroundTemp.size()]);
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

    public URL getURL() {
        return base != null ? base.url() : null;
    }

    public List<String> tokens() {
        return this.tokens;
    }

    public String title() {
        return this.title.toString();
    }

    public String text() {
        return this.text.toString();
    }

    private void print() {
        // TODO: Clean up
        System.out.println("---");
        System.out.println("TEXT: " + text.toString());
        System.out.println("ANCHORS: ");
        for (Anchor anchor : anchors) {
            System.out.println("> " + anchor);
        }
    }

    @Override
    public void setDocumentLocator(Locator locator) {
    }

    @Override
    public void startDocument() {
    }

    @Override
    public void endDocument() {
        // Finish tokenization of text left over
        this.tokenizer.tokenize();
    }

    @Override
    public void ignorableWhitespace(char[] ch, int start, int length) {
    }

    @Override
    public void processingInstruction(String target, String data) {
    }

    @Override
    public void skippedEntity(String name) {
    }

    @Override
    public void startPrefixMapping(String prefix, String uri) {
    }

    @Override
    public void endPrefixMapping(String prefix) {
    }

    enum TextType {
        TITLE, TEXT, ANCHOR_TEXT, IGNORE
    }

    static class Anchor {

        private final String href;
        private final int textStart;
        private final int textEnd;
        private final String anchorText;
        private final int tokenStart;
        private final int tokenEnd;

        Anchor(String href, int textStart, int textEnd, String anchorText, int tokenStart, int tokenEnd) {
            this.href = href;
            this.textStart = textStart;
            this.textEnd = textEnd;
            this.anchorText = anchorText;
            this.tokenStart = tokenStart;
            this.tokenEnd = tokenEnd;
        }

        @Override
        public String toString() {
            return "Anchor[href=" + href +
                    ", textStart=" + textStart +
                    ", textEnd=" + textEnd +
                    ", text=" + anchorText +
                    "]";
        }
    }

    public class SimpleTokenizer {

        private final TokenStream ts;
        private final CharTermAttribute cattr;

        public SimpleTokenizer(CharSequenceReader cleanText) {
            // TODO: setup a good general tokenizer
            Analyzer analyzer = new SimpleAnalyzer();
//            this.analyzer = new StandardAnalyzer(StandardAnalyzer.ENGLISH_STOP_WORDS_SET);
//            this.analyzer = new Analyzer() {
//                @Override
//                protected TokenStreamComponents createComponents(final String fieldName) {
//                    final StandardTokenizer src = new StandardTokenizer();
//                    src.setMaxTokenLength(255);
//                    // return new TokenStreamComponents(src);
////                    TokenStream tok = new StandardFilter(src);
////                    tok = new LowerCaseFilter(tok);
//////                    tok = new StopFilter(tok, stopwords);
////                    return new TokenStreamComponents(src, tok) {
////                        @Override
////                        protected void setReader(final Reader reader) {
////                            // So that if maxTokenLength was changed, the change takes
////                            // effect next time tokenStream is called:
////                            src.setMaxTokenLength(StandardAnalyzer.this.maxTokenLength);
////                            super.setReader(reader);
////                        }
////                    };
//                }
//            };
            ts = analyzer.tokenStream("cleanText", cleanText);
            cattr = ts.addAttribute(CharTermAttribute.class);
            try {
                ts.reset();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        public void tokenize() {
            try {
                while (ts.incrementToken()) {
                    String token = cattr.toString();
                    HtmlSaxParser.this.tokens.add(token);
                }
            } catch (IOException e) {
                throw new RuntimeException("Tokenization failed", e);
            }
        }
    }

    // TODO: Clean up
    public static void main(String[] args) throws Exception {

        String url = "http://www.darpa.mil/program/memex";
        String html =
                "<html><meta /><body><h1><!-- my comment --><a HREF=\"index.html\">My heading 1!</a></h1><div><p>My Paragraph.</p></body></html>";
        html = new String(Files.readAllBytes(Paths.get(
                "ache-tools/src/test/resources/achecrawler/memex/cdr/http%3A%2F%2Fwww.darpa.mil%2Fprogram%2Fmemex")));
        HtmlSaxParser parser = new HtmlSaxParser(url, html);
//        parser.print();
//        PaginaURL parser = new PaginaURL(new URL(url), html);

        final LinkNeighborhood[] neighborhoods = parser.getLinkNeighborhood();
        for (LinkNeighborhood n : neighborhoods) {
            System.out.println("> Around: " + n.getLink().toString());
            System.out.println(n.getAroundString());
        }
    }
}
