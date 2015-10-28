package org.backmeup.zip.utils;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.backmeup.model.exceptions.PluginException;
import org.backmeup.zip.constants.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This Util class constructs loads properties form the file storage.properties
 * found within this bundles jar file.
 * 
 */
public class PropertiesUtil {
    private static final Logger LOGGER = LoggerFactory.getLogger(PropertiesUtil.class);
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
            PropertiesUtil.loadProperties();
        }
        return propertiesUtil;
    }

    private static void loadProperties() {
        InputStream is = PropertiesUtil.class.getClassLoader().getResourceAsStream(
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
            closeQuietly(is);
            
        }
    }

    public String getProperty(final String key) {
        if (key != null && !key.isEmpty()) {
            return properties.getProperty(key);
        }
        return null;
    }
    
    private static void closeQuietly(InputStream resource) {
        try {
            if (resource != null) {
                resource.close();
            }
        } catch (Exception ex) {
            LOGGER.error("", ex);
        }
    }
}
