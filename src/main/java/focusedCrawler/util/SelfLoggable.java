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
package focusedCrawler.util;





//import java.io.PrintStream;

import java.io.*;

/**

 * versao 0.01p 14/03/98

 */

public class SelfLoggable extends Thread {



    /**

     * Local onde as mensagens de LOG sao escritas.

     * por default é System.out.

     */

    public PrintStream outLog = System.out;

//     public FileOutputStream outLog;





    /**

     * Local onde as mensagens de ERRO sao escritas.

     * por default é System.out.

     */

    public PrintStream outErr = System.out;

//    public FileOutputStream outErr;



public SelfLoggable(){

//    try{

//    outLog = new FileOutputStream("Relacional");

//    outErr = new FileOutputStream("RelacionalErro");

//        }

//    catch(IOException io){io.printStackTrace();}

    }

    /**

     * Este metodo muda o local onde as mensagens de ERRO serao escritas.

     * @param   out   a nova saida de Erros.

     */

    public void setErrOutput(PrintStream out) {

        if ( out != null ) this.outErr = out;

        else throw new IllegalArgumentException ("Output nao pode ser null");

    }





    /**

     * Este metodo muda o local onde as mensagens de log serao escritas.

     * @param   out   a nova saida de log.

     */

    public void setLogOutput(PrintStream out) {

        if ( out != null ) this.outLog = out;

        else throw new IllegalArgumentException ("Output nao pode ser null");

    }



    /**

     * Este metodo escreve algo na saida de Log.

     */

    public void writeLog(String str) {

//        try{

//	    String temp;

            if ( print) {

              outLog.println(str);

//            outLog.print(str+"\n\r");

//              temp = str + "\n\r";

//	          outLog.write(temp.getBytes());

//              outLog.flush();

//              }

           }

//        catch(IOException io){io.printStackTrace();}

    }



    /**

     * Este metodo escreve algo na saida de Erros.

     */

    public void writeErr(String str) {

//        try{String temp;

        if ( print) {

              outErr.println(str);

//            outErr.print(str + "\n\r");

//            temp = str + "\n\r";

//            outErr.write(temp.getBytes());

//            outErr.flush();

//           }

        }

//        catch(IOException io){io.printStackTrace();}

    }



    protected boolean print = true;



    /**

     * Este metodo modifica o estado de impressao, isto e', diz se

     * e' para imprimir ou nao mensagens na tela.

     * @param    state    o novo estado de impressao.

     */

    public void setPrint(boolean state) {

        print = state;

    }

}

