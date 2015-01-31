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
public class HourTimeItem extends TimeItemImpl {


	public HourTimeItem () {

        super ();

	} //TimeItemImpl


	public HourTimeItem (String frequencia) throws TimeItemException {

        super (frequencia);

	} //TimeItemImpl


    protected void setVariables () {

		this.minimum = 0;

		this.maximum = 23;

        this.peso = 60000 * 60;

    } //setVariables


	public int getTimeField () {

        return Calendar.HOUR_OF_DAY;

    } //getTimeField


	public static void main (String [] args) throws TimeItemException {

        String codigo = args [0];

		System.out.println ("Codigo = " + codigo);


		TimeItem item = new HourTimeItem (codigo);


        int value = new Integer (args [1]).intValue ();

        System.out.println ("Peso=" + item.getPeso() + " Range(" + item.getMinimum() + "," + item.getMinimum() + ") Valor=" + value);

		System.out.println ("Proximo Tempo = " + item.nextTimeMillis (value, false));

	} //main

}

