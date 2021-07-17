package achecrawler.tokenizers;

public class Tokenizers {

    public static ShingleTokenizer shingles(int size) {
        return new ShingleTokenizer(size);
    }

    public static AlphaNumTokenizer alphaNumeric() {
        return AlphaNumTokenizer.INSTANCE;
    }

    public static UrlTokenizer url() {
        return UrlTokenizer.INSTANCE;
    }

    public static WhitespaceTokenizer whitespace() {
        return WhitespaceTokenizer.INSTANCE;
    }

}
