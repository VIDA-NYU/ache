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



import java.io.ObjectOutput;

import java.io.ObjectInput;

import java.io.IOException;

import java.io.Externalizable;



/**

 * Esta classe implementa um cacheKey default

 * usando um Objeto qualquer como o objeto de hash.

 *

 *

 * @author Oscar Miranda

 * @version %I%, %G%

 * @see focusedCrawler.util.cache.CacheKey

 */

public class StringCacheKey implements CacheKey, Externalizable {

    private String key;



    public StringCacheKey(String key) {

       this.key = key;

    }



    public StringCacheKey() {

    }



    /**

     * modifica o objeto chave

     *

     *

     * @param key a nova chave

     */

    public void setKey(String key) {

        this.key = key;

    }



    /**

     * retorna a chave de hash

     *

     *

     * @return a chave de hash

     *

     * @see focusedCrawler.util.cache.CacheKey

     */

    public Object hashKey() {

        return key;

    }





    public String toString() {

        return "" + key;

    }

    public void writeExternal(ObjectOutput out) throws IOException {

        out.writeInt(key.length());

        out.write(key.getBytes());

    }

    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {

        int ss = in.readInt();

        byte bytes[] = new byte[ss];

        in.read(bytes);

        this.key = new String(bytes);

    }



}

