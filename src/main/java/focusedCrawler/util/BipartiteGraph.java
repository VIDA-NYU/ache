package focusedCrawler.util;

import java.io.IOException;
import java.net.URL;
import java.util.Iterator;

import focusedCrawler.util.cache.CacheException;
import focusedCrawler.util.download.UniversalDownloader;
import focusedCrawler.util.parser.PaginaURL;
import focusedCrawler.util.persistence.PersistentHashtable;

public class BipartiteGraph {

	private PersistentHashtable hubs;
	
	private PersistentHashtable auths; 
	
	private final String separator = "###";
	
	public BipartiteGraph(PersistentHashtable auths){
		this.auths = auths;
	}

	public BipartiteGraph(PersistentHashtable auths, PersistentHashtable hubs){
		this.auths = auths;
		this.hubs = hubs;
	}

	
	public void insertLinks(URL url, URL[] links, boolean hub){
		StringBuffer buffer = new StringBuffer();  
		for (int i = 0; i < links.length; i++) {
			if(!links[i].toString().endsWith("pdf") && !links[i].toString().contains("mailto:") 
					&& !links[i].toString().contains("javascript") && !links[i].toString().endsWith("css")
					&& !links[i].toString().endsWith("jpg") && !links[i].toString().endsWith("gif")
					&& !links[i].toString().endsWith(".ico")){
				if(!hub){
					buffer.append(links[i].toString());
					buffer.append(separator);

					String strLinks = hubs.get(links[i].toString());
					if(strLinks == null){
						strLinks = "";
						strLinks = strLinks + url.getHost() + separator;
					}else{
						String[] tempHosts = strLinks.split(separator);
						boolean contains = false;
						for (int j = 0; j < tempHosts.length && !contains; j++) {
							if(url.getHost().equals(tempHosts[j])){
								contains = true;
							}
						}
						if(!contains){
							strLinks = strLinks + links[i].toString() + separator;
						}
					}
					hubs.put(links[i].toString(), strLinks);
				}else{
					buffer.append(links[i].getHost());
					buffer.append(separator);
					String strLinks = auths.get(links[i].getHost());
					if(strLinks == null){
						strLinks = "";
						strLinks = strLinks + links[i].toString() + separator;
					}else{
						String[] tempLinks = strLinks.split(separator);
						boolean contains = false;
						for (int j = 0; j < tempLinks.length && !contains; j++) {
							if(links[i].toString().equals(tempLinks[j])){
								contains = true;
							}
						}
						if(!contains){
							strLinks = strLinks + links[i].toString() + separator;
						}
					}
					auths.put(links[i].getHost(), strLinks);
				}
			}
			if(!hub){
				auths.put(url.getHost(), buffer.toString());
			}else{
				hubs.put(url.toString(), buffer.toString());	
			}
			auths.commit();
			hubs.commit();
		}

	}

	public void loadHubs() throws CacheException, IOException{
		Iterator<String> iter = auths.getKeys();
		while(iter.hasNext()){
			String url = iter.next();
			System.out.println("URL:" + url);
			String strBacklinks = auths.get(url);
			String[] backlinks = strBacklinks.split(separator);
			for (int i = 0; i < backlinks.length; i++) {
				System.out.println("Downloading:" + backlinks[i]);
				try{
					Page page = UniversalDownloader.downloadPage(backlinks[i]);
					PaginaURL parsedPage = new PaginaURL(new URL(backlinks[i]), 0, 0,page.getContent().length(),	page.getContent(), null);
					URL[] links = parsedPage.links();
					insertLinks(new URL("http://" +url),links,false);
				}catch(Exception ex){
					
				}
				
			}
		}
	}
	
	public static void main(String[] args) {
		try {
			PersistentHashtable auths = new PersistentHashtable(args[0], 100000);
			PersistentHashtable hubs = new PersistentHashtable(args[1], 100000);
			BipartiteGraph bg = new BipartiteGraph(auths,hubs);
			bg.loadHubs();
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
