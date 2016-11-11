package focusedCrawler.target.model;

import java.io.Serializable;
import java.net.URL;

import focusedCrawler.util.parser.LinkNeighborhood;
import focusedCrawler.util.parser.PaginaURL;

@SuppressWarnings("serial")
public class ParsedData implements Serializable {

    private String title;
    private String cleanText;
    private String[] words;
    private String[] wordsMeta;
    private URL[] links;
    private LinkNeighborhood[] linkNeighborhood;

    public ParsedData(PaginaURL page) {
        this.words = page.palavras();
        this.wordsMeta = page.palavrasMeta();
        this.title = page.titulo();
        this.cleanText = page.palavras_to_string();
        this.linkNeighborhood = page.getLinkNeighboor();
        this.links = page.links();
    }

    public String getTitle() {
        return title;
    }

    public String getCleanText() {
        return cleanText;
    }

    public String[] getWords() {
        return words;
    }

    public String[] getWordsMeta() {
        return wordsMeta;
    }

    public URL[] getLinks() {
        return links;
    }

    public LinkNeighborhood[] getLinkNeighborhood() {
        return linkNeighborhood;
    }

}
