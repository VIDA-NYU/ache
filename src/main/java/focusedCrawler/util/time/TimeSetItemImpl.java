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
package focusedCrawler.util.time;

import java.util.Date;

import java.util.StringTokenizer;

import java.util.Vector;

import java.util.Calendar;

import java.util.GregorianCalendar;


public class TimeSetItemImpl implements TimeSetItem {


    private Vector items;

    private Calendar calendar;


    public TimeSetItemImpl () {

        items = new Vector ();

        calendar = new GregorianCalendar ();

    } //TimeSetItemImpl


    public void add (TimeItem item) {

        items.addElement (item);

    } //add


    public void remove (TimeItem item) {

        items.remove (item);

    } //remove


    public void removeAll () {

        items.removeAllElements ();

    } //removeAll


    public long nextTimeMillis () {

        return nextTimeMillis (System.currentTimeMillis ());

    } //nextTimeMillis


    public long nextTimeMillis (long time) {

        int resultado = 0;

        calendar.setTime (new Date (time));


        boolean overflow = false;

        int value;

        TimeItem item = null;

        for (int contador = 0; contador < items.size (); contador ++) {

            item = (TimeItem) items.elementAt (contador);

            value = calendar.get (item.getTimeField ());

            resultado += item.nextTimeMillis (value, overflow);

            overflow = item.overflow ();

        } //for

        return resultado;

    } //nextTimeMillis


    public TimeItem [] getItems () {

        TimeItem [] resultado = new TimeItem [items.size ()];

        items.copyInto (resultado);

        return resultado;

    } //getItems


    public String toString () {

        String resultado = "";

        for (int contador = 0; contador < items.size (); contador ++) {

            resultado += items.elementAt (contador) + " ";

        } //for

        return resultado;

    } //toString


    public String formatTime (long time) {

        long d = time / (1000 * 60 * 60 * 24);

        long h = (time / (60 * 60 * 1000)) % 24;

        long min = (time / (60 * 1000)) % 60;

        long s = (time / 1000) % 60;

        long mil = time % 1000;

        return "(" + d + ", " + h + ", " + min + ", " + s + ": " + mil + ")";

    } //formatTime


    public static TimeSetItem getDefaultTimeSetItem (String codigo) throws TimeItemException {

        TimeSetItem set = new TimeSetItemImpl ();

        set.add (new MillisecondTimeItem ("0"));

        set.add ( new SecondTimeItem ("0"));

        StringTokenizer stValues = new StringTokenizer (codigo.trim (), " ", false);

        int contador = 0;

        while (stValues.hasMoreTokens ()) {

            if (contador == 0) {

                set.add (new MinuteTimeItem (stValues.nextToken ()));

            } //if

            else if (contador == 1) {

                set.add (new HourTimeItem (stValues.nextToken ()));

            } //if

            else if (contador == 2) {

                set.add (new WeekDayTimeItem (stValues.nextToken ()));

            } //if

/*

            else if (contador == 3) {

                set.add (new MonthDayTimeItem (stValues.nextToken ()));

            } //if

            else if (contador == 4) {

                set.add (new MonthTimeItem (stValues.nextToken ()));

            } //if

*/

            else {

                break;

            } //else

            contador ++;

        } //while

        return set;

    } //getDefaultTimeSetItem


    public static void main (String [] args) throws TimeItemException {

        String codigo = args [0];

        java.util.Date date = new java.util.Date();

        long time = System.currentTimeMillis();

        TimeSetItem set = getDefaultTimeSetItem (codigo);

        long sleepTime = set.nextTimeMillis(time);


        System.out.println ("Codigo = " + codigo);

        System.out.println ("Tempo atual = " + date + "(" + date.getTime() + ") Próximo Tempo = " + sleepTime + " " + set.formatTime (sleepTime));

    } //main

}

