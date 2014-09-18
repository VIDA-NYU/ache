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

import java.io.InputStream;

import java.io.OutputStream;

import java.net.URL;

import java.util.Iterator;

import focusedCrawler.util.Log;



/**
 * Define a interface de uma classe que realiza download de documentos.
 * @author Thiago Santos.
 * @version 1.0
 */

public interface Downloader {
    public static String PROTOCOL         = "HTTP";
    public static String PROTOCOL_0       = "HTTP/1.0";

    public static String PROTOCOL_1       = "HTTP/1.1";

    public static String METHOD_GET       = "GET";

    public static String METHOD_POST      = "POST";



    /**
     * Indica o estado desconhecido. Por exemplo, logo após a criação do downloader.
     */
    public static int UNKNOWN       = 0;

    /**
     * Indica que houve muitos redirecionamentos da url.
     */
    public static int LOOP          = UNKNOWN + 1;

    /**
     * Indica que o downloader conseguiu realizar a conexão com sucesso.
     */
    public static int OK            = LOOP + 1;

    /**
     * Indica um falha decorrente de IO.
     */

    public static int FAIL_IO       = OK + 1;

    /**

     * Indica um falha decorrente de um timeout.

     */

    public static int FAIL_TIMEOUT  = FAIL_IO + 1;

    /**

     * Indica falha decorrente de redirecionamento.

     */

    public static int FAIL_REDIRECT = FAIL_TIMEOUT + 1;

    /**

     * Indica falha decorrente do protocolo.

     */

    public static int FAIL_PROTOCOL = FAIL_REDIRECT + 1;



    /**

     * Chaves que indicam as respostas do protocolo.

     */

    public static String RESPONSE_PROTOCOL = "Protocol";

    public static String RESPONSE_CODE     = "ResponseCode";

    public static String RESPONSE_MESSAGE  = "ResponseMessage";



    /**

     * Ajusta se deve-se mostrar os logs normais.

     */

    void setShowNormalLog(boolean newNormalLog) throws DownloaderException;



    /**

     * Indica o log normal da conexao.

     */

    void setNormalLog(Log newNormalLog) throws DownloaderException;



    /**

     * Ajusta se deve-se mostrar os logs normais.

     */

    void setShowErrorLog(boolean newErrorLog) throws DownloaderException;



    /**

     * Indica o log de erro da conexao.

     */

    void setErrorLog(Log newErrorLog) throws DownloaderException;



    /**

     * Ajusta o id do downloader.

     */

    void setId(String newId) throws DownloaderException;

    /**

     * Retorna o id do downloader.

     */

    String getId() throws DownloaderException;



    /**

     * Limpa as propriedades de requisição.

     */

    void clearRequestProperties() throws DownloaderException;

    /**

     * Ajusta uma propriedade do pedido.

     */

    void setRequestProperty(String name, String value) throws DownloaderException;



    /**

     * Ajusta o protocolo do downloader.

     */

    void setProtocol(String newProtocol) throws DownloaderException;

    /**

     * Indica o protocolo do download.

     */

    String getProtocol() throws DownloaderException;



    /**

     * Ajusta o tipo de método do downloader.

     */

    void setMethod(String newMethod) throws DownloaderException;

    /**

     * Retorna o método usado pelo downloader.

     */

    String getMethod() throws DownloaderException;



    /**

     * Ajusta o timeout do downloader.

     */

    void setTimeout(int timeout) throws DownloaderException;

    /**

     * Retorna o valor do timeout.

     */

    int getTimeout() throws DownloaderException;



    /**

     * Ajusta a URL que será o alvo do download.

     */

    void setUrlTarget(URL newUrlTarget) throws DownloaderException;

    /**

     * Retorna a URL alvo.

     */

    URL getUrlTarget() throws DownloaderException;



    /**

     * Ajusta o downloader para seguir os redirecionamentos.

     */

    void setFollowRedirects(boolean newFollowRedirects) throws DownloaderException;

    /**

     * Indica se devemos seguir os redirecionamentos.

     */

    boolean isFollowRedirects() throws DownloaderException;



    /**

     * Ajusta o número de redirecionamento que o downloader pode seguir.

     */

    void setFollowRedirectsTolerance(int newFollowRedirectsTolerance) throws DownloaderException;

    /**

     * Indica o número de redirecionamento que o downloader pode seguir.

     */

    int getFollowRedirectsTolerance() throws DownloaderException;





    /**

     * Limpa as propriedades de requisição.

     */

    void clearResponseProperties() throws DownloaderException;



    /**

     *  Realiza a conexao com o alvo, retornando se a conexão foi realizada

     *  com sucesso, constante OK, houve timeout na conexão (FAIL_TIMEOUT), ou

     *  houve problemas com o redirecionamento da URL (FAIL_REDIRECT).

     */

    void connect() throws DownloaderException;



    /**

     * Retorna o estado do downloader.

     */

    int getStatus() throws DownloaderException;



    /**

     * Retorna o protocolo da resposta da conexão.

     */

    String getResponseProtocol() throws DownloaderException;



    /**

     * Retorna o código da resposta da conexão.

     */

    int getResponseCode() throws DownloaderException;



    /**

     * Retorna o tamanho do conteudo.

     */

    int getContentLength() throws DownloaderException;



    /**

     * Retorna a String de resposta da conexão.

     */

    String getResponseMessage() throws DownloaderException;



    /**

     * Retorna o tipo de conteúdo do InputStream.

     */

    String getContentType() throws DownloaderException;



    /**

     * Retorna a data de modificação da URL.

     */

    long getLastModified() throws DownloaderException;



    /**

     * Retorna uma propriedade presente no header, este método leva em consideração palavras

     * com letras maiusculas e minusculas.

     */

    String getResponseProperty(String name) throws DownloaderException;



    /**

     * Retorna uma propriedade presente no header, informando deve se considerar as

     * diferencas entre as letras maiusculas e minusculas.

     */

    String getResponseProperty(String name, boolean caseSensitive) throws DownloaderException;



    /**

     * Lista os campos presentes no header.

     */

    Iterator listResponse() throws DownloaderException;



    /**

     * Retorna o  canal de comunicação da conexão.

     */

    OutputStream getOutputStream() throws DownloaderException;



    /**

     * Retorna o conteúdo.

     */

    InputStream getInputStream() throws DownloaderException;



    /**

     * Libera recursos do download.

     */

     void close() throws DownloaderException;



    /**

     * Algumas vezes o downloader pode vir a apresentar estados inconsistentes, ou a utilizar recursos

     * sem liberá-los, o que pode levar o sistema que usa um downloader a um estado de falha. O método

     * abaixo fornece a informação se o sistema que usa o downloader deve ou não ser finalizado por causa

     * de problemas decorrentes do downloader.

     */

     boolean isShutdown() throws DownloaderException;

}