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



import focusedCrawler.util.loader.Loadable;
import focusedCrawler.util.loader.Loader;
import focusedCrawler.util.loader.LoaderException;

public class CacheLoader implements Loader {



    private Cache cache;



    public CacheLoader() {

    }



    public CacheLoader(Cache cache) {

        this.cache = cache;



    }



    public Loadable getLoadable() {

        return this.cache;

    }



    public void setLoadable(Loadable loadable) {

        if (loadable instanceof Cache) {

            this.cache = (Cache) loadable;

        }

    }



    public void load() throws LoaderException {

    }



    public void load (Object objeto) throws LoaderException {

        try {

            cache.getUpdate(new ObjectCacheKey (objeto));

        }

        catch (CacheException erro) {

            throw new LoaderException ("Não conseguiu carregar a cache: " + erro.getMessage ());

        }

    }

}