package focusedCrawler.dedup.rules;

import static java.util.Arrays.asList;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import focusedCrawler.dedup.rules.Sequence.TokenSet;
import java.util.List;
import org.junit.Test;

public class RewriteRuleTest {

    @Test
    public void shouldAlignMultipleUrls() {
        // given
        List<String> urls = asList(
                "a.b.u.c.r",
                "a.b.v.c.s",
                "a.b.w.c.t",
                "a.b.x.c.o",
                "a.b.y.c.p",
                "a.b.z.c.m",
                "a.b.z.c.n"
        );

        // when
        Sequence s = Sequence.multipleAlignment(urls);

        // then
        assertThat(s.get(0).first().token, is("a"));
        assertThat(s.get(1).first().token, is("."));
        assertThat(s.get(2).first().token, is("b"));
        assertThat(s.get(3).first().token, is("."));
        assertThat(s.get(4).size(), is(6));
        assertThat(s.get(5).first().token, is("."));
        assertThat(s.get(6).first().token, is("c"));
        assertThat(s.get(7).first().token, is("."));
        assertThat(s.get(8).size(), is(7));

        // when
        RewriteRule rule = new RewriteRule(s);

        // then
        for (String url : urls) {
            assertThat(rule.matches(url), is(true));
            assertThat(rule.rewrite(url), is("a.b.u.c.m"));
        }
    }

    @Test
    public void shouldGenerateRegexContextFromSentence() {
        // given
        Sequence s = new Sequence();
        s.add(new TokenSet("http"));
        s.add(new TokenSet(":"));
        s.add(new TokenSet("/"));
        s.add(new TokenSet("/"));
        s.add(new TokenSet("www"));

        TokenSet t = new TokenSet("1", "2", "3");
        t.addGap();
        s.add(t);

        s.add(new TokenSet("."));
        s.add(new TokenSet("ex", "EX"));
        s.add(new TokenSet("."));
        s.add(new TokenSet("edu"));
        s.add(new TokenSet("/"));
        s.add(new TokenSet("a", "b"));
        s.add(new TokenSet("/"));
        s.add(new TokenSet("c", "d"));

        // when
        RewriteRule rewriteRule = new RewriteRule(s);
        // then
//        assertThat(rewriteRule.context, is("^*://([a-zA-Z]+)[1|2|3].(EX|ex).*/(a|b)/(c|d)$"));

        assertThat(rewriteRule.matches("http://www1.ex.edu/a/c"), is(true));
        assertThat(rewriteRule.matches("http://www2.ex.edu/a/d"), is(true));
        assertThat(rewriteRule.matches("http://www3.ex.edu/b/d"), is(true));
    }

    @Test
    public void shouldGenerateRewriteRuleFromSenquence() {
        // given
        Sequence s = new Sequence();
        s.add(new TokenSet("http"));
        s.add(new TokenSet(":"));
        s.add(new TokenSet("/"));
        s.add(new TokenSet("/"));
        s.add(new TokenSet("www"));
        TokenSet t = new TokenSet("1", "2", "3");
        t.addGap();
        s.add(t);
        s.add(new TokenSet("."));
        s.add(new TokenSet("EX", "ex"));
        s.add(new TokenSet("."));
        s.add(new TokenSet("edu"));
        s.add(new TokenSet("/"));
        s.add(new TokenSet("a", "b"));
        s.add(new TokenSet("/"));
        s.add(new TokenSet("c", "d"));

        // when
        RewriteRule rule = new RewriteRule(s);

        // then
//        assertThat(rule.context, is("^*://([a-zA-Z]+)[1|2|3].(EX|ex).*/(a|b)/(c|d)$"));
        assertThat(rule.transformation, is("$1://$2.EX.$3/a/c"));

        assertThat(rule.matches("http://www1.ex.edu/a/c"), is(true));
        assertThat(rule.matches("http://www2.ex.edu/a/d"), is(true));
        assertThat(rule.matches("http://www3.ex.edu/b/d"), is(true));

        assertThat(rule.rewrite("http://www1.ex.edu/a/c"), is("http://www.EX.edu/a/c"));
        assertThat(rule.rewrite("http://www2.ex.edu/a/d"), is("http://www.EX.edu/a/c"));
        assertThat(rule.rewrite("http://www3.ex.edu/b/d"), is("http://www.EX.edu/a/c"));
    }

    @Test
    public void shouldAlignAndGenerateRewriteRule() {
        // given
        Sequence alignment = Sequence.multipleAlignment(asList(
            "https://example.com/?id=5",
            "http://example.com/?id=5",
            "http://example.com/index.php?id=5"
        ));

        // when
        RewriteRule rule = new RewriteRule(alignment);

        // then
        assertThat(rule.rewrite("https://example.com/index.php?id=6"), is("http://example.com/?id=6"));
    }

}
