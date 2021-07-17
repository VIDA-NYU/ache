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
package achecrawler.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.SimpleTimeZone;
import java.util.StringTokenizer;
import java.util.TimeZone;
import java.util.Vector;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SuppressWarnings({ "unchecked", "rawtypes" })
public class ParameterFile {

    private static final Logger logger = LoggerFactory.getLogger(ParameterFile.class);

    protected static final String PRELOAD_PREFIX = "${";
    protected static final String PRELOAD_SUFIX = "}";
    protected static final String PARENT_FILE = "..";
    public static final int FILE = 0;
    public static final int STRING = FILE + 1;
    public static final int URL = STRING + 1;

    static {
        TimeZone.setDefault(new SimpleTimeZone((TimeZone.getDefault()).getRawOffset(), (TimeZone.getDefault()).getID()));
    }

    private int tipo = FILE;

    private File cfg_file;

    private String content;

    private URL cfg_url;

    private HttpURLConnection url_con;

    private BufferedReader in;

    private Hashtable hash;

    // global data(public use)
    public final Hashtable data = new Hashtable();
    // global static use
    public final static Hashtable staticData = new Hashtable();

    public ParameterFile(String[] args) {
        this(new File(args[0].trim()));
        for (int i = 0; i < args.length; i++) {
            Vector v = new Vector();
            v.addElement(args[i].trim());
            hash.put("" + i, v);
        }
    }

    public ParameterFile(String filename) {
        this(new File(filename));
    }

    public ParameterFile(File file) {
        this(file, new Hashtable());
    }

    public ParameterFile(java.net.URL url) {
        this.tipo = ParameterFile.URL;
        this.cfg_url = url;
        hash = new Hashtable();
        loadHash();
    }

    protected ParameterFile(File cfg_file, Hashtable hash) {
        this.cfg_file = cfg_file;
        this.hash = hash;
        logger.info("CONFIGURATION FILE = " + cfg_file);
        loadHash();
    }

    static public List<String> getSeeds(String seedFile) {
        ArrayList<String> urls = new ArrayList<String>();
        try {
            File file = new File(seedFile);
            FileReader fileReader = new FileReader(file);
            BufferedReader bufferedReader = new BufferedReader(fileReader);
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                line = line.trim();
                if (!line.isEmpty()) {
                    urls.add(line);
                }
            }
            fileReader.close();
            return urls;
        } catch (Exception e) {
            logger.error("Error while reading seed list", e);
            return null;
        }
    }

    public File getCfgFile() {
        return this.cfg_file;
    }

    public Iterator getParameters() {
        if (hash != null) {
            return hash.keySet().iterator();
        } // if
        else {
            return null;
        } // else
    } // getParams

    public String getParam(String param, int posicao) {
        String valor = null;
        if (hash == null) {
            loadHash();
        }
        Vector data = (Vector) hash.get(param);
        if (data != null) {
            if (posicao == -1) {
                for (int i = 0; i < data.size(); i++) {
                    if (i == 0) {
                        valor = transformaToken((String) data.elementAt(i), hash);
                    } else {
                        valor += transformaToken(" " + (String) data.elementAt(i), hash);
                    }
                }
            } else {
                if (posicao >= 0 && posicao < data.size()) {
                    valor = transformaToken((String) data.elementAt(posicao), hash);
                }
            }
        }
        return (valor != null ? valor.trim() : null);
    }

    public String getParam(String param) {
        return getParam(param, -1);
    }

    public String getParamOrDefault(String paramKey, String defaultValue) {
        String paramValue = getParam(paramKey);
        return paramValue == null ? defaultValue : paramValue;
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
        logger.warn(String.format("Valid integer value not found for config key %s." + " Using default value: %d",
                paramKey, defaultValue));
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
        logger.warn(String.format("Valid long value not found for config key %s." + " Using default value: %d",
                configKey, defaultValue));
        return defaultValue;
    }

    public boolean getParamBoolean(String param) {
        return getParamBooleanOrDefault(param, false);
    }

    public boolean getParamBooleanOrDefault(String param, boolean defaultValue) {
        try {
            String value = getParam(param);
            if (value != null) {
                return Boolean.valueOf(value).booleanValue();
            }
        } catch (Exception e) {
        }
        logger.warn(String.format("Valid boolean value not found for config key %s." + " Using default value: %b",
                param, defaultValue));
        return defaultValue;
    }

    public float getParamFloat(String param) {
        String value = getParam(param);
        if (value == null) {
            return 0;
        }
        return Float.parseFloat(value);
    }

    public double getParamDouble(String param) {
        String value = getParam(param);
        if (value == null) {
            return 0;
        }
        return Double.parseDouble(value);
    }

    public short getParamShort(String param) {
        String value = getParam(param);
        if (value == null) {
            return 0;
        }
        return Short.parseShort(value);
    }

    public byte getParamByte(String param) {
        String value = getParam(param);
        if (value == null) {
            return 0;
        }
        return Byte.parseByte(value);
    }

    public String[] getParam(String param, String tokens) {
        StringTokenizer st = new StringTokenizer(getParam(param), tokens);
        String[] r = new String[st.countTokens()];
        int i = 0;
        while (st.hasMoreTokens()) {
            r[i] = st.nextToken();
            i++;
        }
        return r;
    }

    private String transformaToken(String token, Hashtable hash) {
        String resultado = token;
        while (resultado != null && resultado.indexOf(PRELOAD_PREFIX) >= 0) {
            resultado = toParameter(hash, resultado);
        }
        return resultado;
    }

    protected String toParameter(Hashtable result, String value) {
        String resultado = value;
        String str = "";
        int last = 0;
        int start = value.indexOf(PRELOAD_PREFIX, last);
        int end = value.indexOf(PRELOAD_SUFIX, start);
        if (start >= 0) {
            str += value.substring(0, start);
        }
        while (start >= 0 && end >= 0) {
            String param = value.substring(start + PRELOAD_PREFIX.length(), end);
            String param_value = getParam(param);
            if (param_value == null) {
                logger.warn("The parameter '" + param + "' is missing in your configuration file.");
                return null;
            }
            if (param_value != null) {
                str += param_value;
            }
            last = end + 1;
            start = value.indexOf(PRELOAD_PREFIX, last);
            end = value.indexOf(PRELOAD_SUFIX, start);
            if (last < value.length()) {
                if (start == -1) {
                    str += value.substring(last, value.length());
                } else {
                    str += value.substring(last, start);
                }
            }
        }
        if (last > 0) {
            resultado = str;
        }
        return resultado;
    }

    public String getRecursiveFile(String filename) {
        File load = new File(cfg_file.getParent());
        while (filename.startsWith(PARENT_FILE)) {
            if (load.getParent() != null && filename.length() >= 3) {
                load = new File(load.getParent());
                filename = filename.substring(3, filename.length());
            } else {
                logger.error("Could not find parent file of " + load);
                System.exit(1);
            }
        }
        load = new File(load, filename);
        if (load.exists()) {
            return load.getAbsolutePath();
        } else {
            logger.error("File '" + load + "' not found!");
            System.exit(1);
            return null;
        }
    }

    public void listParams() {
        Enumeration chaves = hash.keys();
        String param = "";
        while (chaves.hasMoreElements()) {
            param = (String) chaves.nextElement();
            logger.debug("Parameter: " + param + " : " + this.getParam(param));
        }
    }

    private synchronized void loadHash() {
        try {
            try {
                if (tipo == STRING) {
                    in = new BufferedReader(new StringReader(content));
                } // if
                else if (tipo == FILE) {
                    in = new BufferedReader(new FileReader(cfg_file));
                } // else
                else if (tipo == URL) {
                    try {
                        url_con = (HttpURLConnection) cfg_url.openConnection();
                    } // try
                    catch (IOException erro) {
                        return;
                    } // catch
                    in = new BufferedReader(new InputStreamReader(url_con.getInputStream()));
                } // else
            } // try
            catch (IOException e) {
                logger.error(e.getMessage(), e);
                return;
            } // catch

            String line;
            while ((line = in.readLine()) != null) {
                StringTokenizer st = new StringTokenizer(line);
                int i = 0;
                String param = null;
                Vector values = new Vector();
                int tokenCounter = 0;
                while (st.hasMoreTokens()) {
                    tokenCounter++;
                    String token = st.nextToken();
                    if (token.equals(".") && tokenCounter == 1) {
                        if (st.hasMoreTokens()) {
                            String arquivo_config = transformaToken(st.nextToken(), hash);
                            if (arquivo_config.startsWith("..")) {
                                arquivo_config = getRecursiveFile(arquivo_config);
                            } else {
                                if (!(arquivo_config.startsWith(File.separator) || arquivo_config.charAt(1) == ':')) {
                                    arquivo_config = new File(cfg_file.getParent(), arquivo_config).toString();
                                }
                            }
                            ParameterFile config = new ParameterFile(new File(arquivo_config), hash);
                            Hashtable hash2 = config.hash;
                            Enumeration keys = hash2.keys();
                            while (keys.hasMoreElements()) {
                                Object key = keys.nextElement();
                                hash.put(key, hash2.get(key));
                            }
                        }
                        continue;
                    }
                    if (token.startsWith("#")) {
                        break;
                    } else if (i == 0) {
                        param = token;
                    } else {
                        values.addElement(token);
                    }
                    i++;
                }
                if (param != null && param.length() > 0)
                    hash.put(param, values);
            }
        } catch (IOException ioe) {
            logger.error("Problem while reading configuration file.", ioe);
        } finally {
            try {
                if (in != null) {
                    in.close();
                } // if
            } catch (IOException erro) {
                logger.error("Problem while closing input.", erro);
            } // catch
            if (tipo == URL) {
                url_con.disconnect();
            } // if
        }

    }
    
}
