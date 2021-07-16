package achecrawler.target.model;

import java.io.Serializable;
import java.net.URL;

import achecrawler.util.parser.LinkNeighborhood;
import achecrawler.util.parser.PaginaURL;

@SuppressWarnings("serial")
public class ParsedData implements Serializable {

    private String title;
    private String cleanText;
    private String[] words;
    private String[] wordsMeta;
    private URL[] links;
    private LinkNeighborhood[] linkNeighborhood;

    public ParsedData(PaginaURL page) {
        this.words = page.words();
        this.wordsMeta = page.wordsMeta();
        this.title = page.title();
        this.cleanText = page.wordsAsString();
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
