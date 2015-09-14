package org.backmeup.dropbox;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Properties;

import org.backmeup.model.dto.AuthDataDTO;
import org.backmeup.model.exceptions.InvalidKeyException;
import org.backmeup.model.exceptions.PluginException;

import com.dropbox.client2.DropboxAPI;
import com.dropbox.client2.session.AccessTokenPair;
import com.dropbox.client2.session.AppKeyPair;
import com.dropbox.client2.session.Session.AccessType;
import com.dropbox.client2.session.WebAuthSession;

/**
 * This Helper class constructs and configures the DropboxAPI element. It uses
 * dropbox.properties found within the bundles jar file to retrieve the access
 * token + secret token.
 * 
 * @author fschoeppl
 * 
 */
public class DropboxHelper {
    private static final String PROPERTIES_FILE = "dropbox.properties";

    private static final String PROPERTY_APP_SECRET = "app.secret";
    private static final String PROPERTY_APP_KEY = "app.key";

    public static final String PROPERTY_REQUEST_TOKEN = "dbxreqtoken";
    public static final String PROPERTY_REQUEST_SECRET = "dbxreqsecret";

    public static final String PROPERTY_ACCESS_TOKEN = "dbxaccesstoken";
    public static final String PROPERTY_ACCESS_SECRET = "dbxaccesssecret";

    private static DropboxHelper dropboxHelper;

    private String appKey;
    private String appSecret;

    private DropboxHelper() {

    }

    public static DropboxHelper getInstance() {
        if (dropboxHelper == null) {
            dropboxHelper = new DropboxHelper();
            dropboxHelper.loadProperties();
        }

        return dropboxHelper;
    }

    public WebAuthSession getWebAuthSession() {
        AppKeyPair appKeys = new AppKeyPair(appKey, appSecret);
        return new WebAuthSession(appKeys, AccessType.DROPBOX);
    }

    public DropboxAPI<WebAuthSession> getApi(AuthDataDTO authData) {
        return getApi(authData.getProperties());
    }
    
    public DropboxAPI<WebAuthSession> getApi(Map<String, String> accessData) {
        String token = accessData.get(DropboxHelper.PROPERTY_ACCESS_TOKEN);
        String secret = accessData.get(DropboxHelper.PROPERTY_ACCESS_SECRET);

        if (token == null || secret == null) {
            throw new PluginException(DropboxDescriptor.DROPBOX_ID,
                    "Access token and secret must not be null");
        }

        WebAuthSession session = DropboxHelper.getInstance().getWebAuthSession();
        session.setAccessTokenPair(new AccessTokenPair(token, secret));

        if (!session.isLinked()) {
            throw new InvalidKeyException("org.backmeup.dropbox",
                    "userToken, userSecret", token + ", " + secret,
                    PROPERTIES_FILE);
        }
        return new DropboxAPI<>(session);
    }

    private void loadProperties() {
        InputStream is = getClass().getClassLoader().getResourceAsStream(
                PROPERTIES_FILE);
        if (is == null)
            throw new PluginException(DropboxDescriptor.DROPBOX_ID,
                    "Cannot find dropbox.properties within jar-file!");

        Properties properties = new Properties();
        try {
            properties.load(is);
            is.close();
        } catch (IOException e) {
            throw new PluginException(DropboxDescriptor.DROPBOX_ID,
                    "Could not load dropbox.properties: " + e.getMessage(), e);
        } finally {
            try {
                if (is != null) {
                    is.close();
                }
            } catch (Exception e) {
                throw new PluginException(
                        DropboxDescriptor.DROPBOX_ID,
                        "Could not close dropbox.properties: " + e.getMessage(),
                        e);
            }
        }

        appKey = properties.getProperty(PROPERTY_APP_KEY);
        appSecret = properties.getProperty(PROPERTY_APP_SECRET);
    }
}
