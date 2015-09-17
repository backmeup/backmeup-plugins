package org.backmeup.zip.utils;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.backmeup.model.exceptions.PluginException;
import org.backmeup.zip.constants.Constants;

/**
 * This Util class constructs loads properties form the file storage.properties
 * found within this bundles jar file.
 * 
 */
public class PropertiesUtil {
    public static final String PROPERTIES_FILE = "zip.properties";

    private static PropertiesUtil propertiesUtil;
    private static Properties properties;

    private PropertiesUtil() {

    }

    public static PropertiesUtil getInstance() {
        if (propertiesUtil == null) {
            propertiesUtil = new PropertiesUtil();
        }

        if (properties == null) {
            propertiesUtil.loadProperties();
        }
        return propertiesUtil;
    }

    private void loadProperties() {
        InputStream is = getClass().getClassLoader().getResourceAsStream(
                PROPERTIES_FILE);
        if (is == null) {
            throw new PluginException(
                    Constants.BACKMEUP_ZIP_ID,
                    "Fatal error: cannot find zip.properties within jar-file!");
        }

        properties = new Properties();
        try {
            properties.load(is);
        } catch (IOException e) {
            throw new PluginException(
                    Constants.BACKMEUP_ZIP_ID,
                    "Fatal error: could not load zip.properties: "
                            + e.getMessage(), e);
        } finally {
            try {
                if (is != null) {
                    is.close();
                }
            } catch (Exception e) {
                throw new PluginException(
                        Constants.BACKMEUP_ZIP_ID,
                        "Error: could not close zip.properties: "
                                + e.getMessage(), e);
            }
        }
    }

    public String getProperty(final String key) {
        if (key != null && !key.isEmpty()) {
            return properties.getProperty(key);
        }
        return null;
    }
}
