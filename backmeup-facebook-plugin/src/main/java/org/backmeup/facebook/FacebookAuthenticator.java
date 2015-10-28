package org.backmeup.facebook;

import java.io.UnsupportedEncodingException;
import java.util.Map;

import org.backmeup.model.exceptions.PluginException;
import org.backmeup.plugin.api.OAuthBasedAuthorizable;
import org.backmeup.plugin.api.OAuthBasedConstants;
import org.backmeup.plugin.api.util.PluginUtils;

import com.restfb.DefaultFacebookClient;
import com.restfb.FacebookClient;
import com.restfb.FacebookClient.AccessToken;
import com.restfb.Parameter;
import com.restfb.Version;
import com.restfb.exception.FacebookException;
import com.restfb.scope.ExtendedPermissions;
import com.restfb.scope.ScopeBuilder;
import com.restfb.scope.UserDataPermissions;
import com.restfb.types.User;

/**
 * The FacebookAuthenticator creates a redirect URL based on the app
 * informations and stores Accesstoken in the inputProperties
 * 
 * @author Wolfgang Eibner
 * 
 */
public class FacebookAuthenticator implements OAuthBasedAuthorizable {

    private static final String AUTHENTICATION_ERROR = "An error occurred while retrieving authentication information";

    @Override
    public AuthorizationType getAuthType() {
        return AuthorizationType.OAUTH;
    }

    @Override
    public String createRedirectURL(Map<String, String> inputProperties, String callback) {
        inputProperties.put(FacebookHelper.RT_PROPERTY_CALLBACK_URL, callback);

        ScopeBuilder scopeBuilder = new ScopeBuilder();
        for (UserDataPermissions permission : UserDataPermissions.values()) {
            scopeBuilder.addPermission(permission);
        }
        scopeBuilder.addPermission(ExtendedPermissions.MANAGE_PAGES);

        FacebookClient client = new DefaultFacebookClient(Version.VERSION_2_3);
        return client.getLoginDialogUrl(FacebookHelper.getAppKey(), callback, scopeBuilder);
    }

    @Override
    public String authorize(Map<String, String> authData) {
        String accessToken = authData.get(FacebookHelper.RT_PROPERTY_ACCESS_TOKEN);

        try {
            if (accessToken == null) {
                accessToken = this.retrieveAccessToken(authData);
                authData.put(FacebookHelper.RT_PROPERTY_ACCESS_TOKEN, accessToken);
            }

            FacebookClient client = new DefaultFacebookClient(accessToken, Version.VERSION_2_3);
            User user = client.fetchObject("me", User.class, Parameter.with("fields", "name"));
            return user.getName();

        } catch (FacebookException e) {
            throw new PluginException(FacebookDescriptor.ID, AUTHENTICATION_ERROR, e);
        }
    }

    private String retrieveAccessToken(Map<String, String> inputProperties) {
        String verificationCode = null;
        try {
            String queryParameters = inputProperties.get(OAuthBasedConstants.QUERY_PARAM_PROPERTY);
            if (queryParameters == null) {
                throw new PluginException(FacebookDescriptor.ID,
                        "cannot parse oAuth response: no query parameters found");
            }
            verificationCode = PluginUtils.splitQuery(queryParameters).getParameter("code");
            if (verificationCode == null) {
                throw new PluginException(FacebookDescriptor.ID,
                        "cannot parse oAuth response: no verifiction code found");
            }
        } catch (UnsupportedEncodingException e) {
            throw new PluginException(FacebookDescriptor.ID, "cannot parse oAuth response", e);
        }

        try {
            FacebookClient client = new DefaultFacebookClient(Version.VERSION_2_3);
            AccessToken shortLiveToken = client.obtainUserAccessToken(FacebookHelper.getAppKey(),
                    FacebookHelper.getAppSecret(), inputProperties.get(FacebookHelper.RT_PROPERTY_CALLBACK_URL),
                    verificationCode);
            //TODO: maybe no need to extend access token
            AccessToken longLiveToken = client.obtainExtendedAccessToken(FacebookHelper.getAppKey(),
                    FacebookHelper.getAppSecret(), shortLiveToken.getAccessToken());

            return longLiveToken.getAccessToken();
        } catch (FacebookException e) {
            throw new PluginException(FacebookDescriptor.ID, AUTHENTICATION_ERROR, e);
        }
    }
}