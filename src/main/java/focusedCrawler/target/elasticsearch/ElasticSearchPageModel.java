package focusedCrawler.target.elasticsearch;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Date;

import com.google.common.net.InternetDomainName;

import de.l3s.boilerpipe.BoilerpipeProcessingException;
import de.l3s.boilerpipe.extractors.DefaultExtractor;
import focusedCrawler.target.TargetModel;
import focusedCrawler.util.Page;
import focusedCrawler.util.Target;
import focusedCrawler.util.parser.PaginaURL;

public class ElasticSearchPageModel {

    private String domain;
    private String url;
    private String title;
    private String text;
    private Date retrieved;
    private String[] words;
    private String[] wordsMeta;
    private String topPrivateDomain;
    
    public ElasticSearchPageModel() {
        // mandatory for object unserialization
    }

    public ElasticSearchPageModel(Target target) {
        Page page = (Page) target;

        this.url = target.getIdentifier();
        this.retrieved = new Date();
        this.words = page.getPageURL().palavras();
        this.wordsMeta = page.getPageURL().palavrasMeta();
        this.title = page.getPageURL().titulo();
        this.domain = page.getDomainName();

        try {
            this.text = DefaultExtractor.getInstance().getText(page.getContent());
        } catch (BoilerpipeProcessingException e) {
            this.text = "";
        }

        InternetDomainName domainName = InternetDomainName.from(page.getDomainName());
        if (domainName.isUnderPublicSuffix()) {
            this.topPrivateDomain = domainName.topPrivateDomain().toString();
        } else {
            this.topPrivateDomain = domainName.toString();
        }
    }

    public ElasticSearchPageModel(TargetModel model) {
        this.url = model.url;
        this.retrieved = new Date(model.timestamp * 1000);

        URL url;
        try {
            url = new URL(model.url);
        } catch (MalformedURLException e) {
            throw new IllegalArgumentException("page has an invalid URL: " + model.url);
        }

        String raw_content = (String) model.response.get("body");
        Page page = new Page(url, raw_content);
        PaginaURL pageURL = new PaginaURL(url, 0, 0, raw_content.length(), raw_content, null);
        PaginaURL pageParser = new PaginaURL(page.getURL(), 0, 0, page.getContent().length(),
                page.getContent(), null);
        page.setPageURL(pageParser);

        this.url = model.url;
        this.retrieved = new Date();
        this.words = pageURL.palavras();
        this.wordsMeta = pageURL.palavrasMeta();
        this.title = pageURL.titulo();
        this.domain = url.getHost();

        try {
            this.text = DefaultExtractor.getInstance().getText(page.getContent());
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

}