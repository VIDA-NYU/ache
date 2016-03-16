package focusedCrawler.crawler.async;

import java.io.Closeable;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.http.client.methods.HttpGet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.util.concurrent.ThreadFactoryBuilder;

import crawlercommons.fetcher.BaseFetchException;
import crawlercommons.fetcher.FetchedResult;
import crawlercommons.fetcher.Payload;
import crawlercommons.fetcher.http.SimpleHttpFetcher;
import crawlercommons.fetcher.http.UserAgent;
import focusedCrawler.util.LinkRelevance;

public class HttpDownloader implements Closeable {
    
    private static Logger logger = LoggerFactory.getLogger(HttpDownloader.class);
    
    public static final String PAYLOAD_KEY = "link-relevance";
    private static final int CPU_CORES = Runtime.getRuntime().availableProcessors();
    
    static public class Config {

        @JsonProperty("crawler_manager.downloader.download_thread_pool_size")
        private int downloadThreadPoolSize = 100;

        @JsonProperty("crawler_manager.downloader.max_retry_count")
        private int maxRetryCount = 2;

        @JsonProperty("crawler_manager.downloader.valid_mime_types")
        private String[] validMimeTypes = {
            "text/html",
            "application/x-asp",
            "application/xhtml+xml",
            "application/vnd.wap.xhtml+xml"
        };

        @JsonProperty("crawler_manager.downloader.user_agent.name")
        private String userAgentName = "ACHE";

        @JsonProperty("crawler_manager.downloader.user_agent.url")
        private String userAgentUrl = "https://github.com/ViDA-NYU/ache";

        public Config() {
        }

        public Config(JsonNode config, ObjectMapper objectMapper) throws IOException {
            objectMapper.readerForUpdating(this).readValue(config);
        }

        public int getDownloadThreadPoolSize() {
            return this.downloadThreadPoolSize;
        }

        public int getMaxRetryCount() {
            return this.maxRetryCount;
        }

        public String getUserAgentName() {
            return this.userAgentName;
        }

        public String getUserAgentUrl() {
            return this.userAgentUrl;
        }

        public String[] getValidMimeTypes() {
            return this.validMimeTypes;
        }
	}

    private final SimpleHttpFetcher fetcher;
    private final ExecutorService downloadThreadPool;
    private final ExecutorService distpatchThreadPool;
    private final LinkedBlockingQueue<Runnable> downloadQueue;
    private final LinkedBlockingQueue<Runnable> dispatchQueue;
    private final AtomicInteger numberOfDownloads = new AtomicInteger(0);
	private final int downloadQueueMaxSize;
    
	public HttpDownloader() {
		this(new Config());
	}
	
    public HttpDownloader(Config config) {
    	
        ThreadFactory downloadThreadFactory = new ThreadFactoryBuilder().setNameFormat("downloader-%d").build();
        ThreadFactory dispatcherThreadFactory = new ThreadFactoryBuilder().setNameFormat("dispatcher-%d").build();
        
        this.downloadQueue = new LinkedBlockingQueue<Runnable>();
        this.dispatchQueue = new LinkedBlockingQueue<Runnable>();
        
        int threadPoolSize = config.getDownloadThreadPoolSize();
		this.downloadThreadPool  = new ThreadPoolExecutor(threadPoolSize , threadPoolSize,
                0L, TimeUnit.MILLISECONDS, this.downloadQueue, downloadThreadFactory);
        
        this.distpatchThreadPool  = new ThreadPoolExecutor(CPU_CORES, CPU_CORES,
                0L, TimeUnit.MILLISECONDS, this.dispatchQueue, dispatcherThreadFactory);
        
        this.downloadQueueMaxSize = threadPoolSize * 2;
        
        // Adding some extra connections for URLs that have redirects
        // and thus creates more connections   
        int connectionPoolSize = (int) (threadPoolSize * 2);
        UserAgent userAgent = new UserAgent(config.getUserAgentName(), "", config.getUserAgentUrl());
        
        this.fetcher = new SimpleHttpFetcher(connectionPoolSize, userAgent);
        this.fetcher.setSocketTimeout(30*1000);
        this.fetcher.setMaxConnectionsPerHost(1);
        this.fetcher.setConnectionTimeout(5*60*1000);
        this.fetcher.setMaxRetryCount(config.getMaxRetryCount());
        this.fetcher.setDefaultMaxContentSize(10*1024*1024);
        if(config.getValidMimeTypes() != null) {
            for (String mimeTypes : config.getValidMimeTypes()) {
                this.fetcher.addValidMimeType(mimeTypes);
            }
        }
    }
    
    public Future<FetchedResult> dipatchDownload(String url) {
        try {
            URL urlObj = new URL(url);
            Future<FetchedResult> future = dipatchDownload(urlObj, null);
            return future;
        } catch (MalformedURLException e) {
            throw new IllegalArgumentException("Invalid URL provided: "+url, e);
        }
    }
    
    public Future<FetchedResult> dipatchDownload(URL url, Callback callback) {
        LinkRelevance link = new LinkRelevance(url, 0d);
        return dipatchDownload(link, callback);
    }
    
    public Future<FetchedResult> dipatchDownload(LinkRelevance link, Callback callback) {
        try {
            while(downloadQueue.size() > downloadQueueMaxSize) {
                Thread.sleep(100);
            }
        } catch (InterruptedException e) {
            // ok, just finish execution
        }
        Future<FetchedResult> future = downloadThreadPool.submit(new RequestTask(link, callback));
        numberOfDownloads.incrementAndGet();
        return future;
    }
    
    @Override
    public void close() {
        fetcher.abort();
        downloadThreadPool.shutdownNow();
        distpatchThreadPool.shutdownNow();
        try {
            downloadThreadPool.awaitTermination(10, TimeUnit.SECONDS);
            distpatchThreadPool.awaitTermination(10, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            throw new RuntimeException("Failed to shutdown downloader threads.", e);
        }
    }
    
    public void await() {
        logger.info("Waiting downloader to finish...");
        try {        
            downloadThreadPool.shutdown();
            distpatchThreadPool.shutdown();
            downloadThreadPool.awaitTermination(1, TimeUnit.MINUTES);
            distpatchThreadPool.awaitTermination(1, TimeUnit.MINUTES);
        } catch (InterruptedException e) {
            throw new RuntimeException("Thread interrupted while waiting downloader threads finalize.", e);
        }
        logger.info("Done.");
    }
    
    public boolean hasPendingDownloads() {
        if(numberOfDownloads.get() > 0) {
            return true;
        } else {
            return false;
        }
    }
    
    public interface Callback {
        
        public void completed(FetchedResult result);
        
        public void failed(String url, Exception e);
        
    }
    
    private class RequestTask implements Callable<FetchedResult> {
        
        private final Callback callback;
        private LinkRelevance link;
//        private long startTime;
//        private long finishTime;
        
        public RequestTask(LinkRelevance url, Callback callback) {
            this.link = url;
            this.callback = callback;
        }
        
        @Override
        public FetchedResult call() {

            final Payload payload = new Payload(); // Payload is used as a temporary storage
            payload.put(PAYLOAD_KEY, link);
            
            try {
                FetchedResult result = fetcher.fetch(new HttpGet(), link.getURL().toString(), payload);
                distpatchThreadPool.submit(new FetchFinishedHandler(result, callback, null));
                return result;
            } catch (BaseFetchException e) {
                distpatchThreadPool.submit(new FetchFinishedHandler(null, callback, e));
                return null;
            }
        }
        
    }
    
    private final class FetchFinishedHandler implements Runnable {

        private FetchedResult response;
        private Callback callback;
        private BaseFetchException exception;

        public FetchFinishedHandler(FetchedResult response, Callback callback, BaseFetchException exception) {
            this.response = response;
            this.callback = callback;
            this.exception = exception;
        }

        @Override
        public void run() {
            if(callback != null) {
                if(exception != null) {
                    callback.failed(exception.getUrl(), exception);
                } else {
                    callback.completed(response);
                }
            }
            numberOfDownloads.decrementAndGet();
        }
        
    }

}
