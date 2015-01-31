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
/**

 * @author Marcelo Fernandes

 * @version 1.0,  23/08/99

 */



package focusedCrawler.util.time;



import java.io.IOException;

import java.io.File;

import java.util.StringTokenizer;

import java.util.Calendar;

import java.util.HashSet;

import java.util.GregorianCalendar;



public class WeekDayTimeItem extends TimeItemImpl {



	public WeekDayTimeItem () {

        super ();

	} //WeekDayTimeItem



	public WeekDayTimeItem (String frequencia) throws TimeItemException {

        super (frequencia);

	} //WeekDayTimeItem



    protected void setVariables () {

		this.minimum = 0;

        this.maximum = 6;

        this.peso = 60000 *60 *24;

    } //setVariables



	public int getTimeField () {

        return Calendar.DAY_OF_WEEK;

    } //getTimeField



    protected long transformValue (long value) {

        if (value == Calendar.SUNDAY) {

            return 0;

        } //if

        else if (value == Calendar.MONDAY) {

            return 1;

        } //else

        else if (value == Calendar.TUESDAY) {

            return 2;

        } //else

        else if (value == Calendar.WEDNESDAY) {

            return 3;

        } //else

        else if (value == Calendar.THURSDAY) {

            return 4;

        } //else

        else if (value == Calendar.FRIDAY) {

            return 5;

        } //else

        else if (value == Calendar.SATURDAY) {

            return 6;

        } //else

        else return -1;

    } //transformValue



    public static void main (String [] args) throws TimeItemException {

        String codigo = args [0];

		System.out.println ("Codigo = " + codigo);



		TimeItem item = new WeekDayTimeItem (codigo);



        int value = new Integer (args [1]).intValue ();

        System.out.println ("Peso=" + item.getPeso() + " Range(" + item.getMinimum() + "," + item.getMinimum() + ") Valor=" + value);

		System.out.println ("Proximo Tempo = " + item.nextTimeMillis (value, false));

	} //main

}

