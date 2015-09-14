package org.backmeup.dropbox;

import java.util.Map;

import org.backmeup.model.exceptions.PluginException;
import org.backmeup.plugin.api.OAuthBasedAuthorizable;

import com.dropbox.client2.DropboxAPI;
import com.dropbox.client2.DropboxAPI.Entry;
import com.dropbox.client2.exception.DropboxException;
import com.dropbox.client2.session.AccessTokenPair;
import com.dropbox.client2.session.RequestTokenPair;
import com.dropbox.client2.session.WebAuthSession;
import com.dropbox.client2.session.WebAuthSession.WebAuthInfo;

/**
 * The DropboxAuthenticator creates a redirect URL based on the
 * Dropbox API and stores all information needed for further
 * authentication/authorization within the inputProperties parameter.
 * 
 * @author fschoeppl
 *
 */
public class DropboxAuthenticator implements OAuthBasedAuthorizable {

    @Override
    public AuthorizationType getAuthType() {
        return AuthorizationType.OAUTH;
    }

    @Override
    public String createRedirectURL(Map<String, String> authData, String callback) {
        try {
            WebAuthInfo authInfo = DropboxHelper.getInstance().getWebAuthSession().getAuthInfo();	

            RequestTokenPair requestToken = authInfo.requestTokenPair;
            authData.put(DropboxHelper.PROPERTY_REQUEST_TOKEN, requestToken.key);
            authData.put(DropboxHelper.PROPERTY_REQUEST_SECRET, requestToken.secret);

            return authInfo.url + "&oauth_callback=" + callback;
        } catch (DropboxException e) {
            throw new PluginException(DropboxDescriptor.DROPBOX_ID, "An error occurred while retrieving authentication information", e);
        }
    }

    @Override
    public String authorize(Map<String, String> authData) {
        try {
            WebAuthSession session = DropboxHelper.getInstance().getWebAuthSession();

            // If we don't already have an access token (e.g. first time authorize is called),
            // use request token to obtain an access token
            if(authData.get(DropboxHelper.PROPERTY_ACCESS_TOKEN) == null) {

                // Check if request token pair is set in input properties
                String token = authData.get(DropboxHelper.PROPERTY_REQUEST_TOKEN);
                if("".equals(token)){
                    throw new PluginException(DropboxDescriptor.DROPBOX_ID, "Request token is not set");
                }

                String secret = authData.get(DropboxHelper.PROPERTY_REQUEST_SECRET);
                if("".equals(secret)) {
                    throw new PluginException(DropboxDescriptor.DROPBOX_ID, "Request secret is not set");
                }

                // Use the request token pair to obtain an access token
                RequestTokenPair requestToken = new RequestTokenPair(token, secret);
                session.retrieveWebAccessToken(requestToken);

                // Retrieve access tokens and store them in properties for future use
                AccessTokenPair accessToken = session.getAccessTokenPair();
                authData.put(DropboxHelper.PROPERTY_ACCESS_TOKEN, accessToken.key);
                authData.put(DropboxHelper.PROPERTY_ACCESS_SECRET, accessToken.secret);
            }

            // Now we have an access token.
            // Check if authentication is successful
            DropboxAPI<WebAuthSession> api = DropboxHelper.getInstance().getApi(authData);
            if (!api.getSession().isLinked()) {
                throw new PluginException(DropboxDescriptor.DROPBOX_ID, "An error occurred during authorization");
            }

            // Crawl metadata via the API so that we can be sure that the API is working as expected.
            // Note: This does not ensure that all API calls work.
            Entry entry = api.metadata("/", 100, null, true, null);
            entry.contents.size();

            return DropboxHelper.getInstance().getApi(authData).accountInfo().displayName;

        } catch (DropboxException e) {
            throw new PluginException(DropboxDescriptor.DROPBOX_ID, "An error occurred during post authorization", e);
        }
    }
}
