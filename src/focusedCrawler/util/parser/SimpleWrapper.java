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

import java.util.Vector;

/**
 * <p>Title: </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2003</p>
 * <p>Company: </p>
 * @author not attributable
 * @version 1.0
 */

public class SimpleWrapper {

    private String iniPattern;

    private String middlePattern;
    
    private String endPattern;

    public SimpleWrapper(String initial, String end) {
        this.iniPattern = initial;
        this.endPattern = end;
    }

    public SimpleWrapper(String initial, String middle, String end) {
    	this(initial,end);
    	this.middlePattern = middle;
    }

    public String[] filterMultipleStrings(String content){
    	Vector filteredStrings = new Vector();
        int count = 0;
        int indexIni = content.indexOf(iniPattern);
        while(indexIni != -1){
        	
            content = content.substring(indexIni+iniPattern.length(),content.length());
            int indexEnd = content.indexOf(endPattern);
            if(indexEnd < 0) break;
            String field = null;
            if(middlePattern != null){
            	int indexMid = content.indexOf(middlePattern);
            	field = content.substring(indexMid+middlePattern.length(),indexEnd);
            }else{
            	field = content.substring(0,indexEnd);	
            }
            
            filteredStrings.add(field);
            content = content.substring(indexEnd+endPattern.length(),content.length());
            indexIni = content.indexOf(iniPattern);
            count++;
        }

        String[] recs = new String[count];
        filteredStrings.toArray(recs);
        return recs;
    }

    public String filterString(String content){
        String filteredString = null;
        int indexIni = content.indexOf(iniPattern);
        content = content.substring(indexIni+iniPattern.length(),content.length());
        int indexEnd = content.indexOf(endPattern);
        if(indexEnd > 0) {
            filteredString = content.substring(0,indexEnd);
        }
        return filteredString;

    }


}