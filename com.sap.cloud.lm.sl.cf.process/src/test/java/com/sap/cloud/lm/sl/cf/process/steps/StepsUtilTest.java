package com.sap.cloud.lm.sl.cf.process.steps;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.InputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.apache.commons.io.IOUtils;
import org.cloudfoundry.client.lib.domain.ImmutableUploadToken;
import org.cloudfoundry.client.lib.domain.UploadToken;
import org.flowable.engine.delegate.DelegateExecution;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import com.sap.cloud.lm.sl.cf.client.lib.domain.CloudApplicationExtended;
import com.sap.cloud.lm.sl.cf.client.lib.domain.CloudServiceInstanceExtended;
import com.sap.cloud.lm.sl.cf.client.lib.domain.ImmutableCloudApplicationExtended;
import com.sap.cloud.lm.sl.cf.client.lib.domain.ImmutableCloudServiceInstanceExtended;
import com.sap.cloud.lm.sl.cf.core.model.Phase;
import com.sap.cloud.lm.sl.cf.process.Constants;
import com.sap.cloud.lm.sl.cf.process.mock.MockDelegateExecution;
import com.sap.cloud.lm.sl.cf.process.variables.VariableHandling;
import com.sap.cloud.lm.sl.cf.process.variables.Variables;
import com.sap.cloud.lm.sl.common.SLException;

public class StepsUtilTest {

    private static final String EXAMPLE_USER = "exampleUser";
    private static final String EXAMPLE_MODULE_NAME = "exampleModule";

    protected final DelegateExecution execution = MockDelegateExecution.createSpyInstance();

    @Test
    public void testDetermineCurrentUserWithSetUser() {
        VariableHandling.set(execution, Variables.USER, EXAMPLE_USER);
        String determinedUser = StepsUtil.determineCurrentUser(execution);
        assertEquals(EXAMPLE_USER, determinedUser);
    }

    @Test
    public void testDetermineCurrentUserError() {
        Assertions.assertThrows(SLException.class, () -> StepsUtil.determineCurrentUser(execution));
    }

    @Test
    public void testGetModuleContentAsStream() throws Exception {
        byte[] bytes = "example byte array".getBytes();
        Mockito.when(execution.getVariable(Mockito.eq(constructModuleContentVariable())))
               .thenReturn(bytes);
        InputStream stream = StepsUtil.getModuleContentAsStream(execution, EXAMPLE_MODULE_NAME);
        byte[] readBytes = IOUtils.toByteArray(stream);
        Assertions.assertArrayEquals(bytes, readBytes);
    }

    @Test
    public void testGetModuleContentAsStreamNotFound() {
        Mockito.when(execution.getVariable(Mockito.eq(constructModuleContentVariable())))
               .thenReturn(null);
        Assertions.assertThrows(SLException.class, () -> StepsUtil.getModuleContentAsStream(execution, EXAMPLE_MODULE_NAME));
    }

    private String constructModuleContentVariable() {
        return Constants.VAR_MTA_MODULE_CONTENT_PREFIX + StepsUtilTest.EXAMPLE_MODULE_NAME;
    }

    @Test
    public void testGetServicesToCreateWithCredentials() {
        CloudServiceInstanceExtended service = ImmutableCloudServiceInstanceExtended.builder()
                                                                    .name("my-service")
                                                                    .putCredential("integer-value", 1)
                                                                    .putCredential("double-value", 1.4)
                                                                    .putCredential("string-value", "1")
                                                                    .build();

        VariableHandling.set(execution, Variables.SERVICES_TO_CREATE, Collections.singletonList(service));
        List<CloudServiceInstanceExtended> actualServicesToCreate = VariableHandling.get(execution, Variables.SERVICES_TO_CREATE);

        assertEquals(1, actualServicesToCreate.size());
        assertFalse(actualServicesToCreate.get(0)
                                          .getCredentials()
                                          .isEmpty());
        assertEquals(Integer.class, actualServicesToCreate.get(0)
                                                          .getCredentials()
                                                          .get("integer-value")
                                                          .getClass());
        assertEquals(Double.class, actualServicesToCreate.get(0)
                                                         .getCredentials()
                                                         .get("double-value")
                                                         .getClass());
        assertEquals(String.class, actualServicesToCreate.get(0)
                                                         .getCredentials()
                                                         .get("string-value")
                                                         .getClass());
    }

    @Test
    public void testGetAppsToDeployWithBindingParameters() {
        Map<String, Map<String, Object>> bindingParameters = new HashMap<>();
        Map<String, Object> serviceBindingParameters = new HashMap<>();
        serviceBindingParameters.put("integer-value", 1);
        serviceBindingParameters.put("double-value", 1.4);
        serviceBindingParameters.put("string-value", "1");
        bindingParameters.put("service-1", serviceBindingParameters);

        CloudApplicationExtended application = ImmutableCloudApplicationExtended.builder()
                                                                                .name("my-application")
                                                                                .bindingParameters(bindingParameters)
                                                                                .build();

        VariableHandling.set(execution, Variables.APP_TO_PROCESS, application);
        CloudApplicationExtended actualAppToDeploy = VariableHandling.get(execution, Variables.APP_TO_PROCESS);

        assertFalse(actualAppToDeploy.getBindingParameters()
                                     .isEmpty());
        assertFalse(actualAppToDeploy.getBindingParameters()
                                     .get("service-1")
                                     .isEmpty());
        assertEquals(Integer.class, actualAppToDeploy.getBindingParameters()
                                                     .get("service-1")
                                                     .get("integer-value")
                                                     .getClass());
        assertEquals(Double.class, actualAppToDeploy.getBindingParameters()
                                                    .get("service-1")
                                                    .get("double-value")
                                                    .getClass());
        assertEquals(String.class, actualAppToDeploy.getBindingParameters()
                                                    .get("service-1")
                                                    .get("string-value")
                                                    .getClass());
    }

    @Test
    public void testSetAndGetUploadToken() {
        UploadToken expectedUploadToken = ImmutableUploadToken.builder()
                                                              .packageGuid(UUID.fromString("ab0703c2-1a50-11e9-ab14-d663bd873d93"))
                                                              .build();

        VariableHandling.set(execution, Variables.UPLOAD_TOKEN, expectedUploadToken);
        UploadToken actualUploadToken = VariableHandling.get(execution, Variables.UPLOAD_TOKEN);

        assertEquals(expectedUploadToken.getPackageGuid(), actualUploadToken.getPackageGuid());
    }

    @Test
    public void testSetAndGetPhase() {
        Phase expectedPhase = Phase.UNDEPLOY;
        VariableHandling.set(execution, Variables.PHASE, expectedPhase);
        Phase actualPhase = VariableHandling.get(execution, Variables.PHASE);

        assertEquals(expectedPhase, actualPhase);
    }

    @Test
    public void testShouldVerifyArchiveSignatureSet() {
        VariableHandling.set(execution, Variables.VERIFY_ARCHIVE_SIGNATURE, true);
        Boolean result = VariableHandling.get(execution, Variables.VERIFY_ARCHIVE_SIGNATURE);

        assertTrue(result);
    }

}
