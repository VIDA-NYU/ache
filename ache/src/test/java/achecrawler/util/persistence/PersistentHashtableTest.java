package achecrawler.util.persistence;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;
import org.junit.runners.Parameterized.Parameters;

import achecrawler.link.frontier.LinkRelevance;

@RunWith(Parameterized.class)
public class PersistentHashtableTest {
    
    @Rule
    // a new temp folder is created for each test case
    public TemporaryFolder tempFolder = new TemporaryFolder();
    
    @Parameter
    public PersistentHashtable.DB database;
    
    /* 
     * This test runs multiple times for each of the following parameters,
     * to make sure that it works with all underlying database implementations.
     */
    @Parameters
    public static Iterable<? extends Object> data() {
        return Arrays.asList(PersistentHashtable.DB.ROCKSDB);
    }

    @Test
    public void shouldAddAndGetKey() throws Exception {
        // given
        PersistentHashtable<String> ht = new PersistentHashtable<>(tempFolder.newFolder().toString(), 1000, String.class, database);
        
        // when
        ht.put("my_key", "123");
        
        // then
        assertThat(ht.get("my_key"), is("123"));
    }
    
    @Test
    public void shouldCheckIfKeyExists() throws Exception {
        // given
        PersistentHashtable<String> ht = new PersistentHashtable<>(tempFolder.newFolder().toString(), 1000, String.class, database);
        
        // when
        ht.put("my_existent_key", "123");
        
        // then
        assertThat(ht.get("my_existent_key"), is("123"));
        assertThat(ht.get("unexistent"), is(nullValue()));
    }
    
    @Test
    public void shouldPersistDataIntoHastable() throws Exception {
        // given
        int cacheSize = 1;
        String folder = tempFolder.newFolder().toString();
        PersistentHashtable<String> ht = new PersistentHashtable<>(folder, cacheSize, String.class, database);
        
        // when
        ht.put("my_key1", "111");
        ht.put("my_key2", "222");
        ht.put("http://foo.com/index.php&a=1", "333");
        ht.close();
        
        ht = new PersistentHashtable<>(folder, cacheSize, String.class, database);
        
        // then
        assertThat(ht.get("my_key1"), is("111"));
        assertThat(ht.get("my_key2"), is("222"));
        assertThat(ht.get("http://foo.com/index.php&a=1"), is("333"));
    }

    @Test
    public void shouldReturnOrderedTupleSet() throws Exception {
        // given
        int cacheSize = 1;
        String folder = tempFolder.newFolder().toString();
        PersistentHashtable<LinkRelevance> ht = new PersistentHashtable<>(folder, cacheSize, LinkRelevance.class, database);

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
    
    @Test
    public void shouldIterateOverTuples() throws Exception {
        // given
        PersistentHashtable<Integer> ht = new PersistentHashtable<>(tempFolder.newFolder().toString(), 1000, Integer.class, database);
        ht.put("1", 1);
        ht.put("2", 2);
        ht.put("3", 3);
        ht.commit();
        
        // when
        try(TupleIterator<Integer> it = ht.iterator()) {
            // then
            Tuple<Integer> tuple;
            assertThat(it.hasNext(), is(true));
            tuple = it.next();
            assertThat(tuple.getKey(), is("1"));
            assertThat(tuple.getValue(), is(1));

            assertThat(it.hasNext(), is(true));
            tuple = it.next();
            
            assertThat(tuple.getKey(), is("2"));
            assertThat(tuple.getValue(), is(2));
            
            assertThat(it.hasNext(), is(true));
            tuple = it.next();
            
            assertThat(tuple.getKey(), is("3"));
            assertThat(tuple.getValue(), is(3));
            
            assertThat(it.hasNext(), is(false));
            assertThat(it.next(), is(nullValue()));
        }
    }
    
    @Test
    public void shoudNotCrashWhenIterateOverEmptyHashtable() throws Exception {
        // given
        PersistentHashtable<Integer> ht = new PersistentHashtable<>(tempFolder.newFolder().toString(), 1000, Integer.class, database);

        // when
        try(TupleIterator<Integer> it = ht.iterator()) {
            // then
            assertThat(it.hasNext(), is(false));
            assertThat(it.next(), is(nullValue()));
        }
    }

}
