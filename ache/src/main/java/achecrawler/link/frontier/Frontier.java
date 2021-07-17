package achecrawler.link.frontier;

import java.net.URLDecoder;
import java.util.HashSet;
import java.util.List;

import crawlercommons.robots.SimpleRobotRules;
import achecrawler.util.persistence.PersistentHashtable;
import achecrawler.util.persistence.PersistentHashtable.DB;
import achecrawler.util.persistence.Tuple;
import achecrawler.util.persistence.TupleIterator;


public class Frontier {

    protected PersistentHashtable<LinkRelevance> urlRelevance;

    private final PersistentHashtable<SimpleRobotRules> robotRulesMap;

    public Frontier(String directory, int maxCacheUrlsSize, DB persistentHashtableBackend) {
        this.urlRelevance = new PersistentHashtable<>(directory, maxCacheUrlsSize,
                LinkRelevance.class, persistentHashtableBackend);
        this.robotRulesMap = new PersistentHashtable<>(directory + "_robots", maxCacheUrlsSize,
                SimpleRobotRules.class, persistentHashtableBackend);
    }

    public void commit() {
        urlRelevance.commit();
        robotRulesMap.commit();
    }

    /**
     * DEPRECATED: may cause OutOfMemoryError on large crawls. TODO: Provide an method that uses an
     * iterator, and/or load just a sample of the data.
     */
    @Deprecated
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

    public void visitedLinks(Visitor<LinkRelevance> visitor) throws Exception {
        urlRelevance.visitTuples((Tuple<LinkRelevance> tuple) -> {
            if (tuple.getValue().getRelevance() < 0) {
                visitor.visit(tuple.getValue());
            }
        });
    }

    /**
     * DEPRECATED: may cause OutOfMemoryError on large crawls. TODO: Provide an method that uses an
     * iterator, and/or load just a sample of the data.
     */
    @Deprecated
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

    /**
     * DEPRECATED: may cause OutOfMemoryError on large crawls. TODO: Provide an method that uses an
     * iterator, and/or load just a sample of the data.
     */
    @Deprecated
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

    /**
     * DEPRECATED: may cause OutOfMemoryError on large crawls. TODO: Provide an method that uses an
     * iterator, and/or load just a sample of the data.
     */
    @Deprecated
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
        if (linkRelev == null) {
            return false;
        }
        boolean inserted = false;
        String url = linkRelev.getURL().toString();
        Double relevance = exist(linkRelev);
        if (relevance == null) {
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
    public Double exist(LinkRelevance linkRelev) throws FrontierPersistentException {
        LinkRelevance link = urlRelevance.get(linkRelev.getURL().toString());
        return link == null ? null : link.getRelevance();
    }
    
    public LinkRelevance get(String url) throws FrontierPersistentException {
        return urlRelevance.get(url);
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
        robotRulesMap.close();
    }

    public TupleIterator<LinkRelevance> iterator() {
        return urlRelevance.iterator();
    }

    /**
     * Inserts the robot rules object into the HashMap
     * 
     * @param link
     * @param robotRules
     * @throws NullPointerException
     *             when either of the argument is null
     */
    public void insertRobotRules(LinkRelevance link, SimpleRobotRules robotRules) {
        if (link == null || robotRules == null) {
            throw new NullPointerException("Link argument or robot rules argument cannot be null");
        }
        String hostname = link.getURL().getHost();
        robotRulesMap.put(hostname, robotRules);
    }

    public boolean isDisallowedByRobots(LinkRelevance link) {
        String hostname = link.getURL().getHost();
        SimpleRobotRules rules = robotRulesMap.get(hostname);
        return rules != null && !rules.isAllowed(link.getURL().toString());
    }

}
