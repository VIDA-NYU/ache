package focusedCrawler.target.repository;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.zip.InflaterInputStream;

import org.apache.commons.compress.utils.IOUtils;
import org.archive.format.warc.WARCConstants;
import org.archive.io.ArchiveRecord;
import org.archive.io.warc.WARCReader;
import org.archive.io.warc.WARCReaderFactory;
import org.archive.io.warc.WARCRecord;
import org.archive.io.warc.WARCWriter;
import org.archive.io.warc.WARCWriterPoolSettingsData;
import org.archive.uid.RecordIDGenerator;
import org.archive.uid.UUIDGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import focusedCrawler.target.model.Page;
import focusedCrawler.target.model.TargetModelWarcRecord;

public class WarcTargetRepository implements TargetRepository {

    private static final Logger logger = LoggerFactory.getLogger(WarcTargetRepository.class);

    private boolean compress = false;
    private final Path directory;
    private final long maxFileSize;
    private WARCWriter writer;
    private WARCReader reader;

    private RecordIDGenerator generator = new UUIDGenerator();

    public WarcTargetRepository(String directory) throws IOException {
        this(Paths.get(directory), WARCConstants.DEFAULT_MAX_WARC_FILE_SIZE);
    }

    public WarcTargetRepository(String path, long maxFileSize) throws IOException {
        this(Paths.get(path), maxFileSize);
    }

    public WarcTargetRepository(Path directory, long maxFileSize) throws IOException {
        this.maxFileSize = maxFileSize;
        if (!Files.exists(directory)) {
            directory.toFile().mkdirs();
        }
        this.directory = directory;
        File[] files = { directory.toFile() };
        List<File> outputDirs = Arrays.asList(files);
        this.writer = new WARCWriter(new AtomicInteger(),
                new WARCWriterPoolSettingsData("crawl_data-", "${prefix}" + "-%d",
                        WARCConstants.DEFAULT_MAX_WARC_FILE_SIZE, compress, outputDirs, null, generator));
        //this.reader = WARCReaderFactory.get(new File("crawl_data")); 
    }

    @Override
    public boolean insert(Page target) {
        try {
            TargetModelWarcRecord warcRecord = new TargetModelWarcRecord(target, generator.getRecordID());
            writer.writeRecord(warcRecord);
            return true;
        } catch (IOException e) {
            logger.debug(
                    "Exception thrown while creating a " + "warc record with the following message. " + e.getMessage());
        }
        return false;
    }

    @Override
    public void close() {
        try{
            writer.close();
        }catch(IOException e){
            logger.debug("Exception thrown when trying to close the WARC writer:"+e.getMessage());
        }
    }
    
//    /**
//     * Returns Iterator of WarcReader
//     * @return
//     * @throws IOException
//     */
//    public Iterator<ArchiveRecord>  iterator() throws IOException{
//        return reader.iterator();
//    }
//
//    /**
//     * Return file object of the current file written by WarcWriter
//     * @return
//     */
//    private File getFile(){
//        return writer.getFile();
//    }
//    
//public class FilesIterator implements Iterator<String>, Closeable {
//        
//        private String next;
//        private Iterator<Path> filesIt;
//        private DirectoryStream<Path> filesStream;
//        private BufferedReader linesReader;
//
//        public FilesIterator(Path directory) {
//            try {
//                filesStream = Files.newDirectoryStream(directory);
//                filesIt = filesStream.iterator();
//                if(filesIt.hasNext()) {
//                    linesReader = openFile(filesIt.next());
//                }
//            } catch (IOException e) {
//                throw new IllegalArgumentException(
//                        "Failed to open target repository folder: "+directory, e);
//            }
//            this.next = readNext();
//        }
//
//        private BufferedReader openFile(Path filePath) throws FileNotFoundException {
//            return new BufferedReader(new InputStreamReader(new FileInputStream(filePath.toFile())));
//        }
//        
//        private String readNext() {
//            String nextLine = null;
//            if(linesReader != null) {
//                try {
//                    nextLine = linesReader.readLine();
//                } catch (IOException e) {
//                    nextLine = null;
//                }
//                if(nextLine == null) { // end of file reached
//                    IOUtils.closeQuietly(linesReader);
//                    if(!filesIt.hasNext()) {
//                        IOUtils.closeQuietly(filesStream);
//                        return null; // no more file and lines available
//                    }
//                    // read next file available
//                    Path filePath = null;
//                    try {
//                        filePath = filesIt.next();
//                        linesReader = openFile(filePath);
//                        nextLine = linesReader.readLine();
//                    } catch (IOException e) {
//                        String f = filePath == null ? null : filePath.toString();
//                        throw new IllegalStateException("Failed to open file: "+f, e);
//                    }
//                }
//            }
//            return nextLine;
//        }
//
//        @Override
//        public boolean hasNext() {
//            return this.next != null;
//        }
//
//        @Override
//        public String next() {
//            if(this.next == null) {
//                return null;
//            } else {
//                String returnValue = this.next;
//                this.next = readNext();
//                return returnValue;
//            }
//        }
//        
//        @Override
//        public void remove() {
//            throw new UnsupportedOperationException("remove");
//        }
//
//        @Override
//        public void close() {
//            IOUtils.closeQuietly(linesReader);
//            IOUtils.closeQuietly(filesStream);
//        }
//        
//    }
    
}
