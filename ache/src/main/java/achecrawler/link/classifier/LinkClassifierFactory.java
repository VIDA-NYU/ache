package achecrawler.link.classifier;

import java.nio.file.Paths;

import achecrawler.link.LinkStorageConfig;
import achecrawler.util.string.Stopwords;


public class LinkClassifierFactory {

    private static Stopwords stopwords = Stopwords.DEFAULT;

    public static void setDefaultStopwords(Stopwords stopwords) {
        LinkClassifierFactory.stopwords = stopwords;
    }

    public static LinkClassifier create(String modelPath, LinkStorageConfig config) {
        String typeOfClassifier = config.getTypeOfClassifier();
        switch (typeOfClassifier) {
            case "LinkClassifierRandom":
                return new LinkClassifierRandom();
            case "LinkClassifierBreadthSearch":
                return new LinkClassifierBreadthSearch();
            case "LinkClassifierBaseline":
                return new LinkClassifierBaseline();
            case "LinkClassifierHub":
                return new LinkClassifierHub();
            case "LinkClassifierAuthority":
                return new LinkClassifierAuthority();
            case "LinkClassifierImpl":
                if (modelPath == null || modelPath.isEmpty()) {
                    throw new IllegalArgumentException(
                            "parameter --modelDir can not be empty when using LinkClassifierImpl");
                }
                String featureFilePath = Paths.get(modelPath, "linkclassifier.features").toString();
                String modelFilePath = Paths.get(modelPath, "linkclassifier.model").toString();
                LNClassifier lnClassifier = LNClassifier.create(featureFilePath, modelFilePath,
                    stopwords);
                return new LinkClassifierImpl(lnClassifier);
            case "MaxDepthLinkClassifier":
                return new MaxDepthLinkClassifier(config.getMaxDepth());
            default:
                throw new IllegalArgumentException("Unknown link classifier type: " + typeOfClassifier);
        }
    }

}

