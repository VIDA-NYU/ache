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

package achecrawler.util.string;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("serial")
public class StopListFile extends AbstractStopList {

    public static StopListFile DEFAULT;

    private static String MARCADOR_EXCECOES = "*** Excecoes";
    private static String MARCADOR_IRRELEVANTES = "*** Palavras Irrelevantes";
    private static String MARCADOR_COMPLEMENTARES = "*** Palavras Complementares";
    private static String MARCADOR_PREFIXOS = "*** Prefixos";
    private static String MARCADOR_SUFIXOS = "*** Terminacoes Ignoraveis";

    final int INICIO = 1;
    final int EXCECOES = INICIO + 1;
    final int IRRELEVANTES = EXCECOES + 1;
    final int COMPLEMENTARES = IRRELEVANTES + 1;
    final int PREFIXOS = COMPLEMENTARES + 1;
    final int SUFIXOS = PREFIXOS + 1;

    static {
        String filename = "stopwords.en.txt";
        try (InputStream f = StopListFile.class.getClassLoader().getResourceAsStream(filename)) {
            DEFAULT = new StopListFile(f);
        } catch (IOException e) {
            throw new RuntimeException("Failed to load stopwords file");
        }
    }

    public StopListFile(InputStream... files) throws IOException {
        parseFiles(files);
    }

    public StopListFile(String... filenames) throws IOException {

        InputStream[] files = new InputStream[filenames.length];
        for (int i = 0; i < filenames.length; i++) {
            files[i] = new FileInputStream(filenames[i]);
        }
        parseFiles(files);
    }
    
    public void parseFiles(InputStream... files) throws IOException {
        List<String> excecoes = new ArrayList<>();
        List<String> irrelevantes = new ArrayList<>();
        List<String> complementares = new ArrayList<>();
        List<String> prefixos = new ArrayList<>();
        List<String> sufixos = new ArrayList<>();

        for(int i=0; i < files.length; i++) {
            
            List<String> lines = readLines(files[i]);
            
            int estado = INICIO;
            for (String temp : lines) {
                temp = temp.trim();
                if (temp.length() > 0 && !temp.startsWith("#")) {

                    if (temp.startsWith(MARCADOR_EXCECOES))
                        estado = EXCECOES;
                    else if (temp.startsWith(MARCADOR_IRRELEVANTES))
                        estado = IRRELEVANTES;
                    else if (temp.startsWith(MARCADOR_COMPLEMENTARES))
                        estado = COMPLEMENTARES;
                    else if (temp.startsWith(MARCADOR_PREFIXOS))
                        estado = PREFIXOS;
                    else if (temp.startsWith(MARCADOR_SUFIXOS))
                        estado = SUFIXOS;
                    else {
                        switch (estado)
                        {
                        case EXCECOES:
                            excecoes.add(temp);
                            break;
                        case IRRELEVANTES:
                            irrelevantes.add(temp);
                            break;
                        case COMPLEMENTARES:
                            complementares.add(temp);
                            break;
                        case PREFIXOS:
                            prefixos.add(temp);
                            break;
                        case SUFIXOS:
                            sufixos.add(temp);
                            break;
                        }
                    }
                }
            }
        }
            
        if (excecoes.size() > 0) {
            String tmp[] = new String[excecoes.size()];
            excecoes.toArray(tmp);
            super.setExcecoes(tmp); // modifica o array da super classe que era nulo
        }

        if (irrelevantes.size() > 0) {
            String tmp[] = new String[irrelevantes.size()];
            irrelevantes.toArray(tmp);
            super.setIrrelevantes(tmp); // modifica o array da super classe que era nulo
        }

        if (complementares.size() > 0) {
            String tmp[] = new String[complementares.size()];
            complementares.toArray(tmp);
            super.setComplementares(tmp); // modifica o array da super classe que era nulo
        }

        if (prefixos.size() > 0) {
            String tmp[] = new String[prefixos.size()];
            prefixos.toArray(tmp);
            super.setPrefixos(tmp); // modifica o array da super classe que era nulo
        }

        if (sufixos.size() > 0) {
            String tmp[] = new String[sufixos.size()];
            sufixos.toArray(tmp);
            super.setSufixos(tmp); // modifica o array da super classe que era nulo
        }
        
    }

    private List<String> readLines(InputStream fileStream) throws IOException, FileNotFoundException {
        if(fileStream == null) {
            throw new IllegalArgumentException("Input stream can't be nul;");
        }
        List<String> lines = new ArrayList<>();
        try (BufferedReader in = new BufferedReader(new InputStreamReader(fileStream)) ) {
            for (String temp = in.readLine(); temp != null; temp = in.readLine()) {
                lines.add(temp);
            }
        }
        return lines;
    }

}