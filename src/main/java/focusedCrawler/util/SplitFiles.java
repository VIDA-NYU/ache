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
package focusedCrawler.util;

import java.io.File;
import java.io.InputStream;
import java.io.IOException;
import java.io.FileOutputStream;
import java.io.FileInputStream;
import java.io.OutputStream;
/**
 * <p>Title: </p>
 *
 * <p>Description: </p>
 *
 * <p>Copyright: Copyright (c) 2006</p>
 *
 * <p>Company: </p>
 *
 * @author not attributable
 * @version 1.0
 */
public class SplitFiles {


    public SplitFiles() {

    }

    public void split(File inputDir, int numOfParts, File output) throws
            IOException {
        String[] files = inputDir.list();
        int count = 1;
        int numOfFiles = files.length/numOfParts;
        int countFiles = 0;
        for (int i = 0; i < files.length; i++) {
            if(count < numOfParts && countFiles == numOfFiles){
                count++;
                countFiles = 0;
            }
            copy(new File(inputDir + File.separator + files[i]), new File(output + "_" + count + File.separator + files[i]));
            countFiles++;
        }
    }


    private void copy(File src, File dst) throws IOException {
               InputStream in = new FileInputStream(src);
               OutputStream out = new FileOutputStream(dst);

               // Transfer bytes from in to out
               byte[] buf = new byte[1024];
               int len;
               while ((len = in.read(buf)) > 0) {
                   out.write(buf, 0, len);
               }
               in.close();
               out.close();
        }
    public static void main(String[] args) {
        SplitFiles splitfiles = new SplitFiles();
        try {
            splitfiles.split(new File(args[0]), new Integer(args[1]).intValue(),
                             new File(args[2]));
        } catch (NumberFormatException ex) {
            ex.printStackTrace();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
}
