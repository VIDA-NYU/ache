package achecrawler.target.model;

import java.net.URL;
import java.util.Date;
import java.util.List;
import java.util.Map;

import com.google.common.net.InternetDomainName;

import de.l3s.boilerpipe.extractors.DefaultExtractor;
import achecrawler.link.frontier.LinkRelevance;
import achecrawler.util.Urls;
import achecrawler.util.parser.PaginaURL;

public class TargetModelElasticSearch {

    private String domain;
    private String url;
    private String title;
    private String text;
    private Date retrieved;
    private String[] words;
    private String[] wordsMeta;
    private String topPrivateDomain;
    private String html;
    private Map<String, List<String>> responseHeaders;
    private String isRelevant;
    private double relevance;
    private String crawlerId;

    public TargetModelElasticSearch() {
        // mandatory for object deserialization
    }

    public TargetModelElasticSearch(Page page) {
        this.url = page.getURL().toString();
        this.retrieved = page.getFetchTime() > 0 ? new Date(page.getFetchTime()) : new Date();
        this.domain = page.getDomainName();
        this.responseHeaders = page.getResponseHeaders();
        this.topPrivateDomain = LinkRelevance.getTopLevelDomain(page.getDomainName());
        this.crawlerId = page.getCrawlerId();
        this.isRelevant = page.getTargetRelevance().isRelevant() ? "relevant" : "irrelevant";
        if (page.isHtml()) {
            String contentAsString = page.getContentAsString();
            this.html = contentAsString;
            ParsedData parsedData = page.getParsedData();
            if (parsedData != null) {
                this.words = parsedData.getWords();
                this.wordsMeta = parsedData.getWordsMeta();
                this.title = parsedData.getTitle();
            }
            if (page.getTargetRelevance() != null) {
                this.relevance = page.getTargetRelevance().getRelevance();
            }
            if (contentAsString != null) {
                try {
                    this.text = DefaultExtractor.getInstance().getText(contentAsString);
                } catch (Exception e) {
                    this.text = "";
                }
            }
        }
    }

    public TargetModelElasticSearch(TargetModelCbor model) {

        URL url = Urls.toJavaURL(model.url);
        String rawContent = (String) model.response.get("body");

        Page page = new Page(url, rawContent);
        page.setParsedData(new ParsedData(new PaginaURL(url, rawContent)));

        this.html = rawContent;
        this.url = model.url;
        this.retrieved = new Date(model.timestamp * 1000);
        this.words = page.getParsedData().getWords();
        this.wordsMeta = page.getParsedData().getWordsMeta();
        this.title = page.getParsedData().getTitle();
        this.domain = url.getHost();

        try {
            this.text = DefaultExtractor.getInstance().getText(page.getContentAsString());
        } catch (Exception e) {
            this.text = "";
        }

        InternetDomainName domainName = InternetDomainName.from(page.getDomainName());
        if (domainName.isUnderPublicSuffix()) {
            this.topPrivateDomain = domainName.topPrivateDomain().toString();
        } else {
            this.topPrivateDomain = domainName.toString();
        }
    }

    public String getDomain() {
        return domain;
    }

    public void setDomain(String domain) {
        this.domain = domain;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public Date getRetrieved() {
        return retrieved;
    }

    public void setRetrieved(Date retrieved) {
        this.retrieved = retrieved;
    }

    public String[] getWords() {
        return words;
    }

    public void setWords(String[] words) {
        this.words = words;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String[] getWordsMeta() {
        return wordsMeta;
    }

    public void setWordsMeta(String[] wordsMeta) {
        this.wordsMeta = wordsMeta;
    }

    public String getTopPrivateDomain() {
        return topPrivateDomain;
    }

    public void setTopPrivateDomain(String topPrivateDomain) {
        this.topPrivateDomain = topPrivateDomain;
    }

    public String getHtml() {
        return html;
    }

    public void setHtml(String html) {
        this.html = html;
    }

    public String getIsRelevant() {
        return isRelevant;
    }

    public void setIsRelevant(String isRelevant) {
        this.isRelevant = isRelevant;
    }

    public double getRelevance() {
        return relevance;
    }

    public void setRelevance(double relevance) {
        this.relevance = relevance;
    }

    public Map<String, List<String>> getResponseHeaders() {
        return responseHeaders;
    }

    public void setResponseHeaders(Map<String, List<String>> responseHeaders) {
        this.responseHeaders = responseHeaders;
    }

    public String getCrawlerId() {
        return crawlerId;
    }

}
