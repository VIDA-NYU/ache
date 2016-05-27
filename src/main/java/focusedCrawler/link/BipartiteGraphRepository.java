package focusedCrawler.link;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashSet;
import java.util.StringTokenizer;
import java.util.Vector;

import focusedCrawler.util.parser.BackLinkNeighborhood;
import focusedCrawler.util.parser.LinkNeighborhood;
import focusedCrawler.util.persistence.PersistentHashtable;
import focusedCrawler.util.persistence.Tuple;

public class BipartiteGraphRepository {

//	private PersistentHashtable hubs;
	
//	private PersistentHashtable auths;
	
	private PersistentHashtable<String> authGraph; 
	
	private PersistentHashtable<String> authID;

	private PersistentHashtable<String> hubGraph; 
	
	private PersistentHashtable<String> hubID;
	
	private PersistentHashtable<String> url2id;

	
	private final String separator = "###";
	
	private String authGraphDirectory = "data_backlinks/auth_graph";      
    private String urlIdDirectory = "data_backlinks/url";
    private String authIdDirectory = "data_backlinks/auth_id";
    private String hubIdDirectory = "data_backlinks/hub_id";
    private String hubGraphDirectory = "data_backlinks/hub_graph";

    public BipartiteGraphRepository(String dataPath) {
        int cacheSize = 10000;
        this.authGraph = new PersistentHashtable<>(dataPath + "/" + authGraphDirectory, cacheSize, String.class);
        this.url2id = new PersistentHashtable<>(dataPath + "/" + urlIdDirectory, cacheSize, String.class);
        this.authID = new PersistentHashtable<>(dataPath + "/" + authIdDirectory, cacheSize, String.class);
        this.hubID = new PersistentHashtable<>(dataPath + "/" + hubIdDirectory, cacheSize, String.class);
        this.hubGraph = new PersistentHashtable<>(dataPath + "/" + hubGraphDirectory, cacheSize, String.class);
    }
	
	public Tuple<String>[] getAuthGraph() throws Exception{
		return authGraph.getTableAsArray();
	}

	public Tuple<String>[] getHubGraph() throws Exception{
		return hubGraph.getTableAsArray();
	}
	
	public String getID(String url){
		return url2id.get(url);
	}
	
	public String getHubURL(String id) throws IOException{
		String url = hubID.get(id);
		if(url != null){
			String[] fields = url.split(":::");
			url = fields[0];
		}
		return url;
	}
	
	public String getAuthURL(String id){
		String url = authID.get(id);
		if(url != null){
			String[] fields = url.split(":::");
			url = fields[0];
		}
		return url;
	}

	public String[] getOutlinks(String id){
		String links = hubGraph.get(id);
		if(links != null){
			return links.split("###");	
		}else{
			return null;
		}
	}

	public String[] getBacklinks(String id){
		String links = authGraph.get(id);
		if(links != null){
			return links.split("###");	
		}else{
			return null;
		}

	}
	
	public LinkNeighborhood[] getLNs() throws Exception{
		Tuple<String>[] tuples = authID.getTableAsArray();
		LinkNeighborhood[] lns = new LinkNeighborhood[tuples.length];
		for (int i = 0; i < lns.length; i++) {
			String strln = tuples[i].getValue();
			if(strln != null){
				String[] fields = strln.split(":::");
				lns[i] = new LinkNeighborhood(new URL(fields[0]));
				if(fields.length > 1){
					lns[i].setAnchor(fields[1].split(" "));
					if(fields.length > 2){
						lns[i].setAround(fields[2].split(" "));
					}
				}
			}
		}
		return lns;
	}

	public LinkNeighborhood[] getBacklinkLN() throws Exception{
		Tuple<String>[] tuples = hubID.getTableAsArray();
		LinkNeighborhood[] lns = new LinkNeighborhood[tuples.length];
		for (int i = 0; i < lns.length; i++) {
			String strln = tuples[i].getValue();
			if(strln != null){
				String[] fields = strln.split(":::");
				lns[i] = new LinkNeighborhood(new URL(fields[0]));
				if(fields.length > 1){
					String title = fields[1];
					if(title != null){
						StringTokenizer tokenizer = new StringTokenizer(title," ");
						Vector<String> anchorTemp = new Vector<String>();
						while(tokenizer.hasMoreTokens()){
							 anchorTemp.add(tokenizer.nextToken());
			   		  	}
			   		  	String[] aroundArray = new String[anchorTemp.size()];
			   		  	anchorTemp.toArray(aroundArray);
			   		  	lns[i].setAround(aroundArray);
					}
				}
			}
		}
		return lns;
	}

	
	public LinkNeighborhood getBacklinkLN(URL url) throws MalformedURLException{
		LinkNeighborhood ln = null;
		String urlId = url2id.get(url.toString());
		if(urlId != null){
			String strln = hubID.get(urlId);
			if(strln != null){
				String[] fields = strln.split(":::");
				ln = new LinkNeighborhood(new URL(fields[0]));
				if(fields.length > 1){
					String title = fields[1];
					if(title != null){
						StringTokenizer tokenizer = new StringTokenizer(title," ");
						Vector<String> anchorTemp = new Vector<String>();
						while(tokenizer.hasMoreTokens()){
							 anchorTemp.add(tokenizer.nextToken());
			   		  	}
			   		  	String[] aroundArray = new String[anchorTemp.size()];
			   		  	anchorTemp.toArray(aroundArray);
			   		  	ln.setAround(aroundArray);
					}
				}
			}
		}
		return ln;
	}

	
	public LinkNeighborhood getLN(URL url) throws MalformedURLException{
		LinkNeighborhood ln = null;
		URL normalizedURL = url;//new URL(url.getProtocol(), url.getHost(), "/"); 
		String urlId = url2id.get(normalizedURL.toString());
		if(urlId != null){
			String strln = authID.get(urlId);
			ln = parseString(strln);
		}
		return ln;
	}
	
	
	public LinkNeighborhood[] getOutlinks(URL url) throws IOException{
		String urlId = url2id.get(url.toString());
		if(urlId == null){
			return null;
		} else {
			String[] linkIds = hubGraph.get(urlId).split("###");
			LinkNeighborhood[] lns = new LinkNeighborhood[linkIds.length];
			for (int i = 0; i < lns.length; i++) {
				String strln = authID.get(linkIds[i]);
				if(strln != null){
					String[] fields = strln.split(":::");
					LinkNeighborhood ln = new LinkNeighborhood(new URL(fields[0]));
					lns[i] = ln;
					if(fields.length > 1){
						ln.setAnchor(fields[1].split(" "));
						if(fields.length > 2){
							ln.setAround(fields[2].split(" "));	
						}
					}
						
				}
			}
			return lns;
		}
	}
	
	/**
	 * This method retrieves the the backlinks of a given url.
	 * @param url
	 * @return
	 * @throws IOException
	 */

	public BackLinkNeighborhood[] getBacklinks(URL url) throws IOException {
		URL normalizedURL = new URL(url.getProtocol(), url.getHost(), "/"); 
		String urlId = url2id.get(normalizedURL.toString());
		if(urlId == null){
			return null;
		}
		String strLinks = authGraph.get(urlId);
		if(strLinks == null){
			return null;
		} else {
			Vector<BackLinkNeighborhood> tempBacklinks = new Vector<BackLinkNeighborhood> (); 
			String[] backlinkIds = strLinks.split("###");
			for (int i = 0; i < backlinkIds.length; i++) {
				String url_title = hubID.get(backlinkIds[i]);
				if(url_title != null){
					BackLinkNeighborhood bln = new BackLinkNeighborhood();
					String[] fields = url_title.split(":::");
					bln.setLink(fields[0]);
					if(fields.length > 1){
						bln.setTitle(fields[1]);	
					}
					tempBacklinks.add(bln);
				}
			}
			BackLinkNeighborhood[] blns = new BackLinkNeighborhood[tempBacklinks.size()];
			tempBacklinks.toArray(blns);
			return blns;
		}
	}


	public LinkNeighborhood[] getBacklinksLN(URL url) throws IOException {
		String urlId = url2id.get(url.toString());
		if(urlId == null){
			return null;
		}
		String strLinks = authGraph.get(urlId);
		if(strLinks == null){
			return null;
		} else {
			Vector<LinkNeighborhood> tempLNs = new Vector<LinkNeighborhood> (); 
			String[] linkIds = strLinks.split("###");
			for (int i = 0; i < linkIds.length; i++) {
				String lnStr = authID.get(linkIds[i]);
				LinkNeighborhood ln = parseString(lnStr);
				if(ln != null){
					tempLNs.add(ln);
				}
			}
			LinkNeighborhood[] lns = new LinkNeighborhood[tempLNs.size()];
			tempLNs.toArray(lns);
			return lns;
		}
	}

	/**
	 * Insert outlinks from hubs 
	 * @param page
	 */
	public void insertOutlinks(URL url, LinkNeighborhood[] lns){
		
		String urlId = getId(url.toString());
		String strCurrentLinks = hubGraph.get(urlId);
		HashSet<String> currentLinks = parseRecordForwardLink(strCurrentLinks);
		StringBuffer buffer = new StringBuffer();	
		for (int i = 0; i < lns.length; i++) {
			if(lns[i] != null){
				String lnURL = lns[i].getLink().toString();
				String id = getId(lnURL);
				if(!currentLinks.contains(id)){
					String ln = authID.get(id);
					if(ln == null){
						authID.put(id, lnURL + ":::" + lns[i].getAnchorString() + ":::" + lns[i].getAroundString());
					}
					buffer.append(id);
					buffer.append(separator);
					currentLinks.add(id);
				}
				String strLinks = authGraph.get(id);
				HashSet<String> tempCurrentLinks = parseRecordBacklink(strLinks);
				if(!tempCurrentLinks.contains(urlId)){
					if(tempCurrentLinks.isEmpty()){
						strLinks = urlId + separator;
					}else{
						strLinks = strLinks + urlId + separator;
					}
					String url_string = hubID.get(id);
					if(url_string == null){
						hubID.put(id, lnURL + ":::");
					}
					authGraph.put(id, strLinks);
				}
			}
		}
		if(strCurrentLinks == null){
			strCurrentLinks = buffer.toString();
		} else {
			strCurrentLinks =  strCurrentLinks + buffer.toString();
		}
		if(!strCurrentLinks.equals("")){
			hubGraph.put(urlId, strCurrentLinks);	
		}
	}
	
	
	/**
	 * Insert backlinks from authorities
	 * @param page
	 * @throws IOException 
	 */
	public void insertBacklinks(URL url, BackLinkNeighborhood[] links) throws IOException{
		String urlId = getId(url.toString());
		String strCurrentLinks = authGraph.get(urlId);
		HashSet<String> currentLinks = parseRecordBacklink(strCurrentLinks);
		StringBuffer buffer = new StringBuffer();
		for (int i = 0; i < links.length; i++) {
			String id = getId(links[i].getLink());
			if(!currentLinks.contains(id)){
				String url_string = hubID.get(id);
				if(url_string == null){
					hubID.put(id, links[i].getLink() + ":::" + links[i].getTitle());
				}
				buffer.append(id);
				buffer.append(separator);
				currentLinks.add(id);
			}
			
			String strLinks = hubGraph.get(id);
			HashSet<String> tempCurrentLinks = parseRecordForwardLink(strLinks);
			if(!tempCurrentLinks.contains(urlId)){
				if(tempCurrentLinks.isEmpty()){
					strLinks = urlId + separator;
				}else{
					strLinks = strLinks + urlId + separator;
				}
				hubGraph.put(id, strLinks);
			}
		}
		if(strCurrentLinks == null){
			strCurrentLinks = buffer.toString();
		}else{
			strCurrentLinks =  strCurrentLinks + buffer.toString();
		}
		authGraph.put(urlId, strCurrentLinks);	
	}

	private String getId(String url){
		String id = url2id.get(url);
		if(id == null){
			String maxId = url2id.get("MAX");
			if(maxId == null){
				maxId = "0";
			}
			int newId = Integer.parseInt(maxId) + 1;
			id = newId+"";
			url2id.put(url, id);
			url2id.put("MAX", id);
		}
		return id;
	}

	public void commit(){
		url2id.commit();
		authGraph.commit();
		authID.commit();
		hubID.commit();
		hubGraph.commit();
	}
	
	public void close(){
	    this.commit();
        url2id.close();
        authGraph.close();
        authID.close();
        hubID.close();
        hubGraph.close();
    }
	
	private HashSet<String> parseRecordBacklink(String strLinks){
		HashSet<String> currentLinks = new HashSet<String>();
		if(strLinks != null){
			String[] links = strLinks.split("###");
			for (int i = 0; i < links.length; i++) {
				currentLinks.add(links[i]);
			}
		}
		return currentLinks;
	}

	
	private HashSet<String> parseRecordForwardLink(String strLinks){
		HashSet<String> currentLinks = new HashSet<String>();
		if(strLinks != null){
			String[] linkIds = strLinks.split("###");
			for (int i = 0; i < linkIds.length; i++) {
				currentLinks.add(linkIds[i]);					
			}
		}
		return currentLinks;
	}
	
	private LinkNeighborhood parseString(String lnStr) throws MalformedURLException{
		LinkNeighborhood ln = null;
		if(lnStr != null){
			String[] fields = lnStr.split(":::");
			ln = new LinkNeighborhood(new URL(fields[0]));
			if(fields.length > 1){
				ln.setAnchor(fields[1].split(" "));
				if(fields.length > 2){
					ln.setAround(fields[2].split(" "));	
				}
			}
		}
		return ln;
	}
	
	
}
