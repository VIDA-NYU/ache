package focusedCrawler.dedup;

import static java.util.Arrays.asList;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.Test;

import focusedCrawler.dedup.rules.UrlAlignment.RewriteRule;
import focusedCrawler.dedup.rules.UrlAlignment.Sequence;
import focusedCrawler.dedup.rules.UrlAlignment.TokenSet;
import focusedCrawler.util.AlphaNumTokenizer;

public class URLAlignmentTest {
    
    @Test
    public void shouldParseUrl0() {
        // given
        String url = "http";
        // when
        Sequence s = Sequence.parse(url);
        // then
        assertThat(s.size(), is(1));
        assertThat(s.get(0).first().token, is("http"));
    }

    @Test
    public void shouldParseUrl1() {
        // given
        String url = "http://ex.com/1.htm";
        // when
        Sequence s = Sequence.parse(url);
        // then
        assertThat(s.get(0).first().token, is("http"));
        assertThat(s.get(1).first().token, is(":"));
        assertThat(s.get(2).first().token, is("/"));
        assertThat(s.get(3).first().token, is("/"));
        assertThat(s.get(4).first().token, is("ex"));
        assertThat(s.get(5).first().token, is("."));
        assertThat(s.get(6).first().token, is("com"));
        assertThat(s.get(7).first().token, is("/"));
        assertThat(s.get(8).first().token, is("1"));
        assertThat(s.get(9).first().token, is("."));
        assertThat(s.get(10).first().token, is("htm"));
    }

    @Test
    public void shouldParseTextAndIgnoreSpaces() {
        // given
        String text = "asdf  qwer zxcv";
        // when
        List<String> s = new AlphaNumTokenizer().tokenize(text);
        // then
        assertThat(s.get(0), is("asdf"));
        assertThat(s.get(1), is("qwer"));
        assertThat(s.get(2), is("zxcv"));
    }

    @Test
    public void shouldParseUrl2() {
        // given
        String url = "http://ex.net/add_user/4321/?next=/listings/1234/";
        // when
        Sequence s = Sequence.parse(url);
        // then
        assertThat(s.get(0).first().token, is("http"));
        assertThat(s.get(1).first().token, is(":"));
        assertThat(s.get(2).first().token, is("/"));
        assertThat(s.get(3).first().token, is("/"));
        assertThat(s.get(4).first().token, is("ex"));
        assertThat(s.get(5).first().token, is("."));
        assertThat(s.get(6).first().token, is("net"));
        assertThat(s.get(7).first().token, is("/"));
        assertThat(s.get(8).first().token, is("add"));
        assertThat(s.get(9).first().token, is("_"));
        assertThat(s.get(10).first().token, is("user"));
        assertThat(s.get(11).first().token, is("/"));
        assertThat(s.get(12).first().token, is("4321"));
        assertThat(s.get(13).first().token, is("/"));
        assertThat(s.get(14).first().token, is("?"));
        assertThat(s.get(15).first().token, is("next"));
        assertThat(s.get(16).first().token, is("="));
        assertThat(s.get(17).first().token, is("/"));
        assertThat(s.get(18).first().token, is("listings"));
        assertThat(s.get(19).first().token, is("/"));
        assertThat(s.get(20).first().token, is("1234"));
        assertThat(s.get(21).first().token, is("/"));
    }

    @Test
    public void shouldBeComparedCorrectly() {
        // given
        Sequence s1 = Sequence.parse("a.b.u.c.r");
        Sequence s2 = Sequence.parse("a.b.u.c.r");

        Set<Sequence> aligned = new HashSet<>();

        // when
        aligned.add(s1);

        // then
        assertThat(aligned.contains(s2), is(true));
    }

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

//    @Test
//    public void shouldAlignAndGenerateRewriteRule1() {
//        // given
//        Sequence alignment = Sequence.multipleAlignment(asList(
//            "http://comprar.vlume.com.br/cpm-22/",
//            "http://www.vlumi.com.br/cpm-22/",
//            "http://www.vlume.com.br/cpm-22/"
//        ));
//        // when
//        RewriteRule rule = new RewriteRule(alignment);
//        
//        // then
//        assertThat(rule.rewrite("http://www.vlume.com.br/d-black/"), is("http://comprar.vlume.com.br/d-black/"));
//        assertThat(rule.rewrite("http://www.vlumi.com.br/test/"),    is("http://comprar.vlume.com.br/test/"));
//    }
//    
//    @Test
//    public void shouldAlignAndGenerateRewriteRule2() {
//        // given
//        Sequence alignment = Sequence.multipleAlignment(asList(
//             "http://www.asdf.com/engine/click.html?id=123&z=1",
//            "https://www.asdf.com/engine/click.html?id=234&z=2",
//             "http://www.asdf.com/engine/click.html?id=345&z=3",
//            "https://www.asdf.com/engine/click.html?id=456&z=4"
//        ));
//        // when
//        RewriteRule rule = new RewriteRule(alignment);
//        System.out.println(rule.matches("https://www.ar15.com/forums/t_1_2/1854214_____________________________________________________________________________________________________.html"));
//        // then
//        assertThat(
//            rule.rewrite("https://www.asdf.com/engine/click.html?id=999&z=123"),
//            is("http://www.asdf.com/engine/click.html?id=123&z=1")
//        );
//        assertThat(
//            rule.rewrite("https://www.asdf.com/engine/click.html?id=988&z=10"),
//            is("http://www.asdf.com/engine/click.html?id=123&z=1")
//        );
//    }
//    
//    @Test
//    public void shouldAlignAndGenerateRewriteRule3() {
//        // given
//        Sequence alignment = Sequence.multipleAlignment(asList(
//            "https://example.com/?id=5",
//            "http://example.com/?id=5",
//            "http://example.com/index.php?id=5"
//        ));
//        
//        // when
//        RewriteRule rule = new RewriteRule(alignment);
//        
//        // then
//        assertThat(rule.rewrite("https://example.com/index.php?id=6"), is("http://example.com/?id=6"));
//    }
    
//    @Test
//    public void shouldAlignAndGenerateRewriteRule4() {
//        // given
//        Sequence alignment = Sequence.multipleAlignment(asList(
//            "http://www.firearmstalk.com/images/3/0/7/5/0/ar-15-3127-full.jpg",
//            "http://www.firearmstalk.com/images/3/0/7/5/0/ar-15-3132-full.jpg"
//        ));
//        
//        System.out.println(alignment);
//        
//        // when
//        RewriteRule rule = new RewriteRule(alignment);
//        System.out.println(rule);
//        
//        // then
//        assertThat(rule.rewrite("http://www.firearmstalk.com/images/3/0/7/5/0/ar-15-3127-full.jpg"), is("http://example.com/?id=6"));
//    }
}
