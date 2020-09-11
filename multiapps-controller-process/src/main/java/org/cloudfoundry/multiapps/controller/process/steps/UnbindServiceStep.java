package org.cloudfoundry.multiapps.controller.process.steps;

import java.text.MessageFormat;

import javax.inject.Named;

import org.cloudfoundry.client.lib.CloudControllerClient;
import org.cloudfoundry.multiapps.controller.client.lib.domain.CloudApplicationExtended;
import org.cloudfoundry.multiapps.controller.core.model.ServiceBindingActionsToExecute;
import org.cloudfoundry.multiapps.controller.process.variables.Variables;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;

@Named("unbindServiceStep")
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class UnbindServiceStep extends SyncFlowableStep {

    @Override
    protected StepPhase executeStep(ProcessContext context) throws Exception {
        CloudApplicationExtended app = context.getVariable(Variables.APP_TO_PROCESS);
        ServiceBindingActionsToExecute serviceBindingActionsToExecute = context.getVariable(Variables.SERVICE_BINDING_ACTIONS_TO_EXECUTE);
        getStepLogger().info(MessageFormat.format("Unbinding app \"{0}\" from service \"{1}\"", app.getName(),
                                                  serviceBindingActionsToExecute.getName()));

        CloudControllerClient client = context.getControllerClient();
        client.unbindServiceInstance(app.getName(), serviceBindingActionsToExecute.getName());

        return StepPhase.DONE;
    }

    @Override
    protected String getStepErrorMessage(ProcessContext context) {
        return MessageFormat.format("Error while unbinding app \"{0}\" from service \"{1}\"", context.getVariable(Variables.APP_TO_PROCESS)
                                                                                                     .getName(),
                                    context.getVariable(Variables.SERVICE_BINDING_ACTIONS_TO_EXECUTE)
                                           .getName());
    }

}
