package achecrawler.util;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.StringTokenizer;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.core.SimpleAnalyzer;
import org.apache.lucene.analysis.shingle.ShingleAnalyzerWrapper;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;

import com.google.common.base.Charsets;
import com.google.common.hash.HashFunction;
import com.google.common.hash.Hashing;

public class Tokenizers {

    private static final HashFunction MURMUR3 = Hashing.murmur3_32();
    private static final WhitespaceTokenizer WHITESPACE_TOKENIZER = new WhitespaceTokenizer();

    public static class ShingleTokenizer {

        private Analyzer analyzer;

        public ShingleTokenizer(int size) {
            this.analyzer = new ShingleAnalyzerWrapper(new SimpleAnalyzer(), size);
        }

        public List<String> tokenize(String cleanText) {
            try {
                TokenStream ts = analyzer.tokenStream("cleanText", cleanText);
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
                        "Shigle tokenization failed for string: " + cleanText, e);
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

    public static ShingleTokenizer shingles(int size) {
        return new ShingleTokenizer(size);
    }

    public static class WhitespaceTokenizer {

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

    public static WhitespaceTokenizer whitespace() {
        return WHITESPACE_TOKENIZER;
    }

}
