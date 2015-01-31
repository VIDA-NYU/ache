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
package focusedCrawler.util.distribution.rmi;


import java.rmi.registry.Registry;

import java.rmi.AccessException;

import java.rmi.NotBoundException;

import java.rmi.RemoteException;

import java.rmi.Remote;



public class LookupThread extends Thread {

    private Registry reg;

    private String lookup;

    private boolean found;

    private boolean finished;

    private Remote resultado;

    private Exception erro;



    public LookupThread(Registry reg, String lookup) {

        this.reg = reg;

        this.lookup = lookup;

        this.finished = false;

    } //LookupThread



    public Remote getRemoteObject() {

        return resultado;

    } //LookupThread



    public boolean getFinished () {

        return this.finished;

    } //getFinished



    public boolean getFound () {

        return this.found;

    } //getFound



    public Exception getError () {

        return this.erro;

    } //getError



    public void run() {

        System.out.println("Entrando no lookup!");

        found = false;

        try {

            resultado = reg.lookup(lookup);

            found = true;

        } //try

        catch(AccessException erro) {

            this.erro = erro;

        } //catch

        catch(NotBoundException erro) {

            this.erro = erro;

        } //catch

        catch(RemoteException erro) {

            this.erro = erro;

        } //catch

        this.finished = true;

        System.out.println("Saindo do lookup!");

    }

}

