package focusedCrawler.target.repository;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.URI;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.zip.InflaterInputStream;

import org.apache.commons.compress.utils.IOUtils;
import org.apache.tika.mime.MediaType;
import org.archive.format.warc.WARCConstants;
import org.archive.format.warc.WARCConstants.WARCRecordType;
import org.archive.io.ArchiveRecord;
import org.archive.io.warc.WARCReader;
import org.archive.io.warc.WARCReaderFactory;
import org.archive.io.warc.WARCRecord;
import org.archive.io.warc.WARCRecordInfo;
import org.archive.io.warc.WARCWriter;
import org.archive.io.warc.WARCWriterPoolSettingsData;
import org.archive.uid.RecordIDGenerator;
import org.archive.uid.UUIDGenerator;
import org.archive.util.anvl.ANVLRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import focusedCrawler.target.model.Page;
import focusedCrawler.target.model.TargetModelJson;
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

        // this.writer = new WARCWriter(new AtomicInteger(),
        // new WARCWriterPoolSettingsData("crawl_data-", "${prefix}" + "-%d",
        // WARCConstants.DEFAULT_MAX_WARC_FILE_SIZE, compress, outputDirs, null,
        // generator));
        // this.reader = WARCReaderFactory.get(filePath.toFile());
    }

    @Override
    public boolean insert(Page target) {
        try {
            if (writer == null) {
                File[] files = { directory.toFile() };
                List<File> outputDirs = Arrays.asList(files);
                long timestamp = System.currentTimeMillis();
                long count = 0;
                Path filePath;
                do {
                    String file = String.format("crawl_data-%d-%d.warc", timestamp, count++);
                    filePath = directory.resolve(file);
                } while (Files.exists(filePath));
                OutputStream fileStream = new PrintStream(filePath.toFile());
                this.writer = new WARCWriter(new AtomicInteger(), fileStream, filePath.toFile(),
                        new WARCWriterPoolSettingsData("crawl_data-", "${prefix}" + "-%d", this.maxFileSize, compress,
                                outputDirs, null, generator));
            }
            WARCRecordInfo warcRecord = getWarcRecordInfo(target, generator.getRecordID());
            writer.writeRecord(warcRecord);
            return true;
        } catch (IOException e) {
            logger.debug(
                    "Exception thrown while creating a " + "warc record with the following message. " + e.getMessage());
        }
        return false;
    }

    public WARCWriter getWriter() {
        return writer;
    }

    @Override
    public void close() {
        try {
            if (writer != null) {
                writer.close();
            }
        } catch (IOException e) {
            logger.debug("Exception thrown when trying to close the WARC writer:" + e.getMessage());
        }
    }

    /**
     * Returns Iterator of WarcReader
     * 
     * @return
     * @throws IOException
     */
    public RepositoryIterator iterator() {
        return new RepositoryIterator(new WarcRecordsIterator(directory));
    }

    /**
     * Return file object of the current file written by WarcWriter
     * 
     * @return
     */
    private File getFile() {
        if (writer != null)
            return writer.getFile();
        else
            return null;
    }

    public WARCRecordInfo getWarcRecordInfo(Page page, URI uri) throws IOException {
        WARCRecordInfo warcRecordInfo = new WARCRecordInfo();

        if (page.getURL() != null)
            warcRecordInfo.setUrl(page.getURL().toString());

        byte[] content = page.getContent();
        warcRecordInfo.setContentLength((long) content.length);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        baos.write(content);

        warcRecordInfo.setContentStream(new ByteArrayInputStream(baos.toByteArray()));
        ANVLRecord headerFields = new ANVLRecord();
        if (page.getTargetRelevance() != null) {
            headerFields.addLabelValue("relevance", page.getTargetRelevance().getRelevance() + "");
            headerFields.addLabelValue("isRelevant", page.getTargetRelevance().isRelevant() + "");
        }

        headerFields.addLabelValue(WARCConstants.CONTENT_TYPE, page.getContentType() + "");
        if (page.getRedirectedURL() != null) {
            headerFields.addLabelValue("redirected-url", page.getRedirectedURL().toString());
        }

        if (page.getResponseHeaders() != null) {
            for (String header : page.getResponseHeaders().keySet()) {
                headerFields.addLabelValue(header, page.getResponseHeaders().get(header).toString());
            }
        }
        warcRecordInfo.setExtraHeaders(headerFields);

        warcRecordInfo.setCreate14DigitDate(new Date(page.getFetchTime()).toString());

        warcRecordInfo.setType(WARCRecordType.response);
        warcRecordInfo.setMimetype(getMimeTypeFromContentType(page.getContentType()));
        warcRecordInfo.setRecordId(uri);

        return warcRecordInfo;
    }

    private String getMimeTypeFromContentType(String contentType) {
        String result = "";
        MediaType mt = MediaType.parse(contentType);
        if (mt != null) {
            result = mt.getType() + "/" + mt.getSubtype();
        }

        return result;
    }

    public class RepositoryIterator implements Iterator<WARCRecord>, Closeable {

        private WarcRecordsIterator warcRecordsIterator;

        public RepositoryIterator(WarcRecordsIterator fileIterator) {
            this.warcRecordsIterator = fileIterator;
        }

        @Override
        public boolean hasNext() {
            return warcRecordsIterator.hasNext();
        }

        @Override
        public WARCRecord next() {
            if (!warcRecordsIterator.hasNext()) {
                return null;
            }
            WARCRecord warcRecord = warcRecordsIterator.next();
            try {
                return warcRecord;// jsonMapper.readValue(warcRecord,
                                  // TargetModelJson.class);
            } catch (Exception e) {
                String warc = warcRecord == null ? null : warcRecord.toString();
                throw new IllegalStateException("Failed to unserialize warc record ", e);
            }
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException("remove");
        }

        @Override
        public void close() {
            warcRecordsIterator.close();
        }

    }

    public class WarcRecordsIterator implements Iterator<WARCRecord>, Closeable {

        private WARCRecord next;
        private Iterator<Path> filesIt;
        private DirectoryStream<Path> filesStream;
        private WARCReader warcReader;
        private Iterator<ArchiveRecord> warcRecordIterator;

        public WarcRecordsIterator(Path directory) {
            try {
                filesStream = Files.newDirectoryStream(directory);
                filesIt = filesStream.iterator();
                if (filesIt.hasNext()) {
                    warcReader = openFile(filesIt.next());
                    warcRecordIterator = warcReader.iterator();
                }
            } catch (IOException e) {
                throw new IllegalArgumentException("Failed to open target repository folder: " + directory, e);
            }
        }

        private WARCReader openFile(Path filePath) throws IOException {
            return WARCReaderFactory.get(filePath.toFile());
        }

        private WARCRecord readNext() throws IOException {
            WARCRecord nextRecord = null;
            if (warcReader != null) {
                try {
                    nextRecord = (WARCRecord) warcRecordIterator.next();// (WARCRecord)
                                                                        // warcRecordIterator.next();
                } catch (Exception e) {
                    nextRecord = null;
                }
                if (nextRecord == null) { // end of file reached
                    warcReader.close();
                    if (!filesIt.hasNext()) {
                        IOUtils.closeQuietly(filesStream);
                        return null; // no more file and lines available
                    }
                    // read next file available
                    Path filePath = null;
                    try {
                        filePath = filesIt.next();
                        warcReader = openFile(filePath);
                        warcRecordIterator = warcReader.iterator();
                        nextRecord = (WARCRecord) warcReader.get();// (WARCRecord)
                                                                   // warcRecordIterator.next();
                    } catch (IOException e) {
                        String f = filePath == null ? null : filePath.toString();
                        throw new IllegalStateException("Failed to open file: " + f, e);
                    }
                }
            }
            return nextRecord;
        }

        @Override
        public boolean hasNext() {
            if(warcRecordIterator != null){
                return warcRecordIterator.hasNext();
            }else{
                return false;
            }
        }

        @Override
        public WARCRecord next() {
            WARCRecord returnValue = null;
            try {
                returnValue = readNext();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return returnValue;
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException("remove");
        }

        @Override
        public void close() {
            IOUtils.closeQuietly(warcReader);
            IOUtils.closeQuietly(filesStream);
        }

    }

}
