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





import java.rmi.server.UnicastRemoteObject;

import java.rmi.AlreadyBoundException;

import java.rmi.RemoteException;

import java.rmi.registry.Registry;

import java.rmi.registry.LocateRegistry;

import java.rmi.Naming;

import java.util.*;

import focusedCrawler.util.cache.*;






/**

 * Implementacao de um servidor de cache Remota.

 *

 * @author Oscar Miranda

 * @version 1.0, 1999

 */

public class RemoteCacheServerImpl extends UnicastRemoteObject

    implements RemoteCacheServer {

    protected Cache cache;





    /**

     * Construtor da Classe

     *

     *

     * @param cache cache que vai ser usada pelo servidor

     * @param rminame nome para registrar no bind

     * @param porta_local porta que vai rodar o servidor

     *

     * @see focusedCrawler.util.cache.Cache

     *

     * @throws java.rmi.RemoteException

     * @throws java.net.MalformedURLException

     * @throws java.rmi.AlreadyBoundException

     */

    public RemoteCacheServerImpl(Cache cache, String rminame, int porta_local)

            throws RemoteException, java.net.MalformedURLException,

                   java.rmi.AlreadyBoundException {

        this(cache);



        Registry registry = LocateRegistry.createRegistry(porta_local);



        registry.bind(rminame, this);

    }



    /**

     * Construtor da Classe

     *

     *

     * @param cache cache que vai ser usada pelo servidor

     * @param rminame nome para registrar no bind

     *

     * @throws java.rmi.RemoteException

     * @throws java.net.MalformedURLException

     */

    public RemoteCacheServerImpl(Cache cache, String rminame)

            throws RemoteException, java.net.MalformedURLException {

        this(cache);



        Naming.rebind(rminame, this);

    }



    /**

     * Construtor da Classe

     *

     *

     * @param cache cache que vai ser usada pelo servidor

     *

     */

    public RemoteCacheServerImpl(Cache cache) throws RemoteException {

        super();



        if (cache == null) {

            throw new NullPointerException();

        }



        this.cache = cache;

    }



    /**

     * retorna o tamanho da cache

     *

     * @return  tamanho da cache

     * @throws java.rmi.RemoteException caso aconteca algum erro remoto

     */

    public int size() throws RemoteException {

        return cache.size();

    }



    /**

     * retorna o tamanho maximo da cache. Se algum objeto novo for

     * inserido e a cache estiver com o tamanho maximo algum ou alguns

     * outros objetos deverao ser removidos para o novo objeto ser inserido.

     *

     * @return  tamanho maximo da cache

     * @throws java.rmi.RemoteException caso aconteca algum erro remoto

     */

    public int getMaxSize() throws RemoteException {

        return cache.getMaxSize();

    }



    /**

     * numero de objectos que serao removidos quando acontecer uma falta.

     *

     * @return numero de objectos que serao removidos quando acontecer uma falta.

     * @throws java.rmi.RemoteException caso aconteca algum erro remoto

     */

    public int getRemoveQuantity() throws RemoteException {

        return cache.getRemoveQuantity();

    }



    /**

     * muda Tamanho maximo da cache.

     *

     * @param  newSize o novo tamanho maximo

     * @throws focusedCrawler.util.cache.CacheException caso aconteca algum erro

     * @throws java.rmi.RemoteException caso aconteca algum erro remoto

     */

    public void setMaxSize(int newSize)

            throws CacheException, RemoteException {

        cache.setMaxSize(newSize);

    }



    /**

     * Muda o numero de quantidade de objetos que devem ser removidos quando

     * acontece uma falta.

     *

     * @param qtd o novo numero de quantos objetos serao removidos apos uma falha.

     * @throws focusedCrawler.util.cache.CacheException caso aconteca algum erro

     * @throws java.rmi.RemoteException caso aconteca algum erro remoto

     */

    public void setRemoveQuantity(int qtd)

            throws CacheException, RemoteException {

        cache.setRemoveQuantity(qtd);

    }



    /**

     * Retorna o dado associado a cache dada.

     *

     * @param  key   Chave que representa o objeto na cache

     * @return o objeto procurado ou null se nao estiver na cache.

     * @see focusedCrawler.util.cache.CacheKey

     * @throws focusedCrawler.util.cache.CacheException caso aconteca algum erro

     * @throws java.rmi.RemoteException caso aconteca algum erro remoto

     */

    public Object get(CacheKey key) throws CacheException, RemoteException {

        return cache.get(key);

    }



    /**

     * Retorna um array com dados associados as caches dadas.

     * @param  key   Array de chaves

     * @return Retorna array de objetos, respeitando a ordem das chaves.

     * @see focusedCrawler.util.cache.CacheKey

     * @throws focusedCrawler.util.cache.CacheException caso aconteca algum erro

     */

    public Object[] get(CacheKey[] key) throws CacheException {

        return cache.get(key);

    }



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

                    Object data) throws CacheException, RemoteException {

        return cache.put(key, data);

    }



    /**

     * Coloca um array de dados na cache, sobrescrevendo os valores existentes.

     *

     * @param  key[]   Array de chaves

     * @param  data[]  Array com os novos dados.

     * @return Retorna os valores que estavam na cache antes do put.

     * @see focusedCrawler.util.cache.CacheKey

     * @throws focusedCrawler.util.cache.CacheException caso aconteca algum erro

     */

    public Object[] put(CacheKey[] key, Object[] data) throws CacheException {

        return cache.put(key, data);

    }



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

    java.io.DataOutputStream logFile;





    protected void criarEstatistica(){

//    try{

//        logFile = new java.io.DataOutputStream (new java.io.BufferedOutputStream (new java.io.FileOutputStream("/home/prod2/data/estatisticasCacheServidor"+System.currentTimeMillis())));

  //      logFile.writeBytes("TEMPO DE CHAMADA:" + "\t" );

    //    logFile.writeBytes("TERMO:" + "\t" );

      //  logFile.writeBytes("TEMPO DE RETORNO:" + "\n" );

//    }catch(Exception e){e.printStackTrace();}

    }



    public Object getUpdate(CacheKey key)

            throws CacheException, RemoteException {



        return cache.getUpdate(key);

    }





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

    public Object[] getUpdate(CacheKey[] key) throws CacheException {

        return cache.getUpdate(key);

    }



    /**

     * Remove o dado referente a chave dada da cache

     *

     * @return o dado removido ou null se nao existia na cache

     */

    public Object remove(CacheKey key) throws CacheException, RemoteException {

        return cache.remove(key);

    }



    /**

     * Remove os dado

     * s referente as chaves dadas

     *

     * @return Array com dados removidos, se um dado nao estava na cache sua posicao sera nula

     *

     */

    public Object[] remove(CacheKey[] key) throws CacheException {

        return cache.remove(key);

    }



    /**

     * retorna a enumeracao das chaves desta cache

     */

    public Iterator getKeys() throws CacheException, RemoteException {

        return cache.getKeys();

    }



    /**

     * remove todos elementos da cache

     */

    public void clear() throws CacheException, RemoteException {

        cache.clear();

    }



}

