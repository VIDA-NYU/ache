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

import java.util.Calendar;

import java.util.HashSet;

import java.util.GregorianCalendar;


public class MonthDayTimeItem extends TimeItemImpl {


    private HashSet hs30;

    private HashSet hs31;


	public MonthDayTimeItem () {

        super ();

        hs30 = new HashSet ();

        hs31 = new HashSet ();

        hs31.add (new Integer (Calendar.JANUARY));

        hs31.add (new Integer (Calendar.MARCH));

        hs30.add (new Integer (Calendar.APRIL));

        hs31.add (new Integer (Calendar.MAY));

        hs30.add (new Integer (Calendar.JUNE));

        hs31.add (new Integer (Calendar.JULY));

        hs31.add (new Integer (Calendar.AUGUST));

        hs30.add (new Integer (Calendar.SEPTEMBER));

        hs31.add (new Integer (Calendar.OCTOBER));

        hs30.add (new Integer (Calendar.NOVEMBER));

        hs31.add (new Integer (Calendar.DECEMBER));

	} //MonthDayTimeItem


	public MonthDayTimeItem (String frequencia) throws TimeItemException {

        super (frequencia);

	} //MonthDayTimeItem


    protected void setVariables () {

		this.minimum = 1;

        this.peso = 60000 *60 *24;

    } //setVariables


	public int getTimeField () {

        return Calendar.DAY_OF_MONTH;

    } //getTimeField


	public int getMaximum () {

		Calendar calendar = new GregorianCalendar ();

        int mes = calendar.get (Calendar.MONTH);

        if (mes == Calendar.FEBRUARY) {

            int ano = calendar.get (Calendar.YEAR);

            if (ano % 4 == 0) {

                return 29;

            } //if

            else {

                return 28;

            } //else

        } //if

        else if (hs30.contains (new Integer (mes))) {

            return 30;

        } //else

        else if (hs31.contains (new Integer (mes))) {

            return 31;

        } //else

        return -1;

	} //getMaximum


	public static void main (String [] args) throws TimeItemException {

        String codigo = args [0];

		System.out.println ("Codigo = " + codigo);


		TimeItem item = new MonthDayTimeItem (codigo);


        int value = new Integer (args [1]).intValue ();

        System.out.println ("Peso=" + item.getPeso() + " Range(" + item.getMinimum() + "," + item.getMinimum() + ") Valor=" + value);

		System.out.println ("Proximo Tempo = " + item.nextTimeMillis (value, false));

	} //main

}

