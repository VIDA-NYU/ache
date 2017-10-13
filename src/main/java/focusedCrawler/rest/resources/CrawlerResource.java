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
import java.util.Optional;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMap.Builder;

import focusedCrawler.Main;
import focusedCrawler.config.Configuration;
import focusedCrawler.crawler.async.AsyncCrawler;
import focusedCrawler.util.MetricsManager;
import spark.Request;
import spark.Route;

public class CrawlerResource {
    
    public static final String VERSION = Main.class.getPackage().getImplementationVersion();

    private static final Logger logger = LoggerFactory.getLogger(CrawlerResource.class);
    private final static ObjectMapper json = new ObjectMapper();
    
    private boolean isSearchEnabled = false;
    private String dataPath;
    private AsyncCrawler crawler;
    private Configuration config;

    private ElasticsearchProxyResource esProxyResource;


    public CrawlerResource(Configuration config, String dataPath, ElasticsearchProxyResource esProxyResource) {
        this.config = config;
        this.dataPath = dataPath;
        this.esProxyResource = esProxyResource;
        this.isSearchEnabled = config.getTargetStorageConfig().isElasticsearchRestEnabled();
    }

    public Route getStatus = (request, response) -> {
        Builder<Object, Object> builder = ImmutableMap.builder()
                .put("status", 200)
                .put("version", VERSION)
                .put("searchEnabled", isSearchEnabled);
        try {
            if(this.esProxyResource.isElasticsearchEnabled()) {
                builder.put("esIndexName", esProxyResource.getIndexName())
                       .put("esTypeName",  esProxyResource.getTypeName());
            }
            builder.put("crawlerRunning", crawler == null ? false : crawler.isRunning())
                   .put("crawlerState", crawler == null ? "NEW" : crawler.state().toString());
        } catch(Exception e) {
            logger.error("ERROR",e);
        }
        Map<?, ?> crawlerStatus = builder.build();
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
            Path modelPath = configPath.resolve("model");

            Configuration newConfig = createConfigForCrawlType(config, configPath, params);

            String storedModelPath = storeModelFile(params.model, modelPath);
            String seedPath = getSeedForCrawlType(params, configPath, storedModelPath);

            this.crawler = AsyncCrawler.create(configPath.toString(), dataPath, seedPath,
                    storedModelPath, params.esIndexName, params.esTypeName);
            
            this.crawler.startAsync();
            this.esProxyResource.updateConfig(newConfig);
            this.isSearchEnabled = newConfig.getTargetStorageConfig().isElasticsearchRestEnabled();

            return ImmutableMap.of(
                "message", "Crawler started successfully.",
                "crawlerStarted", true
            );

        } catch (Exception e) {
            logger.error("Failed to start crawler.", e);
            response.status(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            return ImmutableMap.of(
                "message", "Failed to start crawler.",
                "crawlerStarted", false
            );
        }
    };

    public Route stopCrawl = (request, response) -> {
        try {
            if(crawler == null) {
                response.status(HttpServletResponse.SC_BAD_REQUEST);
                return ImmutableMap.of(
                    "message", "Crawler is not running.",
                    "shutdownInitiated", false,
                    "crawlerStopped", false
                );
            }

            boolean awaitStopped = getParamAsBoolean("awaitStopped", request).orElse(false);
            this.crawler.stopAsync();
            if(awaitStopped) {
                this.crawler.awaitTerminated();
                return ImmutableMap.of(
                    "message", "Crawler stopped successfully.",
                    "shutdownInitiated", true,
                    "crawlerStopped", true
                );
            } else {
                return ImmutableMap.of(
                    "message", "Crawler shutdown initiated.",
                    "shutdownInitiated", true,
                    "crawlerStopped", false
                );
            }
        } catch (Exception e) {
            logger.error("Failed to stop crawler.", e);
            response.status(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            return ImmutableMap.of(
                "message", "Failed to stop crawler.",
                "shutdownInitiated", false,
                "crawlerStopped", false
            );
        }
    };

    public Route addSeeds = (request, response) -> {
        try {
            if(crawler == null) {
                response.status(HttpServletResponse.SC_BAD_REQUEST);
                return ImmutableMap.of(
                    "message", "Crawler is not running.",
                    "addedSeeds", false
                );
            }

            AddSeedsParam params = json.readValue(request.body(), AddSeedsParam.class);
            if (params.seeds == null || params.seeds.isEmpty()) {
                response.status(HttpServletResponse.SC_BAD_REQUEST);
                return ImmutableMap.of(
                    "message", "No seeds provided.",
                    "addedSeeds", false
                );
            }
            crawler.addSeeds(params.seeds);
            return ImmutableMap.of(
                "message", "Seeds added successfully.",
                "addedSeeds", true
            );
        } catch (Exception e) {
            logger.error("Failed to add seeds.", e);
            response.status(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            return ImmutableMap.of(
                "message", "Failed to add seeds.",
                "addedSeeds", false
            );
        }
    };

    private Optional<Boolean> getParamAsBoolean(String paramName, Request request) {
        try {
            Boolean valueOf = Boolean.valueOf(request.queryParams(paramName));
            return Optional.of(valueOf);
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    private String getSeedForCrawlType(StartCrawlParams params, Path configPath,
            String storedModelPath) throws FileNotFoundException, IOException {
        String seedPath;
        switch (params.crawlType) {
            case "DeepCrawl":
                seedPath = storeSeedFile(params.seeds, configPath);
                break;
            case "FocusedCrawl":
                seedPath = findSeedFileInModelPackage(storedModelPath);
                break;
            default:
                throw new IllegalArgumentException("Unknown crawlerType: " + params.crawlType);
        }
        return seedPath;
    }

    public void setCrawler(AsyncCrawler crawler) {
        this.crawler = crawler;
    }

    private String findSeedFileInModelPackage(String model) throws IOException {
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(Paths.get(model))) {
            for (Path entry: stream) {
                String seedFile = entry.toString();
                if(seedFile.endsWith("seeds.txt")) {
                    return seedFile; 
                }
            }
        }
        return null;
    }

    private Configuration createConfigForCrawlType(Configuration baseConfig, Path configPath,
                                                   StartCrawlParams params) throws IOException {
        String esIndexName = params.esIndexName;
        String esTypeName = params.esTypeName;

        URL configLocation = getConfigForCrawlType(params.crawlType);
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

    private URL getConfigForCrawlType(String crawlType) {
        String fileName;
        switch (crawlType) {
            case "DeepCrawl":
                fileName = "config/config_website_crawl/ache.yml";
                break;
            case "FocusedCrawl":
                fileName = "config/config_focused_crawl/ache.yml";
                break;
            default:
                throw new UnsupportedOperationException("Unsuported crawl type: " + crawlType);
        }
        return getClass().getClassLoader().getResource(fileName);
    }

    private String storeSeedFile(List<String> seeds, Path configPath)
            throws FileNotFoundException {
        String seedFilePath = configPath.resolve("seed.txt").toString();
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
        public String crawlType;
        public List<String> seeds;
        public byte[] model;
        public String esTypeName;
        public String esIndexName;
    }

    public static class AddSeedsParam {
        public List<String> seeds;
    }

}
