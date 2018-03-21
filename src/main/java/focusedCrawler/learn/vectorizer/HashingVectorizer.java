package focusedCrawler.learn.vectorizer;

import java.util.ArrayList;
import java.util.List;

import com.google.common.base.Charsets;
import com.google.common.hash.HashFunction;
import com.google.common.hash.Hashing;

import focusedCrawler.tokenizers.Tokenizer;
import focusedCrawler.tokenizers.Tokenizers;

public class HashingVectorizer extends IndexedVectorizer {

    private static final HashFunction MURMUR3 = Hashing.murmur3_32();

    private boolean quadratic = false;
    private int hashTrickSize;
    private Tokenizer tokenizer = Tokenizers.alphaNumeric();

    public HashingVectorizer() {
        this(Tokenizers.alphaNumeric(), 18, true);
    }

    public HashingVectorizer(Tokenizer tokenizer) {
        this(tokenizer, 18, true);
    }

    public HashingVectorizer(Tokenizer tokenizer, int bits, boolean quadraticFeatures) {
        this.tokenizer = tokenizer;
        this.quadratic = quadraticFeatures;
        this.hashTrickSize = (int) Math.pow(2, bits);
        for (int i = 0; i < hashTrickSize; i++) {
            super.addFeature(String.valueOf(i));
        }
    }

    @Override
    public SparseVector transform(String url) {
        return transform(tokenizer.tokenize(url));
    }

    public SparseVector transform(List<String> instanceFeatures) {
        List<String> hashes = new ArrayList<>();
        for (String f : instanceFeatures) {
            hashes.add(String.valueOf(getIndexOfFeature(f)));
        }

        if (quadratic) {
            for (String f1 : instanceFeatures) {
                for (String f2 : instanceFeatures) {
                    if (!f1.equals(f2)) {
                        hashes.add(String.valueOf(getIndexOfFeature(f1 + f2)));
                    }
                }
            }
        }

        return SparseVector.binary(hashes, this);
    }

    @Override
    public int getIndexOfFeature(String token) {
        int hash = MURMUR3.hashString(token, Charsets.UTF_8).asInt();
        int featureIndex = Math.abs(hash % hashTrickSize);
        return featureIndex;
    }

    public void fit(List<String> trainingData) {
        // no-op
    }

}
