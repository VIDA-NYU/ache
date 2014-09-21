package focusedCrawler.target;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.net.URLEncoder;
import java.util.ArrayList; 
import java.util.List; 
import java.lang.String;

import weka.classifiers.Classifier;
import weka.core.Instances;

import focusedCrawler.util.Page;
import focusedCrawler.util.ParameterFile;
import focusedCrawler.util.distribution.CommunicationException;
import focusedCrawler.util.storage.Storage;
import focusedCrawler.util.storage.StorageBinderException;
import focusedCrawler.util.storage.StorageDefault;
import focusedCrawler.util.storage.StorageException;
import focusedCrawler.util.storage.StorageFactoryException;
import focusedCrawler.util.storage.distribution.StorageBinder;
import focusedCrawler.util.storage.distribution.StorageCreator;
import focusedCrawler.util.string.StopList;
import focusedCrawler.util.string.StopListArquivo;
import focusedCrawler.util.LangDetection;
import focusedCrawler.util.dashboard.TargetMonitor;

/**
 * This class runs a socket server responsible to store pages coming from the crawler client.
 * @author lbarbosa
 *
 */
public class TargetStorage  extends StorageDefault{

	private TargetClassifier targetClassifier;

	private String fileLocation;
	
	private TargetRepository targetRepository;

	private int totalOfPages;
	
	private int totalOnTopicPages;

	private int limitOfPages;

	private Storage linkStorage;
	  
	private StringBuffer urls = new StringBuffer();

//Data structure for dashboard ///////
	private int numberOfLatestCrawled;
	
	private int numberOfLatestRelevant;//not used yet

	private int numberOfLatestHarvestRates;	

	private List<String> crawledUrls; 
																	  
	private List<String> relevantUrls;
																		
	private List<String> harvestRates;

	private TargetMonitor monitor;
//////////////////////////////////////
	private boolean hardFocus;
	
	private boolean getBacklinks = false;

  private LangDetection langDetect;
	
	public TargetStorage(TargetClassifier targetClassifier, String fileLocation, TargetRepository targetRepository, 
			Storage linkStorage, int limitOfPages, boolean hardFocus, boolean getBacklinks, int numberOfLatestCrawled, int numberOfLatestRelevant,
		int numberOfLatestHarvestRates, TargetMonitor mnt) {
	    this.targetClassifier = targetClassifier;
	    this.fileLocation = fileLocation;
	    this.targetRepository = targetRepository;
	    this.linkStorage = linkStorage;
	    this.limitOfPages = limitOfPages;
	    this.crawledUrls = new ArrayList<String>();
	    this.relevantUrls = new ArrayList<String>();
 	    this.harvestRates = new ArrayList<String>();
	    this.hardFocus = hardFocus;
	    this.getBacklinks = getBacklinks;
	    this.numberOfLatestCrawled = numberOfLatestCrawled;
	    this.numberOfLatestRelevant = numberOfLatestRelevant;
	    this.numberOfLatestHarvestRates = numberOfLatestHarvestRates;
	    this.totalOfPages = 0;
	    this.totalOnTopicPages = 0;
      this.langDetect = new LangDetection();
      this.langDetect.init("libs/profiles/");//This is hard coded, should be fixed
			this.monitor = mnt;
	}

	public TargetStorage(String fileLocation, TargetRepository targetRepository, Storage linkStorage, 
			int limitOfPages, boolean hardFocus, boolean getBacklinks, TargetMonitor mnt) {
		this(null, fileLocation, targetRepository, linkStorage, limitOfPages, hardFocus,getBacklinks, 10, 10, 10, mnt);
	}

	/**
	 * Inserts a page into the repository. 
	 */
	public synchronized Object insert(Object obj) throws StorageException {
		Page page = (Page)obj;
  		if (this.langDetect.detect_page(page) == false){
     			System.out.println(">>>> non-English page: " + page.getIdentifier());
      			return null;
    		}

		urls.append(fileLocation + File.separator + page.getURL().getHost() + File.separator + URLEncoder.encode(page.getIdentifier()));
		urls.append("\n");
		crawledUrls.add(page.getIdentifier());
		totalOfPages++;

		try {
			if(targetClassifier != null){
				double prob = targetClassifier.distributionForInstance(page)[0];
				page.setRelevance(prob);
				System.out.println(">>>PROCESSING: " + page.getIdentifier() + " PROB:" + prob);
				if(prob > 0.99){
					targetRepository.insert(page);
					if(getBacklinks){//set the page is as authority if using backlinks
						page.setAuth(true);
					}
					linkStorage.insert(page);
					relevantUrls.add(page.getIdentifier());
					totalOnTopicPages++;
				}else{
					if(!hardFocus){
						if(getBacklinks){
							if(page.isHub()){
								linkStorage.insert(page);
							}
						}else{
							linkStorage.insert(page);
						}
					}
				}
			}else{
				page.setRelevance(1);
				page.setAuth(true);
				System.out.println(">>>PROCESSING: " + page.getIdentifier());
				linkStorage.insert(page);
				targetRepository.insert(page,totalOfPages);
				totalOnTopicPages++;
			}

			//Export information for dashboard
			harvestRates.add(Integer.toString(totalOnTopicPages) + "\t" + String.valueOf(totalOfPages) + "\t" + String.valueOf(System.currentTimeMillis() / 1000L));
			if(totalOfPages % numberOfLatestHarvestRates == 0) {
        List<String> latestHarvestRates = getLatestHarvestRates(); 
				monitor.exportHarvestInfo(latestHarvestRates);
			}
           
			if(totalOnTopicPages % numberOfLatestRelevant == 0) {
				List<String> latestCrawled = getLatestCrawledURLs();
				monitor.exportCrawledPages(latestCrawled);	

				List<String> latestRelevant = getLatestRelevantURLs();
				monitor.exportRelevantPages(latestRelevant);

			}
			//End exporting

			if(totalOfPages > limitOfPages){
				System.exit(0);
			}
		}
		catch (CommunicationException ex) {
			ex.printStackTrace();
			throw new StorageException(ex.getMessage());
		} catch (TargetClassifierException e) {
			e.printStackTrace();
		}
		return null;
	}

	public void setLimitPages(int limit){
		limitOfPages = limit;
	}


	private int initialIndex(List<String> urls, int number) {
		if(number > urls.size()) {
			return 0;
		} else {
			return urls.size() - number;
		}		
	
	}

	public List<String> getLatestCrawledURLs() {
		List<String> crawled = new ArrayList<String>(crawledUrls);
		crawledUrls.clear();
		return crawled;
	}


	public List<String> getLatestRelevantURLs() {
		List<String> relevant = new ArrayList<String>(relevantUrls);
		relevantUrls.clear();
		return relevant;
	}

	public List<String> getLatestHarvestRates() {
		List<String> rates = new ArrayList<String>(harvestRates);
		harvestRates.clear();
		return rates;

	}
        

	public static void main(String[] args) {
		try{
			ParameterFile config = new ParameterFile(args[0]);
			StopList stoplist = new StopListArquivo(config.getParam("STOPLIST_FILES"));
			boolean useClassifier = config.getParamBoolean("USE_CLASSIFIER");
			TargetClassifier targetClassifier = null;
			//if one wants to use a classifier
			if(useClassifier){
				InputStream is = new FileInputStream(config.getParam("FILE_CLASSIFIER"));
				ObjectInputStream objectInputStream = new ObjectInputStream(is);
				Classifier classifier = (Classifier) objectInputStream.readObject();
				String[] attributes = config.getParam("ATTRIBUTES", " ");
				weka.core.FastVector vectorAtt = new weka.core.FastVector();
				for (int i = 0; i < attributes.length; i++) {
					vectorAtt.addElement(new weka.core.Attribute(attributes[i]));
				}
				String[] classValues = config.getParam("CLASS_VALUES", " ");
				weka.core.FastVector classAtt = new weka.core.FastVector();
				for (int i = 0; i < classValues.length; i++) {
					classAtt.addElement(classValues[i]);
				}
				vectorAtt.addElement(new weka.core.Attribute("class", classAtt));
				Instances insts = new Instances("target_classification", vectorAtt, 1);
				insts.setClassIndex(attributes.length);
				targetClassifier = new TargetClassifierImpl(classifier, insts, attributes, stoplist);
			}
			String targetDirectory = config.getParam("TARGET_STORAGE_DIRECTORY");
			TargetRepository targetRepository = new TargetFileRepository(targetDirectory);
			ParameterFile linkStorageConfig = new ParameterFile(config.getParam("LINK_STORAGE_FILE"));
			Storage linkStorage = new StorageCreator(linkStorageConfig).produce();
			TargetMonitor mnt = new TargetMonitor("data/data_monitor/crawledpages.csv", 
																"data/data_monitor/relevantpages.csv", 
																"data/data_monitor/harvestinfo.csv");//hard coding 
			Storage targetStorage = new TargetStorage(targetClassifier,targetDirectory,targetRepository,
					linkStorage,config.getParamInt("VISITED_PAGE_LIMIT"),config.getParamBoolean("HARD_FOCUS"),
					config.getParamBoolean("BIPARTITE"), 10, 10, 10, mnt);

			StorageBinder binder = new StorageBinder(config);
			binder.bind(targetStorage);
		}catch (java.io.IOException ex) {
			ex.printStackTrace();
		}catch (StorageBinderException ex) {
			ex.printStackTrace();
		}catch (StorageFactoryException ex) {
			ex.printStackTrace();
		}catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
	}
}
