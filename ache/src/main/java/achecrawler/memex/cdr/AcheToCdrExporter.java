package achecrawler.memex.cdr;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.zip.GZIPOutputStream;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.hash.HashFunction;
import com.google.common.hash.Hasher;
import com.google.common.hash.Hashing;

import achecrawler.target.model.Page;
import achecrawler.target.repository.FileSystemTargetRepository;
import achecrawler.target.repository.FileSystemTargetRepository.DataFormat;
import achecrawler.target.repository.FilesTargetRepository;
import achecrawler.target.repository.WarcTargetRepository;
import achecrawler.tools.SimpleBulkIndexer;
import achecrawler.util.CliTool;
import achecrawler.util.CloseableIterator;
import achecrawler.util.persistence.PersistentHashtable;
import io.airlift.airline.Command;
import io.airlift.airline.Option;

@Command(name = "AcheToCdrExporter", description = "Exports crawled data to CDR format")
public class AcheToCdrExporter extends CliTool {

    private static final ObjectMapper jsonMapper = new ObjectMapper();

    //
    // Input data options
    //

    @Option(name = "--input-path", description = "Path to ACHE data target folder", required = true)
    private String inputPath;

    @Option(name = {"--repository-type", "-rt"},
            description = "Which repository type should be used")
    private RepositoryType repositoryType = RepositoryType.FILES;

    public enum RepositoryType {
        FILES, FILESYSTEM_JSON, WARC;
    }

    @Option(name = "--fs-hashed",
            description = "Whether ACHE filesystem repository files names are hashed")
    private boolean hashFilename = false;

    @Option(name = "--fs-compressed",
            description = "Whether ACHE filesystem repository files is compressed")
    private boolean compressData = false;

    //
    // Options for output data format
    //

    @Option(name = "--cdr-version", description = "Which CDR version should be used")
    private CDRVersion cdrVersion = CDRVersion.CDRv31;

    public enum CDRVersion {
        CDRv2, CDRv3, CDRv31
    }

    @Option(name = "--output-file",
            description = "Gziped output file containing data formmated as per CDR schema")
    private String outputFile;

    @Option(name = "--skip-relevant", description = "Whether relevant pages should be skipped")
    private boolean skipRelevant = false;

    @Option(name = "--skip-irrelevant", description = "Whether irrelevant pages should be skipped")
    private boolean skipIrrelevant = false;

    // Elastic Search output options

    @Option(name = {"--output-es-index", "-oi"}, description = "ElasticSearch index name (output)")
    String outputIndex;

    @Option(name = {"--output-es-type", "-ot"}, description = "ElasticSearch index type (output)")
    String outputType;

    @Option(name = {"--output-es-url", "-ou"}, description = "ElasticSearch full HTTP URL address")
    String elasticSearchServer = null;

    @Option(name = {"--output-es-auth", "-oa"},
            description = "User and password for ElasticSearch in format: user:pass")
    String userPass = null;

    @Option(name = {"--output-es-bulk-size", "-obs"}, description = "ElasticSearch bulk size")
    int bulkSize = 25;

    // AWS S3 Support

    @Option(name = {"--accesskey", "-ak"}, description = "AWS ACCESS KEY ID")
    String accessKeyID = "";

    @Option(name = {"--secretkey", "-sk"}, description = "AWS SECRET KEY ID")
    String secretKeyID = "";

    @Option(name = {"--bucket", "-bk"}, description = "AWS S3 BUCKET NAME")
    String bucketName = "";

    @Option(name = {"--region", "-rg"}, description = "AWS S3 Region name")
    String region = "us-east-1";

    @Option(name = {"--skip-upload", "-su"}, description = "Disable upload of objects to S3")
    private boolean skipUpload = false;

    @Option(name = {"--tmp-path", "-tmp"}, description = "Path to temporary working folder")
    String temp = null;

    //
    // Kafka options
    //
    @Option(name = {"--kafka-props"},
            description = "Path to properties files to configure Kafka client")
    String kafkaProperties = null;

    @Option(name = {"--kafka-topic"}, description = "Kafka topic name")
    String kafkaTopicName = null;

    //
    // Runtime variables
    //
    private int processedPages = 0;
    private String id;
    private Object doc;
    
    private PrintWriter out;
    private SimpleBulkIndexer bulkIndexer;
    private PersistentHashtable<CDR31MediaObject> mediaObjectCache;
    private S3Uploader s3Uploader;
    private KafkaProducer<String, String> kafkaProducer;

    public static void main(String[] args) throws Exception {
        CliTool.run(args, new AcheToCdrExporter());
    }

    @Override
    public void execute() throws Exception {

        System.out.println("Reading ACHE data from: " + inputPath);
        System.out.println("Generating CDR file at: " + outputFile);
        System.out.println(" Compressed repository: " + compressData);
        System.out.println("      Hashed file name: " + hashFilename);

        if (!skipUpload) {
            if (temp == null) {
                Path tmpPath = Files.createTempDirectory("cdr-export-tmp");
                Files.createDirectories(tmpPath);
                temp = tmpPath.toString();
            }

            mediaObjectCache = new PersistentHashtable<CDR31MediaObject>(
                    temp, 1000, CDR31MediaObject.class);

            s3Uploader = new S3Uploader(this.accessKeyID, this.secretKeyID,
                    this.bucketName, this.region);
        }

        if (outputFile != null) {
            GZIPOutputStream gzipStream = new GZIPOutputStream(new FileOutputStream(outputFile));
            out = new PrintWriter(gzipStream, true);
        }
        
        this.bulkIndexer = initializeElasticsearchIndexer();
        this.kafkaProducer = initializeKafkaProducer();

        if(!skipUpload) {
            // Process media files
            System.out.println("Pre-processing media files...");
            try(CloseableIterator<Page> it = createIterator()) {
                while (it.hasNext()) {
                    Page pageModel = it.next();
                    try {
                        processMediaFile(pageModel);
                    } catch (Exception e) {
                        System.err.println("Failed to process record.\n" + e.toString());
                    }
                }
            }
            mediaObjectCache.commit();
        }

        // Process HTML files
        System.out.println("Processing HTML pages...");
        try(CloseableIterator<Page> it = createIterator()) {
            while (it.hasNext()) {
                Page pageModel = it.next();
                try {
                    processRecord(pageModel);
                    processedPages++;
                    if (processedPages % 100 == 0) {
                        System.out.printf("Processed %d pages\n", processedPages);
                    }
                } catch (Exception e) {
                    System.err.println("Failed to process record.\n" + e.toString());
                }
            }
        }
        System.out.printf("Processed %d pages\n", processedPages);

        if (out != null)
            out.close();
        if (bulkIndexer != null)
            bulkIndexer.close();
        if (kafkaProducer != null)
            kafkaProducer.close();
        
        System.out.println("done.");
    }

    private KafkaProducer<String, String> initializeKafkaProducer() throws IOException {
        if (this.kafkaProperties == null || this.kafkaProperties.isEmpty()) {
            return null;
        }
        if (this.kafkaTopicName == null || this.kafkaTopicName.isEmpty()) {
            throw new IllegalArgumentException(
                    "Kafka topic name can't be empty: " + this.kafkaTopicName);
        }
        Properties properties = new Properties();
        properties.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        properties.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        properties.load(new FileInputStream(this.kafkaProperties));
        return new KafkaProducer<>(properties);
    }

    private SimpleBulkIndexer initializeElasticsearchIndexer() {
        if (elasticSearchServer != null) {
            if (this.outputIndex == null || this.outputIndex.isEmpty())
                throw new IllegalArgumentException(
                        "Argument for Elasticsearch index can't be empty");
            if (this.outputType == null || this.outputType.isEmpty())
                throw new IllegalArgumentException(
                        "Argument for Elasticsearch type can't be empty");
            return new SimpleBulkIndexer(elasticSearchServer, userPass, bulkSize);
        }
        return null;
    }

    private CloseableIterator<Page> createIterator() throws IOException {
        switch (repositoryType) {
            case FILESYSTEM_JSON:
                return new FileSystemTargetRepository(inputPath, DataFormat.JSON,
                        hashFilename, compressData).pagesIterator();
            case WARC:
                return new WarcTargetRepository(inputPath).pagesIterator();
            case FILES:
                return new FilesTargetRepository(inputPath).pagesIterator();
            default:
                throw new IllegalArgumentException("Unsuported data format: " + repositoryType);
        }
    }

    private void processMediaFile(Page pageModel) throws IOException {
        // What if contentType is empty but the object is an image.
        //
        String contentType = pageModel.getContentType();

        if (contentType == null || contentType.isEmpty()) {
            System.err.println("Ignoring URL with no content-type: " + pageModel.getFinalUrl());
            return;
        }

        if (!contentType.startsWith("image")) {
            return;
        }

        if (cdrVersion != CDRVersion.CDRv31) {
            return;
        }

        createCDR31MediaObject(pageModel);
    }

    private void processRecord(Page page) throws IOException {
        String contentType = page.getContentType();

        if (contentType == null || contentType.isEmpty()) {
            System.err.println("Ignoring URL with no content-type: " + page.getFinalUrl());
            return;
        }

        if (!contentType.startsWith("text/html")) {
            return;
        }

        if (skipRelevant && page.getTargetRelevance().isRelevant()) {
            return;
        }

        if (skipIrrelevant && !page.getTargetRelevance().isRelevant()) {
            return;
        }

        if (cdrVersion == CDRVersion.CDRv31) {
            createCDR31DocumentJson(page);
        } else if (cdrVersion == CDRVersion.CDRv2) {
            createCDR2DocumentJson(page);
        } else {
            createCDR3DocumentJson(page);
        }

        String docAsCdrJson = jsonMapper.writeValueAsString(doc);

        if (doc != null && out != null) {
            out.println(docAsCdrJson);
        }

        if (bulkIndexer != null) {
            bulkIndexer.addDocument(outputIndex, outputType, docAsCdrJson, id);
        }

        if (kafkaProducer != null) {
            ProducerRecord<String, String> record =
                    new ProducerRecord<>(this.kafkaTopicName, id, docAsCdrJson);
            try {
                kafkaProducer.send(record);
            } catch (Exception e) {
                System.err.println("Failed to deliver document to Kafka: " + page.getFinalUrl());
                e.printStackTrace();
            }
        }
    }

    public void createCDR2DocumentJson(Page page) {
        HashMap<String, Object> crawlData = new HashMap<>();
        crawlData.put("response_headers", page.getResponseHeaders());

        CDR2Document.Builder builder = new CDR2Document.Builder()
                .setUrl(page.getFinalUrl())
                .setTimestamp(page.getFetchTime())
                .setContentType(page.getContentType())
                .setVersion("2.0")
                .setTeam("NYU")
                .setCrawler("ACHE")
                .setRawContent(page.getContentAsString())
                .setCrawlData(crawlData);

        CDR2Document doc = builder.build();
        this.id = doc.getId();
        this.doc = doc;
    }

    public void createCDR3DocumentJson(Page page) {
        HashMap<String, Object> crawlData = new HashMap<>();
        crawlData.put("response_headers", page.getResponseHeaders());

        CDR3Document.Builder builder = new CDR3Document.Builder()
                .setUrl(page.getFinalUrl())
                .setTimestampCrawl(new Date(page.getFetchTime()))
                .setTimestampIndex(new Date())
                .setContentType(page.getContentType())
                .setTeam("NYU")
                .setCrawler("ACHE")
                .setRawContent(page.getContentAsString());

        CDR3Document doc = builder.build();
        this.id = doc.getId();
        this.doc = doc;
    }

    public void createCDR31MediaObject(Page page) throws IOException {
        // Hash and upload to S3
        String storedUrl = this.uploadMediaFile(page.getContent(), page.getFinalUrl());

        // Create Media Object for the image
        CDR31MediaObject obj = new CDR31MediaObject();
        obj.setContentType(page.getContentType());
        obj.setTimestampCrawl(new Date(page.getFetchTime()));
        obj.setObjOriginalUrl(page.getFinalUrl());
        obj.setObjStoredUrl(storedUrl);
        obj.setResponseHeaders(page.getResponseHeaders());

        // Save it for including into the HTML pages later
        this.mediaObjectCache.put(page.getFinalUrl(), obj);
    }

    private String uploadMediaFile(byte[] content, String url) throws IOException {
        HashFunction hf = Hashing.sha256();
        Hasher hasher = hf.newHasher();
        hasher.putBytes(content);
        String host = new URL(url).getHost();
        String hs = reverseDomain(host) + "/" + hasher.hash().toString();
        if (skipUpload == false) {
            this.s3Uploader.upload(hs, content);
            System.out.println("Uploaded object: " + hs);
        } else {
            System.out.println("Created object: " + hs);
        }
        return hs;
    }

//    public String[] extractImgLinks(TargetModelJson pageModel) {
//        try {
//            PaginaURL pageParser = new PaginaURL(new Page(pageModel));
//            URL[] parsedLinks = pageParser.links();
//            HashSet<String> links = new HashSet<>();
//            for (URL url : parsedLinks) {
//                links.add(url.toString());
//            }
//            return links.toArray(new String[links.size()]);
//        } catch (MalformedURLException e) {
//            return new String[0];
//        }
//    }

    public String[] extractImgLinks(Page page) {
        HashSet<String> links = new HashSet<>();
        Document doc = Jsoup.parse(page.getContentAsString());
        Elements media = doc.select("[src]");
        for (Element src : media) {
            if (src.tagName().equals("img")) {
                links.add(src.attr("abs:src"));
            }
        }
        return links.toArray(new String[links.size()]);
    }

    public void createCDR31DocumentJson(Page page) {

        List<CDR31MediaObject> mediaObjects = new ArrayList<>();
        if (!skipUpload) {
            String[] imgLinks = extractImgLinks(page);
            for (String link : imgLinks) {
                CDR31MediaObject object = this.mediaObjectCache.get(link);
                if (object != null) {
                    mediaObjects.add(object);
                }
            }
        }

        CDR31Document.Builder builder = new CDR31Document.Builder()
                .setUrl(page.getFinalUrl())
                .setTimestampCrawl(new Date(page.getFetchTime()))
                .setTimestampIndex(new Date())
                .setContentType(page.getContentType())
                .setResponseHeaders(page.getResponseHeaders())
                .setRawContent(page.getContentAsString())
                .setObjects(mediaObjects)
                .setTeam("NYU")
                .setCrawler("ACHE");

        CDR31Document doc = builder.build();
        this.id = doc.getId();
        this.doc = doc;
    }

    private String reverseDomain(String domain) {
        if (domain == null || domain.isEmpty()) {
            return null;
        }
        String[] hostParts = domain.split("\\.");
        if (hostParts.length == 0) {
            return null;
        }
        StringBuilder reverseDomain = new StringBuilder();
        reverseDomain.append(hostParts[hostParts.length - 1]);
        for (int i = hostParts.length - 2; i >= 0; i--) {
            reverseDomain.append('/');
            reverseDomain.append(hostParts[i]);
        }
        return reverseDomain.toString();
    }

}
