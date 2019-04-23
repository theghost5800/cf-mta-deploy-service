package com.sap.cloud.lm.sl.cf.process.steps;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.cloudfoundry.client.lib.domain.CloudApplication;
import org.cloudfoundry.client.lib.domain.CloudRoute;
import org.cloudfoundry.client.lib.domain.CloudTask;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.mockito.Mock;
import org.mockito.Mockito;

import com.sap.cloud.lm.sl.cf.client.lib.domain.CloudInfoExtended;
import com.sap.cloud.lm.sl.cf.core.cf.clients.ApplicationRoutesGetter;
import com.sap.cloud.lm.sl.cf.process.Constants;
import com.sap.cloud.lm.sl.common.util.JsonUtil;
import com.sap.cloud.lm.sl.common.util.Pair;
import com.sap.cloud.lm.sl.common.util.TestUtil;

@RunWith(Parameterized.class)
public abstract class UndeployAppStepTest extends SyncFlowableStepTest<UndeployAppStep> {

    @Mock
    protected ApplicationRoutesGetter applicationRoutesGetter;

    protected StepInput stepInput;
    protected StepOutput stepOutput;

    @Parameters
    public static Iterable<Object[]> getParameters() {
        return Arrays.asList(new Object[][] {
// @formatter:off
          // (0) There are applications to undeploy (host based routing (XSA)):
          {
              "undeploy-apps-step-input-00.json", "undeploy-apps-step-output-00.json",
          },
          // (1) There are applications to undeploy (port based routing (XSA)):
          {
              "undeploy-apps-step-input-01.json", "undeploy-apps-step-output-01.json",
          },
          // (2) No applications to undeploy:
          {
              "undeploy-apps-step-input-02.json", "undeploy-apps-step-output-02.json",
          },
          // (3) There are two routes that should be deleted, but one of them is bound to another application:
          {
              "undeploy-apps-step-input-03.json", "undeploy-apps-step-output-03.json",
          },
          // (4) There are running one-off tasks to cancel:
          {
              "undeploy-apps-step-input-04.json", "undeploy-apps-step-output-04.json",
          },
          // (5) There are not found routes matching app uri:
          {
              "undeploy-apps-step-input-05.json", "undeploy-apps-step-output-05.json",
          },          
          // (6) There are tcp/tcps routes to undeploy (host based routing (XSA)):
          {
              "undeploy-apps-step-input-06.json", "undeploy-apps-step-output-06.json",
          },
// @formatter:on
        });
    }

    public UndeployAppStepTest(String stepInputLocation, String stepOutputLocation) throws Exception {
        String resourceAsString = TestUtil.getResourceAsString(stepInputLocation, UndeployAppStepTest.class);
        stepInput = JsonUtil.fromJson(resourceAsString, StepInput.class);
        System.out.println(JsonUtil.toJson(stepInput, true));
        stepOutput = JsonUtil.fromJson(TestUtil.getResourceAsString(stepOutputLocation, UndeployAppStepTest.class), StepOutput.class);
    }

    @Before
    public void setUp() throws Exception {
        prepareContext();
        prepareClient();
        Mockito.when(client.areTasksSupported())
            .thenReturn(!stepInput.tasksPerApplication.isEmpty());
    }

    @Test
    public void testExecute() throws Exception {
        for (CloudApplication cloudApplication : stepInput.appsToDelete) {
            undeployApp(cloudApplication);
        }

        performAfterUndeploymentValidation();
    }

    protected abstract void performAfterUndeploymentValidation();

    private void undeployApp(CloudApplication cloudApplication) throws Exception {
        context.setVariable(Constants.VAR_APP_TO_PROCESS, JsonUtil.toJson(cloudApplication));
        step.execute(context);

        assertStepFinishedSuccessfully();
        performValidation(cloudApplication);
    }

    protected abstract void performValidation(CloudApplication cloudApplication);

    private void prepareContext() {
        StepsUtil.setAppsToUndeploy(context, stepInput.appsToDelete);
    }

    private void prepareClient() {
        CloudInfoExtended info = Mockito.mock(CloudInfoExtended.class);
        Mockito.when(info.isPortBasedRouting())
            .thenReturn(stepInput.portBasedRouting);
        Mockito.when(client.getCloudInfo())
            .thenReturn(info);
        Mockito.when(applicationRoutesGetter.getRoutes(any(), anyString()))
            .thenAnswer((invocation) -> {

                String appName = (String) invocation.getArguments()[1];
                return stepInput.appRoutesPerApplication.get(appName);

            });
        Mockito.when(client.getTasks(anyString()))
            .thenAnswer((invocation) -> {

                String appName = (String) invocation.getArguments()[0];
                return stepInput.tasksPerApplication.get(appName);

            });
    }

    protected static class StepInput {
        protected boolean portBasedRouting;
        protected List<CloudApplication> appsToDelete = Collections.emptyList();
        protected Map<String, List<CloudRoute>> appRoutesPerApplication = Collections.emptyMap();
        protected Map<String, List<CloudTask>> tasksPerApplication = Collections.emptyMap();
    }

    protected static class StepOutput {
        protected List<Pair<String, String>> expectedRoutesToDelete = Collections.emptyList();
        protected List<String> expectedTasksToCancel = Collections.emptyList();
    }

}
