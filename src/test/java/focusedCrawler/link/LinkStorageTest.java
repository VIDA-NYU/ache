package focusedCrawler.link;

import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import com.google.common.collect.ImmutableMap;
import focusedCrawler.config.ConfigService;
import focusedCrawler.crawler.async.FetchedResultHandler;
import focusedCrawler.crawler.async.HttpDownloader;
import focusedCrawler.crawler.async.HttpDownloaderConfig;
import focusedCrawler.link.frontier.Frontier;
import focusedCrawler.link.frontier.FrontierManager;
import focusedCrawler.link.frontier.LinkRelevance;
import focusedCrawler.link.frontier.selector.LinkSelector;
import focusedCrawler.link.frontier.selector.RandomLinkSelector;
import focusedCrawler.util.DataNotFoundException;
import focusedCrawler.util.LinkFilter;
import focusedCrawler.util.MetricsManager;
import focusedCrawler.util.persistence.PersistentHashtable.DB;
import focusedCrawler.util.storage.Storage;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Map;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

public class LinkStorageTest {

    @Rule
    // a new temp folder is created for each test case
    public TemporaryFolder tempFolder = new TemporaryFolder();
    private LinkFilter emptyLinkFilter = new LinkFilter(new ArrayList<String>());
    private MetricsManager metricsManager = new MetricsManager();
    private LinkStorageConfig config = new LinkStorageConfig();
    private LinkSelector linkSelector = new RandomLinkSelector();

    private Frontier frontier;
    private String dataPath;
    private String modelPath;
    private LinkStorage linkStorage;
    private Storage targetStorage;
    private FrontierManager frontierManager;
    private HttpDownloader downloader;

    private int minimumAccessTimeInterval = 0;
    private int schedulerMaxLinks = 2;
    private boolean downloadSitemapXml = false;

    @Before
    public void setUp() throws Exception {
        frontier = new Frontier(tempFolder.newFolder().toString(), 1000, DB.ROCKSDB);
        dataPath = tempFolder.newFolder().toString();
        modelPath = tempFolder.newFolder().toString();
        Map<?, ?> props = ImmutableMap.of(
            "link_storage.scheduler.max_links", schedulerMaxLinks,
            "link_storage.scheduler.host_min_access_interval", minimumAccessTimeInterval,
            "link_storage.download_sitemap_xml", downloadSitemapXml
        );
        config = new ConfigService(props).getLinkStorageConfig();
        frontierManager = new FrontierManager(frontier, dataPath, modelPath, config,
            linkSelector, null, emptyLinkFilter, metricsManager);
        linkStorage = new LinkStorage(config,frontierManager);
        targetStorage = null;
        downloader = new HttpDownloader(new HttpDownloaderConfig(), dataPath, metricsManager);
    }

    @After
    public void tearDown() throws IOException {
        frontierManager.close();
        linkStorage.close();
        metricsManager.close();
    }

    @Test
    public void testingBlackList() {
        String url = "www.coansonf.com/abasdad";
        LinkRelevance link = null;
        try {
            link = new LinkRelevance(url,1d);
        }catch (MalformedURLException mue){}

        downloader.dipatchDownload(link, new FetchedResultHandler(linkStorage, targetStorage));
        assertThat(link,is(notNullValue()));
        assertThat(linkStorage.getBlackList(), is(notNullValue()));
        assertThat(linkStorage.getBlackList().contains(link.getTopLevelDomainName()), is(notNullValue()));
        assertThat(linkStorage.getBlackList().contains(link.getTopLevelDomainName()), is(true));

        DataNotFoundException dnfe = null;
        try {
            frontierManager.insert(link);
            linkStorage.select(null);
        }catch (Exception e) {
            if(e instanceof DataNotFoundException){
                dnfe = (DataNotFoundException) e;
            }
        }

        assertThat(dnfe, is(notNullValue()));
        assertThat(dnfe.ranOutOfLinks(), is(true));
    }

}
