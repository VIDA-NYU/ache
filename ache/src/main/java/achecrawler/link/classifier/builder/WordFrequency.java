package achecrawler.link.classifier.builder;

import java.util.Comparator;

public class WordFrequency {

    public static Comparator<WordFrequency> WORD_SIZE_ASC_COMPARATOR =
            new Comparator<WordFrequency>() {
                @Override
                public int compare(WordFrequency o1, WordFrequency o2) {
                    return Integer.compare(o1.getWord().length(), o2.getWord().length());
                }
            };

    public static Comparator<WordFrequency> WORD_FREQUENCY_DESC_COMPARATOR =
            new Comparator<WordFrequency>() {
                @Override
                public int compare(WordFrequency o2, WordFrequency o1) {
                    return Integer.compare(o1.getFrequency(), o1.getFrequency());
                }
            };

    private String word;
    private int frequency;

    public WordFrequency(String word, int frequency) {
        this.word = word;
        this.frequency = frequency;
    }

    public String getWord() {
        return word;
    }

    public int getFrequency() {
        return frequency;
    }

    public void setFrequency(int freq) {
        this.frequency = freq;
    }

    public void setWord(String newWord) {
        word = newWord;
    }

    public void incrementFrequncy(int freq) {
        frequency = frequency + freq;
    }

    public boolean equals(WordFrequency wordFrequency) {
        return this.getWord().equals(wordFrequency.getWord());
    }

    public String toString() {
        return getWord() + ":" + getFrequency();
    }

}
