package achecrawler.link.frontier;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Map;
import java.util.Random;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import com.google.common.collect.ImmutableMap;

import achecrawler.config.Configuration;
import achecrawler.link.LinkStorageConfig;
import achecrawler.link.frontier.selector.LinkSelector;
import achecrawler.link.frontier.selector.RandomLinkSelector;
import achecrawler.link.frontier.selector.SitemapsRecrawlSelector;
import achecrawler.link.frontier.selector.TopkLinkSelector;
import achecrawler.util.DataNotFoundException;
import achecrawler.util.LinkFilter;
import achecrawler.util.MetricsManager;
import achecrawler.util.persistence.PersistentHashtable.DB;

public class FrontierManagerTest {

    @TempDir
    public File tempFolder;
    
    private final LinkFilter emptyLinkFilter = new LinkFilter.Builder().build();
    private final MetricsManager metricsManager = new MetricsManager();
    private LinkStorageConfig config = new LinkStorageConfig();
    private Frontier frontier;
    private String dataPath;
    private String modelPath;

    private final int minimumAccessTimeInterval = 0;
    private final int schedulerMaxLinks = 2;
    private final boolean downloadSitemapXml = false;

    @BeforeEach
    void setUp() throws Exception {
        frontier = new Frontier(newSubFolder(tempFolder).toString(), 1000, DB.ROCKSDB);
        dataPath = newSubFolder(tempFolder).toString();
        modelPath = newSubFolder(tempFolder).toString();
        Map<?, ?> props = ImmutableMap.of(
            "link_storage.scheduler.max_links", schedulerMaxLinks,
            "link_storage.scheduler.host_min_access_interval", minimumAccessTimeInterval,
            "link_storage.download_sitemap_xml", downloadSitemapXml
        );
        config = new Configuration(props).getLinkStorageConfig();
    }

    @Test
    void shouldNotInsertLinkOutOfScope() throws Exception {
        // given
        LinkRelevance link1 = new LinkRelevance(new URL("http://www.example1.com/index.html"), 1);
        LinkRelevance link2 = new LinkRelevance(new URL("http://www.example2.com/index.html"), 2);

        LinkSelector linkSelector = new RandomLinkSelector();
        Frontier frontier = new Frontier(newSubFolder(tempFolder).toString(), 1000, DB.ROCKSDB);

        Map<?, ?> props = ImmutableMap.of(
            "link_storage.scheduler.max_links", schedulerMaxLinks,
            "link_storage.scheduler.host_min_access_interval", minimumAccessTimeInterval,
            "link_storage.download_sitemap_xml", downloadSitemapXml,
            "link_storage.link_strategy.use_scope", true
        );
        LinkStorageConfig config = new Configuration(props).getLinkStorageConfig();
        FrontierManager frontierManager = new FrontierManager(frontier, dataPath, modelPath, config,
                linkSelector, null, emptyLinkFilter, metricsManager);

        // when
        frontierManager.addSeedScope(link1);
        frontierManager.insert(link1);
        frontierManager.insert(link2);

        LinkRelevance selectedLink1 = frontierManager.nextURL();
        DataNotFoundException notFoundException = null;
        try {
            frontierManager.nextURL();
        } catch (DataNotFoundException e) {
            notFoundException = e;
        }

        // then
        assertThat(selectedLink1).isNotNull();
        assertThat(selectedLink1.getURL()).isNotNull();
        assertThat(selectedLink1.getURL()).isEqualTo(link1.getURL());

        assertThat(notFoundException).isNotNull();
        assertThat(notFoundException.ranOutOfLinks()).isTrue();
        frontierManager.close();
    }

    @Test
    void shouldRememberScopeOnRestart() throws Exception {
        // given
        LinkRelevance link1 = new LinkRelevance(new URL("http://www.example1.com/index.html"), 1);
        LinkRelevance link2 = new LinkRelevance(new URL("http://www.example2.com/index.html"), 2);

        LinkSelector linkSelector = new RandomLinkSelector();
        String folder = newSubFolder(tempFolder).toString();
        Frontier frontier = new Frontier(folder, 1000, DB.ROCKSDB);

        Map<?, ?> props = ImmutableMap.of(
            "link_storage.scheduler.max_links", schedulerMaxLinks,
            "link_storage.scheduler.host_min_access_interval", minimumAccessTimeInterval,
            "link_storage.download_sitemap_xml", downloadSitemapXml,
            "link_storage.link_strategy.use_scope", true
        );
        LinkStorageConfig config = new Configuration(props).getLinkStorageConfig();
        FrontierManager frontierManager = new FrontierManager(frontier, dataPath, modelPath, config,
                linkSelector, null, emptyLinkFilter, metricsManager);

        // when
        frontierManager.addSeedScope(link1);
        frontierManager.close();
        frontier.close();

        frontier = new Frontier(folder, 1000, DB.ROCKSDB);
        frontierManager = new FrontierManager(frontier, dataPath, modelPath, config,
                linkSelector, null, emptyLinkFilter, new MetricsManager());

        frontierManager.insert(link1);
        frontierManager.insert(link2);

        LinkRelevance selectedLink1 = frontierManager.nextURL();
        DataNotFoundException notFoundException = null;
        try {
            frontierManager.nextURL();
        } catch (DataNotFoundException e) {
            notFoundException = e;
        }

        // then
        assertThat(selectedLink1).isNotNull();
        assertThat(selectedLink1.getURL()).isNotNull();
        assertThat(selectedLink1.getURL()).isEqualTo(link1.getURL());

        assertThat(notFoundException).isNotNull();
        assertThat(notFoundException.ranOutOfLinks()).isTrue();
        frontierManager.close();
    }


    @Test
    void shouldModifyScopeAfterAddingNewSeeds() throws Exception {
        // given
        LinkRelevance link1 = new LinkRelevance(new URL("http://www.example1.com/index.html"), 1);

        LinkRelevance link2_1 = new LinkRelevance(new URL("http://www.example2.com/index.html"), 2);
        LinkRelevance link2_2 = new LinkRelevance(new URL("http://www.example2.com/about.html"), 2);

        LinkSelector linkSelector = new RandomLinkSelector();
        Frontier frontier = new Frontier(newSubFolder(tempFolder).toString(), 1000, DB.ROCKSDB);

        Map<?, ?> props = ImmutableMap.of(
            "link_storage.scheduler.max_links", schedulerMaxLinks,
            "link_storage.scheduler.host_min_access_interval", minimumAccessTimeInterval,
            "link_storage.download_sitemap_xml", downloadSitemapXml,
            "link_storage.link_strategy.use_scope", true
        );
        LinkStorageConfig config = new Configuration(props).getLinkStorageConfig();
        FrontierManager frontierManager = new FrontierManager(frontier, dataPath, modelPath, config,
                linkSelector, null, emptyLinkFilter, metricsManager);

        // when
        frontierManager.addSeedScope(link1);

        frontierManager.insert(link1);
        frontierManager.insert(link2_1);

        LinkRelevance selectedLink1 = frontierManager.nextURL();
        DataNotFoundException notFoundException = null;
        try {
            frontierManager.nextURL();
        } catch (DataNotFoundException e) {
            notFoundException = e;
        }

        frontierManager.addSeeds(asList(link2_1.getURL().toString()));
        LinkRelevance selectedLink2 = frontierManager.nextURL();

        frontierManager.insert(link2_2);
        LinkRelevance selectedLink2_2 = frontierManager.nextURL();

        // then
        assertThat(selectedLink1).isNotNull();
        assertThat(selectedLink1.getURL()).isNotNull();
        assertThat(selectedLink1.getURL()).isEqualTo(link1.getURL());

        assertThat(notFoundException).isNotNull();
        assertThat(notFoundException.ranOutOfLinks()).isTrue();

        assertThat(selectedLink2).isNotNull();
        assertThat(selectedLink2.getURL()).isNotNull();
        assertThat(selectedLink2.getURL()).isEqualTo(link2_1.getURL());

        assertThat(selectedLink2_2).isNotNull();
        assertThat(selectedLink2_2.getURL()).isNotNull();
        assertThat(selectedLink2_2.getURL()).isEqualTo(link2_2.getURL());

        frontierManager.close();
    }

    @Test
    void shouldInsertUrl() throws Exception {
        // given
        LinkSelector linkSelector = new TopkLinkSelector();
        FrontierManager frontierManager = new FrontierManager(frontier, dataPath, modelPath, config,
                linkSelector, null, emptyLinkFilter, metricsManager);
        
        LinkRelevance link1 = new LinkRelevance(new URL("http://www.example1.com/index.html"), 1, LinkRelevance.Type.FORWARD);
        
        // when
        frontierManager.insert(link1);
        
        LinkRelevance nextURL = frontierManager.nextURL();
        
        // then
        assertThat(nextURL).isNotNull();
        assertThat(nextURL.getURL()).isNotNull();
        assertThat(nextURL.getURL()).isEqualTo(link1.getURL());
        assertThat(nextURL.getRelevance()).isEqualTo(link1.getRelevance());
        assertThat(nextURL.getType()).isEqualTo(link1.getType());
        
        frontierManager.close();
    }

    @Test
    void shouldNotInsertUrlTwice() throws Exception {
        // given
        Map<?, ?> props = ImmutableMap.of(
                "link_storage.scheduler.max_links", schedulerMaxLinks,
                "link_storage.scheduler.host_min_access_interval", 0,
                "link_storage.download_sitemap_xml", false
            );
        config = new Configuration(props).getLinkStorageConfig();
        LinkSelector linkSelector = new TopkLinkSelector();
        LinkSelector recrawlSelector = new SitemapsRecrawlSelector();
        FrontierManager frontierManager = new FrontierManager(frontier, dataPath, modelPath, config,
                linkSelector, recrawlSelector, emptyLinkFilter, metricsManager);
        
        LinkRelevance link1 = new LinkRelevance(new URL("http://www.example1.com/sitemap.xml"), 299, LinkRelevance.Type.SITEMAP);
        assertThat(frontierManager.isRelevant(link1)).isTrue();
        
        // when
        frontierManager.insert(link1);
        // then
        assertThat(frontierManager.isRelevant(link1)).isFalse();
        
        // when
        LinkRelevance nextURL = frontierManager.nextURL();
        // then
        assertThat(nextURL).isNotNull();
        assertThat(nextURL.getRelevance()).isEqualTo(299d);
        assertThat(frontierManager.isRelevant(link1)).isFalse();
        
        // when
        nextURL = frontierManager.nextURL();
        // then
        assertThat(nextURL).isNotNull();
        assertThat(nextURL.getRelevance()).isEqualTo(-299d);
        assertThat(frontierManager.isRelevant(link1)).isFalse();
        
        // finalize
        frontierManager.close();
    }

    @Test
    void shouldSelectUrlsInsertedAfterFirstSelect() throws Exception {
        // given
        int minimumAccessTimeInterval = 500;
        int schedulerMaxLinks = 10;
        Map<?, ?> props = ImmutableMap.of(
                "link_storage.scheduler.max_links", schedulerMaxLinks,
                "link_storage.scheduler.host_min_access_interval", minimumAccessTimeInterval,
                "link_storage.download_sitemap_xml", downloadSitemapXml
        );
        config = new Configuration(props).getLinkStorageConfig();
            
        LinkSelector linkSelector = new TopkLinkSelector();
        LinkSelector recrawlSelector = null;

        FrontierManager frontierManager = new FrontierManager(frontier, dataPath, modelPath, config,
                linkSelector, recrawlSelector, emptyLinkFilter, metricsManager);
        
        LinkRelevance link1 = new LinkRelevance(new URL("http://www.example1.com/index1.html"), 1, LinkRelevance.Type.FORWARD);
        LinkRelevance link2 = new LinkRelevance(new URL("http://www.example1.com/index2.html"), 1, LinkRelevance.Type.FORWARD);
        
        LinkRelevance link3 = new LinkRelevance(new URL("http://www.example2.com/index2.html"), 1, LinkRelevance.Type.FORWARD);
        
        // when
        frontierManager.insert(link1);
        frontierManager.insert(link2);
        LinkRelevance nextUrl1 = frontierManager.nextURL();
        
        frontierManager.insert(link3);
        
        // at this point, should not return link2, but it should return link3
        // because it is from another TLD
        LinkRelevance nextUrl3 = frontierManager.nextURL();
            
        
        // then
        assertThat(nextUrl1).isNotNull();
        assertThat(nextUrl1.getURL()).isNotNull();
        assertThat(nextUrl1.getURL()).isEqualTo(link1.getURL());
        assertThat(nextUrl1.getRelevance()).isEqualTo(link1.getRelevance());
        assertThat(nextUrl1.getType()).isEqualTo(link1.getType());
        
        assertThat(nextUrl3).isNotNull();
        assertThat(nextUrl3.getURL()).isNotNull();
        assertThat(nextUrl3.getURL()).isEqualTo(link3.getURL());
        assertThat(nextUrl3.getRelevance()).isEqualTo(link3.getRelevance());
        assertThat(nextUrl3.getType()).isEqualTo(link3.getType());
        
        frontierManager.close();
    }

    @Test
    void shouldInsertRobotsLinkWhenAddDomainForTheFirstTime() throws Exception {
        // given
        Map<?, ?> props = ImmutableMap.of(
            "link_storage.scheduler.max_links", schedulerMaxLinks,
            "link_storage.scheduler.host_min_access_interval", minimumAccessTimeInterval,
            "link_storage.download_sitemap_xml", true
        );
        LinkStorageConfig config = new Configuration(props).getLinkStorageConfig();
        assertThat(config.getDownloadSitemapXml()).isTrue();

        LinkSelector linkSelector = new TopkLinkSelector();
        LinkSelector recrawlSelector = null;

        FrontierManager frontierManager = new FrontierManager(frontier, dataPath, modelPath, config,
                linkSelector, recrawlSelector, emptyLinkFilter, metricsManager);

        LinkRelevance link1 = new LinkRelevance(new URL("http://www.example1.com/sitemap.xml"), 1,
                LinkRelevance.Type.FORWARD);

        // when
        frontierManager.insert(link1);


        // then
        LinkRelevance nextURL;

        nextURL = frontierManager.nextURL();
        assertThat(nextURL).isNotNull();
        assertThat(nextURL.getURL()).isNotNull();
        assertThat(nextURL.getURL().toString()).isEqualTo("http://www.example1.com/robots.txt");
        assertThat(nextURL.getType()).isEqualTo(LinkRelevance.Type.ROBOTS);

        nextURL = frontierManager.nextURL();
        assertThat(nextURL).isNotNull();
        assertThat(nextURL.getURL()).isNotNull();
        assertThat(nextURL.getURL()).isEqualTo(link1.getURL());
        assertThat(nextURL.getRelevance()).isEqualTo(link1.getRelevance());
        assertThat(nextURL.getType()).isEqualTo(link1.getType());

        frontierManager.close();
    }

    @Test
    void shouldInsertUrlsAndSelectUrlsInSortedByRelevance() throws Exception {
        // given
        LinkSelector linkSelector = new TopkLinkSelector();
        LinkSelector recrawlSelector = null;

        FrontierManager frontierManager = new FrontierManager(frontier, dataPath, modelPath, config,
                linkSelector, recrawlSelector, emptyLinkFilter, metricsManager);

        LinkRelevance link1 = new LinkRelevance(new URL("http://www.example1.com/index.html"), 1);
        LinkRelevance link2 = new LinkRelevance(new URL("http://www.example2.com/index.html"), 2);
        LinkRelevance link3 = new LinkRelevance(new URL("http://www.example3.com/index.html"), 3);

        // when
        frontierManager.insert(link1);
        frontierManager.insert(link2);
        frontierManager.insert(link3);

        LinkRelevance selectedLink1 = frontierManager.nextURL();
        LinkRelevance selectedLink2 = frontierManager.nextURL();
        LinkRelevance selectedLink3 = frontierManager.nextURL();
        DataNotFoundException notFoundException = null;
        try {
            frontierManager.nextURL();
        } catch (DataNotFoundException e) {
            notFoundException = e;
        }

        // then

        // should return only 3 inserted links, 4th should be null
        assertThat(selectedLink1).isNotNull();
        assertThat(selectedLink2).isNotNull();
        assertThat(selectedLink3).isNotNull();
        assertThat(notFoundException).isNotNull();
        assertThat(notFoundException.ranOutOfLinks()).isTrue();

        // should return bigger relevance values first
        assertThat(selectedLink1.getURL()).isEqualTo(link3.getURL());
        assertThat(selectedLink2.getURL()).isEqualTo(link2.getURL());
        assertThat(selectedLink3.getURL()).isEqualTo(link1.getURL());

        frontierManager.close();
    }


    @Test
    void shouldNotReturnAgainALinkThatWasAlreadyReturned() throws Exception {
        // given
        LinkSelector linkSelector = new TopkLinkSelector();
        LinkSelector recrawlSelector = null;

        FrontierManager frontierManager = new FrontierManager(frontier, dataPath, modelPath, config,
                linkSelector, recrawlSelector, emptyLinkFilter, metricsManager);
        
        LinkRelevance link1 = new LinkRelevance(new URL("http://www.example1.com/index.html"), 1);
        LinkRelevance link2 = new LinkRelevance(new URL("http://www.example2.com/index.html"), 2);
        
        // when
        frontierManager.insert(link1);
        frontierManager.insert(link2);
        LinkRelevance selectedLink1 = frontierManager.nextURL();
        LinkRelevance selectedLink2 = frontierManager.nextURL();
        DataNotFoundException notFoundException1 = null;
        try {
            frontierManager.nextURL();
        } catch(DataNotFoundException e) {
            notFoundException1 = e;
        }
        
        frontierManager.insert(link1); // insert link 1 again, should not be returned
        
        DataNotFoundException notFoundException2 = null;
        try {
            frontierManager.nextURL();
        } catch(DataNotFoundException e) {
            notFoundException2 = e;
        }
        
        // then
        assertThat(selectedLink1).isNotNull();
        assertThat(selectedLink2).isNotNull();

        assertThat(notFoundException1).isNotNull();
        assertThat(notFoundException1.ranOutOfLinks()).isTrue();

        assertThat(notFoundException2).isNotNull();
        assertThat(notFoundException2.ranOutOfLinks()).isTrue();

        frontierManager.close();
        
    }

    @Test
    void shouldNotReturnLinkReturnedWithinMinimumTimeInterval() throws Exception {
        // given
        int minimumAccessTimeInterval = 500;
        ImmutableMap<String, ?> props = ImmutableMap.of(
            "link_storage.scheduler.max_links", 2,
            "link_storage.scheduler.host_min_access_interval", minimumAccessTimeInterval,
            "link_storage.download_sitemap_xml", false
        );
        LinkStorageConfig config = new Configuration(props).getLinkStorageConfig();
        assertThat(config.getSchedulerHostMinAccessInterval()).isEqualTo(minimumAccessTimeInterval);
        
        LinkSelector linkSelector = new TopkLinkSelector();
        LinkSelector recrawlSelector = null;

        FrontierManager frontierManager = new FrontierManager(frontier, dataPath, modelPath, config,
                linkSelector, recrawlSelector, emptyLinkFilter, metricsManager);
        
        LinkRelevance link1 = new LinkRelevance(new URL("http://www.example1.com/index1.html"), 1);
        LinkRelevance link2 = new LinkRelevance(new URL("http://www.example1.com/index2.html"), 2);
        
        frontierManager.insert(link1);
        frontierManager.insert(link2);
        
        // when
        LinkRelevance selectedLink1 = frontierManager.nextURL();
        DataNotFoundException notFoundException1 = null;
        try {
            frontierManager.nextURL();
            fail("Should not return link right now.");
        } catch(DataNotFoundException e) {
            notFoundException1 = e;
            assertThat(e.ranOutOfLinks()).isFalse();
        }
        
        // should return after minimum time interval
        Thread.sleep(minimumAccessTimeInterval+10);
        LinkRelevance selectedLink2 = frontierManager.nextURL();        
        
        // then
        assertThat(selectedLink1).isNotNull();
        assertThat(selectedLink1.getURL().toString()).isEqualTo(link2.getURL().toString());
        
        assertThat(notFoundException1).isNotNull();
        assertThat(notFoundException1.ranOutOfLinks()).isFalse();
        
        assertThat(selectedLink2).isNotNull();
        assertThat(selectedLink2).isNotNull();
        
        frontierManager.close();
        
    }
    private static File newSubFolder(File root) throws IOException {
        return newSubFolder(root, String.valueOf(new Random().nextLong()));
    }

    private static File newSubFolder(File root, String... subDirs) throws IOException {
        String subFolder = String.join("/", subDirs);
        File result = new File(root, subFolder);
        if (!result.mkdirs()) {
            throw new IOException("Couldn't create folders " + root);
        }
        return result;
    }
}
