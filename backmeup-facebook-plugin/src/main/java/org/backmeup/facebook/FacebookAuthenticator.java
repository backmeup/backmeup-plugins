package org.backmeup.facebook;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.Map;

import org.backmeup.model.exceptions.PluginException;
import org.backmeup.plugin.spi.OAuthBasedAuthorizable;
import org.backmeup.plugin.util.PluginUtils;

import com.restfb.DefaultFacebookClient;
import com.restfb.FacebookClient;
import com.restfb.exception.FacebookException;
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
        return AuthorizationType.OAuth;
    }

    @Override
    public String createRedirectURL(Map<String, String> inputProperties, String callback) {
        inputProperties.put(FacebookHelper.PROPERTY_CALLBACK_URL, callback);

        return "https://www.facebook.com/dialog/oauth?client_id=" + FacebookHelper.getAppKey() + "&redirect_uri=" + callback + "&scope="
                + "user_birthday,user_photos,read_stream,user_about_me,user_activities,"
                + "user_education_history,user_events,user_groups,user_hometown,user_interests,"
                + "user_likes,user_location,user_notes,user_questions,user_relationships," + "user_relationship_details,user_religion_politics,user_status,"
                + "user_subscriptions,user_videos,user_website,user_work_history,email,"
                + "read_friendlists,friends_photos, friends_about_me, friends_activities, friends_birthday, " + "friends_education_history, friends_hometown, "
                + "friends_interests, friends_likes, friends_location, friends_relationships, "
                + "friends_religion_politics, friends_website, friends_work_history, " + "manage_pages";
    }

    @Override
    public String authorize(Map<String, String> authData) {
        String accessToken = authData.get(FacebookHelper.PROPERTY_ACCESS_TOKEN);

        try {
            if (accessToken == null) {
                accessToken = this.retrieveAccessToken(authData);
                authData.put(FacebookHelper.PROPERTY_ACCESS_TOKEN, accessToken);
            }

            FacebookClient client = new DefaultFacebookClient(accessToken);
            User user = client.fetchObject("me", User.class);
            return user.getName();

        } catch (IOException | FacebookException e) {
            throw new PluginException(FacebookDescriptor.ID, AUTHENTICATION_ERROR, e);
        }
    }

    private String retrieveAccessToken(Map<String, String> inputProperties) throws IOException {
        String code = null;
        try {
            code = PluginUtils.splitQuery(inputProperties.get(OAuthBasedAuthorizable.QUERY_PARAM_PROPERTY)).getParameter("code");
        } catch(NullPointerException e) {
            throw new PluginException(FacebookDescriptor.ID, "cannot parse oAuth response", e);
        }
        String callback = inputProperties.get(FacebookHelper.PROPERTY_CALLBACK_URL);
        StringBuilder content = new StringBuilder();

        try {
            URL url = new URL("https://graph.facebook.com/oauth/access_token?" + "client_id=" + FacebookHelper.getAppKey() + "&redirect_uri="
                    + callback + "&client_secret=" + FacebookHelper.getAppSecret() + "&code=" + code);

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
    }
}
