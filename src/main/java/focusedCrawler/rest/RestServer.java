package focusedCrawler.rest;

import static java.util.Objects.requireNonNull;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.lang.management.ManagementFactory;
import java.net.DatagramSocket;
import java.net.ServerSocket;
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
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.codahale.metrics.jvm.ThreadDump;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableMap;

import focusedCrawler.Main;
import focusedCrawler.config.ConfigService;
import focusedCrawler.crawler.async.AsyncCrawler;
import focusedCrawler.target.TargetStorageConfig;
import focusedCrawler.target.repository.elasticsearch.ElasticSearchConfig;
import focusedCrawler.util.MetricsManager;
import spark.Route;
import spark.Service;

public class RestServer {
    
    public static final String VERSION = Main.class.getPackage().getImplementationVersion();
    
    private static final Logger logger = LoggerFactory.getLogger(RestServer.class);
    
    private RestConfig restConfig;
    private Service server;
    private String dataPath;
    
    private boolean isSearchEnabled = false;
    private String esHostAddress;
    private String esIndexName;
    private String esTypeName;
    private CloseableHttpClient httpclient;
    private LabelsManager labelsManager;
    private AsyncCrawler crawler;


    private RestServer(String dataPath, RestConfig restConfig) {
        this(dataPath, restConfig, null, null, null);
    }
    
    private RestServer(String dataPath, RestConfig restConfig, String esIndexName,
                       String esTypeName, String esHostAddress) {
        this.dataPath = dataPath;
        this.restConfig = restConfig;
        if (esIndexName != null && esHostAddress != null) {
            this.esIndexName = esIndexName;
            this.esHostAddress = esHostAddress;
            isSearchEnabled = true;
            if (esTypeName != null && !esTypeName.isEmpty()) {
                this.esTypeName = esTypeName;
            } else {
                this.esTypeName = "page"; // default type name
            }
            this.httpclient = HttpClients.createDefault();
            this.labelsManager = new LabelsManager(dataPath);
        }
    }
 
    public void start() {
        
        String host = restConfig.getHost();
        int port = restConfig.getPort();
        while(!portIsAvailable(port) && port < restConfig.getPort()+100) {
            logger.error("Port {} not available. Trying port {}.", port, port+1);
            port++;
        }
        
        server = Service.ignite();
        server.port(port);
        server.ipAddress(host);
        
        server.staticFiles.location("/public");

        if(restConfig.isEnableCors()) {
            enableCORS("*", "GET");
        }
        
        /*
         * API endpoints routes
         */
        server.get("/status",      Transformers.json(crawlerStatusResource));
        server.get("/metrics",     Transformers.json(metricsResource));
        server.get("/thread/dump", Transformers.text(threadDumpResource));
        
        server.post("/startCrawl", "*/*", Transformers.json(startCrawlResource));
        
        if(isSearchEnabled) {
            /*
             * Elasticsearch proxy routes
             */
            server.get("/_search", "*/*", elasticsearchApiProxy);
            server.post("/_search", "*/*", elasticsearchApiProxy);
            
            /*
             * Endpoints for labeling web pages
             */
            server.get( "/labels", Transformers.json(labelsManager.getLabelsResource));
            server.put( "/labels", Transformers.json(labelsManager.addLabelsResource));
            server.post("/labels", Transformers.json(labelsManager.addLabelsResource));
        }
        
        /*
         * Routes used by the static web application
         */
        server.get("/search",     StaticFileEngine.noopRouter, StaticFileEngine.engine);
        server.get("/monitoring", StaticFileEngine.noopRouter, StaticFileEngine.engine);
        server.get("/startCrawl", StaticFileEngine.noopRouter, StaticFileEngine.engine);
        
        server.awaitInitialization();
        
        logger.info("---------------------------------------------");
        logger.info("ACHE server available at http://{}:{}", host, port);
        logger.info("---------------------------------------------");
    }
    
    private Route crawlerStatusResource = (request, response) -> {
        Map<?, ?> crawlerStatus = ImmutableMap.of(
            "status", 200,
            "name", "ACHE Crawler",
            "version", VERSION,
            "searchEnabled", isSearchEnabled,
            "crawlerRunning", crawler == null ? false : crawler.isRunning()
        );
        return crawlerStatus;
    };
    
    private final static ObjectMapper json = new ObjectMapper();
    
    public static class StartCrawlParams {
        public String crawlType = null;
        public List<String> seeds = null;
        public byte[] model = null;
    }
    
    private Route startCrawlResource = (request, response) -> {
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
            throw new UnsupportedOperationException("Unsuported crawl type");
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
            System.out.println("ModelTmp.zip: "+modelPathTmp.toString());
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
                    System.out.println("Skiping training_data folder/file.");
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

    private Route elasticsearchApiProxy = (request, response) -> {
        try {
            String query = "";
            for (String param : request.queryParams()) {
                query += param + "=" + request.queryParams(param);
            }
            String url = String.format("%s/%s/%s/_search", esHostAddress, esIndexName, esTypeName);
            if (!query.isEmpty()) {
                url += "?" + query;
            }
            HttpPost post = new HttpPost(url);
            post.setEntity(new StringEntity(request.body(), "UTF-8"));
            CloseableHttpResponse apiResponse = httpclient.execute(post);
            try {
                HttpEntity entity = apiResponse.getEntity();
                Header[] headers = apiResponse.getAllHeaders();
                for (Header header : headers) {
                    response.header(header.getName(), header.getValue());
                }
                String body = EntityUtils.toString(entity);
                response.body(body);
                return body;
            } finally {
                apiResponse.close();
            }
        } catch (Exception e) {
            logger.error("Failed to forward request to ElasticSearch.", e);
            throw e;
        }
    };

    private Route metricsResource = (request, response) -> {
        MetricsManager metricsManager = crawler.getMetricsManager();
        return metricsManager != null ? metricsManager.getMetricsRegistry() : null;
    };
    
    private ThreadDump threadDump = new ThreadDump(ManagementFactory.getThreadMXBean());
    
    private Route threadDumpResource = (request, response) -> {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        threadDump.dump(baos);
        return baos.toString();
    };

    private boolean portIsAvailable(int port) {
        ServerSocket ss = null;
        DatagramSocket ds = null;
        try {
            ss = new ServerSocket(port);
            ss.setReuseAddress(true);
            ds = new DatagramSocket(port);
            ds.setReuseAddress(true);
            return true;
        } catch (IOException e) {
            return false;
        } finally {
            if (ds != null)  ds.close();
            if (ss != null) {
                try {
                    ss.close();
                } catch (IOException e) {
                }
            }
        }
    }

    public void shutdown() {
        server.stop();
        try {
            if(this.httpclient != null) {
                this.httpclient.close();
            }
        } catch (IOException e) {
            logger.error("Failed to close http client.", e);
        }
    }
    
    private void enableCORS(final String origin, final String methods) {

        server.options("/*", (request, response) -> {

            String accessControlRequestHeaders = request.headers("Access-Control-Request-Headers");
            if (accessControlRequestHeaders != null) {
                response.header("Access-Control-Allow-Headers", accessControlRequestHeaders);
            }

            String accessControlRequestMethod = request.headers("Access-Control-Request-Method");
            if (accessControlRequestMethod != null) {
                response.header("Access-Control-Allow-Methods", accessControlRequestMethod);
            }

            return "OK";
        });

        server.before((request, response) -> {
            response.header("Access-Control-Allow-Origin", origin);
            response.header("Access-Control-Request-Method", methods);
        });
        
    }

    public static RestServer create(String dataPath, RestConfig restConfig) {
        return new RestServer(dataPath, restConfig);
    }

    public static RestServer create(String configPath, String dataPath,
                                    String esIndexName, String esTypeName) {
        requireNonNull(configPath, "A config path must be provided.");
        requireNonNull(dataPath, "A data path must be provided.");
        ConfigService config = new ConfigService(configPath);
        TargetStorageConfig targetStorageConfig = config.getTargetStorageConfig();
        ElasticSearchConfig esConfig = targetStorageConfig.getElasticSearchConfig();
        List<String> hosts = esConfig.getRestApiHosts();
        if (hosts != null && !hosts.isEmpty()) {
            requireNonNull(esIndexName, "Elasticsearch index name should be provided when using ELASTICSEARCH data format.");
            if(esTypeName == null || esTypeName.isEmpty()) {
                esTypeName = "page";
            }
            String esHostAddress = hosts.iterator().next();
            return new RestServer(dataPath, config.getRestConfig(), esIndexName, esTypeName, esHostAddress);
        } else {
            return new RestServer(dataPath, config.getRestConfig());
        }
    }

}
