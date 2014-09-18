/*
############################################################################
##
## Copyright (C) 2006-2009 University of Utah. All rights reserved.
##
## This file is part of DeepPeep.
##
## This file may be used under the terms of the GNU General Public
## License version 2.0 as published by the Free Software Foundation
## and appearing in the file LICENSE.GPL included in the packaging of
## this file.  Please review the following to ensure GNU General Public
## Licensing requirements will be met:
## http://www.opensource.org/licenses/gpl-license.php
##
## If you are unsure which license is appropriate for your use (for
## instance, you are interested in developing a commercial derivative
## of DeepPeep), please contact us at deeppeep@sci.utah.edu.
##
## This file is provided AS IS with NO WARRANTY OF ANY KIND, INCLUDING THE
## WARRANTY OF DESIGN, MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE.
##
############################################################################
*/
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

/**
 * <p> </p>
 *
 * <p>Description: This class stores a page in the file system</p>
 *
 * <p>Copyright: Copyright (c) 2004</p>
 *
 * <p> </p>
 *
 * @author Luciano Barbosa
 * @version 1.0
 */
public class TargetFileRepository implements TargetRepository {

  private String location;
  
  public TargetFileRepository(){
	  
  }
  
  public TargetFileRepository(String loc){
	  this.location = loc;
  }

  /**
   * The method inserts a page with its respective crawl number.
   */
  public boolean insert(Target target, int counter) {
	    boolean contain = false;
	    String id = target.getIdentifier();
	    FileOutputStream fout;
	    try {
	    	URL url = new URL(id);
	    	String host = url.getHost();
	    	File dir = new File(location + File.separator + URLEncoder.encode(host));
	    	if(!dir.exists()){
	    		dir.mkdir();
	    	}
	    	fout = new FileOutputStream(dir.toString() + File.separator +  URLEncoder.encode(id) + "_" + counter);
	        Writer out = new OutputStreamWriter(fout, "UTF8");
	        out.write(target.getSource());
	        out.close();
	    }
	    catch (IOException ex) {
	      ex.printStackTrace();
	    }
	    return contain;
  }
  
  public boolean insert(Target target) {
    boolean contain = false;
    String id = target.getIdentifier();
    FileOutputStream fout;
    try {
    	URL url = new URL(id);
    	String host = url.getHost();
    	File dir = new File(location + File.separator + URLEncoder.encode(host));
    	if(!dir.exists()){
    		dir.mkdir();
    	}

		FileWriter fw = new FileWriter(dir.toString() + File.separator +  URLEncoder.encode(id));
		BufferedWriter bw = new BufferedWriter(fw);
//    	fout = new FileOutputStream(dir.toString() + File.separator +  URLEncoder.encode(id));
//    	Writer out = new OutputStreamWriter(fout, "UTF8");
//    	out.write(target.getSource());
//    	out.close();
//    	DataOutputStream dout = new DataOutputStream( fout );
//    	dout.writeBytes(target.getSource());
//    	dout.close();
		bw.write(target.getSource());
		bw.close();
    }
    catch (IOException ex) {
      ex.printStackTrace();
    }
    return contain;
  }

  public String getLocation(){
	  return location;
  }
  
}
