package achecrawler.util.string;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Stopwords {

    public static final Stopwords DEFAULT;

    private static final int MIN_LENGTH = 2;
    private static final int MAX_LENGTH = 30;

    private final Set<String> stopwords;

    static {
        String filename = "stopwords.en.txt";
        try (InputStream f = Stopwords.class.getClassLoader().getResourceAsStream(filename)) {
            DEFAULT = new Stopwords(f);
        } catch (IOException e) {
            throw new RuntimeException("Failed to load stop words file");
        }
    }

    public Stopwords(String filename) throws IOException {
        this(Files.newInputStream(Paths.get(filename)));
    }

    public Stopwords(InputStream file) throws IOException {
        this.stopwords = parseFiles(file);
    }

    /**
     * Checks if word is irrelevant. A word is considered irrelevant if:
     * (1) it is included in the stop words list or
     * (2) it has length smaller than {@value MIN_LENGTH} or
     * (3) it has length greater than {@value MAX_LENGTH}.
     *
     * @param word the word to be checked
     * @return true if the word is irrelevant, false otherwise.
     */
    public boolean isIrrelevant(String word) {
        if (word == null) {
            return true;
        }
        word = word.trim();
        int size = word.length();
        if (size < MIN_LENGTH || size > MAX_LENGTH) {
            return true;
        }
      return stopwords.contains(word);
    }

    public Set<String> parseFiles(InputStream file) throws IOException {
        Set<String> stopwords = new HashSet<>();
        for (String line : readLines(file)) {
            stopwords.add(line.trim());
        }
        return stopwords;
    }

    private List<String> readLines(InputStream fileStream) throws IOException {
        if(fileStream == null) {
            throw new IllegalArgumentException("Input stream can't be null");
        }
        List<String> lines = new ArrayList<>();
        try (BufferedReader in = new BufferedReader(new InputStreamReader(fileStream)) ) {
            for (String temp = in.readLine(); temp != null; temp = in.readLine()) {
                lines.add(temp);
            }
        }
        return lines;
    }
}