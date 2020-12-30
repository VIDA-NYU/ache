package achecrawler.tools;

import java.io.File;
import java.io.IOException;
import java.net.URLDecoder;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.cbor.CBORFactory;

import achecrawler.util.LinkFilter;

public class CborDataReclassifier {

    static final ObjectMapper cborMapper = new ObjectMapper(new CBORFactory());
    static final ObjectMapper jsonMapper = new ObjectMapper();

    public static void main(String[] args) throws IOException {

        Path inputLocation = Paths.get(args[0]);
        Path negativeDirectory = Paths.get(args[1]);

        LinkFilter linkfilter = new LinkFilter.Builder().withWhitelistFile(args[2]).build();

        int filesMoved = 0;
        int filesTotal = 0;
        
        DirectoryStream<Path> fileStream = Files.newDirectoryStream(inputLocation);
        
        for (Path p : fileStream) {
            File f = p.toFile();
            
            String url = f.getName();
            url = url.substring(0, url.lastIndexOf('_'));
            url = URLDecoder.decode(url, "UTF-8");

            // TargetModel targetModel = cborMapper.readValue(f, achecrawler.target.TargetModel.class);
            // String url = targetModel.url;

            boolean accept = linkfilter.accept(url);
            if (!accept) {
                // System.out.println(accept + " -> " + url);
                Path newPath = negativeDirectory.resolve(p.getFileName());
                f.renameTo(newPath.toFile());
                filesMoved++;
                if(filesMoved%1000==0) {
                    double percent = filesMoved / (filesTotal+0d);
                    System.out.println(filesMoved + " out of " + filesTotal + " files moved." +
                                       " (" + percent + ")");
                }
            }
            filesTotal++;
        }

    }

}
