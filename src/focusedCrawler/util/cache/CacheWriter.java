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



import java.util.Enumeration;
import java.util.Iterator;

import focusedCrawler.util.writer.Writer;
import focusedCrawler.util.writer.WriterException;


public abstract class CacheWriter implements Writer{



    protected Cache cache;



    public CacheWriter(Cache cache){

        setCache(cache);

    }



    public void setCache(Cache cache){

        this.cache = cache;

    }



    public void write() throws WriterException{

        try{

            Iterator keys = cache.getKeys();

            while(keys.hasNext()) {

                CacheKey key = (CacheKey)keys.next();

                write(key);

            }

            finalize();

        }

        catch (CacheException erro) {

            throw new WriterException ("Não conseguiu escrever na cache: " + erro.getMessage ());

        }

    }



    public abstract void write(Object obj) throws WriterException;



    public abstract void finalize() throws WriterException;



}