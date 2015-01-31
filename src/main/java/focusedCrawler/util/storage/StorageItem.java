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



import java.io.DataInputStream;

import java.io.DataOutputStream;

import java.io.IOException;

public interface StorageItem {



    /**

     * Define o status de insercao

     */

    String UNDEFINED_TYPE = new String(new byte[] {0});



    /**

     * Define o status de insercao

     */

    String INSERT_TYPE = "i";



    /**

     * Define o status de atualizacao

     */

    String UPDATE_TYPE = "u";



    /**

     * Define o status de remocao

     */

    String REMOVE_TYPE = "r";



    /**

     * Retorna o status do item de armazenamento

     */

    String getStatus();



    /**

     * Altera o status do item de armazenamento

     */

    void setStatus(String newStatus);



    /**

     * Retorna o codigo da pagina

     */

    int getCode();



    /**

     * Altera o codigo da pagina

     */

    void setCode(int _code);



    /**

     * Retorna se gravará o status do item

     */

    boolean isToWriteStatus();



    /**

     * Altera se gravará o status do item

     */

    void setToWriteStatus(boolean newToWriteStatus);



    /**

     * Retorna se gravará o código do item

     */

    boolean isToWriteCode();



    /**

     * Altera se gravará o código do item

     */

    void setToWriteCode(boolean newToWriteCode);



    /**

     * Escreve a representação binária do item

     */

    int writeObject(DataOutputStream out) throws IOException;



    /**

     * Lê a representação binária do item

     */

    int readObject(DataInputStream in) throws IOException;



    /**

     * Retorna o tamanho da representação em bytes

     */

    int getByteArraySize();



    /**

     * Copia os dados de um item para outro

     */

    void copy(StorageItem destiny);

}