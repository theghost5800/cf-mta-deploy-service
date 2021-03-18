package org.cloudfoundry.multiapps.controller.core.cf;

import org.cloudfoundry.multiapps.controller.core.security.token.TokenService;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.Mock;

class CloudControllerClientProviderTest {

    @Mock
    private ClientFactory clientFactory;
    @Mock
    private TokenService tokenService;

    private CloudControllerClientProvider clientProvider;

    @BeforeEach
    void setUp() {
        // MockitoAnnotations.open(this).close();
    }

}
