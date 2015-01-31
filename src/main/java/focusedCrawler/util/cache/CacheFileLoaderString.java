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



import focusedCrawler.util.loader.LoaderException;

public class CacheFileLoaderString extends CacheLoader {



    public CacheFileLoaderString() {

    }



    public CacheFileLoaderString(Cache cache) {

        super(cache);

    }



    public void load (Object objeto) throws LoaderException {

        String s = (String) objeto;

        String first = s.substring(0, s.indexOf(":"));

        String second = s.substring(s.indexOf(":") +1);

        try {

            if (Math.random() <= 0.05) {

                System.out.println("load: first="+first+" second="+second+" "+((Cache) getLoadable()).size());

            }

            ((Cache) getLoadable()).put(new ObjectCacheKey(first), second);

        } //try

        catch (CacheException erro) {

            throw new LoaderException ("Não conseguiu carregar a cache: " + erro.getMessage ());

        } //catch



    }

}