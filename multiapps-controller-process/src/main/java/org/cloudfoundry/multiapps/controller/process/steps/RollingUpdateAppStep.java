package org.cloudfoundry.multiapps.controller.process.steps;

import java.text.MessageFormat;
import java.time.Duration;
import java.util.List;
import java.util.UUID;

import javax.inject.Inject;
import javax.inject.Named;

import org.cloudfoundry.multiapps.controller.core.cf.CloudControllerClientFactory;
import org.cloudfoundry.multiapps.controller.core.model.HookPhase;
import org.cloudfoundry.multiapps.controller.core.security.token.TokenService;
import org.cloudfoundry.multiapps.controller.process.variables.Variables;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;

import com.sap.cloudfoundry.client.facade.CloudControllerClient;
import com.sap.cloudfoundry.client.facade.domain.CloudApplication;
import com.sap.cloudfoundry.client.facade.domain.CloudDeployment;

@Named("rollingUpdateAppStep")
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class RollingUpdateAppStep extends TimeoutAsyncFlowableStepWithHooks implements BeforeStepHookPhaseProvider {

    private CloudControllerClientFactory clientFactory;
    private TokenService tokenService;

    @Inject
    public RollingUpdateAppStep(CloudControllerClientFactory clientFactory, TokenService tokenService) {
        this.clientFactory = clientFactory;
        this.tokenService = tokenService;
    }

    @Override
    public List<HookPhase> getHookPhasesBeforeStep(ProcessContext context) {
        return hooksPhaseBuilder.buildHookPhases(List.of(HookPhase.BEFORE_START), context);
    }

    @Override
    protected StepPhase executePollingStep(ProcessContext context) {
        CloudApplication application = context.getVariable(Variables.APP_TO_PROCESS);
        CloudControllerClient client = context.getControllerClient();

        context.getStepLogger()
               .info("Starting rolling update for application \"{0}\"", application.getName());
        UUID applicationGuid = client.getApplicationGuid(application.getName());
        CloudDeployment deployment = client.startRollingDeployment(applicationGuid);
        context.setVariable(Variables.CLOUD_DEPLOYMENT_TO_POLL, deployment);

        return StepPhase.POLL;
    }

    @Override
    public Duration getTimeout(ProcessContext context) {
        return context.getVariable(Variables.START_TIMEOUT);
    }

    @Override
    protected List<AsyncExecution> getAsyncStepExecutions(ProcessContext context) {
        return List.of(new PollRollingUpdateAppStatusExecution(clientFactory, tokenService));
    }

    @Override
    protected String getStepErrorMessage(ProcessContext context) {
        return MessageFormat.format("Error during rolling update of application \"{0}\"", context.getVariable(Variables.APP_TO_PROCESS)
                                                                                                 .getName());
    }

}
