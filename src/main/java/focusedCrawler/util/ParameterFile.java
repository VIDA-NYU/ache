package focusedCrawler.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Vector;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.esotericsoftware.yamlbeans.YamlException;
import com.esotericsoftware.yamlbeans.YamlReader;

public class ParameterFile {

    private static final Logger logger = LoggerFactory.getLogger(ParameterFile.class);

    Map values;
    Map  crawler, linkStorage, formStorage, backlinks, target_storage,model;

    public ParameterFile(File file) throws FileNotFoundException, YamlException {
        // parse this path and store into a map.
        YamlReader yamlReader = new YamlReader(new FileReader(file));
        Object object = yamlReader.read();
        values = (Map) object;
        crawler = (Map) values.get("crawler");
        linkStorage = (Map) values.get("link_storage");
        formStorage = (Map) values.get("form_storage");
        backlinks = (Map) values.get("backlink");
        target_storage = (Map) values.get("target_storage");
        model = (Map) values.get("model");
    }

    public ParameterFile(String path) throws FileNotFoundException, YamlException {
        this(new File(path));
    }

    public ParameterFile(String[] paths) {

    }

    public File getCfgFile() {
        return null;
    }

    public String getParam(String param) {
        if (crawler.containsKey(param))
            return (String) crawler.get(param);
        else if (linkStorage.containsKey(param))
            return (String) linkStorage.get(param);
        else if (formStorage.containsKey(param))
            return (String) formStorage.get(param);
        else if (backlinks.containsKey(param))
            return (String) backlinks.get(param);
        else if (target_storage.containsKey(param))
            return (String) target_storage.get(param);
        else if (model.containsKey(param))
            return (String) model.get(param);
        else
            return null;
    }
    
    public String getParamOrDefault(String paramKey, String defaultValue) {
        String paramValue = getParam(paramKey);
        return paramValue == null ? defaultValue : paramValue;
    }
    

    public String[] getParam(String param, String token) {
        String value = getParam(param);
        if (value != null)
            return value.split(token);
        else
            return null;
    }

    public boolean getParamBoolean(String param) {
        return getParamBooleanOrDefault(param, false);
    }

    public boolean getParamBooleanOrDefault(String param, boolean defaultValue) {

        String value = getParam(param);
        if (value != null) {
            return Boolean.valueOf(value);
        } else {
            logger.warn(String.format("Valid boolean value not found for config key " + param + "."
                    + " Using default value: " + defaultValue));
            return defaultValue;
        }
    }

    public float getParamFloat(String param) {
        String value = getParam(param);
        if (value == null) {
            logger.warn("ParameterFile: getParamFloat WARNING " + param + " == null");
            return 0;
        }
        return Float.parseFloat(value);
    }

    public int getParamInt(String param) {
        return getParamIntOrDefault(param, 0);
    }

    public int getParamIntOrDefault(String paramKey, int defaultValue) {
        try {
            String value = getParam(paramKey);
            if (value != null) {
                return Integer.parseInt(value);
            }
        } catch (NumberFormatException e) {
        }
        logger.warn(String.format("Valid integer value not found for config key %s."
                + " Using default value: %d", paramKey, defaultValue));
        return defaultValue;
    }

    public long getParamLong(String param) {
        return getParamLongOrDefault(param, 0);
    }

    public long getParamLongOrDefault(String configKey, long defaultValue) {
        try {
            String value = getParam(configKey);
            if (value != null) {
                return Long.parseLong(value);
            }
        } catch (Exception e) {
        }
        logger.warn(String.format("Valid long value not found for config key %s."
                + " Using default value: %d", configKey, defaultValue));
        return defaultValue;
    }

    public Iterator getParameters() {
        HashMap values = new HashMap();
        
        values.putAll(crawler);
        values.putAll(linkStorage);
        values.putAll(formStorage);
        values.putAll(backlinks);
        return values.keySet().iterator();
    }

    public void listParams() {
        Iterator params = getParameters();
        while (params.hasNext()) {
            String nextParam = (String) params.next();
            logger.debug("Parameter: " + nextParam + " : " + this.getParam(nextParam));

        }
    }
    
    public void putParam(String key, String value) {
        linkStorage.put(key, value);
    }
    
    static public String[] getSeeds(String seedFile){
        ArrayList<String> urls = new ArrayList<String>();
        try{
            File file = new File(seedFile);
            FileReader fileReader = new FileReader(file);
            BufferedReader bufferedReader = new BufferedReader(fileReader);
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                line = line.trim();
                if(!line.isEmpty()) {
                    urls.add(line);
                }
            }
            fileReader.close();
            return urls.toArray(new String[urls.size()]);
        }
        catch(Exception e){
            logger.error("Error while reading seed list", e);
            return null;
        }
    }

    // public String[]

}
