package achecrawler.util;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Tests a string against a list of regular expression patterns and return true if text matches any
 * of the patterns or if the provided list of patterns is empty.
 */
public class RegexMatcher implements TextMatcher {
    
    private static final Logger logger = LoggerFactory.getLogger(RegexMatcher.class);
    
    private boolean isEmpty;
    private boolean reverse;
    protected List<Pattern> patterns = new ArrayList<Pattern>();
    
    private RegexMatcher(List<String> regexes, boolean reverse) {
        this.reverse = reverse;
        this.isEmpty = regexes == null || regexes.isEmpty();
        if(!this.isEmpty) {
            for (String pattern : regexes) {
                patterns.add(Pattern.compile(pattern, Pattern.CASE_INSENSITIVE | Pattern.DOTALL));
            }
        }
    }

    /** 
     * Performs matching against given String {@code text} and returns true if:
     * <ul>
     *  <li>no patterns were provided</li>
     *  <li>reverse is false and all regexes match the given string 'text'</li>
     *  <li>reverse is true and no regex matches the given text</li>
     *  </ul>
     * @param text
     * @return
     */
    @Override
    public boolean matches(String text) {
        // "reverse != matchesAll(text)" is equivalent to
        // "reverse ? !matchesAll(text) : matchesAll(text)"
        // without a using a if statement
        return isEmpty || (reverse != matchesAll(text));
    }

    private boolean matchesAll(String text) {
        for (Pattern pattern : patterns) {
            if (pattern.matcher(text).matches()) {
                return true;
            }
        }
        return false;
    }

    public static RegexMatcher fromWhitelistFile(String fileName) {
        return new RegexMatcher(loadRegexesFromFile(fileName), false);
    }

    public static RegexMatcher fromBlacklistFile(String fileName) {
        return new RegexMatcher(loadRegexesFromFile(fileName), true);
    }
    
    public static RegexMatcher fromWhitelist(List<String> patterns) {
        return new RegexMatcher(patterns, false);
    }
    
    public static RegexMatcher fromBlacklist(List<String> patterns) {
        return new RegexMatcher(patterns, true);
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
