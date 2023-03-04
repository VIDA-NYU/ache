package achecrawler.config;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.jupiter.api.Assertions.assertEquals;

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

class ConfigurationTest {

    String configFilePath = ConfigurationTest.class.getResource("ache.yml").getPath();

    @Test
    void shouldReadTargeStorageConfig() throws IOException {
        // given
        Configuration configService = new Configuration(configFilePath);

        // when
        TargetStorageConfig config = configService.getTargetStorageConfig();

        // then
        assertThat(config, is(notNullValue()));

        assertThat(config.isSaveNegativePages(), is(false));
        assertThat(config.getVisitedPageLimit(), is(12345));
        assertThat(config.isEnglishLanguageDetectionEnabled(), is(false));
        assertThat(config.isHardFocus(), is(false));
        assertThat(config.isBipartite(), is(true));

        assertThat(config.getDataFormats(), contains("ELASTICSEARCH"));
        assertThat(config.getElasticSearchConfig(), is(notNullValue()));
        assertThat(config.getElasticSearchConfig().getRestApiHosts().get(0), is("http://node01:9201"));
    }

    @Test
    void shouldReadLinkStorageConfig() throws IOException {
        // given
        Configuration configService = new Configuration(configFilePath);

        // when
        LinkStorageConfig config = configService.getLinkStorageConfig();

        // then
        assertThat(config, is(notNullValue()));

        assertThat(config.getMaxPagesPerDomain(), is(222));

        assertThat(config.getOutlinks(), is(false));
        assertThat(config.getBacklinks(), is(true));
        assertThat(config.isUseScope(), is(true));

        assertThat(config.getTypeOfClassifier(), is("LinkClassifierImpl"));
        // TODO: add parameters for link classifier

        assertThat(config.isUseOnlineLearning(), is(false));
        assertThat(config.getOnlineMethod(), is("FORWARD_CLASSIFIER_BINARY"));
        assertThat(config.getLearningLimit(), is(555));

        assertThat(config.getLinkSelector(), is("TopkLinkSelector"));

        assertThat(config.getMaxCacheUrlsSize(), is(222222));

        assertThat(config.getSchedulerHostMinAccessInterval(), is(123));
        assertThat(config.getSchedulerMaxLinks(), is(234));
    }

    @Test
    void shouldReadCrawlerConfig() throws IOException {
        // given
        Configuration configService = new Configuration(configFilePath);

        // when
        AsyncCrawlerConfig config = configService.getCrawlerConfig();

        // then
        assertThat(config, is(notNullValue()));

        assertThat(config.getDownloaderConfig().getDownloadThreadPoolSize(), is(333));
        assertThat(config.getDownloaderConfig().getMaxRetryCount(), is(444));
        assertThat(config.getDownloaderConfig().getUserAgentConfig().name, is("TestAgent"));
        assertThat(config.getDownloaderConfig().getUserAgentConfig().url, is("http://www.test-agent-crawler-example.com/robot"));
        assertThat(config.getDownloaderConfig().getValidMimeTypes()[0], is("test/mimetype"));
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
        assertThat(baseDownloaderConfig.getDownloadThreadPoolSize(), is(333));
        assertThat(baseDownloaderConfig.getMaxRetryCount(), is(444));
        assertThat(baseDownloaderConfig.getUserAgentConfig().name, is("TestAgent"));
        assertThat(baseDownloaderConfig.getUserAgentConfig().url, is("http://www.test-agent-crawler-example.com/robot"));

        assertThat(newConfig, is(notNullValue()));
        HttpDownloaderConfig newDownloaderConfig = newConfig.getCrawlerConfig().getDownloaderConfig();
        assertThat(newDownloaderConfig.getDownloadThreadPoolSize(), is(333));
        assertThat(newDownloaderConfig.getMaxRetryCount(), is(444));
        assertThat(newDownloaderConfig.getUserAgentConfig().name, is("NewAgent"));
        assertThat(newDownloaderConfig.getUserAgentConfig().url, is("http://www.test-agent-crawler-example.com/robot"));

        assertEquals(baseConfig.getTargetStorageConfig().getDataFormats(),
                     newConfig.getTargetStorageConfig().getDataFormats());

        assertEquals(baseConfig.getLinkStorageConfig().getDownloadSitemapXml(),
                     newConfig.getLinkStorageConfig().getDownloadSitemapXml());
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
        assertEquals(baseConfig.getTargetStorageConfig().getDataFormats(),
                     newConfig.getTargetStorageConfig().getDataFormats());
        assertEquals(mapper.writeValueAsString(baseConfig), mapper.writeValueAsString(newConfig));
    }

}
