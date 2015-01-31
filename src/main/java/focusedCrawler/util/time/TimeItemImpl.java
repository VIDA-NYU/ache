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
import java.util.StringTokenizer;
public abstract class TimeItemImpl implements TimeItem {
	protected int minimum;
	protected int maximum;
    protected int peso;
	protected int timefield;
	private int intervalo;
	private boolean acceptAll;
	private int [] values;
    private boolean overflow;
	public TimeItemImpl () {

		acceptAll = false;

        intervalo = 0;

        overflow = false;

        setVariables ();

	} //TimeItemImpl


	public TimeItemImpl (String frequencia) throws TimeItemException {

        this ();

		int tamanho = frequencia.length ();

		if ((frequencia != null) && (tamanho > 0)) {

			if (frequencia.charAt (0) == '*') {

				if (tamanho > 2) {

					setValues (new Integer (frequencia.substring (2, tamanho)).intValue ());

				} //if

				else {

					acceptAll = true;

				}

			} //if

			else {

				StringTokenizer stValues = new StringTokenizer (frequencia, " ,", false);

				int [] tempValues = new int [stValues.countTokens ()];

				int contador = 0;

				while (stValues.hasMoreTokens ()) {

					tempValues [contador] = new Integer (stValues.nextToken ()).intValue ();

					contador ++;

				} //while

				setValues (tempValues);

			} //else

		} //if

	} //TimeItemImpl


    protected abstract void setVariables ();


    public boolean overflow () {

        return this.overflow;

    } //overflow


	public int getMinimum () {

		return this.minimum;

	} //getMinimum


	public int getMaximum () {

		return this.maximum;

	} //getMaximum


	public int getPeso () {

		return this.peso;

	} //getPeso


	public void setValues (int [] values) throws TimeItemException {

		if (values == null) {

			throw new TimeItemException ("Valores nulo!!!");

		} //if

		if (values.length == 0) {

			throw new TimeItemException ("Valores com tamanho 0!!!");

		} //if

		for (int contador = 0; contador < values.length; contador ++) {

			if (values [contador] < 0) {

				throw new TimeItemException ("Valor [" + contador + "] negativo: " + values [contador]);

			} //if

		} //for

		this.values = values;

	}


	public void setValues (int intervalo) throws TimeItemException {

		if (intervalo < 1) {

			throw new TimeItemException ("Intervalo deve ser maior que 0: " + intervalo);

		} //if

		this.intervalo = intervalo;

	} //if


	public boolean isAcceptAll () {

		return this.acceptAll;

	} //isAcceptAll


	public void setIsAcceptAll (boolean acceptAll) {

		this.acceptAll = acceptAll;

	} //setIsAcceptAll


    // Transforma o valor original em outro na escala do campo do tempo

    protected long transformValue (long value) {

        return value;

    } //transformValue


    // Retorna o primeiro valor válido

    protected int getFirstValue () {

        if (isAcceptAll() || (intervalo != 0)) {

            return getMinimum();

        } //if

        return values [0];

    } //getFirstValue


    // Retorna o tempo existente entre o 1o. e o 2o. parâmetro

    protected long timeBetween (long firstTime, long lastTime) {

        if (lastTime >= firstTime) {

            this.overflow = false;

            return (lastTime - firstTime) * getPeso ();

        } //if

        else {

            this.overflow = true;

            return ((getMaximum () - firstTime +1) + (lastTime - getMinimum ())) * getPeso ();

        } //else

    } //timeBetween


    // retorna o tempo decorrido de value até o próximo válido

	public long nextTimeMillis (long value, boolean overflow) {

        // Se aceita todos, esse campo não determina tempo

		if (isAcceptAll ()) {

            this.overflow = false;

			return 0;

		} //if


        // Realizando transformação no valor original

        value = transformValue (value);

        if (overflow) {

            value ++;

        } //if


        // Testando o caso de intervalos. Ex.: */2

        if (intervalo != 0) {

            return timeBetween (value, value + (value % intervalo));

        } //else


        // Testando o caso de valores fixos. Ex.: 2,5,20

		for (int contador = 0; contador < values.length; contador ++) {

   			if (value <= values [contador]) {

                this.overflow = false;

  				return timeBetween (value, values [contador]);

            } //if

		} //for

        // Se chegou aqui, então ocorre overflow no caso de valores fixos

	    return timeBetween (value, getFirstValue ());

	} //nextTimeMillis


    public String toString () {

        if (isAcceptAll ()) {

            return "*";

        } //if

        if (intervalo != 0) {

            return "*/" + intervalo;

        } //if

        String resultado = "";

		for (int contador = 0; contador < values.length; contador ++) {

            if ("".equals (resultado)) {

                resultado += values[contador];

            } //if

            else {

                resultado += "," + values[contador];

            } //else

        } //for

        return resultado;

    } //toString


}

