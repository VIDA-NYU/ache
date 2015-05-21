package focusedCrawler.tools.cborCompression;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.cbor.CBORFactory;

import focusedCrawler.target.TargetModel;

public class CborToGzipCompressor {
    
    static final ObjectMapper cborMapper = new ObjectMapper(new CBORFactory());
    
    public static void main(String[] args) throws IOException {
        
        String inputLocation = args[0];
        String outputLocation = args[1];
        long objectsPerFile = Long.parseLong(args[2]);
        
        File file = new File(inputLocation);
        File[] files = file.listFiles();
        Arrays.sort(files, new Comparator<File>() {
            @Override
            public int compare(File o1, File o2) {
                return Long.compare(o1.lastModified(), o2.lastModified());
            }
        });
        
        String currentArchive = null;
        GzipCborFileWriter gzipCborFileWriter = null;
        
        long objectsWritten = 0;
        for (File f : files) {
            
            TargetModel targetModel = cborMapper.readValue(f, focusedCrawler.target.TargetModel.class);

            // open gzip output file
            if (currentArchive == null || objectsWritten % objectsPerFile == 0) {
                if (currentArchive != null) {
                    gzipCborFileWriter.close();
                }
                currentArchive = new SimpleDateFormat("yyyy-MM-dd_").format(new Date())+System.currentTimeMillis()+".tar.gz";
                String fullArchivePath = outputLocation + File.separator + currentArchive;
                gzipCborFileWriter = new GzipCborFileWriter(fullArchivePath);
            }
            
            // fix key
            String url = targetModel.url;
            String domain = new URL(url).getHost();
            targetModel.setReverseKey(url, domain);
            
            System.out.println("Writing object: "+targetModel.key);
            
            gzipCborFileWriter.writeTargetModel(targetModel);
            objectsWritten++;
        }
        
        if(gzipCborFileWriter != null) {
            gzipCborFileWriter.close();
        }
        
    }
    
}
