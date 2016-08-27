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

import com.google.common.util.concurrent.ThreadFactoryBuilder;

import focusedCrawler.crawler.async.fetcher.FetcherFactory;
import focusedCrawler.crawler.crawlercommons.fetcher.BaseFetchException;
import focusedCrawler.crawler.crawlercommons.fetcher.BaseFetcher;
import focusedCrawler.crawler.crawlercommons.fetcher.FetchedResult;
import focusedCrawler.link.frontier.LinkRelevance;

/**
 * This class manages thread pools for downloading links. Since downloading is a
 * IO-bound process (network IO), we use a large number of threads for
 * downloads, whereas for processing the downloaded data, we use a smaller
 * number of threads, since this is usually a CPU-bound task (and thus, the
 * parallelization performance is limited by the number of CPU cores available).
 * 
 * @author aeciosantos
 *
 */
public class HttpDownloader implements Closeable {
    
    private static final int CPU_CORES = Runtime.getRuntime().availableProcessors();
    
    private final BaseFetcher fetcher;
    private final ExecutorService downloadThreadPool;
    private final ExecutorService distpatchThreadPool;
    private final LinkedBlockingQueue<Runnable> downloadQueue;
    private final LinkedBlockingQueue<Runnable> dispatchQueue;
    private final AtomicInteger numberOfDownloads = new AtomicInteger(0);
	private final int downloadQueueMaxSize;
    
	public HttpDownloader() {
		this(new HttpDownloaderConfig());
	}
	
    public HttpDownloader(HttpDownloaderConfig config) {
    	
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
        
        this.fetcher = FetcherFactory.createFetcher(config);
        
        if(config.getValidMimeTypes() != null) {
            for (String mimeTypes : config.getValidMimeTypes()) {
                this.fetcher.addValidMimeType(mimeTypes);
            }
        }
        
    }
    
    public Future<FetchedResult> dipatchDownload(String url) {
        try {
            return dipatchDownload(new URL(url), null);
        } catch (MalformedURLException e) {
            throw new IllegalArgumentException("Invalid URL provided: "+url, e);
        }
    }
    
    public Future<FetchedResult> dipatchDownload(URL url, Callback callback) {
        return dipatchDownload(new LinkRelevance(url, 0d), callback);
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
        try {        
            downloadThreadPool.shutdown();
            distpatchThreadPool.shutdown();
            downloadThreadPool.awaitTermination(5, TimeUnit.MINUTES);
            distpatchThreadPool.awaitTermination(5, TimeUnit.MINUTES);
        } catch (InterruptedException e) {
            throw new RuntimeException("Thread interrupted while waiting downloader threads finalize.", e);
        }
    }
    
    public boolean hasPendingDownloads() {
        if(numberOfDownloads.get() > 0) {
            return true;
        } else {
            return false;
        }
    }
    
    public interface Callback {
        
        public void completed(LinkRelevance link, FetchedResult result);
        
        public void failed(LinkRelevance link, Exception e);
        
    }
    
    private final class RequestTask implements Callable<FetchedResult> {
        
        private final Callback callback;
        private LinkRelevance link;
        
        public RequestTask(LinkRelevance url, Callback callback) {
            this.link = url;
            this.callback = callback;
        }
        
        @Override
        public FetchedResult call() {
            try {
                FetchedResult result = fetcher.get(link.getURL().toString());
                distpatchThreadPool.submit(new FetchFinishedHandler(link, result, callback, null));
                return result;
            } catch (BaseFetchException e) {
                distpatchThreadPool.submit(new FetchFinishedHandler(link, null, callback, e));
                return null;
            }
        }
        
    }
    
    private final class FetchFinishedHandler implements Runnable {

        final private FetchedResult response;
        final private Callback callback;
        final private BaseFetchException exception;
        final private LinkRelevance link;

        public FetchFinishedHandler(LinkRelevance link, FetchedResult response,
                                    Callback callback, BaseFetchException exception) {
            this.link = link;
            this.response = response;
            this.callback = callback;
            this.exception = exception;
        }

        @Override
        public void run() {
            if(callback != null) {
                if(exception != null) {
                    callback.failed(link, exception);
                } else {
                    callback.completed(link, response);
                }
            }
            numberOfDownloads.decrementAndGet();
        }
        
    }

}
