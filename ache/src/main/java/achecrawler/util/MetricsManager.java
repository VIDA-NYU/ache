package achecrawler.util;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.SortedMap;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.codahale.metrics.ConsoleReporter;
import com.codahale.metrics.Counter;
import com.codahale.metrics.Gauge;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;

public class MetricsManager {

    private static final Logger logger = LoggerFactory.getLogger(MetricsManager.class);

    private final MetricRegistry metrics;
    private final String storageDirectory;
    private ConsoleReporter reporter;

    public MetricsManager() {
        this(true, null);
    }

    public MetricsManager(String directoryPath) {
        this(true, directoryPath);
    }

    public MetricsManager(boolean startConsoleReporter) {
        this(startConsoleReporter, null);
    }

    public MetricsManager(boolean startConsoleReporter, String directoryPath) {
        this(new MetricRegistry(), startConsoleReporter, directoryPath);
    }

    public MetricsManager(MetricRegistry metricsRegistry, boolean startConsoleReporter,
                          String directoryPath) {
        if (directoryPath != null) {
            this.metrics = loadMetrics(metricsRegistry, directoryPath);
        } else {
            this.metrics = metricsRegistry;
        }
        this.storageDirectory = directoryPath;
        if (startConsoleReporter) {
            reporter = ConsoleReporter.forRegistry(metrics).convertRatesTo(TimeUnit.SECONDS)
                    .convertDurationsTo(TimeUnit.MILLISECONDS).build();
            reporter.start(10, TimeUnit.SECONDS);
        }
    }

    public Timer getTimer(String name) {
        return metrics.timer(name);
    }

    public Counter getCounter(String name) {
        return metrics.counter(name);
    }

    public void register(String name, Gauge<?> gauge) {
        metrics.register(name, gauge);
    }

    /**
     * Saves the metrics to a file for it to be reloaded when the crawler restarts
     * 
     * @param metricsRegistry
     * @param directoryPath
     * @throws IOException
     */
    private void saveMetrics(MetricRegistry metricsRegistry, String directoryPath) {
        String directoryName = directoryPath + "/metrics/";
        File file = new File(directoryName);
        if (file.exists()) {
            file.delete();
        }
        file.mkdir();
        try {
            File outputFile = new File(directoryName + "metrics_counters.data");
            BufferedWriter writer = new BufferedWriter(new FileWriter(outputFile));
            SortedMap<String, Counter> counters = metrics.getCounters();
            for (String counter : counters.keySet()) {
                Counter c = counters.get(counter);
                writer.write(counter + ":" + c.getCount());
                writer.newLine();
                writer.flush();
            }
            writer.flush();
            writer.close();
        } catch (IOException e) {
            logger.error("Unable to save metrics to a file." + e.getMessage());
        }
    }

    void saveMetrics(String directoryPath) {
        this.saveMetrics(metrics, directoryPath);
    }

    private void saveMetrics() {
        this.saveMetrics(metrics, storageDirectory);
    }

    /**
     * Loads metrics from the file at the directory path
     * 
     * @param metricsRegistry
     * @param directoryPath
     * @return
     * @throws IOException
     */
    private MetricRegistry loadMetrics(MetricRegistry metricsRegistry, String directoryPath) {
        File metricsFile = new File(directoryPath + "/metrics/metrics_counters.data");
        if (metricsFile.exists()) {
            try {
                BufferedReader reader = new BufferedReader(new FileReader(metricsFile));
                String line = null;
                while ((line = reader.readLine()) != null) {
                    String[] input = line.split(":");
                    Counter counter = metricsRegistry.counter(input[0]);
                    counter.inc(Integer.parseInt(input[1]));
                }
                reader.close();
            } catch (Exception e) {
                logger.error("Unable to deserialize counters : " + e.getMessage());
            }
        }
        return metricsRegistry;
    }

    public void close() {
        if (reporter != null) {
            reporter.report();
            reporter.close();
        }
        saveMetrics();
    }

    public MetricRegistry getMetricsRegistry() {
        return metrics;
    }

}
