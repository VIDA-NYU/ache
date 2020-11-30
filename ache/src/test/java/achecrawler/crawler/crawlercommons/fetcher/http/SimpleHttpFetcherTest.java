/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *  
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package achecrawler.crawler.crawlercommons.fetcher.http;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.net.ConnectException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.http.HttpHeaders;
import org.apache.http.HttpStatus;
import org.apache.http.client.CookieStore;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import achecrawler.crawler.async.HttpDownloaderConfig;
import achecrawler.crawler.async.fetcher.FetcherFactory;
import achecrawler.crawler.cookies.Cookie;
import achecrawler.crawler.cookies.CookieUtils;
import achecrawler.crawler.crawlercommons.fetcher.AbortedFetchException;
import achecrawler.crawler.crawlercommons.fetcher.AbortedFetchReason;
import achecrawler.crawler.crawlercommons.fetcher.BaseFetcher;
import achecrawler.crawler.crawlercommons.fetcher.FetchedResult;
import achecrawler.crawler.crawlercommons.fetcher.IOFetchException;
import achecrawler.crawler.crawlercommons.fetcher.Payload;
import achecrawler.crawler.crawlercommons.fetcher.RedirectFetchException;
import achecrawler.crawler.crawlercommons.fetcher.RedirectFetchException.RedirectExceptionReason;
import achecrawler.crawler.crawlercommons.fetcher.http.BaseHttpFetcher.RedirectMode;
import achecrawler.crawler.crawlercommons.test.RandomResponseHandler;
import achecrawler.crawler.crawlercommons.test.ResourcesResponseHandler;
import achecrawler.crawler.crawlercommons.test.SimulationWebServer;
import achecrawler.crawler.crawlercommons.test.TestUtils;

public class SimpleHttpFetcherTest {

    private SimulationWebServer _webServer;

    @Before
    public void setUp() throws Exception {
        _webServer = new SimulationWebServer();
    }

    @After
    public void shutDown() throws Exception {
        _webServer.stopServer();
    }

    private void startServer(Handler handler, int port) throws Exception {
        _webServer.startServer(handler, port);
//        Thread.sleep(100000);
    }

    private void stopServer() throws Exception {
        _webServer.stopServer();
    }

    private Server getServer() {
        return _webServer.getServer();
    }

    // TODO - merge this code with RedirectResponseHandler class in
    // crawlercommons.test package.
    private class RedirectResponseHandler extends AbstractHandler {

        private boolean _permanent;

        public RedirectResponseHandler() {
            this(false);
        }

        public RedirectResponseHandler(boolean permanent) {
            super();
            _permanent = permanent;
        }

        @Override
        public void handle(String pathInContext, Request baseRequest,
                           HttpServletRequest request, HttpServletResponse response) throws IOException {
            if (pathInContext.endsWith("base")) {
                if (_permanent) {
                    // Can't use sendRedirect, as that forces it to be a temp
                    // redirect.
                    response.setStatus(HttpServletResponse.SC_MOVED_PERMANENTLY);
                    response.addHeader("Location", "http://localhost:8089/redirect");
                    response.flushBuffer();
                } else {
                    response.sendRedirect("http://localhost:8089/redirect");
                }
            } else {
                response.setStatus(HttpServletResponse.SC_OK);
                response.setContentType("text/plain");

                String content = "redirected";
                response.setContentLength(content.length());
                response.getOutputStream().write(content.getBytes("UTF-8"));
            }
        }
    }

    private class LanguageResponseHandler extends AbstractHandler {

        private String _englishContent;
        private String _foreignContent;

        public LanguageResponseHandler(String englishContent, String foreignContent) {
            _englishContent = englishContent;
            _foreignContent = foreignContent;
        }

        @Override
        public void handle(String target, Request baseRequest,
                           HttpServletRequest request, HttpServletResponse response)
                           throws IOException, ServletException {
            String language = request.getHeader(HttpHeaders.ACCEPT_LANGUAGE);
            String content;
            if ((language != null) && (language.contains("en"))) {
                content = _englishContent;
            } else {
                content = _foreignContent;
            }

            response.setStatus(HttpStatus.SC_OK);
            response.setContentType("text/plain");

            response.setContentLength(content.length());
            response.getOutputStream().write(content.getBytes("UTF-8"));
        }
    }

    private class MimeTypeResponseHandler extends AbstractHandler {

        private String _mimeType;

        public MimeTypeResponseHandler(String mimeType) {
            _mimeType = mimeType;
        }

        @Override
        public void handle(String pathInContext, Request baseRequest,
                           HttpServletRequest request, HttpServletResponse response) throws IOException {
            String content = "test";
            response.setStatus(HttpStatus.SC_OK);
            if (_mimeType != null) {
                response.setContentType(_mimeType);
            }

            response.setContentLength(content.length());
            response.getOutputStream().write(content.getBytes("UTF-8"));
        }
    }

    @Test
    public final void testConnectionTimeout() throws Exception {
        startServer(new ResourcesResponseHandler(), 8089);
        BaseFetcher fetcher = new SimpleHttpFetcher(1, TestUtils.CC_TEST_AGENT);
        String url = "http://localhost:8088/simple-page.html";

        try {
            fetcher.get(url);
            fail("Exception not thrown");
        } catch (IOFetchException e) {
            assertTrue(e.getCause() instanceof ConnectException);
        }
    }

    @Test
    public final void testStaleConnection() throws Exception {
        startServer(new ResourcesResponseHandler(), 8089);
        ServerConnector sc = (ServerConnector) getServer().getConnectors()[0];
        sc.setSoLingerTime(-1);

        BaseFetcher fetcher = new SimpleHttpFetcher(1, TestUtils.CC_TEST_AGENT);
        String url = "http://localhost:8089/simple-page.html";
        fetcher.get(url);

        // TODO KKr - control keep-alive (linger?) value for Jetty, so we can
        // set it
        // to something short and thus make this sleep delay much shorter.
        Thread.sleep(2000);

        fetcher.get(url);
    }

    @Test
    public final void testSlowServerTermination() throws Exception {
        // Need to read in more than 2 8K blocks currently, due to how
        // HttpClientFetcher
        // is designed...so use 20K bytes. And the duration is 2 seconds, so 10K
        // bytes/sec.
        startServer(new RandomResponseHandler(20000, 2 * 1000L), 8089);

        // Set up for a minimum response rate of 20000 bytes/second.
        BaseHttpFetcher fetcher = new SimpleHttpFetcher(1, TestUtils.CC_TEST_AGENT);
        fetcher.setMinResponseRate(20000);

        String url = "http://localhost:8089/test.html";
        try {
            fetcher.get(url);
            fail("Aborted fetch exception not thrown");
        } catch (AbortedFetchException e) {
            assertEquals(AbortedFetchReason.SLOW_RESPONSE_RATE, e.getAbortReason());
        }
    }

    @Test
    public final void testNotTerminatingSlowServers() throws Exception {
        // Return 1K bytes at 2K bytes/second - would normally trigger an
        // error.
        startServer(new RandomResponseHandler(1000, 500), 8089);

        // Set up for no minimum response rate.
        BaseHttpFetcher fetcher = new SimpleHttpFetcher(1, TestUtils.CC_TEST_AGENT);
        fetcher.setMinResponseRate(BaseHttpFetcher.NO_MIN_RESPONSE_RATE);

        String url = "http://localhost:8089/test.html";
        fetcher.get(url);
    }

    @Test
    public final void testLargeContent() throws Exception {
        BaseFetcher fetcher = new SimpleHttpFetcher(1, TestUtils.CC_TEST_AGENT);
        startServer(new RandomResponseHandler(fetcher.getDefaultMaxContentSize() * 2), 8089);

        String url = "http://localhost:8089/test.html";
        FetchedResult result = fetcher.get(url);
        assertEquals(HttpStatus.SC_OK, result.getStatusCode());
        assertTrue("Content size should be truncated", result.getContent().length <= fetcher.getDefaultMaxContentSize());
    }

    @Test
    public final void testTruncationWithKeepAlive() throws Exception {
        startServer(new ResourcesResponseHandler(), 8089);

        BaseFetcher fetcher = new SimpleHttpFetcher(1, TestUtils.CC_TEST_AGENT);
        fetcher.setDefaultMaxContentSize(1000);
        fetcher.setMaxContentSize("image/png", 5000);
        String urlToFetch = "http://localhost:8089/karlie.html";

        FetchedResult result1 = fetcher.get(urlToFetch);
        assertEquals(HttpStatus.SC_OK, result1.getStatusCode());
        FetchedResult result2 = fetcher.get(urlToFetch);
        assertEquals(HttpStatus.SC_OK, result2.getStatusCode());

        // Verify that we got the same data from each fetch request.
        assertEquals(1000, result1.getContent().length);
        assertEquals(1000, result2.getContent().length);
        byte[] bytes1 = result1.getContent();
        byte[] bytes2 = result2.getContent();
        for (int i = 0; i < bytes1.length; i++) {
            assertEquals(bytes1[i], bytes2[i]);
        }

        urlToFetch = "http://localhost:8089/bixolabs_mining.png";
        FetchedResult result3 = fetcher.get(urlToFetch);
        assertTrue(result3.getContent().length > 1000);

        fetcher.setMaxContentSize("image/png", 1500);
        try {
            fetcher.get(urlToFetch);
            fail("Aborted fetch exception not thrown");
        } catch (AbortedFetchException e) {
            Assert.assertEquals(AbortedFetchReason.CONTENT_SIZE, e.getAbortReason());
        }

    }

    @Test
    public final void testLargeHtml() throws Exception {
        startServer(new ResourcesResponseHandler(), 8089);
        BaseFetcher fetcher = new SimpleHttpFetcher(1, TestUtils.CC_TEST_AGENT);
        String url = "http://localhost:8089/karlie.html";
        FetchedResult result = fetcher.get(url);

        assertTrue("Content size should be truncated", result.getContentLength() <= fetcher.getDefaultMaxContentSize());

    }

    @Test
    public final void testContentTypeHeader() throws Exception {
        startServer(new ResourcesResponseHandler(), 8089);
        BaseFetcher fetcher = new SimpleHttpFetcher(1, TestUtils.CC_TEST_AGENT);
        String url = "http://localhost:8089/simple-page.html";
        FetchedResult result = fetcher.get(url);

        String contentType = result.getHeaders().get(HttpHeaders.CONTENT_TYPE);
        assertNotNull(contentType);
        assertEquals("text/html", contentType);
    }

    @Test
    public final void testTempRedirectHandling() throws Exception {
        startServer(new RedirectResponseHandler(), 8089);
        BaseFetcher fetcher = new SimpleHttpFetcher(1, TestUtils.CC_TEST_AGENT);
        String url = "http://localhost:8089/base";
        FetchedResult result = fetcher.get(url);

        assertEquals("Redirected URL", "http://localhost:8089/redirect", result.getFetchedUrl());
        assertNull(result.getNewBaseUrl());
        assertEquals(1, result.getNumRedirects());
    }

    @Test
    public final void testPermRedirectHandling() throws Exception {
        startServer(new RedirectResponseHandler(true), 8089);
        BaseFetcher fetcher = new SimpleHttpFetcher(1, TestUtils.CC_TEST_AGENT);
        String url = "http://localhost:8089/base";
        Payload payload = new Payload();
        payload.put("payload-field-1", 1);
        FetchedResult result = fetcher.get(url, payload);

        assertEquals("Redirected URL", "http://localhost:8089/redirect", result.getFetchedUrl());
        assertEquals("New base URL", "http://localhost:8089/redirect", result.getNewBaseUrl());
        assertEquals(1, result.getNumRedirects());
        assertEquals(1, result.getPayload().get("payload-field-1"));
    }

    @Test
    public final void testRedirectPolicy() throws Exception {
        startServer(new RedirectResponseHandler(true), 8089);
        BaseHttpFetcher fetcher = new SimpleHttpFetcher(1, TestUtils.CC_TEST_AGENT);
        fetcher.setRedirectMode(RedirectMode.FOLLOW_TEMP);
        String url = "http://localhost:8089/base";

        try {
            fetcher.get(url);
            fail("Exception should have been thrown");
        } catch (RedirectFetchException e) {
            assertEquals("Redirected URL", "http://localhost:8089/redirect", e.getRedirectedUrl());
            assertEquals(RedirectExceptionReason.PERM_REDIRECT_DISALLOWED, e.getReason());
        }

        stopServer();

        // Now try setting the mode to follow none
        startServer(new RedirectResponseHandler(false), 8089);
        fetcher = new SimpleHttpFetcher(1, TestUtils.CC_TEST_AGENT);
        fetcher.setRedirectMode(RedirectMode.FOLLOW_NONE);

        try {
            fetcher.get(url);
            fail("Exception should have been thrown");
        } catch (RedirectFetchException e) {
            assertEquals("Redirected URL", "http://localhost:8089/redirect", e.getRedirectedUrl());
            assertEquals(RedirectExceptionReason.TEMP_REDIRECT_DISALLOWED, e.getReason());
        }

    }

    @Test
    public final void testAcceptLanguage() throws Exception {
        final String englishContent = "English";
        final String foreignContent = "Foreign";

        startServer(new LanguageResponseHandler(englishContent, foreignContent), 8089);
        BaseFetcher fetcher = new SimpleHttpFetcher(1, TestUtils.CC_TEST_AGENT);
        String url = "http://localhost:8089/";
        FetchedResult result = fetcher.get(url);
        String contentStr = new String(result.getContent(), 0, result.getContentLength(), Charset.defaultCharset());
        assertTrue(englishContent.equals(contentStr));
    }

    @Test
    public final void testMimeTypeFiltering() throws Exception {

        startServer(new MimeTypeResponseHandler("text/xml"), 8089);
        BaseFetcher fetcher = new SimpleHttpFetcher(1, TestUtils.CC_TEST_AGENT);
        Set<String> validMimeTypes = new HashSet<String>();
        validMimeTypes.add("text/html");
        fetcher.setValidMimeTypes(validMimeTypes);

        String url = "http://localhost:8089/";

        try {
            fetcher.get(url);
            fail("Fetch should have failed");
        } catch (AbortedFetchException e) {
            assertEquals(AbortedFetchReason.INVALID_MIMETYPE, e.getAbortReason());
        }
    }

    @Test
    public final void testMimeTypeFilteringNoContentType() throws Exception {

        startServer(new MimeTypeResponseHandler(null), 8089);
        BaseFetcher fetcher = new SimpleHttpFetcher(1, TestUtils.CC_TEST_AGENT);
        Set<String> validMimeTypes = new HashSet<String>();
        validMimeTypes.add("text/html");
        validMimeTypes.add(""); // We want unknown (not reported) mime-types
                                // too.
        fetcher.setValidMimeTypes(validMimeTypes);
        String url = "http://localhost:8089/";

        try {
            fetcher.get(url);
        } catch (AbortedFetchException e) {
            fail("Fetch should not have failed if no mime-type is specified");
        }
    }

    @Test
    public final void testMimeTypeFilteringWithCharset() throws Exception {
        startServer(new MimeTypeResponseHandler("text/html; charset=UTF-8"), 8089);
        BaseFetcher fetcher = new SimpleHttpFetcher(1, TestUtils.CC_TEST_AGENT);
        Set<String> validMimeTypes = new HashSet<String>();
        validMimeTypes.add("text/html");
        fetcher.setValidMimeTypes(validMimeTypes);

        String url = "http://localhost:8089/";

        try {
            fetcher.get(url);
        } catch (AbortedFetchException e) {
            fail("Fetch should have worked");
        }
    }

    @Test
    public final void testHostAddress() throws Exception {
        startServer(new ResourcesResponseHandler(), 8089);
        BaseFetcher fetcher = new SimpleHttpFetcher(1, TestUtils.CC_TEST_AGENT);
        String url = "http://localhost:8089/simple-page.html";
        FetchedResult result = fetcher.get(url);

        String hostAddress = result.getHostAddress();
        assertNotNull(hostAddress);
        assertEquals("127.0.0.1", hostAddress);
    }

    @Test
    public final void testMissingPage() throws Exception {
        startServer(new ResourcesResponseHandler(), 8089);
        BaseFetcher fetcher = new SimpleHttpFetcher(1, TestUtils.CC_TEST_AGENT);
        String url = "http://localhost:8089/this-page-will-not-exist.html";

        FetchedResult result = fetcher.get(url);

        assertEquals(HttpStatus.SC_NOT_FOUND, result.getStatusCode());
        assertEquals(url, result.getFetchedUrl());
        assertEquals("127.0.0.1", result.getHostAddress());
        assertTrue(new String(result.getContent(), "UTF-8").contains("Error 404"));
    }

    @Test
    public void testCookieStore() {
        Cookie cookie = new Cookie("key1", "value1");
        cookie.setDomain(".slides.com");

        HashMap<String, List<Cookie>> map = new HashMap<>();
        List<Cookie> listOfCookies = new ArrayList<>();
        listOfCookies.add(cookie);
        map.put("www.slides.com", listOfCookies);

        SimpleHttpFetcher baseFetcher =
                FetcherFactory.createSimpleHttpFetcher(new HttpDownloaderConfig());
        CookieUtils.addCookies(map, baseFetcher);

        CookieStore globalCookieStore = baseFetcher.getCookieStore();
        List<org.apache.http.cookie.Cookie> resultList = globalCookieStore.getCookies();
        assertTrue(resultList.get(0).getName().equals("key1"));
        assertTrue(resultList.get(0).getValue().equals("value1"));
        assertTrue(resultList.get(0).getDomain().equals("slides.com"));
    }
}
