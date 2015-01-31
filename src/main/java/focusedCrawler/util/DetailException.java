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
/*

 * DetailException.java

 *

 * Created on July 5, 2001, 2:40 PM

 */



package focusedCrawler.util;



import java.io.PrintWriter;

import java.io.PrintStream;



/**

 *

 * @author  joao

 * @version

 */

public class DetailException extends Exception{

    public Throwable detail;



    public DetailException() {

        super();

    }



    public DetailException(String newMessage) {

        super(newMessage);

    }



    public DetailException(String newMessage,Throwable newDetail) {

        super(newMessage);

        this.detail = newDetail;

    }



    public DetailException(Throwable newDetail) {

        super();

        this.detail = newDetail;

    }



    public void printStackTrace() {

        printStackTrace(System.out);

    }



    public void printStackTrace(PrintWriter pw) {

        if (detail != null) {

            detail.printStackTrace(pw);

        }

        pw.println("--");

        super.printStackTrace(pw);

    }



    public void printStackTrace(PrintStream ps){

        if (detail != null) {

            detail.printStackTrace(ps);

        }

        ps.println("--");

        super.printStackTrace(ps);

    }

}

