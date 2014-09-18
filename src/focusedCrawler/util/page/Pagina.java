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



package focusedCrawler.util.page;



import java.net.URL;

import java.util.Date;

import java.util.Vector;

import java.util.Enumeration;






public interface Pagina extends java.io.Serializable {




	public URL       endereco();




	public Date      dataQueFoiVisitado();




	public Date      dataQueFoiModificado();



	

	public int       tamanhoDoArquivo();



	public String    titulo();



	/**  @return  Caso existam, os 150 primeiros caracteres de texto,

	 * ou a primeira sentenca terminada por ponto, caso contrario,

	 * null.

	 */

	public String    paragrafo();



	/**  @return Caso existam, as <code>MAXPALAVRAS</code> palavras diferentes no texto

	 * do documento com maiores ocorrencias em ordem decrescente.

	 */

	public String[]  palavras();



	/**  @return Um array de inteiros com as ocorrencias associadas

	 * as palavras do metodo palavras().

	 */

	public int[]     ocorrencias();





	/**

     * Dada uma palavra desta pagina pagina este metodo retorna uma enumeracao das

     * posicoes desta palavra.

     * <BR> Pode se saber quais as palavras contidas no texto da pagina pelo

     * metodo <code>palavras</code>.

     * @param    palavra   uma palavra do texto da pagina.

     * @return   Um Enumeration com inteiros da classe Integer que sao as posicoes da palavra

     *           dada no documento. Caso a palavra nao exista no documento retorna <code>null</code>.

	 * as palavras do metodo palavras().

     * @see    java.util.Enumeration

     * @see    indexing.util.page.Pagina#palavras()

	 */

	public Enumeration posicoes(String palavra);



	/**  @return O numero de frames que o documento possui.

	 */

	public int       numeroDeFrames();



	/**  @return O numero de formularios.

	 */

	public int       numeroDeFormularios();



	/**  @return O numero de imagens.

	 */

	public int       numeroDeImagens();



	/**  @return Um array com as URL´s contidas no documento.

	 */

	public URL[]     links();



	/**  @return Um Vector contendo todas Strings que representam

	 * e-mail.

	 */

	public Vector    mails();

}
