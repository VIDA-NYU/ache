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
package focusedCrawler.util.cache.rmi;



import java.rmi.*;

import java.util.*;

import focusedCrawler.util.cache.*;






/**

 * Interface de acesso remoto RMI para implementacao

 * da estrutura cliente-servidor da cache remota.

 *

 * Basicamente é uma implementacao remota da interface Cache.

 *

 *

 * @author Oscar Miranda

 * @version %I%, %G%

 */

public interface RemoteCacheServer extends Remote {



    /**

     * retorna o tamanho da cache

     *

     * @return  tamanho da cache

     * @throws java.rmi.RemoteException caso aconteca algum erro remoto

     */

    public int size() throws RemoteException;



    /**

     * retorna o tamanho maximo da cache. Se algum objeto novo for

     * inserido e a cache estiver com o tamanho maximo algum ou alguns

     * outros objetos deverao ser removidos para o novo objeto ser inserido.

     *

     * @return  tamanho maximo da cache

     * @throws java.rmi.RemoteException caso aconteca algum erro remoto

     */

    public int getMaxSize() throws RemoteException;



    /**

     * numero de objectos que serao removidos quando acontecer uma falta.

     *

     * @return numero de objectos que serao removidos quando acontecer uma falta.

     * @throws java.rmi.RemoteException caso aconteca algum erro remoto

     */

    public int getRemoveQuantity() throws RemoteException;



    /**

     * muda Tamanho maximo da cache.

     *

     * @param  newSize o novo tamanho maximo

     * @throws focusedCrawler.util.cache.CacheException caso aconteca algum erro

     * @throws java.rmi.RemoteException caso aconteca algum erro remoto

     */

    public void setMaxSize(int newSize)

            throws CacheException, RemoteException;



    /**

     * Muda o numero de quantidade de objetos que devem ser removidos quando

     * acontece uma falta.

     *

     * @param qtd o novo numero de quantos objetos serao removidos apos uma falha.

     * @throws focusedCrawler.util.cache.CacheException caso aconteca algum erro

     * @throws java.rmi.RemoteException caso aconteca algum erro remoto

     */

    public void setRemoveQuantity(int qtd)

            throws CacheException, RemoteException;



    /**

     * Retorna o dado associado a cache dada.

     *

     * @param  key   Chave que representa o objeto na cache

     * @return o objeto procurado ou null se nao estiver na cache.

     * @see focusedCrawler.util.cache.CacheKey

     * @throws focusedCrawler.util.cache.CacheException caso aconteca algum erro

     * @throws java.rmi.RemoteException caso aconteca algum erro remoto

     */

    public Object get(CacheKey key) throws CacheException, RemoteException;



    /**

     * Retorna um array com dados associados as caches dadas.

     * @param  key   Array de chaves

     * @return Retorna array de objetos, respeitando a ordem das chaves.

     * @see focusedCrawler.util.cache.CacheKey

     * @throws focusedCrawler.util.cache.CacheException caso aconteca algum erro

     */

    public Object[] get(CacheKey[] key) throws CacheException, RemoteException;



    /**

     * Coloca o dado na cache., atualiza dado se a chave ja

     * estiver na cache.

     *

     * @param  key   Chave que representa o objeto na cache

     * @param  data  o objeto de dados.

     * @see focusedCrawler.util.cache.CacheKey

     * @throws focusedCrawler.util.cache.CacheException caso aconteca algum erro

     * @throws java.rmi.RemoteException caso aconteca algum erro remoto

     */

    public Object put(CacheKey key,

                    Object data) throws CacheException, RemoteException;



    /**

     * Coloca um array de dados na cache, sobrescrevendo os valores existentes.

     *

     * @param  key[]   Array de chaves

     * @param  data[]  Array com os novos dados.

     * @return Retorna os valores que estavam na cache antes do put.

     * @see focusedCrawler.util.cache.CacheKey

     * @throws focusedCrawler.util.cache.CacheException caso aconteca algum erro

     */

    public Object[] put(CacheKey[] key, Object[] data) throws CacheException, RemoteException;



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

     * @throws java.rmi.RemoteException caso aconteca algum erro remoto

     */

    public Object getUpdate(CacheKey key)

            throws CacheException, RemoteException;



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

    public Object[] getUpdate(CacheKey[] key) throws CacheException, RemoteException;



    /**

     * Remove o dado referente a chave dada da cache

     *

     * @return o dado removido ou null se nao existia na cache

     */

    public Object remove(CacheKey key) throws CacheException, RemoteException;



    /**

     * Remove os dados referente as chaves dadas

     *

     * @return Array com dados removidos, se um dado nao estava na cache sua posicao sera nula

     *

     */

    public Object[] remove(CacheKey[] key) throws CacheException, RemoteException;



    /**

     * retorna a enumeracao das chaves desta cache

     */

    public Iterator getKeys() throws CacheException, RemoteException;





    /**

     * remove todos elementos da cache

     */

    public void clear() throws CacheException, RemoteException;





}

