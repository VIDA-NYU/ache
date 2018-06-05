package focusedCrawler.dedup.rules;

import static java.util.Arrays.asList;

import focusedCrawler.dedup.rules.Sequence.TokenSet;
import focusedCrawler.tokenizers.AlphaNumTokenizer;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Set;
import java.util.TreeSet;

@SuppressWarnings("serial")
public class Sequence extends ArrayList<TokenSet> {

    static public class Token implements Comparable<Token> {

        enum Type {
            DIGIT, LETTER, PUNCTUATION
        }

        final public String token;
        final public Type type;

        public Token(String token) {
            this.token = token;
            this.type = type(token);
        }

        private Type type(String t) {
            boolean onlyDigits = true;
            boolean onlyLetters = true;
            for (int i = 0; i < t.length(); i++) {
                char ch = t.charAt(i);
                if (!Character.isDigit(ch)) {
                    onlyDigits = false;
                }
                if (!Character.isLetter(ch)) {
                    onlyLetters = false;
                }
                if (!onlyDigits && !onlyLetters) {
                    return Type.PUNCTUATION;
                }
            }
            if (onlyDigits) {
                return Type.DIGIT;
            } else if (onlyLetters) {
                return Type.LETTER;
            } else {
                return Type.PUNCTUATION;
            }
        }

        public boolean onlyDigits() {
            return type == Type.DIGIT;
        }

        public boolean onlyLetters() {
            return type == Type.LETTER;
        }

        @Override
        public String toString() {
            return token;
        }

        @Override
        public int compareTo(Token o) {
            return this.token.compareTo(o.token);
        }

        public boolean isUrlDelimiter() {
            return type == Type.PUNCTUATION;
        }

    }

    @SuppressWarnings("serial")
    static public class TokenSet extends TreeSet<Token> {

        private boolean gap = false;

        public TokenSet() {
        }

        public TokenSet(TokenSet t) {
            super(t);
        }

        public TokenSet(String... t) {
            for (int i = 0; i < t.length; i++) {
                this.add(new Token(t[i]));
            }
        }

        public TokenSet(String s) {
            this.add(new Token(s));
        }

        public void addGap() {
            this.gap = true;
        }

        private boolean hasGap() {
            return gap;
        }

        public static double jaccard(TokenSet aSet, TokenSet bSet) {
            TokenSet union = new TokenSet();
            union.addAll(aSet);
            union.addAll(bSet);

            TokenSet intersection = new TokenSet(aSet);
            intersection.retainAll(bSet);

            if (union.size() > 0) {
                return intersection.size() / ((double) union.size());
            } else {
                return 0d;
            }
        }

        public boolean isUrlDelimiter() {
            return this.size() == 1 && this.first().isUrlDelimiter();
        }

        public boolean isInvariant() {
            return this.size() == 1 && !hasGap();
        }

        public boolean isVariant() {
            return this.size() > 1 && !hasGap();
        }

        public boolean isIrrelevant() {
            return hasGap();
        }

        public boolean onlyDigits() {
            for (Token t : this) {
                if (!t.onlyDigits()) {
                    return false;
                }
            }
            return true;
        }

        public boolean onlyLetters() {
            for (Token t : this) {
                if (!t.onlyLetters()) {
                    return false;
                }
            }
            return true;
        }

    }

    static public class PairwiseAlignment {

        public Sequence consensus;
        public Sequence url1;
        public Sequence url2;
        public double score;

        public PairwiseAlignment(Sequence sequenceA, Sequence sequenceB, Sequence consensus,
                double score) {
            this.consensus = consensus;
            this.url1 = sequenceA;
            this.url2 = sequenceB;
            this.score = score;
        }


        public static Comparator<PairwiseAlignment> DESC_BY_SCORE = new Comparator<PairwiseAlignment>() {
            @Override
            public int compare(PairwiseAlignment o1, PairwiseAlignment o2) {
                return Double.compare(o2.score, o1.score);
            }
        };

    }

    public static Sequence fromTerms(String... terms) {
        return fromTerms(asList(terms));
    }

    public static Sequence fromTerms(List<String> terms) {
        Sequence sequence = new Sequence();
        for (String term : terms) {
            sequence.add(new TokenSet(term));
        }
        return sequence;
    }

    @Override
    public String toString() {
        StringBuilder s = new StringBuilder();
        s.append('<');
        for (int i = 0; i < this.size(); i++) {
            TokenSet tokenSet = get(i);
            s.append('{');
            int tokenCount = 1;
            for (Token token : tokenSet) {
                s.append(token);
                if (tokenCount < tokenSet.size()) {
                    s.append(',');
                }
                tokenCount++;
            }
            if (tokenSet.hasGap()) {
                s.append(',');
                s.append('λ');
            }
            s.append('}');
        }
        s.append('>');
        return s.toString();
    }

    @Override
    public int hashCode() {
        return this.toString().hashCode();
    }

    @Override
    public boolean equals(Object o) {
        return this.toString().equals(o.toString());
    }

    public static Sequence parse(String url) {
        if (url == null || url.isEmpty()) {
            return null;
        }
        List<String> stringTokens = AlphaNumTokenizer.parseTokens(url);
        Sequence sequence = new Sequence();
        for (String token : stringTokens) {
            sequence.add(new TokenSet(token));
        }
        return sequence;
    }

    public static PairwiseAlignment pairwiseAlignment(Sequence sequenceA, Sequence sequenceB) {

        double[][] S = new double[sequenceA.size() + 1][sequenceB.size() + 1];
        char[][] arrow = new char[sequenceA.size() + 1][sequenceB.size() + 1];

        // First of all, fill 1st row/column with zeros
        for (int i = 0; i <= sequenceA.size(); i++) {
            S[i][0] = 0;
        }
        for (int j = 0; j <= sequenceB.size(); j++) {
            S[0][j] = 0;
        }

        // Compute scores matrix
        for (int i = 1; i <= sequenceA.size(); i++) {
            for (int j = 1; j <= sequenceB.size(); j++) {

                double diag =
                        S[i - 1][j - 1] + similarity(sequenceA.get(i - 1), sequenceB.get(j - 1));
                double left = S[i][j - 1];
                double up = S[i - 1][j];

                double score = Math.max(Math.max(diag, left), up);

                S[i][j] = score;

                if (score == diag) {
                    arrow[i][j] = 'd'; // diagonal
                } else if (score == left) {
                    arrow[i][j] = 'l'; // left
                } else if (score == up) {
                    arrow[i][j] = 'u'; // up
                }

            }
        }

        // Traceback optimal path
        List<TokenSet> tokens = new ArrayList<>();
        int i = sequenceA.size() - 1;
        int j = sequenceB.size() - 1;
        while (i >= 0 && j >= 0) {
            char direction = arrow[i + 1][j + 1];
            TokenSet union = new TokenSet();
            if (direction == 'u') {
                union.addAll(sequenceA.get(i));
                union.addGap();
                i--;

            } else if (direction == 'l') {
                union.addAll(sequenceB.get(j));
                union.addGap();
                j--;
            } else if (direction == 'd') {
                union.addAll(sequenceA.get(i));
                union.addAll(sequenceB.get(j));
                i--;
                j--;
            } else {
                throw new IllegalStateException("Should never happen.");
            }
            tokens.add(union);
        }

        // Recreate sequence reversing the order
        Sequence consensus = new Sequence();
        for (int k = tokens.size() - 1; k >= 0; k--) {
            consensus.add(tokens.get(k));
        }

        double score = S[sequenceA.size()][sequenceB.size()];

        return new PairwiseAlignment(sequenceA, sequenceB, consensus, score);
    }

    /* FIXME: use jaccard only if both sequences contain at least one token of the same type (right?) */
    private static double similarity(TokenSet a, TokenSet b) {
        return TokenSet.jaccard(a, b);
    }

    public static Sequence multipleAlignment(List<String> urls) {

        Set<Sequence> sequences = new HashSet<>();
        PriorityQueue<PairwiseAlignment> queue =
                new PriorityQueue<>(urls.size(), PairwiseAlignment.DESC_BY_SCORE);

        for (int i = 0; i < urls.size(); i++) {
            for (int j = i + 1; j < urls.size(); j++) {

                Sequence sequence1 = parse(urls.get(i));
                Sequence sequence2 = parse(urls.get(j));

                sequences.add(sequence1);
                sequences.add(sequence2);

                queue.add(pairwiseAlignment(sequence1, sequence2));
            }
        }

        Set<Sequence> aligned = new HashSet<>();
        while (!queue.isEmpty()) {
            PairwiseAlignment alignment = queue.poll();

            if (!aligned.contains(alignment.url1) && !aligned.contains(alignment.url2)) {

                aligned.add(alignment.url1);
                aligned.add(alignment.url2);
                sequences.remove(alignment.url1);
                sequences.remove(alignment.url2);

                for (Sequence s : sequences) {
                    PairwiseAlignment epilson = pairwiseAlignment(alignment.consensus, s);
                    queue.add(epilson);
                }

                sequences.add(alignment.consensus);
            }
        }

        Sequence consensus = sequences.iterator().next();

        return consensus;
    }

}
