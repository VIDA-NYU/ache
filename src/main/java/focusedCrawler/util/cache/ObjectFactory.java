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



/**

 * Representa uma fabrica de Objetos da cache.
 * Dada uma chave, cria um objeto de dados que ela representa.
 *
 *
 */

public interface ObjectFactory {

    /**
     * Quando o objeto nao estiver na cache, cria um objeto novo.
     *
     * @param key a chave
     * @return o objeto de dado para a chave passada como parametro
     * @throws focusedCrawler.util.cache.FactoryException caso aconteca um erro
     * @see focusedCrawler.util.cache.CacheKey
     */

    public Object produce(CacheKey key) throws FactoryException;

    /**
     * Quando o objeto nao estiver na cache, cria um objeto novo.
     *
     * @param key a chave
     * @return o objeto de dado para a chave passada como parametro
     * @throws focusedCrawler.util.cache.FactoryException caso aconteca um erro
     * @see focusedCrawler.util.cache.CacheKey
     */

    public Object[] produce(CacheKey[] keys) throws FactoryException;



}

