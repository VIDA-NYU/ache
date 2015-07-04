package focusedCrawler.target;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.file.Path;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.cbor.CBORFactory;

import focusedCrawler.util.Target;

public class TargetCBORRepository implements TargetRepository {

    private static final Logger logger = LoggerFactory.getLogger(TargetCBORRepository.class);
    
    private Path repositoryPath;
    
    // This contact information should be read from config file
    private String contactName = "Kien Pham";
    private String contactEmail = "kien.pham@nyu.edu";
    
    private static final ObjectMapper mapper = new ObjectMapper(new CBORFactory());

    public TargetCBORRepository(Path repositoryPath) {
        File file = repositoryPath.toFile();
        if(!file.exists()) {
            file.mkdirs();
        }
        this.repositoryPath = repositoryPath;
    }

    public boolean insert(Target target, int counter) {
        return insert(target);
    }

    public boolean insert(Target target) {
        boolean contain = false;
        try {
            String url = target.getIdentifier();
            URL urlObject = new URL(url);
            String host = urlObject.getHost();
            
            TargetModel targetModel = new TargetModel(contactName, contactEmail,
                                                      urlObject, target.getSource());
            
            Path hostPath = repositoryPath.resolve(URLEncoder.encode(host, "UTF-8"));
            File hostDirectory = hostPath.toFile();
            if (!hostDirectory.exists()) {
                hostDirectory.mkdir();
            }
            
            String filename = URLEncoder.encode(url, "UTF-8");
            
            File outputFile = hostPath.resolve(filename).toFile();
            mapper.writeValue(outputFile, targetModel);
            
        } catch (IOException e) {
            logger.error("Failed to store object in repository.", e);
        }
        return contain;
    }

}
