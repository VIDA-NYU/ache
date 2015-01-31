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

import java.util.Calendar;

/**

 * Title:

 * Description: Encode a long date in a int and decode the int in

 *              a long data is usefull when you have only 4 bytes

 *              to store a date.

 * Copyright:    Copyright (c) 2000

 * Company:      radix.com

 * @author João Batista da Rocha Junior(joao@radix.com)

 * @version 1.0

 */



public class DateEncoder {



    public static int encode(long date){

        return (int)(date/1000);

    }

}