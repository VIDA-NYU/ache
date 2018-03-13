package focusedCrawler.util.persistence.rocksdb;

import org.rocksdb.RocksDB;
import org.rocksdb.RocksIterator;

import focusedCrawler.util.CloseableIterator;
import focusedCrawler.util.KV;

public class RocksDBIterator implements CloseableIterator<KV<byte[], byte[]>> {

        final private RocksIterator cursor;
        private boolean hasNext;
        private boolean isOpen;
        private byte[] value;
        private byte[] key;

        public RocksDBIterator(RocksDB db) {
            this.cursor = db.newIterator();
            this.cursor.seekToFirst();
            this.isOpen = true;
            readNextKV(true);
        }

        private void readNextKV(boolean firstEntry) {
            if(!firstEntry) {
                cursor.next();
            }
            if(cursor.isValid()) {
                this.hasNext = true;
                this.key = cursor.key();
                this.value = cursor.value();
            } else {
                this.close();
            }
        }

        @Override
        public void close() {
            if(this.isOpen) {
                cursor.close();
                this.isOpen = false;
                this.hasNext = false;
            }
        }

        @Override
        public boolean hasNext() {
            return hasNext;
        }

        @Override
        public KV<byte[], byte[]> next() {
            if (!hasNext) {
                return null;
            }
            KV<byte[], byte[]> kv = new KV<>(this.key, this.value);
            readNextKV(false);
            return kv;
        }
        
        public void remove() {
            throw new UnsupportedOperationException("remove() not yet supported by "+getClass().getName());
        }

    }