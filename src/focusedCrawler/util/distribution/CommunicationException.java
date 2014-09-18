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
package focusedCrawler.util.distribution;





import focusedCrawler.util.DetailException;

public class CommunicationException extends DetailException{



	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;



	/**

     * Class constructor

     */

    public CommunicationException () {

        super();

    } //CommunicationException



	/**

     * Class constructor

     * @param message Detailed message of the exception

     */

    public CommunicationException (String message) {

        super(message);

    } //CommunicationException



	/**

     * Class constructor

     * @param message detailed message of the exception

     * @param _detail detail of the exception

     */

    public CommunicationException (String message, Throwable _detail) {

        super(message,_detail);        

    } //CommunicationException



} //CommunicationException

