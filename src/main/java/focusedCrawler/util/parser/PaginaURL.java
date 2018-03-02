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


import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.StringTokenizer;
import java.util.TreeSet;
import java.util.Vector;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.validator.routines.UrlValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import focusedCrawler.crawler.crawlercommons.filters.basic.BasicURLNormalizer;
import focusedCrawler.target.model.Page;
import focusedCrawler.util.string.Acentos;
import focusedCrawler.util.string.StopList;
import okhttp3.HttpUrl;


public class PaginaURL {
    
    public static final Logger logger = LoggerFactory.getLogger(PaginaURL.class);

    private static final String[] schemes = {"http", "https"};
    private static final UrlValidator urlValidator = new UrlValidator(schemes);
    // TOR links (.onion) aren't accepted by the URL validator, so we check them using a regex.
    // Regex ".[^.]+" --> any string of at least 1 char without dot
    private static final Pattern onionValidator = Pattern.compile("https?://.[^.]+\\.onion.*");
    private static final List<String> invalidParameters =
            Arrays.asList("sid", "phpsessid", "sessionid", "jsessionid");
    private static final BasicURLNormalizer urlNormalizer =
            new BasicURLNormalizer(new TreeSet<>(invalidParameters), false);

    private int                MAXPALAVRAS = -1;
    public static final int    MAX_PARAGRAPH_SIZE = 255;
    private URL                baseUrl = null;
    private String             titulo = "";
    private String             paragrafo = "";
    private String[]           palavras = new String[0];
    private int[]              ocorrencias = new int[0];
    private String[]           palavrasMeta = new String[0];
    private int[]              ocorrenciasMeta = new int[0];
    private double             relevance = 0;

    /**
     * palavras_ordem \uFFFD um Hastable que mapeia uma palavras(um termo) a sua lista
     * de posicoes no documento. <BR>
     * Esta lista contem as posicoes do termo no texto. Ex:
     * no texto acima a lista da palavra "um" seria <3,9>. <BR>
     * Os tipo do Hashtable sao String que mapeia um Vector de Integer.
     */
    private Hashtable<String, Vector<Integer>> palavra_posicoes = new Hashtable<>();
    private Hashtable<String, Integer> palavra_pontos = new Hashtable<>();
    private Hashtable<String, Vector<Integer>> palavra_posicoes_meta = new Hashtable<>();
    private int                frames = 0;
    private int                forms = 0;
    private int                images = 0;

    private transient List<String> texto = new ArrayList<String>();
    private transient List<String> textoMeta = new ArrayList<String>();
    private URL[]              URLabsolutas = null;
    private List<String>     links = new ArrayList<String>();
    private boolean            noindex = false;
    private boolean            nofollow = false;
    public static boolean      USAR_DESCRIPTION = false;
    public static int          MAX_SIZE_HOST = 255;

    private transient StopList stoplist;
    private boolean            ordenarTermos = true;
    
    public PaginaURL(Page page) {
        this(page.getURL(), page.getContentAsString());
    }

    public PaginaURL(URL url, String content) {
        this(url, content, null);
    }

    public PaginaURL(URL url, String content, StopList stoplist) {
        this(url, content, false, false, -1, stoplist);
    }

    public PaginaURL(URL url, String arquivo, boolean noindex, boolean nofollow,
                     int max, StopList stoplist) {

        if (max > 0) {
            MAXPALAVRAS = max;
        } else {
            MAXPALAVRAS = -1;
        }

        this.baseUrl = url;
        this.noindex = noindex;
        this.nofollow = nofollow;
        this.stoplist = stoplist;

        if (!arquivo.equals("")) {
            separadorTextoCodigo(arquivo);
        }
    }

	private void loadHashCodes() {
		codes.put("&#1040;", "Ãï¿½");
		codes.put("&#1072;", "ÃÂ°");
		codes.put("&#1041;", "Ãâ");
		codes.put("&#1073;", "ÃÂ±");
		codes.put("&#1042;", "Ãâ");
		codes.put("&#1074;", "ÃÂ²");
		codes.put("&#1043;", "Ãâ");
		codes.put("&#1075;", "ÃÂ³");
		codes.put("&#1044;", "Ãâ");
		codes.put("&#1076;", "ÃÂ´");
		codes.put("&#1045;", "Ãâ¢");
		codes.put("&#1077;", "ÃÂµ");
		codes.put("&#1046;", "Ãâ");
		codes.put("&#1078;", "ÃÂ¶");
		codes.put("&#1047;", "Ãâ");
		codes.put("&#1079;", "ÃÂ·");
		codes.put("&#1048;", "ÃË");
		codes.put("&#1080;", "ÃÂ¸");
		codes.put("&#1049;", "Ãâ¢");
		codes.put("&#1081;", "ÃÂ¹");
		codes.put("&#1050;", "ÃÅ¡");
		codes.put("&#1082;", "ÃÂº");
		codes.put("&#1051;", "Ãâº");
		codes.put("&#1083;", "ÃÂ»");
		codes.put("&#1052;", "ÃÅ");
		codes.put("&#1084;", "ÃÂ¼");
		codes.put("&#1053;", "Ãï¿½");
		codes.put("&#1085;", "ÃÂ½");
		codes.put("&#1054;", "ÃÅ¾");
		codes.put("&#1086;", "ÃÂ¾");
		codes.put("&#1055;", "ÃÅ¸");
		codes.put("&#1087;", "ÃÂ¿");
		codes.put("&#1056;", "ÃÂ ");
		codes.put("&#1088;", "Ãâ¬");
		codes.put("&#1057;", "ÃÂ¡");
		codes.put("&#1089;", "Ãï¿½");
		codes.put("&#1058;", "ÃÂ¢");
		codes.put("&#1090;", "Ãâ");
		codes.put("&#1059;", "ÃÂ£");
		codes.put("&#1091;", "ÃÆ");
		codes.put("&#1060;", "ÃÂ¤");
		codes.put("&#1092;", "Ãâ");
		codes.put("&#1061;", "ÃÂ¥");
		codes.put("&#1093;", "Ãâ¦");
		codes.put("&#1062;", "ÃÂ¦");
		codes.put("&#1094;", "Ãâ ");
		codes.put("&#1063;", "ÃÂ§");
		codes.put("&#1095;", "Ãâ¡");
		codes.put("&#1064;", "ÃÂ¨");
		codes.put("&#1096;", "ÃË");
		codes.put("&#1065;", "ÃÂ©");
		codes.put("&#1097;", "Ãâ°");
		codes.put("&#1066;", "ÃÂª");
		codes.put("&#1098;", "ÃÅ ");
		codes.put("&#1067;", "ÃÂ«");
		codes.put("&#1099;", "Ãâ¹");
		codes.put("&#1068;", "ÃÂ¬");
		codes.put("&#1100;", "ÃÅ");
		codes.put("&#1069;", "ÃÂ­");
		codes.put("&#1101;", "Ãï¿½");
		codes.put("&#1070;", "ÃÂ®");
		codes.put("&#1102;", "ÃÅ½");
		codes.put("&#1071;", "ÃÂ¯");
		codes.put("&#1103;", "Ãï¿½");
	}

    private boolean filterURL = false;
    private ArrayList<String> around = new ArrayList<String>();
    private ArrayList<LinkNeighborhood> linkNeigh = new ArrayList<LinkNeighborhood>();
    private ArrayList<String>  imagens = new ArrayList<String>();
    

    public synchronized LinkNeighborhood[] getLinkNeighboor() {
        HashSet<String> unique = new HashSet<String>();

        Vector<LinkNeighborhood> tempLN = new Vector<LinkNeighborhood>();
        for (int i = 0; i < linkNeigh.size(); i++) {
            LinkNeighborhood ln = linkNeigh.get(i);

            String id = ln.getAnchorString() + ln.getLink().toString() + ln.getAroundString();
            if (!unique.contains(id)) {
                unique.add(id);

                int pointer = ln.getAroundPosition();
                Vector<String> aroundTemp = new Vector<String>();
                for (int j = pointer - (10 + ln.getNumWordsAnchor()); j < pointer + 10; j++) {
                    if (j >= 0 && j < around.size() && (j < pointer - ln.getNumWordsAnchor() || j > pointer - 1)) {
                        aroundTemp.add(around.get(j).toLowerCase());
                    }
                }
                
                String[] around = new String[aroundTemp.size()];
                aroundTemp.toArray(around);
                ln.setAround(around);
                
                if (baseUrl != null && baseUrl.getHost().equals(ln.getLink().getHost())) {
                    ln.setSameSite(true);
                }
                tempLN.add(ln);
            }
        }
        LinkNeighborhood[] lns = new LinkNeighborhood[tempLN.size()];
        tempLN.toArray(lns);
        return lns;
    }
    
    protected void separadorTextoCodigo(String arquivo) {    // arquivo equivale ao codigo HTML da pagina
        if(codes.size() == 0){
        	loadHashCodes();	
        }
    	
        boolean obj_isRDF = false;
        boolean ignorar_espacos = true;
        boolean tag_tipo_fim = false;
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

        // UTILIZANDO AS PALAVRAS DA URL COMO INFORMACAO TEXTUAL
        if (baseUrl != null && !filterURL) {


            StringTokenizer url_pontos = new StringTokenizer(baseUrl.getHost(),"./:");

            while (url_pontos.hasMoreTokens()) {
                String parte_host = url_pontos.nextToken();

                if (!parte_host.equals("www")
                    &&!parte_host.equals("org")
                    &&!parte_host.equals("gov")
                    &&!parte_host.equals("com")
                    &&!parte_host.equals("br")
                    &&!parte_host.equals("onion")) {

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
        HttpUrl base = (baseUrl == null) ? null : HttpUrl.get(baseUrl);

        Vector<String>  frames = new Vector<String>();
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

            while (n < arquivo.length()) {
                if (n_anterior < n) {                       /* we advanced a character */
                    ant1 = ant2;
                    ant2 = c;
                }

                n_anterior = n;
                c = arquivo.charAt(n);
                
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
                        /* faz o token da string */
                        if ((caracterFazParteDePalavra(c)) || (c == ';')
                                || (c == '&')) {
                            str += c;
                            n++;
                            int begin = str.indexOf("&#");
                            int end = str.indexOf(";");
                            if(begin != -1 && end != -1 && (begin+2)<end){
                            	String specialchar = str.substring(begin+2,end);
    							try {
                                	int hex = Integer.parseInt(specialchar);
        							char uni = (char)hex;
        							String unicode =  uni + "";
        							str = str.substring(0,begin) + unicode;
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
                            if(str.contains("ÃÂ±")){
                            	str = str.replace("ÃÂ±", "n");
                            	pos_caracter_especial = -1;
                            	continue;
                            }
                            if (c == '&') {
                                pos_caracter_especial = n;
                            } else   
                            	if (pos_caracter_especial != -1) {
                                int  posicao = str.length()
                                               - (n - pos_caracter_especial)
                                               - 1;
                                char ch = caracterEspecial(str, posicao);
                                
                                if (ch != '\0') {
                                    if (caracterFazParteDePalavra(ch)) {
                                        str = str.substring(0, posicao)
                                              + ch;
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
                      if(insideATag){
                        anchor = anchor + " " + str.toLowerCase();

                        numOfwordsAnchor++;
//                                                  insideATag = false;

                      }
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
                                        	}else{
                                        	}
                                        }
                                        	
                                        tagScript = str.equals("script") || str.equals("style");

                                        if (str.equals("form")) {
                                            this.forms++;
                                        }
                                    }

                                    str = "";
                                    fimDeString = false;
                                }
                            }
                        }
                        break;

                    case FECHANDO:
                        /* FECHANDO - expecting a close bracket, anything else is an error */
//                        if(ln!=null){
//                          ln.setAnchor(anchor);
//                          System.out.println("URL---"+ln.getLink());
 //                         System.out.println("ANC---"+ln.getAnchor());
  //                      }
                    	
                        if((tag_tipo_fim && tagName.equals("a")) || tagName.equals("area")){

                          insideATag = false;
                          if(ln!=null){
                            addLinkNeighborhood(ln, anchor, numOfwordsAnchor);
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
                            }

                            if (tagBody) {
                                em_body = !tag_tipo_fim;
                            }
                            if (tagOption) {
                                em_option = !tag_tipo_fim;
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
                                    tagScript = false;
                                    tagBody = false;
                                    tagTitulo = false;
                                    estado = FECHANDO;
                                } else {
                                    ignorar_espacos = true;
                                    n++;
                                }
                            }
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

                            textoMeta.add(str);    // adiciona a palavra na variavel texto

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
                            atributo = atributo.toLowerCase();
                            if (tagName.equals("a") && atributo.equals("href")) {
                                  insideATag = true;
                                  String urlTemp = addLink(str, base);
                                  if(urlTemp!= null && urlTemp.startsWith("http")){
                                	  if(ln!=null){
                                		  addLinkNeighborhood(ln, anchor, numOfwordsAnchor);
                                		  anchor = "";
                                		  ln = null;
                                	  }
                                	  try {
                                	      ln = new LinkNeighborhood(new URL(urlTemp));
                                	  } catch (Exception e) {
                                	      // Ignoring Exception on purpose since the URL in page is not proper
                                	  }
                                  }
                            } else if (tagName.equals("link") && atributo.equals("href")) {
                            	String urlTemp = null;
                            	if(!str.contains(".css") && !str.endsWith(".js")) {
                            		urlTemp = addLink(str, base);
                            	}
                            	if(urlTemp!= null && urlTemp.startsWith("http")){
                                    try {
                                        ln = new LinkNeighborhood(new URL(urlTemp));
                                    } catch (Exception e) {
                                        // Ignoring Exception on purpose since the URL in page is not proper
                                    }
                                }
                            } else if (tagName.equals("area") && atributo.equals("href")) {
                                String urlTemp = addLink(str, base);
                                if(urlTemp!= null && urlTemp.startsWith("http")){
                                  ln = new LinkNeighborhood(new URL(urlTemp));
                                }
                            } else if (tagName.equals("img")
                                       && atributo.equals("src")) {
                            	if(ln != null){
                            		ln.setImgSource(str);
                            	}
                            	try {
                            		imagens.add(resolveHttpLink(base,str).toString());	
								} catch (Exception e) {
									// TODO: handle exception
								}
                                
                            } 
//                            else if((tagName.equals("area") || tagName.equals("a"))&& atributo.equals("alt")){
//                            	anchor = anchor + " " + str.toLowerCase();
//                            } 
                            else if (tagName.equals("frame") && atributo.equals("src")) {
                                frames.addElement(str);
                                addLink(str, base);
                            } else if (tagName.equals("img") && (atributo.equals("alt") || atributo.equals("title") || atributo.equals("id"))) {
                            	Vector<String> altWords = new Vector<String>();
                            	StringTokenizer st = new StringTokenizer(str);
                                while(st.hasMoreTokens()) {
                                    String token = st.nextToken();
                                	if(token.contains("ÃÆÃÂ±")){
                                		token = token.replace("ÃÆÃÂ±", "n");
                                    }
                                	token = token.toLowerCase();
                                    if(token.contains("&#241;")){
                                    	token = token.replace("&#241;", "n");
                                    }
                                	if(token.contains("&ntilde;")){
                                    	token = token.replace("&ntilde;", "n");
                                    }
                                    if(token.contains("ÃÂ±")){
                                    	token = token.replace("ÃÂ±", "n");
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

                                StringTokenizer st = new StringTokenizer(str);
                                while(st.hasMoreTokens()) {
                                    String token = st.nextToken();
                                    textoMeta.add(token);    // adiciona a palavra na variavel texto
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
                            } else if (tagName.equals("base") && atributo.equals("href")) {
                                try {
                                    HttpUrl oldBase = (baseUrl == null) ? null : HttpUrl.get(baseUrl);
                                    String newBase = resolveHttpLink(oldBase, str);
                                    base = (newBase == null) ? null : HttpUrl.parse(newBase);
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

    private void addLinkNeighborhood(LinkNeighborhood ln, String anchor, int numOfwordsAnchor) {
        String[] anchorArray = tokenizeAnchorText(anchor);
        
        ln.setAnchor(anchorArray);
        ln.setAroundPosition(around.size());
        ln.setNumberOfWordsAnchor(numOfwordsAnchor);
        
        linkNeigh.add(ln.clone());
    }

    private String[] tokenizeAnchorText(String anchor) {
        ArrayList<String> anchorTemp = new ArrayList<String>();
        StringTokenizer tokenizer = new StringTokenizer(anchor," ");
        while(tokenizer.hasMoreTokens()){
          anchorTemp.add(tokenizer.nextToken());
        }
        return (String[]) anchorTemp.toArray(new String[anchorTemp.size()]);
    }

    /**
     * Declara\uFFFD\uFFFDo do M\uFFFDtodo
     *
     *
     * @see
     */
    protected void organizaDados() {

        // Cria arrays temporarios a partir da Hashtable
        //int      size = palavra_posicoes.size();
        int      size = palavra_pontos.size();
        String[] words = new String[size];
        int[]    numbers = new int[size];

        int      sizeMeta = palavra_posicoes_meta.size();
        String[] wordsMeta = new String[sizeMeta];
        int[]    numbersMeta = new int[sizeMeta];
        int	i = 0;

        for (Enumeration<String> seriewords = palavra_pontos.keys();
                seriewords.hasMoreElements(); ) {
            words[i] = ((String) seriewords.nextElement());
            i++;
        }

        i = 0;

        for (Enumeration<Integer> serienumbers = palavra_pontos.elements();
                serienumbers.hasMoreElements(); ) {
            //numbers[i] = ((Vector) serienumbers.nextElement()).size();
            numbers[i] = serienumbers.nextElement().intValue();
            i++;
        }

        i = 0;

        for (Enumeration<String> seriewordsMeta = palavra_posicoes_meta.keys();
                seriewordsMeta.hasMoreElements(); ) {
            wordsMeta[i] = seriewordsMeta.nextElement();
            i++;
        }

        i = 0;

        for (Enumeration<Vector<Integer>> serienumbersMeta = palavra_posicoes_meta.elements();
                serienumbersMeta.hasMoreElements(); ) {
            numbersMeta[i] = serienumbersMeta.nextElement().size();
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

    private HashMap<String,String> codes = new HashMap<String, String>();
    
    /**
     * Este metodo verifica se a partir daposicao dada a substring representa um simbolo
     * codificado tipo :   &aacute;   =  '\uFFFD'
     * retorna o caracter representado pelo simbolo no caso acima retorna '\uFFFD'
     * @param str o string buffer atual do processamento
     * @param pos a posicao inicial de testes
     */
    protected char caracterEspecial(String str, int pos) {
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

    protected String addLink(String link, HttpUrl base) {
        if (nofollow) {
            return "";
        }
        link = link.trim();
        link = resolveHttpLink(base, link);
        if (link == null) {
            return "";
        }
        link = removeFragmentsIfAny(link);
        link = StringEscapeUtils.unescapeHtml4(link);
        if (urlValidator.isValid(link) || onionValidator.matcher(link).matches()) {
            link = urlNormalizer.filter(link);
            if (link != null) {
                boolean exists = links.contains(link);
                if (!exists) {
                    if (base != null && !link.equals(base.toString())) {
                        links.add(link);
                    } else {
                        links.add(link);
                    }
                }
            }
        } else {
            // link is invalid
            link = null;
        }
        return link;
    }

    protected String removeFragmentsIfAny(String inputURL) {
        int fragmentPosition = inputURL.indexOf('#');
        if (fragmentPosition != -1) {
            return inputURL.substring(0, fragmentPosition);
        }
        return inputURL;
    }
    
    protected String resolveHttpLink(HttpUrl base, String link) {
        HttpUrl resolvedUrl;
        if(base == null) {
            resolvedUrl = HttpUrl.parse(link);
        } else {
            resolvedUrl = base.resolve(link);
        }
        if(resolvedUrl == null) {
            return null;
        } else {
            return resolvedUrl.toString();
        }
    }

    /**
     * Este metodo incrementa a pontuacao de um termo na pagina.
     * E' usado quando a pagina e' construida
     */
    protected void adicionaPontuacaoTermo(String termo, int pontos) {
        if (noindex) {
            return;
        }

        if (palavra_pontos == null) {
            palavra_pontos = new Hashtable<>();
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
            palavra_pontos = new Hashtable<>();
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
            texto = new Vector<>();
        }
        termo = termo.toLowerCase().trim();
        resultado = !irrelevante(termo);
        if( resultado ) {
            texto.add(termo);
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
            palavra_posicoes = new Hashtable<>();
        }

        termo = termo.toLowerCase();
         Vector<Integer> posicoes = palavra_posicoes.get(termo);

            if (posicoes == null) {
                    posicoes = new Vector<>();

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
            palavra_posicoes = new Hashtable<>();
        }

        termo = termo.toLowerCase();
        boolean dominio = termo.startsWith("#") && termo.endsWith("#");
        if(!irrelevante(termo) || dominio) {
            Vector<Integer> posicoes = palavra_posicoes.get(termo);

            if (posicoes == null) {
                if (!irrelevante(termo)) {
                    posicoes = new Vector<>();

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
            palavra_posicoes_meta = new Hashtable<>();
        }

        termo = termo.toLowerCase();

        Vector<Integer> posicoesMeta = palavra_posicoes_meta.get(termo);

        if (posicoesMeta == null) {
            if (!irrelevante(termo)) {
                posicoesMeta = new Vector<>();

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
            result = stoplist.isIrrelevant(str);
        }

        return result;
    }

    /**
     * Recebe dois arrays, um com n palavras, e outro com as ocorrencias
     * associadas a cada palavra, e os ordena em ordem decrescente de
     * ocorrencia, para que tenhamos as palavras que ocorrem mais no comeco
     * do array.
     */
    private void ordeneOcorrenc(int a[], String b[]) {
        quicksort_num(a, 0, a.length - 1, b);
    }

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
    private void troque1(int a[], int i, int j) {
        int temp;

        temp = a[i];
        a[i] = a[j];
        a[j] = temp;
    }

    private void troque2(String a[], int i, int j) {
        String temp;
        temp = a[i];
        a[i] = a[j];
        a[j] = temp;
    }

    public ArrayList<String> getImages() {
        return this.imagens;
    }

    public void setRelevance(double relev) {
        this.relevance = relev;
    }

    public double getRelevance() {
        return this.relevance;
    }

    public URL getURL() {
        return baseUrl;
    }

    public String titulo() {
        return titulo;
    }

    public String paragrafo() {
        return paragrafo;
    }

    public String[] palavras() {
        return palavras;
    }

    public String palavras_to_string() {
        if (palavras.length == 0)
            return "";
        String text = palavras[0];
        for (int i = 1; i < palavras.length; i++) {
            text = text + "  " + palavras[i];
        }
        return text;
    }

    public int[] ocorrencias() {
        return ocorrencias;
    }

    public String[] palavrasMeta() {
        return palavrasMeta;
    }

    public int[] ocorrenciasMeta() {
        return ocorrenciasMeta;
    }

    public int numeroDeFrames() {
        return frames;
    }

    public int numeroDeFormularios() {
        return forms;
    }

    public int numeroDeImagens() {
        return images;
    }

    public URL[] links() {
        if (URLabsolutas == null) {
            URLabsolutas = new URL[links.size()];

            for (int i = 0; i < links.size(); i++) {
                try {
                    URLabsolutas[i] = new URL(links.get(i).toString());
                } catch (Throwable t) {
                } // ignora
            }
        }
        return URLabsolutas;
    }

}
