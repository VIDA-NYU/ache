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

import java.util.Vector;

import focusedCrawler.util.Timer;
import focusedCrawler.util.cache.rmi.RemoteCache;

/**
 * É um ObjectFactory que permiti a implementacao de niveis de cache.
 * O que este tipo de factory faz é em vez de criar mesmo o objeto
 * ele pede para uma outra cache.
 *
 * Esta factory é util para fazer hieraquia de caches com
 * velocidades diferentes, um exemplo, eh criar uma cache de
 * 2 niveis para uma cache estatica e uma cache remota.
 *
 *
 * @see focusedCrawler.util.cache.Cache
 */

public class CacheLevel2Factory implements ObjectFactory {

    /**
     * a cache que sera chamada caso esta aconteca uma falha nesta cache.
     */
    private Cache cacheLevel1;


    /**
     * Construtor da Classe
     * recebe como parametro a cache na qual vai criar os objetos
     * solicitados.
     *
     *
     * @param c a cache de 1o. nivel.
     *
     * @see focusedCrawler.util.cache.Cache
     */

    public CacheLevel2Factory(Cache c) {
        if (c == null) {
            throw new NullPointerException();
        }
        cacheLevel1 = c;
//        criarEstatistica();
    }

    private Timer timer = new Timer();

    /**
     * Produz o dado da chave dada.
     * Na verdade retorna o dado da cache de 1o. nivel.
     *
     *
     * @param key a chave do objeto a ser criado
     *
     * @return o dado
     *
     * @throws focusedCrawler.util.cache.FactoryException caso aconteca um erro
     *
     * @see focusedCrawler.util.cache.CacheKey
     */


    public Object produce(CacheKey key) throws FactoryException {
        try {
        Object o = this.cacheLevel1.getUpdate(key);
            return o;
        } catch (CacheException ce) {
            throw new FactoryException(ce.getMessage());
        }
    }

    public Object[] produce(CacheKey[] keys) throws FactoryException {
        Object[] result = new Object[keys.length];
        for (int counter=0; counter < keys.length; counter++) {
            result[counter] = produce(keys[counter]);
        } //for
        return result;
    }

}

