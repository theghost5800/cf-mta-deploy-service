package org.cloudfoundry.multiapps.controller.process.steps;

import java.text.MessageFormat;

import org.cloudfoundry.client.v3.deployments.DeploymentStatusReason;
import org.cloudfoundry.client.v3.deployments.DeploymentStatusValue;
import org.cloudfoundry.multiapps.controller.core.cf.CloudControllerClientFactory;
import org.cloudfoundry.multiapps.controller.core.security.token.TokenService;
import org.cloudfoundry.multiapps.controller.persistence.services.ProcessLoggerProvider;
import org.cloudfoundry.multiapps.controller.process.variables.Variables;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sap.cloudfoundry.client.facade.CloudControllerClient;
import com.sap.cloudfoundry.client.facade.domain.CloudApplication;
import com.sap.cloudfoundry.client.facade.domain.CloudDeployment;

public class PollRollingUpdateAppStatusExecution implements AsyncExecution {

    private static final Logger LOGGER = LoggerFactory.getLogger(PollRollingUpdateAppStatusExecution.class);

    private CloudControllerClientFactory clientFactory;
    private TokenService tokenService;

    public PollRollingUpdateAppStatusExecution(CloudControllerClientFactory clientFactory, TokenService tokenService) {
        this.clientFactory = clientFactory;
        this.tokenService = tokenService;
    }

    @Override
    public AsyncExecutionState execute(ProcessContext context) {
        CloudApplication application = context.getVariable(Variables.APP_TO_PROCESS);
        CloudDeployment deploymentToPoll = context.getVariable(Variables.CLOUD_DEPLOYMENT_TO_POLL);
        CloudControllerClient client = context.getControllerClient();

        context.getStepLogger()
               .debug("Getting rolling update status for application \"{0}\"...", application.getName());

        ProcessLoggerProvider processLoggerProvider = context.getStepLogger()
                                                             .getProcessLoggerProvider();

        var user = context.getVariable(Variables.USER);
        var correlationId = context.getVariable(Variables.CORRELATION_ID);
        var logCacheClient = clientFactory.createLogCacheClient(tokenService.getToken(user), correlationId);
        StepsUtil.saveAppLogs(context, logCacheClient, deploymentToPoll.getApplicationGuid(), application.getName(), LOGGER,
                              processLoggerProvider);

        CloudDeployment deployment = client.getRollingDeployment(deploymentToPoll.getGuid());
        if (deployment.getValue() == DeploymentStatusValue.ACTIVE && deployment.getReason() == DeploymentStatusReason.DEPLOYING) {
            return AsyncExecutionState.RUNNING;
        }

        if (deployment.getValue() == DeploymentStatusValue.FINALIZED && deployment.getReason() == DeploymentStatusReason.DEPLOYED) {
            context.getStepLogger()
                   .debug("Rolling update for application \"{0}\" is finished", application.getName());
            return AsyncExecutionState.FINISHED;
        }

        context.getStepLogger()
               .error("Rolling update for application \"{0}\" is in inconsistant state - status \"(1)\" reason \"{2}\"",
                      application.getName(), deployment.getValue(), deployment.getReason());
        return AsyncExecutionState.ERROR;
    }

    @Override
    public String getPollingErrorMessage(ProcessContext context) {
        return MessageFormat.format("Error during rolling update application \"{0}\"", context.getVariable(Variables.APP_TO_PROCESS)
                                                                                              .getName());
    }

}
