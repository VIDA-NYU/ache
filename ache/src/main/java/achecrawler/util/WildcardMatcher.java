package achecrawler.util;

import java.util.List;

/**
 * Tests a string against a list of wildcard patterns and return true if link matches any of the
 * patterns or if the provided list of patterns is empty. The only wildcard character supported is
 * the "*".
 */
public class WildcardMatcher implements TextMatcher {

    private String[] wildcards;
    private boolean reverse;
    private boolean isEmpty;

    private WildcardMatcher(List<String> wildcards, boolean reverse) {
        this.reverse = reverse;
        this.isEmpty = wildcards == null || wildcards.isEmpty();
        if (!isEmpty) {
            this.wildcards = (String[]) wildcards.toArray(new String[wildcards.size()]);
        }
    }

    @Override
    public boolean matches(String text) {
        // "reverse != matchesAll(text)" is equivalent to
        // "reverse ? !matchesAll(text) : matchesAll(text)"
        // without a using a if statement
        return isEmpty || (reverse != matchesAll(text));
    }

    private boolean matchesAll(String text) {
        for (int i = 0; i < wildcards.length; i++) {
            if (wildcardMatch(text, wildcards[i])) {
                return true;
            }
        }
        return false;
    }

    public boolean wildcardMatch(String text, String wildcard) {
        int idxText = 0;
        int idxWildcard = 0;
        int match = 0;
        int star = -1;
        int lenText = text.length();
        int wildcardLength = wildcard.length();
        while (idxText < lenText) {
            if ( idxWildcard < wildcardLength && 
                 (text.charAt(idxText) == wildcard.charAt(idxWildcard)
                  &&
                  wildcard.charAt(idxWildcard) != '*') 
                ) {
                idxText++;
                idxWildcard++;
            } else if (idxWildcard < wildcardLength && wildcard.charAt(idxWildcard) == '*') {
                match = idxText;
                star = idxWildcard;
                idxWildcard++;
            } else if (star != -1) {
                idxWildcard = star + 1;
                match++;
                idxText = match;
            } else {
                return false;
            }
        }
        while (idxWildcard < wildcardLength && wildcard.charAt(idxWildcard) == '*') {
            idxWildcard++;
        }
        if (idxWildcard == wildcardLength) {
            return true;
        } else {
            return false;
        }
    }

    public static WildcardMatcher fromWhitelist(List<String> whitelist) {
        return new WildcardMatcher(whitelist, false);
    }

    public static TextMatcher fromBlacklist(List<String> blacklist) {
        return new WildcardMatcher(blacklist, true);
    }

}
