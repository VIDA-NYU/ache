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



import focusedCrawler.util.ParameterFile;
import focusedCrawler.util.cache.CacheKey;


public class RemoteCacheKey implements CacheKey {



    private String hostname;

    private int port;

    private String objectname;



    public RemoteCacheKey(ParameterFile config, String paramhostname, String paramport, String paramobjectname) {

        setHostname (config.getParam (paramhostname));

        setPort (new Integer (config.getParam (paramport)).intValue());

        setObjectname (config.getParam (paramobjectname));

    } //RemoteCacheKey



    public RemoteCacheKey(String hostname, int port, String objectname) {

        this.hostname = hostname;

        this.port = port;

        this.objectname = objectname;

    } //RemoteCacheKey



    public Object hashKey() {

        return hostname + ":" + port + "/" + objectname;

    } //hashKey



    public String getHostname () {

        return this.hostname;

    } //getHostname



    public void setHostname (String hostname) {

        this.hostname = hostname;

    } //setHostname



    public int getPort () {

        return this.port;

    } //getPort



    public void setPort (int port) {

        this.port = port;

    } //setPort



    public String getObjectname () {

        return this.objectname;

    } //getObjectname



    public void setObjectname (String objectname) {

        this.objectname = objectname;

    } //getObjectname



    public String toString() {

        return "rmi:" + getHostname() + ":" + getPort() + "/" + getObjectname();

    } //toString

}