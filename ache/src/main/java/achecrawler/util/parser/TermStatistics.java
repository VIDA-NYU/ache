package achecrawler.util.parser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import achecrawler.util.string.StopList;

public class TermStatistics {

    private boolean noindex = false;
    private boolean sortTerms = true;
    private StopList stoplist;

    private Map<String, List<Integer>> wordPositions = new HashMap<>();
    private Map<String, List<Integer>> wordPositionsMeta = new HashMap<>();
    private Map<String, Integer> wordScores = new HashMap<>();

    private List<String> text = new ArrayList<>();
    private List<String> textMeta = new ArrayList<String>();

    private String[] words = new String[0];
    private int[] occurrences = new int[0];
    private String[] wordsMeta = new String[0];
    private int[] occurrencesMeta = new int[0];

    public TermStatistics() {
        this(null, false);
    }

    public TermStatistics(StopList stoplist, boolean noindex) {
        this.stoplist = stoplist;
        this.noindex = noindex;
    }

    protected void addTermScore(String term, int score) {
        if (noindex) {
            return;
        }
        term = term.toLowerCase();
        boolean domain = term.startsWith("#") && term.endsWith("#");
        if (!isIrrelevant(term) || domain) {
            Integer wordScore = wordScores.get(term);
            if (wordScore == null) {
                wordScore = score;
            } else {
                wordScore = wordScore.intValue() + score;
            }
            wordScores.put(term, wordScore);
        }
    }

    public boolean addTermToText(String term) {
        if (noindex) {
            return false;
        }
        term = term.toLowerCase().trim();
        boolean isRelevant = !isIrrelevant(term);
        if (isRelevant) {
            text.add(term);
            return true;
        } else {
            return false;
        }
    }

    public void addTermToMeta(String term, int termCount) {
        textMeta.add(term);
        for (int i = 0; i < termCount; i++) {
            addTermMetaPosition(term, textMeta.size());
        }
    }

    protected void addTermPosition(String term, int position) {
        if (noindex) {
            return;
        }
        term = term.toLowerCase();
        boolean domain = term.startsWith("#") && term.endsWith("#");
        if (!isIrrelevant(term) || domain) {
            List<Integer> positions = wordPositions.get(term);
            if (positions == null) {
                if (!isIrrelevant(term)) {
                    positions = new ArrayList<>();
                    wordPositions.put(term, positions);
                    positions.add(position);
                }
            } else {
                positions.add(position);
            }
        }
    }

    protected void addTermMetaPosition(String term, int pos) {
        if (noindex) {
            return;
        }
        term = term.toLowerCase();
        List<Integer> positionsMeta = wordPositionsMeta.get(term);
        if (positionsMeta == null) {
            if (!isIrrelevant(term)) {
                positionsMeta = new ArrayList<>();
                wordPositionsMeta.put(term, positionsMeta);
                positionsMeta.add(new Integer(pos));
            }
        } else {
            positionsMeta.add(new Integer(pos));
        }
    }

    public boolean isIrrelevant(String str) {
        if (stoplist != null) {
            return stoplist.isIrrelevant(str);
        } else {
            return false;
        }
    }

    public void setNoindex(boolean value) {
        this.noindex = value;
    }

    protected void sortData() {

        int size = wordScores.size();
        String[] words = new String[size];
        int[] numbers = new int[size];

        int sizeMeta = wordPositionsMeta.size();
        String[] wordsMeta = new String[sizeMeta];
        int[] numbersMeta = new int[sizeMeta];

        int i = 0;
        for (Entry<String, Integer> entry : wordScores.entrySet()) {
            words[i] = entry.getKey();
            numbers[i] = entry.getValue();
            i++;
        }

        i = 0;
        for (Entry<String, List<Integer>> entry : wordPositionsMeta.entrySet()) {
            wordsMeta[i] = entry.getKey();
            numbersMeta[i] = entry.getValue().size();
            i++;
        }

        if (sortTerms) {
            // Sorts both arrays in descendant order
            sortOccurences(numbers, words);
            sortOccurences(numbersMeta, wordsMeta);
        }

        this.words = words;
        this.occurrences = numbers;
        this.wordsMeta = wordsMeta;
        this.occurrencesMeta = numbersMeta;
    }

    public String[] words() {
        return words;
    }

    public String wordsAsString() {
        if (words.length == 0)
            return "";
        String text = words[0];
        for (int i = 1; i < words.length; i++) {
            text = text + "  " + words[i];
        }
        return text;
    }

    public int[] occurrences() {
        return occurrences;
    }

    public String[] wordsMeta() {
        return wordsMeta;
    }

    public int[] occurrencesMeta() {
        return occurrencesMeta;
    }

    /**
     * Sorts two parallel arrays based on the decreasing ordering of values of the first (int)
     * array.
     */
    private void sortOccurences(int a[], String b[]) {
        quicksort(a, 0, a.length - 1, b);
    }

    private void quicksort(int a[], int left, int right, String b[]) {
        int pivot;
        int l = left;
        int r = right;

        if (left < right) {
            pivot = a[(left + right) / 2];

            while (l <= r) {
                while (a[l] > pivot & l < right) {
                    l++;
                }

                while (a[r] < pivot & r > left) {
                    r--;
                }

                if (l <= r) {
                    swapInt(a, l, r);
                    swapString(b, l, r);
                    l++;
                    r--;
                }
            }

            if (left < r) {
                quicksort(a, left, r, b);
            }

            if (l < right) {
                quicksort(a, l, right, b);
            }
        }
    }

    private void swapInt(int a[], int i, int j) {
        int temp = a[i];
        a[i] = a[j];
        a[j] = temp;
    }

    private void swapString(String a[], int i, int j) {
        String temp = a[i];
        a[i] = a[j];
        a[j] = temp;
    }

}
