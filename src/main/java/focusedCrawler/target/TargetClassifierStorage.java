package focusedCrawler.target;

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

public class TargetClassifierStorage  extends StorageDefault{

	private TargetClassifier targetClassifier;

	private String fileLocation;
	
	private TargetRepository targetRepository;

	private int totalOfPages;
	
	private int totalOnTopicPages;

	private int limitOfPages;

	private Storage linkStorage;
	  
	private StringBuffer urls = new StringBuffer();

	public TargetClassifierStorage(TargetClassifier targetClassifier, String fileLocation, TargetRepository targetRepository, Storage linkStorage) {
	    this.targetClassifier = targetClassifier;
	    this.fileLocation = fileLocation;
	    this.targetRepository = targetRepository;
	    this.linkStorage = linkStorage;
	  }

	  public synchronized Object insert(Object obj) throws StorageException {
	      
	      Page page = (Page)obj;
	      urls.append(fileLocation + "/" + page.getURL().getHost()+"/" +URLEncoder.encode(page.getIdentifier()));
	      urls.append("\n");
	      totalOfPages++;
	      try {
	    	  double prob = targetClassifier.distributionForInstance(page)[0];
		      System.out.println(">>>PROCESSING: " + page.getIdentifier() + " PROB:" + prob);
	    	  if(prob > 0.5){
		    	  linkStorage.insert(page);
		    	  page.setContent(page.getCleanContent());
		    	  targetRepository.insert(page);
		          totalOnTopicPages++;
	    	  }
	          System.out.println(getClass() + "TOTAL_PAGES=" + totalOfPages
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
			// TODO Auto-generated catch block
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

  	        TargetClassifier targetClassifier = new TargetClassifierImpl(classifier, insts, attributes, stoplist);

	        String targetDirectory = config.getParam("TARGET_STORAGE_DIRECTORY");
	        TargetRepository targetRepository = new TargetFileRepository(targetDirectory);
	        ParameterFile linkStorageConfig = new ParameterFile(config.getParam(
	            "LINK_STORAGE_FILE"));
	        Storage linkStorage = new StorageCreator(linkStorageConfig).produce();
	        Storage targetStorage = new TargetClassifierStorage(targetClassifier,targetDirectory,targetRepository,linkStorage);
	        ((TargetClassifierStorage) targetStorage).setLimitPages(config.getParamInt("VISITED_PAGE_LIMIT"));
	        StorageBinder binder = new StorageBinder(config);
	        binder.bind(targetStorage);
	      }catch (java.io.IOException ex) {
	        ex.printStackTrace();
	      }
//	      catch (ClassNotFoundException ex) {
//	        ex.printStackTrace();
//	      }
	      catch (StorageBinderException ex) {
	        ex.printStackTrace();
	      }
	      catch (StorageFactoryException ex) {
	        ex.printStackTrace();
	      } catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	    }

}
