package achecrawler.crawler.async.fetcher;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.Proxy;
import java.net.Socket;
import java.net.URL;
import java.security.GeneralSecurityException;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;

import org.apache.http.HttpHeaders;
import org.apache.http.HttpStatus;
import org.apache.tika.metadata.Metadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import achecrawler.crawler.cookies.OkHttpCookieJar;
import achecrawler.crawler.crawlercommons.fetcher.AbortedFetchException;
import achecrawler.crawler.crawlercommons.fetcher.AbortedFetchReason;
import achecrawler.crawler.crawlercommons.fetcher.BadProtocolFetchException;
import achecrawler.crawler.crawlercommons.fetcher.BaseFetchException;
import achecrawler.crawler.crawlercommons.fetcher.EncodingUtils;
import achecrawler.crawler.crawlercommons.fetcher.FetchedResult;
import achecrawler.crawler.crawlercommons.fetcher.IOFetchException;
import achecrawler.crawler.crawlercommons.fetcher.Payload;
import achecrawler.crawler.crawlercommons.fetcher.UrlFetchException;
import achecrawler.crawler.crawlercommons.fetcher.http.BaseHttpFetcher;
import achecrawler.crawler.crawlercommons.fetcher.http.UserAgent;
import okhttp3.CipherSuite;
import okhttp3.ConnectionSpec;
import okhttp3.Cookie;
import okhttp3.Credentials;
import okhttp3.OkHttpClient;
import okhttp3.OkHttpClient.Builder;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okhttp3.Route;


@SuppressWarnings("serial")
public class OkHttpFetcher extends BaseHttpFetcher {

    private static Logger LOGGER = LoggerFactory.getLogger(OkHttpFetcher.class);

    private static final String TEXT_MIME_TYPES[] = {
        "text/html",
        "application/x-asp",
        "application/xhtml+xml",
        "application/vnd.wap.xhtml+xml"
    };

    private static final int DEFAULT_MAX_THREADS = 1;

    transient private OkHttpClient _httpClient;
    private OkHttpCookieJar cookieJar ;
    private int connectTimeoutTime;
    private int readTimeoutTime;
    private int proxyPort;
    private String proxyHost;
    private String username;
    private String password;

    public OkHttpFetcher(UserAgent userAgent) {
        this(DEFAULT_MAX_THREADS, userAgent, null, 30000, 30000, null, 8080, null, null);
    }

    public OkHttpFetcher(int maxThreads, UserAgent userAgent, OkHttpCookieJar cookieJar,
                         int connectTimeoutTime, int readTimeoutTime, String host, int port,
                         String username, String password) {
        super(maxThreads, userAgent);
        this._httpClient = null;
        this.cookieJar = cookieJar;
        this.connectTimeoutTime = connectTimeoutTime;
        this.readTimeoutTime = readTimeoutTime;
        this.proxyHost = host;
        this.proxyPort = port;
        this.username = username;
        this.password = password;
    }

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

        return request(url, payload);
    }

    private FetchedResult request(String url, Payload payload) throws BaseFetchException {
        init();
        Request request = new Request.Builder()
                .url(url)
                .build();

        try {
            return doRequest(request, url, payload);
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
        return fetch(url, new Payload());
    }

    public FetchedResult fetch(String url, Payload payload) throws BaseFetchException {
        init();

        Request request = new Request.Builder()
                .url(url)
                .build();
        try {
            return doRequest(request, url, payload);
        } catch (BaseFetchException e) {
            if (LOGGER.isTraceEnabled()) {
                LOGGER.trace("Exception fetching {} {}", url, e.getMessage());
            }
            throw e;
        }
    }

    private int getRedirectCount(Response response) {
        Integer count = 0;
        Response previous = response.priorResponse();
        while (previous != null) {
            count++;
            previous = previous.priorResponse();
        }
        return count;
    }

    private FetchedResult doRequest(Request request, String url, Payload payload) throws BaseFetchException {
        Response response = null;
        Metadata headerMap = new Metadata();
        String redirectedUrl = null;
        String newBaseUrl = null;
        int numRedirects = 0;
        String contentType = "";
        String mimeType = "";
        String hostAddress = null;
        int statusCode = HttpStatus.SC_INTERNAL_SERVER_ERROR;
        String reasonPhrase = null;

        StringBuilder fetchTrace = null;
        if (LOGGER.isTraceEnabled()) {
            fetchTrace = new StringBuilder("Fetched url: " + url);
        }

        try {
            response = _httpClient.newCall(request).execute();


            Map<String,List<String>> headers = response.headers().toMultimap();
            for(Map.Entry<String,List<String>> header : headers.entrySet()){
                headerMap.add(header.getKey(),header.getValue().toString());
            }

            statusCode = response.code();
            reasonPhrase = response.message();

            if (LOGGER.isTraceEnabled()) {
                fetchTrace.append("; status code: " + statusCode);
                if (headerMap.get(HttpHeaders.CONTENT_LENGTH) != null) {
                    fetchTrace.append("; Content-Length: " + headerMap.get(HttpHeaders.CONTENT_LENGTH));
                }

                if (headerMap.get(HttpHeaders.LOCATION) != null) {
                    fetchTrace.append("; Location: " + headerMap.get(HttpHeaders.LOCATION));
                }
            }

            Integer redirects = getRedirectCount(response);
            if (redirects != null) {
                numRedirects = redirects.intValue();
            }

            redirectedUrl = response.request().url().toString();

            if (response.isRedirect() || numRedirects>0){
                newBaseUrl = response.request().url().toString();
            }

            hostAddress = response.request().url().host();
            if (hostAddress == null) {
                throw new UrlFetchException(url, "Host address not saved in context");
            }

            String cth = response.header(HttpHeaders.CONTENT_TYPE);
            if (cth != null) {
                contentType = cth;
            }

            mimeType = getMimeTypeFromContentType(contentType);
            Set<String> mimeTypes = getValidMimeTypes();
            if ((mimeTypes != null) && (mimeTypes.size() > 0)) {
                if (!mimeTypes.contains(mimeType)) {
                    response.close();
                    throw new AbortedFetchException(url, "Invalid mime-type: " + mimeType, AbortedFetchReason.INVALID_MIMETYPE);
                }
            }

        } catch (IOException e) {
            throw new IOFetchException(url, e);
        } catch (IllegalStateException e) {
            throw new UrlFetchException(url, e.getMessage());
        } catch (BaseFetchException e) {
            throw e;
        } catch (Exception e) {
            throw new IOFetchException(url, new IOException(e));
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
        try{
            ResponseBody body = response.peekBody(targetLength);
            content = body.bytes();
            long totalReadTime = Math.max(1,response.receivedResponseAtMillis() - response.sentRequestAtMillis());
            readRate = (content.length * 1000L) / totalReadTime;
        } catch (IOException e) {
            throw new IOFetchException(url, e);
        } finally {
            response.close();
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

            try {
                if ("gzip".equals(contentEncoding) || "x-gzip".equals(contentEncoding)) {
                    if (truncated) {
                        throw new AbortedFetchException(url, "Truncated compressed data", AbortedFetchReason.CONTENT_SIZE);
                    } else {
                        EncodingUtils.ExpandedResult expandedResult = EncodingUtils.processGzipEncoded(content, maxContentSize);
                        truncated = expandedResult.isTruncated();
                        if ((truncated) && (!isTextMimeType(mimeType))) {
                            throw new AbortedFetchException(url, "Truncated decompressed image", AbortedFetchReason.CONTENT_SIZE);
                        } else {
                            content = expandedResult.getExpanded();
                            if (LOGGER.isTraceEnabled()) {
                                fetchTrace.append("; unzipped to " + content.length + " bytes");
                            }
                        }
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


    private void init() {
        if (_httpClient == null) {
            synchronized (OkHttpFetcher.class) {
                if (_httpClient != null) {
                    return;
                }
//                _httpClient = new OkHttpClient(); // default
//                _httpClient = getCustomOkHttpClient(); // custom ciphers
                _httpClient = getUnsafeOkHttpClient(); // all trusting trust manager, all default ciphers
            }
        }

    }

    private OkHttpClient getUnsafeOkHttpClient() {
        try {
            // Create a trust manager that does not validate certificate chains
            final TrustManager[] trustAllCerts;
            trustAllCerts = new TrustManager[] {
                    new X509TrustManager() {
                        @Override
                        public void checkClientTrusted(java.security.cert.X509Certificate[] chain, String authType) {
                        }

                        @Override
                        public void checkServerTrusted(java.security.cert.X509Certificate[] chain, String authType) {
                        }

                        @Override
                        public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                            return new java.security.cert.X509Certificate[0];
                        }
                    }
            };

            // Install the all-trusting trust manager
            final SSLContext sslContext = SSLContext.getInstance("SSL");
            sslContext.init(null, trustAllCerts, new java.security.SecureRandom());

            // Create an SSL socket factory with our all-trusting manager
            final SSLSocketFactory sslSocketFactory = sslContext.getSocketFactory();

            HostnameVerifier noopHostnameVerifier = new HostnameVerifier() {
                @Override
                public boolean verify(String s, SSLSession sslSession) {
                    return true;
                }
            };

            OkHttpClient.Builder clientBuilder = new OkHttpClient.Builder()
                    .sslSocketFactory(sslSocketFactory, (X509TrustManager) trustAllCerts[0])
                    .hostnameVerifier(noopHostnameVerifier)
                    .connectTimeout(connectTimeoutTime, TimeUnit.MILLISECONDS)
                    .readTimeout(readTimeoutTime, TimeUnit.MILLISECONDS);

            if (proxyHost != null) {
                okhttp3.Authenticator proxyAuthenticator = new okhttp3.Authenticator() {
                    @Override
                    public Request authenticate(Route route, Response response) throws IOException {
                        String credential = Credentials.basic(username, password);
                        return response.request().newBuilder()
                                .header("Proxy-Authorization", credential)
                                .build();
                    }
                };

                Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(proxyHost, proxyPort));
                clientBuilder
                        .proxy(proxy)
                        .proxyAuthenticator(proxyAuthenticator);
            }
            if (cookieJar != null) {
                clientBuilder.cookieJar(cookieJar);
            }

            return clientBuilder.build();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private OkHttpClient getCustomOkHttpClient() {
        
        final ConnectionSpec specModernTLS = new ConnectionSpec.Builder(ConnectionSpec.MODERN_TLS)
                .cipherSuites(new CustomCipherSuites().getCustomCipherSuites().toArray(new CipherSuite[0]))
                .build();

        final ConnectionSpec specClearText = new ConnectionSpec.Builder(ConnectionSpec.CLEARTEXT)
                .build();

        final ConnectionSpec specCompatibleTLS = new ConnectionSpec.Builder(ConnectionSpec.COMPATIBLE_TLS)
                .build();

        final List<ConnectionSpec> specs = new ArrayList<>();
        specs.add(specModernTLS);
        specs.add(specClearText);
        specs.add(specCompatibleTLS);

        X509TrustManager trustManager;
        SSLSocketFactory sslSocketFactory;

        try {
            trustManager = defaultTrustManager();
            sslSocketFactory = defaultSslSocketFactory(trustManager);

            SSLSocketFactory customSslSocketFactory =
                    new DelegatingSSLSocketFactory(sslSocketFactory) {
                        @Override
                        protected SSLSocket configureSocket(SSLSocket socket) throws IOException {
                            socket.setEnabledCipherSuites(javaNames(specModernTLS.cipherSuites()));
                            return socket;
                        }
                    };

            Builder clientBuilder = new OkHttpClient.Builder().connectionSpecs(specs)
                    .sslSocketFactory(customSslSocketFactory, trustManager)
                    .connectTimeout(connectTimeoutTime, TimeUnit.MILLISECONDS)
                    .readTimeout(readTimeoutTime, TimeUnit.MILLISECONDS);

            if (cookieJar != null) {
                clientBuilder.cookieJar(cookieJar);
            }

            return clientBuilder.build();

        } catch (GeneralSecurityException gse) {
        }

        return new OkHttpClient();
    }

    /**
     * Returns the VM's default SSL socket factory, using {@code trustManager} for trusted root
     * certificates.
     */
    private static SSLSocketFactory defaultSslSocketFactory(X509TrustManager trustManager)
            throws NoSuchAlgorithmException, KeyManagementException {
        SSLContext sslContext = SSLContext.getInstance("TLS");
        sslContext.init(null, new TrustManager[] { trustManager }, null);

        return sslContext.getSocketFactory();
    }

    /** Returns a trust manager that trusts the VM's default certificate authorities. */
    private static X509TrustManager defaultTrustManager() throws GeneralSecurityException {
        TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(
                TrustManagerFactory.getDefaultAlgorithm());
        trustManagerFactory.init((KeyStore) null);
        TrustManager[] trustManagers = trustManagerFactory.getTrustManagers();
        if (trustManagers.length != 1 || !(trustManagers[0] instanceof X509TrustManager)) {
            throw new IllegalStateException("Unexpected default trust managers:"
                    + Arrays.toString(trustManagers));
        }
        return (X509TrustManager) trustManagers[0];

    }

    private static String[] javaNames(List<CipherSuite> cipherSuites) {
        String[] result = new String[cipherSuites.size()];
        for (int i = 0; i < result.length; i++) {
            result[i] = cipherSuites.get(i).javaName();
        }
        return result;
    }

    /**
     * An SSL socket factory that forwards all calls to a delegate. Override {@link #configureSocket}
     * to customize a created socket before it is returned.
     */
    static class DelegatingSSLSocketFactory extends SSLSocketFactory {
        protected final SSLSocketFactory delegate;

        DelegatingSSLSocketFactory(SSLSocketFactory delegate) {
            this.delegate = delegate;
        }

        @Override public String[] getDefaultCipherSuites() {
            return delegate.getDefaultCipherSuites();
        }

        @Override public String[] getSupportedCipherSuites() {
            return delegate.getSupportedCipherSuites();
        }

        @Override public Socket createSocket(
                Socket socket, String host, int port, boolean autoClose) throws IOException {
            return configureSocket((SSLSocket) delegate.createSocket(socket, host, port, autoClose));
        }

        @Override public Socket createSocket(String host, int port) throws IOException {
            return configureSocket((SSLSocket) delegate.createSocket(host, port));
        }

        @Override public Socket createSocket(
                String host, int port, InetAddress localHost, int localPort) throws IOException {
            return configureSocket((SSLSocket) delegate.createSocket(host, port, localHost, localPort));
        }

        @Override public Socket createSocket(InetAddress host, int port) throws IOException {
            return configureSocket((SSLSocket) delegate.createSocket(host, port));
        }

        @Override public Socket createSocket(
                InetAddress address, int port, InetAddress localAddress, int localPort) throws IOException {
            return configureSocket((SSLSocket) delegate.createSocket(
                    address, port, localAddress, localPort));
        }

        protected SSLSocket configureSocket(SSLSocket socket) throws IOException {
            return socket;
        }
    }

    @Override
    public void abort() {

    }

    private boolean isTextMimeType(String mimeType) {
        for (String textContentType : TEXT_MIME_TYPES) {
            if (textContentType.equals(mimeType)) {
                return true;
            }
        }
        return false;
    }

    public void updateCookies(Map<String, List<Cookie>> map) {
        this.cookieJar.update(map);
    }


//    /** An interceptor that allows runtime changes to the URL hostname. */
//    /** might be useful in the future to intercept any responses */
//    public final class HostSelectionInterceptor implements Interceptor {
//        private volatile String host;
//
//        public void setHost(String host) {
//            this.host = host;
//        }
//
//        @Override
//        public okhttp3.Response intercept(Interceptor.Chain chain) throws IOException {
//            Request request = chain.request();
//            Response response = chain.proceed(request);
//
//            LOGGER.info(chain.connection().toString());
//                String host = this.host;
//                if (host != null) {
//                    HttpUrl newUrl = request.url().newBuilder()
//                            .host(host)
//                            .build();
//                    request = request.newBuilder()
//                            .url(newUrl)
//                            .build();
//            }
//
//            return response;
//        }
//    }


}
