package focusedCrawler.link.frontier;

import java.net.URLDecoder;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import focusedCrawler.util.persistence.PersistentHashtable;
import focusedCrawler.util.persistence.Tuple;
import focusedCrawler.util.persistence.TupleIterator;


public class Frontier {

    protected PersistentHashtable<LinkRelevance> urlRelevance;
    protected Map<String, Integer> scope = null;
    private boolean useScope = false;

    public Frontier(String directory, int maxCacheUrlsSize, Map<String, Integer> scope) {
        
        this.urlRelevance = new PersistentHashtable<>(directory, maxCacheUrlsSize, LinkRelevance.class);
        
        if (scope == null) {
            this.useScope = false;
            this.scope = new HashMap<String, Integer>();
        } else {
            this.scope = scope;
            this.useScope = true;
        }
    }

    public Frontier(String directory, int maxCacheUrlsSize) {
        this(directory, maxCacheUrlsSize, null);
    }

    public void commit() {
        urlRelevance.commit();
    }

    public HashSet<String> visitedAuths() throws Exception {
        HashSet<String> result = new HashSet<String>();
        List<Tuple<LinkRelevance>> tuples = urlRelevance.getTable();
        for (Tuple<LinkRelevance> tuple : tuples) {
            double value = tuple.getValue().getRelevance();
            if (value < -200) {
                result.add(URLDecoder.decode(tuple.getKey(), "UTF-8"));
            }
        }
        return result;
    }

    public HashSet<String> visitedLinks() throws Exception {
        HashSet<String> result = new HashSet<String>();
        List<Tuple<LinkRelevance>> tuples = urlRelevance.getTable();
        for (Tuple<LinkRelevance> tuple : tuples) {
            double value = tuple.getValue().getRelevance();
            if (value < 0) {
                result.add(URLDecoder.decode(tuple.getKey(), "UTF-8"));
            }
        }
        return result;
    }

    public HashSet<String> unvisitedAuths() throws Exception {
        HashSet<String> result = new HashSet<String>();
        List<Tuple<LinkRelevance>> tuples = urlRelevance.getTable();
        for (Tuple<LinkRelevance> tuple : tuples) {
            double value = tuple.getValue().getRelevance();
            if (value > 200) {
                result.add(URLDecoder.decode(tuple.getKey(), "UTF-8"));
            }
        }
        return result;
    }

    public HashSet<String> visitedHubs() throws Exception {
        HashSet<String> result = new HashSet<String>();
        List<Tuple<LinkRelevance>> tuples = urlRelevance.getTable();
        for (Tuple<LinkRelevance> tuple : tuples) {
            double value = tuple.getValue().getRelevance();
            if (value > -200 && value < -100) {
                result.add(URLDecoder.decode(tuple.getKey(), "UTF-8"));
            }
        }
        return result;
    }

    public HashSet<String> unvisitedHubs() throws Exception {
        HashSet<String> result = new HashSet<String>();
        List<Tuple<LinkRelevance>> tuples = urlRelevance.getTable();
        for (Tuple<LinkRelevance> tuple : tuples) {
            double value = tuple.getValue().getRelevance();
            if (value > 100 && value < 200) {
                result.add(URLDecoder.decode(tuple.getKey(), "UTF-8"));
            }
        }
        return result;
    }

    public void update(LinkRelevance linkRelevance) {
        String url = linkRelevance.getURL().toString();
        LinkRelevance link = urlRelevance.get(url);
        if (link != null) {
            if (link.getRelevance() > 0) { // not visited url
                urlRelevance.put(url, linkRelevance);
            }
        }
    }

    /**
     * This method inserts a new link into the frontier
     * 
     * @param linkRelev
     * @return
     * @throws FrontierPersistentException
     */
    public boolean insert(LinkRelevance linkRelev) throws FrontierPersistentException {
        boolean inserted = false;
        String url = linkRelev.getURL().toString();
        Integer rel = exist(linkRelev);
        if (rel == null && url.toString().length() < 210) {
            urlRelevance.put(url, linkRelev);
            inserted = true;
        }

        return inserted;
    }

    /**
     * It verifies whether a given URL was already visited or does not belong to
     * the scope.
     * 
     * @param linkRelev
     * @return
     * @throws FrontierPersistentException
     */
    public Integer exist(LinkRelevance linkRelev) throws FrontierPersistentException {
        String url = linkRelev.getURL().toString();
        LinkRelevance resStr = urlRelevance.get(url);
        if (resStr != null) {
            return (int) resStr.getRelevance();
        } else {
            Integer result = new Integer(-1);
            if (useScope == true) {
                String host = linkRelev.getURL().getHost();
                if (scope.get(host) != null) {
                    result = null;
                }
            } else {
                result = null;
            }
            return result;
        }
    }

    /**
     * It deletes a URL from frontier (marks as visited).
     * 
     * @param linkRelevance
     * @throws FrontierPersistentException
     */
    public void delete(LinkRelevance linkRelevance) throws FrontierPersistentException {

        String url = linkRelevance.getURL().toString();
        if (exist(linkRelevance) != null) {
            // we don't want to delete the URL file, it is useful to avoid visiting an old url
            double relevance = linkRelevance.getRelevance();
            double negativeRelevance = relevance > 0 ? -1*relevance : relevance;
            urlRelevance.put(url, new LinkRelevance(linkRelevance.getURL(), negativeRelevance, linkRelevance.getType()));
        }
    }

    public void close() {
        urlRelevance.close();
    }

    public TupleIterator<LinkRelevance> iterator() {
        return urlRelevance.iterator();
    }

}
