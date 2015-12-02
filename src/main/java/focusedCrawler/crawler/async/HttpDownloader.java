package focusedCrawler.crawler.async;

import java.io.Closeable;
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

    private static final int DEFAULT_MAX_RETRY_COUNT = 3;
    private static final int DEFAULT_MAX_DOWNLOAD_THREADS = 50;
    private static final int DEFAULT_DOWNLOAD_QUEUE_MAX_SIZE = DEFAULT_MAX_DOWNLOAD_THREADS*2;
    private static final int CPU_CORES = Runtime.getRuntime().availableProcessors();

    private static final String[] DEFAULT_TEXT_MIME_TYPES = {
        "text/html",
        "application/x-asp",
        "application/xhtml+xml",
        "application/vnd.wap.xhtml+xml"
    };

    private final SimpleHttpFetcher fetcher;
    private final ExecutorService downloadThreadPool;
    private final ExecutorService distpatchThreadPool;
    private LinkedBlockingQueue<Runnable> downloadQueue;
    private LinkedBlockingQueue<Runnable> dispatchQueue;
    private final AtomicInteger numberOfDownloads = new AtomicInteger(0);
    
    public HttpDownloader(UserAgent userAgent) {
        
        ThreadFactory downloadThreadFactory = new ThreadFactoryBuilder().setNameFormat("downloader-%d").build();
        ThreadFactory dispatcherThreadFactory = new ThreadFactoryBuilder().setNameFormat("dispatcher-%d").build();
        
        this.downloadQueue = new LinkedBlockingQueue<Runnable>();
        this.dispatchQueue = new LinkedBlockingQueue<Runnable>();
        
        this.downloadThreadPool  = new ThreadPoolExecutor(DEFAULT_MAX_DOWNLOAD_THREADS, DEFAULT_MAX_DOWNLOAD_THREADS,
                0L, TimeUnit.MILLISECONDS, this.downloadQueue, downloadThreadFactory);
        
        this.distpatchThreadPool  = new ThreadPoolExecutor(CPU_CORES, CPU_CORES,
                0L, TimeUnit.MILLISECONDS, this.dispatchQueue, dispatcherThreadFactory);
        
        // Adding some extra connections for URLs that have redirects
        // and thus creates more connections   
        int connectionPoolSize = (int) (DEFAULT_MAX_DOWNLOAD_THREADS * 1.5);
        
        this.fetcher = new SimpleHttpFetcher(connectionPoolSize, userAgent);
        this.fetcher.setSocketTimeout(30*1000);
        this.fetcher.setConnectionTimeout(5*60*1000);
        this.fetcher.setMaxRetryCount(DEFAULT_MAX_RETRY_COUNT);
        this.fetcher.setDefaultMaxContentSize(10*1024*1024);
        for (String mimeTypes : DEFAULT_TEXT_MIME_TYPES) {
            this.fetcher.addValidMimeType(mimeTypes);
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
            while(downloadQueue.size() > DEFAULT_DOWNLOAD_QUEUE_MAX_SIZE) {
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
//            this.startTime = System.currentTimeMillis();

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
//            finally {
//                this.finishTime = System.currentTimeMillis();
//                System.out.println("Time to download: "+(finishTime-startTime)+"ms URL:"+link.getURL().toString());
//            }
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
