package achecrawler.target.repository;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintStream;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Iterator;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.InflaterInputStream;

import org.apache.commons.compress.utils.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.io.CountingOutputStream;

import achecrawler.target.model.Page;
import achecrawler.target.model.TargetModelJson;
import achecrawler.util.CloseableIterator;

/**
 * A target repository that stores pages in compressed (DEFLATE) text files containing one JSON
 * object per line. Files have a maximum size, and additional files are created when the size limit
 * is reached.
 * 
 * @author aeciosantos
 *
 */
public class FilesTargetRepository implements TargetRepository {
    
    private static final long DEFAULT_MAX_FILE_SIZE = 256*1024*1024;
    
    private static final Logger logger = LoggerFactory.getLogger(FilesTargetRepository.class);
    private static final ObjectMapper jsonMapper = new ObjectMapper();

    private final Path directory;
    private final long maxFileSize;
    
    private DeflaterOutputStream currentFile;
    private CountingOutputStream bytesCounter;

    public FilesTargetRepository(String directory) {
        this(Paths.get(directory), DEFAULT_MAX_FILE_SIZE);
    }
    
    public FilesTargetRepository(String directory, long maxFileSize) {
        this(Paths.get(directory), maxFileSize);
    }

    public FilesTargetRepository(Path directory, long maxFileSize) {
        this.maxFileSize = maxFileSize;
        if (!Files.exists(directory)) {
            directory.toFile().mkdirs();
        }
        this.directory = directory;
    }

    public boolean insert(Page target) {
        return insert(new TargetModelJson(target));
    }
    
    public boolean insert(TargetModelJson target) {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            jsonMapper.writeValue(baos, target);
            baos.write("\n".getBytes());
            synchronized (this) {
                DeflaterOutputStream currentFile = getCurrentFile(baos);
                baos.writeTo(currentFile);
                currentFile.flush();
            }
            return true;
        } catch (IOException e) {
            logger.error("Failed to store object in repository.", e);
            return false;
        }
    }

    private DeflaterOutputStream getCurrentFile(ByteArrayOutputStream baos) throws IOException {
        if(this.currentFile == null) {
            openNewFile();
        }  else {
            if(bytesCounter.getCount() + baos.size() > maxFileSize) {
                openNewFile();
            }
        }
        return currentFile;
    }

    private synchronized void openNewFile() throws IOException {
        if(currentFile != null) {
            // flush and automatically closes file
            try(OutputStream out = this.currentFile) {
                out.flush();
            }
        }
        long timestamp = System.currentTimeMillis();
        long count = 0;
        Path filePath;
        do {
            String file = String.format("crawl_data-%d-%d.deflate", timestamp, count++);
            filePath = directory.resolve(file);
        } while (Files.exists(filePath));
        OutputStream fileStream = new PrintStream(filePath.toFile());
        this.bytesCounter = new CountingOutputStream(fileStream);
        this.currentFile = new DeflaterOutputStream(this.bytesCounter, true);
    }
    
    public void close() {
        IOUtils.closeQuietly(currentFile);
    }

    @Override
    public CloseableIterator<Page> pagesIterator() {
        return new RepositoryIterator(new JsonLinesIterator(directory));
    }

    public class RepositoryIterator implements CloseableIterator<Page> {

        private JsonLinesIterator jsonLinesIterator;

        public RepositoryIterator(JsonLinesIterator fileIterator) {
            this.jsonLinesIterator = fileIterator;
        }
        
        @Override
        public boolean hasNext() {
            return jsonLinesIterator.hasNext();
        }

        @Override
        public Page next() {
            if(!jsonLinesIterator.hasNext()) {
                return null;
            }
            String jsonLine = jsonLinesIterator.next();
            try {
                TargetModelJson jsonModel = jsonMapper.readValue(jsonLine, TargetModelJson.class);
                return new Page(jsonModel);
            } catch (Exception e) {
                String json = jsonLine == null ? null : jsonLine.toString();
                throw new IllegalStateException("Failed to unserialize json: "+json, e);
            }
        }
        
        @Override
        public void remove() {
            throw new UnsupportedOperationException("remove");
        }
        
        @Override
        public void close() {
            jsonLinesIterator.close();
        }
        
    }
    
    public class JsonLinesIterator implements Iterator<String>, Closeable {
        
        private String next;
        private Iterator<Path> filesIt;
        private DirectoryStream<Path> filesStream;
        private BufferedReader linesReader;

        public JsonLinesIterator(Path directory) {
            try {
                filesStream = Files.newDirectoryStream(directory);
                filesIt = filesStream.iterator();
                if(filesIt.hasNext()) {
                    linesReader = openFile(filesIt.next());
                }
            } catch (IOException e) {
                throw new IllegalArgumentException(
                        "Failed to open target repository folder: "+directory, e);
            }
            this.next = readNext();
        }

        private BufferedReader openFile(Path filePath) throws FileNotFoundException {
            return new BufferedReader(new InputStreamReader(
                    new InflaterInputStream(new FileInputStream(filePath.toFile()))));
        }
        
        private String readNext() {
            String nextLine = null;
            if(linesReader != null) {
                try {
                    nextLine = linesReader.readLine();
                } catch (IOException e) {
                    nextLine = null;
                }
                if(nextLine == null) { // end of file reached
                    IOUtils.closeQuietly(linesReader);
                    if(!filesIt.hasNext()) {
                        IOUtils.closeQuietly(filesStream);
                        return null; // no more file and lines available
                    }
                    // read next file available
                    Path filePath = null;
                    try {
                        filePath = filesIt.next();
                        linesReader = openFile(filePath);
                        nextLine = linesReader.readLine();
                    } catch (IOException e) {
                        String f = filePath == null ? null : filePath.toString();
                        throw new IllegalStateException("Failed to open file: "+f, e);
                    }
                }
            }
            return nextLine;
        }

        @Override
        public boolean hasNext() {
            return this.next != null;
        }

        @Override
        public String next() {
            if(this.next == null) {
                return null;
            } else {
                String returnValue = this.next;
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
            IOUtils.closeQuietly(linesReader);
            IOUtils.closeQuietly(filesStream);
        }
        
    }
}
