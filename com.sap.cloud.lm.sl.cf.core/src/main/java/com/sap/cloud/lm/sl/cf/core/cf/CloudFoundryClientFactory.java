package com.sap.cloud.lm.sl.cf.core.cf;

import java.util.ArrayList;
import java.util.UUID;

import javax.inject.Inject;
import javax.inject.Named;

import org.cloudfoundry.client.lib.CloudControllerClient;
import org.cloudfoundry.client.lib.CloudCredentials;
import org.cloudfoundry.client.lib.domain.CloudSpace;
import org.cloudfoundry.client.lib.oauth2.OAuthClient;
import org.cloudfoundry.client.lib.rest.CloudControllerRestClient;
import org.cloudfoundry.client.lib.rest.CloudControllerRestClientFactory;
import org.cloudfoundry.client.lib.rest.ImmutableCloudControllerRestClientFactory;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.web.client.RestTemplate;

import com.sap.cloud.lm.sl.cf.client.ResilientCloudControllerClient;
import com.sap.cloud.lm.sl.cf.core.util.ApplicationConfiguration;
import com.sap.cloud.lm.sl.cf.core.util.UserMessageLogger;

@Named
public class CloudFoundryClientFactory extends ClientFactory {

    private final ApplicationConfiguration configuration;
    private final CloudControllerRestClientFactory clientFactory;
    private final OAuthClientFactory oAuthClientFactory;
    private final OperationsPerformanceMonitor operationsPerformanceMonitor;

    @Inject
    public CloudFoundryClientFactory(ApplicationConfiguration configuration, OAuthClientFactory oAuthClientFactory,
                                     OperationsPerformanceMonitor operationsPerformanceMonitor) {
        this.clientFactory = ImmutableCloudControllerRestClientFactory.builder()
                                                                      .clientConnectTimeout(configuration.getControllerClientConnectTimeout())
                                                                      .clientConnectionPoolSize(configuration.getControllerClientConnectionPoolSize())
                                                                      .clientThreadPoolSize(configuration.getControllerClientThreadPoolSize())
                                                                      .shouldTrustSelfSignedCertificates(configuration.shouldSkipSslValidation())
                                                                      .build();
        this.configuration = configuration;
        this.oAuthClientFactory = oAuthClientFactory;
        this.operationsPerformanceMonitor = operationsPerformanceMonitor;
    }

    @Override
    protected CloudControllerClient createClient(CloudCredentials credentials, UserMessageLogger userMessageLogger) {
        OAuthClient oAuthClient = oAuthClientFactory.createOAuthClient();
        CloudControllerRestClient controllerClient = clientFactory.createClient(configuration.getControllerUrl(), credentials, null,
                                                                                oAuthClient);
        addTaggingInterceptor(controllerClient.getRestTemplate());
        return createClient(controllerClient, userMessageLogger);
    }

    @Override
    protected CloudControllerClient createClient(CloudCredentials credentials, String org, String space,
                                                 UserMessageLogger userMessageLogger) {
        OAuthClient oAuthClient = oAuthClientFactory.createOAuthClient();
        CloudControllerRestClient controllerClient = clientFactory.createClient(configuration.getControllerUrl(), credentials, org, space,
                                                                                oAuthClient);

        addTaggingInterceptor(controllerClient.getRestTemplate(), org, space);
        return createClient(controllerClient, userMessageLogger);
    }

    @Override
    protected CloudControllerClient createClient(CloudCredentials credentials, String spaceId, UserMessageLogger userMessageLogger) {
        CloudSpace target = computeTarget(credentials, spaceId, userMessageLogger);
        OAuthClient oAuthClient = oAuthClientFactory.createOAuthClient();
        CloudControllerRestClient controllerClient = clientFactory.createClient(configuration.getControllerUrl(), credentials, target,
                                                                                oAuthClient);
        addTaggingInterceptor(controllerClient.getRestTemplate(), target.getOrganization()
                                                                        .getName(),
                              target.getName());
        return createClient(controllerClient, userMessageLogger);
    }

    private CloudControllerClient createClient(CloudControllerRestClient controllerClient, UserMessageLogger userMessageLogger) {
        return userMessageLogger == null ? new ResilientCloudControllerClient(controllerClient)
            : new CloudControllerClientPerformanceLogger(controllerClient, userMessageLogger, operationsPerformanceMonitor);
    }

    private void addTaggingInterceptor(RestTemplate template) {
        addTaggingInterceptor(template, null, null);
    }

    private void addTaggingInterceptor(RestTemplate template, String org, String space) {
        if (template.getInterceptors()
                    .isEmpty()) {
            template.setInterceptors(new ArrayList<>());
        }
        ClientHttpRequestInterceptor requestInterceptor = new TaggingRequestInterceptor(configuration.getVersion(), org, space);
        template.getInterceptors()
                .add(requestInterceptor);
    }

    protected CloudSpace computeTarget(CloudCredentials credentials, String spaceId, UserMessageLogger userMessageLogger) {
        CloudControllerClient clientWithoutTarget = createClient(credentials, userMessageLogger);
        return clientWithoutTarget.getSpace(UUID.fromString(spaceId));
    }
}
