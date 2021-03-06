package com.sap.cloud.lm.sl.cf.process.steps;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Named;

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;

import com.sap.cloud.lm.sl.cf.client.lib.domain.CloudApplicationExtended;
import com.sap.cloud.lm.sl.cf.client.lib.domain.ImmutableCloudApplicationExtended;
import com.sap.cloud.lm.sl.cf.core.cf.HandlerFactory;
import com.sap.cloud.lm.sl.cf.core.cf.v2.ConfigurationEntriesCloudModelBuilder;
import com.sap.cloud.lm.sl.cf.core.helpers.ModuleToDeployHelper;
import com.sap.cloud.lm.sl.cf.core.model.ConfigurationEntry;
import com.sap.cloud.lm.sl.cf.core.security.serialization.SecureSerialization;
import com.sap.cloud.lm.sl.cf.process.Messages;
import com.sap.cloud.lm.sl.cf.process.variables.Variables;
import com.sap.cloud.lm.sl.mta.model.DeploymentDescriptor;
import com.sap.cloud.lm.sl.mta.model.Module;

@Named("buildApplicationDeployModelStep")
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class BuildApplicationDeployModelStep extends SyncFlowableStep {

    @Inject
    private ModuleToDeployHelper moduleToDeployHelper;

    @Override
    protected StepPhase executeStep(ProcessContext context) {
        Module module = context.getVariable(Variables.MODULE_TO_DEPLOY);
        getStepLogger().debug(Messages.BUILDING_CLOUD_APP_MODEL, module.getName());

        Module applicationModule = findModuleInDeploymentDescriptor(context, module.getName());
        context.setVariable(Variables.MODULE_TO_DEPLOY, applicationModule);
        CloudApplicationExtended modifiedApp = StepsUtil.getApplicationCloudModelBuilder(context)
                                                        .build(applicationModule, moduleToDeployHelper);
        modifiedApp = ImmutableCloudApplicationExtended.builder()
                                                       .from(modifiedApp)
                                                       .uris(getApplicationUris(context, modifiedApp))
                                                       .build();
        context.setVariable(Variables.APP_TO_PROCESS, modifiedApp);

        buildConfigurationEntries(context, modifiedApp);
        context.setVariable(Variables.TASKS_TO_EXECUTE, modifiedApp.getTasks());

        getStepLogger().debug(Messages.CLOUD_APP_MODEL_BUILT);
        return StepPhase.DONE;
    }

    private Module findModuleInDeploymentDescriptor(ProcessContext context, String module) {
        HandlerFactory handlerFactory = StepsUtil.getHandlerFactory(context.getExecution());
        DeploymentDescriptor deploymentDescriptor = context.getVariable(Variables.COMPLETE_DEPLOYMENT_DESCRIPTOR);
        return handlerFactory.getDescriptorHandler()
                             .findModule(deploymentDescriptor, module);
    }

    @Override
    protected String getStepErrorMessage(ProcessContext context) {
        return Messages.ERROR_BUILDING_CLOUD_APP_MODEL;
    }

    private List<String> getApplicationUris(ProcessContext context, CloudApplicationExtended modifiedApp) {
        if (context.getVariable(Variables.USE_IDLE_URIS)) {
            return modifiedApp.getIdleUris();
        }
        return modifiedApp.getUris();
    }

    private void buildConfigurationEntries(ProcessContext context, CloudApplicationExtended app) {
        if (context.getVariable(Variables.SKIP_UPDATE_CONFIGURATION_ENTRIES)) {
            context.setVariable(Variables.CONFIGURATION_ENTRIES_TO_PUBLISH, Collections.emptyList());
            return;
        }
        DeploymentDescriptor deploymentDescriptor = context.getVariable(Variables.COMPLETE_DEPLOYMENT_DESCRIPTOR);

        ConfigurationEntriesCloudModelBuilder configurationEntriesCloudModelBuilder = getConfigurationEntriesCloudModelBuilder(context);
        Map<String, List<ConfigurationEntry>> allConfigurationEntries = configurationEntriesCloudModelBuilder.build(deploymentDescriptor);
        List<ConfigurationEntry> updatedModuleNames = allConfigurationEntries.getOrDefault(app.getModuleName(), Collections.emptyList());
        context.setVariable(Variables.CONFIGURATION_ENTRIES_TO_PUBLISH, updatedModuleNames);
        context.setVariable(Variables.SKIP_UPDATE_CONFIGURATION_ENTRIES, false);

        getStepLogger().debug(Messages.CONFIGURATION_ENTRIES_TO_PUBLISH, SecureSerialization.toJson(updatedModuleNames));
    }

    private ConfigurationEntriesCloudModelBuilder getConfigurationEntriesCloudModelBuilder(ProcessContext context) {
        String organizationName = context.getVariable(Variables.ORGANIZATION_NAME);
        String spaceName = context.getVariable(Variables.SPACE_NAME);
        String spaceGuid = context.getVariable(Variables.SPACE_GUID);
        return new ConfigurationEntriesCloudModelBuilder(organizationName, spaceName, spaceGuid);
    }

}
