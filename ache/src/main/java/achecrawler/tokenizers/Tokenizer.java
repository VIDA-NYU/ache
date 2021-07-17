package achecrawler.tokenizers;

import java.util.List;

public interface Tokenizer {

    public List<String> tokenize(String text);

}
