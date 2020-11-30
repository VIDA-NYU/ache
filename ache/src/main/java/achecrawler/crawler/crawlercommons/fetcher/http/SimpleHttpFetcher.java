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

import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLHandshakeException;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;

import org.apache.commons.io.IOUtils;
import org.apache.http.Header;
import org.apache.http.HeaderElement;
import org.apache.http.HeaderElementIterator;
import org.apache.http.HttpClientConnection;
import org.apache.http.HttpEntity;
import org.apache.http.HttpEntityEnclosingRequest;
import org.apache.http.HttpException;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpHost;
import org.apache.http.HttpInetConnection;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.HttpVersion;
import org.apache.http.NoHttpResponseException;
import org.apache.http.ProtocolException;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.CookieStore;
import org.apache.http.client.HttpRequestRetryHandler;
import org.apache.http.client.RedirectException;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.ConnectionKeepAliveStrategy;
import org.apache.http.conn.HttpClientConnectionManager;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.cookie.Cookie;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.DefaultRedirectStrategy;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.message.BasicHeader;
import org.apache.http.message.BasicHeaderElementIterator;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HTTP;
import org.apache.http.protocol.HttpContext;
import org.apache.http.protocol.HttpCoreContext;
import org.apache.http.protocol.HttpRequestExecutor;
import org.apache.tika.metadata.Metadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import achecrawler.crawler.cookies.ConcurrentCookieJar;
import achecrawler.crawler.crawlercommons.fetcher.AbortedFetchException;
import achecrawler.crawler.crawlercommons.fetcher.AbortedFetchReason;
import achecrawler.crawler.crawlercommons.fetcher.BadProtocolFetchException;
import achecrawler.crawler.crawlercommons.fetcher.BaseFetchException;
import achecrawler.crawler.crawlercommons.fetcher.EncodingUtils;
import achecrawler.crawler.crawlercommons.fetcher.EncodingUtils.ExpandedResult;
import achecrawler.crawler.crawlercommons.fetcher.FetchedResult;
import achecrawler.crawler.crawlercommons.fetcher.IOFetchException;
import achecrawler.crawler.crawlercommons.fetcher.Payload;
import achecrawler.crawler.crawlercommons.fetcher.RedirectFetchException;
import achecrawler.crawler.crawlercommons.fetcher.RedirectFetchException.RedirectExceptionReason;
import achecrawler.crawler.crawlercommons.fetcher.UrlFetchException;

@SuppressWarnings("serial")
public class SimpleHttpFetcher extends BaseHttpFetcher {
    private static Logger LOGGER = LoggerFactory.getLogger(SimpleHttpFetcher.class);

    // We tried 10 seconds for all of these, but got a number of connection/read
    // timeouts for
    // sites that would have eventually worked, so bumping it up to 30 seconds.
    private static final int DEFAULT_SOCKET_TIMEOUT = 30 * 1000;

    // As of HttpComponents v.4.2.1, this will also include timeout needed to
    // get Connection from Pool.
    // From initial comment of the deprecated 'CONNECTION_POOL_TIMEOUT' static
    // element:
    // "This normally doesen't ever hit this timeout, since we manage the number
    // of
    // fetcher threads to be <= the maxThreads value used to configure a
    // HttpFetcher. However the limit of connections/host can cause a timeout,
    // when redirects cause multiple threads to hit the same domain.
    // We therefore jack this right up."
    private static final int DEFAULT_CONNECTION_TIMEOUT = 100 * 1000;

    private static final int DEFAULT_MAX_THREADS = 1;

    private static final int BUFFER_SIZE = 8 * 1024;
    private static final int DEFAULT_MAX_RETRY_COUNT = 10;

    private static final int DEFAULT_BYTEARRAY_SIZE = 32 * 1024;

    // Use the same values as Firefox (except that we don't accept deflate,
    // which we're not sure is implemented correctly - see the notes in
    // EncodingUtils/EncodingUtilsTest for more details).
    private static final String DEFAULT_ACCEPT = "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8";
    private static final String DEFAULT_ACCEPT_CHARSET = "utf-8,ISO-8859-1;q=0.7,*;q=0.7";
    private static final String DEFAULT_ACCEPT_ENCODING = "x-gzip, gzip";

    // Keys used to access data in the Http execution context.
    private static final String PERM_REDIRECT_CONTEXT_KEY = "perm-redirect";
    private static final String REDIRECT_COUNT_CONTEXT_KEY = "redirect-count";
    private static final String HOST_ADDRESS = "host-address";

    // To be polite, set it small; if we use it, we will use less than a second
    // delay between subsequent fetches
    private static final int DEFAULT_KEEP_ALIVE_DURATION = 5000;

    private IdleConnectionMonitorThread monitor;
    
    // Store cookies loaded from configuration file
    private CookieStore globalCookieStore = null;


    private static final String SSL_CONTEXT_NAMES[] = { "TLS", "Default", "SSL", };

    private static final String TEXT_MIME_TYPES[] = { "text/html", "application/x-asp", "application/xhtml+xml", "application/vnd.wap.xhtml+xml", };

    private HttpVersion _httpVersion;
    private int _socketTimeout;
    private int _connectionTimeout;
    private int _connectionRequestTimeout;
    private int _maxRetryCount;
    
    private HttpHost proxy;

    transient private CloseableHttpClient _httpClient;
    transient private PoolingHttpClientConnectionManager _connectionManager;

    

    private static class MyRequestRetryHandler implements HttpRequestRetryHandler {
        private int _maxRetryCount;

        public MyRequestRetryHandler(int maxRetryCount) {
            _maxRetryCount = maxRetryCount;
        }

        @Override
        public boolean retryRequest(IOException exception, int executionCount, HttpContext context) {
            if (LOGGER.isTraceEnabled()) {
                LOGGER.trace("Decide about retry #" + executionCount + " for exception " + exception.getMessage());
            }

            if (executionCount >= _maxRetryCount) {
                // Do not retry if over max retry count
                return false;
            } else if (exception instanceof NoHttpResponseException) {
                // Retry if the server dropped connection on us
                return true;
            } else if (exception instanceof SSLHandshakeException) {
                // Do not retry on SSL handshake exception
                return false;
            }

            HttpRequest request = (HttpRequest) context.getAttribute(HttpCoreContext.HTTP_REQUEST);
            boolean idempotent = !(request instanceof HttpEntityEnclosingRequest);
            // Retry if the request is considered idempotent
            return idempotent;
        }
    }

    private static class MyRedirectException extends RedirectException {

        private URI _uri;
        private RedirectExceptionReason _reason;

        public MyRedirectException(String message, URI uri, RedirectExceptionReason reason) {
            super(message);
            _uri = uri;
            _reason = reason;
        }

        public URI getUri() {
            return _uri;
        }

        public RedirectExceptionReason getReason() {
            return _reason;
        }
    }

    /**
     * Handler to record last permanent redirect (if any) in context.
     * 
     */
    private static class MyRedirectStrategy extends DefaultRedirectStrategy {

        private RedirectMode _redirectMode;

        public MyRedirectStrategy(RedirectMode redirectMode) {
            super();

            _redirectMode = redirectMode;
        }

        @Override
        public URI getLocationURI(final HttpRequest request, final HttpResponse response, final HttpContext context) throws ProtocolException {
            URI result = super.getLocationURI(request, response, context);

            // HACK - some sites return a redirect with an explicit port number
            // that's the same as
            // the default port (e.g. 80 for http), and then when you use this
            // to make the next
            // request, the presence of the port in the domain triggers another
            // redirect, so you
            // fail with a circular redirect error. Avoid that by converting the
            // port number to
            // -1 in that case.
            //
            // Detailed scenrio:
            // http://www.test.com/MyPage ->
            // http://www.test.com:80/MyRedirectedPage ->
            // http://www.test.com/MyRedirectedPage
            // We can save bandwidth:
            if (result.getScheme().equalsIgnoreCase("http") && (result.getPort() == 80)) {
                try {
                    result = new URI(result.getScheme(), result.getUserInfo(), result.getHost(), -1, result.getPath(), result.getQuery(), result.getFragment());
                } catch (URISyntaxException e) {
                    LOGGER.warn("Unexpected exception removing port from URI", e);
                }
            }

            // Keep track of the number of redirects.
            Integer count = (Integer) context.getAttribute(REDIRECT_COUNT_CONTEXT_KEY);
            if (count == null) {
                count = new Integer(0);
            }

            context.setAttribute(REDIRECT_COUNT_CONTEXT_KEY, count + 1);

            // Record the last permanent redirect
            int statusCode = response.getStatusLine().getStatusCode();
            if (statusCode == HttpStatus.SC_MOVED_PERMANENTLY) {
                context.setAttribute(PERM_REDIRECT_CONTEXT_KEY, result);
            }

            RedirectExceptionReason reason = null;

            if (_redirectMode == RedirectMode.FOLLOW_NONE) {
                switch (statusCode) {
                    case HttpStatus.SC_MOVED_TEMPORARILY:
                    reason = RedirectExceptionReason.TEMP_REDIRECT_DISALLOWED;
                        break;
                    case HttpStatus.SC_MOVED_PERMANENTLY:
                    reason = RedirectExceptionReason.PERM_REDIRECT_DISALLOWED;
                        break;
                    case HttpStatus.SC_TEMPORARY_REDIRECT:
                    reason = RedirectExceptionReason.TEMP_REDIRECT_DISALLOWED;
                        break;
                    case HttpStatus.SC_SEE_OTHER:
                    reason = RedirectExceptionReason.SEE_OTHER_DISALLOWED;
                        break;
                    default:
                }
            }

            if (_redirectMode == RedirectMode.FOLLOW_TEMP) {
                switch (statusCode) {
                    case HttpStatus.SC_MOVED_PERMANENTLY:
                    reason = RedirectExceptionReason.PERM_REDIRECT_DISALLOWED;
                        break;
                    case HttpStatus.SC_SEE_OTHER:
                    reason = RedirectExceptionReason.SEE_OTHER_DISALLOWED;
                        break;
                    default:
                }
            }

            if (reason != null)
                throw new MyRedirectException("RedirectMode disallowed redirect: " + _redirectMode, result, reason);

            return result;
        }
    }

    /**
     * HttpExecutor to record host address in context.
     */
    static class MyHttpRequestExecutor extends HttpRequestExecutor {
        @Override
        public HttpResponse execute(HttpRequest request, HttpClientConnection conn, HttpContext context)
                throws IOException, HttpException {
            HttpInetConnection connection = (HttpInetConnection) conn;
            context.setAttribute(HOST_ADDRESS, connection.getRemoteAddress().getHostAddress());
            return super.execute(request, conn, context);
        }
        
    }

    private static class DummyX509TrustManager implements X509TrustManager {
        private X509TrustManager standardTrustManager = null;

        /**
         * Constructor for DummyX509TrustManager.
         */
        public DummyX509TrustManager(KeyStore keystore) throws NoSuchAlgorithmException, KeyStoreException {
            super();
            String algo = TrustManagerFactory.getDefaultAlgorithm();
            TrustManagerFactory factory = TrustManagerFactory.getInstance(algo);
            factory.init(keystore);
            TrustManager[] trustmanagers = factory.getTrustManagers();
            if (trustmanagers.length == 0) {
                throw new NoSuchAlgorithmException(algo + " trust manager not supported");
            }
            this.standardTrustManager = (X509TrustManager) trustmanagers[0];
        }

        /**
         * @see javax.net.ssl.X509TrustManager#checkClientTrusted(X509Certificate[],
         *      String)
         */
        @SuppressWarnings("unused")
        public boolean isClientTrusted(X509Certificate[] certificates) {
            return true;
        }

        /**
         * @see javax.net.ssl.X509TrustManager#checkServerTrusted(X509Certificate[],
         *      String)
         */
        @SuppressWarnings("unused")
        public boolean isServerTrusted(X509Certificate[] certificates) {
            return true;
        }

        /**
         * @see javax.net.ssl.X509TrustManager#getAcceptedIssuers()
         */
        public X509Certificate[] getAcceptedIssuers() {
            return this.standardTrustManager.getAcceptedIssuers();
        }

        public void checkClientTrusted(X509Certificate[] arg0, String arg1) throws CertificateException {
            // do nothing

        }

        public void checkServerTrusted(X509Certificate[] arg0, String arg1) throws CertificateException {
            // do nothing

        }
    }

    public static class MyConnectionKeepAliveStrategy implements ConnectionKeepAliveStrategy {

        public long getKeepAliveDuration(HttpResponse response, HttpContext context) {
            if (response == null) {
                throw new IllegalArgumentException("HTTP response may not be null");
            }
            HeaderElementIterator it = new BasicHeaderElementIterator(response.headerIterator(HTTP.CONN_KEEP_ALIVE));
            while (it.hasNext()) {
                HeaderElement he = it.nextElement();
                String param = he.getName();
                String value = he.getValue();
                if (value != null && param.equalsIgnoreCase("timeout")) {
                    try {
                        return Long.parseLong(value) * 1000;
                    } catch (NumberFormatException ignore) {
                    }
                }
            }
            return DEFAULT_KEEP_ALIVE_DURATION;
        }
    }

    public static class IdleConnectionMonitorThread extends Thread {

        private final HttpClientConnectionManager connMgr;

        public IdleConnectionMonitorThread(HttpClientConnectionManager connMgr) {
            super();
            this.connMgr = connMgr;
            this.setDaemon(true);
        }

        @Override
        public void run() {
            while (!interrupted()) {
                // Close expired connections
                connMgr.closeExpiredConnections();
                // Optionally, close connections
                // that have been idle longer than 30 sec
                connMgr.closeIdleConnections(30, TimeUnit.SECONDS);
                try {
                    // TODO is it better to implement as
                    // Thread.currentThread().sleep(30000);
                    // and add a javac declaration?
                    Thread.currentThread();
                    Thread.sleep(30000);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }
    }

    public SimpleHttpFetcher(UserAgent userAgent) {
        this(DEFAULT_MAX_THREADS, userAgent);
    }

    public SimpleHttpFetcher(int maxThreads, UserAgent userAgent) {
        super(maxThreads, userAgent);

        _httpVersion = HttpVersion.HTTP_1_1;
        _socketTimeout = DEFAULT_SOCKET_TIMEOUT;
        _connectionTimeout = DEFAULT_CONNECTION_TIMEOUT;
        _maxRetryCount = DEFAULT_MAX_RETRY_COUNT;

        // Just to be explicit, we rely on lazy initialization of this so that
        // we don't have to worry about serializing it.
        _httpClient = null;
    }

    public HttpVersion getHttpVersion() {
        return _httpVersion;
    }

    public void setHttpVersion(HttpVersion httpVersion) {
        if (_httpClient == null) {
            _httpVersion = httpVersion;
        } else {
            throw new IllegalStateException("Can't change HTTP version after HttpClient has been initialized");
        }
    }

    public int getSocketTimeout() {
        return _socketTimeout;
    }

    public void setSocketTimeout(int socketTimeoutInMs) {
        if (_httpClient == null) {
            _socketTimeout = socketTimeoutInMs;
        } else {
            throw new IllegalStateException("Can't change socket timeout after HttpClient has been initialized");
        }
    }

    public int getConnectionTimeout() {
        return _connectionTimeout;
    }

    public void setConnectionTimeout(int connectionTimeoutInMs) {
        if (_httpClient == null) {
            _connectionTimeout = connectionTimeoutInMs;
        } else {
            throw new IllegalStateException("Can't change connection timeout after HttpClient has been initialized");
        }
    }

    public int getConnectionRequestTimeout() {
        return _connectionRequestTimeout;
    }

    public void setConnectionRequestTimeout(int _connectionRequestTimeoutInMs) {
        if (_httpClient == null) {
            _connectionRequestTimeout = _connectionRequestTimeoutInMs;
        } else {
            throw new IllegalStateException("Can't change connection request timeout after HttpClient has been initialized");
        }
    }

    public int getMaxRetryCount() {
        return _maxRetryCount;
    }

    public void setMaxRetryCount(int maxRetryCount) {
        _maxRetryCount = maxRetryCount;
    }

    public void setCookieStore(CookieStore cookieStore) {
        globalCookieStore = cookieStore;
    }

    @Override
    public FetchedResult get(String url, Payload payload) throws BaseFetchException {
        try {
            URL realUrl = new URL(url);
            String protocol = realUrl.getProtocol();
            if (!protocol.equals("http") && !protocol.equals("https")) {
                throw new BadProtocolFetchException(url);
            }
        } catch (MalformedURLException e) {
            throw new UrlFetchException(url, e.getMessage());
        }

        return request(new HttpGet(), url, payload);
    }

    private FetchedResult request(HttpRequestBase request, String url, Payload payload) throws BaseFetchException {
        init();

        try {
            return doRequest(request, url, payload);
//        } catch (HttpFetchException e) {
//            // Don't bother generating a trace for a 404 (not found)
//            if (LOGGER.isTraceEnabled() && (e.getHttpStatus() != HttpStatus.SC_NOT_FOUND)) {
//                LOGGER.trace("Exception fetching {} {}", url, e.getMessage());
//            }
//
//            throw e;
        } catch (AbortedFetchException e) {
            // Don't bother reporting that we bailed because the mime-type
            // wasn't one that we wanted.
            if (e.getAbortReason() != AbortedFetchReason.INVALID_MIMETYPE) {
                LOGGER.debug("Exception fetching {} {}", url, e.getMessage());
            }
            throw e;
        } catch (BaseFetchException e) {
            LOGGER.debug("Exception fetching {} {}", url, e.getMessage());
            throw e;
        }
    }

    public FetchedResult fetch(String url) throws BaseFetchException {
        return fetch(new HttpGet(), url, new Payload());
    }

    public FetchedResult fetch(HttpRequestBase request, String url, Payload payload) throws BaseFetchException {
        init();

        try {
            return doRequest(request, url, payload);
        } catch (BaseFetchException e) {
            if (LOGGER.isTraceEnabled()) {
                LOGGER.trace("Exception fetching {} {}", url, e.getMessage());
            }
            throw e;
        }
    }

    private FetchedResult doRequest(HttpRequestBase request, String url, Payload payload) throws BaseFetchException {
        LOGGER.trace("Fetching " + url);

        HttpResponse response;
        long readStartTime;
        Metadata headerMap = new Metadata();
        String redirectedUrl = null;
        String newBaseUrl = null;
        int numRedirects = 0;
        boolean needAbort = true;
        String contentType = "";
        String mimeType = "";
        String hostAddress = null;
        int statusCode = HttpStatus.SC_INTERNAL_SERVER_ERROR;
        String reasonPhrase = null;

        // Create a local instance of cookie store, and bind to local context
        // Without this we get killed w/lots of threads, due to sync() on single
        // cookie store.
        HttpContext localContext = new BasicHttpContext();
        localContext.setAttribute(HttpClientContext.COOKIE_STORE, globalCookieStore);

        StringBuilder fetchTrace = null;
        if (LOGGER.isTraceEnabled()) {
            fetchTrace = new StringBuilder("Fetched url: " + url);
        }

        try {
            request.setURI(new URI(url));

            readStartTime = System.currentTimeMillis();
            response = _httpClient.execute(request, localContext);

            Header[] headers = response.getAllHeaders();
            for (Header header : headers) {
                headerMap.add(header.getName(), header.getValue());
            }

            statusCode = response.getStatusLine().getStatusCode();
            reasonPhrase = response.getStatusLine().getReasonPhrase();

            if (LOGGER.isTraceEnabled()) {
                fetchTrace.append("; status code: " + statusCode);
                if (headerMap.get(HttpHeaders.CONTENT_LENGTH) != null) {
                    fetchTrace.append("; Content-Length: " + headerMap.get(HttpHeaders.CONTENT_LENGTH));
                }

                if (headerMap.get(HttpHeaders.LOCATION) != null) {
                    fetchTrace.append("; Location: " + headerMap.get(HttpHeaders.LOCATION));
                }
            }

//            if ((statusCode < 200) || (statusCode >= 300)) {
//                // We can't just check against SC_OK, as some wackos return 201,
//                // 202, etc
//                throw new HttpFetchException(url, "Error fetching " + url + " due to \"" + reasonPhrase + "\"", statusCode, headerMap);
//            }

            redirectedUrl = extractRedirectedUrl(url, localContext);

            URI permRedirectUri = (URI) localContext.getAttribute(PERM_REDIRECT_CONTEXT_KEY);
            if (permRedirectUri != null) {
                newBaseUrl = permRedirectUri.toURL().toExternalForm();
            }

            Integer redirects = (Integer) localContext.getAttribute(REDIRECT_COUNT_CONTEXT_KEY);
            if (redirects != null) {
                numRedirects = redirects.intValue();
            }

            hostAddress = (String) (localContext.getAttribute(HOST_ADDRESS));
            if (hostAddress == null) {
                throw new UrlFetchException(url, "Host address not saved in context");
            }

            Header cth = response.getFirstHeader(HttpHeaders.CONTENT_TYPE);
            if (cth != null) {
                contentType = cth.getValue();
            }

            // Check if we should abort due to mime-type filtering. Note that
            // this will fail if the server
            // doesn't report a mime-type, but that's how we want it as this
            // configuration is typically
            // used when only a subset of parsers are installed/enabled, so we
            // don't want the auto-detect
            // code in Tika to get triggered & try to process an unsupported
            // type. If you want unknown
            // mime-types from the server to be processed, set "" as one of the
            // valid mime-types in
            // FetcherPolicy.
            mimeType = getMimeTypeFromContentType(contentType);
            Set<String> mimeTypes = getValidMimeTypes();
            if ((mimeTypes != null) && (mimeTypes.size() > 0)) {
                if (!mimeTypes.contains(mimeType)) {
                    throw new AbortedFetchException(url, "Invalid mime-type: " + mimeType, AbortedFetchReason.INVALID_MIMETYPE);
                }
            }

            needAbort = false;
        } catch (ClientProtocolException e) {
            // Oleg guarantees that no abort is needed in the case of an
            // IOException
            // (which is is a subclass of)
            needAbort = false;

            // If the root case was a "too many redirects" error, we want to map
            // this to a specific
            // exception that contains the final redirect.
            if (e.getCause() instanceof MyRedirectException) {
                MyRedirectException mre = (MyRedirectException) e.getCause();
                String redirectUrl = url;

                try {
                    redirectUrl = mre.getUri().toURL().toExternalForm();
                } catch (MalformedURLException e2) {
                    LOGGER.warn("Invalid URI saved during redirect handling: " + mre.getUri());
                }

                throw new RedirectFetchException(url, redirectUrl, mre.getReason());
            } else if (e.getCause() instanceof RedirectException) {
                LOGGER.error(e.getMessage());
                throw new RedirectFetchException(url, extractRedirectedUrl(url, localContext), RedirectExceptionReason.TOO_MANY_REDIRECTS);
            } else {
                throw new IOFetchException(url, e);
            }
        } catch (IOException e) {
            // Oleg guarantees that no abort is needed in the case of an
            // IOException
            needAbort = false;
            throw new IOFetchException(url, e);
        } catch (URISyntaxException e) {
            throw new UrlFetchException(url, e.getMessage());
        } catch (IllegalStateException e) {
            throw new UrlFetchException(url, e.getMessage());
        } catch (BaseFetchException e) {
            throw e;
        } catch (Exception e) {
            // Map anything else to a generic IOFetchException
            // TODO KKr - create generic fetch exception
            throw new IOFetchException(url, new IOException(e));
        } finally {
            safeAbort(needAbort, request);
        }

        // Figure out how much data we want to try to fetch.
        int maxContentSize = getMaxContentSize(mimeType);
        int targetLength = maxContentSize;
        boolean truncated = false;
        String contentLengthStr = headerMap.get(HttpHeaders.CONTENT_LENGTH);
        if (contentLengthStr != null) {
            try {
                int contentLength = Integer.parseInt(contentLengthStr);
                if (contentLength > targetLength) {
                    truncated = true;
                } else {
                    targetLength = contentLength;
                }
            } catch (NumberFormatException e) {
                // Ignore (and log) invalid content length values.
                LOGGER.warn("Invalid content length in header: " + contentLengthStr);
            }
        }

        // Now finally read in response body, up to targetLength bytes.
        // Note that entity might be null, for zero length responses.
        byte[] content = new byte[0];
        long readRate = 0;
        HttpEntity entity = response.getEntity();
        needAbort = true;

        if (entity != null) {
            InputStream in = null;

            try {
                in = entity.getContent();
                byte[] buffer = new byte[BUFFER_SIZE];
                int bytesRead = 0;
                int totalRead = 0;
                ByteArrayOutputStream out = new ByteArrayOutputStream(DEFAULT_BYTEARRAY_SIZE);

                int readRequests = 0;
                int minResponseRate = getMinResponseRate();
                // TODO KKr - we need to monitor the rate while reading a
                // single block. Look at HttpClient
                // metrics support for how to do this. Once we fix this, fix
                // the test to read a smaller (< 20K)
                // chuck of data.
                while ((totalRead < targetLength) && ((bytesRead = in.read(buffer, 0, Math.min(buffer.length, targetLength - totalRead))) != -1)) {
                    readRequests += 1;
                    totalRead += bytesRead;
                    out.write(buffer, 0, bytesRead);

                    // Assume read time is at least one millisecond, to avoid
                    // DBZ exception.
                    long totalReadTime = Math.max(1, System.currentTimeMillis() - readStartTime);
                    readRate = (totalRead * 1000L) / totalReadTime;

                    // Don't bail on the first read cycle, as we can get a
                    // hiccup starting out.
                    // Also don't bail if we've read everything we need.
                    if ((readRequests > 1) && (totalRead < targetLength) && (readRate < minResponseRate)) {
                        throw new AbortedFetchException(url, "Slow response rate of " + readRate + " bytes/sec", AbortedFetchReason.SLOW_RESPONSE_RATE);
                    }

                    // Check to see if we got interrupted, but don't clear the
                    // interrupted flag.
                    if (Thread.currentThread().isInterrupted()) {
                        throw new AbortedFetchException(url, AbortedFetchReason.INTERRUPTED);
                    }
                }

                content = out.toByteArray();
                needAbort = truncated || (in.available() > 0);
            } catch (IOException e) {
                // We don't need to abort if there's an IOException
                throw new IOFetchException(url, e);
            } finally {
                safeAbort(needAbort, request);
                safeClose(in);
            }
        }

        // Toss truncated image content.
        if ((truncated) && (!isTextMimeType(mimeType))) {
            throw new AbortedFetchException(url, "Truncated image", AbortedFetchReason.CONTENT_SIZE);
        }

        // Now see if we need to uncompress the content.
        String contentEncoding = headerMap.get(HttpHeaders.CONTENT_ENCODING);
        if (contentEncoding != null) {
            if (LOGGER.isTraceEnabled()) {
                fetchTrace.append("; Content-Encoding: " + contentEncoding);
            }

            // TODO KKr We might want to just decompress a truncated gzip
            // containing text (since we have a max content size to save us
            // from any gzip corruption). We might want to break the following
            // out into a separate method, by the way (if not refactor this
            // entire monolithic method).
            //
            try {
                if ("gzip".equals(contentEncoding) || "x-gzip".equals(contentEncoding)) {
                    if (truncated) {
                        throw new AbortedFetchException(url, "Truncated compressed data", AbortedFetchReason.CONTENT_SIZE);
                    } else {
                        ExpandedResult expandedResult = EncodingUtils.processGzipEncoded(content, maxContentSize);
                        truncated = expandedResult.isTruncated();
                        if ((truncated) && (!isTextMimeType(mimeType))) {
                            throw new AbortedFetchException(url, "Truncated decompressed image", AbortedFetchReason.CONTENT_SIZE);
                        } else {
                            content = expandedResult.getExpanded();
                            if (LOGGER.isTraceEnabled()) {
                                fetchTrace.append("; unzipped to " + content.length + " bytes");
                            }
                        }
                        // } else if ("deflate".equals(contentEncoding)) {
                        // content =
                        // EncodingUtils.processDeflateEncoded(content);
                        // if (LOGGER.isTraceEnabled()) {
                        // fetchTrace.append("; inflated to " + content.length +
                        // " bytes");
                        // }
                    }
                }
            } catch (IOException e) {
                throw new IOFetchException(url, e);
            }
        }

        // Finally dump out the trace msg we've been building.
        if (LOGGER.isTraceEnabled()) {
            LOGGER.trace(fetchTrace.toString());
        }

        // TODO KKr - Save truncated flag in FetchedResult/FetchedDatum.
        return new FetchedResult(url, redirectedUrl, System.currentTimeMillis(), headerMap, content, contentType, (int) readRate, payload, newBaseUrl, numRedirects, hostAddress, statusCode,
                        reasonPhrase);
    }

    private boolean isTextMimeType(String mimeType) {
        for (String textContentType : TEXT_MIME_TYPES) {
            if (textContentType.equals(mimeType)) {
                return true;
            }
        }
        return false;
    }

    private String extractRedirectedUrl(String url, HttpContext localContext) {
        // This was triggered by HttpClient with the redirect count was
        // exceeded.
        HttpHost host = (HttpHost) localContext.getAttribute(HttpCoreContext.HTTP_TARGET_HOST);
        HttpUriRequest finalRequest = (HttpUriRequest) localContext.getAttribute(HttpCoreContext.HTTP_REQUEST);

        try {
            URL hostUrl = new URI(host.toURI()).toURL();
            return new URL(hostUrl, finalRequest.getURI().toString()).toExternalForm();
        } catch (MalformedURLException e) {
            LOGGER.warn("Invalid host/uri specified in final fetch: " + host + finalRequest.getURI());
            return url;
        } catch (URISyntaxException e) {
            LOGGER.warn("Invalid host/uri specified in final fetch: " + host + finalRequest.getURI());
            return url;
        }
    }

    private static void safeClose(Closeable o) {
        if (o != null) {
            try {
                o.close();
            } catch (Exception e) {
                // Ignore any errors
            }
        }
    }

    private static void safeAbort(boolean needAbort, HttpRequestBase request) {
        if (needAbort && (request != null)) {
            try {
                request.abort();
            } catch (Throwable t) {
                // Ignore any errors
            }
        }
    }

    private void init() {
        if (_httpClient == null) {
            synchronized (SimpleHttpFetcher.class) {
                if (_httpClient != null)
                    return;

                final HttpClientBuilder httpClientBuilder = HttpClientBuilder.create();
                final RequestConfig.Builder requestConfigBuilder = RequestConfig.custom();

                // Set the socket and connection timeout to be something
                // reasonable.
                requestConfigBuilder.setSocketTimeout(_socketTimeout);
                requestConfigBuilder.setConnectTimeout(_connectionTimeout);
                requestConfigBuilder.setConnectionRequestTimeout(_connectionRequestTimeout);
                
                if(proxy != null){
                    LOGGER.info("Configuring fetcher to use proxy: "+proxy.toURI());
                    httpClientBuilder.setProxy(proxy);
                }


                /*
                 * CoreConnectionPNames.TCP_NODELAY='http.tcp.nodelay':
                 * determines whether Nagle's algorithm is to be used. Nagle's
                 * algorithm tries to conserve bandwidth by minimizing the
                 * number of segments that are sent. When applications wish to
                 * decrease network latency and increase performance, they can
                 * disable Nagle's algorithm (that is enable TCP_NODELAY. Data
                 * will be sent earlier, at the cost of an increase in bandwidth
                 * consumption. This parameter expects a value of type
                 * java.lang.Boolean. If this parameter is not set, TCP_NODELAY
                 * will be enabled (no delay).
                 */
//                FIXME Could not find this parameter in http-client version 4.5
//                HttpConnectionParams.setTcpNoDelay(params, true);
//                HttpProtocolParams.setVersion(params, _httpVersion);
                
                httpClientBuilder.setUserAgent(_userAgentString);
                
//                HttpProtocolParams.setContentCharset(params, "UTF-8");
//                HttpProtocolParams.setHttpElementCharset(params, "UTF-8");

                /*
                 * CoreProtocolPNames.USE_EXPECT_CONTINUE=
                 * 'http.protocol.expect-continue': activates the Expect:
                 * 100-Continue handshake for the entity enclosing methods. The
                 * purpose of the Expect: 100-Continue handshake is to allow the
                 * client that is sending a request message with a request body
                 * to determine if the origin server is willing to accept the
                 * request (based on the request headers) before the client
                 * sends the request body. The use of the Expect: 100-continue
                 * handshake can result in a noticeable performance improvement
                 * for entity enclosing requests (such as POST and PUT) that
                 * require the target server's authentication. The Expect:
                 * 100-continue handshake should be used with caution, as it may
                 * cause problems with HTTP servers and proxies that do not
                 * support HTTP/1.1 protocol. This parameter expects a value of
                 * type java.lang.Boolean. If this parameter is not set,
                 * HttpClient will not attempt to use the handshake.
                 */
                requestConfigBuilder.setExpectContinueEnabled(true);

                /*
                 * CoreProtocolPNames.WAIT_FOR_CONTINUE=
                 * 'http.protocol.wait-for-continue': defines the maximum period
                 * of time in milliseconds the client should spend waiting for a
                 * 100-continue response. This parameter expects a value of type
                 * java.lang.Integer. If this parameter is not set HttpClient
                 * will wait 3 seconds for a confirmation before resuming the
                 * transmission of the request body.
                 */
//                FIXME Could not find this parameter in http-client version 4.5
//                params.setIntParameter(CoreProtocolPNames.WAIT_FOR_CONTINUE, 5000);

//                FIXME Could not find this parameter in http-client version 4.5
//                CookieSpecParamBean cookieParams = new CookieSpecParamBean(params);
//                cookieParams.setSingleHeader(false);
                
                // Create and initialize connection socket factory registry
                RegistryBuilder<ConnectionSocketFactory> registry = RegistryBuilder.<ConnectionSocketFactory>create();
                registry.register("http", PlainConnectionSocketFactory.getSocketFactory());
                SSLConnectionSocketFactory sf = createSSLConnectionSocketFactory();
                if (sf != null) {
                    registry.register("https", sf);
                } else {
                    LOGGER.warn("No valid SSLContext found for https");
                }

                _connectionManager = new PoolingHttpClientConnectionManager(registry.build());
                _connectionManager.setMaxTotal(_maxThreads);
                _connectionManager.setDefaultMaxPerRoute(getMaxConnectionsPerHost());
                
                /*
                 * CoreConnectionPNames.STALE_CONNECTION_CHECK=
                 * 'http.connection.stalecheck': determines whether stale
                 * connection check is to be used. Disabling stale connection
                 * check may result in a noticeable performance improvement (the
                 * check can cause up to 30 millisecond overhead per request) at
                 * the risk of getting an I/O error when executing a request
                 * over a connection that has been closed at the server side.
                 * This parameter expects a value of type java.lang.Boolean. For
                 * performance critical operations the check should be disabled.
                 * If this parameter is not set, the stale connection check will
                 * be performed before each request execution.
                 * 
                 * We don't need I/O exceptions in case if Server doesn't
                 * support Kee-Alive option; our client by default always tries
                 * keep-alive.
                 */
                // Even with stale checking enabled, a connection can "go stale"
                // between the check and the next request. So we still need to
                // handle the case of a closed socket (from the server side),
                // and disabling this check improves performance.
                // Stale connections will be checked in a separate monitor thread
                _connectionManager.setValidateAfterInactivity(-1);
                
                httpClientBuilder.setConnectionManager(_connectionManager);
                httpClientBuilder.setRetryHandler(new MyRequestRetryHandler(_maxRetryCount));
                httpClientBuilder.setRedirectStrategy(new MyRedirectStrategy(getRedirectMode()));
                httpClientBuilder.setRequestExecutor(new MyHttpRequestExecutor());

                // FUTURE KKr - support authentication
//                FIXME Could not find this parameter in http-client version 4.5
//                HttpClientParams.setAuthenticating(params, false);
                
                requestConfigBuilder.setCookieSpec(CookieSpecs.DEFAULT);

                if (getMaxRedirects() == 0) {
                    requestConfigBuilder.setRedirectsEnabled(false);
                } else {
                    requestConfigBuilder.setRedirectsEnabled(true);
                    requestConfigBuilder.setMaxRedirects(getMaxRedirects());
                }

                // Set up default headers. This helps us get back from servers
                // what we want.
                HashSet<Header> defaultHeaders = new HashSet<Header>();
                defaultHeaders.add(new BasicHeader(HttpHeaders.ACCEPT_LANGUAGE, getAcceptLanguage()));
                defaultHeaders.add(new BasicHeader(HttpHeaders.ACCEPT_CHARSET, DEFAULT_ACCEPT_CHARSET));
                defaultHeaders.add(new BasicHeader(HttpHeaders.ACCEPT_ENCODING, DEFAULT_ACCEPT_ENCODING));
                defaultHeaders.add(new BasicHeader(HttpHeaders.ACCEPT, DEFAULT_ACCEPT));

                httpClientBuilder.setDefaultHeaders(defaultHeaders);
                
                httpClientBuilder.setKeepAliveStrategy(new MyConnectionKeepAliveStrategy());
                
                monitor = new IdleConnectionMonitorThread(_connectionManager);
                monitor.start();
                
                httpClientBuilder.setDefaultRequestConfig(requestConfigBuilder.build());
                _httpClient = httpClientBuilder.build();
            }
        }

    }

    private SSLConnectionSocketFactory createSSLConnectionSocketFactory() {
        SSLConnectionSocketFactory sf = null;
        for (String contextName : SSL_CONTEXT_NAMES) {
            try {
                SSLContext sslContext = SSLContext.getInstance(contextName);
                sslContext.init(null, new TrustManager[] { new DummyX509TrustManager(null) }, null);
                HostnameVerifier hostnameVerifier = NoopHostnameVerifier.INSTANCE;
                sf = new SSLConnectionSocketFactory(sslContext, hostnameVerifier);
                break;
            } catch (NoSuchAlgorithmException e) {
                LOGGER.debug("SSLContext algorithm not available: " + contextName);
            } catch (Exception e) {
                LOGGER.debug("SSLContext can't be initialized: " + contextName, e);
            }
        }
        return sf;
    }
    
    public void setProxy(String scheme, String host, int port) {
        this.proxy = new HttpHost(host, port, scheme);
    }

    @Override
    public void abort() {
        // TODO Actually try to abort
    }

    @Override
    protected void finalize() {
        monitor.interrupt();
        _connectionManager.shutdown();
        IOUtils.closeQuietly(_httpClient);
        _httpClient = null;
    }

    public void setUserAgentString(String userAgentString) {
        this._userAgentString = userAgentString;
    }

    /**
     * Update cookie store with a map of cookies.
     * key : domain name
     * value : List of cookies associated with that domain name
     * @param cookies
     * @throws NullPointerException if the cookies argument is null
     */
	public void updateCookieStore(Map<String, List<Cookie>> cookies) {
		if(cookies == null) {
			throw new NullPointerException("Cookies argument can not be null");
		}
		if(globalCookieStore == null) {
			globalCookieStore = new ConcurrentCookieJar();
		}
		for(List<Cookie> listOfCookies : cookies.values()) {
			for(Cookie cookie: listOfCookies) {
				globalCookieStore.addCookie(cookie);
			}
		}
	}

	/**
	 * Updates the current cookie store with cookie
	 * @param cookie
	 * @throws NullPointerException if the cookie argument is null
	 */
	public void updateCookieStore(Cookie cookie) {
		if(cookie == null) {
			throw new NullPointerException("Argument cookie is null.");
		}
		if(globalCookieStore == null) {
			globalCookieStore = new ConcurrentCookieJar();
		}
		globalCookieStore.addCookie(cookie);
	}
	
	/**
	 * Returns cookie store for testing.
	 * @return
	 */
	public CookieStore getCookieStore() {
		return globalCookieStore;
	}
}
