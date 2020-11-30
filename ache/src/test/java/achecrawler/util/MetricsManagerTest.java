package achecrawler.util;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import com.codahale.metrics.Counter;

public class MetricsManagerTest {

	MetricsManager metricsManager;

	@Rule
	public TemporaryFolder tempFolder = new TemporaryFolder();

	@Before
	public void setUp() throws IOException {
		// metricsManager = new MetricsManager("/tmp/testing");
		metricsManager = new MetricsManager(tempFolder.newFolder().getAbsolutePath());
	}

	@Test
	public void testSavingOfMetrics() throws IOException {
		Counter counter = metricsManager.getCounter("sample_counter");
		counter.inc();

		String directoryPath = tempFolder.newFolder().getAbsolutePath(); // "/tmp/testing";
		metricsManager.saveMetrics(directoryPath);
		assertTrue(new File(directoryPath + "/metrics/metrics_counters.data").exists());
	}

	@Test
	public void testLoadingOfMetrics() throws IOException {
		Counter counter = metricsManager.getCounter("sample_counter");
		counter.inc();
		counter.inc();

		String directoryPath = tempFolder.newFolder().getAbsolutePath(); // "/tmp/testing";
		metricsManager.saveMetrics(directoryPath);

		MetricsManager metricsManager2 = new MetricsManager(directoryPath);
		Counter testCounter = metricsManager2.getCounter("sample_counter");
		assertTrue(testCounter.getCount() == 2);

	}

	@Test
	public void testLoadingOfMetrics2Counters() throws IOException {
		Counter counter = metricsManager.getCounter("sample_test_counter");
		counter.inc();
		counter.inc();

		Counter sampleCounter2 = metricsManager.getCounter("sample_test_counter2");
		for (int i = 0; i < 10; i++) {
			sampleCounter2.inc();
		}

		String directoryPath = tempFolder.newFolder().getAbsolutePath(); // "/tmp/testing";
		metricsManager.saveMetrics(directoryPath);

		MetricsManager metricsManager2 = new MetricsManager(directoryPath);
		Counter testCounter = metricsManager2.getCounter("sample_test_counter");
		assertTrue(testCounter.getCount() == 2);
		
		Counter testCounter2 = metricsManager2.getCounter("sample_test_counter2");
		assertTrue(testCounter2.getCount() == 10);
	}
}
