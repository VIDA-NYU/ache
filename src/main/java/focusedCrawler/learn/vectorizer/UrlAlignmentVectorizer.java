package focusedCrawler.learn.vectorizer;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import focusedCrawler.dedup.rules.RewriteRule;
import focusedCrawler.dedup.rules.Sequence;

public class UrlAlignmentVectorizer extends IndexedVectorizer {

    private List<Sequence> alignments = new ArrayList<>();
    private int maxUrlsPerAlignment = 10;

    public void fit(Map<String, List<String>> urlsByHash) {
        Set<Sequence> alignments = new HashSet<>();
        for (List<String> list : urlsByHash.values()) {
            if (list.size() > 1) {
                // sample urls by taking first urls
                int maxUrls = Math.min(maxUrlsPerAlignment, list.size());
                System.out.printf("Aligning %d out of %d duplicated urls.\n", maxUrls, list.size());
                List<String> subList = list.subList(0, maxUrls);

                // align urls
                alignments.add(Sequence.multipleAlignment(subList));

                // align pieces separately
                List<String> queryStrings = new ArrayList<>();
                List<String> fileNames = new ArrayList<>();
                for (String url : subList) {
                    try {
                        String query = new URL(url).getQuery();
                        if (query != null && query.length() > 0) {
                            queryStrings.add(query);
                        }
                        String file = new URL(url).getFile();
                        if (file != null && file.length() > 0) {
                            fileNames.add(file);
                        }
                    } catch (MalformedURLException e) {
                        throw new IllegalArgumentException("Invalid url found: " + url, e);
                    }
                }
                if (queryStrings.size() > 1) {
                    alignments.add(Sequence.multipleAlignment(queryStrings));
                }
                if (fileNames.size() > 1) {
                    alignments.add(Sequence.multipleAlignment(fileNames));
                }
            }
        }
        for (Sequence s : alignments) {
            this.alignments.add(s);
            super.addFeature(s.toString());
        }
    }

    @Override
    public SparseVector transform(String url) {
        List<String> matchedAlignments = new ArrayList<>();
        for (Sequence s : alignments) {
            RewriteRule rewriteRule = new RewriteRule(s, 3);
            if (rewriteRule.matches(url)) {
                matchedAlignments.add(s.toString());
            }
        }
        return SparseVector.binary(matchedAlignments, this);
    }

    @Override
    public void fit(List<String> trainingData) {
        throw new UnsupportedOperationException("Method not supported");
    }

}
