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



package focusedCrawler.util.download;



/**

 * Classe responsavel pela criacao de um Thread de download para URL`s.

 * @author Thiago Santos 17/11/1999

 */

public class ThreadDownloadFactoryURL implements ThreadDownloadFactory {



    /**

     * Cria um thread de download para URL.

     * @param alvo URL alvo.

     * @return Um thread que e responsavel pelo download da URL dada.

     */

    public ThreadDownload criarThread(String alvo) {

        return new ThreadDownloadURL(alvo);

    }



    public ThreadDownload criarThread(String alvo,int timeout) {

        return new ThreadDownloadURL(alvo,timeout);

    }



    public static void main(String args[]){

        String url = args[0];

        int timeout = Integer.parseInt(args[1]);

        ThreadDownloadFactoryURL t = new ThreadDownloadFactoryURL();

        while(true){



        try {

            ThreadDownload t1 = t.criarThread(url,timeout);

            t1.start();

            long INICIO = System.currentTimeMillis();

            long TEMPO  = 0;

            long MAXIMO = timeout;

            boolean ok = true;

            while( !t1.ready() && ok ) {

                Thread.sleep(100);

                TEMPO = (System.currentTimeMillis()-INICIO);

                ok = TEMPO < MAXIMO;

                System.out.println("Dormindo TEMPO "+TEMPO);

            }

            t1.setReady(true);

            System.out.println( ok ? "TEMPO OK" : "TEMPO ESTOURADO" );

            System.out.println( "Tempo total = " + TEMPO );

            System.out.println("DADOS ->"+t1.getDados());

        }

        catch(Exception exc) {

            exc.printStackTrace();

        }

      }

    }

}







/*--- formatting done in "Convenção de Codificação do Radix" style on 11-17-1999 ---*/



