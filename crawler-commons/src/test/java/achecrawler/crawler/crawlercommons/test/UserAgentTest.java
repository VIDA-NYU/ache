package achecrawler.crawler.crawlercommons.test;

import static org.junit.Assert.assertEquals;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import achecrawler.crawler.crawlercommons.fetcher.http.UserAgent;


public class UserAgentTest {

    @Before
    public void setUp() throws Exception {}

    @After
    public void tearDown() throws Exception {}

    @Test
    public void buildUserAgentString1() {
        UserAgent ua = new UserAgent.Builder()
            .setAgentName("MyCrawler")
            .setCrawlerVersion("1.0")
            .build();
        assertEquals("Mozilla/5.0 (compatible; MyCrawler/1.0)", ua.getUserAgentString());
        assertEquals("MyCrawler", ua.getAgentName());
    }
    
    @Test
    public void buildUserAgentString2() {
        UserAgent ua = new UserAgent.Builder()
            .setAgentName("MyCrawler")
            .setCrawlerVersion("1.0")
            .setWebAddress("www.mycraler.com/bot.html")
            .build();
        assertEquals("Mozilla/5.0 (compatible; MyCrawler/1.0; +www.mycraler.com/bot.html)", ua.getUserAgentString());
        assertEquals("MyCrawler", ua.getAgentName());
    }

    @Test
    public void buildUserAgentString3() {
        UserAgent ua = new UserAgent.Builder()
            .setAgentName("MyCrawler")
            .setCrawlerVersion("1.0")
            .setEmailAddress("bot@mycraler.com")
            .build();
        assertEquals("Mozilla/5.0 (compatible; MyCrawler/1.0; bot@mycraler.com)", ua.getUserAgentString());
        assertEquals("MyCrawler", ua.getAgentName());
    }
    
    @Test
    public void buildUserAgentString4() {
        UserAgent ua = new UserAgent.Builder()
            .setAgentName("MyCrawler")
            .setCrawlerVersion("1.0")
            .setWebAddress("www.mycraler.com/bot.html")
            .setEmailAddress("bot@mycraler.com")
            .build();
        assertEquals("Mozilla/5.0 (compatible; MyCrawler/1.0; +www.mycraler.com/bot.html bot@mycraler.com)", ua.getUserAgentString());
        assertEquals("MyCrawler", ua.getAgentName());
    }
    
    @Test
    public void buildUserAgentString5() {
        UserAgent ua = new UserAgent.Builder()
            .setAgentName("MyCrawler")
            .setCrawlerVersion("1.0")
            .setWebAddress("www.mycraler.com/bot.html")
            .setUserAgentString("Mozilla/5.0 (Linux; Android 6.0.1; Nexus 5X Build/MMB29P)")
            .build();
        assertEquals("Mozilla/5.0 (Linux; Android 6.0.1; Nexus 5X Build/MMB29P)", ua.getUserAgentString());
        assertEquals("MyCrawler", ua.getAgentName());
    }
}
