package focusedCrawler.util;

import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class LinkFilter {

    private LinkWhiteList whitelist;
    private LinkBlackList blacklist;

    public LinkFilter(String configPath) {
        this(new LinkWhiteList(Paths.get(configPath, "/link_whitelist.txt").toString()),
             new LinkBlackList(Paths.get(configPath,"/link_blacklist.txt").toString()));
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
    
    public static class LinkWhiteList extends RegexMatcher {
        
        public LinkWhiteList(List<String> urlPatterns) {
            super(urlPatterns);
        }
        
        public LinkWhiteList(String filename) {
            super(filename);
        }
        
        public boolean accept(String link) {
            if(patterns == null || patterns.isEmpty()) {
                return true;
            }
            if(matches(link)) {
                return true;
            }
            return false;
        }
        
    }
    
    public static class LinkBlackList extends RegexMatcher {
        
        public LinkBlackList(String filename) {
            super(filename);
        }
        
        public LinkBlackList(List<String> urlPatterns) {
            super(urlPatterns);
        }
        
        public boolean accept(String link) {
            if(patterns == null || patterns.isEmpty()) {
                return true;
            }
            if(super.matches(link)) {
                return false;
            }
            return true;
        }
        
    }

}
