package focusedCrawler.dedup;

import static java.util.Arrays.asList;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class URLAlignment {
    
    static public class Token implements Comparable<Token> {
        
        final public String token;
        final public char type;

        public Token(String token) {
            this.token = token;
            this.type = type(token);
        }

        private char type(String t) {
            boolean onlyDigits = true;
            boolean onlyLetters = true;
            for (int i = 0; i < t.length(); i++) {
                char ch = t.charAt(i);
                if (!Character.isDigit(ch))
                    onlyDigits = false;
                if (!Character.isLetter(ch))
                    onlyLetters = false;
                if (!onlyDigits && !onlyLetters)
                    return 'p';
            }
            if (onlyDigits)
                return 'd';
            else if (onlyLetters)
                return 'l';
            else
                return 'p';
        }

        public boolean onlyDigits() {
            return type == 'd';
        }

        public boolean onlyLetters() {
            return type == 'l';
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
            return type == 'p';
//            return "/".equals(token) ||
//                   "?".equals(token) ||
//                   "=".equals(token) ||
//                   "&".equals(token) ||
//                   "#".equals(token) ||
//                   ";".equals(token) ||
//                   ":".equals(token) ||
////                   "-".equals(token) ||
//                   ".".equals(token) ;
        }
    }

    @SuppressWarnings("serial")
    static public class TokenSet extends TreeSet<Token> {
        
        private boolean gap = false;

        public TokenSet() {}

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

            if (union.size() > 0)
                return intersection.size() / ((double) union.size());
            else
                return 0d;
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
                if(!t.onlyDigits())
                    return false;
            }
            return true;
        }

        public boolean onlyLetters() {
            for (Token t : this) {
                if(!t.onlyLetters())
                    return false;
            }
            return true;
        }

    }

    @SuppressWarnings("serial")
    public static class Sequence extends ArrayList<TokenSet> {

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
                if(tokenSet.hasGap()) {
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

        public static List<String> parseTokens(String url) {
            if(url == null || url.isEmpty()) {
                return null;
            }
            
            List<String> sequence = new ArrayList<String>();
            StringBuilder token = new StringBuilder();
            
            int i = 0;
            char ch = url.charAt(0);
            token.append(ch);
            
            CharType type = getCharType(ch);
            CharType previousType = type;
            
            i++;
            while(i < url.length()) {
                
                ch = url.charAt(i);
                type = getCharType(ch);
                
                if(type != previousType || previousType.equals(CharType.PUNCTUATION)) {
                    // build token
                    sequence.add(token.toString());
                    token = new StringBuilder();
                }
                token.append(ch);
                
                previousType = type;
                type = getCharType(ch);
                
                i++;
            }
            
            if(token.length() > 0) {
                sequence.add(token.toString());
            }
            
            return sequence;
        }
        
        public static Sequence parse(String url) {
            if(url == null || url.isEmpty()) {
                return null;
            }
            List<String> stringTokens = parseTokens(url);
            Sequence sequence = new Sequence();
            for(String token : stringTokens) {
                sequence.add(new TokenSet(token));
            }
            return sequence;
        }
        
        private static CharType getCharType(char currentChar) {
            if(Character.isDigit(currentChar)) {
                return CharType.DIGIT;
            }
            else if(Character.isLetter(currentChar)) {
                return CharType.ALPHA;
            }
            else {
                return CharType.PUNCTUATION;
            }
        }
        
        private static enum CharType {
            DIGIT, ALPHA, PUNCTUATION
        }

        public static Sequence multipleAlignment(List<String> urls) {
            
            Set<Sequence> sequences = new HashSet<>();
            PriorityQueue<PairwiseAlignment> queue = new PriorityQueue<>(urls.size(), PairwiseAlignment.DESC_BY_SCORE);
            
            for (int i = 0; i < urls.size(); i++) {
                for (int j = i + 1; j < urls.size(); j++) {
                    
                    Sequence sequence1 = parse(urls.get(i));
                    Sequence sequence2 = parse(urls.get(j));
                    
                    sequences.add(sequence1);
                    sequences.add(sequence2);
                    
                    queue.add(new PairwiseAlignment(sequence1, sequence2));
                }
            }
            
//            System.out.println("PAIRWISE");
//            for(PairwiseAlignment alignment : queue) {
//                System.out.println(alignment.score + " "+alignment.consensus.toString());
//            }
//            System.out.println("PAIRWISE END");
//            
//            System.out.println("Aligning...");
            
            Set<Sequence> aligned = new HashSet<>();
            while(!queue.isEmpty()) {
                PairwiseAlignment alignment = queue.poll();
                
                if(!aligned.contains(alignment.url1) && !aligned.contains(alignment.url2)) {
                    
                    aligned.add(alignment.url1);
                    aligned.add(alignment.url2);
                    sequences.remove(alignment.url1);
                    sequences.remove(alignment.url2);
                    
                    for(Sequence s : sequences) {
                        PairwiseAlignment epilson = new PairwiseAlignment(alignment.consensus, s);
                        queue.add(epilson);
                    }
                    
                    sequences.add(alignment.consensus);
                }
            }
            
            Sequence consensus = sequences.iterator().next();
            
            return consensus;
        }

    }
    
    static public class RewriteRule {
        
        
        public final String context;
        public final String transformation;
        private Pattern contextPattern;
        private Pattern transformationPattern;
        private int backreferences;
        private int setCardinality; // cardinality of token set
        
        public RewriteRule(Sequence sequence) {
            this(sequence, 5);
        }
        
        public RewriteRule(Sequence sequence, int setCardinality) {
            this.setCardinality = setCardinality;
            this.backreferences = 0;
            StringBuilder context = new StringBuilder();
            StringBuilder transformation = new StringBuilder();
            
            context.append('^');

            int indexLastProcessed = -1;
            boolean allInvariant = true;

            for (int i = 0; i < sequence.size(); i++) {

                TokenSet token = sequence.get(i);
                
                boolean isUrlDelimiter = token.isUrlDelimiter();
                boolean isInvariant = token.isInvariant();
                
                if (isUrlDelimiter) {
                    // Deal with unprocessed tokens before the URL delimiter
                    if(indexLastProcessed + 1 < i) {
                        processNonUrlDelimiterTokens(sequence, context, transformation,
                                                     indexLastProcessed, allInvariant, i);
                    }
                    
                    // Append URL delimiter token
                    context.append("\\");
                    context.append(token.first().token); // escaping token for regex
                    
                    if(!token.isIrrelevant())
                        transformation.append(token.first());
                    
                    indexLastProcessed = i;
                }
                
                if(isUrlDelimiter) {
                    allInvariant = true;
                } else {
                    if(!isInvariant) {
                        allInvariant = false;
                    }
                }
            }
            
            if(indexLastProcessed < sequence.size() - 1) {
                processNonUrlDelimiterTokens(sequence, context, transformation,
                                             indexLastProcessed, allInvariant, sequence.size());
            }
            
            context.append('$');
            
            this.context = context.toString();
            this.contextPattern = Pattern.compile(this.context);
            
            this.transformation = transformation.toString();
            this.transformationPattern = Pattern.compile("\\$\\d+");
        }

        private void processNonUrlDelimiterTokens(Sequence sequence,
                                                  StringBuilder context,
                                                  StringBuilder transformation,
                                                  int indexLastTokenSetUsed,
                                                  boolean allInvariant,
                                                  int i) {
            if (allInvariant) {
                // generalize all invariant tokens to *
                // PS: matching [a-zA-Z0-9]* is more efficient than .*
                context.append("([a-zA-Z0-9]*)");
                
                // add a back-reference to transformation, so this token can be reused
                backreferences++;
                transformation.append("$");
                transformation.append(backreferences);
            } else {
                // process each token separately
                for (int j = indexLastTokenSetUsed + 1; j < i; j++) {
                    processToken(sequence.get(j), context, transformation);
                }
            }
        }
        
        public void processToken(TokenSet tokenSet, StringBuilder context, StringBuilder transformation) {
            if (tokenSet.isInvariant()) {
                // generalize according to type
                for (Token token : tokenSet) {
                    if (token.onlyDigits()) {
                        context.append("([0-9]+)");
                    } else if (token.onlyLetters()) {
                        context.append("([a-zA-Z]+)");
                    } else {
                        context.append("(");
                        context.append(Pattern.quote(token.token));
                        context.append("(");
                    }
                }
                
                // add a back-reference to transformation, so this token can be reused
                backreferences++;
                transformation.append("$");
                transformation.append(backreferences);
                
            } else if (tokenSet.isVariant()) {
                // all tokens are grouped inside parentheses separated by |
                
                
                
                // generalize to tokens' type
                if(tokenSet.size() > setCardinality && tokenSet.onlyDigits()) {
                    context.append("(?:[0-9]+)");
                } else if (tokenSet.size() > setCardinality && tokenSet.onlyLetters()) {
                    context.append("(?:[a-zA-Z]+)");
                }
                // do not generalize
                else {
                    int count = 0;
                    context.append("(?:"); // non-capturing group, since we don't need back reference
                    for (Token t : tokenSet) {
                        count++;
                        context.append(Pattern.quote(t.token));
                        if (count < tokenSet.size())
                            context.append("|");
                    }
                    context.append(")");
                }
                
                // insert one random token to transformation, so other variations are ignored
                transformation.append(tokenSet.first());
                
            } else if (tokenSet.isIrrelevant()) {
                if(tokenSet.size() == 1) {
                    context.append("(?:");
                    context.append(Pattern.quote(tokenSet.first().token));
                    context.append(")");
                } else {
                    // all tokens are grouped inside square braces separated by |
                    int count = 0;
                    context.append("(?:");
                    for (Token t : tokenSet) {
                        count++;
                        context.append(Pattern.quote(t.token));
                        if (count < tokenSet.size())
                            context.append("|");
                    }
                    context.append(")");
                }
                // do not add to transformation, so irrelevant tokens are left out
            }
        }

        public boolean matches(String url) {
            return contextPattern.matcher(url).matches();
        }

        public String rewrite(String url) {
//            System.out.println("URLAlignment.RewriteRule.rewrite()");
//            System.out.println("url: "+url);
//            System.out.println(this);
            
            Matcher contextMatcher = contextPattern.matcher(url);
            if(!contextMatcher.matches()) {
                throw new IllegalArgumentException("Rewrite rule can't be applied to this URL because the context doesn't match it. Context="+context+" URL="+url);
            }
            
            int contextGroup = 0;            
            StringBuffer builder = new StringBuffer();
            Matcher transformationMatcher = transformationPattern.matcher(transformation);
            while (transformationMatcher.find()) {
                String backref = transformationMatcher.group();
                
                if(contextGroup < contextMatcher.groupCount()) {
                    String replacement = contextMatcher.group(++contextGroup);
//                    System.out.println(backref +" : "+replacement);
                    transformationMatcher.appendReplacement(builder, replacement);
                } else {
                    System.err.println("Didn't find replacement for "+backref);
                }
            }
            transformationMatcher.appendTail(builder);
            
//            System.out.println("rewrite: " + builder.toString());
            return builder.toString();
        }
        
        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append("c: ");
            sb.append(this.context);
            sb.append("\n");
            sb.append("t: ");
            sb.append(this.transformation);
            return sb.toString();
        }
        
        @Override
        public int hashCode() {
            return this.toString().hashCode();
        }
        
        @Override
        public boolean equals(Object o) {
            return this.toString().equals(o.toString());
        }
        
    }

    static public class PairwiseAlignment {

        public Sequence consensus;
        public Sequence url1;
        public Sequence url2;
        public double score;

        public PairwiseAlignment(Sequence sequenceA, Sequence sequenceB) {

            double[][] S = new double[sequenceA.size() + 1][sequenceB.size() + 1];
            char[][] arrow = new char[sequenceA.size() + 1][sequenceB.size() + 1];

            // First of all, fill 1st row/column with zeros
            for (int i = 0; i <= sequenceA.size(); i++)
                S[i][0] = 0;
            for (int j = 0; j <= sequenceB.size(); j++)
                S[0][j] = 0;

            // Compute scores matrix
            for (int i = 1; i <= sequenceA.size(); i++) {
                for (int j = 1; j <= sequenceB.size(); j++) {
                    
                    double diag = S[i - 1][j - 1] + sf(sequenceA.get(i - 1), sequenceB.get(j - 1));
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

            this.consensus = consensus;
            this.url1 = sequenceA;
            this.url2 = sequenceB;
            this.score = S[sequenceA.size()][sequenceB.size()];
        }

        private double sf(TokenSet a, TokenSet b) {
            // FIXME: use jaccard only if both sequences contain at least one token of the same type
            // (right?)
            return TokenSet.jaccard(a, b);
        }
        
        public static Comparator<PairwiseAlignment> DESC_BY_SCORE = new Comparator<PairwiseAlignment>() {
            @Override
            public int compare(PairwiseAlignment o1, PairwiseAlignment o2) {
                return Double.compare(o2.score, o1.score);
            }
        };

    }

    public static void main(String[] args) throws IOException {

        Sequence sequenceA = Sequence.fromTerms("http", "://", "www", ".", "nextechclassifieds", ".", "com", "/", "listings", "/", "send_as_email", "/", "1125374", "/");
        Sequence sequenceB = Sequence.fromTerms("http", "://", "www", ".", "nextechclassifieds", ".", "com", "/", "listings", "/", "report_bad_ad", "/", "1175989", "/");

//        Sequence sequenceA = Sequence.fromTerms("www", ".", "irs", ".", "gov", "/", "foia", "/", "index", ".", "html");
//        Sequence sequenceB = Sequence.fromTerms("www",".","irs",".","ustreas",".","gov","/","foia");
//
//        Sequence sequenceA = Sequence.fromTerms("http", ":", "/", "/", "www", ".", "ex", "/");
//        Sequence sequenceB = Sequence.fromTerms("http", ":", "/", "/", "www", ".", "un", "/", "home");

        new PairwiseAlignment(sequenceA, sequenceB);
        
//        List<String> urls = asList(
//            "http://www.nextechclassifieds.com/account/login/?next=/listings/1188183/",
//            "http://www.nextechclassifieds.com/account/login/?next=/listings/1147073/",
//            "http://www.nextechclassifieds.com/listings/report_bad_ad/1139323/",
//            "http://www.nextechclassifieds.com/listings/send_as_email/1076410/",
//            "http://www.nextechclassifieds.com/listings/send_as_email/927136/",
//            "http://www.nextechclassifieds.com/listings/send_as_email/1095216/",
//            "http://www.nextechclassifieds.com/listings/send_as_email/1182794/",
//            "http://www.nextechclassifieds.com/listings/send_as_email/1186583/",
//            "http://www.nextechclassifieds.com/listings/send_as_email/1184366/",
//            "http://www.nextechclassifieds.com/favorites/add_listing/1003210/",
//            "http://www.nextechclassifieds.com/favorites/add_listing/1140491/",
//            "http://www.nextechclassifieds.com/ratings/rate/38572/",
//            "http://www.nextechclassifieds.com/ratings/rate/79221/"
//        );
        
        List<String> urls = asList(
            "http://arguntrader.com/viewtopic.php?p=159529&sid=757e90770f1134b54be3bc9d3e81a93c",
            "http://arguntrader.com/viewtopic.php?p=159411&sid=757e90770f1134b54be3bc9d3e81a93c",
            "http://arguntrader.com/viewtopic.php?f=53&t=32045&p=160008&sid=757e90770f1134b54be3bc9d3e81a93c",
            "http://arguntrader.com/viewtopic.php?f=54&t=32045&sid=757e90770f1134b54be3bc9d3e81a93c&start=100"
        );
        
//        List<String> urls = asList(
//            "https://www.ar15.com/biz/engine/click.html?id=1392&z=9",
//            "https://www.ar15.com/biz/engine/click.html?id=1391&z=7",
//             "http://www.ar15.com/biz/engine/click.html?id=1392&z=9",
//             "http://www.ar15.com/biz/engine/click.html?id=1391&z=7",
//            "https://www.ar15.com/forums/transfer.html?id=1294&z=18"
//        );
        
//        List<String> urls = asList(
//            "http://www.ar15.com/forums/rss.html?b=2&f=467#sd",
//            "https://www.ar15.com/forums/rss.html?b=2&f=467"
//        );
        
        Sequence consensus = Sequence.multipleAlignment(urls);
        
        System.err.println(consensus);
        
        RewriteRule rewriteRule = new RewriteRule(consensus);
        System.err.println(rewriteRule);
        
        System.out.println(rewriteRule.matches(urls.get(0)));
        System.out.println(rewriteRule.rewrite(urls.get(0)));
    }
}
