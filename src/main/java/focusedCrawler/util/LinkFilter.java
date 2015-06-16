package focusedCrawler.util;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class LinkFilter {

    private static final Logger logger = LoggerFactory.getLogger(LinkFilter.class);
    
    private LinkWhiteList whitelist;
    private LinkBlackList blacklist;

    public LinkFilter(String configPath) {
        this(new LinkWhiteList(configPath+"/link_whitelist.txt"),
             new LinkBlackList(configPath+"/link_blacklist.txt"));
    }
    
    public LinkFilter(List<String> regularExpressions) {
        this(new LinkWhiteList(regularExpressions));
    }
    
    public LinkFilter(LinkWhiteList linkWhiteList) {
        this.whitelist = linkWhiteList;
        this.blacklist = new LinkBlackList(new ArrayList<String>());
    }
    
    public LinkFilter(LinkBlackList linkBlackList) {
        this.whitelist = new LinkWhiteList(new ArrayList<String>());
        this.blacklist = linkBlackList;
    }
    
    public LinkFilter(LinkWhiteList linkWhiteList, LinkBlackList linkBlackList) {
        this.whitelist = linkWhiteList;
        this.blacklist = linkBlackList;
    }

    public boolean accept(String link) {
        if(whitelist.accept(link) && blacklist.accept(link))
            return true;
        else
            return false;
    }
    
    
    /**
     * Tests a link agains a list of patterns and return true if link matches any of the patterns.
     */
    public static class LinkMatcher {
    
        List<Pattern> patterns = new ArrayList<Pattern>(); 
    
        public LinkMatcher(String filename) {
            this(loadRegexesFromFile(filename));
        }
        
        public LinkMatcher(List<String> urlPatterns) {
            for (String urlPattern : urlPatterns) {
                Pattern p = Pattern.compile(urlPattern);
                patterns.add(p);
            }
        }
    
        public boolean matches(String url) {
            for (Pattern pattern : patterns) {
                if(pattern.matcher(url).matches()) {
                    return true;
                }
            }
            return false;
        }
        
        public static LinkMatcher fromFile(String filename) {
            return new LinkMatcher(filename);
        }
        
        public static List<String> loadRegexesFromFile(String filename) {
            logger.info("Loading regex patterns from file: "+filename);
            List<String> urlPatterns = new ArrayList<String>();
            try (BufferedReader br = new BufferedReader(new FileReader(filename))) {
                String line;
                while ((line = br.readLine()) != null) {
                    String trimedLine = line.trim();
                    if(!trimedLine.equals("")) {
                        urlPatterns.add(trimedLine);
                        logger.info(trimedLine);
                    }
                }
            } catch (IOException e) {
                logger.warn("Couldn't load link filter patterns from file: "+filename);
            }
            return urlPatterns;
        }
        
    }
    
    public static class LinkWhiteList extends LinkMatcher {
        
        public LinkWhiteList(List<String> urlPatterns) {
            super(urlPatterns);
        }
        
        public LinkWhiteList(String filename) {
            super(filename);
        }
        
        public boolean accept(String link) {
            if(patterns == null || patterns.size()==0) {
                return true;
            }
            if(matches(link)) {
                return true;
            }
            return false;
        }
        
    }
    
    public static class LinkBlackList extends LinkMatcher {
        
        public LinkBlackList(String filename) {
            super(filename);
        }
        
        public LinkBlackList(List<String> urlPatterns) {
            super(urlPatterns);
        }
        
        public boolean accept(String link) {
            if(patterns == null || patterns.size()==0) {
                return true;
            }
            if(super.matches(link)) {
                return false;
            }
            return true;
        }
        
    }

}
