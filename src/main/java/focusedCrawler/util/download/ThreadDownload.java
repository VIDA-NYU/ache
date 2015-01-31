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
package focusedCrawler.util.download;

import java.io.OutputStream;
import java.io.InputStream;


public abstract class ThreadDownload extends Thread {



    /**

     * Indica o alvo que deve ser tratado pelo Thread.

     */

    protected String  alvo;



    /**

     * Indica o tempo maximo que o thread tem para realizar suas atividades.

     */

    protected int timeout;



    /**

     * Indica se o thread esta pronto ou nao.

     */

    public boolean ready;



    /**

    *  Representa qualquer informacao adicional que deva ser passada para o Thread

    */

    protected String complemento;



    /**

    *  Indica se houve finalização do download por timeout

    */
    private boolean finalizadoPorTimeout = false;

    protected InputStream input;
    
    protected OutputStream out;
    
    /**
     * Cria um thread sem alvo ou tempo definido.
     */
    public ThreadDownload() {

    }



    /**

     * Cria um thread com alvo ja definido.

     *

     * @param alvo Alvo que será utilizado pelo thread.

     */

    public ThreadDownload(String alvo) {

        this.setAlvo(alvo);

    }



    /**

     * Cria um thread com alvo ja definido e timeout

     *

     * @param alvo Alvo que será utilizado pelo thread.

     * @param timeout Tempo de vida do thread.

     */

    public ThreadDownload(String alvo, int timeout) {

        this.setAlvo(alvo);

        this.setTimeout(timeout);

    }



    /**

     * Cria um thread com alvo ja definido e timeout

     *

     * @param alvo Alvo que será utilizado pelo thread.

     * @param timeout Tempo de vida do thread.

     * @param complemento Complemento de informacao do thread.

     */

    public ThreadDownload(String alvo, int timeout, String complemento) {

        this.setAlvo(alvo);

        this.setTimeout(timeout);

        this.setComplemento(complemento);

    }



    /**

     * Ajusta o alvo, onde estao os dados desejados.

     * @param alvo Novo alvo do thread.

     */

    public abstract void setAlvo(String alvo);



    /**

     * Ajusta o timeout de busca dos dados desejados.

     * @param timeout Novo tempo do thread.

     */

    public void setTimeout(int timeout) {

        this.timeout = timeout;

    }



    /**

     * Ajusta a informacao adicional que o thread deve receber. Por exemplo, se o

     * metodo HTTP seria GET ou POST.

     */

    public void setComplemento(String complemento) {

        this.complemento = complemento;

    }



    /**

     * Indica se o thread ja terminou suas atividades.

     * @return <code></b>true</b></code>, se ja terminou, <code></b>false</b></code>, caso contrario.

     */

    public boolean ready() {

        return ready;

    }



    /**

     * Ajusta o estado de execucao de um thread indicando que o loop existente no run() deve ser finalizado.

     * @param <code></b>true</b></code>, para finalizar, <code></b>false</b></code>, caso contrario.

     */

    public void setReady(boolean newReady) {

        this.ready = newReady;

    }



    /**

     * Seta se o download foi finalizado por causa do timeout

     *

     * @param foiTimeout <code> True </code> caso o download tenha finalizado pelo timeout, <code> false </code> caso contrário

     */

    protected void setFinalizadoPorTimeout(boolean foiTimeout) {

        finalizadoPorTimeout = foiTimeout;

    }



    /**

     * Indica se o download foi finalizado por causa do timeout

     *

     * @return <code> True </code> caso o download tenha finalizado pelo timeout, <code> false </code> caso contrário

     */

    public boolean getFinalizadoPorTimeout() {

        return finalizadoPorTimeout;

    }


    public InputStream getInputStream(){
    	return input;
    }
    
    public OutputStream getOutPutStream(){
    	return out;
    }

    /**

     * Retorna o texto que foi recuperado pelo thread.

     * @return Um String que contem todos os dados coletados pelo thread.

     */

    public abstract String getDados();



    /**

     * Libera os recursos utilizados pelo thread, como Stream, conexoes com SGBD`s, etc.

     */

    public abstract void finalizar();

}

