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



import java.io.*;

import java.util.Calendar;

import java.util.GregorianCalendar;





//teste

import java.util.Enumeration;

// versao 0.01p 16/02/98

public class Log

{

    private PrintStream logOut = System.out;

    private static Log logDefault = new Log (System.out);

    public static boolean log = false;



    static {

        log = Boolean.valueOf(System.getProperty("log","false")).booleanValue();

    }



    public Log() {

        this("log.dat");

    }



    public Log(String filename) {

        if ( filename != null && !filename.equals("")) {

          try {

            logOut = new PrintStream ( new FileOutputStream( filename));

          } catch ( FileNotFoundException fnfe){

            throw new IllegalArgumentException ("nome de arquivo invalido :" +fnfe.getMessage());

          }

        } else {

          throw new IllegalArgumentException ("nome de arquivo invalido");

        }

    }



    public Log(PrintStream printstream) {

       logOut = printstream;

    }



    public static Log getLogDefault(){

      return logDefault;

    }



    /**

     *  Escreve o string dado em um arquivo.

     *  @deprecated este metodo será substituido pelo metodo logMessage

     */

    public void writeLine(String str)

    {

        logOut.println(str);

        logOut.flush();

   }



    public synchronized void logMessage (Object obj, String objectName, String action) {

        logMessage (obj.getClass().getName(), objectName, action);

    } //logMessage



    public synchronized void logMessage (String classNick, String objectName, String action) {

        log (classNick, objectName, action, logOut);

    } //logMessage



    public synchronized static void log (Object obj, String objectName, String action) {

        log (obj.getClass().getName(), objectName, action);

    }//log



    public synchronized static void log (String classNick, String objectName, String action) {

        log (classNick, objectName, action, System.out);

    } //Log



    public synchronized static void log (String classNick, String objectName, String action, PrintStream logStream) {

        logStream.println ("[" + getData () + "] [" + classNick + "] [" + objectName + "] [" + action + "]");

        logStream.flush();

    } //Log



    public static String getData () {

        Calendar calendar = new GregorianCalendar ();

        String day_of_month = new Integer (calendar.get (Calendar.DAY_OF_MONTH)).toString();

        String month = getMonth (calendar.get (Calendar.MONTH));

        String year = new Integer (calendar.get (Calendar.YEAR)).toString();

        String hour = new Integer (calendar.get (Calendar.HOUR_OF_DAY)).toString();

        String minute = new Integer (calendar.get (Calendar.MINUTE)).toString();

        String second = new Integer (calendar.get (Calendar.SECOND)).toString();

//        String milissecond = new Integer (calendar.get (Calendar.MILLISECOND)).toString ();

        return day_of_month + "/" + month + "/" + year + ":" + hour + ":" + minute + ":" + second;

    } //getData



    public static String getMonth (int month) {

        switch (month) {

            case Calendar.JANUARY:   return "JAN";

            case Calendar.FEBRUARY:  return "FEB";

            case Calendar.MARCH:     return "MAR";

            case Calendar.APRIL:     return "APR";

            case Calendar.MAY:       return "MAY";

            case Calendar.JUNE:      return "JUN";

            case Calendar.JULY:      return "JUL";

            case Calendar.AUGUST:    return "AUG";

            case Calendar.SEPTEMBER: return "SET";

            case Calendar.OCTOBER:   return "OCT";

            case Calendar.NOVEMBER:  return "NOV";

            case Calendar.DECEMBER:  return "DEC";

        } //switch

        return "";

    } //getMonth





    static public void main(String[] args) {

        Log.log(new Log().getClass().getName(), "log do sistema", "erro");

    }

}
