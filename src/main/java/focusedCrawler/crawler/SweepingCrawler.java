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
package focusedCrawler.crawler;

import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.io.BufferedWriter;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Vector;
import java.util.Enumeration;
import java.util.Hashtable;
import java.rmi.RemoteException;

import focusedCrawler.util.string.StopList;
import focusedCrawler.util.string.StopListArquivo;
import focusedCrawler.util.download.ThreadDownloadFactoryURL;
import focusedCrawler.util.download.ThreadDownload;
import focusedCrawler.util.ParameterFile;
import focusedCrawler.util.page.Pagina;
import focusedCrawler.util.parser.PaginaURL;
import focusedCrawler.util.storage.Storage;

/**

 * <p>Description: This crawler sweeps sites from their root pages.  </p>

 *

 * <p>Copyright: Copyright (c) 2004</p>

 * @author Luciano Barbosa

 * @version 1.0

 */

public class SweepingCrawler {



        protected Hashtable hash_hosts;

        protected Hashtable hash_urls;

        protected Hashtable hash_url_altura;

        protected Vector    vector_urls;

        protected String    saida;

        protected boolean   ignorar_tipo;

        protected int       altura_maxima;

        protected StopList  stoplist;

        protected int       numAncoras;

        protected int timeout = 5000; // in seconds

        protected Hashtable sites_numUrls;

        protected ThreadDownloadFactoryURL t;


    public SweepingCrawler(String[] links,String saida, boolean ignorar_tipo, int altura_maxima,StopList stoplist,int timeout,String filter) throws Exception {

        hash_hosts = new Hashtable(links.length);
        int chute = links.length * 100;
        hash_urls = new Hashtable(chute);
        hash_url_altura = new Hashtable(chute);
        vector_urls = new Vector(chute);
        sites_numUrls = new Hashtable(links.length);
        URL link = null;
        String aux = null;

        for( int i = 0; i < links.length; i++ ) {
            System.out.println("URL ->" + links[i]);
            String aux1 = links[i];
            if(!aux1.endsWith("/"))
                aux1 = aux1.substring(0,aux1.lastIndexOf("/")+1);
            sites_numUrls.put(aux1,new Integer(0));
            try{
                link = new URL(links[i]);
            }catch(Exception e){e.printStackTrace();}

            //System.out.println("LINK ->"+link);

            aux = link.getHost();
            Vector urls = new Vector();
            urls.add(links[i]);
            hash_hosts.put(aux,urls);

            aux = link.toString();//FuncoesJDBC.URLtoString(link);

            hash_urls.put(aux,aux);

            hash_url_altura.put(aux,new Integer(0));

            //vector_urls.addElement(aux);

        }

        numAncoras = links.length;

        this.saida = saida;

        this.ignorar_tipo = ignorar_tipo;

        this.altura_maxima = altura_maxima;

        this.stoplist = stoplist;

        this.timeout = timeout;

    }

	public static String downloadPage(URL urlCon) throws IOException {
		
//		System.out.println("URL:" + urlCon.toString());

		try {
			Thread.sleep(100);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}  
	    URLConnection conn = urlCon.openConnection();
	    if(conn.getContentType()!= null && !conn.getContentType().contains("text")){
	    	return null;
	    }

	    conn.connect();
	    conn.setConnectTimeout(10000);
	    conn.setReadTimeout(10000);
	    StringBuffer buffer = new StringBuffer();
	    try{
	      BufferedReader inCon = new BufferedReader(new InputStreamReader(conn.
	          getInputStream()));
	      String inputLine;
	      while ((inputLine = inCon.readLine()) != null) {
	        buffer.append(inputLine);
	      }
	      inCon.close();
	    }catch(java.lang.IllegalArgumentException ex){
//	      System.out.println("ILLEGAL ARGUMENT!!!\n");
	      return null;
	    }
	    catch(java.net.SocketTimeoutException ex){
//	      System.out.println("TIMEOUT EXCEPTION!!!\n");
	      return null;
	    }
	    catch(java.io.FileNotFoundException ex) {
//	      System.out.println("REMOTE FILE NOT FOUND!!!\n");
	      return null;
	    }
	    catch(java.net.UnknownHostException ex) {
//	      System.out.println("UNKNOWN HOST!!!\n");
	      return null;
	    }catch(Exception ex){
//	      System.out.println("Generic Exception\n");
	      return null;

	    }
//	    System.out.println("FINISHED TO DOWNLOAD THE PAGE : " + urlCon.toString() + "\n");
//	    System.out.println(pageRes.getContent());
	    return buffer.toString();
	}
    
    private java.util.HashSet visited = new java.util.HashSet();
    /**
     * This method runs the SweepingCrawler
     */
    public void execute () {

        try {
        	int cont = 0;
            t = new ThreadDownloadFactoryURL();
            BufferedWriter writer = new BufferedWriter(new FileWriter(saida));
            java.util.Enumeration hosts = null;
            while(true) {
            	try {
            		if(hosts == null || !hosts.hasMoreElements()){
            			hosts = hash_hosts.elements();
            		}
            		Vector urls = (Vector)hosts.nextElement();
//            		Vector urls = (Vector)hash_hosts.get(host);
            		java.util.Random random = new java.util.Random();
            		int index = random.nextInt(urls.size());
            		System.out.println("####INDEX" + index);
            		System.out.println("####SIZE" + urls.size());
                    String link = (String)urls.elementAt(index); //FuncoesJDBC.URLtoString((String)vector_urls.elementAt(cont));
                    Integer alt = (Integer)hash_url_altura.get(link);
                    System.out.println("Examinando o link "+link+" com altura = "+alt);
                    //System.out.println("HASH_ALTURA " + hash_url_altura);
                    if( alt == null || visited.contains(link)) {
                        cont++;
                        continue;
                    }
                    visited.add(link);
                    int altura = alt.intValue();
                    URL url = new URL(link);
                    String arquivo = downloadPage(url);
                    Pagina pag = null;
                    URL[] links = null;
                    pag = new PaginaURL(url,0,0,0,arquivo,stoplist);
                    try{

                      FileOutputStream fout = new FileOutputStream("data" + File.separator + "data_review" + File.separator + URLEncoder.encode(link),true);
       		    	  DataOutputStream dout = new DataOutputStream(fout);
       		    	  dout.writeBytes(arquivo);
       		    	  dout.close();
                    }
                    catch (IOException ex) {
                        ex.printStackTrace();
                    }
                    links = pag.links();
                    int contadorDeUrls = 0;
                    for( int i = 0; i < links.length; i++ ) {
                        link = links[i].toString();
                        if( link.indexOf('@') < 0 ) {
                              if( belongsToScope(links[i])) {
                                if( !visited.contains(link) ) {
                                	Integer aux = new Integer(altura+1);
                                	hash_url_altura.put(link,aux);
                                	String hostTemp = links[i].getHost();
                                	Vector vectorTemp = (Vector)hash_hosts.get(hostTemp);
                                	vectorTemp.add(link);
                                	hash_hosts.put(new URL(link).getHost(), vectorTemp);
                                	vector_urls.addElement(link);
                                	contadorDeUrls++;
                                	writer.write((link.toString()) + "\n" );
                                	writer.flush();
                                }
                            }else {
                            	System.out.println("NOT VALID  Host  ->"+link);
                            }
                        }
                    }
                    Enumeration urls1 = sites_numUrls.keys();
                    while(urls1.hasMoreElements()){
                        String urlTemp = (String)urls1.nextElement();
                        if((url.toString()).indexOf(urlTemp) != -1){
                            Integer numUrls = (Integer)sites_numUrls.get(urlTemp);
                            sites_numUrls.remove(urlTemp);
                            int num = numUrls.intValue() + contadorDeUrls;
                            System.out.println("NUMERO DE LINKS " + num);
                            sites_numUrls.put(urlTemp,new Integer(num));
                        }
                    }
                }catch(Exception exc) {
                    exc.printStackTrace();
                }
                cont++;
            }
        }
        catch(Exception exc) {
            exc.printStackTrace();
        }

    }



    public boolean belongsToScope(URL url){

        boolean retorno = false;
        String host = url.getHost();
        Enumeration urls = sites_numUrls.keys();
//        System.out.println("##########SIZE:" + sites_numUrls.size() );
        while(urls.hasMoreElements()){
             String urlTemp = (String)urls.nextElement();
//             System.out.println("####ORIGINAL" + url.getHost());
//             System.out.println("####TEMP" + urlTemp);
             try {
					if(host.equals(new URL(urlTemp).getHost())){
//						System.out.println("####ORIGINAL" + url.getHost());
//			             System.out.println("####TEMP" + urlTemp);
						return true;
					}
					    
				} catch (MalformedURLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
//                if((url.toString()).indexOf(urlTemp) != -1){
//                    retorno = true;
//                }
            }
        return retorno;
    }

  
     public static void main(String[] args) {
       BufferedReader reader;
        try {
            ParameterFile config = new ParameterFile(args[0]);
            String nomeArquivoServidores = config.getParam ("LISTA_DE_URLS_VARREDURA");
            reader = new BufferedReader(new InputStreamReader(new FileInputStream(nomeArquivoServidores)));
            Vector urls = new Vector();
            String linha = null;
            while((linha = reader.readLine())!=null){
                urls.addElement(linha);
            }
            String[] links = new String[urls.size()];
            for(int i = 0;i< urls.size();i++ ) {
                links[i] = (String)urls.elementAt(i);
                System.out.println("ADICIONANDO -> "+links[i]);
            }
            String saida = config.getParam("ARQUIVO_DE_SAIDA_VARREDURA");
            System.out.println("SAIDA -> "+saida);
            String filter = config.getParam("STRING_FILTER_URL");
            if(filter == null){
              filter = "";
            }  
            boolean ignorar_tipo = false;
            ignorar_tipo = Boolean.valueOf(config.getParam("CONSIDERAR_CGI_VARREDURA")).booleanValue();
            int altura_maxima = 1;
            altura_maxima = Integer.valueOf(config.getParam("ALTURA_MAXIMA_VARREDURA")).intValue();
            StopList stoplist = new StopListArquivo (config.getParam ("STOPLIST_FILES"));
            int timeout = new Integer (config.getParam ("VARREDURA_SITE_TIMEOUT")).intValue ();
//            ParameterFile formStorageConfig = new ParameterFile(config.getParam("FORM_STORAGE_FILE"));
//            Storage formStorage = new StorageCreator(formStorageConfig).produce();
            SweepingCrawler var = new SweepingCrawler(links, saida,ignorar_tipo, altura_maxima,stoplist,timeout,filter);
           // var.setStorage(formStorage);
            var.execute();
        }
        catch(Exception exc) {
            exc.printStackTrace();
        }

    }





}

