package org.cloudfoundry.multiapps.controller.process.steps;

import java.text.MessageFormat;
import java.util.List;

import javax.inject.Named;

import org.cloudfoundry.client.lib.ApplicationServicesUpdateCallback;
import org.cloudfoundry.client.lib.CloudControllerClient;
import org.cloudfoundry.client.lib.CloudOperationException;
import org.cloudfoundry.multiapps.common.SLException;
import org.cloudfoundry.multiapps.controller.client.lib.domain.CloudApplicationExtended;
import org.cloudfoundry.multiapps.controller.client.lib.domain.CloudServiceInstanceExtended;
import org.cloudfoundry.multiapps.controller.core.model.ServiceBindingActionsToExecute;
import org.cloudfoundry.multiapps.controller.process.Messages;
import org.cloudfoundry.multiapps.controller.process.variables.Variables;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;

@Named("bindServiceStep")
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class BindServiceStep extends SyncFlowableStep {

    @Override
    protected StepPhase executeStep(ProcessContext context) throws Exception {
        CloudApplicationExtended app = context.getVariable(Variables.APP_TO_PROCESS);
        ServiceBindingActionsToExecute serviceBindingActionsToExecute = context.getVariable(Variables.SERVICE_BINDING_ACTIONS_TO_EXECUTE);
        getStepLogger().info(MessageFormat.format("Binding app \"{0}\" from service \"{1}\"", app.getName(),
                                                  serviceBindingActionsToExecute.getName()));

        CloudControllerClient client = context.getControllerClient();
        client.bindServiceInstance(app.getName(), serviceBindingActionsToExecute.getName(),
                                   serviceBindingActionsToExecute.getBindingParameters(), getApplicationServicesUpdateCallback(context));

        return StepPhase.DONE;
    }

    private ApplicationServicesUpdateCallback getApplicationServicesUpdateCallback(ProcessContext context) {
        return new DefaultApplicationServicesUpdateCallback(context);
    }

    @Override
    protected String getStepErrorMessage(ProcessContext context) {
        return MessageFormat.format("Error while binding app \"{0}\" from service \"{1}\"", context.getVariable(Variables.APP_TO_PROCESS)
                                                                                                   .getName(),
                                    context.getVariable(Variables.SERVICE_BINDING_ACTIONS_TO_EXECUTE)
                                           .getName());
    }

    private class DefaultApplicationServicesUpdateCallback implements ApplicationServicesUpdateCallback {

        private final ProcessContext context;

        private DefaultApplicationServicesUpdateCallback(ProcessContext context) {
            this.context = context;
        }

        @Override
        public void onError(CloudOperationException e, String applicationName, String serviceName) {
            List<CloudServiceInstanceExtended> servicesToBind = context.getVariable(Variables.SERVICES_TO_BIND);
            CloudServiceInstanceExtended serviceToBind = findServiceCloudModel(servicesToBind, serviceName);

            if (serviceToBind != null && serviceToBind.isOptional()) {
                getStepLogger().warn(e, Messages.COULD_NOT_BIND_APP_TO_OPTIONAL_SERVICE, applicationName, serviceName);
                return;
            }
            throw new SLException(e, Messages.COULD_NOT_BIND_APP_TO_SERVICE, applicationName, serviceName, e.getMessage());
        }

        private CloudServiceInstanceExtended findServiceCloudModel(List<CloudServiceInstanceExtended> servicesCloudModel,
                                                                   String serviceName) {
            return servicesCloudModel.stream()
                                     .filter(service -> service.getName()
                                                               .equals(serviceName))
                                     .findAny()
                                     .orElse(null);
        }

    }

}
