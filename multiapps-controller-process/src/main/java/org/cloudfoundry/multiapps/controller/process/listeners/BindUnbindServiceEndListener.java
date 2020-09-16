package org.cloudfoundry.multiapps.controller.process.listeners;

import javax.inject.Named;

import org.cloudfoundry.multiapps.controller.client.lib.domain.CloudApplicationExtended;
import org.cloudfoundry.multiapps.controller.process.Constants;
import org.cloudfoundry.multiapps.controller.process.variables.VariableHandling;
import org.cloudfoundry.multiapps.controller.process.variables.Variables;
import org.flowable.engine.RuntimeService;
import org.flowable.engine.delegate.DelegateExecution;
import org.flowable.engine.delegate.ExecutionListener;
import org.flowable.engine.impl.context.Context;

@Named("bindUnbindServiceEndListener")
public class BindUnbindServiceEndListener implements ExecutionListener {

    private static final long serialVersionUID = 1L;

    @Override
    public void notify(DelegateExecution execution) {
        RuntimeService runtimeService = Context.getProcessEngineConfiguration()
                                               .getRuntimeService();

        CloudApplicationExtended app = VariableHandling.get(execution, Variables.APP_TO_PROCESS);
        String service = VariableHandling.get(execution, Variables.SERVICE_TO_UNBIND_BIND);
        boolean shouldUnbindService = VariableHandling.get(execution, Variables.SHOULD_UNBIND_SERVICE);
        boolean shouldBindService = VariableHandling.get(execution, Variables.SHOULD_BIND_SERVICE);

        String superExecutionId = execution.getParentId();
        String exportedVariableName = buildExportedVariableName(app.getName(), service);

        runtimeService.setVariable(superExecutionId, exportedVariableName, shouldUnbindService || shouldBindService);
    }

    public static String buildExportedVariableName(String appName, String service) {
        StringBuilder variableNameBuilder = new StringBuilder();
        variableNameBuilder.append(Constants.VAR_IS_APPLICATION_SERVICE_BINDING_UPDATED_VAR_PREFIX);
        variableNameBuilder.append(appName);
        variableNameBuilder.append('_');
        variableNameBuilder.append(service);
        return variableNameBuilder.toString();
    }
}
