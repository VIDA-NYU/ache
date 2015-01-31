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



import java.util.*;

import java.rmi.RemoteException;

import focusedCrawler.util.cache.Cache;
import focusedCrawler.util.cache.CacheException;
import focusedCrawler.util.cache.CacheKey;
import focusedCrawler.util.cache.ObjectDestroyer;
import focusedCrawler.util.cache.ObjectFactory;
import focusedCrawler.util.distribution.rmi.RemoteObjectGenerator;











/**

 * Classe que utiliza uma cache remota via RMI.

 * Permite a utilizacao de uma cache remota.

 *

 * <BR>Modo de usar:

 * <BR>--

 * <BR>Cache myCache = new RemoteCache("rmi://server:port/server-name");

 * <BR>--

 *

 * @see focusedCrawler.util.cache.rmi.RemoteCacheServer

 * @see focusedCrawler.util.cache.rmi.RemoteCacheServerImpl

 */

public class RemoteCache implements Cache {

    private int FAULT_TOLERANT = 1;



    private RemoteCacheServer server;

    private RemoteObjectGenerator objectGenerator;



    /**

     * Construtor da Classe

     *

     * @param url a url do servidor de cache

     *

     *

     * @throws java.rmi.NotBoundException

     * @throws java.rmi.UnknownHostException

     * @throws java.rmi.RemoteException

     * @param url a url rmi da cache

     */

    public RemoteCache(RemoteObjectGenerator _objectGenerator) throws CacheException {

        this.objectGenerator = _objectGenerator;

        createServer();

    }



    private void createServer() throws CacheException {

        try {

            Thread.sleep(1000);

        } //try

        catch (InterruptedException error) {

            error.printStackTrace();

        } //catch

        this.server = (RemoteCacheServer) objectGenerator.getObject();

    } //createServer



    /**

     * retorna o tamanho da cache

     * @return  tamanho da cache, retorna -1 se nao conseguir conectar

     *          com o servidor

     */

    public int size() {

        try {

            return server.size();

        } //try

        catch (RemoteException re) {

            re.printStackTrace();

            for (int counter=0; counter < FAULT_TOLERANT; counter++) {

                try {

                    createServer();

                    return server.size();

                } //for

                catch (CacheException error) {

                } //catch

                catch (RemoteException error) {

                } //catch

            } //for

            return -1;

        } //catch

    } //size



    /**

     * retorna o tamanho maximo da cache. Se algum objeto novo for

     * inserido e a cache estiver com o tamanho maximo algum ou alguns

     * outros objetos deverao ser removidos para o novo objeto ser inserido.

     * @return  tamanho maximo da cache, retorna -1 se nao conseguir conectar

     *          com o servidor

     */

    public int getMaxSize() {

        try {

            return server.getMaxSize();

        } catch (RemoteException re) {

            re.printStackTrace();

            for (int counter=0; counter < FAULT_TOLERANT; counter++) {

                try {

                    createServer();

                    return server.getMaxSize();

                } //for

                catch (CacheException error) {

                } //catch

                catch (RemoteException error) {

                } //catch

            } //for

            return -1;

        }

    }



    /**

     * numero de objectos que serao removidos quando acontecer uma falta.

     * @return numero de objectos que serao removidos quando acontecer uma falta

     *        retorna -1 se nao conseguir conectar com o servidor

     */

    public int getRemoveQuantity() {

        try {

            return server.getRemoveQuantity();

        } catch (RemoteException re) {

            for (int counter=0; counter < FAULT_TOLERANT; counter++) {

                try {

                    createServer();

                    return server.getRemoveQuantity();

                } //for

                catch (CacheException error) {

                } //catch

                catch (RemoteException error) {

                } //catch

            } //for

            return -1;

        }

    }



    /**

     * muda Tamanho maximo da cache.

     * @param  newSize o novo tamanho maximo

     * @throws focusedCrawler.util.cache.CacheException caso aconteca algum erro

     */

    public void setMaxSize(int newSize) throws CacheException {

        try {

            server.setMaxSize(newSize);

        } catch (RemoteException re) {

            for (int counter=0; counter < FAULT_TOLERANT; counter++) {

                try {

                    createServer();

                } //try

                catch (CacheException error) {

                    continue;

                } //catch

                try {

                    server.setMaxSize(newSize);

                    return;

                } //for

                catch (RemoteException error) {

                    continue;

                } //catch

                catch (CacheException error) {

                    continue;

                } //catch

            } //for

            throw new CacheException("erro conectando com cache : " + re.detail);

        }

    }



    /**

     * Muda o numero de quantidade de objetos que devem ser removidos quando

     * acontece uma falta.

     * @param qtd o novo numero de quantos objetos serao removidos apos uma falha.

     * @throws focusedCrawler.util.cache.CacheException caso aconteca algum erro

     */

    public void setRemoveQuantity(int qtd) throws CacheException {

        try {

            server.setRemoveQuantity(qtd);

        } catch (RemoteException re) {

            for (int counter=0; counter < FAULT_TOLERANT; counter++) {

                try {

                    createServer();

                } //try

                catch (CacheException error) {

                    continue;

                } //catch

                try {

                    server.setRemoveQuantity(qtd);

                } //for

                catch (RemoteException error) {

                    continue;

                } //catch

                catch (CacheException error) {

                    continue;

                } //catch

                break;

            } //for

            throw new CacheException("erro conectando com cache : " + re.detail);

        }

    }



    /**

     * Retorna o dado associado a cache dada.

     *

     * @param  key   Chave que representa o objeto na cache

     * @return o objeto procurado ou null se nao estiver na cache.

     * @see focusedCrawler.util.cache.CacheKey

     * @throws focusedCrawler.util.cache.CacheException caso aconteca algum erro

     */

    public Object get(CacheKey key) throws CacheException {

        try {

            return server.get(key);

        } catch (RemoteException re) {

            for (int counter=0; counter < FAULT_TOLERANT; counter++) {

                try {

                    createServer();

                } //try

                catch (CacheException error) {

                    continue;

                } //catch

                try {

                    return server.get(key);

                } //for

                catch (RemoteException error) {

                    continue;

                } //catch

                catch (CacheException error) {

                    continue;

                } //catch

            } //for

            throw new CacheException("erro conectando com cache : " + re.detail);

        }

    }



    /**

     * Retorna um array com dados associados as caches dadas.

     * @param  key   Array de chaves

     * @return Retorna array de objetos, respeitando a ordem das chaves.

     * @see focusedCrawler.util.cache.CacheKey

     * @throws focusedCrawler.util.cache.CacheException caso aconteca algum erro

     */

    public Object[] get(CacheKey[] key) throws CacheException {

        try {

            return server.get(key);

        } catch (RemoteException re) {

            for (int counter=0; counter < FAULT_TOLERANT; counter++) {

                try {

                    createServer();

                } //try

                catch (CacheException error) {

                    continue;

                } //catch

                try {

                    return server.get(key);

                } //for

                catch (RemoteException error) {

                    continue;

                } //catch

                catch (CacheException error) {

                    continue;

                } //catch

            } //for

            throw new CacheException("erro conectando com cache : " + re.detail);

        }

    }



    /**

     * Coloca o dado na cache., atualiza dado se a chave ja

     * estiver na cache.

     *

     * @param  key   Chave que representa o objeto na cache

     * @param  data  o objeto de dados.

     * @see focusedCrawler.util.cache.CacheKey

     * @throws focusedCrawler.util.cache.CacheException caso aconteca algum erro

     */

    public Object put(CacheKey key, Object data) throws CacheException {

        try {

            return server.put(key, data);

        } catch (RemoteException re) {

            for (int counter=0; counter < FAULT_TOLERANT; counter++) {

                try {

                    createServer();

                } //try

                catch (CacheException error) {

                    continue;

                } //catch

                try {

                    return server.put(key,data);

                } //for

                catch (RemoteException error) {

                    continue;

                } //catch

                catch (CacheException error) {

                    continue;

                } //catch

            } //for

            throw new CacheException("erro conectando com cache : " + re.detail);

        }

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

        try {

            return server.put(key, data);

        } catch (RemoteException re) {

            for (int counter=0; counter < FAULT_TOLERANT; counter++) {

                try {

                    createServer();

                } //try

                catch (CacheException error) {

                    continue;

                } //catch

                try {

                    return server.put(key,data);

                } //for

                catch (RemoteException error) {

                    continue;

                } //catch

                catch (CacheException error) {

                    continue;

                } //catch

            } //for

            throw new CacheException("erro conectando com cache : " + re.detail);

        }

    }



    /**

     * Retorna o dado associado na cache remota.

     *

     * @param  key   Chave que representa o objeto na cache

     * @return o objeto procurado

     * @see focusedCrawler.util.cache.CacheKey

     * @see focusedCrawler.util.cache.ObjectFactory

     * @throws focusedCrawler.util.cache.CacheException caso aconteca algum erro

     */

    public Object getUpdate(CacheKey key) throws CacheException {

        try {

            return server.getUpdate(key);

        } catch (RemoteException re) {

            for (int counter=0; counter < FAULT_TOLERANT; counter++) {

                try {

                    createServer();

                } //try

                catch (CacheException error) {

                    continue;

                } //catch

                try {

                    return server.getUpdate(key);

                } //for

                catch (RemoteException error) {

                    continue;

                } //catch

                catch (CacheException error) {

                    continue;

                } //catch

            } //for

            throw new CacheException("erro conectando com cache : " + re.detail);

        }

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

        try {

            return server.getUpdate(key);

        } catch (RemoteException re) {

            for (int counter=0; counter < FAULT_TOLERANT; counter++) {

                try {

                    createServer();

                } //try

                catch (CacheException error) {

                    continue;

                } //catch

                try {

                    return server.getUpdate(key);

                } //for

                catch (RemoteException error) {

                    continue;

                } //catch

                catch (CacheException error) {

                    continue;

                } //catch

            } //for

            throw new CacheException("erro conectando com cache : " + re.detail);

        }

    }



    /**

     * Sem implementacao

     */

    public void setFactory(ObjectFactory fac) {

    }



    /**

     * Sem implementacao

     */

    public ObjectFactory getFactory() {

        return null;

    }



    public void setDestroyer(ObjectDestroyer d) {

    }



    public ObjectDestroyer getDestroyer() {

        return null;

    }



    /**

     * Remove o dado referente a chave dada da cache

     *

     * @return o dado removido ou null se nao existia na cache

     */

    public Object remove(CacheKey key) throws CacheException {

        try {

            return server.remove(key);

        } catch (RemoteException re) {

            if (re.detail instanceof CacheException) {

                throw (CacheException) re.detail;

            } else {

                if (re.detail != null) re.detail.printStackTrace();

                throw new CacheException("erro conectando com cache : " + re.detail);

            }

        }

    }



    /**

     * Remove os dados referente as chaves dadas

     *

     * @return Array com dados removidos, se um dado nao estava na cache sua posicao sera nula

     *

     */

    public Object[] remove(CacheKey[] key) throws CacheException {

        try {

            return server.remove(key);

        } catch (RemoteException re) {

            if (re.detail instanceof CacheException) {

                throw (CacheException) re.detail;

            } else {

                if (re.detail != null) re.detail.printStackTrace();

                throw new CacheException("erro conectando com cache : " + re.detail);

            }

        }

    }



    /**

     * retorna a enumeracao das chaves desta cache

     */

    public Iterator getKeys() throws CacheException {

        try {

            return server.getKeys();

        } catch (RemoteException re) {

            if (re.detail instanceof CacheException) {

                throw (CacheException) re.detail;

            } else {

                if (re.detail != null) re.detail.printStackTrace();

                throw new CacheException("erro conectando com cache : " + re.detail);

            }

        }

    }



    public void clear() throws CacheException {

        try {

            server.clear();

        } catch (RemoteException re) {

            if (re.detail instanceof CacheException) {

                throw (CacheException) re.detail;

            } else {

                if (re.detail != null) re.detail.printStackTrace();

                throw new CacheException("erro conectando com cache : " + re.detail);

            }

        }

    }

}

