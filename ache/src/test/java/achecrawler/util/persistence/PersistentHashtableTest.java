package achecrawler.util.persistence;

import java.io.File;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import achecrawler.link.frontier.LinkRelevance;

import static org.assertj.core.api.Assertions.assertThat;

public class PersistentHashtableTest {

    @TempDir
    public File tempFolder;
//    public PersistentHashtable.DB database;
    
    /* 
     * This test runs multiple times for each of the following parameters,
     * to make sure that it works with all underlying database implementations.
     */
    public static Iterable<? extends Object> data() {
        return Arrays.asList(PersistentHashtable.DB.ROCKSDB);
    }

    @MethodSource("data")
    @ParameterizedTest
    void shouldAddAndGetKey(PersistentHashtable.DB database) {
        // given
        PersistentHashtable<String> ht = new PersistentHashtable<>(tempFolder.toString(), 1000, String.class, database);
        
        // when
        ht.put("my_key", "123");
        
        // then
        assertThat(ht.get("my_key")).isEqualTo("123");
    }

    @MethodSource("data")
    @ParameterizedTest
    void shouldCheckIfKeyExists(PersistentHashtable.DB database) {
        // given
        PersistentHashtable<String> ht = new PersistentHashtable<>(tempFolder.toString(), 1000, String.class, database);

        // when
        ht.put("my_existent_key", "123");

        // then
        assertThat(ht.get("my_existent_key")).isEqualTo("123");
        assertThat(ht.get("unexistent")).isNull();
    }

    @MethodSource("data")
    @ParameterizedTest
    void shouldPersistDataIntoHashtable(PersistentHashtable.DB database) {
        // given
        int cacheSize = 1;
        String folder = tempFolder.toString();
        PersistentHashtable<String> ht = new PersistentHashtable<>(folder, cacheSize, String.class, database);

        // when
        ht.put("my_key1", "111");
        ht.put("my_key2", "222");
        ht.put("http://foo.com/index.php&a=1", "333");
        ht.close();

        ht = new PersistentHashtable<>(folder, cacheSize, String.class, database);

        // then
        assertThat(ht.get("my_key1")).isEqualTo("111");
        assertThat(ht.get("my_key2")).isEqualTo("222");
        assertThat(ht.get("http://foo.com/index.php&a=1")).isEqualTo("333");
    }

    @MethodSource("data")
    @ParameterizedTest
    void shouldReturnOrderedTupleSet(PersistentHashtable.DB database) throws Exception {
        // given
        int cacheSize = 1;
        String folder = tempFolder.toString();
        PersistentHashtable<LinkRelevance> ht = new PersistentHashtable<>(folder, cacheSize, LinkRelevance.class, database);

        Comparator<LinkRelevance > linkRelevanceComparator = (o1, o2) -> Double.compare(o2.getRelevance(), o1.getRelevance());

        // when
        ht.put("http://bar.com", new LinkRelevance("http://bar.com", 2d));
        ht.put("http://foo.com", new LinkRelevance("http://foo.com", 1d));
        ht.put("http://zoo.com", new LinkRelevance("http://bar.com", 3d));
        ht.commit();

        List<Tuple<LinkRelevance>> keys = ht.orderedSet(linkRelevanceComparator);

        // then
        assertThat(keys.size()).isEqualTo(3);
        assertThat(keys.get(0).getValue().getRelevance()).isEqualTo(3d);
        assertThat(keys.get(1).getValue().getRelevance()).isEqualTo(2d);
        assertThat(keys.get(2).getValue().getRelevance()).isEqualTo(1d);
    }

    @MethodSource("data")
    @ParameterizedTest
    void shouldIterateOverTuples(PersistentHashtable.DB database) throws Exception {
        // given
        PersistentHashtable<Integer> ht = new PersistentHashtable<>(tempFolder.toString(), 1000, Integer.class, database);
        ht.put("1", 1);
        ht.put("2", 2);
        ht.put("3", 3);
        ht.commit();
        
        // when
        try(TupleIterator<Integer> it = ht.iterator()) {
            // then
            Tuple<Integer> tuple;
            assertThat(it.hasNext()).isTrue();
            tuple = it.next();
            assertThat(tuple.getKey()).isEqualTo("1");
            assertThat(tuple.getValue()).isEqualTo(1);

            assertThat(it.hasNext()).isTrue();
            tuple = it.next();
            
            assertThat(tuple.getKey()).isEqualTo("2");
            assertThat(tuple.getValue()).isEqualTo(2);
            
            assertThat(it.hasNext()).isTrue();
            tuple = it.next();
            
            assertThat(tuple.getKey()).isEqualTo("3");
            assertThat(tuple.getValue()).isEqualTo(3);
            
            assertThat(it.hasNext()).isFalse();
            assertThat(it.next()).isNull();
        }
    }

    @MethodSource("data")
    @ParameterizedTest
    void shoudNotCrashWhenIterateOverEmptyHashtable(PersistentHashtable.DB database) throws Exception {
        // given
        PersistentHashtable<Integer> ht = new PersistentHashtable<>(tempFolder.toString(), 1000, Integer.class, database);

        // when
        try(TupleIterator<Integer> it = ht.iterator()) {
            // then
            assertThat(it.hasNext()).isFalse();
            assertThat(it.next()).isNull();
        }
    }

}
