package focusedCrawler.dedup.rules;

import focusedCrawler.dedup.rules.Sequence.Token;
import focusedCrawler.dedup.rules.Sequence.TokenSet;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RewriteRule {

    public final String context;
    public final String transformation;
    private Pattern contextPattern;
    private Pattern transformationPattern;
    private int backreferences;
    private int setCardinality; // cardinality of token set

    public RewriteRule(List<String> urls) {
        this(createRewriteRule(urls, 10));
    }

    public RewriteRule(List<String> urls, int maxUrlsForAlignment) {
        this(createRewriteRule(urls, maxUrlsForAlignment));
    }

    private static Sequence createRewriteRule(List<String> urls, int maxUrlsForAlignment) {
        int size = Math.min(urls.size(), maxUrlsForAlignment);
        Sequence alignment = Sequence.multipleAlignment(urls.subList(0, size));
        return alignment;
    }

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
                if (indexLastProcessed + 1 < i) {
                    processNonUrlDelimiterTokens(sequence, context, transformation,
                            indexLastProcessed, allInvariant, i);
                }

                // Append URL delimiter token
                context.append("\\");
                context.append(token.first().token); // escaping token for regex

                if (!token.isIrrelevant()) {
                    transformation.append(token.first());
                }

                indexLastProcessed = i;
            }

            if (isUrlDelimiter) {
                allInvariant = true;
            } else {
                if (!isInvariant) {
                    allInvariant = false;
                }
            }
        }

        if (indexLastProcessed < sequence.size() - 1) {
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

    public void processToken(TokenSet tokenSet, StringBuilder context,
            StringBuilder transformation) {
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
            if (tokenSet.size() > setCardinality && tokenSet.onlyDigits()) {
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
                    if (count < tokenSet.size()) {
                        context.append("|");
                    }
                }
                context.append(")");
            }

            // insert one random token to transformation, so other variations are ignored
            transformation.append(tokenSet.first());

        } else if (tokenSet.isIrrelevant()) {
            if (tokenSet.size() == 1) {
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
                    if (count < tokenSet.size()) {
                        context.append("|");
                    }
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

        Matcher contextMatcher = contextPattern.matcher(url);
        if (!contextMatcher.matches()) {
            throw new IllegalArgumentException(
                    "Rewrite rule can't be applied to this URL because the context doesn't match it. Context="
                            + context + " URL=" + url);
        }

        int contextGroup = 0;
        StringBuffer builder = new StringBuffer();
        Matcher transformationMatcher = transformationPattern.matcher(transformation);
        while (transformationMatcher.find()) {
            String backref = transformationMatcher.group();

            if (contextGroup < contextMatcher.groupCount()) {
                String replacement = contextMatcher.group(++contextGroup);
                transformationMatcher.appendReplacement(builder, replacement);
            } else {
                System.err.println("Didn't find replacement for " + backref);
            }
        }
        transformationMatcher.appendTail(builder);

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
