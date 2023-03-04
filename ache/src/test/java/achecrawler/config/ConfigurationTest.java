package achecrawler.config;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.junit.jupiter.api.Test;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.google.common.collect.ImmutableMap;

import achecrawler.crawler.async.AsyncCrawlerConfig;
import achecrawler.crawler.async.HttpDownloaderConfig;
import achecrawler.link.LinkStorageConfig;
import achecrawler.target.TargetStorageConfig;

import static org.assertj.core.api.Assertions.assertThat;

class ConfigurationTest {

    String configFilePath = ConfigurationTest.class.getResource("ache.yml").getPath();

    @Test
    void shouldReadTargeStorageConfig() throws IOException {
        // given
        Configuration configService = new Configuration(configFilePath);

        // when
        TargetStorageConfig config = configService.getTargetStorageConfig();

        // then
        assertThat(config).isNotNull();

        assertThat(config.isSaveNegativePages()).isFalse();
        assertThat(config.getVisitedPageLimit()).isEqualTo(12345);
        assertThat(config.isEnglishLanguageDetectionEnabled()).isFalse();
        assertThat(config.isHardFocus()).isFalse();
        assertThat(config.isBipartite()).isTrue();

        assertThat(config.getDataFormats()).containsExactly("ELASTICSEARCH");
        assertThat(config.getElasticSearchConfig()).isNotNull();
        assertThat(config.getElasticSearchConfig().getRestApiHosts().get(0)).isEqualTo("http://node01:9201");
    }

    @Test
    void shouldReadLinkStorageConfig() throws IOException {
        // given
        Configuration configService = new Configuration(configFilePath);

        // when
        LinkStorageConfig config = configService.getLinkStorageConfig();

        // then
        assertThat(config).isNotNull();

        assertThat(config.getMaxPagesPerDomain()).isEqualTo(222);

        assertThat(config.getOutlinks()).isFalse();
        assertThat(config.getBacklinks()).isTrue();
        assertThat(config.isUseScope()).isTrue();

        assertThat(config.getTypeOfClassifier()).isEqualTo("LinkClassifierImpl");
        // TODO: add parameters for link classifier

        assertThat(config.isUseOnlineLearning()).isFalse();
        assertThat(config.getOnlineMethod()).isEqualTo("FORWARD_CLASSIFIER_BINARY");
        assertThat(config.getLearningLimit()).isEqualTo(555);

        assertThat(config.getLinkSelector()).isEqualTo("TopkLinkSelector");

        assertThat(config.getMaxCacheUrlsSize()).isEqualTo(222222);

        assertThat(config.getSchedulerHostMinAccessInterval()).isEqualTo(123);
        assertThat(config.getSchedulerMaxLinks()).isEqualTo(234);
    }

    @Test
    void shouldReadCrawlerConfig() throws IOException {
        // given
        Configuration configService = new Configuration(configFilePath);

        // when
        AsyncCrawlerConfig config = configService.getCrawlerConfig();

        // then
        assertThat(config).isNotNull();

        assertThat(config.getDownloaderConfig().getDownloadThreadPoolSize()).isEqualTo(333);
        assertThat(config.getDownloaderConfig().getMaxRetryCount()).isEqualTo(444);
        assertThat(config.getDownloaderConfig().getUserAgentConfig().name).isEqualTo("TestAgent");
        assertThat(config.getDownloaderConfig().getUserAgentConfig().url).isEqualTo("http://www.test-agent-crawler-example.com/robot");
        assertThat(config.getDownloaderConfig().getValidMimeTypes()[0]).isEqualTo("test/mimetype");
    }

    @Test
    void shouldCopyAndUpdateConfig() throws IOException {
        // given
        Configuration baseConfig = new Configuration(configFilePath);

        String params = "crawler_manager.downloader.user_agent.name: NewAgent";
        InputStream is = new ByteArrayInputStream(params.getBytes(StandardCharsets.UTF_8));

        // when
        Configuration newConfig = baseConfig.copyUpdating(is);

        // then
        HttpDownloaderConfig baseDownloaderConfig = baseConfig.getCrawlerConfig().getDownloaderConfig();
        assertThat(baseDownloaderConfig.getDownloadThreadPoolSize()).isEqualTo(333);
        assertThat(baseDownloaderConfig.getMaxRetryCount()).isEqualTo(444);
        assertThat(baseDownloaderConfig.getUserAgentConfig().name).isEqualTo("TestAgent");
        assertThat(baseDownloaderConfig.getUserAgentConfig().url).isEqualTo("http://www.test-agent-crawler-example.com/robot");

        assertThat(newConfig).isNotNull();
        HttpDownloaderConfig newDownloaderConfig = newConfig.getCrawlerConfig().getDownloaderConfig();
        assertThat(newDownloaderConfig.getDownloadThreadPoolSize()).isEqualTo(333);
        assertThat(newDownloaderConfig.getMaxRetryCount()).isEqualTo(444);
        assertThat(newDownloaderConfig.getUserAgentConfig().name).isEqualTo("NewAgent");
        assertThat(newDownloaderConfig.getUserAgentConfig().url).isEqualTo("http://www.test-agent-crawler-example.com/robot");

        assertThat(newConfig.getTargetStorageConfig().getDataFormats()).isEqualTo(baseConfig.getTargetStorageConfig().getDataFormats());

        assertThat(newConfig.getLinkStorageConfig().getDownloadSitemapXml()).isEqualTo(baseConfig.getLinkStorageConfig().getDownloadSitemapXml());
    }

    @Test
    void shouldNotChangeAfterSerialized() throws IOException {
        // given
        Map<?, ?> settings = ImmutableMap.of("target_storage.data_format.type", "ELASTICSEARCH");
        Configuration baseConfig = new Configuration(settings);
        ObjectMapper mapper = new ObjectMapper(new YAMLFactory());

        // when
        Configuration newConfig = baseConfig.copy();
        // then
        assertThat(newConfig.getTargetStorageConfig().getDataFormats()).isEqualTo(baseConfig.getTargetStorageConfig().getDataFormats());
        assertThat(mapper.writeValueAsString(newConfig)).isEqualTo(mapper.writeValueAsString(baseConfig));
    }

}
