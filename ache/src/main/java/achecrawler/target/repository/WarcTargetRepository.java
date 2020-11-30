package achecrawler.target.repository;


import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TimeZone;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.compress.utils.IOUtils;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import achecrawler.target.classifier.TargetRelevance;
import achecrawler.target.model.Page;
import achecrawler.target.model.TargetModelJson;
import achecrawler.util.CloseableIterator;

public class WarcTargetRepository implements TargetRepository {

    private static final Logger logger = LoggerFactory.getLogger(WarcTargetRepository.class);
    
    private  static final String CRLF = "\r\n";
    private static final String ISO_8601_DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss'Z'";
    private static final ThreadLocal<SimpleDateFormat> dateFormater =
            new ThreadLocal<SimpleDateFormat>() {
                @Override
                protected SimpleDateFormat initialValue() {
                    Locale currentLocale = Locale.getDefault();
                    SimpleDateFormat dateFormat = new SimpleDateFormat(ISO_8601_DATE_FORMAT, currentLocale);
                    dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
                    return dateFormat;
                }
            };

    private final RecordIDGenerator generator = new UUIDGenerator();

    private final boolean compress;
    private final Path directory;
    private final long maxFileSize;
    private WARCWriter writer;


    public WarcTargetRepository(String directory) throws IOException {
        this(Paths.get(directory), WARCConstants.DEFAULT_MAX_WARC_FILE_SIZE, true);
    }

    public WarcTargetRepository(String path, long maxFileSize) throws IOException {
        this(Paths.get(path), maxFileSize, true);
    }

    public WarcTargetRepository(Path directory, long maxFileSize, boolean compress) throws IOException {
        this.maxFileSize = maxFileSize;
        if (!Files.exists(directory)) {
            directory.toFile().mkdirs();
        }
        this.directory = directory;
        this.compress = compress;
    }

    @Override
    public boolean insert(Page target) {
        try {
            if (writer == null) {
                createWarcWriter();
            }
            WARCRecordInfo warcRecord = getWarcRecordInfo(target, generator.getRecordID());
            synchronized (writer) {
                writer.checkSize();
                writer.writeRecord(warcRecord);
                writer.resetTmpStats();
                writer.resetTmpRecordLog();
            }
            return true;
        } catch (IOException e) {
            logger.error("Exception thrown while creating a WARC record.", e);
        }
        return false;
    }

    private synchronized void createWarcWriter() {
        if (this.writer != null) {
            return;
        }
        List<File> outputDirs = Arrays.asList(directory.toFile());
        List<String> metadata = new ArrayList<>();
        this.writer = new WARCWriter(new AtomicInteger(),
                new WARCWriterPoolSettingsData("crawl_data", "${prefix}-${timestamp17}-${serialno}",
                        this.maxFileSize, this.compress, outputDirs, metadata, generator));
    }

    /*
     * Default visibility (for use in unit tests).
     */
    WARCWriter getWriter() {
        return writer;
    }

    @Override
    public synchronized void close() {
        try {
            if (writer != null) {
                writer.close();
            }
        } catch (IOException e) {
            logger.error("Exception thrown when trying to close the WARC writer.", e);
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

    public WARCRecordInfo getWarcRecordInfo(Page page, URI uri) throws IOException {
        WARCRecordInfo warcRecord = new WARCRecordInfo();

        warcRecord.setUrl(page.getFinalUrl());
        warcRecord.setRecordId(uri);
        warcRecord.setType(WARCRecordType.response);
        warcRecord.setMimetype(WARCConstants.HTTP_RESPONSE_MIMETYPE);

        // Store fetch times using ISO-8601 format
        Date fetchTime = createFetchTimeDate(page);
        warcRecord.setCreate14DigitDate(dateFormater.get().format(fetchTime));

        // Re-create response body based on content bytes and response headers
        byte[] contentBytes = createContentBytes(page);
        warcRecord.setContentLength(contentBytes.length);
        warcRecord.setContentStream(new ByteArrayInputStream(contentBytes));

        // Store ACHE-specific metadata as non-standard extension header fields
        if (page.getTargetRelevance() != null) {
            TargetRelevance targetRelevance = page.getTargetRelevance();
            warcRecord.addExtraHeader("ACHE-IsRelevant", String.valueOf(targetRelevance.isRelevant()));
            warcRecord.addExtraHeader("ACHE-Relevance", String.format("%.10f", targetRelevance.getRelevance()));
        }
        warcRecord.addExtraHeader("ACHE-Requested-URL", page.getRequestedUrl());

        return warcRecord;
    }

    private Date createFetchTimeDate(Page page) {
        Date fetchTime;
        if (page.getFetchTime() != 0L) {
            fetchTime = new Date(page.getFetchTime());
        } else {
            fetchTime = new Date(System.currentTimeMillis());
        }
        return fetchTime;
    }

    private byte[] createContentBytes(Page page) throws IOException, UnsupportedEncodingException {
        Map<String, List<String>> responseHeaders = page.getResponseHeaders();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        if (responseHeaders != null) {
            baos.write(convertHeaderToString(responseHeaders).getBytes());
        }
        baos.write(page.getContent());
        return baos.toByteArray();
    }

    private String convertHeaderToString(Map<String, List<String>> responseHeaders) {
        StringBuilder sb = new StringBuilder();
        for (Entry<String, List<String>> header : responseHeaders.entrySet()) {
            List<String> headerValue = header.getValue();
            if(headerValue == null || headerValue.isEmpty()) {
                continue;
            }
            for (String value : headerValue) {
                sb.append(header.getKey());
                sb.append(':');
                sb.append(' ');
                sb.append(value);
                sb.append(CRLF);
            }
        }
        sb.append(CRLF);
        return sb.toString();
    }

    public class RepositoryIterator implements CloseableIterator<WARCRecord> {

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
            return warcRecordsIterator.next();
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

    public class WarcRecordsIterator implements CloseableIterator<WARCRecord> {

        private Iterator<Path> filesIt;
        private DirectoryStream<Path> filesStream;
        private WARCReader warcReader;
        private Iterator<ArchiveRecord> warcRecordIterator;

        public WarcRecordsIterator(Path directory) {
            try {
                filesStream = Files.newDirectoryStream(directory);
                filesIt = filesStream.iterator();
                if (filesIt.hasNext()) {
                    Path file = filesIt.next();
                    warcReader = openFile(file);
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
                    nextRecord = (WARCRecord) warcRecordIterator.next();
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
                        nextRecord = (WARCRecord) warcReader.get();
                    } catch (IOException e) {
                        String f = filePath == null ? null : filePath.toString();
                        throw new IllegalStateException("Failed to open file: " + f, e);
                    }
                }
            }
            Object warcType = nextRecord.getHeader().getHeaderValue("WARC-Type");
            if (nextRecord != null && warcType.equals("warcinfo")) {
                // skip the header of the warc file
                if (hasNext()) {
                    return readNext();
                }
            }
            return nextRecord;
        }

        @Override
        public boolean hasNext() {
            if (warcRecordIterator != null) {
                return warcRecordIterator.hasNext() || filesIt.hasNext();
            } else {
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

    @Override
    public CloseableIterator<Page> pagesIterator() {
        WarcRecordsIterator it = new WarcRecordsIterator(directory);
        return new PagesIterator(it);
    }

    public static class PagesIterator implements CloseableIterator<Page> {

        private WarcRecordsIterator it;

        public PagesIterator(WarcRecordsIterator it) {
            this.it = it;
        }

        @Override
        public boolean hasNext() {
            return it.hasNext();
        }

        @Override
        public Page next() {
            return new Page(it.next());
        }

        @Override
        public void close() throws Exception {
            it.close();
        }

    }

    public TargetModelJsonIterator targetModelJsonIterator() {
        WarcRecordsIterator it = new WarcRecordsIterator(directory);
        return new TargetModelJsonIterator(new PagesIterator(it));
    }

    static class TargetModelJsonIterator implements CloseableIterator<TargetModelJson> {

        private PagesIterator it;

        public TargetModelJsonIterator(PagesIterator it) {
            this.it = it;
        }

        @Override
        public boolean hasNext() {
            return it.hasNext();
        }

        @Override
        public TargetModelJson next() {
            return new TargetModelJson(it.next());
        }

        @Override
        public void close() throws Exception {
            it.close();
        }

    }

}
