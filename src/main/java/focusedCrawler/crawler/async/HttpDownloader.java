package focusedCrawler.crawler.async;

import static com.codahale.metrics.MetricRegistry.name;

import java.io.BufferedOutputStream;
import java.io.Closeable;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.codahale.metrics.ConsoleReporter;
import com.codahale.metrics.Gauge;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;
import com.codahale.metrics.Timer.Context;
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

    private static final Logger logger = LoggerFactory.getLogger(HttpDownloader.class);
    
    private final BaseFetcher fetcher;
    private final ExecutorService downloadThreadPool;
    private final ExecutorService distpatchThreadPool;
    private final LinkedBlockingQueue<Runnable> downloadQueue;
    private final LinkedBlockingQueue<Runnable> dispatchQueue;
    private final AtomicInteger numberOfDownloads = new AtomicInteger(0);
    private final AtomicInteger runningRequests = new AtomicInteger(0);
    private final AtomicInteger runningHandlers = new AtomicInteger(0);
    private final int downloadQueueMaxSize;
    private final PrintWriter requestLog;
    
    private final MetricRegistry metrics = new MetricRegistry();
    private final Timer fetchTimer = metrics.timer(name("downloader", "fetch", "time"));
    private final Timer handlerTimer = metrics.timer(name("downloader", "handler", "time"));

    private ConsoleReporter reporter;
    
	public HttpDownloader() {
		this(new HttpDownloaderConfig(), null);
	}
	
    public HttpDownloader(HttpDownloaderConfig config, String dataPath) {
    	
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
        if(dataPath == null) {
            requestLog = null;
        } else {
            Path logPath = Paths.get(dataPath, "data_monitor", "downloadrequests.csv");
            try {
                Files.createDirectories(logPath.getParent());
                this.requestLog = openLogFile(logPath);
            } catch (IOException e) {
                throw new RuntimeException("Failed to open downloader log at path: "+logPath.toString(), e);
            }
        }
        
        setupMetrics();
    }

    private void setupMetrics() {
        Gauge<Integer> downloadQueueGauge = () -> downloadQueue.size();
        metrics.register(name("downloader", "downloadQueue", "size"), downloadQueueGauge);
        
        Gauge<Integer> dispatchQueueGauge = () -> dispatchQueue.size();
        metrics.register(name("downloader", "dispatchQueue", "size"), dispatchQueueGauge);
        
        Gauge<Integer> numberOfDownloadsGauge = () -> numberOfDownloads.get();
        metrics.register(name("downloader", "pendingDownloads"), numberOfDownloadsGauge);
        
        Gauge<Integer> runningRequestsGauge = () -> runningRequests.get();
        metrics.register(name("downloader", "runningRequests"), runningRequestsGauge);
        
        Gauge<Integer> runningHandlersGauge = () -> runningHandlers.get();
        metrics.register(name("downloader", "runningHandlers"), runningHandlersGauge);
        
        reporter = ConsoleReporter.forRegistry(metrics)
                .convertRatesTo(TimeUnit.SECONDS)
                .convertDurationsTo(TimeUnit.MILLISECONDS)
                .build();
        reporter.start(10, TimeUnit.SECONDS);
    }

    private PrintWriter openLogFile(Path path) throws FileNotFoundException {
        boolean append = true;
        BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(path.toFile(), append));
        boolean autoFlush = true;
        return new PrintWriter(bos, autoFlush);
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
            while(downloadQueue.size() >= downloadQueueMaxSize) {
                Thread.sleep(10);
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
        downloadThreadPool.shutdownNow();
        distpatchThreadPool.shutdownNow();
        try {
            downloadThreadPool.awaitTermination(10, TimeUnit.SECONDS);
            distpatchThreadPool.awaitTermination(10, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            throw new RuntimeException("Failed to shutdown downloader threads.", e);
        }
        if(requestLog != null) {
            requestLog.close();
        }
        reporter.report();
        reporter.close();
    }
    
    public void await() {
        try {
            logger.info("Waiting downloads be finalized...");
            long timeWaited = 0;
            while(downloadQueue.size() > 0 || runningRequests.get() > 0) {
                Thread.sleep(10);
                timeWaited = 10;
                if(timeWaited % 5000 == 0) {
                    logger.info("Still waiting to finish downloads...");
                }
            }
            while(dispatchQueue.size() > 0 || runningHandlers.get() > 0) {
                Thread.sleep(10);
                timeWaited = 10;
                if(timeWaited % 5000 == 0) {
                    logger.info("Still waiting to process downloaded pages...");
                }
            }
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
            runningRequests.incrementAndGet();
            try {
                return doRequest();
            } catch(Throwable e) {
                logger.error("Failed to execute result handler", e);
                return null;
            } finally {
                runningRequests.decrementAndGet();
            }
        }

        private FetchedResult doRequest() {
            BaseFetchException exception = null;
            FetchedResult result = null;
            String url = link.getURL().toString();
            
            final Timer.Context context = fetchTimer.time();
            try {
                result = fetcher.get(url);
            } catch (BaseFetchException e) {
                exception = e;
            } finally {
                context.stop();
            }
            
            if(requestLog != null) {
                if(result != null) {
                    requestLog.printf("%d\t%s\t%s\t%s\n", result.getFetchTime(),
                                      result.getStatusCode(), result.getHostAddress(), url);
                } else {
                    requestLog.printf("%d\t%s\t%s\t%s\n", System.currentTimeMillis(),
                                      -1, "unknown", url);
                }
            }
            distpatchThreadPool.submit(new FetchFinishedHandler(link, result, callback, exception));
            return result;
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
            runningHandlers.incrementAndGet();
            try{
                doHandle();
            } catch(Throwable e) {
                logger.error("Failed to execute result handler", e);
            }
            finally {
                runningHandlers.decrementAndGet();
                numberOfDownloads.decrementAndGet();
            }
        }

        private void doHandle() {
            if(callback != null) {
                Context context = handlerTimer.time();
                try {
                    if(exception != null) {
                        callback.failed(link, exception);
                    } else {
                        callback.completed(link, response);
                    }
                } finally {
                    context.stop();
                }
            }
        }
        
    }

}
