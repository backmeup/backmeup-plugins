package org.backmeup.filegenerator;

import java.util.Properties;

import org.backmeup.plugin.spi.OAuthBased;

public class FilegeneratorAuthenticator implements OAuthBased {

  @Override
  public AuthorizationType getAuthType() {
    return AuthorizationType.OAuth;
  }

  @Override
  public String postAuthorize(Properties inputProperties) {
    // do nothing
    return null;
  }

  @Override
  public String createRedirectURL(Properties inputProperties, String callbackUrl) {
    // return anything
    return "NOT_NEEDED";
  }

}
