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
/*--- formatted by Jindent 2.1, (www.c-lab.de/~jindent) ---*/

package focusedCrawler.util.parser;


import java.io.*;
import java.net.*;
import java.util.*;

import focusedCrawler.util.page.Pagina;
import focusedCrawler.util.string.Acentos;
import focusedCrawler.util.string.StopList;
import focusedCrawler.util.string.StopListArquivo;


public class PaginaURL implements Pagina {

    public long timeToParse = 0;
    private int 			   maxPosValue = 0;
    private int                notaDaImagem;
    private int                MAXPALAVRAS = -1;
    public static final int    MAX_PARAGRAPH_SIZE = 255;
    private URL                pagina = null;
    private long               visitado = 0;
    private long               modificado = 0;
    private int                tamanho = 0;
    private String             titulo = "";
    private String             paragrafo = "";
    private String[]           palavras = new String[0];
    private int[]              ocorrencias = new int[0];
    private String[]           palavrasMeta = new String[0];
    private int[]              ocorrenciasMeta = new int[0];
    private int                fileHashCode;
    private double             relevance = 0;

    /**
     * palavras_ordem \uFFFD um Hastable que mapeia uma palavras(um termo) a sua lista
     * de posicoes no documento. <BR>
     * Esta lista contem as posicoes do termo no texto. Ex:
     * no texto acima a lista da palavra "um" seria <3,9>. <BR>
     * Os tipo do Hashtable sao String que mapeia um Vector de Integer.
     */
    private Hashtable          palavra_posicoes = new Hashtable();
    private Hashtable          palavra_pontos = new Hashtable();
    private Hashtable          palavra_posicoes_meta = new Hashtable();
    private int                frames = 0;
    private int                forms = 0;
    private int                images = 0;

    private transient Vector   texto = new Vector();
    private transient Vector   textoMeta = new Vector();
    private URL[]              URLabsolutas = null;
    private Vector             links = new Vector();
    private Vector             mailList =
        new Vector();    // Vector de e-mails para o lights
    private boolean            noindex = false;
    private boolean            nofollow = false;
    public static boolean      USAR_DESCRIPTION = false;
    public static int          MAX_SIZE_HOST = 255;

    // protected    Escopo meuEscopo = new EscopoWEB();
    private transient StopList stoplist;
    private boolean            ordenarTermos = true;

    public void setRelevance(double relev){
      this.relevance = relev;
    }

    public double getRelevance(){
      return this.relevance;
    }

    /**
     * O arquivo completo da pagina
     */
    private transient String   arquivo = "";

    /**
     * Declara\uFFFD\uFFFDo do M\uFFFDtodo
     *
     *
     * @return
     *
     * @see
     */
    public URL endereco() {
        return pagina;
    }

    public URL getURL() {
        return pagina;
    }

    /**
     * Declara\uFFFD\uFFFDo do M\uFFFDtodo
     *
     *
     * @return
     *
     * @see
     */
    public Date dataQueFoiVisitado() {
        return new Date(visitado);
    }

    /**
     * Declara\uFFFD\uFFFDo do M\uFFFDtodo
     *
     *
     * @return
     *
     * @see
     */
    public Date dataQueFoiModificado() {
        return new Date(modificado);
    }

    /**
     * Declara\uFFFD\uFFFDo do M\uFFFDtodo
     *
     *
     * @return
     *
     * @see
     */
    public int tamanhoDoArquivo() {
        return tamanho;
    }

    /**
     * Declara\uFFFD\uFFFDo do M\uFFFDtodo
     *
     *
     * @return
     *
     * @see
     */
    public String titulo() {
        return titulo;
    }

    /**
     * Declara\uFFFD\uFFFDo do M\uFFFDtodo
     *
     *
     * @return
     *
     * @see
     */
    public String paragrafo() {
        return paragrafo;
    }

    /**
     * Declara\uFFFD\uFFFDo do M\uFFFDtodo
     *
     *
     * @return
     *
     * @see
     */
    public String[] palavras() {
        return palavras;
    }

    /**
     * Declara\uFFFD\uFFFDo do M\uFFFDtodo
     *
     *
     * @return
     *
     * @see
     */
    public int[] ocorrencias() {
        return ocorrencias;
    }


    /**
     * Declara\uFFFD\uFFFDo do M\uFFFDtodo
     *
     *
     * @return
     *
     * @see
     */
    public String[] palavrasMeta() {
        return palavrasMeta;
    }

    /**
     * Declara\uFFFD\uFFFDo do M\uFFFDtodo
     *
     *
     * @return
     *
     * @see
     */
    public int[] ocorrenciasMeta() {
        return ocorrenciasMeta;
    }



    /**
     * Declara\uFFFD\uFFFDo do M\uFFFDtodo
     *
     *
     * @return
     *
     * @see
     */
    public int numeroDeFrames() {
        return frames;
    }

    /**
     * Declara\uFFFD\uFFFDo do M\uFFFDtodo
     *
     *
     * @return
     *
     * @see
     */
    public int numeroDeFormularios() {
        return forms;
    }

    /**
     * Declara\uFFFD\uFFFDo do M\uFFFDtodo
     *
     *
     * @return
     *
     * @see
     */
    public int numeroDeImagens() {
        return images;
    }

    /**
     * Declara\uFFFD\uFFFDo do M\uFFFDtodo
     *
     *
     * @return
     *
     * @see
     */
    public Vector mails() {
        return mailList;
    }

    
    
    /**
     * Declara\uFFFD\uFFFDo do M\uFFFDtodo
     *
     *
     * @return
     *
     * @see
     */
    public URL[] links() {
        if (URLabsolutas == null) {
            URLabsolutas = new URL[links.size()];

            for (int i = 0; i < links.size(); i++) {
                try {
                    URLabsolutas[i] = new URL(links.elementAt(i).toString());
                } catch (Throwable t) {
                }    // ignora
             }
        }

        return URLabsolutas;
    }

    /**
     * Declara\uFFFD\uFFFDo do M\uFFFDtodo
     *
     *
     * @param palavra
     *
     * @return
     *
     * @see
     */
    public Enumeration posicoes(String palavra) {
        Vector v = (Vector) palavra_posicoes.get(palavra);

        if (v != null) {
            return v.elements();
        } else {
            return null;
        }
    }

    /**
     * Fornece todos o atributos da pagina ja em formato HTML
     */
    public String toString() {
        String str = "<p>" + "<a href=\"" + pagina.toExternalForm().trim()
                     + "\">" + titulo + "</a>    " + paragrafo + "<p>"
                     + " Lista de palavras:    " + this.listapalavras()
                     + "<p>" + " Lista de links:    " + this.listalinks()
                     + "<p>" + " Lista de e-mails :    " + this.listamails()
                     + "<p>" + " N\uFFFD de frames  =  " + frames
                     + ",    N\uFFFD de formularios  =  " + forms
                     + ",    N\uFFFD de imagens  =  " + images + "<p>"
                     + " Visitado  =  " + new Date(visitado)
                     + ",    modificado  =  " + new Date(modificado)
                     + ",    tamanho  =  " + tamanho;

        return str;
    }

    /**
     * Declara\uFFFD\uFFFDo do M\uFFFDtodo
     *
     *
     * @return
     *
     * @see
     */
    private String listapalavras() {
        if (palavras == null) {
            return null;
        }

        String t1 = "";

        for (int i = 0; i < palavras.length; i++) {
            t1 = t1 + "  " + palavras[i] + "[" + ocorrencias[i] + "], ";
        }

        return t1;
    }

    /**
     * Declara\uFFFD\uFFFDo do M\uFFFDtodo
     *
     *
     * @return
     *
     * @see
     */
    private String listalinks() {
        if (URLabsolutas == null) {
            return null;
        }

        String t1 = "";

        for (int i = 0; i < URLabsolutas.length; i++) {
            t1 = t1 + ",  " + URLabsolutas[i].toString().trim();
        }

        return t1;
    }

    /**
     * Declara\uFFFD\uFFFDo do M\uFFFDtodo
     *
     *
     * @return
     *
     * @see
     */
    private String listamails() {
        if (mailList == null) {
            return null;
        }

        String t1 = "";

        for (int i = 0; i < mailList.size(); i++) {
            t1 = t1 + ",  " + mailList.elementAt(i);
        }

        return t1;
    }

    /**
     * Construtor que recebe como parametro uma String
     * @param     strurl    String que representa a URL a ser avaliada.
     * @param     stoplist  o Stoplist
     * @exception  IOException Caso haja problemas com a conexao ou de manipulacao de Streams.
     * @exception  MalformedURLException  Caso a string srturl represente uma URL mal formada.
     */
    public PaginaURL(String strurl, StopList stoplist)
            throws IOException, MalformedURLException {
        this(new java.net.URL(strurl), stoplist);
    }

    public PaginaURL(URL url, StopList stoplist) throws IOException {
        this(url, url.openConnection(), stoplist);
    }

    /**
     * Construtor que nao esta sujeito a Exceptions, e possui parametros bem
     * definidos.
     * @param      url      URL a ser visitada
     * @param      vist     Data em que o documento foi visitado
     * @param      mod      Data em que foi modificado
     * @param      tam      Tamanho do arquivo
     * @param      arquivo  String que representa todo o arquivo
     * @param      escopo   o escopo da pagina
     */

    /*
     * public PaginaURL(URL url,long vist,long mod,int tam,String arquivo,Escopo escopo) {
     * this(url,vist,mod,tam,arquivo,false,false,escopo);
     * }
     */

    /**
     * Construtor que nao esta sujeito a Exceptions, e possui parametros bem
     * definidos.
     * @param      url      URL a ser visitada
     * @param      vist     Data em que o documento foi visitado
     * @param      mod      Data em que foi modificado
     * @param      tam      Tamanho do arquivo
     * @param      arquivo  String que representa todo o arquivo
     * @param      noindex  true para nao processar o texto da pagina
     * @param      nofollow true para nao criar lista de links
     * @param      escopo   o escopo da pagina
     */

    /*
     * public PaginaURL(URL url,long vist,long mod,int tam,String arquivo,boolean noindex, boolean nofollow,Escopo escopo) {
     * this(url,vist,mod,tam,arquivo,false,false,escopo,-1);
     * }
     */

    /**
     * Construtor que nao esta sujeito a Exceptions, e possui parametros bem
     * definidos.
     * @param      url      URL a ser visitada
     * @param      vist     Data em que o documento foi visitado
     * @param      mod      Data em que foi modificado
     * @param      tam      Tamanho do arquivo
     * @param      arquivo  String que representa todo o arquivo
     * @param      noindex  true para nao processar o texto da pagina
     * @param      nofollow true para nao criar lista de links
     * @param      escopo   o escopo da pagina
     * @param      max      o tamanho maximo do centroide
     */

    /*
     * public PaginaURL(URL url,long vist,long mod,int tam,String arquivo,boolean noindex, boolean nofollow,Escopo escopo, int max) {
     * this(url,vist,mod,tam,arquivo,noindex,nofollow,escopo,max,new StopListArquivo());
     * }
     */

    /**
     * Construtor que nao esta sujeito a Exceptions, e possui parametros bem
     * definidos.
     * @param      url      URL a ser visitada
     * @param      vist     Data em que o documento foi visitado
     * @param      mod      Data em que foi modificado
     * @param      tam      Tamanho do arquivo
     * @param      arquivo  String que representa todo o arquivo
     * @param      escopo   o escopo da pagina
     * @param      max      o tamanho maximo do centroide
     * @param      stoplist o stoplist
     */
    public PaginaURL(URL url, long vist, long mod, int tam,
                         String arquivo, StopList stoplist) {

        // public PaginaURL(URL url,long vist,long mod,int tam,String arquivo,Escopo escopo,StopList stoplist) {
        this(url, vist, mod, tam, arquivo, false, false, -1, stoplist);
    }

    /**
     * Construtor que nao esta sujeito a Exceptions, e possui parametros bem
     * definidos.
     * @param      url      URL a ser visitada
     * @param      vist     Data em que o documento foi visitado
     * @param      mod      Data em que foi modificado
     * @param      tam      Tamanho do arquivo
     * @param      arquivo  String que representa todo o arquivo
     * @param      noindex  true para nao processar o texto da pagina
     * @param      nofollow true para nao criar lista de links
     * @param      escopo   o escopo da pagina
     * @param      max      o tamanho maximo do centroide
     * @param      stoplist o stoplist da pagina
     */
    public PaginaURL(URL url, long vist, long mod, int tam,
                         String arquivo, boolean noindex, boolean nofollow,
                         int max, StopList stoplist) {

        // public PaginaURL(URL url,long vist,long mod,int tam,String arquivo,boolean noindex, boolean nofollow,Escopo escopo, int max, StopList stoplist) {
        // meuEscopo = escopo;
        if (max > 0) {
            MAXPALAVRAS = max;
        } else {
            MAXPALAVRAS = -1;
        }

        pagina = url;
        visitado = vist;
        modificado = mod;
        tamanho = tam;
        this.noindex = noindex;
        this.nofollow = nofollow;
        this.stoplist = stoplist;
        this.arquivo = arquivo;

        // if(!arquivo.equals("")) separadorTextoCodigo(new StringBuffer(arquivo));
        if (!arquivo.equals("")) {
            long t1 = System.currentTimeMillis();
            separadorTextoCodigo(arquivo);
            timeToParse = (System.currentTimeMillis() -t1);

            // heuristicaImagem();
            // System.runFinalizersOnExit(true);
        }
    }

    public PaginaURL(URL url, URLConnection conexao, StopList sl) throws IOException {
        this(url, conexao, -1, sl);
    }

    public PaginaURL(URL url, URLConnection conexao,
                         int max, StopList sl) throws IOException {
        this(url, conexao.getDate(), conexao.getLastModified(),
             conexao.getContentLength(), conexao.getInputStream(), max,
             sl);

//        System.out.println("CONEXAO: RESPONSE CODE = " + ((HttpURLConnection) conexao).getResponseCode());

        URL url_final = ((HttpURLConnection) conexao).getURL();

//        System.out.println("CONEXAO: GET URL       = " + url_final);

//        if (!url_final.equals(url)) {
//            System.out.println("A URL '" + url + "' foi redirecionada para '" + url_final + "'");
//        } else {
//            System.out.println("URL OK");
//        }

        ((HttpURLConnection) conexao).disconnect();
    }

    /**
     * Construtor que recebe uma URL, uma URLConnection e um InputStream
     * @param      url       URL a ser avaliada
     * @param      vist      Data em que o documento foi visitado
     * @param      mod       Data em que foi modificado
     * @param      tam       Tamanho do arquivo
     * @param      file      o inputstream do arquivo
     * @param      max       o tamanho maximo do centroide
     * @param      stoplist o stoplist da pagina
     * @exception  IOException caso haja problemas com a conexao ou de manipulacao de Streams.
     */
    public PaginaURL(URL url, long vis, long mod, int tam,
                         InputStream file, int max) throws IOException {
        this(url, vis, mod, tam, file, max, null);
    }

    /**
     * Construtor que recebe uma URL, uma URLConnection e um InputStream
     * @param      url       URL a ser avaliada
     * @param      vist      Data em que o documento foi visitado
     * @param      mod       Data em que foi modificado
     * @param      tam       Tamanho do arquivo
     * @param      file      o inputstream do arquivo
     * @param      max       o tamanho maximo do centroide
     * @param      stoplist o stoplist da pagina
     * @exception  IOException caso haja problemas com a conexao ou de manipulacao de Streams.
     */
    public PaginaURL(URL url, long vis, long mod, int tam,
                         InputStream file, int max,
                         StopList stoplist) throws IOException {
        // super(url,vis,mod,tam,file,max,stoplist);
        if (max > 0) {
            MAXPALAVRAS = max;
        } else {
            MAXPALAVRAS = -1;
        }

        pagina = url;
        visitado = vis;
        modificado = mod;
        tamanho = tam;

        // StringBuffer arquivo =  converteArquivo(file);
        String arquivo = converteArquivo(file);

        this.arquivo = arquivo;
        this.stoplist = stoplist;

        if (!arquivo.equals("")) {
            long t1 = System.currentTimeMillis();
            separadorTextoCodigo(arquivo);
            timeToParse = (System.currentTimeMillis() -t1);

            // heuristicaImagem();
            // System.runFinalizersOnExit(true);
        }
    }

    private void loadHashCodes(){
    	codes.put("&#1040;","Ð�");codes.put("&#1072;","Ð°");codes.put("&#1041;","Ð‘");codes.put("&#1073;","Ð±");codes.put("&#1042;","Ð’");
    	codes.put("&#1074;","Ð²");codes.put("&#1043;","Ð“");codes.put("&#1075;","Ð³");codes.put("&#1044;","Ð”");codes.put("&#1076;","Ð´");
    	codes.put("&#1045;","Ð•");codes.put("&#1077;","Ðµ");codes.put("&#1046;","Ð–");codes.put("&#1078;","Ð¶");codes.put("&#1047;","Ð—");
    	codes.put("&#1079;","Ð·");codes.put("&#1048;","Ð˜");codes.put("&#1080;","Ð¸");codes.put("&#1049;","Ð™");codes.put("&#1081;","Ð¹");
    	codes.put("&#1050;","Ðš");codes.put("&#1082;","Ðº");codes.put("&#1051;","Ð›");codes.put("&#1083;","Ð»");codes.put("&#1052;","Ðœ");
    	codes.put("&#1084;","Ð¼");codes.put("&#1053;","Ð�");codes.put("&#1085;","Ð½");codes.put("&#1054;","Ðž");codes.put("&#1086;","Ð¾");
    	codes.put("&#1055;","ÐŸ");codes.put("&#1087;","Ð¿");codes.put("&#1056;","Ð ");codes.put("&#1088;","Ñ€");codes.put("&#1057;","Ð¡");
    	codes.put("&#1089;","Ñ�");codes.put("&#1058;","Ð¢");codes.put("&#1090;","Ñ‚");codes.put("&#1059;","Ð£");codes.put("&#1091;","Ñƒ");
    	codes.put("&#1060;","Ð¤");codes.put("&#1092;","Ñ„");codes.put("&#1061;","Ð¥");codes.put("&#1093;","Ñ…");codes.put("&#1062;","Ð¦");
    	codes.put("&#1094;","Ñ†");codes.put("&#1063;","Ð§");codes.put("&#1095;","Ñ‡");codes.put("&#1064;","Ð¨");codes.put("&#1096;","Ñˆ");
    	codes.put("&#1065;","Ð©");codes.put("&#1097;","Ñ‰");codes.put("&#1066;","Ðª");codes.put("&#1098;","ÑŠ");codes.put("&#1067;","Ð«");
    	codes.put("&#1099;","Ñ‹");codes.put("&#1068;","Ð¬");codes.put("&#1100;","ÑŒ");codes.put("&#1069;","Ð­");codes.put("&#1101;","Ñ�");
    	codes.put("&#1070;","Ð®");codes.put("&#1102;","ÑŽ");codes.put("&#1071;","Ð¯");codes.put("&#1103;","Ñ�");

    }
    
    /**
     * Converte um InputStream, para uma String.
     * @param      in      ImputStream a ser convertido
     * @return     a String correspondente a in
     * @exception  IOException
     */

    // private StringBuffer converteArquivo(InputStream in) throws IOException {
    private String converteArquivo(InputStream in) throws IOException {

        /*
         * StringBuffer arquivo = new StringBuffer();
         * int i;
         * try {
         * while ((i = in.read()) != -1) {
         * arquivo.append((char)i);
         * }
         * } catch(IOException ioe) {
         * in.close();
         * throw ioe;
         * }
         * in.close();
         * return arquivo;
         * }
         */

        // System.out.println("CONVERTENDO O ARQUIVO "+endereco());
        StringBuffer   arquivo = new StringBuffer();
        BufferedReader bin = new BufferedReader(new InputStreamReader(in));
        String         inputLine;

        try {
            while ((inputLine = bin.readLine()) != null) {
                arquivo.append(inputLine).append("\n");
            }
        } catch (IOException ioe) {
            bin.close();

            throw ioe;
        }

        bin.close();

        // System.out.println("ARQUIVO "+endereco()+" CONVERTIDO");
        // return arquivo;
//        System.out.println(arquivo.toString());
//        FileOutputStream fout = new FileOutputStream("/home/lbarbosa/parallel_corpus/pc_crawler/tst1");
//        Writer out = new OutputStreamWriter(fout, "UTF8");
//        out.write(arquivo.toString());
//        out.close();
        return arquivo.toString();
    }

    /*
     * String result = null;
     * try {
     * int tamanho = in.available();
     * byte[] file = new byte[tamanho];
     * in.read(file);
     * result = new String(file);
     * } catch(IOException ioe) {
     * in.close();
     * throw ioe;
     * }
     * in.close();
     * return new StringBuffer(result);
     * }
     */

    private boolean filterURL = false;

    private Vector around = new Vector();
    private Vector linkNeigh = new Vector();

    public void setFilterWorsOfURLs(boolean filterURL){
      this.filterURL = filterURL;
    }

    public LinkNeighborhood[] getLinkNeighboor(){
      HashSet<String> unique = new HashSet<String>();
      Vector<LinkNeighborhood> tempLN = new Vector<LinkNeighborhood>();
      for (int i = 0; i < linkNeigh.size(); i++) {
        LinkNeighborhood ln = (LinkNeighborhood)linkNeigh.elementAt(i);
        String id = ln.getAnchorString() + ln.getLink().toString()+ln.getAroundString();
        if(!unique.contains(id)){
        	unique.add(id);
            int pointer = ln.getAroundPosition();
            Vector aroundTemp = new Vector();
            for (int j = pointer - (10 + ln.getNumWordsAnchor()); j < pointer + 10; j++) {
              if(j >=0 && j <around.size() && (j < pointer - ln.getNumWordsAnchor() || j > pointer-1)){
                aroundTemp.add(((String)around.elementAt(j)).toLowerCase());
              }
            }
            String[] around = new String[aroundTemp.size()];
            aroundTemp.toArray(around);
            ln.setAround(around);
            if(getURL().getHost().equals(ln.getLink().getHost())){
            	ln.setSameSite(true);
            }
            tempLN.add(ln);
        }
      }
      LinkNeighborhood[] lns = new LinkNeighborhood[tempLN.size()];
      tempLN.toArray(lns);
      return lns;
    }

    
    private Vector  imagens = new Vector();
    
    protected void separadorTextoCodigo(String arquivo) {    // arquivo equivale ao codigo HTML da pagina
        if(codes.size() == 0){
        	loadHashCodes();	
        }
    	
//    	System.out.println(arquivo);	
        boolean obj_isRDF = false;
        boolean ignorar_espacos = true;
        boolean tag_tipo_fim = false;
        boolean tag_tipo_vazia = true;
        boolean em_script = false;
        boolean ehInicioALT = true;
        boolean em_titulo = false;
        boolean em_option = false;
        boolean em_comentario = false;
        int num_comentario = 0;
        int     PONTUACAO_PALAVRAS_TEXTO       = 2;
        int     PONTUACAO_PALAVRAS_OPTION      = 1;
        int     PONTUACAO_PALAVRAS_URL         = 3;
        int     PONTUACAO_PALAVRAS_META        = 1;
        int     PONTUACAO_PALAVRAS_TITULO      = 7;
        int     PONTUACAO_PALAVRAS_DESCRIPTION = 5;
        int     PONTUACAO_PALAVRAS_ALT         = 1;
        int    posicao_da_palavra 			   = 1;
        int numOfHtmlTags = 0;

        // UTILIZANDO AS PALAVRAS DA URL COMO INFORMACAO TEXTUAL
        if (pagina != null && !filterURL) {


            StringTokenizer url_pontos = new StringTokenizer(pagina.getHost(),"./:");

            while (url_pontos.hasMoreTokens()) {
                String parte_host = url_pontos.nextToken();

                if (!parte_host.equals("www")
                    &&!parte_host.equals("org")
                    &&!parte_host.equals("gov")
                    &&!parte_host.equals("com")
                    &&!parte_host.equals("br")) {

                    boolean adicionou = adicionaAoVetorDeTexto(parte_host);

                    if( adicionou ) {
                        adicionaTermoPosicao(parte_host,posicao_da_palavra);    // atualiza o centroide
                        adicionaPontuacaoTermo(parte_host,PONTUACAO_PALAVRAS_URL);
                        String parte_host_sem_acento = Acentos.retirarNotacaoHTMLAcentosANSI(parte_host);
                        if( !parte_host_sem_acento.equals(parte_host) ) {
                            adicionou = adicionaAoVetorDeTexto(parte_host_sem_acento);
                            if( adicionou ) {
                                adicionaTermoPosicao(parte_host_sem_acento,posicao_da_palavra);    // atualiza o centroide
                                adicionaPontuacaoTermo(parte_host_sem_acento,PONTUACAO_PALAVRAS_URL);
                            }
                        }
                        posicao_da_palavra++;
                    }
                }
            }

        }

        boolean em_body = false;
        boolean em_meta_robots = false;
        boolean tagScript = false;
        boolean tagTitulo = false;
        boolean tagBody = false;
        boolean tagOption = false;
        int     pos_caracter_especial = -1;
        char    quote_char = '\0';
        URL     base = pagina;    // pagina = URL da pagina atual...

        Vector  frames = new Vector();
        char    c = '\0';
        char    ant1 = '\0';
        char    ant2 = '\0';
        int     n = 0;
        int     n_anterior = 0;
        String  str = "";
        String anchor = "";
        int numOfwordsAnchor = 0;

        LinkNeighborhood ln = null;
        String  tagName = "";
        String lastTag = "";
        String  atributo = "";

        boolean insideATag = false;

        boolean em_meta_description = false; // thiago
        String  str_da_metatag_description = null; // thiago



        final int INICIO = 1;
        final int TAG_NAME = 2;
        final int TOKEN_PALAVRA = 3;
        final int PALAVRA = 4;
        final int ATRIBUTO = 5;
        final int FECHANDO = 6;
        final int IGUAL = 7;
        final int VALOR = 8;
        final int META_TAG = 10;
        final int ALT_TAG = 11;
        int       estado = INICIO;

        try {

//          FileOutputStream fout = null;
//          DataOutputStream dout = null;
            //System.out.println("FORM!!! : " + form.getURL());
//            try {
//              fout = new FileOutputStream("/home/lbarbosa/test");
//               dout = new DataOutputStream( fout );
//              dout.writeBytes("begin");
//
//            }
//            catch (FileNotFoundException ex) {
//              ex.printStackTrace();
//                 }
//            catch (IOException ex) {
//              ex.printStackTrace();
//         }

            while (n < arquivo.length()) {
                if (n_anterior < n) {                       /* we advanced a character */
                    ant1 = ant2;
                    ant2 = c;
                }

                n_anterior = n;
                c = arquivo.charAt(n);
//                System.out.print(c+"");
//                int ascii = (int) c;
//                System.out.print(ascii);
//                System.out.println("");
//                dout.writeBytes(c+"");
//                dout.flush();
//                if(c=='\u0000'){
//                	organizaDados();
//                	return;
//                }
                if (em_comentario && num_comentario > 0) {
                    if ((ant1 == '-') && (ant2 == '-') || (c == '>')) {
                        num_comentario--;
                        if (num_comentario == 0)
                            em_comentario = false;
                    }

                    n++;
                } else if (ignorar_espacos) {
                    if (Character.isWhitespace(c)) {
                        n++;
                    } else {
                        ignorar_espacos = false;
                    }
                } else {
                    boolean fimDeString = false;

                    switch (estado) {

                    case INICIO:

                        /* INICIO - Esperando texto ou caracter de abertura de tag '<' */

                        // System.out.println("Entrei no inicio e caractere=" + c);
                        if (c == '<') {
                            estado = TAG_NAME;
                            tagName = "";
                            tag_tipo_fim = false;
                            em_meta_robots = false;
                            n++;
                        } else {
                            estado = TOKEN_PALAVRA;
                            pos_caracter_especial = -1;
                        }

                        quote_char = '\0';

                        break;

                    case TOKEN_PALAVRA:
//                    	if(str.contains("1044")){
//                    		System.out.println("TEST");
//                    	}
                        /* faz o token da string */
                        if ((caracterFazParteDePalavra(c)) || (c == ';')
                                || (c == '&')) {
                            str += converteChar(c);
                            n++;
                            int begin = str.indexOf("&#");
                            int end = str.indexOf(";");
                            if(begin != -1 && end != -1){
                            	String specialchar = str.substring(begin+2,end);
    							try {
                                	int hex = Integer.parseInt(specialchar);
        							char uni = (char)hex;
        							String unicode =  uni + "";
        							str = str.substring(0,begin) + unicode;
//        							System.out.println(unicode);
                                	pos_caracter_especial = -1;
                                	continue;
								} catch (Exception e) {
									// TODO: handle exception
								}
                            }
                            if(str.toLowerCase().contains("&ntilde;")){
                            	str = str.toLowerCase().replace("&ntilde;", "n");
                            	pos_caracter_especial = -1;
                            	continue;
                            }
                            if(str.contains("Ã±")){
                            	str = str.replace("Ã±", "n");
                            	pos_caracter_especial = -1;
                            	continue;
                            }
                            if (c == '&') {
                                pos_caracter_especial = n;
                            } else   
//                            	System.out.println(str + ":" + pos_caracter_especial);
                            	if (pos_caracter_especial != -1) {
                                int  posicao = str.length()
                                               - (n - pos_caracter_especial)
                                               - 1;
                                char ch = caracterEspecial(str, posicao);
                                
                                if (ch != '\0') {
                                    if (caracterFazParteDePalavra(ch)) {
                                        str = str.substring(0, posicao)
                                              + converteChar(ch);
                                    } else {
                                        str = str.substring(0, posicao);
                                        estado = PALAVRA;

                                        if (em_titulo) {
                                            titulo += str + ch;
                                        }
                                    }
                                }
                                if ((c == ';')
                                        || (n - pos_caracter_especial) > 9) {
                                    pos_caracter_especial = -1;
                                }
                            }
                        } else {
                            estado = PALAVRA;

                            if (em_titulo) {
                                titulo += str;
                            }
                            if (!(c == '<')) {
                                if (em_titulo) {
//                                	if(!Character.isLetterOrDigit(c)){
//                                		c = ' ';
//                                	}
                                    titulo += c;
                                }

                                n++;
                            }
                        }

                        break;


                    case PALAVRA:
//                      System.out.println("PALAVRA:"+lastTag);
                      if(insideATag){
                        anchor = anchor + " " + str.toLowerCase();

                        numOfwordsAnchor++;
//                                                  insideATag = false;

//                          System.out.println("ANCHOR:"+anchor);
                      }
//                      if(anchor.indexOf("school") != -1){
//                        System.out.println("TEST");
//                      }
                        /* PALAVRA - palavra pronta */
                        if (!em_script && (str.length() > 0)) {
                            if (em_body && paragrafo.length() + str.length() < MAX_PARAGRAPH_SIZE) {
                                if (Character.isWhitespace(c)) {
                                    paragrafo +=
                                        str + c;    // atualiza variavel paragrafo
                                } else {
                                    paragrafo += str + " ";
                                }
                            }

                            if (!em_titulo) {
                                boolean adicionou = adicionaAoVetorDeTexto(str);
                                if( adicionou ) {
                                  around.add(str);
                                  adicionaTermoPosicao(str,posicao_da_palavra);    // atualiza o centroide
                                  if(em_option){
                                      adicionaPontuacaoTermo(str,PONTUACAO_PALAVRAS_OPTION);
                                    }else{
                                      adicionaPontuacaoTermo(str,PONTUACAO_PALAVRAS_TEXTO);
                                    }
                                    String str_sem_acento = Acentos.retirarNotacaoHTMLAcentosANSI(str);
                                    if( !str_sem_acento.equals(str) ) {
                                        adicionou = adicionaAoVetorDeTexto(str_sem_acento);
                                        if( adicionou ) {
                                            adicionaTermoPosicao(str_sem_acento,posicao_da_palavra);    // atualiza o centroide
                                            if(em_option){
                                              adicionaPontuacaoTermo(str_sem_acento,PONTUACAO_PALAVRAS_OPTION);
                                            }else{
                                              adicionaPontuacaoTermo(str_sem_acento,PONTUACAO_PALAVRAS_TEXTO);
                                            }
                                        }
                                    }
                                    posicao_da_palavra++;
                                }
                            } else {
                                boolean adicionou = adicionaAoVetorDeTexto(str);
                                if( adicionou ) {
                                    adicionaTermoPosicao(str,posicao_da_palavra);    // atualiza o centroide
                                    adicionaPontuacaoTermo(str,PONTUACAO_PALAVRAS_TITULO);
                                    String str_sem_acento = Acentos.retirarNotacaoHTMLAcentosANSI(str);
                                    if( !str_sem_acento.equals(str) ) {
                                        adicionou = adicionaAoVetorDeTexto(str_sem_acento);
                                        if( adicionou ) {
                                            adicionaTermoPosicao(str_sem_acento,posicao_da_palavra);    // atualiza o centroide
                                            adicionaPontuacaoTermo(str_sem_acento,PONTUACAO_PALAVRAS_TITULO);
                                        }
                                    }
                                    posicao_da_palavra++;
                                }
                            }
                        }

                        estado = INICIO;
                        ignorar_espacos = true;
                        str = "";

                        break;

                    case TAG_NAME:

                        /* TAG_NAME - terminated by space, \r, \n, >, / */
                        if (em_script) {
                            if ( c != '>'){
                                if ( "/script".startsWith(str + c) || "/SCRIPT".startsWith(str + c) || "/style".startsWith(str + c) || "/STYLE".startsWith(str + c)) {
                                    str +=c;
                                }
                                else {
                                    str = "";
                                    estado = INICIO;
                                }
                                n++;
                            } else if ( c == '>'){
                                if (str.equalsIgnoreCase("/script") || str.equalsIgnoreCase("/style")) {
                                    fimDeString = true;
                                    tag_tipo_fim = true;
                                    tagScript = true;
                                    estado = FECHANDO;
                                } else {
                                     n++;
                                }
                            }
                        }
                        else {
                            if (str.equals("BASE")) {
//                                System.out.println("EM TAG_NAME, str="+str + ", c="+c+", tagTitulo="+tagTitulo);
                                if (c == '>') {
                                    estado = FECHANDO;
                                } else {
                                    n++;
                                }
                            } else {
//                                if ((c == '"') || (c == '\'')) {
//                                if ((c == '\'')) {
//                                    organizaDados(); //new
//                                    return;    /* error - these are not allowed in tagname */
//                                } else 
                                	if (c == ' ') {

                                    /*
                                     * Note: Both mozilla and XML don't allow any spaces between < and tagname.
                                     * Need to check for zero-length tagname.
                                     */
//                                    if (str.length() == 0) {
//                                        organizaDados(); //new
//                                        return;    /* str is the buffer we're working on */
//                                    }

                                    fimDeString = true;
                                    estado = ATRIBUTO;
                                    ignorar_espacos = true;
                                    n++;
                                } else if (c == '/') {
                                    if (tagName.length() == 0) {
                                        tag_tipo_fim =
                                            true;    /* indicates end tag if no tag name read yet */
                                    } else if (obj_isRDF) {    /* otherwise its an empty tag (RDF only) */
                                        fimDeString = true;
                                        tag_tipo_vazia = true;
                                        estado = FECHANDO;
                                    } 
//                                    else {
//                                        organizaDados(); //new
//                                        return;
//                                    }

                                    n++;
                                } else if (c == '>') {
                                    fimDeString = true;
    //                                tag_tipo_fim = true;
                                    estado = FECHANDO;
                                } else if ((c != '\r') && (c != '\n')) {

                                    // System.out.println("Estou NO CAMINHO CERTO!!!!");
                                    str += c;
                                    n++;
                                } else {
                                    fimDeString = true;
                                    estado =
                                        ATRIBUTO;    /* note - mozilla allows newline after tag name */
                                    ignorar_espacos = true;
                                    n++;
                                }

                                if (fimDeString) {
                                    //if (str.equals("!--")) {    /* html comment */
                                    if (str.startsWith("!--")) {    /* html comment */
                                        em_comentario = true;
                                        num_comentario++;
                                        estado = INICIO;
                                    } else {
                                        str = str.toLowerCase();
                                        tagName = str;
                                        tagBody = str.equals("body");
                                        tagTitulo = str.equals("title");
                                        tagOption = str.equals("option");
                                        if(tagName.equals("html")){
                                        	if(!tag_tipo_fim){
                                            	numOfHtmlTags++;
                                        	}else{
                                        		numOfHtmlTags--;
                                        	}
//                                        	System.out.println(">>>>>>>>>>>>>" + numOfHtmlTags);
                                        }
                                        	
                                        //if (tagTitulo) {
                                        //    System.out.println("achot tag titulo " + str);
                                        //}
                                        tagScript = str.equals("script") || str.equals("style");

                                        if (str.equals("form")) {
                                            this.forms++;
                                        }
                                    }

                                    str = "";
                                    fimDeString = false;
                                }

                                // System.out.println("A STRING DO ATRIBUTO EH: " + str + " estado novo "+ estado);
                            }
                        }
                        break;

                    case FECHANDO:
                        /* FECHANDO - expecting a close bracket, anything else is an error */
//                        System.out.println("END OF TAG:"+tagName);
//                        if(ln!=null){
//                          ln.setAnchor(anchor);
//                          System.out.println("URL---"+ln.getLink());
 //                         System.out.println("ANC---"+ln.getAnchor());
  //                      }
                    	
                        if((tag_tipo_fim && tagName.equals("a")) || tagName.equals("area")){

                          insideATag = false;
                          if(ln!=null){
                            Vector anchorTemp = new Vector();
  //                          System.out.println("URL---"+ln.getLink());
  //                          System.out.println("ANC---"+anchor);
                            StringTokenizer tokenizer = new StringTokenizer(anchor," ");
                            while(tokenizer.hasMoreTokens()){
                              anchorTemp.add(tokenizer.nextToken());
                            }
                            String[] anchorArray = new String[anchorTemp.size()];
                            anchorTemp.toArray(anchorArray);
                            ln.setAnchor(anchorArray);
                            ln.setAroundPosition(around.size());
                            ln.setNumberOfWordsAnchor(numOfwordsAnchor);
                            linkNeigh.add(ln.clone());
//                            anchor = "";
                            ln = null;
                          }
                          anchor = "";
                        }
                        // System.out.println("Entrei em fechando");
                        if (c == '>') {
                            if (tagScript) {

                                /* we're inside a script tag (not RDF) */
                                em_script = !tag_tipo_fim;
                            }

                            if (tagTitulo) {
                                em_titulo = !tag_tipo_fim;
                                //System.out.println("EM tag titulo " + str + ", em_titulo"+ em_titulo);
                                //System.out.println("EM tag titulo " + str + ", tag_tipo_fim"+ tag_tipo_fim);
                                //System.out.println("EM tag titulo " + str + ", tagTitulo"+ tagTitulo);
                            }

                            if (tagBody) {
                                em_body = !tag_tipo_fim;

                                // System.out.println("Entrei no estado inicial");
                            }
                            if (tagOption) {
                                em_option = !tag_tipo_fim;

                                // System.out.println("Entrei no estado inicial");
                            }
//                            if(tag_tipo_fim && tagName.equals("html") && numOfHtmlTags == 0){
//                                organizaDados();
//                                return;
//                            }

                            tagTitulo = false;
                            tagBody = false;
                            tagScript = false;
                            tagOption = false;
                            estado = INICIO;
                            str = "";
                            tagName = "";

                            numOfwordsAnchor = 0;
                            ignorar_espacos = true;
                            n++;
                        } 
                        else {
                        	
                            organizaDados(); //new
                            return;                         /* error */
                        }

                        break;

                    case ATRIBUTO:

                        /* ATRIBUTO - expecting an attribute name, or / (RDF only) or > indicating no more attributes */

                        /*
                         * accept attributes without values, such as <tag attr1 attr2=val2>
                         * or <tag attr2=val2 attr1>
                         */
                        if (quote_char == c) {
                            quote_char = '\0';              /* close quote */
                        } else if (((c == '"') || (c == '\''))
                                   && (quote_char == '\0')) {

                            /* start a quote if none is already in effect */
                            quote_char = c;
                        }

                        if (quote_char == '\0') {
                            if ((((c == '/') && obj_isRDF) || (c == '>'))
                                    && (str.length() == 0)) {
                                estado = FECHANDO;
                            } else if ((c == ' ') || (c == '=')
                                       || (c == '\n') || (c == '\r')
                                       || ((c == '/') && obj_isRDF)
                                       || (c == '>')) {
                                atributo = str;
                                str = "";
                                estado = IGUAL;
//System.out.println("[ATRIBUTO c='"+c+"', estado=IGUAL], atributo="+atributo);
                                /* if non-null attribute name */
                            } else {
                                str += c;
                                n++;
                            }
                        } else {
                            str += c;
                            n++;
                        }

                        break;

                    case IGUAL:
                        atributo = atributo.toLowerCase();
                        tagName = tagName.toLowerCase();

                        // System.out.println("------------------------------------");
                        // System.out.println(" A TAG NAME EH: " + tagName);
                        // if(atributo.equals("src") && tagName.equals("img") && (c == '='))
                        // {
                        // ignorar_espacos = true;
                        // estado = IMAGEM;
                        // n++;
                        // }
                        // else
                        // {
/****
                        if (atributo.equals("content")
                                && tagName.equals("meta") && (c == '=')) {
                            ignorar_espacos = true;
                            estado = META_TAG;
                            n++;
                        } else if (atributo.equals("alt")
                                        && tagName.equals("img") && (c == '=')) {
                            ignorar_espacos = true;
                            estado = ALT_TAG;
                            n++;
                        } else {
***/
                            if ((c == ' ') || (c == '\n')
                                    || (c == '\r')) {
                                ignorar_espacos = true;
                                n++;
                            } else if (c == '=') {
                                ignorar_espacos = true;
                                estado = VALOR;
                                n++;
                            } else {    /* no value for the attribute - error in RDF? */
                                str = "";
                                atributo = "";

                                // estado = ATRIBUTO;
                                if (c == '>') {
                                    // System.out.println("Entrei aqui no MENOR QUE");
                                    tagScript = false;
                                    tagBody = false;
                                    tagTitulo = false;
                                    estado = FECHANDO;
                                } else {
                                    ignorar_espacos = true;

                                    // System.out.println("Entrei PARA ANDAR NA LINHA");
                                    n++;
                                }
                            }
//                        }

                        // }
                        break;

                    case ALT_TAG: // nao usa mais, foi mudado, ver no estado VALOR 
                        if (((c == ' ') || (c == '"')) && ehInicioALT) {
                            ignorar_espacos = false;

                            boolean adicionou = adicionaAoVetorDeTexto(str);
                            if( adicionou ) {
                                adicionaTermoPosicao(str,posicao_da_palavra);    // atualiza o centroide
                                adicionaPontuacaoTermo(str,PONTUACAO_PALAVRAS_ALT);
                                String str_sem_acento = Acentos.retirarNotacaoHTMLAcentosANSI(str);
                                if( !str_sem_acento.equals(str) ) {
                                    adicionou = adicionaAoVetorDeTexto(str_sem_acento);
                                    if( adicionou ) {
                                        adicionaTermoPosicao(str_sem_acento,posicao_da_palavra);    // atualiza o centroide
                                        adicionaPontuacaoTermo(str_sem_acento,PONTUACAO_PALAVRAS_ALT);
                                    }
                                }
                                posicao_da_palavra++;
                            }

                            str = "";
                            ehInicioALT = false;
                        } else {
                            if (c == '>') {
//                                estado = INICIO; //nao sei se esta' ok
                                estado = VALOR;
                                ehInicioALT = true;
                            } else {
                                if (c == '.' || c == ',') {
                                } else {
                                    if ((c != '\0') && (c != '\r')
                                            && (c != '\n') && (c != '"')) {
                                        str += c;
                                    } else {
                                        if (c == '"') {
                                            estado = ATRIBUTO;
                                            ehInicioALT = true;
                                        }
                                    }
                                }
                            }
                        }

                        n++;

                        break;

                    case META_TAG: // nao usa mais, foi mudado, ver no estado VALOR [ogm]
                        if ((c == ' ') || (c == '"') || (c == '\n') || (c == ',')) {
                            ignorar_espacos = false;

                            textoMeta.addElement(str);    // adiciona a palavra na variavel texto

                            for (int contadorI = 0;
                                    contadorI < PONTUACAO_PALAVRAS_META;
                                    contadorI++) {
                                adicionaTermoMetaPosicao(str, textoMeta.size());
                            }

                            str = "";
                        } else {
                            if (c == '>') {
                                estado = INICIO;
//                                estado = VALOR;
                            } else {
                                if (c == '.' || c == ',') {
                                } else {
                                    if ((c != '\0') && (c != '\r')
                                            && (c != '\n') && (c != '"')) {
                                        str += c;
                                    }
                                }
                            }
                        }

                        n++;

                        break;

                    case VALOR:

                        /* expecting a value, or space, / (RDF only), or > indicating end of value. */
                        /* whether the current character should be included in value */
                        boolean include = true;
//                        System.out.println("LENGTH:"+str.length());
//                        if(str.length() > 300){
//                          System.out.println("TEST");
//                        }
                        if (quote_char == c || str.length() > 10000) {
                            quote_char = '\0';              /* close quote */
                            include = false;
                        } else if (((c == '"') || (c == '\''))
                                   && (quote_char == '\0')) {

                            /* start a quote if none is already in effect */
                            quote_char = c;
                            include = false;

                        }

                        if (quote_char == '\0') {
                            if ((c == '/') && obj_isRDF) {
                                fimDeString = true;
                                estado = FECHANDO;
                                n++;
//                            } else if (c == '>' || str.length() > 10000) {
                            } else if (c == '>' || str.length() > 100000) {
                                fimDeString = true;
                                estado = FECHANDO;
                            } else if ((c == ' ') || (c == '\r')
                                       || (c == '\n')) {
                                fimDeString = true;
                                ignorar_espacos = true;
                                estado = ATRIBUTO;    /* if non-null value name */
                                n++;
                            } else if (include) {
                                str += c;
                                n++;
                            } else {
                                n++;
                            }
                        } else if (include) {
                            str += c;
                            n++;
                        } else {
                            n++;
                        }

                        if (fimDeString) {
                            tagName = tagName.toLowerCase();

//                            System.out.println("TAG:"+tagName);
                            atributo = atributo.toLowerCase();

//                            System.out.println("[VALOR, estado='"+estado+"', c="+c+"] "+tagName+"."+atributo+"="+str);

                            if (tagName.equals("a")
                                    && atributo.equals("href")) {
                                  insideATag = true;
                                  String urlTemp = adicionaLink(str, base);
//                                  System.out.println("----URL:"+urlTemp);
                                  if(urlTemp!= null && urlTemp.startsWith("http")){
                                	  if(ln!=null){
                                		  Vector anchorTemp = new Vector();
                                		  StringTokenizer tokenizer = new StringTokenizer(anchor," ");
                                		  while(tokenizer.hasMoreTokens()){
                                			  anchorTemp.add(tokenizer.nextToken());
                                		  }
                                		  String[] anchorArray = new String[anchorTemp.size()];
                                		  anchorTemp.toArray(anchorArray);
                                		  ln.setAnchor(anchorArray);
                                		  ln.setAroundPosition(around.size());
                                		  ln.setNumberOfWordsAnchor(numOfwordsAnchor);
                                		  linkNeigh.add(ln.clone());
                                		  anchor = "";
                                		  ln = null;
                                	  }
                                	  ln = new LinkNeighborhood(new URL(urlTemp));
                                  }
//                                System.out.println("CREATE LINK:"  + urlTemp);
                            } else if (tagName.equals("link")
                                       && atributo.equals("href")) {
                                String urlTemp = adicionaLink(str, base);
                                if(urlTemp!= null && urlTemp.startsWith("http")){
                                  ln = new LinkNeighborhood(new URL(urlTemp));
                                }
//                                System.out.println("CREATE LINK:"  + urlTemp);
                            } else if (tagName.equals("area")
                                       && atributo.equals("href")) {
                                adicionaLink(str, base);
                                String urlTemp = adicionaLink(str, base);
                                if(urlTemp!= null && urlTemp.startsWith("http")){
                                  ln = new LinkNeighborhood(new URL(urlTemp));
                                }
                            } else if (tagName.equals("img")
                                       && atributo.equals("src")) {
                            	if(ln != null){
                            		ln.setImgSource(str);
                            	}
                            	try {
                            		imagens.addElement(parseLink(base,str).toString());	
								} catch (Exception e) {
									// TODO: handle exception
								}
                                
                            } 
//                            else if((tagName.equals("area") || tagName.equals("a"))&& atributo.equals("alt")){
//                            	anchor = anchor + " " + str.toLowerCase();
//                            } 
                            else if (tagName.equals("frame")
                                       && atributo.equals("src")) {
                                frames.addElement(str);
                                adicionaLink(str, base);
                            } else if (tagName.equals("img") && (atributo.equals("alt") || atributo.equals("title") || atributo.equals("id"))) {
//                                System.out.println("img.alt.str="+str);
                            	Vector<String> altWords = new Vector<String>();
                            	StringTokenizer st = new StringTokenizer(str);
                                while(st.hasMoreTokens()) {
                                    String token = st.nextToken();
                                	if(token.contains("ÃƒÂ±")){
                                		token = token.replace("ÃƒÂ±", "n");
                                    }
                                	token = token.toLowerCase();
                                    if(token.contains("&#241;")){
                                    	token = token.replace("&#241;", "n");
                                    }
                                	if(token.contains("&ntilde;")){
                                    	token = token.replace("&ntilde;", "n");
                                    }
                                    if(token.contains("Ã±")){
                                    	token = token.replace("Ã±", "n");
                                    }

                                    altWords.add(token);
                                    if(!caracterFazParteDePalavra(token.charAt(0))){
                                    	token = token.substring(1);
                                    }
                                    if(token.equals("")){
                                    	break;
                                    }
                                    if(!caracterFazParteDePalavra(token.charAt(token.length()-1))){
                                    	token = token.substring(0,token.length()-1);
                                    }
                                    if(token.equals("")){
                                    	break;
                                    }
                                    boolean adicionou = adicionaAoVetorDeTexto(token);
                                    if( adicionou ) {
                                        adicionaTermoPosicao(token,posicao_da_palavra);    // atualbejiza o centroide
                                        adicionaPontuacaoTermo(token,PONTUACAO_PALAVRAS_ALT);
                                        String token_sem_acento = Acentos.retirarNotacaoHTMLAcentosANSI(token);
                                        if( !token_sem_acento.equals(token) ) {
                                            adicionou = adicionaAoVetorDeTexto(token_sem_acento);
                                            if( adicionou ) {
                                                adicionaTermoPosicao(token_sem_acento,posicao_da_palavra);    // atualiza o centroide
                                                adicionaPontuacaoTermo(token_sem_acento,PONTUACAO_PALAVRAS_ALT);
                                            }
                                        }
                                        posicao_da_palavra++;
                                    }
                                }
                                if(ln != null){
                                	String[] current = ln.getImgAlt();
                                	if(current == null){
                                    	String[] terms = new String[altWords.size()];
                                    	altWords.toArray(terms);
                                    	ln.setImgAlt(terms);
                                	}else{
                                		String[] terms = new String[altWords.size()+current.length];
                                		int indexTerms = 0;
                                		for (int i = 0; i < current.length; i++,indexTerms++) {
											terms[indexTerms] = current[i];
										}
                                		for (int i = 0; i < altWords.size(); i++,indexTerms++) {
                                			terms[indexTerms] = altWords.elementAt(i);
										}
                                		ln.setImgAlt(terms);
                                	}
                                }
                            } else if (tagName.equals("meta")
                                       && atributo.equals("content")) {
                                if( em_meta_description ) {
                                    str_da_metatag_description = str;
                                    em_meta_description = false;
                                    if( USAR_DESCRIPTION ) {
                                        StringTokenizer st = new StringTokenizer(str);
                                        while(st.hasMoreTokens()) {
                                            String token = st.nextToken();
                                            int posicao = texto.size();
                                            boolean adicionou = adicionaAoVetorDeTexto(token);
                                            if( adicionou ) {
                                                adicionaTermoPosicao(token,posicao_da_palavra);    // atualiza o centroide
                                                adicionaPontuacaoTermo(token,PONTUACAO_PALAVRAS_DESCRIPTION);
                                                String token_sem_acento = Acentos.retirarNotacaoHTMLAcentosANSI(token);
                                                if( !token_sem_acento.equals(token) ) {
                                                    adicionou = adicionaAoVetorDeTexto(token_sem_acento);
                                                    if( adicionou ) {
                                                        adicionaTermoPosicao(token_sem_acento,posicao_da_palavra);    // atualiza o centroide
                                                        adicionaPontuacaoTermo(token_sem_acento,PONTUACAO_PALAVRAS_DESCRIPTION);
                                                    }
                                                }
                                                posicao_da_palavra++;
                                            }
                                        }
                                    }
                                }

//                                System.out.println("meta.content.str="+str);
                                StringTokenizer st = new StringTokenizer(str);
                                while(st.hasMoreTokens()) {
                                    String token = st.nextToken();
                                    textoMeta.addElement(token);    // adiciona a palavra na variavel texto
                                    for (int contadorI = 0;
                                            contadorI < PONTUACAO_PALAVRAS_META;
                                            contadorI++) {
                                        adicionaTermoMetaPosicao(token, textoMeta.size());
                                    }
                                }
                            } else if (tagName.equals("meta")
                                       && atributo.equals("name")) {
                                if (str.toLowerCase().equals("robot")) {
                                    em_meta_robots = true;
                                }
                                if (str.toLowerCase().equals("description") || str.toLowerCase().equals("descricao")) {
                                    //System.out.println("meta.description.str="+str);
                                    em_meta_description = true;
                                }
                            } else if (em_meta_robots
                                       && atributo.equals("content")) {
                                if (str.toLowerCase().indexOf("noindex")
                                        != -1) {
                                    noindex = true;
                                }

                                if (str.toLowerCase().indexOf("nofollow")
                                        != -1) {
                                    nofollow = true;
                                }
                            } else if (tagName.equals("base")
                                       && atributo.equals("href")) {
                                try {
                                    base = parseLink(pagina, str);
                                } catch (Exception e) {
                                }                           // ignora
                             }

                            str = "";
                            atributo = "";
                            fimDeString = false;
                        }

                        break;
                    default:
                        break;
                    }
                }
            }
            if( USAR_DESCRIPTION ) {
                if( str_da_metatag_description != null ) {
                    paragrafo = str_da_metatag_description;
                }
            }
            if( estado == PALAVRA && str != null && !"".equals(str) ) {
                boolean adicionou = adicionaAoVetorDeTexto(str);
                if( adicionou ) {
                    adicionaTermoPosicao(str,posicao_da_palavra);    // atualiza o centroide
                    adicionaPontuacaoTermo(str,PONTUACAO_PALAVRAS_TEXTO);
                    String str_sem_acento = Acentos.retirarNotacaoHTMLAcentosANSI(str);
                    if( !str_sem_acento.equals(str) ) {
                        adicionou = adicionaAoVetorDeTexto(str_sem_acento);
                        if( adicionou ) {
                            adicionaTermoPosicao(str_sem_acento,posicao_da_palavra);    // atualiza o centroide
                            adicionaPontuacaoTermo(str_sem_acento,PONTUACAO_PALAVRAS_TEXTO);
                        }
                    }
                    posicao_da_palavra++;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        this.frames = frames.size();
        this.images = imagens.size();
        organizaDados();

    }

    /**
     * Declara\uFFFD\uFFFDo do M\uFFFDtodo
     *
     *
     * @see
     */
    protected void organizaDados() {

        // Cria arrays temporarios a partir da Hashtable
        // System.out.println("ORGANIZA DADOS");
        //int      size = palavra_posicoes.size();
        int      size = palavra_pontos.size();
        String[] words = new String[size];
        int[]    numbers = new int[size];

        int      sizeMeta = palavra_posicoes_meta.size();
        String[] wordsMeta = new String[sizeMeta];
        int[]    numbersMeta = new int[sizeMeta];
        int	i = 0;

        for (Enumeration seriewords = palavra_pontos.keys();
                seriewords.hasMoreElements(); ) {
            words[i] = ((String) seriewords.nextElement());
            i++;
        }

        i = 0;

        for (Enumeration serienumbers = palavra_pontos.elements();
                serienumbers.hasMoreElements(); ) {
            //numbers[i] = ((Vector) serienumbers.nextElement()).size();
            numbers[i] = ((Integer) serienumbers.nextElement()).intValue();
            i++;
        }

        i = 0;

        for (Enumeration seriewordsMeta = palavra_posicoes_meta.keys();
                seriewordsMeta.hasMoreElements(); ) {
            wordsMeta[i] = ((String) seriewordsMeta.nextElement());
            i++;
        }

        i = 0;

        for (Enumeration serienumbersMeta = palavra_posicoes_meta.elements();
                serienumbersMeta.hasMoreElements(); ) {
            numbersMeta[i] = ((Vector) serienumbersMeta.nextElement()).size();
            i++;
        }


        // Ordena os arrays em ordem decrescente.
        if (ordenarTermos) {
            ordeneOcorrenc(numbers, words);
            ordeneOcorrenc(numbersMeta, wordsMeta);
            // Copia os arrays para os atributos da pagina.
        }

        if (MAXPALAVRAS >= 0) {
            int npalavras = Math.min(size, MAXPALAVRAS);
            int npalavrasMeta = Math.min(sizeMeta, MAXPALAVRAS);

            palavras = new String[npalavras];
            ocorrencias = new int[npalavras];

            palavrasMeta = new String[npalavrasMeta];
            ocorrenciasMeta = new int[npalavrasMeta];


            for (i = 0; i < npalavras; i++) {
                palavras[i] = words[i];
                ocorrencias[i] = numbers[i];
            }

            for (i = 0; i < npalavrasMeta; i++) {
                palavrasMeta[i] = wordsMeta[i];
                ocorrenciasMeta[i] = numbersMeta[i];
            }


            while (i < size) {
                palavra_posicoes.remove(words[i]);    // remove os termos que nao serao usados
                i++;
            }
            while (i < sizeMeta) {
                palavra_posicoes_meta.remove(wordsMeta[i]);    // remove os termos que nao serao usados
                i++;
            }

        } else {
            palavras = words;
            ocorrencias = numbers;
            palavrasMeta = wordsMeta;
            ocorrenciasMeta = numbersMeta;
        }
    }

    /**
     * <BR>Retorna true se o caracter pode ser usado na palavra
     * <BR>no caso, retorna true se o caracter for uma letra um digito ou o caracter '-'
     * <BR>obs: as palavras do centroide so conterao caracteres que esta funcao retorne true
     */
    protected boolean caracterFazParteDePalavra(char c) {
    	int ascii = (int)c;
        return (Character.isLetterOrDigit(c) || c == '-' || c == '&' || c == '#' || (ascii > 2305 && ascii < 2651));
    }

    /**
     * Este metodo retorna o caracter dado com acentuacao correta
     */
    protected char converteChar(char c) {
        /*switch ((int) c) {

        case 224: // '\uFFFD'

        case 225:        // '\uFFFD'

        case 226: // '\uFFFD'

        case 227:        // '\uFFFD'

        case 228: // '\uFFFD'

        case 299: // '\uFFFD'
            return 'a';

        case 192: //'\uFFFD'

        case 193:        // '\uFFFD'

        case 194: // '\uFFFD'

        case 195:        // '\uFFFD'

        case 196: // '\uFFFD'

        case 197: // '\uFFFD'
            return 'A';

        case 232: // '\uFFFD'

        case 233:        // '\uFFFD'

        case 234: // '\uFFFD'

        case 235: // '\uFFFD'
            return 'e';

        case 200: // '\uFFFD'

        case 201:        // '\uFFFD'

        case 202: // '\uFFFD'

        case 203: // '\uFFFD'
            return 'E';

        case 236: // '\uFFFD'

        case 237:        // '\uFFFD'

        case 238: // '\uFFFD'

        case 239: // '\uFFFD'
            return 'i';

        case 204: // '\uFFFD'

        case 205:        // '\uFFFD'

        case 206: // '\uFFFD'

        case 207: // '\uFFFD'
            return 'I';

        case 242: // '\uFFFD'

        case 243:        // '\uFFFD'

        case 244: // '\uFFFD'

        case 245:        // '\uFFFD'

        case 246: // '\uFFFD'
            return 'o';

        case 210: // '\uFFFD'

        case 211:        // '\uFFFD'

        case 212: // '\uFFFD'

        case 213:        // '\uFFFD'

        case 214: // '\uFFFD'
            return 'O';

        case 249: // '\uFFFD'

        case 250:        // '\uFFFD'

        case 251: // '\uFFFD'

        case 252: // '\uFFFD'
            return 'u';

        case 217: // '\uFFFD'

        case 218:        // '\uFFFD'

        case 219: // '\uFFFD'

        case 220: // '\uFFFD'
            return 'U';

        case 231: // '\uFFFD'
            return 'c';

        case 199: // '\uFFFD'
            return 'C';

        case 241: // '\uFFFD'
            return 'n';

        case 209: // '\uFFFD'
            return 'N';

        case 253: // '\uFFFD'
                return 'y';

        case 221: // '\uFFFD'
            return 'Y';

        default:
            return c;    // caracter comum
        }*/

         return c;
    }

    private HashMap<String,String> codes = new HashMap<String, String>();
    
//    protected String codes2Words(String word){
//
//    }
    
//    protected String convertStr(String str) {
//    	
//    }
    
    /**
     * Este metodo verifica se a partir daposicao dada a substring representa um simbolo
     * codificado tipo :   &aacute;   =  '\uFFFD'
     * retorna o caracter representado pelo simbolo no caso acima retorna '\uFFFD'
     * @param str o string buffer atual do processamento
     * @param pos a posicao inicial de testes
     */
    protected char caracterEspecial(String str, int pos) {
//        System.out.println(str);
    	int size = str.length() - pos;
        String resStr = codes.get(str.substring(pos));
        if(resStr != null){
        	return resStr.charAt(0);
        }
        if (size == 4) {
            if (str.endsWith("&lt;")) {
                return '<';
            } else if (str.endsWith("&gt;")) {
                return '>';
            }
        } else if (size == 5) {
            if (str.endsWith("&amp;")) {
                return '&';
            }
        } else if (size == 6) {
            if (str.endsWith("&nbsp;")) {
                return ' ';
            } else if (str.endsWith("&copy;")) {
                return '\uFFFD';
            } else if (str.endsWith("&ordm;")) {
                return '\uFFFD';
            } else if (str.endsWith("&quot;")) {
                return '\"';
            } else if (str.endsWith("&auml;")) {
                return '\uFFFD';
            } else if (str.endsWith("&Auml;")) {
                return '\uFFFD';
            } else if (str.endsWith("&euml;")) {
                return '\uFFFD';
            } else if (str.endsWith("&Euml;")) {
                return '\uFFFD';
            } else if (str.endsWith("&iuml;")) {
                return '\uFFFD';
            } else if (str.endsWith("&Iuml;")) {
                return '\uFFFD';
            } else if (str.endsWith("&ouml;")) {
                return '\uFFFD';
            } else if (str.endsWith("&Ouml;")) {
                return '\uFFFD';
            } else if (str.endsWith("&uuml;")) {
                return '\uFFFD';
            } else if (str.endsWith("&Uuml;")) {
                return '\uFFFD';
            }
        } else if (size == 7) {
            if (str.endsWith("&acirc;")) {
                return '\uFFFD';
            } else if (str.endsWith("&Acirc;")) {
                return '\uFFFD';
            } else if (str.endsWith("&ecirc;")) {
                return '\uFFFD';
            } else if (str.endsWith("&Ecirc;")) {
                return '\uFFFD';
            } else if (str.endsWith("&icirc;")) {
                return '\uFFFD';
            } else if (str.endsWith("&Icirc;")) {
                return '\uFFFD';
            } else if (str.endsWith("&ocirc;")) {
                return '\uFFFD';
            } else if (str.endsWith("&Ocirc;")) {
                return '\uFFFD';
            } else if (str.endsWith("&ucirc;")) {
                return '\uFFFD';
            } else if (str.endsWith("&Ucirc;")) {
                return '\uFFFD';
            }
        } else if (size == 8) {
            if (str.endsWith("&aacute;")) {
                return '\uFFFD';
            } else if (str.endsWith("&Aacute;")) {
                return '\uFFFD';
            } else if (str.endsWith("&eacute;")) {
                return '\uFFFD';
            } else if (str.endsWith("&Eacute;")) {
                return '\uFFFD';
            } else if (str.endsWith("&iacute;")) {
                return '\uFFFD';
            } else if (str.endsWith("&Iacute;")) {
                return '\uFFFD';
            } else if (str.endsWith("&oacute;")) {
                return '\uFFFD';
            } else if (str.endsWith("&Oacute;")) {
                return '\uFFFD';
            } else if (str.endsWith("&uacute;")) {
                return '\uFFFD';
            } else if (str.endsWith("&Uacute;")) {
                return '\uFFFD';
            } else if (str.endsWith("&atilde;")) {
                return '\uFFFD';
            } else if (str.endsWith("&Atilde;")) {
                return '\uFFFD';
            } else if (str.endsWith("&otilde;")) {
                return '\uFFFD';
            } else if (str.endsWith("&Otilde;")) {
                return '\uFFFD';
            } else if (str.endsWith("&ntilde;")) {
                return '\uFFFD';
            } else if (str.endsWith("&Ntilde;")) {
                return '\uFFFD';
            } else if (str.endsWith("&ccedil;")) {
                return '\uFFFD';
            } else if (str.endsWith("&Ccedil;")) {
                return '\uFFFD';
            } else if (str.endsWith("&agrave;")) {
                return '\uFFFD';
            } else if (str.endsWith("&Agrave;")) {
                return '\uFFFD';
            } else if (str.endsWith("&egrave;")) {
                return '\uFFFD';
            } else if (str.endsWith("&Egrave;")) {
                return '\uFFFD';
            } else if (str.endsWith("&igrave;")) {
                return '\uFFFD';
            } else if (str.endsWith("&Igrave;")) {
                return '\uFFFD';
            } else if (str.endsWith("&ograve;")) {
                return '\uFFFD';
            } else if (str.endsWith("&Ograve;")) {
                return '\uFFFD';
            } else if (str.endsWith("&ugrave;")) {
                return '\uFFFD';
            } else if (str.endsWith("&Ugrave;")) {
                return '\uFFFD';
            }
        } else if (size > 9) {

            // retorna '\0' se nao achou
            return '\0';
        }

        // retorna '\0' se nao achou o simbolo correspondente
        return '\0';
    }

    /**
     * Este metodo adiciona um link na lista de links desta pagina
     * E' usado quando a pagina e' construida
     */
    protected String adicionaLink(String link, URL base) {
        if (nofollow) {
            return "";
        }

        try {

            // System.out.println();
            // System.out.println("link1='"+link+"'");
            // System.out.println("urlX='"+new URL(pagina,link)+"'");
            link = link.trim();

            URL url = parseLink(base, link);

            link = url.toString();

            // System.out.println("url.getProtocol()='"+url.getProtocol()+"'");
            if (url.getProtocol().equals("mailto")) {
                int idx = link.indexOf("?");

                // System.out.println("am idx="+idx);
                if (idx >= 0) {
                    link = link.substring(0, idx);
                }

                idx = link.indexOf(",");

                if (idx >= 0) {
                    if (idx + 1 < link.length()) {
                        String link2 = "mailto:" + link.substring(idx + 1);

                        adicionaLink(link2, base);
                    }

                    link = link.substring(0, idx);
                }

                if (!mailList.contains(link)) {
                    mailList.addElement(link);
                }
            }

            // System.out.println("link2='"+link+"'");
            boolean existe = links.contains(link);

            if (link != null &&!existe) {
                if (base != null) {
                    if (!link.equals(base.toString())) {
                        link = processarRedirecionamentos(link);

                        if (link != null) {
                            links.addElement(link);
                        }
                    }
                } else {
                    link = processarRedirecionamentos(link);

                    if (link != null) {
                        links.addElement(link);
                    }
                }
            }
        } catch (MalformedURLException mfue) {
        }    // ignora
        return link;
     }

    /**
     * Declara\uFFFD\uFFFDo do M\uFFFDtodo
     *
     *
     * @param link
     *
     * @return
     *
     * @see
     */
    protected String processarRedirecionamentos(String link) {

        // System.out.println("processarRedirecionamentos");
        if (link.indexOf("www.cade.com.br") > 0
                && link.indexOf("&Redirect=") > 0) {
            return null;
        }
        else if (link.indexOf(" ") != -1) {
            return null;
        }
        else {
            return link;
        }

        /*
         * {
         * try {
         * URL u = new URL(link);
         * //        System.out.println("URL TIPO"+TiposURL.tipoURL(u));
         * if( TiposURL.tipoURL(u) == TiposURL.E_MAIL )
         * return link;
         * //        System.out.println("TIPO E_MAIL");
         * if( TiposURL.tipoURL(u) != TiposURL.LINK )
         * return null;
         * //        System.out.println("TIPO_LINK");
         * HttpURLConnection u_c = (HttpURLConnection)u.openConnection();
         * //        System.out.println("RESPONSE_CODE = "+u_c.getResponseCode());
         * if( u_c.getResponseCode() != HttpURLConnection.HTTP_OK )
         * return null;
         * URL u_f = u_c.getURL();
         * //        System.out.println("PASSEI NO TESTE"+u_f);
         * return (u_f == null ? null : u_f.toString());
         * }
         * catch(Exception exc)
         * {
         * return null;
         * }
         * }
         */
    }

    /**
     * Declara\uFFFD\uFFFDo do M\uFFFDtodo
     *
     *
     * @param base
     * @param link
     *
     * @return
     *
     * @throws MalformedURLException
     *
     * @see
     */
    protected URL parseLink(URL base,
                            String link) throws MalformedURLException {
        String  original = link;
        int     i, limit, c;
        int     start = 0;
        String  newProtocol = null;
        boolean aRef = false;
        String  protocol = null;
        String  host = null;
        int     port = -1;
        String  file = null;
        String  ref = null;

        try {
            limit = link.length();

            while ((limit > 0) && (link.charAt(limit - 1) <= ' ')) {
                limit--;    // eliminate trailing whitespace
            }

            while ((start < limit) && (link.charAt(start) <= ' ')) {
                start++;    // eliminate leading whitespace
            }

            if (link.regionMatches(true, start, "url:", 0, 4)) {
                start += 4;
            }

            if (start < link.length() && link.charAt(start) == '#') {

                /*
                 * we're assuming this is a ref relative to the context URL.
                 * This means protocols cannot start w/ '#', but we must parse
                 * ref URL's like: "hello:there" w/ a ':' in them.
                 */
                aRef = true;
            }

            for (i = start;
                    !aRef && (i < limit) && ((c = link.charAt(i)) != '/');
                    i++) {
                if (c == ':') {
                    newProtocol = link.substring(start, i).toLowerCase();
                    start = i + 1;

                    break;
                }
            }

            // Only use our context if the protocols match.
            if ((base != null)
                    && ((newProtocol == null)
                        || newProtocol.equals(base.getProtocol()))) {
                protocol = base.getProtocol();
                host = base.getHost();
                port = base.getPort();
                file = base.getFile();
            } else {
                protocol = newProtocol;
            }

            if (protocol == null) {
                throw new MalformedURLException("no protocol: " + original);
            }

            // if ((handler = getURLStreamHandler(protocol)) == null) {
            // throw new MalformedURLException("unknown protocol: "+protocol);
            // }
            i = link.indexOf('#', start);

            if (i >= 0) {
                ref = link.substring(i + 1, limit);
                limit = i;
            }

            // if ("http".equals(protocol)) return parseURL(protocol, host, port, file, ref, link, start, limit);
            if ("http".equals(protocol)) {
                return parseURL(protocol, host, port, file, null, link,
                                start, limit);
            } else {
                return new URL(base, link);
            }
        } catch (MalformedURLException e) {
            throw e;
        } catch (Exception e) {
            throw new MalformedURLException(original + ": " + e);
        }
    }

    /**
     * Faz o parse dos links dado a representacao do link (igual ao que esta no tag)
     * e sua base
     */
    protected URL parseURL(String protocol, String host, int port,
                           String file, String ref, String spec, int start,
                           int limit) throws MalformedURLException {

        // System.out.println("--------------------------");
        // System.out.println("pu protocol='"+protocol+"'");
        // System.out.println("pu host='"+host+"'");
        // System.out.println("pu port='"+port+"'");
        // System.out.println("pu file='"+file+"'");
        // System.out.println("pu ref='"+ref+"'");
        // System.out.println("pu spec='"+spec+"'");
        if (file != null) {
            int index_barra = file.lastIndexOf('/');
            int index_ponto = file.lastIndexOf('.');

            if (index_barra > index_ponto &&!file.endsWith("/")) {
                file += '/';

                // System.out.println("pu file2='"+file+"'");
                // System.out.println("--------------------------");
            }
        }

        int i;

        if ((start <= limit - 2) && (spec.charAt(start) == '/')
                && (spec.charAt(start + 1) == '/')) {
            start += 2;
            i = spec.indexOf('/', start);

            if (i < 0) {
                i = limit;
            }

            int prn = spec.indexOf(':', start);

            port = -1;

            if ((prn < i) && (prn >= 0)) {
                try {
                    port = Integer.parseInt(spec.substring(prn + 1, i));
                } catch (Exception e) {

                    // ignore bogus port numbers
                }

                if (prn > start) {
                    host = spec.substring(start, prn);
                }
            } else {
                host = spec.substring(start, i);
            }

            start = i;
            file = null;
        } else if (host == null) {
            host = "";
        }

        if (start < limit) {

            /*
             * If the context URL is a CGI URL, the context to be the
             * URL's file up to the / before ? character.
             */
            if (file != null) {
                int questionMarkIndex = file.indexOf('?');

                if (questionMarkIndex > -1) {
                    int lastSlashIndex = file.lastIndexOf('?',
                                                          questionMarkIndex);

                    file = file.substring(0, ++lastSlashIndex);
                }
            }

            if (spec.charAt(start) == '/') {
                file = spec.substring(start, limit);
            } else if (file != null && file.length() > 0) {

                /*
                 * relative to the context file - use either
                 * Unix separators || platform separators
                 */
                int ind = Math.max(file.lastIndexOf('/'),
                                   file.lastIndexOf(File.separatorChar));

                file = file.substring(0, ind) + "/"
                       + spec.substring(start, limit);
            } else {
                file = "/" + spec.substring(start, limit);
            }
        }

        if ((file == null) || (file.length() == 0)) {
            file = "/";
        }

        while ((i = file.indexOf("/./")) >= 0) {
            file = file.substring(0, i) + file.substring(i + 2);
        }

        while ((i = file.indexOf("/../")) >= 0) {
            if ((limit = file.lastIndexOf('/', i - 1)) >= 0) {
                file = file.substring(0, limit) + file.substring(i + 3);
            } else {
                file = file.substring(i + 3);
            }
        }

        if (file.endsWith("/..")) {
            i = file.length() - 3;

            if ((limit = file.lastIndexOf('/', i - 1)) >= 0) {
                file = file.substring(0, limit);
            } else {
                file = "";
            }
        }

        if (file.length() == 0) {
            file = "/";
        }

        if (ref == null) {
            return new URL(protocol, host, port, file);
        } else {
            return new URL(protocol, host, port, file + "#" + ref);
        }
    }

    /*
     * protected URL parseLink(URL base, String link) throws MalformedURLException {
     * // remove referencia
     * int ref = link.indexOf('#');
     * if ( ref != -1 ) {
     * link = link.substring(0,ref);
     * }
     * if ( link.equals("") ) throw new MalformedURLException();
     *
     * // retorna link se ele for absoluto
     * try {
     * URL tmp  = new URL(link);
     * return tmp;
     * } catch(MalformedURLException e) { } //ignora
     *
     * String strbase = base.toString().trim();
     * int a,b,c;
     * a = strbase.length();
     * while( a > 1 )
     * {
     * if( strbase.charAt( strbase.length()-1 )=='/' )
     * strbase = strbase.substring( 0,strbase.length()-1 );
     * a--;
     * }
     * // Se a base tiver um arquivo no final este pedaco do codigo o retira.
     * a=0;
     * for(int i = 0; i < strbase.length()-1 ; i++ )
     * {
     * if( strbase.charAt(i)=='/' )
     * a++;
     * }
     * if( a > 2 )
     * { b = strbase.lastIndexOf('/');
     * c = strbase.lastIndexOf('.');
     * if( c != -1 && b < c )
     * strbase = strbase.substring( 0,b );
     * }
     * // Garante que a base sempre terminara com '/'.
     * strbase = strbase + '/';
     * // Retira o lixo que pode vir antes da URL "crua".
     * String strnova = link;
     * if( strnova.indexOf("http:")!=-1 )
     * return new URL(strnova);
     * // Se a URL classificada como relativa comecar com o caracter '~' a base deve
     * // ser o apenas o servidor. Utilizamos esta novabase para garantir isto.
     * else
     * {
     * // Retira os dois pontos e '/'
     * strnova = limpaLink( link );
     * String novabase = strbase;
     * if( !strnova.equals("") )
     * {
     * if( strnova.charAt(0)=='~' )
     * {
     * a = strbase.indexOf("//");
     * if( a != -1 )
     * {
     * b = strbase.indexOf("/",a+2);
     * if( b!=-1 )
     * novabase = strbase.substring(0,b+1);
     * else
     * novabase = strbase.substring(0,strbase.length());
     * }
     * }
     * }
     * // Se a URL relativa contiver um caminho(diretorio) que ja existe na base devemos "truncar"
     * // a base para que tenhamos um caminho correto.
     * String find = "";
     * if( strnova.indexOf("/")!=-1 )
     * find = strnova.substring( 0,strnova.indexOf("/")-1 );
     * if( !find.equals("") && novabase.indexOf(find)!=-1 )
     * {
     * strnova = novabase.substring( 0,novabase.indexOf(find) ) + strnova;
     * return new URL(strnova);
     * }
     * // Caso contrario e uma URL relativa sem nada especial.
     * else
     * {
     * strnova = novabase + strnova;
     * return new URL(strnova);
     * }
     * }
     * }
     * private String limpaLink(String link) {
     * int pos;
     * // Indexa e retira os ".." se existirem.
     * pos = link.indexOf("..");
     * if( pos != -1 )
     * link = link.substring( pos+2,link.length() ).trim();
     * // Se depois de tudo isso ainda existir alguma coisa e comecar por '/', o retiramos.
     * if( link.length() > 0 && link.charAt(0)=='/' )
     * link = link.substring( 1,link.length() ).trim();
     * return link;
     * }
     * //
     */

    /**
     * Este metodo incrementa a pontuacao de um termo na pagina.
     * E' usado quando a pagina e' construida
     */
    protected void adicionaPontuacaoTermo(String termo, int pontos) {
        if (noindex) {
            return;
        }

        if (palavra_pontos == null) {
            palavra_pontos = new Hashtable();
        }

        termo = termo.toLowerCase();
        boolean dominio = termo.startsWith("#") && termo.endsWith("#");
        if(!irrelevante(termo) || dominio) {
            Integer p_pontos = (Integer) palavra_pontos.get(termo);

            if (p_pontos == null) {
                p_pontos = new Integer(pontos);
            }
            else {
                p_pontos = new Integer(p_pontos.intValue()+pontos);
            }

            palavra_pontos.put(termo, p_pontos);
        }
    }
    /**
     * Este metodo incrementa a pontuacao de um termo na pagina.
     * E' usado quando a pagina e' construida
     */
    protected void adicionaPontuacaoTermoSemStopList(String termo, int pontos) {
        if (noindex) {
            return;
        }

        if (palavra_pontos == null) {
            palavra_pontos = new Hashtable();
        }

        termo = termo.toLowerCase();
        Integer p_pontos = (Integer) palavra_pontos.get(termo);

        if (p_pontos == null) {
            p_pontos = new Integer(pontos);
        }
        else {
            p_pontos = new Integer(p_pontos.intValue()+pontos);
        }

        palavra_pontos.put(termo, p_pontos);
    }

    /**
     * Este metodo incrementa a pontuacao de um termo na pagina.
     * E' usado quando a pagina e' construida
     */
    protected boolean adicionaAoVetorDeTexto(String termo) {
        boolean resultado = false;
        if (noindex) {
            return resultado;
        }

        if (texto == null) {
            texto = new Vector();
        }
        termo = termo.toLowerCase().trim();
        resultado = !irrelevante(termo);
        if( resultado ) {
            texto.addElement(termo);
        }
        return resultado;
    }


        /**
     * Este metodo adiciona um termo na lista de termos com suas posicoes
     * e' dado tambem uma posicao do termo.
     * E' usado quando a pagina e' construida
     */
    protected void adicionaTermoPosicaoSemStopList(String termo, int pos) {
        if (noindex) {
            return;
        }

        if (palavra_posicoes == null) {
            palavra_posicoes = new Hashtable();
        }

        termo = termo.toLowerCase();
         Vector posicoes = (Vector) palavra_posicoes.get(termo);

            if (posicoes == null) {
                    posicoes = new Vector();

                    palavra_posicoes.put(termo, posicoes);
                    posicoes.addElement(new Integer(pos));

            } else {
                posicoes.addElement(new Integer(pos));
            }
    }



    /**
     * Este metodo adiciona um termo na lista de termos com suas posicoes
     * e' dado tambem uma posicao do termo.
     * E' usado quando a pagina e' construida
     */
    protected void adicionaTermoPosicao(String termo, int pos) {
        if (noindex) {
            return;
        }
        
        
        if (palavra_posicoes == null) {
            palavra_posicoes = new Hashtable();
        }

        termo = termo.toLowerCase();
        boolean dominio = termo.startsWith("#") && termo.endsWith("#");
        if(!irrelevante(termo) || dominio) {
            Vector posicoes = (Vector) palavra_posicoes.get(termo);

            if (posicoes == null) {
                if (!irrelevante(termo)) {
                    posicoes = new Vector();

                    palavra_posicoes.put(termo, posicoes);
                    posicoes.addElement(new Integer(pos));
                }
            } else {
                posicoes.addElement(new Integer(pos));
            }
        }

    }


    /**
     * Este metodo adiciona um termo na lista de termos com suas posicoes
     * e' dado tambem uma posicao do termo.
     * E' usado quando a pagina e' construida
     */
    protected void adicionaTermoMetaPosicao(String termo, int pos) {
        if (noindex) {
            return;
        }

        if (palavra_posicoes_meta == null) {
            palavra_posicoes_meta = new Hashtable();
        }

        termo = termo.toLowerCase();

        Vector posicoesMeta = (Vector) palavra_posicoes_meta.get(termo);

        if (posicoesMeta == null) {
            if (!irrelevante(termo)) {
                posicoesMeta = new Vector();

                palavra_posicoes_meta.put(termo, posicoesMeta);
                posicoesMeta.addElement(new Integer(pos));
            }
        } else {
            posicoesMeta.addElement(new Integer(pos));
        }
    }



    /**
     * Identifica se uma palavra e considerada insignificante, isto e,
     * nao vale a pena ser armazenada.
     */
    protected boolean irrelevante(String str) {
        boolean result = false;

        if (stoplist != null) {
            result = stoplist.eIrrelevante(str);
        }

        return result;
    }

    /**
     * Verifica se uma String que possa vir a se tornar URL e de algum
     * tipo de arquivo que nao nos interessa
     */

    /*
     * private boolean linkIrrelevante(String strnova) {
     * String str[] =  {"news:","gopher:", "telnet:", "cgi-bin","pl_cad",
     * "?", ".gif", ".jpg", ".pdf", ".bmp", ".ps",  ".jpeg",
     * ".pcx", ".exe", ".com", ".class", ".zip", ".arj",  ".gzip",
     * ".gz",".z",".cgi",".mid",".mpeg",".ram",".mp1",".mp2",
     * ".mp3",".wav",".snd",".au",".aif" };
     * boolean achou = false;
     * for(int i=0;i<str.length && !achou;i++)
     * {if(strnova.indexOf(str[i])!=-1) achou=true;}
     * return achou;
     * }
     */

    /**
     * Recebe dois arrays, um com n palavras, e outro com as ocorrencias
     * associadas a cada palavra, e os ordena em ordem decrescente de
     * ocorrencia, para que tenhamos as palavras que ocorrem mais no comeco
     * do array.
     */
    private void ordeneOcorrenc(int a[], String b[]) {
        quicksort_num(a, 0, a.length - 1, b);
    }

    /**
     * Declara\uFFFD\uFFFDo do M\uFFFDtodo
     *
     *
     * @param a
     * @param left
     * @param right
     * @param b
     *
     * @see
     */
    private void quicksort_num(int a[], int left, int right, String b[]) {
        int pivot;
        int l = left;
        int r = right;

        if (left < right) {
            pivot = a[(left + right) / 2];

            while (l <= r) {
                while (a[l] > pivot & l < right) {
                    l++;
                }

                while (a[r] < pivot & r > left) {
                    r--;
                }

                if (l <= r) {
                    troque1(a, l, r);
                    troque2(b, l, r);

                    l++;
                    r--;
                }
            }

            if (left < r) {
                quicksort_num(a, left, r, b);
            }

            if (l < right) {
                quicksort_num(a, l, right, b);
            }
        }
    }

    // Funcoes auxiliares do quicksort

    /**
     * Declara\uFFFD\uFFFDo do M\uFFFDtodo
     *
     *
     * @param a
     * @param i
     * @param j
     *
     * @see
     */
    private void troque1(int a[], int i, int j) {
        int temp;

        temp = a[i];
        a[i] = a[j];
        a[j] = temp;
    }

    /**
     * Declara\uFFFD\uFFFDo do M\uFFFDtodo
     *
     *
     * @param a
     * @param i
     * @param j
     *
     * @see
     */
    private void troque2(String a[], int i, int j) {
        String temp;

        temp = a[i];
        a[i] = a[j];
        a[j] = temp;
    }

    /**
     * M\uFFFDtodo que retorna a nota da pagina
     */
    public int notaDaPagina() {
        return notaDaImagem;
    }


//+-------- IMPLEMENTANDO A INTERFACE PaginaAjustavel-------------+//
        /**
            Ajusta o endereco da pagina.
         */
        public void setEndereco(URL newEndereco) {
        if( newEndereco != null ) {
            this.pagina = newEndereco;
          }
        }

        /**
        Ajusta a data de visitacao.
         */
        public void setDataQueFoiVisitado(Date newData) {
        if( newData != null )
            this.visitado = newData.getTime();
        }

        /**
            Ajusta a data de modificacao.
         */
        public void setDataQueFoiModificado(Date newData) {
            if( newData != null )
                setDataQueFoiModificado(newData.getTime());
        }

        /**
            Ajusta a data de modificacao.
         */
        public void setDataQueFoiModificado(long newData) {
            this.modificado = newData;
        }

        /**
            Ajusta o tamanho do arquivo.
         */
        public void setTamanhoDoArquivo(int newTamanho) {
        if( newTamanho >= 0 )
            this.tamanho = newTamanho;
        else
            this.tamanho = 0;
        }

        public int getFileHashCode(){
            return fileHashCode;
        }

        public void setFileHashCode(int hashCode){
            fileHashCode = hashCode ;
        }
        /**
            Ajusta o titulo.
         */
        public void setTitulo(String newTitulo) {
        if( newTitulo != null )
            this.titulo = newTitulo;
        else
            this.titulo = "";
        }

        /**
            Ajusta o paragrafo.
         */
        public void setParagrafo(String newParagrafo) {
        if( newParagrafo != null )
            this.paragrafo = newParagrafo;
        else
            this.paragrafo = "";
        }

        /**
        Ajusta o array de palavras.
        IMPORTANTE: Este metodo so aceita a substituicao do array de palavras por outro do mesmo tamanho,
        onde as entradas sao na verdade os codigos dos seus respectivos termos obedecendo a ordem semantica.
         */
        public void setPalavras(String[] newPalavras) {
        if( newPalavras != null ) {
            this.palavras = newPalavras;
        }
        else
            this.palavras = new String[0];
        }

        /**
        Ajusta o array de ocorrencias.
         */
        public void setOcorrencias(int[] newOcorrencias) {
            if( newOcorrencias != null )
            this.ocorrencias = newOcorrencias;
        else
            this.ocorrencias = new int[0];
        }

        /**
            Ajusta o numero de frames.
         */
        public void setNumeroDeFrames(int newFrames) {
            if( newFrames >= 0 )
            this.frames = newFrames;
        else
            this.frames = 0;
        }

        /**
            Ajusta o numero de formularios.
         */
        public void setNumeroDeFormularios(int newForms) {
        if( newForms >= 0 )
            this.forms = newForms;
        else
            this.forms = 0;
        }

        /**
            Ajusta o numero de imagens.
         */
        public void setNumeroDeImagens(int newImages) {
        if( newImages >= 0 )
            this.images = newImages;
        else
            this.images = 0;
        }

        public Vector<String> getImages(){
        	return this.imagens;
        }
        
        /**
            Ajusta o array de links.
         */
        public void setLinks(URL[] newLinks) {
        if( newLinks != null )
          {
            this.links = new Vector(newLinks.length);
            for( int i = 0; i < newLinks.length; i++ )
                 links.addElement(newLinks[i]);
            this.URLabsolutas = newLinks;
          }
        else
          {
            this.links = new Vector(0);
            this.URLabsolutas = newLinks;
          }
        }

        /**
            Ajusta o vetor de e-mails.
         */
        public void setMails(Vector newMails) {
        if( newMails != null )
            this.mailList = newMails;
        else
            this.mailList = new Vector(0);
        }

        /**
            Substitui a entrada do termo na hastable que possui o mapeamento de posicoes.
        */
        public void substituirTermoPorCodigo(String termo,String codigo) {
        Vector v = (Vector)palavra_posicoes.get(termo);
        if( v != null ) {
            //System.out.println("<><><><>TROCANDO "+termo+" POR "+codigo);
            palavra_posicoes.remove(termo);
            palavra_posicoes.put(codigo,v);
        }
    }

//+-----------------------------------------------------------+//

        
    /**
    */
    public Hashtable palavra_posicoes(){
        return palavra_posicoes;
    }

    public void setPalavra_posicoes(Hashtable newPalavra_posicoes) {
        this.palavra_posicoes = newPalavra_posicoes;
    }

    /**
    */
    public Hashtable palavra_posicoes_meta(){
        return palavra_posicoes_meta;
    }

    public StopList stoplist() {
        return stoplist;
    }


     /**
     * metodo para testes
     */
    public static void main(String[] args) {
        try {
        	        	
            long          start = System.currentTimeMillis();
/*
            ParameterFile config = new ParameterFile(args[0].trim());
            StopList st = new StopListArquivo(config.getParam("STOPLIST_FILES"));
            if( config.getParam("GERAR_SEM_ACENTO") != null ) {
                PaginaURL.GERAR_SEM_ACENTO = Boolean.valueOf(config.getParam("GERAR_SEM_ACENTO")).booleanValue();
            }
            if( config.getParam("USAR_DESCRIPTION") != null ) {
                PaginaURL.USAR_DESCRIPTION = Boolean.valueOf(config.getParam("USAR_DESCRIPTION")).booleanValue();
            }
*/
            PaginaURL.USAR_DESCRIPTION = true;
            StopList st = null;
            try {
                st = new StopListArquivo(args[0]);
            }
            catch(Exception ioe) {
            }

//            StringBuffer content = new StringBuffer();
//            BufferedReader input = new BufferedReader(new FileReader(new File(
//                    args[1])));
//            for (String line = input.readLine(); line != null;
//                    line = input.readLine()) {
//
//                content.append(line);
//                content.append("\n");
//
//            }
//
//            String src = content.toString();
//            PaginaURL p = new PaginaURL(new URL("http://www"), 0, 0,
//                                           src.length(),
//                                           src, st);
            
            PaginaURL p = new PaginaURL(args[1].trim(), st);
            System.out.println(p.toString());
//            Vector<String> images = p.getImages();
//            for (int i = 0; i < images.size(); i++) {
//				System.out.println(images.elementAt(i));
//			}
            
//            System.out.println("USAR_DESCRIPTION = "+PaginaURL.USAR_DESCRIPTION);
//            long          end = System.currentTimeMillis();
//
//            long          time = end - start;
////            System.out.println("URL:");
////            System.out.println("--");
////            System.out.println(p.endereco());
//            System.out.println("Title:");
//            System.out.println("--");
//            System.out.println(p.titulo());
            
//            System.out.println("Modified -> " + p.dataQueFoiModificado());
//            System.out.println("Visited   -> " + p.dataQueFoiVisitado());
//            System.out.println("Paragraph:");
//            System.out.println("--");
//            System.out.println(p.paragrafo());
//            System.out.println("Links:");
//            System.out.println("--");
//
            URL[] links = p.links();

            for (int i = 0; i < links.length; i++) {
//                String str = i + " = " + links[i];
                System.out.println(links[i]);	
            }
//
//            String[] termos = p.palavras();
//            int[]    ocorrencias = p.ocorrencias();
//
//            System.out.println("Centroide:_SIZE("+termos.length+")");
//            System.out.println("--");
//
//            String[] termosMeta = p.palavrasMeta();
//            int[]    ocorrenciasMeta = p.ocorrenciasMeta();
//
//            for (int i = 0; i < termos.length; i++) {
//                Enumeration posicoes_pos = p.posicoes(termos[i]);
//                String posicoes = "Nula";
//                if( posicoes_pos != null ) {
//                    posicoes = "";
//                    int c = 0;
//                    while(posicoes_pos.hasMoreElements()) {
//                        posicoes += (c == 0 ? "" : ",")+posicoes_pos.nextElement();
//                        c++;
//                    }
//                }
//                String str = termos[i] + " = " + ocorrencias[i] + ", PL = "+posicoes;
//
//                System.out.println(str);
//            }
//            System.out.println("\nMeta termos:");
//
//            for (int i = 0; i < termosMeta.length; i++) {
//                String str = termosMeta[i] + " = " + ocorrenciasMeta[i];
//
//                System.out.println(str);
//            }
//
//            System.out.println("Palavras[" + termos.length + "] em "
//                               + p.endereco() + " tamanho do arquivo : "
//                               + p.tamanhoDoArquivo());

//
//            Vector<HACCluster> vLinks = new Vector<HACCluster>();
//            HashSet<String> uniqueURLs = new HashSet<String>();
            LinkNeighborhood[] ln =  p.getLinkNeighboor();
            System.out.println("SIZE:" + ln.length);
            for (int i = 0; i < ln.length; i++) {
            	System.out.println("URL:" + ln[i].getLink().toString());
            	System.out.println("ANC:" + ln[i].getAnchorString());
            	System.out.println("ARO:" + ln[i].getAroundString());
            	System.out.println("SRC:" + ln[i].getImgSrc());
            	System.out.println("ALT:" + ln[i].getAltString());
            }
//            System.out.println(">>>>>>>>>>"+ln.length);
//            for (int i = 0; i < ln.length; i++) {
//                System.out.println("Title:" + p.titulo());
//                if(!uniqueURLs.contains(ln[i].getLink().toString()) && !ln[i].getLink().getFile().equals("/")){
//                	uniqueURLs.add(ln[i].getLink().toString());
//                }else{
//                	continue;
//                }
//                String[] anchor = ln[i].getAnchor();
//            	System.out.println(">>>URL:" + ln[i].getLink().toString());
//                System.out.println(">>>ANC:" + ln[i].getAnchorString());
//                System.out.println(">>>ARO:" + ln[i].getAroundString());
//                if(anchor.length > 3){
//	                System.out.println("URL:"+ln[i].getLink());
//	                String url = ln[i].getLink().toString();
//	                String normURL = url;
//	        		int index = url.indexOf("?");
//	        		if(index != -1){
//	        			if(!url.contains("gen") && !url.substring(index).contains("&")){
//	        				normURL = url.substring(0,index);				
//	        			}
//	        		}
//	        		index = normURL.indexOf("index.jsp");
//	        		if(index != -1){
//	        			normURL = normURL.substring(0,index);
//	//        			normURL = normURL.substring(0,normURL.length()-9);
//	        		}
//	        		index = normURL.indexOf("wtSlotClick");
//	        		if(index != -1){
//	        			normURL = normURL.substring(0,index) + normURL.substring(index+24);
//	        		}
//	                URL url1 = new URL(normURL);
//	                VSMVector point = new VSMVector(url1.getFile(), st);
//	//                VSMVector point1 = new VSMVector(ln[i].getAnchorString(), st);
//	                vLinks.add(new HACCluster(url1.getFile(), point, null));
//	                System.out.println("FILE:"+ln[i].getLink().getFile());
//	                
//	                System.out.print("ANC: ");
//                
//                    for (int j = 0; j < anchor.length; j++) {
//                    	System.out.print(anchor[j] + " ");
//                    }
//                    System.out.println("");
//                }
//                System.out.println("");
//                System.out.print("ARO: ");
//                String[] around = ln[i].getAround();
//                for (int j = 0; j < around.length; j++) {
//                	System.out.print(around[j] + " ");
//                }
//                System.out.println("");
//                System.out.println("SOURCE:" + ln[i].getImgSrc());
//                System.out.print("ALT: ");
//                if(ln[i].getImgAlt() != null){
//                	String[] imgAlt = ln[i].getImgAlt();
//                	for (int j = 0; j < imgAlt.length; j++) {
//                		System.out.print(imgAlt[j] + " ");
//                	}
//                }
//            }
//			}

            // System.exit(1);

            /*
             * URL url=new URL(args[0]);
             * System.out.println("URL = "+url);
             * URLConnection con = url.openConnection();
             * System.out.println("getURL() = "+con.getURL());
             * System.out.println("getContentType() = "+con.getContentType());
             */
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    // fim da classe PaginaURL
}
