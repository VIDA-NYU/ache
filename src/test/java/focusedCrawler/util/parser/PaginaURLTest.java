package focusedCrawler.util.parser;

import static org.junit.Assert.*;

import java.io.File;
import java.io.FileNotFoundException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.Scanner;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;



public class PaginaURLTest {
    
    public static final Logger logger = LoggerFactory.getLogger(PaginaURLTest.class);
    
    
    @Test
    public void linksShouldNotContainFragments(){
        
        // put the path of directory where we have the crawled pages to be tested
        //File URLdirecory = new File("/Users/rajatpawar/Documents/ache/url_output");
        
        File URLdirecory = new File(PaginaURLTest.class.getResource("PaginaURL/PaginaURLTest").getPath());
        
        File[] allDirectories = URLdirecory.listFiles();
        
        for (File eachDirectory: allDirectories) {
            
            File[] htmlFiles = eachDirectory.listFiles();
            
                for( File eachtmlFile: htmlFiles) {
                    
                if(!(eachtmlFile.getName().equals(".DS_Store"))) {
                    URL fileUrl;
                    try {
                        fileUrl = new URL(URLDecoder.decode(eachtmlFile.getName()));
                        String source = readContentsOfFile(eachtmlFile);
                        PaginaURL pageParser = new PaginaURL(fileUrl, 0, 0,source.length(),source, null);
                        Object[] extractedLinks = pageParser.links();
                        assertEquals(" This file has some fragments: " + eachtmlFile.getName(),false,hasFragments(extractedLinks));
                    } catch (MalformedURLException e) {
                        logger.error("URL of input file not in proper format.", e);
                    }
                   
                }  
            }
        }
    }
    
    private boolean hasFragments(Object[] urls){
        for (Object url : urls) {
                if(url.toString().contains("#"))
                    return true;
        }
        return false;
    }
    

    private String readContentsOfFile(File fileUrl){
        
        Scanner fileScanner;
        StringBuilder source = new StringBuilder();
        try {
            fileScanner = new Scanner(fileUrl);
            while(fileScanner.hasNext())
            source.append(fileScanner.nextLine() + "\n");
        } catch (FileNotFoundException e) {
            logger.error("Unable to find file!", e);
        }
        
        return source.toString();
    }

}

