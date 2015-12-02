package focusedCrawler.util.persistence;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;

import java.util.Vector;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import focusedCrawler.util.vsm.VSMElement;

public class PersistentHashtableTest {
    
    @Rule
    // a new temp folder is created for each test case
    public TemporaryFolder tempFolder = new TemporaryFolder();

    @Test
    public void shoudAddAndGetKey() throws Exception {
        // given
        PersistentHashtable ht = new PersistentHashtable(tempFolder.newFolder().toString(), 1000);
        
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
        PersistentHashtable ht = new PersistentHashtable(folder, cacheSize);
        
        // when
        ht.put("my_key1", "111");
        ht.put("my_key2", "222");
        ht.close();
        
        ht = new PersistentHashtable(folder, cacheSize);
        Vector<VSMElement> keys = ht.orderedSet();
        
        // then
        assertThat(keys.size(), is(2));
        assertThat(keys.get(0).getWord(), is("my_key2"));
        assertThat(keys.get(1).getWord(), is("my_key1"));
        
        assertThat(ht.get("my_key1"), is("111"));
        assertThat(ht.get("my_key2"), is("222"));
    }

    @Test
    public void shoudReturnOrderedKeySet() throws Exception {
        // given
        int cacheSize = 1;
        
        String folder = tempFolder.newFolder().toString();
        PersistentHashtable ht = new PersistentHashtable(folder, cacheSize);
        
        // when
        ht.put("my_key1", "111");
        ht.put("my_key2", "222");
        ht.commit();
        
        Vector<VSMElement> keys = ht.orderedSet();
        
        // then
        assertThat(keys.size(), is(2));
        assertThat(keys.get(0).getWord(), is("my_key2"));
        assertThat(keys.get(1).getWord(), is("my_key1"));
        
        assertThat(ht.get("my_key1"), is("111"));
        assertThat(ht.get("my_key2"), is("222"));
    }

}
