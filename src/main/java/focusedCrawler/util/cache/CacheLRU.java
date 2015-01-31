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

import focusedCrawler.util.DoubleLinkedListNode;

/**

 * Implementacao de uma cache LRU.
 * nesta cache quando um dado novo vai ser inserido e
 * a cache esta cheia, os objetos que serao removidos
 * sao aqueles usados ha mais tempo.

 * <BR> A implementação baseia-se na cache FIFO so que
 * quando um dado é consultado ele vai para o inicio da fila
 * isso garante que quando a cache estiver cheia e
 * um dado novo for inserido, os dados removidos(os do final da lista)
 * serão os utilizados há mais tempo(cache LRU).
 *
 */

public class CacheLRU extends CacheFIFO {

    /**
     * Construtor da Classe
     *
     *
     * @param cache_size o tamanho maximo desta cache
     * @param qt_rm a quantidade de itens removidos quando acontece uma falha
     *
     */

    public CacheLRU(int cache_size, int qt_rm) {
        super(cache_size, qt_rm);
    }

    /**
     * Construtor da Classe
     *
     * coloca a quantidade de itens removidos quando ha falha
     * em metade do tamanho da cache dado + 1
     *
     * @param cache_size o tamanho maximo da cache
     */

    public CacheLRU(int cache_size) {
        super(cache_size);
    }

    /**
     * Construtor da Classe
     * cria uma cache com tamanho maximo 10
     */

    public CacheLRU() {
        super();
    }

    /**
     * Atualiza estruturas da cache quando o objeto for encontrado na cache.
     * move o elemento encontrado para o inicio da lista(cache LRU).
     *
     * @param l o no da lista ligada que o qual dado foi encontrado
     * @param ce a entry que foi achada
     * @throws focusedCrawler.util.cache.CacheException caso aconteca algum erro
     * @see focusedCrawler.util.DoubleLinkedListNode
     * @see focusedCrawler.util.cache.CacheEntry
     */

    protected synchronized void dataFound(DoubleLinkedListNode l,
                             CacheEntry ce) throws CacheException {
        l.moveFirst();
        updateList(ce);
    }



    /**
     * atualiza os dados dependentes do dado encontrado.
     * por exemplo, numa estrutura em arvore,
     * este metodo deveria colocar os filhos do no da arvore
     * encontrado no inicio da fila.
     *
     * @param ce a entry que foi achada
     * @throws focusedCrawler.util.cache.CacheException caso aconteca algum erro
     * @see focusedCrawler.util.DoubleLinkedListNode
     * @see focusedCrawler.util.cache.CacheEntry
     */
    protected synchronized void updateList(CacheEntry ce) throws CacheException {

    }



    /**
     * Atualiza estruturas da cache quando o objeto nao for encontrado na cache.
     *
     * @param l o no da lista ligada do dado novo
     * @param ce a entry nova
     * @throws focusedCrawler.util.cache.CacheException caso aconteca algum erro
     * @see focusedCrawler.util.DoubleLinkedListNode
     * @see focusedCrawler.util.cache.CacheEntry
     */

    protected synchronized void dataIsNew(DoubleLinkedListNode l,

                             CacheEntry ce) throws CacheException {

        updateList(ce);

    }

}