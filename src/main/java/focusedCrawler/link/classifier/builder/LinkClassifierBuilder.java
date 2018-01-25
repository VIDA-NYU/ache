package focusedCrawler.link.classifier.builder;

import java.io.BufferedOutputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.Vector;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import focusedCrawler.link.BipartiteGraphRepository;
import focusedCrawler.link.classifier.LNClassifier;
import focusedCrawler.link.classifier.LinkClassifier;
import focusedCrawler.link.classifier.LinkClassifierAuthority;
import focusedCrawler.link.classifier.LinkClassifierHub;
import focusedCrawler.link.classifier.LinkClassifierImpl;
import focusedCrawler.link.frontier.FrontierManager;
import focusedCrawler.link.frontier.LinkRelevance;
import focusedCrawler.util.Sampler;
import focusedCrawler.util.SmileUtil;
import focusedCrawler.util.parser.LinkNeighborhood;
import focusedCrawler.util.parser.PaginaURL;
import focusedCrawler.util.persistence.Tuple;
import focusedCrawler.util.string.PorterStemmer;
import focusedCrawler.util.string.StopList;
import focusedCrawler.util.vsm.VSMElement;
import smile.classification.SVM;
import smile.classification.SoftClassifier;
import smile.data.Attribute;
import smile.data.AttributeDataset;
import smile.data.NumericAttribute;
import smile.math.kernel.LinearKernel;

public class LinkClassifierBuilder {
    
    private static final Logger logger = LoggerFactory.getLogger(LinkClassifierBuilder.class);
    
	private BipartiteGraphRepository graphRep;
	private LinkNeighborhoodWrapper wrapper;
	private StopList stoplist;
	private PorterStemmer stemmer;
	private String[] features;
    private int maxSamples = 5000; // TODO make this configurable
    
	private Path linkClassifierFolder;

    private FrontierManager frontierManager;
	
    public LinkClassifierBuilder(String dataPath, StopList stoplist, FrontierManager frontierManager) {
		this.stoplist = stoplist;
		this.frontierManager = frontierManager;
		this.graphRep = frontierManager.getGraphRepository();
		this.stemmer = new PorterStemmer();
		this.wrapper = new LinkNeighborhoodWrapper(stoplist);
		this.linkClassifierFolder = Paths.get(dataPath, "/link_classifier/");
        if (!Files.exists(linkClassifierFolder)) {
            try {
                Files.createDirectories(linkClassifierFolder);
            } catch (IOException e) {
                throw new RuntimeException("Failed to create link classifier folder: "
                        + linkClassifierFolder.toString(), e);
            }
        }
	}

    public synchronized LinkClassifier forwardlinkTraining(Set<String> relUrls, int levels,
            String className) throws Exception {
		
	    List<Sampler<LinkNeighborhood>> instances = loadTrainingInstances(relUrls, levels);
		
		AttributeDataset inputDataset = createSmileInput(instances, false);
        
		logger.info("Training new link classifier...");
		SoftClassifier<double[]> classifier = trainSmileClassifier(inputDataset);

        String modelFile = linkClassifierFolder.resolve("link_classifier.model").toString();
        String featuresFile = linkClassifierFolder.resolve("link_classifier.features").toString();
        
        logger.info("Link Clasifier model file: "+modelFile);
        logger.info("Link Clasifier features file: "+featuresFile);
        
        SmileUtil.writeSmileClassifier(modelFile, classifier);
        writeFeaturesFile(featuresFile, features);

        String[] classValues = null;
        if (levels == 0) {
            classValues = new String[] {"POS", "NEG"};
        } else {
            classValues = new String[] {"0", "1", "2"};
        }
        
        return createLinkClassifierImpl(features, classValues, classifier, className, levels);
	}

    public LinkClassifier createLinkClassifierImpl(String[] attributes, String[] classValues,
    		SoftClassifier<double[]> classifier, String className, int levels) {

        LinkNeighborhoodWrapper wrapper = new LinkNeighborhoodWrapper(attributes, stoplist);

        LinkClassifier linkClassifier = null;
        if (className.indexOf("LinkClassifierImpl") != -1) {
        	LNClassifier lnClassifier = new LNClassifier(classifier, wrapper, attributes);
            linkClassifier = new LinkClassifierImpl(lnClassifier);
        }
        if (className.indexOf("LinkClassifierAuthority") != -1) {
            linkClassifier = new LinkClassifierAuthority(classifier,  wrapper, attributes);
        }
        if (className.indexOf("LinkClassifierHub") != -1) {
            linkClassifier = new LinkClassifierHub(classifier, wrapper, attributes);
        }
        return linkClassifier;
    }

    public SoftClassifier<double[]> trainSmileClassifier(AttributeDataset smileInput)
            throws Exception {
        SVM<double[]> classifier = new SVM<double[]>(new LinearKernel(), 1.0);
        int[] y = asIntVector(smileInput.y());
        double[][] x = smileInput.x();
        classifier.learn(x, y);
        classifier.finish();
        classifier.trainPlattScaling(x, y);
        return classifier;
    }

    private int[] asIntVector(double[] doubleVec) {
        int[] intVec = new int[doubleVec.length];
        for (int i = 0; i < intVec.length; i++) {
            intVec[i] = (int) doubleVec[i];
        }
        return intVec;
    }

    private void writeFeaturesFile(String featuresFile, String[] features)
            throws FileNotFoundException, IOException {
        OutputStream fout = new FileOutputStream(featuresFile, false);
        OutputStream bout = new BufferedOutputStream(fout);
        OutputStreamWriter outputFile = new OutputStreamWriter(bout);
        for (int i = 0; i < features.length; i++) {
            outputFile.write(features[i] + " ");
        }
        outputFile.close();
    }

    private List<Sampler<LinkNeighborhood>> loadTrainingInstances(Set<String> relevantUrls,
            int levels) throws Exception {

        final List<Sampler<LinkNeighborhood>> instances = new ArrayList<>();
        if (levels == 0) {
            // positive and negative case
            instances.add(new Sampler<LinkNeighborhood>(maxSamples));
            instances.add(new Sampler<LinkNeighborhood>(maxSamples));
        } else {
            // levels case
            for (int i = 0; i < levels; i++) {
                instances.add(new Sampler<LinkNeighborhood>(maxSamples));
            }
        }

        frontierManager.getFrontier().visitedLinks((LinkRelevance lr) -> {
            try {
                URL url = lr.getURL();
                LinkNeighborhood ln = graphRep.getLN(url);
                if (ln == null) {
                    return;
                }

                if (levels == 0) {
                    if (relevantUrls.contains(url.toString())) {
                        instances.get(0).sample(ln);
                    } else {
                        if (instances.get(1).reservoirSize() < instances.get(0).reservoirSize()) {
                            instances.get(1).sample(ln);
                        }
                    }
                } else {
                    if (relevantUrls.contains(ln.getLink().toString())) {
                        instances.get(0).sample(ln);
                        addBacklinks(instances, ln.getLink(), 1, levels, relevantUrls);
                    }
                }
            } catch (Exception e) {

            }
        });

        return instances;
    }
	
	
    private void addBacklinks(List<Sampler<LinkNeighborhood>> instances, URL url, int level,
            int limit, Set<String> relUrls) throws IOException {
        if (level >= limit) {
            return;
        }
        LinkNeighborhood[] backlinks = graphRep.getBacklinksLN(url);
        for (int i = 0; i < backlinks.length; i++) {
            URL tempURL = backlinks[i].getLink();
            if (!relUrls.contains(tempURL.toString())) {
                instances.get(level).sample(backlinks[i]);
            }
            addBacklinks(instances, tempURL, level + 1, limit, relUrls);
        }
    }

    public LinkClassifier backlinkTraining(HashMap<String, VSMElement> outlinkWeights)
            throws Exception {

        List<VSMElement> trainingSet = new ArrayList<VSMElement>();
        Tuple<String>[] tuples = graphRep.getHubGraph();
        for (int i = 0; i < tuples.length; i++) {
            String hubId = tuples[i].getKey();
            String[] outlinks = tuples[i].getValue().split("###");
            double totalProb = 0;
            for (int j = 0; j < outlinks.length; j++) {
                VSMElement elem = outlinkWeights.get(outlinks[j] + "_auth");
                if (elem != null) {
                    totalProb = totalProb + elem.getWeight();
                }
            }
            String url = graphRep.getHubURL(hubId);
            if (url != null && outlinks.length > 20) {
                LinkNeighborhood ln = graphRep.getBacklinkLN(new URL(url));
                if (ln != null) {
                    VSMElement elem =
                            new VSMElement(ln.getLink().toString() + ":::" + ln.getAroundString(),
                                    totalProb / outlinks.length);
                    trainingSet.add(elem);
                }
            }
        }
        System.out.println("TOTAL TRAINING:" + trainingSet.size());
		
		
        Collections.sort(trainingSet, VSMElement.DESC_ORDER_COMPARATOR);

        List<LinkNeighborhood> allLNs = new ArrayList<LinkNeighborhood>();
        for (int i = 0; i < trainingSet.size(); i++) {
            String[] parts = trainingSet.get(i).getWord().split(":::");
            LinkNeighborhood ln = new LinkNeighborhood(new URL(parts[0]));
            if (parts.length > 1) {
                StringTokenizer tokenizer = new StringTokenizer(parts[1], " ");
                Vector<String> aroundTemp = new Vector<String>();
                while (tokenizer.hasMoreTokens()) {
                    aroundTemp.add(tokenizer.nextToken());
                }
                String[] aroundArray = new String[aroundTemp.size()];
                aroundTemp.toArray(aroundArray);
                ln.setAround(aroundArray);
            }
            allLNs.add(ln);
        }

        // Sample the backlinks based on position in the list
        List<LinkNeighborhood> posSites = new ArrayList<LinkNeighborhood>();
        List<LinkNeighborhood> negSites = new ArrayList<LinkNeighborhood>();
        int sampleSize = Math.min(5000, allLNs.size() / 2);
        for (int i = 0; i < allLNs.size(); i++) {
            if (posSites.size() < sampleSize) {
                posSites.add(allLNs.get(i));
            }
        }
        for (int i = allLNs.size() - 1; i >= 0; i--) {
            if (negSites.size() < sampleSize) {
                negSites.add(allLNs.get(i));
            }
        }


        // TODO Following code is not really doing a sample, it's just converting it the lists in
        // Sampler objects to be compatible with createSmileInput method. Need to do some refactoring
        // in this whole class.
        List<Sampler<LinkNeighborhood>> instances = new ArrayList<Sampler<LinkNeighborhood>>(2);
        Sampler<LinkNeighborhood> posSamples = new Sampler<LinkNeighborhood>(posSites.size());
        Sampler<LinkNeighborhood> negSamples = new Sampler<LinkNeighborhood>(negSites.size());
        instances.add(posSamples);
        instances.add(negSamples);

        // Train actual classifier
        AttributeDataset smileInput = createSmileInput(instances, true);
        SoftClassifier<double[]> classifier = trainSmileClassifier(smileInput);

        String[] classValues = new String[] {"POS", "NEG"};
        return createLinkClassifierImpl(features, classValues, classifier, "LinkClassifierHub", 0);
    }


    
    private AttributeDataset createSmileInput(List<Sampler<LinkNeighborhood>> instances, boolean backlink)
            throws IOException {
        List<LinkNeighborhood> allInstances = new ArrayList<LinkNeighborhood>();
        for (int i = 0; i < instances.size(); i++) {
            Sampler<LinkNeighborhood> sampler = instances.get(i);
            for (LinkNeighborhood ln : sampler.getSamples()) {
                allInstances.add(ln);
            }
        }
        Attribute[] attributes = selectBestFeaturesForSmile(allInstances, backlink);
        AttributeDataset dataset =  new AttributeDataset("link_classifier", attributes);
        
        generatLines(attributes, instances, dataset);

        return dataset;
    }

    /**
     * This method creates the a line in the smile file for each instance.
     * 
     * @param attributes
     * @param instances
     * @param dataset
     * @throws IOException
     */
    private void generatLines(Attribute[] attributes, List<Sampler<LinkNeighborhood>> instances, AttributeDataset dataset)
            throws IOException {
    	String[] features = new String[attributes.length];
    	for(int i = 0 ; i < attributes.length; i ++) {
    		features[i] = attributes[i].getName();
    	}
    	this.features = features;
        for (int i = 0; i < instances.size(); i++) {
            Sampler<LinkNeighborhood> levelSamples = instances.get(i);
            for (LinkNeighborhood ln : levelSamples.getSamples()) {
                HashMap<String, Instance> featureValue = wrapper.extractLinks(ln, features);
                Iterator<String> iter = featureValue.keySet().iterator();
                while (iter.hasNext()) {
                    String url = (String) iter.next();
                    Instance instance = (Instance) featureValue.get(url);
                    double[] values = instance.getValues();
                    dataset.add(values);
                }
            }
        }
    }

	/**
	 * This method selects the  features to be used by the classifier.
	 * 
	 * @param allNeighbors
	 * @param backlink
	 * @return
	 * @throws MalformedURLException
	 */
    private Attribute[] selectBestFeaturesForSmile(List<LinkNeighborhood> allNeighbors, boolean backlink)
            throws MalformedURLException {

		List<Attribute> finalWords = new ArrayList<>();
		Set<String> usedURLTemp = new HashSet<>();
		Map<String, WordFrequency> urlWords = new HashMap<>();
		Map<String, WordFrequency> anchorWords = new HashMap<>();
		Map<String, WordFrequency> aroundWords = new HashMap<>();
		for (int l = 0; l < allNeighbors.size(); l++) {
			LinkNeighborhood element = allNeighbors.get(l);
		        //anchor
			String[] anchorTemp = element.getAnchor();
			for (int j = 0; j < anchorTemp.length; j++) {
				String word = stemmer.stem(anchorTemp[j]);
				if(word == null || stoplist.isIrrelevant(word)){
					continue;
				}
				WordFrequency wf = (WordFrequency) anchorWords.get(word);
				if (wf != null) {
					anchorWords.put(word, new WordFrequency(word, wf.getFrequency()+1));
				}
				else {
					anchorWords.put(word, new WordFrequency(word, 1));
				}
			}
		        //around
			String[] aroundTemp = element.getAround();
			for (int j = 0; j < aroundTemp.length; j++) {
				String word = stemmer.stem(aroundTemp[j]);
				if(word == null || stoplist.isIrrelevant(word)){
					continue;
				}
				WordFrequency wf = (WordFrequency) aroundWords.get(word);
				if (wf != null) {
					aroundWords.put(word, new WordFrequency(word, wf.getFrequency()+1));
				}
				else {
					aroundWords.put(word, new WordFrequency(word, 1));
				}
			}

		        //url
			if(!usedURLTemp.contains(element.getLink().toString())){
				usedURLTemp.add(element.getLink().toString());
				PaginaURL pageParser = new PaginaURL(new URL("http://"),element.getLink().getFile().toString(), stoplist);
				String[] urlTemp = pageParser.palavras();
				for (int j = 0; j < urlTemp.length; j++) {
//		            String word =  stemmer.stem(urlTemp[j]);
					String word =  urlTemp[j];
					if(stoplist.isIrrelevant(word)){
						continue;
					}
					WordFrequency wf = (WordFrequency) urlWords.get(word);
					if (wf != null) {
						urlWords.put(word, new WordFrequency(word, wf.getFrequency()+1));
					}
					else {
						urlWords.put(word, new WordFrequency(word, 1));
					}
				}
			}
		}

		String[][] fieldWords = new String[WordField.FIELD_NAMES.length][];

		Vector<WordFrequency> aroundVector = new Vector<>(aroundWords.values());
		Collections.sort(aroundVector,new WordFrequencyComparator());
		FilterData filterData1 = new FilterData(100,2);
		Vector<WordFrequency> aroundFinal = filterData1.filter(aroundVector,null);
		String[] aroundTemp = new String[aroundFinal.size()];

		for (int i = 0; i < aroundFinal.size(); i++) {
			WordFrequency wf = aroundFinal.elementAt(i);
			NumericAttribute attribute = new NumericAttribute("around_"+wf.getWord());
			finalWords.add(attribute);
			aroundTemp[i] = wf.getWord();
		}
		fieldWords[WordField.AROUND] = aroundTemp;

		    
		Vector<WordFrequency> urlVector = new Vector<>(urlWords.values());
		Collections.sort(urlVector,new WordFrequencyComparator());
		FilterData filterData2 = new FilterData(150,2);
		@SuppressWarnings("unchecked")
        Vector<WordFrequency> urlFinal = filterData2.filter(urlVector,(Vector<WordFrequency>)aroundFinal.clone());
		String[] urlTemp = new String[urlFinal.size()];

		for (int i = 0; i < urlTemp.length; i++) {
			WordFrequency wf = urlFinal.elementAt(i);
			NumericAttribute attribute = new NumericAttribute("url_"+wf.getWord());
			finalWords.add(attribute);
			urlTemp[i] = wf.getWord();
		}
		fieldWords[WordField.URLFIELD] = urlTemp;

		if(!backlink){
			Vector<WordFrequency> anchorVector = new Vector<>(anchorWords.values());
			Collections.sort(anchorVector, new WordFrequencyComparator());
			FilterData filterData3 = new FilterData(150,2);
			Vector<WordFrequency> anchorFinal = filterData3.filter(anchorVector,null);
			String[] anchorTemp = new String[anchorFinal.size()];

			for (int i = 0; i < anchorFinal.size(); i++) {
				WordFrequency wf = anchorFinal.elementAt(i);
				NumericAttribute attribute = new NumericAttribute("anchor_"+wf.getWord());
				finalWords.add(attribute);
				anchorTemp[i] = wf.getWord();
			}
			fieldWords[WordField.ANCHOR] = anchorTemp;
		}

		wrapper.setFeatures(fieldWords);

		Attribute[] features = new Attribute[finalWords.size()];
		finalWords.toArray(features);
		return features;
	}

}
