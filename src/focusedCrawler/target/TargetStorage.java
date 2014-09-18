package focusedCrawler.target;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.net.URLEncoder;

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

	private boolean hardFocus;
	
	private boolean getBacklinks = false;

  private LangDetection langDetect;
	
	public TargetStorage(TargetClassifier targetClassifier, String fileLocation, TargetRepository targetRepository, 
			Storage linkStorage, int limitOfPages, boolean hardFocus, boolean getBacklinks) {
	    this.targetClassifier = targetClassifier;
	    this.fileLocation = fileLocation;
	    this.targetRepository = targetRepository;
	    this.linkStorage = linkStorage;
	    this.limitOfPages = limitOfPages;
	    this.hardFocus = hardFocus;
	    this.getBacklinks = getBacklinks;
      this.langDetect = new LangDetection();
      this.langDetect.init("libs/profiles/");//This is hard coded, should be fixed
	}

	public TargetStorage(String fileLocation, TargetRepository targetRepository, Storage linkStorage, 
			int limitOfPages, boolean hardFocus, boolean getBacklinks) {
		this(null, fileLocation, targetRepository, linkStorage, limitOfPages, hardFocus,getBacklinks);
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
			System.out.println("TOTAL_PAGES=" + totalOfPages
					+ ": PAGE:" + page.getURL() + " RELEVANT:" +
					totalOnTopicPages );
			System.out.println("---------------------------");
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
			Storage targetStorage = new TargetStorage(targetClassifier,targetDirectory,targetRepository,
					linkStorage,config.getParamInt("VISITED_PAGE_LIMIT"),config.getParamBoolean("HARD_FOCUS"),
					config.getParamBoolean("BIPARTITE"));
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
