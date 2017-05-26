package focusedCrawler.link.classifier;

import java.nio.file.Paths;

import focusedCrawler.util.string.StopList;
import focusedCrawler.util.string.StopListFile;


public class LinkClassifierFactory {

    private static StopList stoplist = StopListFile.DEFAULT;

    public static void setDefaultStoplist(StopList stoplist) {
        LinkClassifierFactory.stoplist = stoplist;
    }

    public static LinkClassifier create(String modelPath, String type) {
        switch (type) {
            case "LinkClassifierBreadthSearch":
                return new LinkClassifierBreadthSearch();
            case "LinkClassifierBaseline":
                return new LinkClassifierBaseline();
            case "LinkClassifierHub":
                return new LinkClassifierHub();
            case "LinkClassifierAuthority":
                return new LinkClassifierAuthority();
            case "LinkClassifierImpl":
                String featureFilePath = Paths.get(modelPath, "linkclassifier.features").toString();
                String modelFilePath = Paths.get(modelPath, "linkclassifier.model").toString();
                LNClassifier lnClassifier = LNClassifier.create(featureFilePath, modelFilePath, stoplist);
                return new LinkClassifierImpl(lnClassifier);
            case "MaxDepthLinkClassifier":
                return new MaxDepthLinkClassifier(1);
            default:
                throw new IllegalArgumentException("Unknown link classifier type: " + type);
        }
    }

}

