package focusedCrawler.util;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Tests a string against a list of patterns and return true if link matches any of the patterns.
 */
public class RegexMatcher {
    
    private static final Logger logger = LoggerFactory.getLogger(RegexMatcher.class);
    
    List<Pattern> patterns = new ArrayList<Pattern>();

    protected RegexMatcher(String filename) {
        this(loadRegexesFromFile(filename));
    }

    protected RegexMatcher(List<String> textPatterns) {
        for (String pattern : textPatterns) {
            patterns.add(Pattern.compile(pattern, Pattern.CASE_INSENSITIVE | Pattern.DOTALL));
        }
    }

    public boolean matches(String text) {
        for (Pattern pattern : patterns) {
            if (pattern.matcher(text).matches()) {
                return true;
            }
        }
        return false;
    }

    public static RegexMatcher fromFile(String filename) {
        return new RegexMatcher(filename);
    }
    
    public static RegexMatcher fromList(List<String> patterns) {
        return new RegexMatcher(patterns);
    }
    
    private static List<String> loadRegexesFromFile(String filename) {
        List<String> textPatterns = new ArrayList<String>();
        try (BufferedReader br = new BufferedReader(new FileReader(filename))) {
            String line;
            while ((line = br.readLine()) != null) {
                String trimedLine = line.trim();
                if (!trimedLine.equals("")) {
                    textPatterns.add(trimedLine);
                    logger.info(trimedLine);
                }
            }
        } catch (IOException e) {
            logger.warn("Couldn't load patterns from file: " + filename + " Using a empty list.");
        }
        return textPatterns;
    }

}
