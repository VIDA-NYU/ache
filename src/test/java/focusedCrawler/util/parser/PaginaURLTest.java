package focusedCrawler.util.parser;

import static org.junit.Assert.*;

import java.io.File;
import java.io.FileFilter;
import java.io.FileNotFoundException;
import java.io.UnsupportedEncodingException;
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
    public void linksShouldNotContainFragments() throws UnsupportedEncodingException {

        File URLdirecory = new File(PaginaURLTest.class.getResource("PaginaURL/paginaURLTest")
                .getPath());

        File[] allDirectories = URLdirecory.listFiles(new FileFilter() {

            @Override
            public boolean accept(File pathname) {
                if (pathname.getName().equals(".DS_Store"))
                    return false;
                return true;
            }
        });

        for (File eachDirectory : allDirectories) {

            File[] htmlFiles = eachDirectory.listFiles();

            for (File eachtmlFile : htmlFiles) {
                URL fileUrl;
                try {
                    fileUrl = new URL(URLDecoder.decode(eachtmlFile.getName(), "UTF-8"));
                    String source = readContentsOfFile(eachtmlFile);
                    PaginaURL pageParser = new PaginaURL(fileUrl, 0, 0, source.length(), source,
                            null);
                    Object[] extractedLinks = pageParser.links();
                    assertEquals(" This file has some fragments: " + eachtmlFile.getName(), false,
                            hasFragments(extractedLinks));
                } catch (MalformedURLException e) {
                    logger.error("URL of input file not in proper format.", e);
                }
            }
        }
    }

    private boolean hasFragments(Object[] urls) {
        for (Object url : urls) {
            if (url.toString().contains("#"))
                return true;
        }
        return false;
    }

    private String readContentsOfFile(File fileUrl) {

        StringBuilder source = new StringBuilder();
        try {
            Scanner fileScanner = new Scanner(fileUrl);
            while (fileScanner.hasNext())
                source.append(fileScanner.nextLine() + "\n");
            fileScanner.close();
        } catch (FileNotFoundException e) {
            logger.error("Unable to find file!", e);
        }

        return source.toString();
    }

}
