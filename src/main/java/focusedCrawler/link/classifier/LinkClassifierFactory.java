package focusedCrawler.link.classifier;

import java.nio.file.Paths;

import focusedCrawler.link.classifier.builder.LinkNeighborhoodWrapper;
import focusedCrawler.util.string.StopList;
import focusedCrawler.util.string.StopListFile;
import weka.classifiers.Classifier;
import weka.core.Instances;


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
                throw new IllegalArgumentException("Unknown link classifier type: "+type);
        }
    }

    public static LinkClassifier createLinkClassifierImpl(String[] attributes, String[] classValues,
                                                          Classifier classifier, String className,
                                                          int levels) {

        LinkNeighborhoodWrapper wrapper = new LinkNeighborhoodWrapper(attributes, stoplist);

        weka.core.FastVector vectorAtt = new weka.core.FastVector();
        for (int i = 0; i < attributes.length; i++) {
            vectorAtt.addElement(new weka.core.Attribute(attributes[i]));
        }
        weka.core.FastVector classAtt = new weka.core.FastVector();
        for (int i = 0; i < classValues.length; i++) {
            classAtt.addElement(classValues[i]);
        }
        vectorAtt.addElement(new weka.core.Attribute("class", classAtt));
        Instances insts = new Instances("link_classification", vectorAtt, 1);
        insts.setClassIndex(attributes.length);

        LinkClassifier linkClassifier = null;
        if (className.indexOf("LinkClassifierImpl") != -1) {
            LNClassifier lnClassifier = new LNClassifier(classifier, insts, wrapper, attributes);
            linkClassifier = new LinkClassifierImpl(lnClassifier);
        }
        if (className.indexOf("LinkClassifierAuthority") != -1) {
            linkClassifier = new LinkClassifierAuthority(classifier, insts, wrapper, attributes);
        }
        if (className.indexOf("LinkClassifierHub") != -1) {
            linkClassifier = new LinkClassifierHub(classifier, insts, wrapper, attributes);
        }
        return linkClassifier;
    }

}

