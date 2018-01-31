package focusedCrawler.link.classifier.builder;

import java.util.Comparator;

public class WordFrequencyComparator implements Comparator<WordFrequency> {

    public WordFrequencyComparator() {}

    /**
     * equals
     *
     * @param obj Object
     * @return boolean
     */
    public boolean equals(Object obj) {
        return false;
    }

    /**
     * compare
     *
     * @param o1 Object
     * @param o2 Object
     * @return int
     */
    public int compare(WordFrequency o1, WordFrequency o2) {
        if (o1.getFrequency() < o2.getFrequency())
            return 1;
        else if (o1.getFrequency() == o2.getFrequency())
            return 0;
        else
            return -1;
    }

}
