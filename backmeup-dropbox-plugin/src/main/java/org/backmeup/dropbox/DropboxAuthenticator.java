package org.backmeup.dropbox;

import java.util.Properties;

import org.backmeup.model.exceptions.PluginException;
import org.backmeup.model.spi.ValidationExceptionType;
import org.backmeup.plugin.spi.OAuthBasedAuthorizable;

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
		return AuthorizationType.OAuth;
	}

	@Override
	public String createRedirectURL(Properties inputProperties, String callback) {
		WebAuthInfo authInfo;
		try {
			authInfo = DropboxHelper.getInstance().getWebAuthSession().getAuthInfo();		
			RequestTokenPair rtp = authInfo.requestTokenPair;
			inputProperties.setProperty(DropboxHelper.PROPERTY_TOKEN, rtp.key);
			inputProperties.setProperty(DropboxHelper.PROPERTY_SECRET, rtp.secret);
			return authInfo.url + "&oauth_callback=" + callback;
		} catch (DropboxException e) {
			throw new PluginException(DropboxDescriptor.DROPBOX_ID, "An error occurred while retrieving authentication information", e);
		}
	}

	@Override
	public String authorize(Properties inputProperties) {
		// Retrieve auth info from DB
		try {
			WebAuthSession session = DropboxHelper.getInstance().getWebAuthSession();
			
			WebAuthInfo authInfo = session.getAuthInfo();       
	        RequestTokenPair rtp = authInfo.requestTokenPair;
	        //inputProperties.setProperty(DropboxHelper.PROPERTY_TOKEN, rtp.key);
	        inputProperties.setProperty(DropboxHelper.PROPERTY_SECRET, rtp.secret);

			String token = inputProperties.getProperty(DropboxHelper.PROPERTY_TOKEN);
			String secret = inputProperties.getProperty(DropboxHelper.PROPERTY_SECRET);

			session.setAccessTokenPair(new AccessTokenPair(token, secret));
			session.retrieveWebAccessToken(new RequestTokenPair(token, secret));
			// Update access token in DB
			AccessTokenPair atp = session.getAccessTokenPair();
			inputProperties.setProperty(DropboxHelper.PROPERTY_TOKEN, atp.key);
			inputProperties.setProperty(DropboxHelper.PROPERTY_SECRET, atp.secret);
			
			DropboxAPI<WebAuthSession> api = DropboxHelper.getApi(inputProperties);
            if (!api.getSession().isLinked()) {
                throw new PluginException(DropboxDescriptor.DROPBOX_ID, "An error occurred during post authorization");          
            }
            
            // 2. Crawl metadata via the API so that we can be sure that the API is working as expected.
            // Note: This does not ensure that all API calls work.
            Entry entry = api.metadata("/", 100, null, true, null);
            entry.contents.size();
			
			return DropboxHelper.getApi(inputProperties).accountInfo().displayName;
			
		} catch (DropboxException e) {
			throw new PluginException(DropboxDescriptor.DROPBOX_ID, "An error occurred during post authorization", e);
		}
	}

}
