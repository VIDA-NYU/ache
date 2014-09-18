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
package focusedCrawler.util.storage.socket;


public interface CommunicationConstants {



    public static final int METHOD_PING                     =  0;

    public static final int METHOD_INSERT                   =  1;

    public static final int METHOD_INSERT_ARRAY             =  2;

    public static final int METHOD_SELECT                   =  3;

    public static final int METHOD_SELECT_ARRAY             =  4;

    public static final int METHOD_SELECT_ENUMERATION       =  5;

    public static final int METHOD_UPDATE                   =  6;

    public static final int METHOD_UPDATE_ARRAY             =  7;

    public static final int METHOD_REMOVE                   =  8;

    public static final int METHOD_REMOVE_ARRAY             =  9;

    public static final int METHOD_ADD_RESOURCE             = 10;

    public static final int METHOD_ADD_RESOURCE_ARRAY       = 11;

    public static final int METHOD_REMOVE_RESOURCE          = 12;

    public static final int METHOD_REMOVE_RESOURCE_ARRAY    = 13;

    public static final int METHOD_COMMIT                   = 14;

    public static final int METHOD_ROLLBACK                 = 15;

    public static final int METHOD_FINALIZE                 = 16;



    public static final int METHOD_COUNT                    = 17;





    public static final int RETURN_OK                       = 151;

    public static final int RETURN_DATA_NOT_FOUND           = 152;

    public static final int RETURN_STORAGE_EXCEPTION        = 153;

    public static final int RETURN_COMMUNICATION_EXCEPTION  = 154;



    public static final int PARAM_OBJECT                    = 161;

    public static final int PARAM_ARRAY                     = 162;

    public static final int PARAM_ENUMERATION               = 163;



    public static final int[] RETURN_TYPES = {

        PARAM_OBJECT,       //METHOD_PING                     =  0;

        PARAM_OBJECT,       //METHOD_INSERT                   =  1;

        PARAM_ARRAY,        //METHOD_INSERT_ARRAY             =  2;

        PARAM_OBJECT,       //METHOD_SELECT                   =  3;

        PARAM_ARRAY,        //METHOD_SELECT_ARRAY             =  4;

        PARAM_ENUMERATION,  //METHOD_SELECT_ENUMERATION       =  5;

        PARAM_OBJECT,       //METHOD_UPDATE                   =  6;

        PARAM_ARRAY,        //METHOD_UPDATE_ARRAY             =  7;

        PARAM_OBJECT,       //METHOD_REMOVE                   =  8;

        PARAM_ARRAY,        //METHOD_REMOVE_ARRAY             =  9;

        PARAM_OBJECT,       //METHOD_ADD_RESOURCE             = 10;

        PARAM_ARRAY,        //METHOD_ADD_RESOURCE_ARRAY       = 11;

        PARAM_OBJECT,       //METHOD_REMOVE_RESOURCE          = 12;

        PARAM_ARRAY,        //METHOD_REMOVE_RESOURCE_ARRAY    = 13;

        PARAM_OBJECT,       //METHOD_COMMIT                   = 14;

        PARAM_OBJECT,       //METHOD_ROLLBACK                 = 15;

        PARAM_OBJECT        //METHOD_FINALIZE                 = 16;

    };



    public static final int[] PARAM_TYPES = {

        PARAM_OBJECT,   //METHOD_PING                     =  0;

        PARAM_OBJECT,   //METHOD_INSERT                   =  1;

        PARAM_ARRAY,    //METHOD_INSERT_ARRAY             =  2;

        PARAM_OBJECT,   //METHOD_SELECT                   =  3;

        PARAM_ARRAY,    //METHOD_SELECT_ARRAY             =  4;

        PARAM_OBJECT,   //METHOD_SELECT_ENUMERATION       =  5;

        PARAM_OBJECT,   //METHOD_UPDATE                   =  6;

        PARAM_ARRAY,    //METHOD_UPDATE_ARRAY             =  7;

        PARAM_OBJECT,   //METHOD_REMOVE                   =  8;

        PARAM_ARRAY,    //METHOD_REMOVE_ARRAY             =  9;

        PARAM_OBJECT,   //METHOD_ADD_RESOURCE             = 10;

        PARAM_ARRAY,    //METHOD_ADD_RESOURCE_ARRAY       = 11;

        PARAM_OBJECT,   //METHOD_REMOVE_RESOURCE          = 12;

        PARAM_ARRAY,    //METHOD_REMOVE_RESOURCE_ARRAY    = 13;

        PARAM_OBJECT,   //METHOD_COMMIT                   = 14;

        PARAM_OBJECT,   //METHOD_ROLLBACK                 = 15;

        PARAM_OBJECT    //METHOD_FINALIZE                 = 16;

    };

}