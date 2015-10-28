package org.backmeup.mail.test;

import java.util.ArrayList;

import org.backmeup.model.dto.AuthDataDTO;
import org.backmeup.model.dto.PluginProfileDTO;
import org.backmeup.model.spi.PluginDescribable.PluginType;

public final class TestDataManager {
    private TestDataManager() {
        // Utility classes should not have public constructor
    }

    public static PluginProfileDTO getProfileEmail() {
        String pluginId = "org.backmeup.mail";
        PluginType profileType = PluginType.Source;
        AuthDataDTO authData = getAuthDataEmail();

        PluginProfileDTO pluginProfile = new PluginProfileDTO();
        pluginProfile.setPluginId(pluginId);
        pluginProfile.setProfileType(profileType);
        pluginProfile.setAuthData(authData);
        pluginProfile.setOptions(new ArrayList<String>());

        return pluginProfile;
    }

    public static AuthDataDTO getAuthDataEmail() {
        String authName = "EmailWork";

        AuthDataDTO authData = new AuthDataDTO();
        authData.setName(authName);
        authData.addProperty("Username", "max");
        authData.addProperty("Password", "password123");
        authData.addProperty("Type", "imap");
        authData.addProperty("Host", "localhost");
        authData.addProperty("Port", "3143");
        authData.addProperty("SSL", "false");

        return authData;
    }

}
