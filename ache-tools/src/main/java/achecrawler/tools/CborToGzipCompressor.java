package achecrawler.tools;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Comparator;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.cbor.CBORFactory;

import achecrawler.target.model.TargetModelCbor;

public class CborToGzipCompressor {
    
    static final ObjectMapper cborMapper = new ObjectMapper(new CBORFactory());
    static final ObjectMapper jsonMapper = new ObjectMapper();
    
    public static void main(String[] args) throws IOException {
        
        String inputLocation = args[0];
        String outputLocation = args[1];
        long objectsPerFile = Long.parseLong(args[2]);
        boolean useJson = false;
        
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
            
//            if(!f.getName().contains("showthread.php")) {
//                continue;
//            }
            
            TargetModelCbor targetModel = cborMapper.readValue(f, achecrawler.target.model.TargetModelCbor.class);

            // open gzip output file
            if (currentArchive == null || objectsWritten % objectsPerFile == 0) {
                if (currentArchive != null) {
                    gzipCborFileWriter.close();
                }
                currentArchive = new File(inputLocation).getName()+"_"+System.currentTimeMillis();
                
                if(useJson)
                    currentArchive = currentArchive+"_json.tar.gz";
                else
                    currentArchive = currentArchive+"_cbor.tar.gz";
                
                String fullArchivePath = outputLocation + File.separator + currentArchive;
                
                if(useJson) {
                    gzipCborFileWriter = new GzipCborFileWriter(fullArchivePath, jsonMapper);
                } else {
                    gzipCborFileWriter = new GzipCborFileWriter(fullArchivePath, cborMapper);
                }
            }
            
            // fix key for objects stored with wrong key 
            targetModel.key = targetModel.computeReverseKey(targetModel.url);
            
            System.out.println("Writing object: "+f.getName());
            
            gzipCborFileWriter.writeTargetModel(targetModel);
            objectsWritten++;
        }
        
        if(gzipCborFileWriter != null) {
            gzipCborFileWriter.close();
        }
        
    }
    
}
