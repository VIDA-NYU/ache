package focusedCrawler.config;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;

import java.io.IOException;

import org.junit.Test;

import focusedCrawler.crawler.async.AsyncCrawlerConfig;
import focusedCrawler.link.LinkStorageConfig;
import focusedCrawler.target.TargetStorageConfig;

public class ConfigurationTest {

    String configFilePath = ConfigurationTest.class.getResource("ache.yml").getPath();

    @Test
    public void shouldReadTargeStorageConfig() throws IOException {
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
        assertThat(config.getElasticSearchConfig().getHost(), is("elasticsearch.localhost"));
        assertThat(config.getElasticSearchConfig().getPort(), is(9999));
        assertThat(config.getElasticSearchConfig().getClusterName(), is("elasticsearch-test"));

        assertThat(config.getStorageServerConfig(), is(notNullValue()));
        assertThat(config.getStorageServerConfig().getHost(), is("targetstorage.localhost"));
        assertThat(config.getStorageServerConfig().getPort(), is(19876));
    }

    @Test
    public void shouldReadLinkStorageConfig() throws IOException {
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

        assertThat(config.getStorageServerConfig(), is(notNullValue()));
        assertThat(config.getStorageServerConfig().getHost(), is("linkstorage.localhost"));
        assertThat(config.getStorageServerConfig().getPort(), is(19888));

        assertThat(config.getSchedulerHostMinAccessInterval(), is(123));
        assertThat(config.getSchedulerMaxLinks(), is(234));
    }

    @Test
    public void shouldReadCrawlerConfig() throws IOException {
        // given
        Configuration configService = new Configuration(configFilePath);

        // when
        AsyncCrawlerConfig config = configService.getCrawlerConfig();

        // then
        assertThat(config, is(notNullValue()));

        assertThat(config.getDownloaderConfig().getDownloadThreadPoolSize(), is(333));
        assertThat(config.getDownloaderConfig().getMaxRetryCount(), is(444));
        assertThat(config.getDownloaderConfig().getUserAgentName(), is("TestAgent"));
        assertThat(config.getDownloaderConfig().getUserAgentUrl(), is("http://www.test-agent-crawler-example.com/robot"));
        assertThat(config.getDownloaderConfig().getValidMimeTypes()[0], is("test/mimetype"));
    }

}
