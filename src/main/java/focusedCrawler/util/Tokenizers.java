package focusedCrawler.util;

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

public class Tokenizers {

    private static HashFunction murmur = Hashing.murmur3_32();

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
                throw new RuntimeException("Shigle tokenization failed for string: "+cleanText, e);
            }
        }
        
        public Set<String> tokensSet(String cleanText) {
            return new HashSet<String>(tokenize(cleanText));
        }
        
        public Set<Integer> hashedTokenSet(String cleanText) {
            HashSet<Integer> hashedTokens = new HashSet<>();
            for(String token : tokenize(cleanText)) {
                hashedTokens.add(murmur.hashString(token, Charsets.UTF_8).asInt());
            }
            return hashedTokens;
        }        

    }

    public static ShingleTokenizer shingles(int size) {
        return new ShingleTokenizer(size);
    }

}
