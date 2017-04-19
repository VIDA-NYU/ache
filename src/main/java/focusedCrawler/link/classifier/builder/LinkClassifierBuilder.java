package focusedCrawler.link.classifier.builder;

import java.io.BufferedOutputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.StringReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.Vector;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import focusedCrawler.link.BipartiteGraphRepository;
import focusedCrawler.link.classifier.LinkClassifier;
import focusedCrawler.link.classifier.LinkClassifierFactory;
import focusedCrawler.link.frontier.Frontier;
import focusedCrawler.util.parser.LinkNeighborhood;
import focusedCrawler.util.parser.PaginaURL;
import focusedCrawler.util.persistence.Tuple;
import focusedCrawler.util.string.PorterStemmer;
import focusedCrawler.util.string.StopList;
import focusedCrawler.util.vsm.VSMElement;
import focusedCrawler.util.vsm.VSMElementComparator;
import weka.classifiers.Classifier;
import weka.core.Instances;
import weka.core.SerializationHelper;

public class LinkClassifierBuilder {
    
    private static final Logger logger = LoggerFactory.getLogger(LinkClassifierBuilder.class);

	private BipartiteGraphRepository graphRep;
	private LinkNeighborhoodWrapper wrapper;
	private StopList stoplist;
	private PorterStemmer stemmer;
	private Frontier frontier;
	private String[] features;
	
	private Path linkClassifierFolder;
	
	public LinkClassifierBuilder(String dataPath, BipartiteGraphRepository graphRep, StopList stoplist, LinkNeighborhoodWrapper wrapper, Frontier frontier){
		this.graphRep = graphRep;
		this.stemmer = new PorterStemmer();
		this.stoplist = stoplist;
		this.wrapper = wrapper;
		this.frontier = frontier;
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
	
	public synchronized LinkClassifier forwardlinkTraining(HashSet<String> relSites, int levels, String className) throws Exception{
		
	    Vector<Vector<LinkNeighborhood>> instances = loadTrainingInstances(relSites, levels);
		
		String wekaInputAsString = createWekaInput(instances, false);
        
		logger.info("Training new link classifier...");
		Classifier classifier = trainWekaClassifier(wekaInputAsString);

        String modelFile = linkClassifierFolder.resolve("link_classifier.model").toString();
        String featuresFile = linkClassifierFolder.resolve("link_classifier.features").toString();
        
        logger.info("Link Clasifier model file: "+modelFile);
        logger.info("Link Clasifier features file: "+featuresFile);
        
        SerializationHelper.write(modelFile, classifier);
        writeFeaturesFile(featuresFile, features);

        String[] classValues = null;
        if (levels == 0) {
            classValues = new String[] {"POS", "NEG"};
        } else {
            classValues = new String[] {"0", "1", "2"};
        }
        
        return LinkClassifierFactory.createLinkClassifierImpl(
                features, classValues, classifier, className, levels);
	}
    
    public Classifier trainWekaClassifier(String wekaInputAsString) throws Exception {
        Instances data = null;
        try(StringReader reader = new StringReader(wekaInputAsString)) {
            data = new Instances(reader);
        }
        data.setClassIndex(data.numAttributes() - 1);
        Classifier classifier = new weka.classifiers.functions.SMO();
        classifier.setOptions(weka.core.Utils.splitOptions("-C 1.0 -L 0.0010 -P 1.0E-12 -N 0 -M -V -1 -W 1 -K \"weka.classifiers.functions.supportVector.PolyKernel -C 250007 -E 1.0\" -no-cv"));
        classifier.buildClassifier(data);
        return classifier;
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

    private Vector<Vector<LinkNeighborhood>> loadTrainingInstances(
            HashSet<String> relSites, int levels)
            throws Exception,
                   MalformedURLException,
                   IOException {

        Vector<Vector<LinkNeighborhood>> instances = null;
        if (levels == 0) {// pos and neg case
            instances = new Vector<Vector<LinkNeighborhood>>(2);
            instances.add(new Vector<LinkNeighborhood>());
            instances.add(new Vector<LinkNeighborhood>());
        } else { // levels case
            instances = new Vector<Vector<LinkNeighborhood>>(levels);
            for (int i = 0; i < levels; i++) {
                instances.add(new Vector<LinkNeighborhood>());
            }
        }

        HashSet<String> visitedLinks = frontier.visitedLinks();
        for (Iterator<String> iterator = visitedLinks.iterator(); iterator.hasNext();) {
            URL url = new URL(iterator.next());
            
            LinkNeighborhood ln = graphRep.getLN(url);
            if (ln == null) {
                continue;
            }

            if (levels == 0) {
                if (relSites.contains(url.toString())) {
                    instances.elementAt(0).add(ln);
                } else {
                    if (instances.elementAt(1).size() < instances.elementAt(0).size()) {
                        instances.elementAt(1).add(ln);
                    }
                }
            } else {
                if (relSites.contains(ln.getLink().toString())) {
                    instances.elementAt(0).add(ln);
                    addBacklinks(instances, ln.getLink(), 1, levels, relSites);
                }
            }
        }
        return instances;
    }
	
	
	private void addBacklinks(Vector<Vector<LinkNeighborhood>> instances, URL url, int level, int limit, HashSet<String> relSites) throws IOException{
		if(level >= limit){
			return;
		}
		LinkNeighborhood[] backlinks = graphRep.getBacklinksLN(url);
		for (int i = 0; i < backlinks.length; i++) {
			URL tempURL = backlinks[i].getLink();
			if(!relSites.contains(tempURL.toString())){
				instances.elementAt(level).add(backlinks[i]);				
			}
			addBacklinks(instances,tempURL,level+1,limit,relSites);
		}
	}
	
	public LinkClassifier backlinkTraining(HashMap<String,VSMElement> outlinkWeights) throws Exception{
//		HashMap<String,VSMElement> sitesCount = new HashMap<String, VSMElement>();
		Vector<VSMElement> trainingSet = new Vector<VSMElement>();
		Tuple<String>[] tuples = graphRep.getHubGraph();
		for (int i = 0; i < tuples.length; i++) {
			String hubId = tuples[i].getKey();
			String[] outlinks = tuples[i].getValue().split("###");
			double totalProb = 0;
			for (int j = 0; j < outlinks.length; j++) {
				VSMElement elem = outlinkWeights.get(outlinks[j]+"_auth");
				if(elem != null){
					totalProb = totalProb + elem.getWeight();
				}
			}
			String url = graphRep.getHubURL(hubId);
			if(url != null && outlinks.length > 20){
				LinkNeighborhood ln = graphRep.getBacklinkLN(new URL(url));
				if(ln != null){
					VSMElement elem = new VSMElement(ln.getLink().toString() + ":::" + ln.getAroundString(), totalProb/outlinks.length);
					trainingSet.add(elem);
				}
			}
		}
		System.out.println("TOTAL TRAINING:" + trainingSet.size());
		
		Vector<Vector<LinkNeighborhood>> instances = new Vector<Vector<LinkNeighborhood>>(2);
		Vector<LinkNeighborhood> posSites = new Vector<LinkNeighborhood>();
		Vector<LinkNeighborhood> negSites = new Vector<LinkNeighborhood>();
		instances.add(posSites);
		instances.add(negSites);
		Collections.sort(trainingSet,new VSMElementComparator());
		Vector<LinkNeighborhood> allLNs = new Vector<LinkNeighborhood>();
		for (int i = 0; i < trainingSet.size(); i++) {
			String[] parts = trainingSet.elementAt(i).getWord().split(":::");
			LinkNeighborhood ln = new LinkNeighborhood(new URL(parts[0]));
			if(parts.length > 1){
				StringTokenizer tokenizer = new StringTokenizer(parts[1]," ");
				Vector<String> aroundTemp = new Vector<String>();
				while(tokenizer.hasMoreTokens()){
					aroundTemp.add(tokenizer.nextToken());
	   		  	}
	   		  	String[] aroundArray = new String[aroundTemp.size()];
	   		  	aroundTemp.toArray(aroundArray);
	   		  	ln.setAround(aroundArray);
			}
			allLNs.add(ln);
		}
		int sampleSize = Math.min(5000,allLNs.size()/2);
		for (int i = 0; i < allLNs.size(); i++) {
			if(posSites.size() < sampleSize){
				posSites.add(allLNs.elementAt(i));
			}
		}
		for (int i = allLNs.size()-1; i >= 0 ; i--) {
			if(negSites.size() < sampleSize){
				negSites.add(allLNs.elementAt(i));
			}
		}
		
		LinkNeighborhood[] pos = new LinkNeighborhood[posSites.size()];
		posSites.toArray(pos);
		
		LinkNeighborhood[] neg = new LinkNeighborhood[negSites.size()];
		negSites.toArray(neg);
		
		String wekaInput = createWekaInput(instances, true);
		
		Classifier classifier = trainWekaClassifier(wekaInput);
		
		String[] classValues = new String[]{"POS","NEG"};
		return LinkClassifierFactory.createLinkClassifierImpl(features, classValues, classifier, "LinkClassifierHub",0);
	}


    /**
     * Creates the weka input file
     * 
     * @param instances
     * @param backlink
     * @return
     * @throws IOException
     */
    private String createWekaInput(Vector<Vector<LinkNeighborhood>> instances, boolean backlink)
            throws IOException {

        StringBuffer output = new StringBuffer();
        output.append("@relation classifier\n");
        Vector<LinkNeighborhood> allInstances = new Vector<LinkNeighborhood>();
        for (int i = 0; i < instances.size(); i++) {
            allInstances.addAll(instances.elementAt(i));
        }
        features = selectBestFeatures(allInstances, backlink);
        for (int i = 0; i < features.length; i++) {
            output.append("@attribute " + features[i] + " REAL \n");
        }
        output.append("@attribute class {");
        for (int i = 1; i < instances.size(); i++) {
            output.append(i + ",");
        }
        output.append(instances.size() + "}\n");
        output.append("\n");
        output.append("@data\n");
        output.append(generatLines(features, instances));

        return output.toString();
    }

    /**
     * This method creates the a line in the weka file for each instance
     * 
     * @param features
     * @param instances
     * @return
     * @throws IOException
     */
    private String generatLines(String[] features, Vector<Vector<LinkNeighborhood>> instances)
            throws IOException {
        StringBuffer buffer = new StringBuffer();
        for (int i = 0; i < instances.size(); i++) {
            Vector<LinkNeighborhood> level = instances.elementAt(i);
            System.out.println(level.size());
            for (int j = 0; j < level.size(); j++) {
                LinkNeighborhood ln = level.elementAt(j);
                StringBuffer line = new StringBuffer();
                HashMap<String, Instance> featureValue = wrapper.extractLinks(ln, features);
                Iterator<String> iter = featureValue.keySet().iterator();
                while (iter.hasNext()) {
                    String url = (String) iter.next();
                    Instance instance = (Instance) featureValue.get(url);
                    double[] values = instance.getValues();
                    line.append("{");
                    boolean containsValue = false;
                    for (int l = 0; l < values.length; l++) {
                        if (values[l] > 0) {
                            containsValue = true;
                            line.append(l + " " + (int) values[l]);
                            line.append(",");
                        }
                    }
                    line.append(values.length + " " + (i + 1));
                    line.append("}");
                    line.append("\n");
                    if (containsValue) {
                        buffer.append(line);
                    } else {
                        line = new StringBuffer();
                    }
                }
            }
        }
        return buffer.toString();
    }

	/**
	 * This method selects the  features to be used by the classifier.
	 * @param allNeighbors
	 * @param backlink
	 * @return
	 * @throws MalformedURLException
	 */
	private String[] selectBestFeatures(Vector<LinkNeighborhood> allNeighbors, boolean backlink) throws MalformedURLException{
		Vector<String> finalWords = new Vector<>();
		Set<String> usedURLTemp = new HashSet<>();
		Map<String, WordFrequency> urlWords = new HashMap<>();
		Map<String, WordFrequency> anchorWords = new HashMap<>();
		Map<String, WordFrequency> aroundWords = new HashMap<>();
		for (int l = 0; l < allNeighbors.size(); l++) {
			LinkNeighborhood element = allNeighbors.elementAt(l);
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

//		    System.out.println("AROUND:"+aroundVector);
		for (int i = 0; i < aroundFinal.size(); i++) {
			WordFrequency wf = aroundFinal.elementAt(i);
//		      System.out.println("around_"+wf.getWord()  + ":" + wf.getFrequency());
			finalWords.add("around_"+wf.getWord());
			aroundTemp[i] = wf.getWord();
		}
		fieldWords[WordField.AROUND] = aroundTemp;

		    
		Vector<WordFrequency> urlVector = new Vector<>(urlWords.values());
//		    System.out.println("URL1:"+urlVector);
		Collections.sort(urlVector,new WordFrequencyComparator());
		FilterData filterData2 = new FilterData(150,2);
		@SuppressWarnings("unchecked")
        Vector<WordFrequency> urlFinal = filterData2.filter(urlVector,(Vector<WordFrequency>)aroundFinal.clone());
		String[] urlTemp = new String[urlFinal.size()];

//		    String[] urlTemp = new String[3];

//		    System.out.println("URL:"+urlVector);

		for (int i = 0; i < urlTemp.length; i++) {
			WordFrequency wf = urlFinal.elementAt(i);
//		      System.out.println("url_"+wf.getWord()  + ":" + wf.getFrequency());
			finalWords.add("url_"+wf.getWord());
			urlTemp[i] = wf.getWord();
		}
		fieldWords[WordField.URLFIELD] = urlTemp;

		if(!backlink){
			Vector<WordFrequency> anchorVector = new Vector<>(anchorWords.values());
			Collections.sort(anchorVector, new WordFrequencyComparator());
			FilterData filterData3 = new FilterData(150,2);
			Vector<WordFrequency> anchorFinal = filterData3.filter(anchorVector,null);
			String[] anchorTemp = new String[anchorFinal.size()];

//			    System.out.println("ANCHOR:"+anchorVector);
			for (int i = 0; i < anchorFinal.size(); i++) {
				WordFrequency wf = anchorFinal.elementAt(i);
//			    System.out.println("anchor_"+wf.getWord() + ":" + wf.getFrequency());
				finalWords.add("anchor_"+wf.getWord());
				anchorTemp[i] = wf.getWord();
			}
			fieldWords[WordField.ANCHOR] = anchorTemp;
		}

		wrapper.setFeatures(fieldWords);

		String[] features = new String[finalWords.size()];
		finalWords.toArray(features);
		return features;
	}

}
