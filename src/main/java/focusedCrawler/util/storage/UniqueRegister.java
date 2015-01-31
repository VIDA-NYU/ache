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
package focusedCrawler.util.storage;


public class UniqueRegister {

    private String  desc;

    private int     code;



    public UniqueRegister() {

    } //UniqueRegister



    public UniqueRegister(String _desc, int _code) {

        this.desc = _desc;

        this.code = _code;

    } //UniqueRegister



    public int getCode(){

        return code;

    } //getCode



    public String getDesc(){

        return desc;

    } //getDesc



    public void setCode(int _code){

        this.code = _code;

    } //setCode



    public void setDesc(String _desc){

        this.desc = _desc;

    } //setDesc



    public String toString() {

        return code + ":" + desc;

    } //toString

}