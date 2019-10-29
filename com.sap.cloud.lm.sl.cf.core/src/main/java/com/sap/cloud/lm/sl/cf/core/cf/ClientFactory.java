package com.sap.cloud.lm.sl.cf.core.cf;

import org.cloudfoundry.client.lib.CloudControllerClient;
import org.cloudfoundry.client.lib.CloudCredentials;
import org.springframework.security.oauth2.common.OAuth2AccessToken;

import com.sap.cloud.lm.sl.cf.core.util.UserMessageLogger;

public abstract class ClientFactory {

    public CloudControllerClient createClient(OAuth2AccessToken token, UserMessageLogger userMessageLogger) {
        return createClient(createCredentials(token), userMessageLogger);
    }

    public CloudControllerClient createClient(OAuth2AccessToken token, String org, String space, UserMessageLogger userMessageLogger) {
        return createClient(createCredentials(token), org, space, userMessageLogger);
    }

    public CloudControllerClient createClient(OAuth2AccessToken token, String spaceId, UserMessageLogger userMessageLogger) {
        return createClient(createCredentials(token), spaceId, userMessageLogger);
    }

    protected abstract CloudControllerClient createClient(CloudCredentials credentials, UserMessageLogger userMessageLogger);

    protected abstract CloudControllerClient createClient(CloudCredentials credentials, String org, String space,
                                                          UserMessageLogger userMessageLogger);

    protected abstract CloudControllerClient createClient(CloudCredentials credentials, String spaceId,
                                                          UserMessageLogger userMessageLogger);

    private static CloudCredentials createCredentials(OAuth2AccessToken token) {
        boolean refreshable = (token.getRefreshToken() != null);
        return new CloudCredentials(token, refreshable);
    }
}
