package focusedCrawler.link.frontier;

import java.io.IOException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Vector;

import net.sf.ehcache.CacheException;
import focusedCrawler.util.LinkRelevance;
import focusedCrawler.util.persistence.PersistentHashtable;
import focusedCrawler.util.vsm.VSMElement;

public class BaselineLinkSelector implements LinkSelectionStrategy {
    
    private final PersistentHashtable urlRelevance;
    private final HashMap<String, Integer> hostPages;
    private final boolean useScope;
    private final boolean random;
    
    public BaselineLinkSelector(PersistentHashtable urlRelevance) {
        this(urlRelevance, null, false);
    }
    
    public BaselineLinkSelector(PersistentHashtable urlRelevance,
                                HashMap<String,Integer> scope)  {
        this(urlRelevance, scope, false);
    }
    
    public BaselineLinkSelector(PersistentHashtable urlRelevance,
                                HashMap<String,Integer> scope,
                                boolean random)  {
        this.urlRelevance = urlRelevance;
        this.random = random;
        if(scope != null) {
            this.useScope = true;
            this.hostPages = scope;
        } else {
            this.useScope = false;
            this.hostPages = new HashMap<String, Integer>();
        }
    }

    @Override
    public LinkRelevance[] select(int numberOfLinks)  {
        if (useScope) {
            return siteSelection(numberOfLinks);
        } else {
            if (random) {
                return randomSelection(numberOfLinks);
            } else {
                return nonRandomSelection(numberOfLinks);
            }
        }
    }

    public LinkRelevance[] siteSelection(int numberOfLinks) {
        LinkRelevance[] result = null;
        try {
            Iterator<VSMElement> keys = urlRelevance.orderedSet().iterator();
            Vector<LinkRelevance> tempList = new Vector<LinkRelevance>();
            HashMap<String, Integer> hostCount = new HashMap<String, Integer>();
            for (int count = 0; count < numberOfLinks && keys.hasNext();) {
                VSMElement elem = keys.next();
                String key = elem.getWord();
                String url = URLDecoder.decode(key, "UTF-8");
                if (url != null && !url.isEmpty()) {
                    String host = filterServer(new URL(url).getHost());
                    Integer intCount = hostCount.get(host);
                    if (intCount == null) {
                        hostCount.put(host, new Integer(0));
                    }
                    int numOfLinks = hostCount.get(host).intValue();
                    if (numOfLinks < numberOfLinks / hostPages.size()) {
                        hostCount.put(host, new Integer(numOfLinks + 1));
                        Integer relevInt = new Integer((int) elem.getWeight());
                        if (relevInt != null && relevInt.intValue() != -1) {
                            int relev = relevInt.intValue();
                            if (relev > 0) {
                                LinkRelevance linkRel = new LinkRelevance(new URL(url), relev);
                                tempList.add(linkRel);
                                count++;
                            }
                        }
                    }
                }
            }
            result = new LinkRelevance[tempList.size()];
            tempList.toArray(result);
            System.out.println(">> TOTAL LOADED: " + result.length);
        } catch (IOException ex) {
            ex.printStackTrace();
        } catch (CacheException ex) {
            ex.printStackTrace();
        }
        return result;
    }

    public LinkRelevance[] nonRandomSelection(int numberOfLinks) {
        LinkRelevance[] result = null;
        int[] classLimits = new int[] { 500, 1000, 5000 };
        int[] classCount = new int[classLimits.length];
        try {
            Iterator<VSMElement> keys = urlRelevance.orderedSet().iterator();
            Vector<LinkRelevance> tempList = new Vector<LinkRelevance>();
            int count = 0;
            for (int i = 0; count < numberOfLinks && keys.hasNext(); i++) {
                VSMElement elem = keys.next();
                String key = elem.getWord();
                String url = URLDecoder.decode(key, "UTF-8");
                if (url != null) {
                    Integer relevInt = new Integer((int) elem.getWeight());
                    if (relevInt != null && relevInt.intValue() != -1) {
                        int relev = relevInt.intValue();
                        if (relev > 0) {
                            int index = relev / 100;
                            if (classCount[index] < classLimits[index]) {
                                if (relev == 299 || i % 5 == 0) {
                                    LinkRelevance linkRel = new LinkRelevance(new URL(url), relev);
                                    // System.out.println(url + ":" + relev);
                                    tempList.add(linkRel);
                                    count++;
                                    classCount[index]++;
                                }
                            }
                        }
                    }
                }
            }
            result = new LinkRelevance[tempList.size()];
            tempList.toArray(result);
            System.out.println(">> TOTAL LOADED: " + result.length);
        } catch (IOException ex) {
            ex.printStackTrace();
        } catch (CacheException ex) {
            ex.printStackTrace();
        }
        return result;
    }

    /**
     * This method returns the next links to be visited by the crawler
     * 
     * @param numberOfLinks
     * @return
     * @throws FrontierPersistentException
     */
    public LinkRelevance[] randomSelection(int numberOfLinks) {
        HashMap<Integer, Integer> queue = new HashMap<Integer, Integer>();
        LinkRelevance[] result = null;
        // int[] classCount = new int[3];
        // int[] classLimits = new int[]{3000,2000,1000};
        try {
            Iterator<String> keys = urlRelevance.getKeys();

            Vector<LinkRelevance> tempList = new Vector<LinkRelevance>();
            int count = 0;
            for (; count < numberOfLinks && keys.hasNext();) {
                String key = ((String) keys.next());
                String url = URLDecoder.decode(key, "UTF-8");
                // System.out.println(url);
                if (url != null) {
                    // System.out.println("$$$"+(String)urlRelevance.get(url));
                    Integer relevInt = new Integer((String) urlRelevance.get(url));
                    if (relevInt != null) {
                        int relev = relevInt.intValue();
                        if (relev > 0) {
                            // int index = relev/100;
                            // if(classCount[index] < classLimits[index]){
                            Integer numOccur = ((Integer) queue.get(relevInt));
                            int numOccurInt = 0;
                            if (numOccur != null) {
                                numOccurInt++;
                            } else {
                                numOccurInt = 1;
                            }
                            queue.put(relevInt, new Integer(numOccurInt));
                            LinkRelevance linkRel = new LinkRelevance(new URL(url), relev);
                            tempList.add(linkRel);
                            count++;
                            // classCount[index]++;
                            // }
                        }
                    }
                }
            }

            result = new LinkRelevance[tempList.size()];
            tempList.toArray(result);
            System.out.println(">> TOTAL LOADED: " + result.length);
            queue.clear();
        } catch (IOException ex) {
            ex.printStackTrace();
        } catch (CacheException ex) {
            ex.printStackTrace();
        }

        return result;
    }
    
    private String filterServer(String server){
        if(server.lastIndexOf(".") != -1){
            String serverTemp = server.substring(0,server.lastIndexOf("."));
            int index = serverTemp.lastIndexOf(".");
            if(index != -1){
                server = server.substring(index+1);
            }
        }
        return server;
    }

}
