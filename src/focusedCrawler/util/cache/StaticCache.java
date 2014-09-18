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



import java.util.Hashtable;
import java.util.Iterator;

import java.util.Enumeration;

import java.util.Vector;



/**
 * Implementação de uma cache estática.
 * Para guardar dados em uma hashtable.
 * Esta cache é basicamente uma interface para uma hashtable.
 *
 */

public class StaticCache implements Cache {

    private int           FLUSH_SIZE;
    private int           MAX_SIZE;
    private Hashtable     cache;
    private ObjectFactory factory;
    private ObjectDestroyer destroyer;

    public StaticCache() {
        this(1000);
    }



    public StaticCache(int init_size) {

        if (init_size < 1) {
            throw new IllegalArgumentException("tamanho de cache invalido :" + init_size);
        }
        FLUSH_SIZE = 5000;
        MAX_SIZE = 100000;
        this.cache = new Hashtable(init_size + 10);
        System.out.println("StaticCache> cache = " + this + " " + init_size);
        System.out.println("StaticCache> MAX_SIZE = " + MAX_SIZE + " FLUSH_SIZE = " + FLUSH_SIZE);
    }



    /**
     * @see focusedCrawler.util.cache.Cache#setFactory()
     */

    public void setFactory(ObjectFactory _factory) {

        this.factory = _factory;

    }



    /**

     * @see focusedCrawler.util.cache.Cache#getFactory()

     */

    public ObjectFactory getFactory() {

        return factory;

    }



    /**

     * @see focusedCrawler.util.cache.Cache#maxSize()

     */

    public int getMaxSize() {

        return MAX_SIZE;

    }



    /**

     * @see focusedCrawler.util.cache.Cache#setMaxSize()

     */

    public synchronized void setMaxSize(int newSize) throws CacheException {

        MAX_SIZE = newSize;

        System.out.println("StaticCache.setMaxSize()> MAX_SIZE = " + MAX_SIZE + " FLUSH_SIZE = " + FLUSH_SIZE);

    }



    /**

     * @see focusedCrawler.util.cache.Cache#setRemoveQuantity()

     */

    public void setRemoveQuantity(int qtd) {

        FLUSH_SIZE = qtd;

        System.out.println("StaticCache.setRemoveQuantity()> MAX_SIZE = " + MAX_SIZE + " FLUSH_SIZE = " + FLUSH_SIZE);

    }



    public int getRemoveQuantity() {

        return FLUSH_SIZE;

    }



    /**

     * @see focusedCrawler.util.cache.Cache#removeQuantity()

     */

    public int removeQuantity() throws CacheException {

        int cont = 0;

        if( cache != null ) {

            Enumeration e = cache.keys();

            while( cont < FLUSH_SIZE && e.hasMoreElements() ) {

                Object old = cache.remove(e.nextElement());

                if (destroyer != null) {

                    destroyer.destruct(old);

                }

                cont++;

            }

        }

        System.out.println("StaticCache> removi "+cont+" elementos.");

        return cont;

    }



    /**

     * @see focusedCrawler.util.cache.Cache#size()

     */

    public int size() {

        return cache.size();

    }



    /**

     * @see focusedCrawler.util.cache.Cache#get()

     */

    public final synchronized Object get(CacheKey key) throws CacheException {

        return cache.get(key.hashKey());

    }



    /**

     * Retorna um array com dados associados as caches dadas.

     * @param  key   Array de chaves

     * @return Retorna array de objetos, respeitando a ordem das chaves.

     * @see focusedCrawler.util.cache.CacheKey

     * @throws focusedCrawler.util.cache.CacheException caso aconteca algum erro

     */

    public Object[] get(CacheKey[] key) throws CacheException {

        Object[] data = new Object[key.length];

        for(int i=key.length-1;i>=0;i--){

            data[i] = get(key[i]);

        }

        return data;

    }



    /**

     * @see focusedCrawler.util.cache.Cache#put()

     */

    public final synchronized Object put(CacheKey key, Object data) throws CacheException {

        return cache.put(key.hashKey(), data);

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

        Object[] curData = new Object[key.length];

        for(int i=key.length-1;i>=0;i--){

            curData[i] = put(key[i],data[i]);

        }

        return curData;

    }



    /**

     * @see focusedCrawler.util.cache.Cache#getUpdate()

     */



    int contadorDeAcertos = 0;

    int contadorDeErros = 0;



    public int getContadorDeAcertos(){

        return contadorDeAcertos;

    }



    public int getContadorDeErros(){

        return contadorDeErros;

    }



    long tempoTotalDeAcerto = 0;



    public long getTempoDeAcerto(){

        return tempoTotalDeAcerto;

    }



    long tempoTotalDeProduce = 0;



    public long getTempoProduce(){

        return tempoTotalDeProduce;

    }



    public synchronized Object getUpdate(CacheKey key) throws CacheException {

        long tempoAcerto = System.currentTimeMillis();

        Object retorno = get(key);

        tempoTotalDeAcerto += (System.currentTimeMillis() - tempoAcerto);

        if (retorno == null) {

            long tempoDeProduce = System.currentTimeMillis();

            if (factory == null) {

                throw new CacheException("Fabrica nao atribuida!");

            } //if

            retorno = factory.produce(key);

            tempoTotalDeProduce += (System.currentTimeMillis() - tempoDeProduce);

            if( cache.size() > MAX_SIZE ) {

                System.out.println("StaticCache> ESTOURO, size = "+cache.size()+", inseri "+retorno);

                removeQuantity();

            }

            contadorDeErros++;

        }

        else

            contadorDeAcertos++;

        return retorno;

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

        Object[] data = new Object[key.length];

        int[] faltando = new int[key.length];

        int faltando_size = 0;

        for(int i=key.length-1;i>=0;i--){

            data[i] = get(key[i]);

            if (data[i] == null) {

                faltando[faltando_size++] = i;

            }

        }



        if (faltando_size > 0) {

            if (factory == null) {

                throw new CacheException("Fabrica nao atribuida!");

            } //if

            // recupera os que estao faltando

            CacheKey[] keyFaltando = new CacheKey[faltando_size];

            for(int i=0;i<faltando_size;i++) {

                keyFaltando[i] =  key[faltando[i]];

            }

            Object[] dataFaltando = factory.produce(keyFaltando);

            for(int i=0;i<faltando_size;i++) {

                data[faltando[i]] = dataFaltando[i];

            }

        }



        return data;

    }





    /**

     * @see focusedCrawler.util.cache.Cache#remove()

     */

    public Object remove(CacheKey key) throws CacheException {

        return cache.remove(key.hashKey());

    }



    /**

     * Remove os dados referente as chaves dadas

     *

     * @return Array com dados removidos, se um dado nao estava na cache sua posicao sera nula

     *

     */

    public Object[] remove(CacheKey[] key) throws CacheException {

        Object[] data = new Object[key.length];

        for(int i=key.length-1;i>=0;i--){

            data[i] = remove(key[i]);

        }

        return data;

    }



    /**

     * @see focusedCrawler.util.cache.Cache#getKeys()

     */

    public Iterator getKeys() throws CacheException {

        return cache.values().iterator();

    }



    public void setDestroyer(ObjectDestroyer destroyer) {

        this.destroyer = destroyer;

    }



    public ObjectDestroyer getDestroyer() {

        return destroyer;

    }



    public void clear() throws CacheException {

        Enumeration keys = cache.keys();

        while(keys.hasMoreElements()) {

            Object key = keys.nextElement();

            Object old = cache.remove(key);

            if ( destroyer != null) {

                destroyer.destruct(old);

            }

        }

    }





}



