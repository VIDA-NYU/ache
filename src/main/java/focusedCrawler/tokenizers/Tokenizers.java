package focusedCrawler.tokenizers;

import focusedCrawler.util.AlphaNumTokenizer;

public class Tokenizers {

    public static ShingleTokenizer shingles(int size) {
        return new ShingleTokenizer(size);
    }

    public static AlphaNumTokenizer alphaNumeric() {
        return new AlphaNumTokenizer();
    }
    
    public static UrlTokenizer url() {
        return new UrlTokenizer();
    }

}
