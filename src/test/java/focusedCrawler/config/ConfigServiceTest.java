package focusedCrawler.config;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;

import java.io.IOException;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import focusedCrawler.crawler.CrawlerManagerConfig;
import focusedCrawler.link.LinkStorageConfig;

public class ConfigServiceTest {

    String configFilePath = ConfigServiceTest.class.getResource("ache.yml").getPath();
    
    @Before
    public void setUp() throws Exception {
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void shouldReadTargeStorageConfig() throws IOException {
        // given
        ConfigService configService = new ConfigService(configFilePath);
        
        // when
        TargetStorageConfig config = configService.getTargetStorageConfig();
        
        // then
        assertThat(config, is(notNullValue()));
        
        assertThat(config.isUseClassifier(), is(false));
        assertThat(config.isSaveNegativePages(), is(false));
        assertThat(config.getVisitedPageLimit(), is(12345));
        assertThat(config.isEnglishLanguageDetectionEnabled(), is(false));
        assertThat(config.getRelevanceThreshold(), is(0.6f));
        assertThat(config.isHardFocus(), is(false));
        assertThat(config.isBipartite(), is(true));
        
        assertThat(config.isRefreshSync(), is(false));
        assertThat(config.getCrawledRefreshFrequency(), is(123));
        assertThat(config.getRelevantRefreshFrequency(), is(234));
        assertThat(config.getHarvestInfoRefreshFrequency(), is(345));
        
        assertThat(config.getDataFormat(), is("ELASTICSEARCH"));
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
        ConfigService configService = new ConfigService(configFilePath);
        
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
        
        assertThat(config.getMaxSizeLinkQueue(), is(111111));
        assertThat(config.getMaxCacheUrlsSize(), is(222222));
        
        assertThat(config.getStorageServerConfig(), is(notNullValue()));
        assertThat(config.getStorageServerConfig().getHost(), is("linkstorage.localhost"));
        assertThat(config.getStorageServerConfig().getPort(), is(19888));
    }
    
    @Test
    public void shouldReadCrawlerManagerConfig() throws IOException {
        // given
        ConfigService configService = new ConfigService(configFilePath);
        
        // when
        CrawlerManagerConfig config = configService.getCrawlerManagerConfig();
        
        // then
        assertThat(config, is(notNullValue()));
        
        assertThat(config.getRobotThreadGroup(), is("crawler_group_test"));
        assertThat(config.getRobotManagerRestingTime(), is(11111L));
        assertThat(config.getRobotManagerSleepCheckTime(), is(11111L));
        assertThat(config.getRobotManagerMaxTime(), is(11111L));
        
        assertThat(config.getRobotManagerRobotErrorTime(), is(222222L));
        assertThat(config.getRobotManagerRobotThreadFactor(), is(222222));
        assertThat(config.getRobotQuantity(), is(222222));
        assertThat(config.getDownloaderMaxBlockedThreads(), is(222222L));
        
    }

}
