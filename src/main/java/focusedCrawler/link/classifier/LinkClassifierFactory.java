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
        
        String featureFilePath = Paths.get(modelPath, "linkclassifier.features").toString();
        String modelFilePath = Paths.get(modelPath, "linkclassifier.model").toString();

        LinkClassifier linkClassifier = null;
        if (type.indexOf("LinkClassifierBreadthSearch") != -1) {
            linkClassifier = new LinkClassifierBreadthSearch();
        }
        if (type.indexOf("LinkClassifierBaseline") != -1) {
            linkClassifier = new LinkClassifierBaseline();
        }
        if (type.indexOf("LinkClassifierHub") != -1) {
            linkClassifier = new LinkClassifierHub();
        }
        if (type.indexOf("LinkClassifierAuthority") != -1) {
            linkClassifier = new LinkClassifierAuthority();
        }
        if (type.indexOf("LinkClassifierImpl") != -1) {
            LNClassifier lnClassifier = LNClassifier.create(featureFilePath, modelFilePath, stoplist);
            linkClassifier = new LinkClassifierImpl(lnClassifier);
        }
        if (type.indexOf("MaxDepthLinkClassifier") != -1) {
            linkClassifier = new MaxDepthLinkClassifier(1);
        }
        return linkClassifier;
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

