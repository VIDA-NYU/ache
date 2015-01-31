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

import focusedCrawler.util.loader.Loadable;



/**

 * Representa uma cache com operacoes sobre os dados da cache.

 * Seu uso é da seguinte maneira:

 *    1) get dado da cache

 *        Dada uma chave, usa o metodo get(chave).

 *        caso o dado nao esteja na cache retorna null e podera

 *        ser adicionado usando o metodo put

 *    2) getUpdate

 *        retorna o valor associado a chave de qualquer jeito,

 *        se o valor nao estiver na cache, utiliza o ObjectFactory

 *        para cria-lo

 *

 *

 * @author Oscar Miranda

 * @version 1.1, 2001

 */

public interface Cache extends Loadable {



    /**

     * retorna o tamanho da cache

     *

     * @return  tamanho da cache

     */

    public int size();



    /**

     * retorna o tamanho maximo da cache. Se algum objeto novo for

     * inserido e a cache estiver com o tamanho maximo algum ou alguns

     * outros objetos deverao ser removidos para o novo objeto ser inserido.

     *

     * @return  tamanho maximo da cache

     */

    public int getMaxSize();



    /**

     * retorna o numero de objectos que serao removidos quando acontecer uma falta.

     *

     * @return numero de objectos que serao removidos quando acontecer uma falta.

     */

    public int getRemoveQuantity();



    /**

     * muda Tamanho maximo da cache.

     *

     * @param  newSize o novo tamanho maximo

     * @throws focusedCrawler.util.cache.CacheException caso aconteca algum erro

     */

    public void setMaxSize(int newSize) throws CacheException;



    /**

     * Muda o numero de quantidade de objetos que devem ser removidos quando

     * acontece uma falta.

     *

     * @param qtd o novo numero de quantos objetos serao removidos apos uma falha.

     * @throws focusedCrawler.util.cache.CacheException caso aconteca algum erro

     */

    public void setRemoveQuantity(int qtd) throws CacheException;



    /**

     * Retorna o dado associado a cache dada.

     *

     * @param  key   Chave que representa o objeto na cache

     * @return o objeto procurado ou null se nao estiver na cache.

     * @see focusedCrawler.util.cache.CacheKey

     * @throws focusedCrawler.util.cache.CacheException caso aconteca algum erro

     */

    public Object get(CacheKey key) throws CacheException;



    /**

     * Retorna um array com dados associados as caches dadas.

     * @param  key   Array de chaves

     * @return Retorna array de objetos, respeitando a ordem das chaves.

     * @see focusedCrawler.util.cache.CacheKey

     * @throws focusedCrawler.util.cache.CacheException caso aconteca algum erro

     */

    public Object[] get(CacheKey[] key) throws CacheException;





    /**

     * Coloca o dado na cache., atualiza dado se a chave ja

     * estiver na cache.

     *

     * @param  key   Chave que representa o objeto na cache

     * @param  data  o objeto de dados.

     * @return old data or null if it does not exists in cache

     * @see focusedCrawler.util.cache.CacheKey

     * @throws focusedCrawler.util.cache.CacheException caso aconteca algum erro

     */

    public Object put(CacheKey key, Object data) throws CacheException;



    /**

     * Coloca um array de dados na cache, sobrescrevendo os valores existentes.

     *

     * @param  key[]   Array de chaves

     * @param  data[]  Array com os novos dados.

     * @return Retorna os valores que estavam na cache antes do put.

     * @see focusedCrawler.util.cache.CacheKey

     * @throws focusedCrawler.util.cache.CacheException caso aconteca algum erro

     */

    public Object[] put(CacheKey[] key, Object[] data) throws CacheException;



    /**

     * Retorna o dado associado a cache dada.

     * caso o objeto nao esteja na cache, cria um novo utilizando o ObjectFactory

     * e coloca na cache. se a cache estiver cheia, remove alguns dado da cache.

     *

     * @param  key   Chave que representa o objeto na cache

     * @return o objeto procurado

     * @see focusedCrawler.util.cache.CacheKey

     * @see focusedCrawler.util.cache.ObjectFactory

     * @throws focusedCrawler.util.cache.CacheException caso aconteca algum erro

     */

    public Object getUpdate(CacheKey key) throws CacheException;



    /**

     * Retorna um array de dados assoaciado um array de chaves fornecido.

     * caso o objeto nao esteja na cache, cria um novo utilizando o ObjectFactory

     * e coloca na cache. se a cache estiver cheia, remove alguns dado da cache.

     * Observe que existe uma correspondencia entre a posicao do objeto retornado e

     * a posicao da chave fornecida.

     * @param  key[]   Array de chaves

     * @return Array com os objetos procurados.

     * @see focusedCrawler.util.cache.CacheKey

     * @see focusedCrawler.util.cache.ObjectFactory

     * @throws focusedCrawler.util.cache.CacheException caso aconteca algum erro

     */

    public Object[] getUpdate(CacheKey[] key) throws CacheException;





    /**

     * Muda o ObjectFactory da cache.

     *

     * @param fac a nova fabrica de objetos

     * @see focusedCrawler.util.cache.ObjectFactory

     */

    public void setFactory(ObjectFactory fac);



    /**

     * Retorna o ObjectFactory da cache.

     *

     * @return a fabrica desta cache

     * @see focusedCrawler.util.cache.ObjectFactory

     */

    public ObjectFactory getFactory();



    /**

     * Remove o dado referente a chave dada da cache

     *

     * @return o dado removido ou null se nao existia na cache

     */

    public Object remove(CacheKey key) throws CacheException;



    /**

     * Remove os dados referente as chaves dadas

     *

     * @return Array com dados removidos, se um dado nao estava na cache sua posicao sera nula

     *

     */

    public Object[] remove(CacheKey[] key) throws CacheException;



    /**

     * retorna a enumeracao das chaves desta cache

     */

    public Iterator getKeys() throws CacheException;



    /**

     * Remove todos os elementos da cache

     */

    public void clear() throws CacheException;



    /**

     * seta o destroyer da cache

     */

    public void setDestroyer(ObjectDestroyer destroyer);



    /**

     * retorna o destroyer da cache

     */

    public ObjectDestroyer getDestroyer();

}

