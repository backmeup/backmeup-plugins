package org.backmeup.facebook;

import java.io.IOException;
import java.util.Properties;

/**
 * offers application key and secret
 * 
 * @author Wolfgang Eibner
 *
 */
public class FacebookHelper {
    private static final String PROPERTY_APP_SECRET = "app.secret";
    private static final String PROPERTY_APP_KEY = "app.key";

    public static final String PROPERTY_CALLBACK_URL = "fbcallback";
    public static final String PROPERTY_ACCESS_TOKEN = "fbaccesstoken";

    private static final Properties PROPERTIES = new Properties();
    private static final String PROPERTYFILE = "facebook.properties";

    private FacebookHelper() {
    }

    static {
        try {
            ClassLoader loader = FacebookHelper.class.getClassLoader();
            if (loader.getResourceAsStream(PROPERTYFILE) != null) {
                PROPERTIES.load(loader.getResourceAsStream(PROPERTYFILE));
            } else {
                throw new IOException("unable to load properties file: " + PROPERTYFILE);
            }
        } catch (IOException e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    public static String getProperty(String key) {
        return PROPERTIES.getProperty(key);
    }

    public static String getProperty(String key, String defaultValue) {
        return PROPERTIES.getProperty(key, defaultValue);
    }

    public static String getAppKey() {
        return getProperty(PROPERTY_APP_KEY);
    }

    public static String getAppSecret() {
        return getProperty(PROPERTY_APP_SECRET);
    }

}
