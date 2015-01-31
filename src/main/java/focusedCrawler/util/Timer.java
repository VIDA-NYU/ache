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



public class Timer {

    private long time;

    private int contador=0;

    private long total=0;



    public void reset() {

        time = System.currentTimeMillis();

    }

    

    public void clear() {

        contador = 0;

        total = 0;

    }



    /**

     * retorna o tempo em ms entre o ultimo reset e a hora que o metodo for chamado. 

     */

    public long check() {

        long diff = System.currentTimeMillis()-time;

        contador++;

        total += diff;

        return diff;

    }



    public long media() {

      if (contador>0) return total/contador;

      else return 0;

    }



    public int contador() {

        return contador;

    }



    public long total() {

        return total;

    }



    public Timer() {

        reset();

        clear();

    }

    

    public String mediaString() {

        return toString(media());

    }



    public static String toString(long diff) {

        if (diff==0) return "0";

        long ms = diff%1000;

        long seg = (diff/1000)%60;

        long min = (diff/60000)%60;

        long h =   (diff/3600000)%60;

        long dia = diff/216000000;



        String str = null;

        if (dia>1) str = dia + "dias";

        else if (dia==1) str = "1dia";

        if (h>0) {

            if (str!=null) str += " ";

            else str = "";

            str += h + "h";

        }

        if (min>0) {

            if (str!=null) str += " ";

            else str = "";

            str += min + "min";

        }

        if (seg>0) {

            if (str!=null) str += " ";

            else str = "";

            str += seg + "s";

        }

        if (ms>0) {

            if (str!=null) str += " ";

            else str = "";

            str += ms + "ms";

        }

        return str;

    }



    public String toString() {

        return toString(check());

    }



    public static void main(String args[]) {

        long sleep=0;

        if (args.length>0) {

            sleep=Integer.parseInt(args[0]);

            System.out.println("Tempo esperado " + Timer.toString(sleep));

        }

        Timer t = new Timer();

        try { Thread.currentThread().sleep(sleep); }

        catch(InterruptedException ie) { }

        System.out.println("Tempo passado depois da inicializacao " + t);

    }

}



