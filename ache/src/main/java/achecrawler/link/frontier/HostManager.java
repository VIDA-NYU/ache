package achecrawler.link.frontier;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import achecrawler.util.persistence.PersistentHashtable;
import achecrawler.util.persistence.PersistentHashtable.DB;

public class HostManager {
    
    final private PersistentHashtable<Boolean> hostsData;
    
    public HostManager(Path path, DB persistentHashTableBackend) {
        if(!Files.exists(path)) {
            try {
                Files.createDirectory(path);
            } catch (IOException e) {
                throw new RuntimeException(
                        "Failed to create hosts manager directory: " + path.toString(), e);
            }
        }
        this.hostsData = new PersistentHashtable<>(path.toString(), 10000, Boolean.class, persistentHashTableBackend);
    }
    
    public boolean isKnown(String host) {
        Boolean data = hostsData.get(host);
        if(data != null) {
            return true;
        }
        return false;
    }
    
    public void insert(String host) {
        hostsData.put(host, true);
    }
    
    public void close() {
        hostsData.close();
    }

}
