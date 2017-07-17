package focusedCrawler.rest.resources;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.URL;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableMap;

import focusedCrawler.Main;
import focusedCrawler.crawler.async.AsyncCrawler;
import focusedCrawler.util.MetricsManager;
import spark.Route;

public class CrawlerResource {
    
    public static final String VERSION = Main.class.getPackage().getImplementationVersion();

    private static final Logger logger = LoggerFactory.getLogger(CrawlerResource.class);
    private final static ObjectMapper json = new ObjectMapper();
    
    private boolean isSearchEnabled = false;
    private String dataPath;
    private String esIndexName;
    private String esTypeName;
    private AsyncCrawler crawler;

    public CrawlerResource(String dataPath, String esIndexName, String esTypeName) {
        if (esIndexName != null && esTypeName != null) {
            this.isSearchEnabled = true;
        }
        this.dataPath = dataPath;
        this.esIndexName = esIndexName;
        this.esTypeName = esTypeName;
    }

    public Route getStatus = (request, response) -> {
        Map<?, ?> crawlerStatus = ImmutableMap.of(
            "status", 200,
            "name", "ACHE Crawler",
            "version", VERSION,
            "searchEnabled", isSearchEnabled,
            "crawlerRunning", crawler == null ? false : crawler.isRunning()
        );
        return crawlerStatus;
    };
    
    public Route metricsResource = (request, response) -> {
        MetricsManager metricsManager = crawler.getMetricsManager();
        return metricsManager != null ? metricsManager.getMetricsRegistry() : null;
    };
    
    public Route startCrawl = (request, response) -> {
        try {
            StartCrawlParams params = json.readValue(request.body(), StartCrawlParams.class);

            Path configPath = Paths.get(dataPath, "config");
            Files.createDirectories(configPath);
            createConfigFile(params.crawlType, configPath);

            if ("DeepCrawl".equals(params.crawlType)) {
                String seedPath = storeSeedFile(params, configPath);
                this.crawler = AsyncCrawler.create(configPath.toString(), dataPath, seedPath, null, esIndexName, esTypeName);
            } else if ("FocusedCrawl".equals(params.crawlType)) {
                Path modelPath = configPath.resolve("model");
                storeModelFile(params, modelPath);
                String seedPath = findSeedFileInModelPackage(modelPath);
                this.crawler = AsyncCrawler.create(configPath.toString(), dataPath, seedPath, modelPath.toString(), esIndexName, esTypeName);
            } else {
                throw new IllegalArgumentException("Unrecognized crawlerType: " + params.crawlType);
            }

            this.crawler.startAsync();
            this.crawler.awaitRunning();

            return ImmutableMap.of(
                "message", "Crawler started successfully.",
                "crawlerStarted", true
            );

        } catch (Exception e) {
            logger.error("Failed to start crawler.", e);
            return ImmutableMap.of(
                "message", "Failed to start crawler.",
                "crawlerStarted", false
            );
        }
    };

    public void setCrawler(AsyncCrawler crawler) {
        this.crawler = crawler;
    }

    private String findSeedFileInModelPackage(Path model) throws IOException {
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(model)) {
            for (Path entry: stream) {
                String seedFile = entry.toString();
                if(seedFile.endsWith("seeds.txt")) {
                    return seedFile; 
                }
            }
        }
        return null;
    }

    private void createConfigFile(String crawlType, Path configPath) throws IOException {
        File configFile = configPath.resolve("ache.yml").toFile();
        URL stream;
        if ("DeepCrawl".equals(crawlType)) {
            stream = getClass().getClassLoader().getResource("config/config_website_crawl/ache.yml");
        } else if ("FocusedCrawl".equals(crawlType)) {
            stream = getClass().getClassLoader().getResource("config/config_focused_crawl/ache.yml");
        } else {
            throw new UnsupportedOperationException("Unsuported crawl type: " + crawlType);
        }
        FileUtils.copyURLToFile(stream, configFile);
    }

    private String storeSeedFile(StartCrawlParams params, Path configPath)
            throws FileNotFoundException {
        String seedFilePath = configPath.resolve("seed.txt").toString();
        try (PrintStream fileWriter = new PrintStream(seedFilePath)) {
            if (params != null && !params.seeds.isEmpty()) {
                for (String seed : params.seeds) {
                    fileWriter.println(seed);
                }
            }
        }
        return seedFilePath;
    }

    private String storeModelFile(StartCrawlParams params, Path modelPath) throws IOException {
        if (params.model != null && params.model.length > 0) {
            Path modelPathTmp = modelPath.resolve("tmp.zip");
            Files.createDirectories(modelPath);
            Files.write(modelPathTmp, params.model);
            unzipFile(modelPathTmp, modelPath);
            Files.delete(modelPathTmp);
        }
        return modelPath.toString();
    }
    
    private void unzipFile(Path file, Path outputDir) throws IOException {
        ZipFile zipFile = new ZipFile(file.toFile());
        try {
            Enumeration<? extends ZipEntry> entries = zipFile.entries();
            while (entries.hasMoreElements()) {
                ZipEntry entry = entries.nextElement();
                if(entry.getName().startsWith("training_data")) {
                    logger.info("Skiping training_data folder/file.");
                    continue;
                }
                File entryDestination = new File(outputDir.toFile(), entry.getName());
                if (entry.isDirectory()) {
                    entryDestination.mkdirs();
                } else {
                    entryDestination.getParentFile().mkdirs();
                    InputStream in = zipFile.getInputStream(entry);
                    OutputStream out = new FileOutputStream(entryDestination);
                    IOUtils.copy(in, out);
                    IOUtils.closeQuietly(in);
                    out.close();
                }
            }
        } finally {
            zipFile.close();
        }
    }

    public static class StartCrawlParams {
        public String crawlType = null;
        public List<String> seeds = null;
        public byte[] model = null;
    }

}
