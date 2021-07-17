package achecrawler.tokenizers;

import java.util.ArrayList;
import java.util.List;

/**
 * Simple text tokenizer that breaks text into alpha-numeric tokens. Tokens are any contiguous
 * sequence of letters or any sequence of numbers. Any other character is considered punctuation and
 * broke down into its own token of size 1.
 * 
 * As an example, 'ex123.com/' is tokenized to: {'ex', '123', '.', 'com', '/'}.
 * 
 * @author aeciosantos
 *
 */
public class AlphaNumTokenizer implements Tokenizer {
    
    public static final AlphaNumTokenizer INSTANCE = new AlphaNumTokenizer();

    private enum CharType {
        DIGIT, ALPHA, PUNCTUATION, SPACE
    }

    @Override
    public List<String> tokenize(String text) {
        return parseTokens(text);
    }

    public static List<String> parseTokens(String text) {
        if (text == null || text.isEmpty()) {
            return null;
        }

        List<String> sequence = new ArrayList<>();
        StringBuilder token = new StringBuilder();

        int i = 0;
        char ch = text.charAt(0);
        token.append(ch);

        CharType type = getCharType(ch);
        CharType previousType = type;

        i++;
        while (i < text.length()) {
            ch = text.charAt(i);
            type = getCharType(ch);

            if (type != previousType || previousType.equals(CharType.PUNCTUATION)) {
                // build token
                if (token.length() > 0) {
                    sequence.add(token.toString());
                    token = new StringBuilder();
                }
            }

            if (type != CharType.SPACE) {
                token.append(ch);
            }

            previousType = type;
            i++;
        }

        if (token.length() > 0) {
            sequence.add(token.toString());
        }

        return sequence;
    }


    private static CharType getCharType(char currentChar) {
        if (Character.isDigit(currentChar)) {
            return CharType.DIGIT;
        } else if (Character.isLetter(currentChar)) {
            return CharType.ALPHA;
        } else if (currentChar == ' ') {
            return CharType.SPACE;
        } else {
            return CharType.PUNCTUATION;
        }
    }

}
