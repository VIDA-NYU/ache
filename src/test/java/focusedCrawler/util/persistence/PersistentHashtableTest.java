package focusedCrawler.util.persistence;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.util.Comparator;
import java.util.List;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import focusedCrawler.link.frontier.LinkRelevance;

public class PersistentHashtableTest {
    
    @Rule
    // a new temp folder is created for each test case
    public TemporaryFolder tempFolder = new TemporaryFolder();

    @Test
    public void shoudAddAndGetKey() throws Exception {
        // given
        PersistentHashtable<String> ht = new PersistentHashtable<>(tempFolder.newFolder().toString(), 1000, String.class);
        
        // when
        ht.put("my_key", "123");
        
        // then
        assertThat(ht.get("my_key"), is("123"));
    }
    
    
    @Test
    public void shoudPersistDataIntoHastable() throws Exception {
        // given
        int cacheSize = 1;
        String folder = tempFolder.newFolder().toString();
        PersistentHashtable<String> ht = new PersistentHashtable<>(folder, cacheSize, String.class);
        
        // when
        ht.put("my_key1", "111");
        ht.put("my_key2", "222");
        ht.put("http://foo.com/index.php&a=1", "333");
        ht.close();
        
        ht = new PersistentHashtable<>(folder, cacheSize, String.class);
        
        // then
        assertThat(ht.get("my_key1"), is("111"));
        assertThat(ht.get("my_key2"), is("222"));
        assertThat(ht.get("http://foo.com/index.php&a=1"), is("333"));
    }

    @Test
    public void shoudReturnOrderedTupleSet() throws Exception {
        // given
        int cacheSize = 1;
        String folder = tempFolder.newFolder().toString();
        PersistentHashtable<LinkRelevance> ht = new PersistentHashtable<>(folder, cacheSize, LinkRelevance.class);

        Comparator<LinkRelevance > linkRelevanceComparator = new Comparator<LinkRelevance >() {
            public int compare(LinkRelevance o1, LinkRelevance o2) {
                return Double.compare(o2.getRelevance(), o1.getRelevance());
            }
        };
        
        // when
        ht.put("http://bar.com", new LinkRelevance("http://bar.com", 2d));
        ht.put("http://foo.com", new LinkRelevance("http://foo.com", 1d));
        ht.put("http://zoo.com", new LinkRelevance("http://bar.com", 3d));
        ht.commit();
        
        List<Tuple<LinkRelevance>> keys = ht.orderedSet(linkRelevanceComparator);
        
        // then
        assertThat(keys.size(), is(3));
        assertThat(keys.get(0).getValue().getRelevance(), is(3d));
        assertThat(keys.get(1).getValue().getRelevance(), is(2d));
        assertThat(keys.get(2).getValue().getRelevance(), is(1d));
    }

}
