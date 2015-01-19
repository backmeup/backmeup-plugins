package org.backmeup.facebook;

import java.io.IOException;
import java.util.Properties;

/**
 * offers application key and secret 
 * 
 * @author Wplfgang Eibner
 *
 */
public class FacebookHelper {
	public static final String PROPERTY_TOKEN = "token";
	public static final String PROPERTY_TOKEN_DESC = "The Facebook access token";
	
    private static final Properties PROPERTIES = new Properties();
    private static final String PROPERTYFILE = "facebook.properties";

    private FacebookHelper() {
    }

    static {
        try {
            ClassLoader loader = Thread.currentThread().getContextClassLoader();
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
		return getProperty("app.key");
	}
	
	public static String getAppSecret() {
	    return getProperty("app.secret");
	}

}
