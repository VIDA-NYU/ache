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
package focusedCrawler.util.cache;



import java.io.BufferedWriter;

import java.io.IOException;

import java.io.File;

import java.io.FileWriter;

import java.io.PrintWriter;

import java.util.Enumeration;

import focusedCrawler.util.writer.WriterException;


public class CacheFileWriter extends CacheWriter{



    private PrintWriter out = null;

    private File cacheFile = null;



    public CacheFileWriter(Cache cache, File cacheFile) throws WriterException{

        super(cache);

        this.cacheFile = cacheFile;

    }



    public void setCacheFile(File cacheFile) throws WriterException{

        try{

            System.out.println("CacheFileWriter> Writing data [" + cacheFile.getAbsolutePath() + "] ...");

            out = new PrintWriter(new BufferedWriter(new FileWriter(cacheFile)));

        }catch(IOException exc){

            throw new WriterException(exc.getMessage());

        }

    }



    public void write(Object obj) throws WriterException{

        try{



            if(out==null){

                setCacheFile(this.cacheFile);

            }

            CacheKey key = (CacheKey)obj;

            Object val = cache.get(key);

            if( key != null && val != null ) {

                String strKey = key + "";

                String strVal = val + "";

                if( strKey.indexOf('\n') < 0 && strVal.indexOf('\n') < 0 ) {

                    System.out.println("CacheFileWriter> ok [" + strKey + "," + strVal + "]");

                    out.println(strKey+":"+strVal);

                }

                else {

                    System.out.println("CacheFileWriter> ignore [" + strKey + "," + strVal + "]");

                }

            }

        }catch(CacheException exc){

            throw new WriterException(exc.getMessage());

        }

    }



    public void finalize() throws WriterException{

        if(out!=null){

            out.close();

            out = null;

        }

    }

}