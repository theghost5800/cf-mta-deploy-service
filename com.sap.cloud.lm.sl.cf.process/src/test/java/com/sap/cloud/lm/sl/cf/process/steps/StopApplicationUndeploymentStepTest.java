package com.sap.cloud.lm.sl.cf.process.steps;

import static org.mockito.Mockito.verify;

import java.util.Arrays;
import java.util.List;

import org.cloudfoundry.client.lib.domain.CloudApplication;
import org.junit.Assert;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;

import com.sap.cloud.lm.sl.cf.core.cf.metadata.processor.EnvMtaMetadataParser;
import com.sap.cloud.lm.sl.cf.core.cf.metadata.processor.MtaMetadataParser;
import com.sap.cloud.lm.sl.cf.core.model.HookPhase;
import com.sap.cloud.lm.sl.cf.process.util.HooksPhaseBuilder;
import com.sap.cloud.lm.sl.cf.process.util.HooksPhaseGetter;

class StopApplicationUndeploymentStepTest extends UndeployAppStepTest {

    @Mock
    private MtaMetadataParser mtaMetadataParser;
    @Mock
    private EnvMtaMetadataParser envMtaMetadataParser;
    @Mock
    private HooksPhaseGetter hooksPhaseGetter;
    @Mock
    private HooksPhaseBuilder hooksPhaseBuilder;

    @Test
    void testGetHookPhaseBefore() {
        Mockito.when(hooksPhaseBuilder.buildHookPhases(Arrays.asList(HookPhase.BEFORE_STOP, HookPhase.APPLICATION_BEFORE_STOP_LIVE),
                                                       context))
               .thenReturn(Arrays.asList(HookPhase.BLUE_GREEN_APPLICATION_BEFORE_STOP_LIVE, HookPhase.APPLICATION_BEFORE_STOP_LIVE));
        List<HookPhase> expectedHooks = Arrays.asList(HookPhase.BLUE_GREEN_APPLICATION_BEFORE_STOP_LIVE,
                                                      HookPhase.APPLICATION_BEFORE_STOP_LIVE);
        List<HookPhase> hookPhasesBeforeStep = ((StopApplicationUndeploymentStep) step).getHookPhasesBeforeStep(context);
        Assert.assertEquals(expectedHooks, hookPhasesBeforeStep);
    }

    @Test
    void testGetHookPhaseAfter() {
        Mockito.when(hooksPhaseBuilder.buildHookPhases(Arrays.asList(HookPhase.AFTER_STOP, HookPhase.APPLICATION_AFTER_STOP_LIVE), context))
               .thenReturn(Arrays.asList(HookPhase.BLUE_GREEN_APPLICATION_AFTER_STOP_LIVE, HookPhase.APPLICATION_AFTER_STOP_LIVE));
        List<HookPhase> expectedHooks = Arrays.asList(HookPhase.BLUE_GREEN_APPLICATION_AFTER_STOP_LIVE,
                                                      HookPhase.APPLICATION_AFTER_STOP_LIVE);
        List<HookPhase> hookPhasesBeforeStep = ((StopApplicationUndeploymentStep) step).getHookPhasesAfterStep(context);
        Assert.assertEquals(expectedHooks, hookPhasesBeforeStep);
    }

    @Override
    protected void performValidation(CloudApplication cloudApplication) {
        verify(client).stopApplication(cloudApplication.getName());
    }

    @Override
    protected UndeployAppStep createStep() {
        return new StopApplicationUndeploymentStep();
    }

    @Override
    protected void performAfterUndeploymentValidation() {
    }

}
