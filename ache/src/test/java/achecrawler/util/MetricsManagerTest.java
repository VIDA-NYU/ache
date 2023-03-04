package achecrawler.util;

import java.io.File;
import java.nio.file.Paths;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import com.codahale.metrics.Counter;

import static org.assertj.core.api.Assertions.assertThat;

public class MetricsManagerTest {

    @TempDir
    public File tempFolder;

    @Test
    void testSavingOfMetrics() {
		File metricsManagerPath = Paths.get(tempFolder.getAbsolutePath(), "metrics_manager_path").toFile();
		MetricsManager metricsManager = new MetricsManager(metricsManagerPath.getAbsolutePath());

		Counter counter = metricsManager.getCounter("sample_counter");
		counter.inc();

		String directoryPath = Paths.get(tempFolder.getAbsolutePath(), "saved_path").toString();
		metricsManager.saveMetrics(directoryPath);
		assertThat(new File(directoryPath + "/metrics/metrics_counters.data").exists()).isTrue();
	}

    @Test
    void testLoadingOfMetrics() {
		File metricsManagerPath = Paths.get(tempFolder.getAbsolutePath(), "metrics_manager_path").toFile();
		MetricsManager metricsManager = new MetricsManager(metricsManagerPath.getAbsolutePath());

		Counter counter = metricsManager.getCounter("sample_counter");
		counter.inc();
		counter.inc();

		String directoryPath = Paths.get(tempFolder.getAbsolutePath(), "saved_path").toString();
		metricsManager.saveMetrics(directoryPath);

		MetricsManager metricsManager2 = new MetricsManager(directoryPath);
		Counter testCounter = metricsManager2.getCounter("sample_counter");
        assertThat(testCounter.getCount()).isEqualTo(2);
	}

    @Test
    void testLoadingOfMetrics2Counters() {
		File metricsManagerPath = Paths.get(tempFolder.getAbsolutePath(), "metrics_manager_path").toFile();
		MetricsManager metricsManager = new MetricsManager(metricsManagerPath.getAbsolutePath());

		Counter counter = metricsManager.getCounter("sample_test_counter");
		counter.inc();
		counter.inc();

		Counter sampleCounter2 = metricsManager.getCounter("sample_test_counter2");
		for (int i = 0; i < 10; i++) {
			sampleCounter2.inc();
		}

		String directoryPath = Paths.get(tempFolder.getAbsolutePath(), "saved_path").toString();
		metricsManager.saveMetrics(directoryPath);

		MetricsManager metricsManager2 = new MetricsManager(directoryPath);
		Counter testCounter = metricsManager2.getCounter("sample_test_counter");
        assertThat(testCounter.getCount()).isEqualTo(2);
		
		Counter testCounter2 = metricsManager2.getCounter("sample_test_counter2");
        assertThat(testCounter2.getCount()).isEqualTo(10);
	}
}
