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
package focusedCrawler.target;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import focusedCrawler.util.Target;

/**
 * <p> </p>
 *
 * <p>Description: This class stores a page in the file system</p>
 *
 * <p>Copyright: Copyright (c) 2004</p>
 *
 * <p> </p>
 *
 * @author Luciano Barbosa
 * @version 1.0
 */
public class TargetFileRepository implements TargetRepository {

    private static final Logger logger = LoggerFactory.getLogger(TargetFileRepository.class);
    private Path location;

    public TargetFileRepository(String location) {
        this.location = Paths.get(location);
    }
    
    public TargetFileRepository(Path location) {
    	File directory = location.toFile();
    	if(!directory.exists()) {
    		directory.mkdirs();
    	}
        this.location = location;
    }

    public boolean insert(Target target, int counter) {
        return insert(target);
    }

    public boolean insert(Target target) {
        boolean contain = false;
        try {
            String id = target.getIdentifier();
            URL url = new URL(id);
            String host = url.getHost();

            Path hostPath = location.resolve(URLEncoder.encode(host, "UTF-8"));

            File hostDirectory = hostPath.toFile();
            if (!hostDirectory.exists()) {
                hostDirectory.mkdir();
            }

            Path filePath = hostPath.resolve(URLEncoder.encode(id, "UTF-8"));
            FileWriter fw = new FileWriter(filePath.toFile());
            BufferedWriter bw = new BufferedWriter(fw);
            bw.write(target.getSource());
            bw.close();
            fw.close();
        } catch (IOException e) {
            logger.error("Failed to store object in repository.", e);
        }
        return contain;
    }

}
