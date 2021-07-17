package achecrawler.tokenizers;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

public class WhitespaceTokenizer implements Tokenizer {

    static final WhitespaceTokenizer INSTANCE = new WhitespaceTokenizer();

    @Override
    public List<String> tokenize(String text) {
        List<String> tokens = new ArrayList<String>();
        StringTokenizer tokenizer = new StringTokenizer(text, " ");
        while (tokenizer.hasMoreTokens()) {
            tokens.add(tokenizer.nextToken());
        }
        return tokens;
    }

    public String[] tokenizeToArray(String text) {
        List<String> tokens = this.tokenize(text);
        return (String[]) tokens.toArray(new String[tokens.size()]);
    }

}
