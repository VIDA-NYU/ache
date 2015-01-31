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

 * usa um int do hashCode como identificador

 * se o hashCode de dois objetos forem iguals entao os objetos sao iguais

 * esta regra deve ser mantida se for usada outra chave

 */

public class IntCacheKey implements CacheKey, Externalizable {

    private int key;



    public IntCacheKey(int key) {

       this.key = key;

    }



    public IntCacheKey() {

    }



    /**

     * modifica o objeto chave

     *

     *

     * @param key a nova chave

     */

    public void setKey(int key) {

        this.key = key;

    }



    public int getValue() {

        return key;

    }



    public int hashCode() {

        return key;

    }



    public boolean equals(Object other) {

        return other.hashCode() == hashCode();

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

        return this;

    }





    public String toString() {

        return "" + key;

    }



    public void writeExternal(ObjectOutput out) throws IOException {

        out.writeInt(key);

    }

    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {

        key = in.readInt();

    }



}

