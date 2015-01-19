package org.backmeup.plugin.storage.constants;

public final class Constants {
    public static final String PROP_USERNAME = "username";
    public static final String PROP_USERNAME_LABEL = "Username";
    public static final String PROP_USERNAME_DESC = "Your BackMeUp username";

    public static final String PROP_PASSWORD = "password";
    public static final String PROP_PASSWORD_LABEL = "Password";
    public static final String PROP_PASSWROD_DESC = "Your BackMeUp password";
    
    public static final String PROP_STORAGE_URL = "storage.url";
    public static final String PROP_DOWNLOAD_BASE = "storage.downloadBase";
    
    public static final String ACCESS_TOKEN = "backmeupStorageAccessToken";
    public static final String CONNECTION_STRING = "connectionString";
    
    private Constants() {
        // Utility classes should not have a public constructor
    }
}
