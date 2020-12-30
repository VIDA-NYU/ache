package achecrawler.tools;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import achecrawler.target.model.TargetModelCbor;
import achecrawler.util.parser.PaginaURL;

@JsonInclude(Include.NON_NULL)
public class MemexCrawlSchema {
    
    public String url;

    public long timestamp;
    public String team;
    public String crawler;
    public String raw_content;
    public String content_type;
    public List<String> images;
//    public List<String> videos;
//    public CrawlData crawl_data = new CrawlData();
    
    public MemexCrawlSchema(String url, long timestamp, String team, String crawler,
                            String raw_content, String content_type, List<String> images) {
        this.url = url;
        this.timestamp = timestamp;
        this.team = team;
        this.crawler = crawler;
        this.raw_content = raw_content;
        this.content_type = content_type;
        this.images = images;
    }
    
    public MemexCrawlSchema(TargetModelCbor model) {
        this.url = model.url;
        this.timestamp = model.timestamp*1000;
        this.team = "NYU";
        this.crawler = "ACHE";
        this.content_type = "text/html";
        this.raw_content = model.response.get("body").toString();
        URL url;
        try {
            url = new URL(model.url);
        } catch (MalformedURLException e) {
            throw new IllegalArgumentException("page has an invalid URL: "+model.url);
        }
        PaginaURL pageParser = new PaginaURL(url,this.raw_content);
        
        this.images = new ArrayList<String>(pageParser.getImages());
//        this.crawl_data.html_title = pageParser.titulo();
    }
    
    static class CrawlData {
        public String html_title;
    }
    
}
