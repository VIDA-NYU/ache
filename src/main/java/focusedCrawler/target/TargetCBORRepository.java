package focusedCrawler.target;

import focusedCrawler.util.Target;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.security.NoSuchAlgorithmException;

import com.fasterxml.jackson.dataformat.cbor.*;
import com.fasterxml.jackson.databind.ObjectMapper;
/*
*
*/

public class TargetCBORRepository implements TargetRepository {

  private String location;
		private String domain;
  private TargetModel targetModel;
  
  public TargetCBORRepository(){
	targetModel = new TargetModel("Kien Pham", "kien.pham@nyu.edu");//This contact information should be read from config file
	  
  }
  
		public TargetCBORRepository(String loc, String domain){
	targetModel = new TargetModel("Kien Pham", "kien.pham@nyu.edu");//This contact information should be read from config file
	  this.location = loc;
    this.domain = domain;
  }

  /**
   * The method inserts a page with its respective crawl number.
   */
  public boolean insert(Target target, int counter) {
	boolean contain = false;
    try {
     URL urlObj = new URL(target.getIdentifier());
		 String host = urlObj.getHost();
		 String url = target.getIdentifier();
		 this.targetModel.setTimestamp();
		 this.targetModel.setUrl(url);
		 this.targetModel.setContent(target.getSource());
     this.targetModel.setKey(url, this.domain);
     HashMap<String, Object> h = (HashMap<String, Object>)this.targetModel.request.get("client");
     h.put("hostname", "gray17.poly.edu"); //TODO this should be in the config file
     h.put("address", "128.238.182.77");
     h.put("robots", "classic");
		 
		CBORFactory f = new CBORFactory();
    	ObjectMapper mapper = new ObjectMapper(f);
		File dir = new File(location + File.separator + URLEncoder.encode(host));
		if(!dir.exists()){
            dir.mkdir();
        }
		File out = new File(dir.toString() + File.separator + URLEncoder.encode(url) + "_" + counter);
		mapper.writeValue(out, this.targetModel);

    }
    catch (IOException ex) {
      ex.printStackTrace();
    }
    catch(NoSuchAlgorithmException e1) {
				e1.printStackTrace();
    }
    
    return contain;
  }
  

  public boolean insert(Target target) {
    boolean contain = false;
    try {
     URL urlObj = new URL(target.getIdentifier());
		 String host = urlObj.getHost();
		 String url = target.getIdentifier();
		 this.targetModel.setTimestamp();
		 this.targetModel.setUrl(url);
		 this.targetModel.setContent(target.getSource());
     this.targetModel.setKey(url, this.domain);
     HashMap<String, Object> h = (HashMap<String, Object>)this.targetModel.request.get("client");
     h.put("hostname", "gray17.poly.edu"); //TODO this should be in the config file
     h.put("address", "128.238.182.77");
     h.put("robots", "classic");

		CBORFactory f = new CBORFactory();
    	ObjectMapper mapper = new ObjectMapper(f);
		File dir = new File(location + File.separator + URLEncoder.encode(host));
		if(!dir.exists()){
            dir.mkdir();
        }
		File out = new File(dir.toString() + File.separator + URLEncoder.encode(url));
		mapper.writeValue(out, this.targetModel);
  
    }
    catch (IOException ex) {
      ex.printStackTrace();
    }
    catch(NoSuchAlgorithmException e1) {
				e1.printStackTrace();
    }
     
    return contain;
  }

  public String getLocation(){
	  return location;
  }
  
}
