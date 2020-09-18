package org.cloudfoundry.multiapps.controller.process.steps;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.Map;
import java.util.stream.Stream;

import org.cloudfoundry.client.lib.domain.ImmutableCloudApplication;
import org.cloudfoundry.multiapps.controller.client.lib.domain.CloudApplicationExtended;
import org.cloudfoundry.multiapps.controller.client.lib.domain.ImmutableCloudApplicationExtended;
import org.cloudfoundry.multiapps.controller.persistence.services.FileStorageException;
import org.cloudfoundry.multiapps.controller.process.util.ServiceBindingParametersGetter;
import org.cloudfoundry.multiapps.controller.process.variables.Variables;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;

class DetermineServiceBindUnbindStepTest extends SyncFlowableStepTest<DetermineServiceBindUnbindStep> {

    private static final String APP_NAME = "test_application";
    private static final String SERVICE_NAME = "test_service";

    @Mock
    private ServiceBindingParametersGetter serviceBindingParametersGetter;

    static Stream<Arguments> testDetermineServiceBindUnbind() {
        return Stream.of(
                         // (1) Service binding exist but service is no more part of the MTA
                         Arguments.of(false, false, true, true, false),
                         // (2) Service binding exist, service is no more part of the MTA and keepExistingBindings strategy is set to true
                         Arguments.of(false, true, true, false, false),
                         // (3) Service is part from MTA and binding doesn't exist
                         Arguments.of(true, false, false, false, true),
                         // (4) Service is part from MTA, it is already binded and existing parameters match to MTA parameters
                         Arguments.of(true, false, true, false, false));
    }

    @ParameterizedTest
    @MethodSource
    void testDetermineServiceBindUnbind(boolean servicePartFromMta, boolean keepExistingBinding, boolean serviceBindingExist,
                                        boolean expectedUnbindValue, boolean expectedBindValue)
        throws FileStorageException {
        CloudApplicationExtended application = buildCloudApplicationExtended(servicePartFromMta, keepExistingBinding);
        prepareContext(application);
        prepareClient(application, serviceBindingExist);
        prepareServiceBindingParametersGetter(Collections.emptyMap(), Collections.emptyMap());

        step.execute(execution);

        assertStepFinishedSuccessfully();
        assertEquals(expectedUnbindValue, context.getVariable(Variables.SHOULD_UNBIND_SERVICE), "Expected unbind value not match");
        assertEquals(expectedBindValue, context.getVariable(Variables.SHOULD_BIND_SERVICE), "Expected bind value not match");
    }

    @Test
    void testRebindServiceDueToChangedBindingParameters() throws FileStorageException {
        CloudApplicationExtended application = buildCloudApplicationExtended(true, false);
        prepareContext(application);
        prepareClient(application, true);
        Map<String, Object> mtaBindingParameters = Map.of("test-config", "test-value");
        prepareServiceBindingParametersGetter(mtaBindingParameters, null);

        step.execute(execution);

        assertStepFinishedSuccessfully();
        assertTrue(context.getVariable(Variables.SHOULD_UNBIND_SERVICE), "Expected unbind value not match");
        assertTrue(context.getVariable(Variables.SHOULD_BIND_SERVICE), "Expected bind value not match");
    }

    private CloudApplicationExtended buildCloudApplicationExtended(boolean servicePartFromMta, boolean keepExistingBinding) {
        CloudApplicationExtended.AttributeUpdateStrategy attributeUpdateStrategy = ImmutableCloudApplicationExtended.AttributeUpdateStrategy.builder()
                                                                                                                                            .shouldKeepExistingServiceBindings(keepExistingBinding)
                                                                                                                                            .build();
        ImmutableCloudApplicationExtended.Builder applicationBuilder = ImmutableCloudApplicationExtended.builder()
                                                                                                        .name(APP_NAME)
                                                                                                        .attributesUpdateStrategy(attributeUpdateStrategy);
        return servicePartFromMta ? applicationBuilder.addService(SERVICE_NAME)
                                                      .build()
            : applicationBuilder.build();
    }

    private void prepareContext(CloudApplicationExtended application) {
        context.setVariable(Variables.APP_TO_PROCESS, application);
        context.setVariable(Variables.SERVICE_TO_UNBIND_BIND, SERVICE_NAME);
    }

    private void prepareClient(CloudApplicationExtended application, boolean serviceBindingExist) {
        if (serviceBindingExist) {
            when(client.getApplication(APP_NAME)).thenReturn(ImmutableCloudApplication.copyOf(application)
                                                                                      .withServices(SERVICE_NAME));
            return;
        }
        when(client.getApplication(APP_NAME)).thenReturn(ImmutableCloudApplication.copyOf(application)
                                                                                  .withServices(Collections.emptyList()));
    }

    private void prepareServiceBindingParametersGetter(Map<String, Object> mtaBindingParameters,
                                                       Map<String, Object> existingServiceBindingParameters)
        throws FileStorageException {
        when(serviceBindingParametersGetter.getServiceBindingParametersFromMta(any(), any())).thenReturn(mtaBindingParameters);
        when(serviceBindingParametersGetter.getServiceBindingParametersFromExistingInstance(any(),
                                                                                            any())).thenReturn(existingServiceBindingParameters);
    }

    @Override
    protected DetermineServiceBindUnbindStep createStep() {
        return new DetermineServiceBindUnbindStep() {
            @Override
            protected ServiceBindingParametersGetter getServiceBindingParametersGetter(ProcessContext context) {
                return serviceBindingParametersGetter;
            }

        };
    }

}
