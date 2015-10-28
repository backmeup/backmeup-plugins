package org.backmeup.facebook;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * Facebook plugin configuration and properties.
 * 
 * @author Wolfgang Eibner
 *
 */
public class FacebookHelper {
    public static final String PROPERTY_APP_SECRET = "app.secret";
    public static final String PROPERTY_APP_KEY = "app.key";
    public static final String PROPERTY_MAX_PHOTOS_PER_ALBUM = "debug.max_photos_per_album";
    public static final String PROPERTY_SKIP_ALBUMS = "debug.skip_albums";
    public static final String PROPERTY_VERBOSE = "debug.verbose";
    public static final String PROPERTY_DATA_DIR = "output.data_dir";
    public static final String PROPERTY_HTML_DIR = "output.html_dir";
    public static final String PROPERTY_FB_TMP_DIR_ROOT = "fb.tempDir";

    private static final Map<String, String> DEFAULTS = new HashMap<>();
    static {
        DEFAULTS.put(PROPERTY_MAX_PHOTOS_PER_ALBUM, "-1");
        DEFAULTS.put(PROPERTY_SKIP_ALBUMS, "");
        DEFAULTS.put(PROPERTY_VERBOSE, Boolean.FALSE.toString());

        DEFAULTS.put(PROPERTY_DATA_DIR, "xmldata");
        DEFAULTS.put(PROPERTY_HTML_DIR, "html");
        DEFAULTS.put(PROPERTY_FB_TMP_DIR_ROOT, System.getProperty("java.io.tmpdir"));
    }

    /* only used at runtime/with service */
    public static final String RT_PROPERTY_CALLBACK_URL = "fbcallback";
    public static final String RT_PROPERTY_ACCESS_TOKEN = "fbaccesstoken";

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
        String ret = PROPERTIES.getProperty(key);
        if (ret != null) {
            return ret;
        }
        return DEFAULTS.get(key);
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

    public static long getDebugMaxPics() {
        try {
            return Long.parseLong(FacebookHelper.getProperty(FacebookHelper.PROPERTY_MAX_PHOTOS_PER_ALBUM, "-1"));
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    public static List<String> getDebugSkipAlbums() {
        return Arrays.asList(FacebookHelper.getProperty(FacebookHelper.PROPERTY_SKIP_ALBUMS, "").split(";"));
    }

    public static boolean isDebugVerbose() {
        return Boolean.parseBoolean(getProperty(PROPERTY_VERBOSE));
    }
}
