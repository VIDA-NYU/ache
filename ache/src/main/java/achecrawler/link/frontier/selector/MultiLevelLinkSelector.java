package achecrawler.link.frontier.selector;

import java.util.ArrayList;
import java.util.List;

import achecrawler.link.frontier.LinkRelevance;

public class MultiLevelLinkSelector implements LinkSelector {

    private final int[] classLimits = new int[] {10000, 20000, 30000};
    private final int[] countTopClass = new int[classLimits.length];

    private int numberOfLinks;
    private List<LinkRelevance> links;
    private int[] classCount = new int[classLimits.length];

    @Override
    public void startSelection(int numberOfLinks) {
        this.numberOfLinks = numberOfLinks;
        this.links = new ArrayList<LinkRelevance>();
        this.classCount = new int[classLimits.length];
    }

    @Override
    public void evaluateLink(LinkRelevance linkRelevance) {
        if (links.size() >= numberOfLinks) {
            return;
        }
        int relev = (int) linkRelevance.getRelevance();
        if (relev > 0) {
            int index = relev / 100;
            if (classCount[index] < classLimits[index]) {
                boolean insert = false;
                if (index == 2) { // top class
                    if (relev >= 280 && countTopClass[2] < 15000) {
                        insert = true;
                        countTopClass[2]++;
                    }
                    if (relev >= 250 && relev < 280 && countTopClass[1] < 10000) {
                        insert = true;
                        countTopClass[1]++;
                    }
                    if (relev > 200 && relev < 250 && countTopClass[0] < 5000) {
                        insert = true;
                        countTopClass[0]++;
                    }
                    if (insert) {
                        links.add(linkRelevance);
                        classCount[index]++;
                    }
                } else {
                    links.add(linkRelevance);
                    classCount[index]++;
                }
            }
        }
    }

    @Override
    public List<LinkRelevance> getSelectedLinks() {
        List<LinkRelevance> selectedLinks = links;
        this.links = null; // clean-up references
        return selectedLinks;
    }

}
