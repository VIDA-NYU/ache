package achecrawler.dedup;

import java.util.List;

public interface DupDetector {

    boolean detectAndIndex(String url, byte[] contentBytes);

    boolean detectAndIndex(String url, String content);

    DupData getDuplicationSample();

    void close();

    public class DupData {

        public final List<List<String>> duplicates;
        public final List<String> unique;

        public DupData(List<List<String>> duplicates, List<String> unique) {
            this.duplicates = duplicates;
            this.unique = unique;
        }
    }

}
