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

import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;

import java.util.Vector;

import java.util.Enumeration;

import java.io.PrintStream;

import java.io.IOException;

import java.io.FileOutputStream;

import focusedCrawler.util.DoubleLinkedList;
import focusedCrawler.util.DoubleLinkedListNode;
import focusedCrawler.util.Timer;


/**

 * Essa classe implementa uma cache que é tratada com uma
 * fila de objetos, quando um objeto novo é inserido
 * ele é colocado no inicio da fila.
 * E os elementos que são removidos por falha na cache
 * são os do final da fila(cache FIFO).
 *
 */

public class CacheFIFO implements Cache {

    /**
     *  tamanho maximo da cache
     * @see focusedCrawler.util.cache.Cache
     */

    protected int              cache_size;

    /**
     *  quantidade de itens removidos quando acontece uma falha.
     * @see focusedCrawler.util.cache.Cache
     */

    protected int              cache_remove;

    /**
     *  hashtable que guarda os dados da cache
     * contem mapeamento : CacheKey -> DoubleLinkedList
     */

    protected Hashtable        cache;

    /**
     *  Lista dos dados da cache, lista de CacheEntry
     * @see focusedCrawler.util.DoubleLinkedListNode
     * @see focusedCrawler.util.DoubleLinkedList
     */

    protected DoubleLinkedList list;

    /**
     *  Free-list,    // lista de CacheEntry
     * @see focusedCrawler.util.DoubleLinkedListNode
     * @see focusedCrawler.util.DoubleLinkedList
     */

//    protected DoubleLinkedList freelist;

    /**
     * ObjectFactory desta cache
     * @see focusedCrawler.util.cache.ObjectFactory
     */

    protected ObjectFactory    factory;

    protected ObjectDestroyer  destroyer;

    /**
     * Muda o ObjectFactory da cache.
     * @param fac a nova fabrica de objetos
     * @see focusedCrawler.util.cache.ObjectFactory
     */

    public void setFactory(ObjectFactory fac) {
        factory = fac;
    }

    /**
     * Retorna o ObjectFactory da cache.
     * @return a fabrica desta cache
     * @see focusedCrawler.util.cache.ObjectFactory
     */

    public ObjectFactory getFactory() {
        return factory;
    }

    /**
     * muda o destroyer da cache
     */

    public void setDestroyer(ObjectDestroyer d) {
        destroyer = d;
    }

    /**
     * muda o destroyer da cache
     */

    public ObjectDestroyer getDestroyer() {
        return destroyer;
    }

    /**
     * Construtor da Classe
     *
     *
     * @param cache_size o tamanho maximo desta cache
     * @param qtd_remove a quantidade de itens removidos quando acontece uma falha
     *
     * @see focusedCrawler.util.cache.Cache
     */

    public CacheFIFO(int cache_size, int qtd_remove) {
        if (cache_size < 1) {
            throw new IllegalArgumentException("tamanho de cache invalido :"
                                               + cache_size);
        }
        this.cache_size = cache_size;
        setRemoveQuantity(qtd_remove);
        this.cache = new Hashtable(cache_size + 10);
        this.list = new DoubleLinkedList();
//        this.freelist = new DoubleLinkedList();
//        for (int i = 0; i < cache_size; i++) {
//            CacheEntry ce = new CacheEntry();
//            freelist.insertFirst(ce);
//        }
        System.out.println("CACHE> cache_max_size = " + this + " "
                           + cache_size);
        System.out.println("CACHE> qtd_remove = " + this + " " + qtd_remove);
    }

    /**
     * Construtor da Classe.
     * coloca a quantidade de itens removidos quando ha falha
     * em metade do tamanho da cache dado + 1
     *
     * @param cache_size o tamanho maximo da cache
     *
     * @see focusedCrawler.util.cache.Cache
     */

    public CacheFIFO(int cache_size) {
        this(cache_size, (cache_size / 2) + 1);
    }

    /**
     * Construtor da Classe
     * cria uma cache com tamanho maximo 10
     *
     * @see focusedCrawler.util.cache.Cache
     */

    public CacheFIFO() {

        this(10);

    }



    /**

     * retorna o tamanho maximo da cache. Se algum objeto novo for

     * inserido e a cache estiver com o tamanho maximo algum ou alguns

     * outros objetos deverao ser removidos para o novo objeto ser inserido.

     * @return  tamanho maximo da cache

     */

    public int getMaxSize() {

        return cache_size;

    }



    /**

     * muda Tamanho maximo da cache.

     * @param  newSize o novo tamanho maximo

     * @throws focusedCrawler.util.cache.CacheException caso aconteca algum erro

     */

    public synchronized void setMaxSize(int newSize) throws CacheException {

        if (newSize < 0) {
            throw new IllegalArgumentException("tamanho de cache invalido :"
                                               + cache_size);
        }

        // remove alguns
        if (newSize < size()) {
            removeSomeDataFromCache(size() - newSize);
            //remove entries da freelist
//            for (int i = newSize; i > cache_size; i--) {
//                freelist.removeHead();
//            }
        } else {
            //insere mais na freelist
//            for (int i = cache_size; i < newSize; i++) {
//                CacheEntry ce = new CacheEntry();
//                freelist.insertFirst(ce);
//            }
        }
        cache_size = newSize;
    }

    /**
     * Muda o numero de quantidade de objetos que devem ser removidos quando
     * acontece uma falta.
     * @param qtd o novo numero de quantos objetos serao removidos apos uma falha.
     */

    public void setRemoveQuantity(int qtd) {
        if (qtd < 1) {
            throw new IllegalArgumentException("Quantidade a Remover :"
                                               + qtd);
        }
        if (qtd > cache_size) {
            qtd = cache_size;
        }
        cache_remove = qtd;
    }

    /**
     * numero de objectos que serao removidos quando acontecer uma falta.
     * @return numero de objectos que serao removidos quando acontecer uma falta.
     */

    public int getRemoveQuantity() {
        return cache_remove;
    }

    /**
     * retorna o tamanho da cache
     * @return  tamanho da cache
     */

    public int size() {
        return list.size();
    }

    // private int log_calls=0;
    // private int log_fault=0;

    private int log_cache_flush = 0;

    /**
     * Retorna uma enumeracao contendo as chaves da cache
     *
     * @return uma enumeracao
     * @see focusedCrawler.util.cache.CacheKey
     * @throws focusedCrawler.util.cache.CacheException caso aconteca algum erro
     */

    public synchronized Iterator getKeys() throws CacheException {
        HashSet keys = new HashSet();
        DoubleLinkedListNode node = list.head();
        while(node != null) {
            CacheEntry ce = (CacheEntry) node.data();
            keys.add(ce.getKey());
            node = node.next();
        }
        return keys.iterator();
    }



    /**
     * Retorna o dado associado a cache dada.
     *
     * @param  key   Chave que representa o objeto na cache
     * @return o objeto procurado ou null se nao estiver na cache.
     * @see focusedCrawler.util.cache.CacheKey
     * @throws focusedCrawler.util.cache.CacheException caso aconteca algum erro
     */

    public final synchronized Object get(CacheKey key) throws CacheException {
    //    log_calls++;
      //  System.out.println("CACHE> get before '"+key+"': "+list);

        DoubleLinkedListNode l =
            (DoubleLinkedListNode) cache.get(key.hashKey());

        if (l != null) {
            CacheEntry ce = (CacheEntry) l.data();
            dataFound(l, ce);
            ce.hit();
            // System.out.println("CACHE> get after '"+key+"': "+list);
            return ce.dado;
        } else {
            // log_fault++;
            // System.out.println("CACHE> fault : "+log_fault+"/"+log_calls);
            // cache fault
            return null;
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
        Object[] data = new Object[key.length];
        for(int i=key.length-1;i>=0;i--){
            data[i] = get(key[i]);
        }
        return data;
    }

    protected synchronized void dataFound(DoubleLinkedListNode l,

            CacheEntry ce) throws CacheException {

}


    /**
     * Coloca o dado na cache, atualiza dado se a chave ja
     * estiver na cache
     *
     * @param  key   Chave que representa o objeto na cache
     * @param  data  o objeto de dados.
     * @see focusedCrawler.util.cache.CacheKey
     * @throws focusedCrawler.util.cache.CacheException caso aconteca algum erro
     */

    public final synchronized Object put(CacheKey key, Object data) throws CacheException {

        DoubleLinkedListNode l =
            (DoubleLinkedListNode) cache.get(key.hashKey());

        // if (l!=null) throw new CacheException("dado ja esta na cache");
        // System.out.println("CACHE> put before '"+key+"': "+list);

        CacheEntry ce;
        if (l == null) {
            l = putInterno(key, data);
            ce = (CacheEntry) l.data();
        } else {
            ce = (CacheEntry) l.data();
            ce.setData(data);
        }
        ce.hit();
        // System.out.println("CACHE> put before '"+key+"': "+list);
        return null;

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
     * Coloca a chave e o dado associado na cache
     * testa se a cache esta cheia, se estiver remove algo para colocar o novo
     *
     * @param key    a chave do dado a ser inserido
     * @param data   o dado
     *
     * @throws CacheException
     *
     * @see focusedCrawler.util.cache.CacheKey
     * @see focusedCrawler.util.cache.CacheException
     */

    protected synchronized DoubleLinkedListNode putInterno(CacheKey key, Object data)
            throws CacheException {
//        System.out.println("CACHE> SIZE " + size() + ", MAX_SIZE " + getMaxSize());
        if (size() >= getMaxSize()) {
            removeSomeDataFromCache(getRemoveQuantity());
        }

        if (size() >= getMaxSize()) {
//            System.out.println("ERRO LIBERANDO DADOS NA CACHE FIFO(putInterno),size="+size()+", freelist.size="+freelist.size());
            throw new CacheException("ERRO LIBERANDO DADOS NA CACHE FIFO");
        }

//        DoubleLinkedListNode l = freelist.removeHead();
//        CacheEntry ce = (CacheEntry) l.data();
        
        CacheEntry ce = new CacheEntry();
        ce.setKey(key);
        ce.setData(data);
        DoubleLinkedListNode node = new DoubleLinkedListNode(ce);
        cache.put(key.hashKey(), node);
        list.insertNodeFirst(node);
//        System.out.println("CACHE> INSERTING "+ key.toString() + " CACHE SIZE:" + cache.size());
        dataIsNew(node, ce);
        return node;
        // System.out.println("CACHE> putInt out "+Thread.currentThread());
    }
    
    
    protected synchronized void dataIsNew(DoubleLinkedListNode l,

            CacheEntry ce) throws CacheException {

    }

    public int getTotalDeChamdas(){
        return totalDeChamadas;
    }

    public int getTotalDeFalhas(){
        return totalDeFalhas;
    }

    public long getTempoDeProducao(){
        return tempoDeProducao;
    }

    public int getTotalDeAcertos(){
        return totalDeAcertos;
    }

    public long getTempoDeAcerto(){
        return tempoDeAcerto;
    }

    public long getTempoDoGet(){
        return tempoGet;
    }

    int totalDeChamadas = 0;

    long tempoGet = 0;

    int totalDeFalhas = 0;

    long tempoDeProducao = 0;

    int totalDeAcertos = 0;

    long tempoDeAcerto = 0;



    /**
     * Retorna o dado associado a cache dada.
     * caso o objeto nao esteja na cache, cria um novo utilizando o ObjectFactory
     * e coloca na cache. se a cache estiver cheia, remove alguns dado da cache.
     *
     * @param  key   Chave que representa o objeto na cache
     * @return o objeto procurado
     * @see focusedCrawler.util.cache.CacheKey
     * @see focusedCrawler.util.cache.ObjectFactory
     * @throws focusedCrawler.util.cache.CacheException caso aconteca algum erro ou a fabrica nao esteja setada
     */

    public Object getUpdate(CacheKey key) throws CacheException {
        Object value = get(key);
        if (value == null) {
            if (factory == null) throw new CacheException("factory not set");
            put(key, value = factory.produce(key));
        }
        return value;
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

     * @throws focusedCrawler.util.cache.CacheException caso aconteca algum erro ou a fabrica nao esteja setada

     */

    public Object[] getUpdate(CacheKey[] key) throws CacheException {

        Object[] data = get(key);

        int notfound = 0;

        for (int i = 0; i < data.length; i++) if (data[i] == null) notfound++;

        if (notfound > 0) {

            if (factory == null) throw new CacheException("factory not set");



            CacheKey[] key2 = new CacheKey[notfound];

            int idx[] = new int[notfound];

            notfound = 0;

            for (int i = 0; i < data.length; i++) {

                if (data[i] == null) idx[notfound++]=i;

            }

            for (int i = 0; i < notfound; i++) key2[i] = key[idx[i]];



            Object[] newData = factory.produce(key2);

            put(key2, newData);

            for (int i = 0; i < notfound; i++) data[idx[i]] = newData[i];

        }



        return data;

    }





    protected boolean _do_log /* = false; // */ = false;

    public void setDoLog(boolean val) {

        _do_log = val;

    }

    protected PrintStream _log_out = System.out;

    public void setLogOut(PrintStream xxx) {
        _log_out = xxx;
    }



    public void log_cache() {
        PrintStream _log = _log_out;
        if (_log == null) return;
        String flush_str_log = "CACHE> flush " + this + " : " + log_cache_flush + ", cache_size="+cache_size+", cache_remove="+cache_remove;
        flush_str_log += "\n Listagem TOP-DOWN em " + new java.util.Date();
        int idx = 1;
        DoubleLinkedListNode walk = list.last();
        int cont = 0;
        long sum = 0;
        while(walk != null) {
            Object _dado = walk.data();
            flush_str_log += "\n"+idx+". " + _dado;
            if (_dado != null && _dado instanceof CacheEntry) {
                cont++;
                sum += ((CacheEntry) _dado).tempo_de_vida();
            }
            walk = walk.previous();
            idx++;
        }
        if (cont > 0) flush_str_log += "\nTempo de vida medio = " + Timer.toString((long)sum/cont);
        if (_log_out == null) {
            try {
                _log_out = new PrintStream(new FileOutputStream("/home/bright/spool/logs/cache.flush.log",true));
            } catch(IOException ioe) {
                System.out.println("flush_log_init error: " + ioe.getMessage());
            }
        }
        new Throwable().printStackTrace(_log);
        _log.println(flush_str_log);
        _log.flush();
    }

    /**
     * Remove <CODE>N</CODE> objeto da cache e atualiza estruturas.
     * Nesta cache FIFO remove os ultimos da lista.
     *
     * @param n o numero de elementos a remover da cache
     * @throws focusedCrawler.util.cache.CacheException caso aconteca algum erro
     */

    public synchronized void removeSomeDataFromCache(int n)
            throws CacheException {

        log_cache_flush++;
        if (_do_log) {
            if (_log_out == null) setLogOut(System.out);
            _log_out.println("--------------------------CACHE_BEFORE_CLEAR---------");
            log_cache();
            System.out.println("CACHE> flush " + this + " [" + n + "] : " + log_cache_flush);
        }

        // System.out.println("CACHE> flush cache before "+list);
        for (int i = 0; i < n && size() > 0; i++) {
            removeSomeDataFromCache();
        }
        if (_do_log) {
            _log_out.println("--------------------------CACHE_AFTER_CLEAR----------");
            log_cache();
        }
    }

    /**
     * Este metodo é chamado quando a cache esta cheia.
     * Remove um unico elemento da cache.
     * Nesta implementacao remove o ultimo da lista.
     *
     * @throws focusedCrawler.util.cache.CacheException caso aconteca algum erro
     */

    public synchronized void removeSomeDataFromCache()
            throws CacheException {

        DoubleLinkedListNode last = list.removeLast(); //remove o ultimo da lista usada
//        freelist.insertNodeFirst(last);
        CacheEntry ce = (CacheEntry) last.data(); // pega o cacheEntry e a chave
        remove(ce.key);
        freeData(ce);
        last = null;
        ce = null;
//        System.out.println(">>>>>>>>>>LAST:" + list.last().data().toString());
    }

    protected int _cont_removed = 0;

    protected long _sum_lifetime = 0;



    /**
     * Libera recursos do dado que esta sendo removido
     *
     * @param ce a CacheEntry que foi removida
     * @throws focusedCrawler.util.cache.CacheException caso aconteca algum erro
     * @see focusedCrawler.util.cache.CacheEntry
     */

    protected synchronized void freeData(CacheEntry ce) throws CacheException {
        _cont_removed++;
        _sum_lifetime += ce.tempo_de_vida();
        if (_do_log) {
            _log_out.println("CACHE.FLUSH.REMOVENDO "+ce+", total_removidos="+_cont_removed
                        +", tempo de vida medio=" + Timer.toString((long)_sum_lifetime/_cont_removed));
            _log_out.flush();
        }
        if (destroyer != null) {
            destroyer.destruct(ce.getData());
        }
        ce.clear();
        ce.key = null;
        ce.dado = null;
    }

    /**
     * Remove da cache o dado de chave passada
     *
     * @param  key A chave a ser removida
     *
     * @throws focusedCrawler.util.cache.CacheException caso aconteca algum erro
     *
     * @see focusedCrawler.util.cache.CacheEntry
     */

    public Object remove(CacheKey key) throws CacheException {
        DoubleLinkedListNode l = (DoubleLinkedListNode) cache.remove(key.hashKey());
        if (l == null) {
            return null;
        }
        CacheEntry ce = (CacheEntry) l.data();
        list.removeNode(l);
//        freelist.insertNodeFirst(l);
        return ce.dado;
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
     * remove todos os dados da cache
     */

    public void clear() throws CacheException {
        removeSomeDataFromCache(getMaxSize());
    }

    public String toString() {
        return super.toString() + "[factory="+this.factory+",cache_size="+cache_size+",cache.size()="+cache.size()+"cache_remove="+cache_remove+"]";
    }

}

