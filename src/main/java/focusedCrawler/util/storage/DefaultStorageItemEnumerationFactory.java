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
import focusedCrawler.util.ParameterFile;
import focusedCrawler.util.storage.AbstractStorageFactory;
import focusedCrawler.util.storage.Storage;
import focusedCrawler.util.storage.StorageItemEnumerationFactory;
import focusedCrawler.util.storage.StorageItemEnumerationFactoryException;
public class DefaultStorageItemEnumerationFactory extends AbstractStorageItemEnumerationFactory {
    private StorageItemEnumerationFactory factory;
    private String factoryClassName;
    public DefaultStorageItemEnumerationFactory() {
        super();
    } //StorageFactory
    public DefaultStorageItemEnumerationFactory(ParameterFile config) {
        super(config);
    } //StorageFactory
    public DefaultStorageItemEnumerationFactory(ParameterFile config, String newFactoryClassName) {
        super(config);
        factoryClassName = newFactoryClassName;
    } //StorageFactory
    public StorageItemEnumerationFactory getFactory() {
        return factory;
    } //main
    public void checkFactory() throws StorageItemEnumerationFactoryException {
        if (factory == null) {
            if ((getConfig() == null) && (factoryClassName == null)) {
                throw new StorageItemEnumerationFactoryException("config not set!");
            } //if
            if ((getConfig() != null) && (factoryClassName == null)) {
                factoryClassName = getConfig().getParam("STORAGE_ITEM_ENUMERATION_FACTORY_CLASS_NAME");
            } //if
            try {
                factory = (StorageItemEnumerationFactory) Class.forName(factoryClassName).newInstance();
                factory.setConfig(getConfig());
            } //try
            catch(ClassNotFoundException error) {
                throw new StorageItemEnumerationFactoryException(error);
            } //catch
            catch(IllegalAccessException error) {
                throw new StorageItemEnumerationFactoryException(error);
            } //catch
            catch(InstantiationException error) {
                throw new StorageItemEnumerationFactoryException(error);
            } //catch
        } //if
    }
    public StorageItemEnumeration produce() throws StorageItemEnumerationFactoryException {
        checkFactory();
        return factory.produce();
    }
    
}

