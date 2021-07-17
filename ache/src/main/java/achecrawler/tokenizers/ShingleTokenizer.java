package achecrawler.tokenizers;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.core.SimpleAnalyzer;
import org.apache.lucene.analysis.shingle.ShingleAnalyzerWrapper;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;

import com.google.common.base.Charsets;
import com.google.common.hash.HashFunction;
import com.google.common.hash.Hashing;

public class ShingleTokenizer implements Tokenizer {

    private static final HashFunction MURMUR3 = Hashing.murmur3_32();

    private Analyzer analyzer;

    public ShingleTokenizer(int size) {
        this.analyzer = new ShingleAnalyzerWrapper(new SimpleAnalyzer(), size);
    }

    public List<String> tokenize(String text) {
        try {
            TokenStream ts = analyzer.tokenStream("cleanText", text);
            CharTermAttribute cattr = ts.addAttribute(CharTermAttribute.class);
            ts.reset();
            List<String> tokens = new ArrayList<String>();
            while (ts.incrementToken()) {
                String token = cattr.toString();
                tokens.add(token);
            }
            ts.close();
            return tokens;
        } catch (IOException e) {
            throw new RuntimeException(
                    "Shigle tokenization failed for string: " + text, e);
        }
    }

    public Set<String> tokensSet(String cleanText) {
        return new HashSet<String>(tokenize(cleanText));
    }

    public Set<Integer> hashedTokenSet(String cleanText) {
        HashSet<Integer> hashedTokens = new HashSet<>();
        for (String token : tokenize(cleanText)) {
            hashedTokens.add(MURMUR3.hashString(token, Charsets.UTF_8).asInt());
        }
        return hashedTokens;
    }

}
