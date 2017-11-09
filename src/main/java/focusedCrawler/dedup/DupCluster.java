package focusedCrawler.dedup;

import java.util.List;

public class DupCluster {

    private String contentDigest;

    private List<String> dupUrls;

    public DupCluster(String contentDigest, List<String> dupUrls) {
        this.contentDigest = contentDigest;
        this.dupUrls = dupUrls;
    }

    boolean hasDup() {
        return dupUrls.size() > 1;
    }

    public String getContentDigest() {
        return contentDigest;
    }

    public List<String> getDupUrls() {
        return dupUrls;
    }

}
