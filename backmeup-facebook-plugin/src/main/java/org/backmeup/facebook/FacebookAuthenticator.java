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
    	String redirectURL = client.getLoginDialogUrl(FacebookHelper.getAppKey(), callback, scopeBuilder);
    	System.out.println("== Facebook callback: "+redirectURL);
    	return redirectURL;
    	
        /*inputProperties.put(FacebookHelper.PROPERTY_CALLBACK_URL, callback);

        return "https://www.facebook.com/dialog/oauth?client_id=" + FacebookHelper.getAppKey() + "&redirect_uri=" + callback + "&scope="
                + "user_birthday,user_photos,read_stream,user_about_me,user_activities,"
                + "user_education_history,user_events,user_groups,user_hometown,user_interests,"
                + "user_likes,user_location,user_notes,user_questions,user_relationships," + "user_relationship_details,user_religion_politics,user_status,"
                + "user_subscriptions,user_videos,user_website,user_work_history,email,"
                + "read_friendlists,friends_photos, friends_about_me, friends_activities, friends_birthday, " + "friends_education_history, friends_hometown, "
                + "friends_interests, friends_likes, friends_location, friends_relationships, "
                + "friends_religion_politics, friends_website, friends_work_history, " + "manage_pages";
        */
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
            System.out.println("== Facebook name: "+user.getName());
            return user.getName();

        } catch (FacebookException e) {
            throw new PluginException(FacebookDescriptor.ID, AUTHENTICATION_ERROR, e);
        }
    }

    private String retrieveAccessToken(Map<String, String> inputProperties) {
        String verificationCode = null;
        try {
        	verificationCode = PluginUtils.splitQuery(inputProperties.get(OAuthBasedConstants.QUERY_PARAM_PROPERTY)).getParameter("code");
        } catch (NullPointerException | UnsupportedEncodingException e) {
            throw new PluginException(FacebookDescriptor.ID, "cannot parse oAuth response", e);
        }
        
        try {
	        FacebookClient client = new DefaultFacebookClient(Version.VERSION_2_3);
	        AccessToken shortLiveToken = client.obtainUserAccessToken(FacebookHelper.getAppKey(), FacebookHelper.getAppSecret(), inputProperties.get(FacebookHelper.RT_PROPERTY_CALLBACK_URL), verificationCode);
	        AccessToken longLiveToken = client.obtainExtendedAccessToken(FacebookHelper.getAppKey(), FacebookHelper.getAppSecret(), shortLiveToken.getAccessToken());
	        
	        System.out.println("== Short Live Token: "+shortLiveToken.getAccessToken());
	        System.out.println("== Short Live Token type: "+shortLiveToken.getTokenType());
	        System.out.println("== Short Live Token exp: "+shortLiveToken.getExpires());
	        System.out.println("== Long Live Token: "+shortLiveToken.getAccessToken());
	        System.out.println("== Long Live Token type: "+shortLiveToken.getTokenType());
	        System.out.println("== Long Live Token exp: "+shortLiveToken.getExpires());
        
	        return longLiveToken.getAccessToken();
        } catch(FacebookException e) {
        	throw new PluginException(FacebookDescriptor.ID, AUTHENTICATION_ERROR, e);
        }
        
        /*
        StringBuilder content = new StringBuilder();

        try {
            URL url = new URL("https://graph.facebook.com/oauth/access_token?" + "client_id=" + FacebookHelper.getAppKey() + "&redirect_uri=" + callback
                    + "&client_secret=" + FacebookHelper.getAppSecret() + "&code=" + code);

            try (BufferedReader reader = new BufferedReader(new InputStreamReader(url.openStream(), Charset.forName("UTF-8")))) {
                int temp;

                while ((temp = reader.read()) != -1) {
                    content.append((char) temp);
                }
            }
        } catch (IOException ex) {
            throw new PluginException(FacebookDescriptor.ID, AUTHENTICATION_ERROR, ex);
        }

        if (content.toString().contains("access_token")) {
            String[] params = content.toString().split("&");
            for (String param : params) {
                String[] kvPair = param.split("=");
                String name = kvPair[0];
                if (name.equals("access_token")) {
                    return kvPair[1];
                }
            }
        }

        throw new PluginException(FacebookDescriptor.ID, AUTHENTICATION_ERROR);
        */
    }
}