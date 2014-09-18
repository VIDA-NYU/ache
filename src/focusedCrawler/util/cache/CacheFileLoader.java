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

import focusedCrawler.util.Log;
import focusedCrawler.util.loader.LoaderException;

public class CacheFileLoader extends CacheLoader {
    /**

     * Indica com a cache será, se mapeando string em int, ou int em string

     */

    private boolean cacheStringToInt;

    /**

     * Indica como esta disposta a entrada de uma linha do arquivo,

     * se int:string ou string:int

     */

    private boolean fileIntToString;



    public CacheFileLoader() {

    }



    public CacheFileLoader(Cache cache) {

        this(cache,true,true);

    }



    public CacheFileLoader(Cache cache, boolean _cacheStringToInt) {

        this(cache,_cacheStringToInt,true);

    }



    public CacheFileLoader(Cache cache, boolean _cacheStringToInt, boolean _fileIntToString) {

        super(cache);

        this.cacheStringToInt = _cacheStringToInt;

        this.fileIntToString = _fileIntToString;

    }



    public void load (Object objeto) throws LoaderException {

        String str = (String) objeto;

        int pos = str.indexOf(":");

        if( pos > 0 ) {

            Integer i;

            String s;

            String first = str.substring(0,pos);

            String second = str.substring(pos+1);

            try {

                if(fileIntToString){

                    i = new Integer(first.trim());

                    s = second;

                }else{

                    s = first;

                    i = new Integer(second.trim());

                }

                try {

                    if (Math.random() <= 0.05) {

                        System.out.println("load: i="+i+" s="+s+" "+((Cache) getLoadable()).size()+" "+cacheStringToInt);

                    }

                    if (cacheStringToInt) {

                        ((Cache) getLoadable()).put(new ObjectCacheKey(s), i);

                    } //if

                    else {

                        ((Cache) getLoadable()).put(new ObjectCacheKey(i), s);

                    } //else

                } //try

                catch (CacheException erro) {

                    throw new LoaderException ("Não conseguiu carregar a cache: " + erro.getMessage ());

                } //catch

            }

            catch(NumberFormatException exc) {

                Log.log("CFL","Cache loader","line error '"+str+"'");

            }

        }

        else {

            Log.log("CFL","Cache loader","line error '"+str+"'");

        }

    }

}