package focusedCrawler.crawler.async;

import java.io.Closeable;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.apache.http.client.methods.HttpGet;

import com.google.common.util.concurrent.ThreadFactoryBuilder;

import crawlercommons.fetcher.BaseFetchException;
import crawlercommons.fetcher.FetchedResult;
import crawlercommons.fetcher.Payload;
import crawlercommons.fetcher.http.SimpleHttpFetcher;
import crawlercommons.fetcher.http.UserAgent;
import focusedCrawler.util.LinkRelevance;

public class HttpDownloader implements Closeable {
    
    public static final String PAYLOAD_KEY = "link-relevance";

    private static final int CPU_CORES = Runtime.getRuntime().availableProcessors();
    private static final int DEFAULT_MAX_RETRY_COUNT = 3;
    private static final int DEFAULT_MAX_THREADS = 300;
    private static final int DEFAULT_DOWNLOAD_QUEUE_MAX_SIZE = DEFAULT_MAX_THREADS*2;
    private static final String[] DEFAULT_TEXT_MIME_TYPES = {
        "text/html",
        "application/x-asp",
        "application/xhtml+xml",
        "application/vnd.wap.xhtml+xml"
    };

    private final SimpleHttpFetcher fetcher;
    private final ExecutorService downloadThreadPool;
    private final ExecutorService distpatchThreadPool;
    private final LinkedBlockingQueue<Runnable> downloadQueue;
    
    public HttpDownloader(UserAgent userAgent) {
        
        ThreadFactory downloadThreadFactory = new ThreadFactoryBuilder().setNameFormat("downloader-%d").build();
        ThreadFactory dispatcherThreadFactory = new ThreadFactoryBuilder().setNameFormat("dispatcher-%d").build();
        
        this.downloadQueue = new LinkedBlockingQueue<Runnable>();
        this.downloadThreadPool  = new ThreadPoolExecutor(DEFAULT_MAX_THREADS, DEFAULT_MAX_THREADS,
                0L, TimeUnit.MILLISECONDS, this.downloadQueue, downloadThreadFactory);
        
//        this.downloadThreadPool  = Executors.newFixedThreadPool(DEFAULT_MAX_THREADS, downloadThreadFactory);
        this.distpatchThreadPool = Executors.newFixedThreadPool(CPU_CORES, dispatcherThreadFactory);
        
        // Adding some extra connections for URL that have redirects
        // and then create more connections   
        int connectionPoolSize = (int) (DEFAULT_MAX_THREADS * 1.5);
        
        this.fetcher = new SimpleHttpFetcher(connectionPoolSize, userAgent);
        this.fetcher.setSocketTimeout(30*1000);
        this.fetcher.setConnectionTimeout(5*60*1000);
        this.fetcher.setMaxRetryCount(DEFAULT_MAX_RETRY_COUNT);
        for (String mimeTypes : DEFAULT_TEXT_MIME_TYPES) {
            this.fetcher.addValidMimeType(mimeTypes);
        }
    }
    
    public Future<FetchedResult> dipatchDownload(String url) {
        try {
            URL urlObj = new URL(url);
            return this.dipatchDownload(urlObj, null);
        } catch (MalformedURLException e) {
            throw new IllegalArgumentException("Invalid URL provided: "+url, e);
        }
    }
    
    public Future<FetchedResult> dipatchDownload(URL url, Callback callback) {
        LinkRelevance link = new LinkRelevance(url, 0d);
        return dipatchDownload(link, callback);
    }
    
    public Future<FetchedResult> dipatchDownload(LinkRelevance link, Callback callback) {
        System.out.println("Current queue size: "+downloadQueue.size());
        try {
            while(downloadQueue.size() > DEFAULT_DOWNLOAD_QUEUE_MAX_SIZE) {
                Thread.sleep(100);
            }
        } catch (InterruptedException e) {
            // ok, just finish execution
        }
        return downloadThreadPool.submit(new RequestTask(link, callback));
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
    
    public interface Callback {
        
        public void completed(FetchedResult result);
        
        public void failed(String url, Exception e);
        
    }
    
    private class RequestTask implements Callable<FetchedResult> {
        
        private final Callback callback;
        private LinkRelevance link;
        private long startTime;
        private long finishTime;
        
        public RequestTask(LinkRelevance url, Callback callback) {
            this.link = url;
            this.callback = callback;
        }
        
        @Override
        public FetchedResult call() {
            this.startTime = System.currentTimeMillis();
            try {
                final Payload payload = new Payload();
                payload.put(PAYLOAD_KEY, link);
                
                FetchedResult result = fetcher.fetch(new HttpGet(), link.getURL().toString(), payload);
                if (callback != null) {
                    distpatchThreadPool.submit(new SuccessHandler(result, callback));
                }
                
                return result;
            } catch (BaseFetchException e) {
                if (callback != null) {
                    distpatchThreadPool.submit(new FailureHandler(e, callback));
                }
                return null;
            } finally {
                this.finishTime = System.currentTimeMillis();
//                System.out.println("Time to download: "+(finishTime-startTime)+"ms URL:"+url.getURL().toString());
            }
        }
        
    }
    
    private final class SuccessHandler implements Runnable {

        private FetchedResult response;
        private Callback callback;

        public SuccessHandler(FetchedResult response, Callback callback) {
            this.response = response;
            this.callback = callback;
        }

        @Override
        public void run() {
            callback.completed(response);
        }
        
    }
    
    private final class FailureHandler implements Runnable {

        private Callback callback;
        private BaseFetchException exception;

        public FailureHandler(BaseFetchException exception, Callback callback) {
            this.exception = exception;
            this.callback = callback;
        }

        @Override
        public void run() {
            callback.failed(exception.getUrl(), exception);
        }
        
    }

    public void await() {
        System.out.println("Waiting downloader finish.");
        try {
            downloadThreadPool.awaitTermination(Integer.MAX_VALUE, TimeUnit.MINUTES);
            distpatchThreadPool.awaitTermination(Integer.MAX_VALUE, TimeUnit.MINUTES);
        } catch (InterruptedException e) {
            throw new RuntimeException("Failed to await downloader threads.", e);
        }
    }

}
