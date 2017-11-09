package focusedCrawler.learn.vectorizer;

import java.util.ArrayList;
import java.util.List;

import com.google.common.base.Charsets;
import com.google.common.hash.Hashing;

import focusedCrawler.util.AlphaNumTokenizer;

public class HashingVectorizer extends IndexedVectorizer {

    private boolean quadraticFeatures = false;
    private int hashTrickSize;

    public HashingVectorizer() {
        this(18, true);
    }

    public HashingVectorizer(int b, boolean quadraticFeatures) {
        this.quadraticFeatures = quadraticFeatures;
        this.hashTrickSize = (int) Math.pow(2, b);
        for (int i = 0; i < hashTrickSize; i++) {
            super.addFeature(String.valueOf(i));
        }
    }

    @Override
    public SparseVector transform(String url) {
        return transform(AlphaNumTokenizer.parseTokens(url));
    }

    public SparseVector transform(List<String> instanceFeatures) {
        List<String> hashes = new ArrayList<>();
        for (String f : instanceFeatures) {
            hashes.add(String.valueOf(getIndexOfFeature(f)));
        }
        
        if(quadraticFeatures) {
            for(String f1 : instanceFeatures) {
                for(String f2 : instanceFeatures) {
                    if(!f1.equals(f2)) {
                        hashes.add(String.valueOf(getIndexOfFeature(f1+f2)));
                    }
                }
            }
        }
        
        return SparseVector.binary(hashes, this);
    }

    @Override
    public int getIndexOfFeature(String token) {
        int hash = Hashing.murmur3_32().hashString(token, Charsets.UTF_8).asInt();
        int featureIndex = Math.abs(hash % hashTrickSize);
        return featureIndex;
    }

}
