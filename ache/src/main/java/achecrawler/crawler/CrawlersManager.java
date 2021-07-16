package achecrawler.crawler;

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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.common.collect.ImmutableMap;

import achecrawler.config.Configuration;
import achecrawler.crawler.async.AsyncCrawler;
import achecrawler.rest.resources.CrawlerResource.StartCrawlParams;
import achecrawler.target.repository.elasticsearch.ElasticSearchConfig;

public class CrawlersManager {

    private static Logger logger = LoggerFactory.getLogger(CrawlersManager.class);

    private Configuration baseConfig;
    private String baseDataPath;

    private Map<String, CrawlContext> crawlers = new HashMap<>();

    public CrawlersManager(String dataPath) {
        this(dataPath, new Configuration());
    }

    public CrawlersManager(String baseDataPath, Configuration baseConfig) {
        this.baseConfig = baseConfig;
        this.baseDataPath = baseDataPath;
    }

    public CrawlContext startCrawl(String crawlerId) {
        if (!crawlers.containsKey(crawlerId)) {
            throw new IllegalArgumentException("No crawler with crawler_id: " + crawlerId);
        }
        CrawlContext crawlContext = crawlers.get(crawlerId);
        crawlContext.getCrawler().startAsync();
        return crawlContext;
    }

    public CrawlContext getCrawl(String crawlerId) {
        return crawlers.get(crawlerId);
    }

    public Map<String, CrawlContext> getCrawls() {
        return ImmutableMap.copyOf(crawlers);
    }

    public CrawlContext createCrawler(String crawlerId, StartCrawlParams params) throws Exception {
        return createCrawler(crawlerId, params.crawlType, params.seeds, params.model,
                params.esIndexName, params.esTypeName);
    }

    public CrawlContext createCrawler(String crawlerId, CrawlType crawlType,
            List<String> seeds, byte[] model) throws Exception {
        return createCrawler(crawlerId, crawlType, seeds, model, null, null);
    }

    public CrawlContext createCrawler(String crawlerId, CrawlType crawlType, List<String> seeds,
            byte[] model, String esIndexName, String esTypeName)
            throws Exception {

        Path configPath = Paths.get(baseDataPath, crawlerId, "config");
        createConfigForCrawlType(baseConfig, configPath, crawlType, esIndexName, esTypeName);

        String modelPath = storeModelFile(model, configPath.resolve("model"));
        String seedPath = getSeedForCrawlType(crawlType, seeds, configPath, modelPath);

        return createCrawler(crawlerId, configPath.toString(), seedPath, modelPath, esIndexName,
                esTypeName);
    }

    public CrawlContext createCrawler(String crawlerId, String configPath, String seedPath,
            String modelPath,
            String esIndexName, String esTypeName) throws Exception {

        String dataPath = Paths.get(baseDataPath, crawlerId).toString();

        CrawlContext context = new CrawlContext();
        context.crawlerId = crawlerId;
        context.dataPath = dataPath;
        context.seedPath = seedPath;
        context.modelPath = modelPath;
        context.crawler = AsyncCrawler.create(crawlerId, configPath, dataPath, seedPath, modelPath,
            esIndexName, esTypeName);

        crawlers.put(crawlerId, context);
        return context;
    }

    private Configuration createConfigForCrawlType(Configuration baseConfig, Path configPath,
            CrawlType crawlType, String esIndexName, String esTypeName) throws IOException {

        URL configLocation = getConfigForCrawlType(crawlType);
        InputStream configStream = configLocation.openStream();
        try {
            Configuration crawlConfig = baseConfig.copyUpdating(configStream);
            if (esIndexName != null && !esIndexName.isEmpty()) {
                crawlConfig.getTargetStorageConfig().getElasticSearchConfig()
                        .setIndexName(esIndexName);
            }
            if (esTypeName != null && !esTypeName.isEmpty()) {
                crawlConfig.getTargetStorageConfig().getElasticSearchConfig()
                        .setTypeName(esTypeName);
            }
            Files.createDirectories(configPath);
            crawlConfig.writeToFile(configPath.resolve("ache.yml"));
            return crawlConfig;
        } finally {
            configStream.close();
        }
    }

    private URL getConfigForCrawlType(CrawlType crawlType) {
        String fileName;
        switch (crawlType) {
            case DeepCrawl:
                fileName = "config/config_website_crawl/ache.yml";
                break;
            case FocusedCrawl:
                fileName = "config/config_focused_crawl/ache.yml";
                break;
            default:
                throw new UnsupportedOperationException("Unsuported crawl type: " + crawlType);
        }
        return getClass().getClassLoader().getResource(fileName);
    }

    private String getSeedForCrawlType(CrawlType crawlType, List<String> seeds, Path configPath,
            String storedModelPath) throws FileNotFoundException, IOException {
        String seedPath;
        switch (crawlType) {
            case DeepCrawl:
                seedPath = storeSeedFile(seeds, configPath);
                break;
            case FocusedCrawl:
                seedPath = findSeedFileInModelPackage(storedModelPath);
                break;
            default:
                throw new IllegalArgumentException("Unknown crawlerType: " + crawlType);
        }
        return seedPath;
    }

    private String storeSeedFile(List<String> seeds, Path configPath)
            throws FileNotFoundException {
        String seedFilePath = configPath.resolve("seeds.txt").toString();
        try (PrintStream fileWriter = new PrintStream(seedFilePath)) {
            if (seeds != null && !seeds.isEmpty()) {
                for (String seed : seeds) {
                    fileWriter.println(seed);
                }
            }
        }
        return seedFilePath;
    }

    private String storeModelFile(byte[] model, Path modelPath) throws IOException {
        if (model != null && model.length > 0) {
            Path modelPathTmp = modelPath.resolve("tmp.zip");
            Files.createDirectories(modelPath);
            Files.write(modelPathTmp, model);
            unzipFile(modelPathTmp, modelPath);
            Files.delete(modelPathTmp);
            return modelPath.toString();
        } else {
            return null;
        }
    }

    private void unzipFile(Path file, Path outputDir) throws IOException {
        ZipFile zipFile = new ZipFile(file.toFile());
        try {
            Enumeration<? extends ZipEntry> entries = zipFile.entries();
            while (entries.hasMoreElements()) {
                ZipEntry entry = entries.nextElement();
                if (entry.getName().startsWith("training_data")) {
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

    private String findSeedFileInModelPackage(String model) throws IOException {
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(Paths.get(model))) {
            for (Path entry : stream) {
                String seedFile = entry.toString();
                if (seedFile.endsWith("seeds.txt")) {
                    return seedFile;
                }
            }
        }
        return null;
    }

    public class CrawlContext {

        public String crawlerId;

        @JsonIgnore
        public AsyncCrawler crawler;
        @JsonIgnore
        public String dataPath;
        @JsonIgnore
        public String seedPath;
        @JsonIgnore
        public String modelPath;

        @JsonIgnore
        public AsyncCrawler getCrawler() {
            return this.crawler;
        }

        public String getCrawlerId() {
            return crawlerId;
        }

        public String getCrawlerState() {
            return crawler == null ? "NEW" : crawler.state().toString();
        }

        public boolean isCrawlerRunning() {
            return crawler == null ? false : crawler.isRunning();
        }

        public boolean isSearchEnabled() {
            return crawler.getConfig().getTargetStorageConfig().isElasticsearchRestEnabled();
        }

        public String getEsIndexName() {
            return isSearchEnabled() ? getEsConfig().getIndexName() : null;
        }

        public String getEsTypeName() {
            return isSearchEnabled() ? getEsConfig().getTypeName() : null;
        }

        @JsonIgnore
        public ElasticSearchConfig getEsConfig() {
            return crawler.getConfig().getTargetStorageConfig().getElasticSearchConfig();
        }

    }

    public enum CrawlType {
        DeepCrawl, FocusedCrawl
    }

}
