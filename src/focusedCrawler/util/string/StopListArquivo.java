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
/*

 * @(#)StopListArquivo.java

 *

 * Copyright (c) 1997-1999 Departamento de Informática - UFPE

 *    Grupo:

 *      Luciano de A. Barbosa           (lab)

 *      Oscar G. de Miranda             (ogm)

 *      Thiago L.V.L. Santos          (tlvls)

 *      Flavio Couto                   (frco)

 */



package focusedCrawler.util.string;



import java.io.File;

import java.io.BufferedReader;

import java.io.FileReader;

import java.io.IOException;

import java.util.Vector;

import java.util.StringTokenizer;

import focusedCrawler.util.ParameterFile;



public class StopListArquivo extends AbstractStopList {


        public static String PORTUGUES = "darwin"+File.separator+"contentanaliser"+File.separator+"text"+File.separator+"factory"+File.separator+"stoplist"+File.separator+"portugues.txt";
        public static String INGLES    = "darwin"+File.separator+"contentanaliser"+File.separator+"text"+File.separator+"factory"+File.separator+"stoplist"+File.separator+"ingles.txt";
        public static String COMPUTES  = "darwin"+File.separator+"contentanaliser"+File.separator+"text"+File.separator+"factory"+File.separator+"stoplist"+File.separator+"computes.txt";
        public static String DEFAULT   = PORTUGUES+" "+INGLES+" "+COMPUTES;
        public static String MARCADOR_EXCECOES       = "*** Excecoes";
        public static String MARCADOR_IRRELEVANTES   = "*** Palavras Irrelevantes";
        public static String MARCADOR_COMPLEMENTARES = "*** Palavras Complementares";
        public static String MARCADOR_PREFIXOS       = "*** Prefixos";
        public static String MARCADOR_SUFIXOS        = "*** Terminacoes Ignoraveis";



    public StopListArquivo() {

        super();

        try {

            inicializarArquivos( DEFAULT );

        }

        catch(IOException ioe) {

            ioe.printStackTrace();

            System.out.println("Não conseguiu inicializar o arquivo default : "+ DEFAULT );

        }

    }



    public StopListArquivo( String filenames ) throws IOException {

        super();

        inicializarArquivos( filenames );

    }



    public StopListArquivo( ParameterFile config ) throws IOException {

        super();

        inicializarArquivos( config.getParam("STOPLIST_FILES") );

    }



    private void inicializarArquivos( String filenames ) throws IOException {

        StringTokenizer tokens = new StringTokenizer( filenames," " );



        final int INICIO         = 1;

        final int EXCECOES       = INICIO + 1;

        final int IRRELEVANTES   = EXCECOES + 1;

        final int COMPLEMENTARES = IRRELEVANTES + 1;

        final int PREFIXOS       = COMPLEMENTARES + 1;

        final int SUFIXOS        = PREFIXOS + 1;



        Vector excecoes       = new Vector();

        Vector irrelevantes   = new Vector();

        Vector complementares = new Vector();

        Vector prefixos       = new Vector();

        Vector sufixos        = new Vector();



        while( tokens.hasMoreTokens() )

             {

               BufferedReader in = new BufferedReader( new FileReader(tokens.nextToken()) );

               try {

                     int estado = INICIO;

                     for( String temp = in.readLine() ; temp != null ; temp = in.readLine() )

                        {

                          temp = temp.trim();

                          if( temp.length() > 0 && !temp.startsWith("#") )

                            {

                              if( temp.startsWith(MARCADOR_EXCECOES) )

                                  estado = EXCECOES;

                              else if( temp.startsWith(MARCADOR_IRRELEVANTES) )

                                  estado = IRRELEVANTES;

                              else if( temp.startsWith(MARCADOR_COMPLEMENTARES) )

                                  estado = COMPLEMENTARES;

                              else if( temp.startsWith(MARCADOR_PREFIXOS) )

                                  estado = PREFIXOS;

                              else if( temp.startsWith(MARCADOR_SUFIXOS) )

                                  estado = SUFIXOS;

                              else {

                                     switch( estado )

                                           {

                                             case EXCECOES:

                                                excecoes.addElement(temp);

                                                break;

                                             case IRRELEVANTES:

                                                irrelevantes.addElement(temp);

                                                break;

                                             case COMPLEMENTARES:

                                                complementares.addElement(temp);

                                                break;

                                             case PREFIXOS:

                                                prefixos.addElement(temp);

                                                break;

                                             case SUFIXOS:

                                                sufixos.addElement(temp);

                                                break;

                                           }

                                   }

                            }

                        }

                   }

               catch( IOException ioe )

                   {

                     in.close();

                   }

               in.close();

             }



        if( excecoes.size() > 0 )

          {

            String tmp[] = new String[ excecoes.size() ];

            excecoes.copyInto( tmp );

            super.setExcecoes( tmp );          // modifica o array da super classe que era nulo

          }

        if( irrelevantes.size() > 0 )

          {

            String tmp[] = new String[ irrelevantes.size() ];

            irrelevantes.copyInto( tmp );

            super.setIrrelevantes( tmp );          // modifica o array da super classe que era nulo

          }

        if( complementares.size() > 0 )

          {

            String tmp[] = new String[ complementares.size() ];

            complementares.copyInto( tmp );

            super.setComplementares( tmp );    // modifica o array da super classe que era nulo

          }

        if( prefixos.size() > 0 )

          {

            String tmp[] = new String[ prefixos.size() ];

            prefixos.copyInto( tmp );

            super.setPrefixos( tmp );       // modifica o array da super classe que era nulo

          }

        if( sufixos.size() > 0 )

          {

            String tmp[] = new String[ sufixos.size() ];

            sufixos.copyInto( tmp );

            super.setSufixos( tmp );       // modifica o array da super classe que era nulo

          }

    }



    public static void main(String[] args) throws Exception {

        StopListArquivo stopList = new StopListArquivo(args[0]);

        stopList.printListas();

        String palavra = "منتدى";

        System.out.println();

        System.out.println("Palavra                      = " + palavra );

        System.out.println("apenasNumeroEHifen           = " + stopList.apenasNumeroEHifen( palavra ) );

        System.out.println("possuiCaracteresIrrelevantes = " + stopList.possuiCaracteresIrrelevantes( palavra ) );

        System.out.println("eExcecao                     = " + stopList.pertenceAoArray( palavra,stopList.getExcecoes() ) );

        System.out.println("eIrrelevante                 = " + stopList.pertenceAoArray( palavra,stopList.getIrrelevantes() ) );

        System.out.println("eComplementar                = " + stopList.pertenceAoArray( palavra,stopList.getComplementares() ) );

        System.out.println("temPrefixo                   = " + stopList.possuiPrefixos( palavra,stopList.getPrefixos() ) );

        System.out.println("temSufixo                    = " + stopList.possuiSufixos( palavra,stopList.getSufixos() ) );

        System.out.println("RESULTADO -> " + stopList.eIrrelevante(palavra) );

    }

}