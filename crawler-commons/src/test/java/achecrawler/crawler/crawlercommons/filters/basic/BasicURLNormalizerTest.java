package achecrawler.crawler.crawlercommons.filters.basic;

import java.util.Arrays;
import java.util.List;
import java.util.TreeSet;

import org.junit.Assert;
import org.junit.Test;

/** Unit tests for BasicURLNormalizer. */
public class BasicURLNormalizerTest {
	
    private BasicURLNormalizer normalizer;

    public BasicURLNormalizerTest() {
        normalizer = new BasicURLNormalizer();
    }

    @Test
    public void testNUTCH1098() throws Exception {
        // check that % encoding is normalized
        normalizeTest("http://foo.com/%66oo.html", "http://foo.com/foo.html");

        // check that % encoding works correctly at end of URL
        normalizeTest("http://foo.com/%66oo.htm%6c", "http://foo.com/foo.html");
        normalizeTest("http://foo.com/%66oo.ht%6dl", "http://foo.com/foo.html");

        // check that % decoder do not overlap strings
        normalizeTest("http://foo.com/%66oo.ht%6d%6c", "http://foo.com/foo.html");

        // check that % decoder leaves high bit chars alone
        normalizeTest("http://foo.com/%66oo.htm%C0", "http://foo.com/foo.htm%C0");

        // check that % decoder leaves control chars alone
        normalizeTest("http://foo.com/%66oo.htm%1A", "http://foo.com/foo.htm%1A");

        // check that % decoder converts to upper case letters
        normalizeTest("http://foo.com/%66oo.htm%c0", "http://foo.com/foo.htm%C0");

        // check that % decoder leaves encoded spaces alone
        normalizeTest("http://foo.com/you%20too.html", "http://foo.com/you%20too.html");

        // check that spaces are encoded into %20
        normalizeTest("http://foo.com/you too.html", "http://foo.com/you%20too.html");

        // check that encoded # are not decoded
        normalizeTest("http://foo.com/file.html%23cz", "http://foo.com/file.html%23cz");

        // check that encoded / are not decoded
        normalizeTest("http://foo.com/fast/dir%2fcz", "http://foo.com/fast/dir%2Fcz");

        // check that control chars are encoded
        normalizeTest("http://foo.com/\u001a!", "http://foo.com/%1A!");

        // check that control chars are always encoded into 2 digits
        normalizeTest("http://foo.com/\u0001!", "http://foo.com/%01!");

        // check encoding of spanish chars
        normalizeTest("http://mydomain.com/en Espa\u00F1ol.aspx", "http://mydomain.com/en%20Espa%C3%B1ol.aspx");
    }

    @Test
    public void testNUTCH2064() throws Exception {
        // Ampersand and colon and other punctuation characters are not to be
        // unescaped
        normalizeTest("http://x.com/s?q=a%26b&m=10", "http://x.com/s?m=10&q=a%26b");
        normalizeTest("http://x.com/show?http%3A%2F%2Fx.com%2Fb", "http://x.com/show?http%3A%2F%2Fx.com%2Fb");
        normalizeTest("http://google.com/search?q=c%2B%2B", "http://google.com/search?q=c%2B%2B");
        // do also not touch the query part which is
        // application/x-www-form-urlencoded
        normalizeTest("http://x.com/s?q=a+b", "http://x.com/s?q=a+b");
        // and keep Internationalized domain names
        // http://bücher.de/ may be http://xn--bcher-kva.de/
        // but definitely not http://b%C3%BCcher.de/
        normalizeTest("http://b\u00fccher.de/", "http://b\u00fccher.de/");
        // test whether percent-encoding works together with other
        // normalizations
        normalizeTest("http://x.com/./a/../%66.html", "http://x.com/f.html");
        // [ and ] need escaping as well
        normalizeTest("http://x.com/?x[y]=1", "http://x.com/?x%5By%5D=1");
        // boundary test for first character outside the ASCII range (U+0080)
        normalizeTest("http://x.com/foo\u0080", "http://x.com/foo%C2%80");
        normalizeTest("http://x.com/foo%c2%80", "http://x.com/foo%C2%80");
    }

    @Test
    public void testNormalizer() throws Exception {
        // check that leading and trailing spaces are removed
        normalizeTest(" http://foo.com/ ", "http://foo.com/");

        // check that protocol is lower cased
        normalizeTest("HTTP://foo.com/", "http://foo.com/");

        // check that host is lower cased
        normalizeTest("http://Foo.Com/index.html", "http://foo.com/index.html");
        normalizeTest("http://Foo.Com/index.html", "http://foo.com/index.html");

        // check that port number is normalized
        normalizeTest("http://foo.com:80/index.html", "http://foo.com/index.html");
        normalizeTest("http://foo.com:81/", "http://foo.com:81/");

        // check that null path is normalized
        normalizeTest("http://foo.com", "http://foo.com/");

        // check that references are removed
        normalizeTest("http://foo.com/foo.html#ref", "http://foo.com/foo.html");

        // // check that encoding is normalized
        normalizeTest("http://foo.com/%66oo.html", "http://foo.com/foo.html");

        // check that unnecessary "../" are removed

        normalizeTest("http://foo.com/aa/./foo.html", "http://foo.com/aa/foo.html");
        normalizeTest("http://foo.com/aa/../", "http://foo.com/");
        normalizeTest("http://foo.com/aa/bb/../", "http://foo.com/aa/");
        normalizeTest("http://foo.com/aa/..", "http://foo.com/");
        normalizeTest("http://foo.com/aa/bb/cc/../../foo.html", "http://foo.com/aa/foo.html");
        normalizeTest("http://foo.com/aa/bb/../cc/dd/../ee/foo.html", "http://foo.com/aa/cc/ee/foo.html");
        normalizeTest("http://foo.com/../foo.html", "http://foo.com/foo.html");
        normalizeTest("http://foo.com/../../foo.html", "http://foo.com/foo.html");
        normalizeTest("http://foo.com/../aa/../foo.html", "http://foo.com/foo.html");
        normalizeTest("http://foo.com/aa/../../foo.html", "http://foo.com/foo.html");
        normalizeTest("http://foo.com/aa/../bb/../foo.html/../../", "http://foo.com/");
        normalizeTest("http://foo.com/../aa/foo.html", "http://foo.com/aa/foo.html");
        normalizeTest("http://foo.com/../aa/../foo.html", "http://foo.com/foo.html");
        normalizeTest("http://foo.com/a..a/foo.html", "http://foo.com/a..a/foo.html");
        normalizeTest("http://foo.com/a..a/../foo.html", "http://foo.com/foo.html");
        normalizeTest("http://foo.com/foo.foo/../foo.html", "http://foo.com/foo.html");
        normalizeTest("http://foo.com//aa/bb/foo.html", "http://foo.com/aa/bb/foo.html");
        normalizeTest("http://foo.com/aa//bb/foo.html", "http://foo.com/aa/bb/foo.html");
        normalizeTest("http://foo.com/aa/bb//foo.html", "http://foo.com/aa/bb/foo.html");
        normalizeTest("http://foo.com//aa//bb//foo.html", "http://foo.com/aa/bb/foo.html");
        normalizeTest("http://foo.com////aa////bb////foo.html", "http://foo.com/aa/bb/foo.html");
        normalizeTest("http://foo.com/aa?referer=http://bar.com", "http://foo.com/aa?referer=http%3A%2F%2Fbar.com");
    }

    @Test
    public void testQueryParametersReordering() throws Exception {
        normalizeTest("http://foo.com/index?a=1&b=2", "http://foo.com/index?a=1&b=2");
        normalizeTest("http://foo.com/index?b=2&a=1", "http://foo.com/index?a=1&b=2");
    }
    
    @Test
    public void testRemoveSessionQueryParameters() throws Exception {
    	List<String> invalidParameters = Arrays.asList("sid","phpsessid","sessionid", "jsessionid");
		normalizer = new BasicURLNormalizer(new TreeSet<>(invalidParameters), true);
        normalizeTest("http://foo.com/foo.php?phpsessid=2Aa3ASdfasfdadf&a=1", "http://foo.com/foo.php?a=1");
        normalizeTest("http://foo.com/foo.php?phpsessid=2Aa3ASdfasfdadf", "http://foo.com/foo.php");
        normalizeTest("http://foo.com/index.jsp;jsessionid=1", "http://foo.com/index.jsp");
    }

    private void normalizeTest(String weird, String normal) throws Exception {
        Assert.assertEquals("normalizing: " + weird, normal, normalizer.filter(weird));
    }

    public static void main(String[] args) throws Exception {
        new BasicURLNormalizerTest().testNormalizer();
    }

}