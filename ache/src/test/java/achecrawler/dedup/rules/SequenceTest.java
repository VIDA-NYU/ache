package achecrawler.dedup.rules;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import achecrawler.tokenizers.AlphaNumTokenizer;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.junit.Test;

public class SequenceTest {

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

}
