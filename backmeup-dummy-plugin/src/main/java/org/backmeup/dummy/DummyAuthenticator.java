package org.backmeup.dummy;

import java.util.Map;

import org.backmeup.plugin.spi.OAuthBasedAuthorizable;

public class DummyAuthenticator implements OAuthBasedAuthorizable {

    @Override
    public AuthorizationType getAuthType() {
        return AuthorizationType.OAuth;
    }

    @Override
    public String authorize(Map<String, String> authData) {
        // do nothing
        return null;
    }

    @Override
    public String createRedirectURL(Map<String, String> authData, String callbackUrl) {
        // return anything
        return "NOT_NEEDED";
    }
}
