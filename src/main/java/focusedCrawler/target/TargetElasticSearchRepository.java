package focusedCrawler.target;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Date;

import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.ImmutableSettings;
import org.elasticsearch.common.settings.ImmutableSettings.Builder;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.node.Node;
import org.elasticsearch.node.NodeBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.google.common.net.InternetDomainName;

import de.l3s.boilerpipe.BoilerpipeProcessingException;
import de.l3s.boilerpipe.extractors.DefaultExtractor;
import focusedCrawler.config.TargetStorageConfig;
import focusedCrawler.util.Page;
import focusedCrawler.util.Target;
import focusedCrawler.util.parser.PaginaURL;

public class TargetElasticSearchRepository implements TargetRepository {

    public static class ElasticSearchClientFactory {

        private static final Logger logger = LoggerFactory
                .getLogger(TargetElasticSearchRepository.ElasticSearchClientFactory.class);

        private static Node clientNode;
        private static Client client;

        @SuppressWarnings("resource")
        public static Client createClient(TargetStorageConfig config) {

            if (client != null) {
                return client;
            }

            String elasticSearchHost = config.getElasticSearchHost();
            int elasticSearchPort = config.getElasticSearchPort();
            String clusterName = config.getClusterName();

            Builder settingsBuilder = ImmutableSettings.settingsBuilder();
            if (clusterName != null) {
                settingsBuilder.put("cluster.name", clusterName);
            }
            Settings settings = settingsBuilder.build();

            if (elasticSearchHost != null) {
                logger.info("Creating a ElasticSearch TransportClient for address: {}:{}",
                        elasticSearchHost, elasticSearchPort);
                int port = elasticSearchPort != 0 ? elasticSearchPort : 9300;
                InetSocketTransportAddress socketAddress = new InetSocketTransportAddress(
                        elasticSearchHost, port);
                client = new TransportClient(settings).addTransportAddress(socketAddress);
                return client;
            } else {
                logger.info("Creating a ElasticSearch Node Client for address: %s:%s",
                        elasticSearchHost, elasticSearchPort);
                clientNode = NodeBuilder.nodeBuilder().client(true).settings(settings).node();
                client = clientNode.client();
                return client;
            }
        }

        public void closeClient() {
            if (client != null) {
                client.close();
            }
            if (clientNode != null) {
                clientNode.close();
            }
        }

    }

    public static class ElasticSearchPageModel {

        private String domain;
        private String url;
        private String title;
        private String text;
        private Date retrieved;
        private String[] words;
        private String[] wordsMeta;
        private String topPrivateDomain;

        public ElasticSearchPageModel(Target target) {
            Page page = (Page) target;

            this.url = target.getIdentifier();
            this.retrieved = new Date();
            this.words = page.getPageURL().palavras();
            this.wordsMeta = page.getPageURL().palavrasMeta();
            this.title = page.getPageURL().titulo();
            this.domain = page.getDomainName();

            try {
                this.text = DefaultExtractor.getInstance().getText(page.getContent());
            } catch (BoilerpipeProcessingException e) {
                this.text = "";
            }

            InternetDomainName domainName = InternetDomainName.from(page.getDomainName());
            if (domainName.isUnderPublicSuffix()) {
                this.topPrivateDomain = domainName.topPrivateDomain().toString();
            } else {
                this.topPrivateDomain = domainName.toString();
            }
        }

        public ElasticSearchPageModel(TargetModel model) {
            this.url = model.url;
            this.retrieved = new Date(model.timestamp * 1000);

            URL url;
            try {
                url = new URL(model.url);
            } catch (MalformedURLException e) {
                throw new IllegalArgumentException("page has an invalid URL: " + model.url);
            }

            String raw_content = (String) model.response.get("body");
            Page page = new Page(url, raw_content);
            PaginaURL pageURL = new PaginaURL(url, 0, 0, raw_content.length(), raw_content, null);
            PaginaURL pageParser = new PaginaURL(page.getURL(), 0, 0, page.getContent().length(),
                    page.getContent(), null);
            page.setPageURL(pageParser);

            this.url = model.url;
            this.retrieved = new Date();
            this.words = pageURL.palavras();
            this.wordsMeta = pageURL.palavrasMeta();
            this.title = pageURL.titulo();
            this.domain = url.getHost();

            try {
                this.text = DefaultExtractor.getInstance().getText(page.getContent());
            } catch (Exception e) {
                this.text = "";
            }

            InternetDomainName domainName = InternetDomainName.from(page.getDomainName());
            if (domainName.isUnderPublicSuffix()) {
                this.topPrivateDomain = domainName.topPrivateDomain().toString();
            } else {
                this.topPrivateDomain = domainName.toString();
            }
        }

        public String getDomain() {
            return domain;
        }

        public void setDomain(String domain) {
            this.domain = domain;
        }

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }

        public String getText() {
            return text;
        }

        public void setText(String text) {
            this.text = text;
        }

        public Date getRetrieved() {
            return retrieved;
        }

        public void setRetrieved(Date retrieved) {
            this.retrieved = retrieved;
        }

        public String[] getWords() {
            return words;
        }

        public void setWords(String[] words) {
            this.words = words;
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public String[] getWordsMeta() {
            return wordsMeta;
        }

        public void setWordsMeta(String[] wordsMeta) {
            this.wordsMeta = wordsMeta;
        }

        public String getTopPrivateDomain() {
            return topPrivateDomain;
        }

        public void setTopPrivateDomain(String topPrivateDomain) {
            this.topPrivateDomain = topPrivateDomain;
        }

    }

    private static final ObjectMapper mapper = new ObjectMapper();
    static {
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }

    private Client client;
    private String typeName;
    private String indexName;
    
    public TargetElasticSearchRepository(TargetStorageConfig config,
                                         String indexName,
                                         String typeName) {
        this.client = ElasticSearchClientFactory.createClient(config);
        this.indexName = indexName;
        this.typeName = typeName;
        this.createIndexMapping(indexName);
    }

    private void createIndexMapping(String indexName) {
        
        boolean exists = client.admin().indices().prepareExists(indexName)
                .execute().actionGet().isExists();
        
        if(!exists) {
            String targetMapping = "{\"properties\": {"
                + "\"domain\": {\"type\": \"string\",\"index\": \"not_analyzed\"},"
                + "\"words\": {\"type\": \"string\",\"index\": \"not_analyzed\"},"
                + "\"wordsMeta\": {\"type\": \"string\",\"index\": \"not_analyzed\"},"
                + "\"retrieved\": {\"format\": \"dateOptionalTime\",\"type\": \"date\"},"
                + "\"text\": {\"type\": \"string\"},\"title\": {\"type\": \"string\"},"
                + "\"url\": {\"type\": \"string\",\"index\": \"not_analyzed\"},"
                + "\"topPrivateDomain\": {\"type\": \"string\",\"index\": \"not_analyzed\"}"
                + "}}";
            
            client.admin().indices().prepareCreate(indexName)
                .addMapping("target", targetMapping)
                .addMapping("negative", targetMapping)
                .execute()
                .actionGet();
        }
    }

    public boolean insert(Target target, int counter) {
        return index(target);
    }

    public boolean insert(Target target) {
        return index(target);
    }

    private boolean index(Target target) {
        // This contact information should be read from config file
        Object data;
        boolean memexDataFormat = false;

        if (memexDataFormat) {
            TargetModel targetModel = new TargetModel("Kien Pham", "kien.pham@nyu.edu");
            targetModel.resetTimestamp();
            targetModel.setUrl(target.getIdentifier());
            targetModel.setContent(target.getSource());
            data = targetModel;
        } else {
            data = new ElasticSearchPageModel(target);
        }

        String docId = target.getIdentifier();
        IndexResponse response = client.prepareIndex(indexName, typeName, docId)
                .setSource(serializeAsJson(data))
                .execute()
                .actionGet();

        return response.isCreated();
    }

    private String serializeAsJson(Object model) {
        String targetAsJson;
        try {
            targetAsJson = mapper.writeValueAsString(model);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to serialize TargetModel to JSON.", e);
        }
        return targetAsJson;
    }

}
