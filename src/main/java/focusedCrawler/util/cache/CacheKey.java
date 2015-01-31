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
package focusedCrawler.util.cache;



/**

 * Identifica a chave da cache.

 * Uma chave é basicamente um objeto que possui um objeto de hash.

 * Cada dado da cache deve ter uma chave CacheKey para representa-lo

 * unicamente.   

 *

 * @author Oscar Miranda

 * @version 1.0, 1999

 */

public interface CacheKey extends java.io.Serializable {



    /**

     * retorna a hashKey

     *

     * @return o objeto que representa a chave de hash.

     *

     * @see java.lang.Object

     */

    public Object hashKey();

}

