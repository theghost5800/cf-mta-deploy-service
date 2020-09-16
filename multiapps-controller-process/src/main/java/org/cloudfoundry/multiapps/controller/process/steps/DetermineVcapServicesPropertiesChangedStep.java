package org.cloudfoundry.multiapps.controller.process.steps;

import java.text.MessageFormat;
import java.util.List;

import javax.inject.Named;

import org.cloudfoundry.multiapps.controller.client.lib.domain.CloudApplicationExtended;
import org.cloudfoundry.multiapps.controller.process.Messages;
import org.cloudfoundry.multiapps.controller.process.listeners.BindUnbindServiceEndListener;
import org.cloudfoundry.multiapps.controller.process.variables.Variables;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;

@Named("determineVcapServicesPropertiesChangedStep")
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class DetermineVcapServicesPropertiesChangedStep extends SyncFlowableStep {

    @Override
    protected StepPhase executeStep(ProcessContext context) {
        List<String> services = context.getVariable(Variables.SERVICES_TO_UNBIND_BIND);
        CloudApplicationExtended app = context.getVariable(Variables.APP_TO_PROCESS);
        getStepLogger().debug(Messages.DETERMINE_VCAP_SERVICES_PROPERTIES_CHANGED_FOR_APPLICATION, app.getName());

        boolean changedVcapServicesProperties = services.stream()
                                                        .map(service -> BindUnbindServiceEndListener.buildExportedVariableName(app.getName(),
                                                                                                                               service))
                                                        .map(variableName -> StepsUtil.getObject(context.getExecution(), variableName))
                                                        .filter(Boolean.class::isInstance)
                                                        .map(Boolean.class::cast)
                                                        .anyMatch(variableValue -> variableValue == true);

        context.setVariable(Variables.VCAP_SERVICES_PROPERTIES_CHANGED, changedVcapServicesProperties);
        return StepPhase.DONE;
    }

    @Override
    protected String getStepErrorMessage(ProcessContext context) {
        return MessageFormat.format(Messages.ERROR_WHILE_DETERMINE_VCAP_SERVICES_PROPERTIES_CHANGED_FOR_APPLICATION,
                                    context.getVariable(Variables.APP_TO_PROCESS)
                                           .getName());
    }

}
