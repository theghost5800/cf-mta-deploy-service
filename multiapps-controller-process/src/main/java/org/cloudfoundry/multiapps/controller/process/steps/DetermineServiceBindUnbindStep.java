package org.cloudfoundry.multiapps.controller.process.steps;

import java.text.MessageFormat;
import java.util.Map;
import java.util.Objects;

import javax.inject.Named;

import org.cloudfoundry.client.lib.CloudControllerClient;
import org.cloudfoundry.client.lib.domain.CloudApplication;
import org.cloudfoundry.multiapps.controller.client.lib.domain.CloudApplicationExtended;
import org.cloudfoundry.multiapps.controller.client.lib.domain.CloudApplicationExtended.AttributeUpdateStrategy;
import org.cloudfoundry.multiapps.controller.persistence.services.FileStorageException;
import org.cloudfoundry.multiapps.controller.process.Messages;
import org.cloudfoundry.multiapps.controller.process.util.ServiceBindingParametersGetter;
import org.cloudfoundry.multiapps.controller.process.variables.Variables;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;

@Named("determineServiceBindUnbindStep")
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class DetermineServiceBindUnbindStep extends SyncFlowableStep {

    @Override
    protected StepPhase executeStep(ProcessContext context) throws FileStorageException {
        CloudApplicationExtended app = context.getVariable(Variables.APP_TO_PROCESS);
        String service = context.getVariable(Variables.SERVICE_TO_UNBIND_BIND);
        getStepLogger().debug(Messages.DETERMINE_BIND_UNBIND_OPERATIONS_APPLICATION_0_SERVICE_1, app.getName(), service);

        if (!isServicePartFromMta(app, service) && !shouldKeepExistingServiceBindings(app)) {
            context.setVariable(Variables.SHOULD_UNBIND_SERVICE, true);
            context.setVariable(Variables.SHOULD_BIND_SERVICE, false);
            return StepPhase.DONE;
        }

        CloudControllerClient client = context.getControllerClient();
        CloudApplication existingApp = client.getApplication(app.getName());

        ServiceBindingParametersGetter serviceBindingParametersGetter = getServiceBindingParametersGetter(context);
        Map<String, Object> bindingParameters = serviceBindingParametersGetter.getServiceBindingParametersFromMta(app, service);
        if (!doesServiceBindingExist(service, existingApp)) {
            context.setVariable(Variables.SHOULD_UNBIND_SERVICE, false);
            context.setVariable(Variables.SHOULD_BIND_SERVICE, true);
            context.setVariable(Variables.SERVICE_BINDING_PARAMETERS, bindingParameters);
            return StepPhase.DONE;
        }

        if (shouldRebindService(serviceBindingParametersGetter, existingApp, service, bindingParameters)) {
            context.setVariable(Variables.SHOULD_UNBIND_SERVICE, true);
            context.setVariable(Variables.SHOULD_BIND_SERVICE, true);
            context.setVariable(Variables.SERVICE_BINDING_PARAMETERS, bindingParameters);
            return StepPhase.DONE;
        }

        getStepLogger().info(Messages.WILL_NOT_REBIND_APP_TO_SERVICE, service, app.getName());
        context.setVariable(Variables.SHOULD_UNBIND_SERVICE, false);
        context.setVariable(Variables.SHOULD_BIND_SERVICE, false);
        return StepPhase.DONE;
    }

    private boolean isServicePartFromMta(CloudApplicationExtended app, String service) {
        return app.getServices()
                  .contains(service);
    }

    private boolean shouldKeepExistingServiceBindings(CloudApplicationExtended app) {
        AttributeUpdateStrategy appAttributesUpdateBehavior = app.getAttributesUpdateStrategy();
        return appAttributesUpdateBehavior.shouldKeepExistingServiceBindings();
    }

    protected ServiceBindingParametersGetter getServiceBindingParametersGetter(ProcessContext context) {
        return new ServiceBindingParametersGetter(context, fileService, configuration.getMaxManifestSize());
    }

    private boolean doesServiceBindingExist(String service, CloudApplication existingApp) {
        return existingApp.getServices()
                          .contains(service);
    }

    private boolean shouldRebindService(ServiceBindingParametersGetter serviceBindingParametersGetter, CloudApplication app,
                                        String serviceName, Map<String, Object> newBindingParameters) {
        Map<String, Object> currentBindingParameters = serviceBindingParametersGetter.getServiceBindingParametersFromExistingInstance(app,
                                                                                                                                      serviceName);
        return !Objects.equals(currentBindingParameters, newBindingParameters);
    }

    @Override
    protected String getStepErrorMessage(ProcessContext context) {
        return MessageFormat.format(Messages.ERROR_WHILE_DETERMINE_BIND_UNBIND_OEPRATIONS_OF_APPLICATION_TO_SERVICE,
                                    context.getVariable(Variables.APP_TO_PROCESS)
                                           .getName(),
                                    context.getVariable(Variables.SERVICE_TO_UNBIND_BIND));
    }

}
