package achecrawler.target.repository;

import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Iterator;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.InflaterInputStream;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.compress.utils.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.cbor.CBORFactory;

import achecrawler.target.model.Page;
import achecrawler.target.model.TargetModelCbor;
import achecrawler.target.model.TargetModelJson;
import achecrawler.util.CloseableIterator;

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
    
    private final Path directory;
    private final DataFormat dataFormat;
    private final boolean hashFilename;
    private final boolean compressData;

    public FileSystemTargetRepository(String directory,
                                      DataFormat dataFormat,
                                      boolean hashFilename) {
		this(Paths.get(directory), dataFormat, hashFilename, false);
    }
    
    public FileSystemTargetRepository(String directory,
                                      DataFormat dataFormat,
                                      boolean hashFilename,
                                      boolean compressData) {
        this(Paths.get(directory), dataFormat, hashFilename, compressData);
    }
    
    public FileSystemTargetRepository(Path directory,
                                      DataFormat dataFormat,
                                      boolean hashFilename,
                                      boolean compressData) {
    	File fileDir = directory.toFile();
    	if(!fileDir.exists()) {
    		fileDir.mkdirs();
    	}
        this.directory = directory;
        this.dataFormat = dataFormat;
        this.hashFilename = hashFilename;
        this.compressData = compressData;
    }
    
    @Override
    public void close() {}

    public boolean insert(Page target) {
        try {
            String id = target.getURL().toString();
            URL url = new URL(id);
            Path hostPath = getHostPath(url);
            File hostDirectory = hostPath.toFile();
            if (!hostDirectory.exists()) {
                hostDirectory.mkdirs();
            }

            Path filePath = getFilePath(id, hostPath);
            
            try(OutputStream fileStream = new PrintStream(filePath.toFile())) {
                if(compressData) {
                    try(OutputStream gzipStream = new DeflaterOutputStream(fileStream)) {
                        serializeData(target, url, gzipStream);
                    }
                } else {
                    serializeData(target, url, fileStream);
                }
            }
            
        } catch (IOException e) {
            logger.error("Failed to store object in repository.", e);
        }
        
        return false;
    }

    private void serializeData(Page target, URL url, OutputStream fileStream)
            throws IOException, JsonGenerationException, JsonMappingException {
        switch(dataFormat) {
        case HTML:
        {
            fileStream.write(target.getContent());
            break;
        }
        case JSON:
        {
            TargetModelJson targetModel = new TargetModelJson(target);
            jsonMapper.writeValue(fileStream, targetModel);
            break;
        }
        case CBOR:
        {
            TargetModelCbor targetModel = new TargetModelCbor("", "", url, target.getContentAsString());
            cborMapper.writeValue(fileStream, targetModel);
            break;
        }
        }
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
            return readFile(filePath);
        } catch (IOException e) {
            return null;
        }
    }

    private <T> T readFile(Path filePath) throws IOException, FileNotFoundException {
        if (!Files.exists(filePath)) {
            return null;
        }
        try (InputStream fileStream = new FileInputStream(filePath.toFile())) {
            if(compressData) {
                try(InputStream gzipStream = new InflaterInputStream(fileStream)) {
                    return unserializeData(gzipStream);
                }
            } else {
                return unserializeData(fileStream);
            }
        }
    }
    
    @SuppressWarnings("unchecked")
    private <T> T unserializeData(InputStream inputStream) {
        T object = null;
        try {
            if (dataFormat.equals(DataFormat.CBOR)) {
                    object = (T) cborMapper.readValue(inputStream, TargetModelCbor.class);
            } else if (dataFormat.equals(DataFormat.JSON)) {
                object = (T) jsonMapper.readValue(inputStream, TargetModelJson.class);
            } else if (dataFormat.equals(DataFormat.HTML)) {
                byte[] fileData = IOUtils.toByteArray(inputStream);
                object = (T) new String(fileData);
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to unserialize object.", e);
        }
        return object;
    }

    @Override
    public CloseableIterator<Page> pagesIterator() {
        return new PagesIterator(new FilesIterator(directory));
    }

    public FilesIterator filesIterator() {
        return new FilesIterator(directory);
    };
    
    public class PagesIterator implements CloseableIterator<Page> {
        
        private FilesIterator fileIterator;

        public PagesIterator(FilesIterator fileIterator) {
            this.fileIterator = fileIterator;
        }
        
        @Override
        public boolean hasNext() {
            return fileIterator.hasNext();
        }

        @Override
        public Page next() {
            if(!fileIterator.hasNext()) {
                return null;
            }
            Path filePath = fileIterator.next();
            try {
                if (dataFormat.equals(DataFormat.CBOR)) {
                    TargetModelCbor object = readFile(filePath);
                    return new Page(object);
                } else if (dataFormat.equals(DataFormat.JSON)) {
                    TargetModelJson object = readFile(filePath);
                    return new Page(object);
                } else if (dataFormat.equals(DataFormat.HTML)) {
                    URL url;
                    if(hashFilename) {
                        url = new URL("http://"+filePath);
                    } else {
                        url = new URL(URLDecoder.decode(filePath.toString(), "UTF-8"));
                    }
                    String object = readFile(filePath);
                    return new Page(url, object);
                } else {
                    throw new IllegalStateException("Unsuported data format: "+dataFormat);
                }
            } catch (Exception e) {
                String f = filePath == null ? null : filePath.toString();
                throw new IllegalStateException("Failed to read file: "+f, e);
            }
        }
        
        @Override
        public void remove() {
            throw new UnsupportedOperationException("remove");
        }

        @Override
        public void close() {
            fileIterator.close();
        }
        
    }
    
    public class FilesIterator implements Iterator<Path>, Closeable {
        
        private Path next;
        private Iterator<Path> fileIt;
        private Iterator<Path> hostIt;
        private DirectoryStream<Path> hostsStream;
        private DirectoryStream<Path> filesStream;

        public FilesIterator(Path directory) {
            try {
                hostsStream = Files.newDirectoryStream(directory);
                hostIt = hostsStream.iterator();
                if(hostIt.hasNext()) {
                    filesStream = Files.newDirectoryStream(hostIt.next());
                    fileIt = filesStream.iterator();
                }
            } catch (IOException e) {
                throw new IllegalArgumentException("Failed to open target repository folder: "+directory, e);
            }
            this.next = readNext();
        }
        
        private Path readNext() {
            if(fileIt != null && !fileIt.hasNext()) {
                IOUtils.closeQuietly(filesStream); // no more files on this folder, close it.
                if(!hostIt.hasNext()) {
                    IOUtils.closeQuietly(hostsStream);
                    return null; // no more file and folders available
                }
                // iterate over next folder available
                Path hostPath = null;
                try {
                    hostPath = hostIt.next();
                    filesStream = Files.newDirectoryStream(hostPath);
                    fileIt = filesStream.iterator();
                } catch (IOException e) {
                    String f = hostPath == null ? null : hostPath.toString();
                    throw new IllegalArgumentException("Failed to open host folder: "+f, e);
                }
            }
            
            if(fileIt != null && fileIt.hasNext()) {
                return fileIt.next();
            }
            
            return null;
        }

        @Override
        public boolean hasNext() {
            return this.next != null;
        }

        @Override
        public Path next() {
            if(this.next == null) {
                return null;
            } else {
                Path returnValue = this.next;
                this.next = readNext();
                return returnValue;
            }
        }
        
        @Override
        public void remove() {
            throw new UnsupportedOperationException("remove");
        }

        @Override
        public void close() {
            IOUtils.closeQuietly(filesStream);
            IOUtils.closeQuietly(hostsStream);
        }
        
    }
    
}
