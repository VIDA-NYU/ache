package achecrawler.tools;

import java.io.BufferedOutputStream;
import java.io.Closeable;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorOutputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.cbor.CBORFactory;

import achecrawler.target.model.TargetModelCbor;

public class GzipCborFileWriter implements Closeable {
    
    private static final Logger logger = LoggerFactory.getLogger(GzipCborFileWriter.class);
    
    private final ObjectMapper mapper;
    
    private FileOutputStream fileOutput = null;
    private BufferedOutputStream bufOutput = null;
    private GzipCompressorOutputStream gzipOutput = null;
    private TarArchiveOutputStream tarOutput = null;
    
    public GzipCborFileWriter(String outputFilename, ObjectMapper mapper) throws IOException {
        File file = new File(outputFilename);
        if(file.exists()) {
            logger.warn("File already exists: "+file.getCanonicalPath());
        }
        createNewGzipFileStream(file);
        this.mapper = mapper;
    }

    public GzipCborFileWriter(String outputFilename) throws IOException {
        this(outputFilename, new ObjectMapper(new CBORFactory()));
    }
    
    public synchronized void writeTargetModel(TargetModelCbor target) throws IOException {

        byte[] byteData = mapper.writeValueAsBytes(target);
        
        TarArchiveEntry tarEntry = new TarArchiveEntry(target.key);
        tarEntry.setSize(byteData.length);
        
        tarOutput.putArchiveEntry(tarEntry);
        tarOutput.write(byteData);
        tarOutput.closeArchiveEntry();
    }
    
    private void createNewGzipFileStream(File archive) throws IOException {    
        fileOutput = new FileOutputStream(archive);
        bufOutput = new BufferedOutputStream(fileOutput);
        gzipOutput = new GzipCompressorOutputStream(bufOutput);
        tarOutput = new TarArchiveOutputStream(gzipOutput);
        tarOutput.setLongFileMode(TarArchiveOutputStream.LONGFILE_GNU);
    }
    
    @Override
    public void close() {
        try {
            tarOutput.finish();
            tarOutput.close();
            gzipOutput.close();
            bufOutput.close();
            fileOutput.close();
        } catch (IOException e) {
            logger.error("Error in closing stream: " + e);
        }
    }
    
}
