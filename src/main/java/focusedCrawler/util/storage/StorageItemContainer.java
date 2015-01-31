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



/**

 * Title:

 * Description:

 * Copyright:    Copyright (c) 2001

 * Company:      Radix

 * @author

 * @version 1.0

 */



public class StorageItemContainer extends StorageDefault {



    private int blockSize;

    private StorageItem[] itens;

    private int counter;

    private StorageItemFactory factory;



    public StorageItemContainer() {

        counter = 0;

    }



    public StorageItemContainer(int newBlockSize, StorageItemFactory newFactory) throws StorageItemFactoryException {

        super();

        setBlockSize(newBlockSize);

        setFactory(newFactory);

    }



    public int getBlockSize() {

        return blockSize;

    }



    public void setBlockSize(int newBlockSize) {

        blockSize = newBlockSize;

    }



    public StorageItemFactory getFactory() {

        return factory;

    }



    public void setFactory(StorageItemFactory newFactory) throws StorageItemFactoryException {

        factory = newFactory;

        itens = getFactory().produce(getBlockSize());

    }



    public Object insert(Object obj) {

        ((StorageItem) obj).copy(itens[counter]);

        counter++;

        return null;

    }



    public Object[] selectArray(Object[] objs) throws StorageException {

        if (counter == itens.length) {

            counter = 0;

            return itens;

        } //if

        else {

            int temp = counter;

            counter = 0;

            try {

                StorageItem[] result = getFactory().produce(temp);

                System.arraycopy(itens,0,result,0,result.length);

                return result;

            } //try

            catch (StorageItemFactoryException error) {

                throw new StorageException(error);

            } //catch

        }

    }

}