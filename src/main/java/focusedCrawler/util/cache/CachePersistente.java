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

 * Implementacao de uma cache LRU.

 * nesta cache quando um dado novo vai ser inserido e

 * a cache esta cheia, os objetos que serao removidos

 * sao aqueles usados ha mais tempo, onde apos n flush será salvo o estado

 * atual da cahce

 *

 * @author Oscar Miranda

 * @version %I%, %G%

 */




import java.io.*;

import java.util.Enumeration;
import java.util.Iterator;

import java.util.StringTokenizer;

import java.lang.reflect.*;

import focusedCrawler.util.DoubleLinkedListNode;



public class CachePersistente implements Cache {

    private int limite_flushs = 0;

    private int numero_flushs = 0;



    private Cache cache;

    private Constructor construtorKey;

    private Constructor construtorData;



    private String fileName ;

    /**

     * Construtor da Classe

     *

     * @param cache_size o tamanho maximo desta cache

     * @param qt_rm a quantidade de itens removidos quando acontece uma falha

     *

     */

    public CachePersistente(Cache cache, int limite_flushs, String file,

      String key_classname, String data_classname) throws CacheException {

        this.limite_flushs = limite_flushs;

        this.cache = cache;

        try{

          Class clsKey = Class.forName(key_classname);

          Class clsData = Class.forName(data_classname);



          this.construtorKey = clsKey.getConstructor( new Class[]{java.lang.String.class});

          this.construtorData = clsData.getConstructor( new Class[]{java.lang.String.class});

        } catch (ClassNotFoundException e) {

            throw new CacheException("Nao foi encontrada as classes" + key_classname +

                " e " + data_classname);

        } catch (NoSuchMethodException ne){

            throw new CacheException("Nao existe construtor " + key_classname +"( String str)"+

                " ou " + data_classname+"( String str)");

        }

        this.fileName = file;

        ler_estado();

    }



    /**

     * retorna o tamanho da cache

     *

     * @return  tamanho da cache

     */

    public int size() {

      return cache.size();

    }



    /**

     * retorna o tamanho maximo da cache. Se algum objeto novo for

     * inserido e a cache estiver com o tamanho maximo algum ou alguns

     * outros objetos deverao ser removidos para o novo objeto ser inserido.

     *

     * @return  tamanho maximo da cache

     */

    public int getMaxSize(){

      return cache.getMaxSize();

    }



    /**

     * numero de objectos que serao removidos quando acontecer uma falta.

     *

     * @return numero de objectos que serao removidos quando acontecer uma falta.

     */

    public int getRemoveQuantity(){

      return cache.getRemoveQuantity();

    }

    /**

     * muda Tamanho maximo da cache.

     *

     * @param  newSize o novo tamanho maximo

     * @throws focusedCrawler.util.cache.CacheException caso aconteca algum erro

     */

    public void setMaxSize(int newSize) throws CacheException{

      cache.setMaxSize(newSize);

    }

    /**

     * Muda o numero de quantidade de objetos que devem ser removidos quando

     * acontece uma falta.

     *

     * @param qtd o novo numero de quantos objetos serao removidos apos uma falha.

     * @throws focusedCrawler.util.cache.CacheException caso aconteca algum erro

     */

    public void setRemoveQuantity(int qtd) throws CacheException{

      cache.setRemoveQuantity(qtd);

    }

    /**

     * Retorna o dado associado a cache dada.

     *

     * @param  key   Chave que representa o objeto na cache

     * @return o objeto procurado ou null se nao estiver na cache.

     * @see focusedCrawler.util.cache.CacheKey

     * @throws focusedCrawler.util.cache.CacheException caso aconteca algum erro

     */

    public Object get(CacheKey key) throws CacheException{

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

        throw new CacheException("metodo nao implementado");

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

     */

    public Object getUpdate(CacheKey key) throws CacheException{

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

        throw new CacheException("metodo nao implementado");

    }



    /**

     * Muda o ObjectFactory da cache.

     *

     * @param fac a nova fabrica de objetos

     * @see focusedCrawler.util.cache.ObjectFactory

     */

    public void setFactory(ObjectFactory fac){

      cache.setFactory(fac);

    }



    /**

     * muda o destroyer

     */

    public void setDestroyer(ObjectDestroyer d) {

        cache.setDestroyer(d);

    }



    public ObjectDestroyer getDestroyer() {

        return cache.getDestroyer();

    }



    /**

     * Retorna o ObjectFactory da cache.

     *

     * @return a fabrica desta cache

     * @see focusedCrawler.util.cache.ObjectFactory

     */

    public ObjectFactory getFactory() {

      return cache.getFactory();

    }



    /**

     * Remove o dado referente a chave dada da cache

     *

     * @return o dado removido ou null se nao existia na cache

     */

    public Object remove(CacheKey key) throws CacheException{

      return cache.remove(key);

    }

    /**

     * retorna a enumeracao das chaves desta cache

     */

    public Iterator getKeys() throws CacheException{

      return cache.getKeys();

    }



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

    public final synchronized Object put(CacheKey key,

                                       Object data) throws CacheException {

        DoubleLinkedListNode l =

            (DoubleLinkedListNode) cache.get(key);



        CacheEntry ce;

        if (l == null) {

            if ( limite_flushs > 0)

              numero_flushs++;

        }



        if ( limite_flushs < numero_flushs)

          salva_estado();





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

        throw new CacheException("metodo nao implementado");

    }





    public void setLimiteFlush(int limite){

      if (limite < limite_flushs) {

        if ( limite_flushs < numero_flushs)

          try {

            salva_estado();

          } catch (CacheException ce){

            System.out.println("++Nao foi possivel gravar o estado atual da cache");

          }

      }

      limite_flushs = limite;

    }



    public boolean salva_estado() throws CacheException{

      boolean resposta = false;

      try {

        BufferedWriter out = new BufferedWriter( new FileWriter(fileName));

        Iterator enumkeys = cache.getKeys();

        while ( enumkeys.hasNext()){

           Object key = enumkeys.next();

           Object data = cache.get( (ObjectCacheKey) key);

           System.out.println("write " + key + " " + data);

           System.out.println("write " + key.toString() + " " + data.toString());

           out.write(key.toString() + " " + data.toString() + "\n");

        }

        out.close();

        resposta = true;

      } catch (IOException e) {

            throw new CacheException("Erro ao gravar no arquivo " +fileName);

      }

      return resposta;

    }



    public boolean ler_estado() throws CacheException{

      boolean resposta = false;

      try {

        BufferedReader in = new BufferedReader(new FileReader(fileName));

        String str;

        StringTokenizer st;

        while ((str = in.readLine()) != null) {

          st = new StringTokenizer(str);

          if ( st.countTokens() >= 2) {

            String str_key = st.nextToken();

            String str_data = st.nextToken();



            Object objdata = construtorData.newInstance( new Object[]{str_data});

            Object objkey = construtorKey.newInstance( new Object[]{str_key});



            cache.put( new ObjectCacheKey(objkey), objdata);

          }else{

            throw new CacheException("Tokens menores que 2 no arquivo" +fileName);

          }

        }

        in.close();

        resposta = true;

      } catch (IOException e) {

          throw new CacheException("Erro na leitura do arquivo " +fileName);

      } catch (InvocationTargetException ie){

        throw new CacheException("Invocacao errada");

      } catch (IllegalAccessException iae){

        throw new CacheException("Acesso ilegal");

      } catch (InstantiationException ie ){

            throw new CacheException("Tipos invalidos no arquivo " +fileName);

      }

      return resposta;

    }



    /**

     * Remove os dados referente as chaves dadas

     *

     * @return Array com dados removidos, se um dado nao estava na cache sua posicao sera nula

     *

     */

    public Object[] remove(CacheKey[] key) throws CacheException {

        throw new CacheException("metodo nao implementado");

    }



    public void clear() throws CacheException {

        throw new CacheException("metodo nao implementado");

    }



    public static void main (String[] args){

        try {



        CacheLRU cache_urls = new CacheLRU(10, 2);



        String file_name = args[1];

        CachePersistente cache_per = new CachePersistente(cache_urls, 1, file_name,

        "java.lang.String", "java.lang.Integer");



          ObjectCacheKey key = new ObjectCacheKey("http://www.hpg.com.br/");

          System.out.println("get("+key+") = " + cache_per.get(key));



          key = new ObjectCacheKey("http://www.fenticeira.hpg.com.br/");

          System.out.println("get("+key+") = " + cache_per.get(key));

          key = new ObjectCacheKey("http://www.cosmo.hpg.com.br/");

          System.out.println("get("+key+") = " + cache_per.get(key));

          cache_per.salva_estado();

        } catch (CacheException ce){

           System.out.println("Fudeu a cache!!!");

        }

    }

}