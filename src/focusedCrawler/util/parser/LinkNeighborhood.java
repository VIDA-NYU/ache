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
package focusedCrawler.util.parser;


import java.io.Serializable;
import java.net.MalformedURLException;
import java.net.URL;

public class LinkNeighborhood implements Serializable{

  /**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private URL link;
	
	private String[] anchor = new String[0];
		
	private String[] around = new String[0];
	
	private String imgSource;
	  
	private String[] imgAlt;
	  
	private int aroundPosition;
	
	private int numOfWordsAnchor;
	  
	private boolean sameSite = false;
	
	public LinkNeighborhood(URL link) {
		this.link = link;
	}
	
	public void setURL(URL url){
		this.link = url;
	}
	
	public void setAnchor(String[] anchor){
	    this.anchor = anchor;
	}
	
	public void setAround(String[] around){
	    this.around = around;
	}
	
	public void setAroundPosition(int pos){
	    this.aroundPosition = pos;
	}
	
	public void setNumberOfWordsAnchor(int num){
	    this.numOfWordsAnchor = num;
	}
	
	public void setImgSource(String source){
		this.imgSource = source;
	}
	  
	public void setImgAlt(String[] alt){
		this.imgAlt = alt;
	}
	
	public void setSameSite(boolean sameSite){
		this.sameSite = sameSite;
	}
	  
	public URL getLink(){
		return this.link;
	}
	
  public String getDomainName(){
    String domain = link.getHost();
    return domain.startsWith("www.") ? domain.substring(4) : domain;
  }

	public int getAroundPosition(){
		return this.aroundPosition;
	}
	
	public int getNumWordsAnchor(){
		return this.numOfWordsAnchor;
	}
	
	public String[] getAnchor(){
		return this.anchor;
	}
	
	public String getAltString(){
		StringBuffer buffer = new StringBuffer();
		String[] alts = getImgAlt();
		for (int j = 0; alts != null && j < alts.length; j++) {
			buffer.append(alts[j]);
	        buffer.append(" ");
		}
		return buffer.toString();
	}
	
	public String getAnchorString(){
		StringBuffer buffer = new StringBuffer();
		String[] anchors = getAnchor();
		for (int j = 0; j < anchors.length; j++) {
			buffer.append(anchors[j]);
			buffer.append(" ");
		}
		return buffer.toString();
	}
	
	public String getAroundString(){
		StringBuffer buffer = new StringBuffer();
		String[] arounds = getAround();
		for (int j = 0; j < arounds.length; j++) {
			buffer.append(arounds[j]);
	        buffer.append(" ");
		}
		return buffer.toString();
	}
	  
	public String[] getAround(){
		return this.around;
	}
	public String[] getImgAlt(){
		return this.imgAlt;
	}
	
	public String getImgSrc(){
		return this.imgSource;
	}
	
	public boolean getSameSite(){
		return this.sameSite;
	}
	
	public static LinkNeighborhood createLN(String strFormat) throws MalformedURLException{
		  
		String[] parts = strFormat.split("::");
		LinkNeighborhood ln = new LinkNeighborhood(new URL(parts[0]));
		if(parts.length > 2){
			if(parts[1].contains(",")){
				String[] anchorWords = parts[1].split(",");
				ln.setAnchor(anchorWords);
			}
			if(parts[2].contains(",")){
				String[] aroundWords = parts[2].split(",");
				ln.setAround(aroundWords);
			}
		}
		return ln;
	}
  
	  public String toString(){
		  StringBuffer buffer = new StringBuffer();
//	  buffer.append(link.toString());
//	  buffer.append(":");
		  for (int i = 0; anchor!=null && i < anchor.length; i++) {
			  if(i != 0){
				  buffer.append(",");  
			  }
			  buffer.append(anchor[i]);
		  }
		  buffer.append("::");
		  for (int i = 0; around!=null && i < around.length; i++) {
			  if(i != 0){
				  buffer.append(",");  
			  }
			  buffer.append(around[i]);
		  }
		  return buffer.toString();
  }

  public LinkNeighborhood clone(){
	  LinkNeighborhood ln = new LinkNeighborhood(link);
	  ln.setAnchor(anchor);
	  ln.setAround(around);
	  ln.setNumberOfWordsAnchor(numOfWordsAnchor);
	  ln.setAroundPosition(aroundPosition);
	  ln.setImgAlt(imgAlt);
	  ln.setImgSource(imgSource);
	  return ln;
  }
  
}
