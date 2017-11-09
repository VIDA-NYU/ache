package focusedCrawler.learn.vectorizer;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

public class UrlParserVectorizer extends IndexedVectorizer {

    public UrlParserVectorizer() {}

    public void fit(Map<String, List<String>> urlsByHash) {
        Set<String> allTerms = new TreeSet<>();

        for (List<String> list : urlsByHash.values()) {
            if (list.size() > 1) {
                for(String urlStr :list) {
                    allTerms.addAll(parseTokens(urlStr));
                }
            }
        }
        for(String t : allTerms) {
            super.addFeature(t);
        }
    }

    private Set<String> parseTokens(String urlStr) {
        Set<String> terms = new TreeSet<>();
        try {
            URL urlObj = new URL(urlStr);
            String query = urlObj.getQuery();
            if(query != null && query.length() > 0) {
                String[] split = query.split("&");
                for (int i = 0; i < split.length; i++) {
                    terms.add(split[i]);
                    String[] kv = split[i].split("=");
                    if(kv != null && kv.length>0) {
                        terms.add(kv[0]+"=");
                    }
                }
            }
            String file = urlObj.getFile();
            if(file !=null && file.length() > 0) {
                String[] split = file.split("\\?");
                if(split.length>0) {
                    terms.add(split[0]);
                }
            }
            return terms;
        } catch (MalformedURLException e) {
            throw new IllegalArgumentException("Invalid url found: "+urlStr, e);
        }
    }

    public void partialFit(List<String> intanceFeatures) {
        for (String feature : intanceFeatures) {
            super.addFeature(feature);
        }
    }

    @Override
    public SparseVector transform(String url) {
        ArrayList<String> urlTokens = new ArrayList<>(parseTokens(url));
        List<String> knownFeatures = filterFeatures(urlTokens);
        return SparseVector.binary(knownFeatures, this);
    }

    private List<String> filterFeatures(List<String> intanceFeatures) {
        List<String> copy = new ArrayList<>(intanceFeatures);
        Iterator<String> it = copy.iterator();
        while (it.hasNext()) {
            if (!super.contains(it.next()))
                it.remove();
        }
        return copy;
    }

}
