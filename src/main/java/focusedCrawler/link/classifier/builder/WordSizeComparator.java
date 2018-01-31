package focusedCrawler.link.classifier.builder;

import java.util.Comparator;

public class WordSizeComparator implements Comparator<WordFrequency> {

    /**
     * compare
     *
     * @param o1 Object
     * @param o2 Object
     * @return int
     */
    public int compare(WordFrequency o1, WordFrequency o2) {
        if (o1.getWord().length() < o2.getWord().length())
            return -1;
        else if (o1.getWord().length() == o2.getWord().length())
            return 0;
        else
            return 1;
    }

}
