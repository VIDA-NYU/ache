package focusedCrawler.target;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.apache.commons.codec.digest.DigestUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.cbor.CBORFactory;

import focusedCrawler.util.Page;
import focusedCrawler.util.Target;

/**
 * A target repository that stores pages in the file system. Files are organized per domain,
 * one folder for each domain. Supports serialization of data different formats:
 * - Plain HTML (may lose some data, about headers)
 * - JSON
 * - CBOR
 * 
 * @author aeciosantos
 *
 */
public class FileSystemTargetRepository implements TargetRepository {

	public enum DataFormat {
		HTML, JSON, CBOR
	}
	
    private static final Logger logger = LoggerFactory.getLogger(FileSystemTargetRepository.class);
    private static final ObjectMapper jsonMapper = new ObjectMapper();
    private static final ObjectMapper cborMapper = new ObjectMapper(new CBORFactory());
    
    private Path directory;
	private DataFormat dataFormat;
    private boolean hashFilename;

    public FileSystemTargetRepository(String directory,
                                      DataFormat dataFormat,
                                      boolean hashFilename) {
		this(Paths.get(directory), dataFormat, hashFilename);
    }
    
    public FileSystemTargetRepository(Path directory,
                                      DataFormat dataFormat,
                                      boolean hashFilename) {
    	File fileDir = directory.toFile();
    	if(!fileDir.exists()) {
    		fileDir.mkdirs();
    	}
        this.directory = directory;
        this.dataFormat = dataFormat;
        this.hashFilename = hashFilename;
    }

    public boolean insert(Target target, int counter) {
        return insert(target);
    }

    public boolean insert(Target target) {
        try {
            String id = target.getIdentifier();
            URL url = new URL(id);
            Path hostPath = getHostPath(url);

            File hostDirectory = hostPath.toFile();
            if (!hostDirectory.exists()) {
                hostDirectory.mkdirs();
            }

            Path filePath = getFilePath(id, hostPath);
            
            try(PrintStream fileStream = new PrintStream(filePath.toFile())) {
                switch(dataFormat) {
                	case HTML:
                	{
	            	    fileStream.print(target.getSource());
    	            	break;
                	}
                	case JSON:
                	{
                		TargetModelJson targetModel = new TargetModelJson((Page) target);
                		jsonMapper.writeValue(fileStream, targetModel);
                		break;
                	}
                	case CBOR:
                	{
                		TargetModel targetModel = new TargetModel("", "", url, target.getSource());
                		cborMapper.writeValue(fileStream, targetModel);
                		break;
                	}
                }
            }
        } catch (IOException e) {
            logger.error("Failed to store object in repository.", e);
        }
        
        return false;
    }

    public boolean exists(String urlString) {
        try {
            Path hostPath = getHostPath(urlString);
    
            File hostDirectory = hostPath.toFile();
            if (!hostDirectory.exists()) {
                return false;
            }
            
            Path filePath = getFilePath(urlString, hostPath);
            
            if (filePath.toFile().exists()) {
                return true;
            }
            
        } catch (UnsupportedEncodingException | MalformedURLException e) {
            return false;
        }
        return false;
    }

    private Path getHostPath(URL url) throws MalformedURLException, UnsupportedEncodingException {
        String host = url.getHost();
        Path hostPath = directory.resolve(URLEncoder.encode(host, "UTF-8"));
        return hostPath;
    }
    
    private Path getHostPath(String url) throws MalformedURLException, UnsupportedEncodingException {
        return getHostPath(new URL(url));
    }

    private Path getFilePath(String url, Path hostPath) throws UnsupportedEncodingException {
        Path filePath;
        if(hashFilename) {
            String filenameEncoded =  DigestUtils.sha256Hex(url);
            filePath = hostPath.resolve(filenameEncoded);
        } else {
            filePath = hostPath.resolve(URLEncoder.encode(url, "UTF-8"));
        }
        return filePath;
    }

    public <T> T get(String url) {
        try {
            Path hostPath = getHostPath(url);
            Path filePath = getFilePath(url, hostPath);
            if (!Files.exists(filePath)) {
                return null;
            }
            return unserializeData(filePath);
        } catch (UnsupportedEncodingException | MalformedURLException e) {
            return null;
        }
    }
    
    private <T> T unserializeData(Path path) {
        T nextObject = null;
        try {
            byte[] fileData = Files.readAllBytes(path);
            if (dataFormat.equals(DataFormat.CBOR)) {
                nextObject = (T) cborMapper.readValue(fileData, TargetModel.class);
            } else if (dataFormat.equals(DataFormat.JSON)) {
                nextObject = (T) jsonMapper.readValue(fileData, TargetModelJson.class);
            } else if (dataFormat.equals(DataFormat.HTML)) {
                nextObject = (T) new String(fileData);
            }
        }
        catch (IOException e) {
            throw new RuntimeException("Failed to read object from repository.", e);
        }
        return nextObject;
    }

}
