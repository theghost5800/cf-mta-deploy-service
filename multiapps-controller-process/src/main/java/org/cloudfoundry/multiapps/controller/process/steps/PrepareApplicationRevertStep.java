package org.cloudfoundry.multiapps.controller.process.steps;

import java.text.MessageFormat;

import javax.inject.Named;

import org.cloudfoundry.client.v3.Metadata;
import org.cloudfoundry.multiapps.controller.core.cf.metadata.MtaMetadataAnnotations;
import org.cloudfoundry.multiapps.controller.core.cf.metadata.MtaMetadataLabels;
import org.cloudfoundry.multiapps.controller.core.cf.metadata.util.MtaMetadataUtil;
import org.cloudfoundry.multiapps.controller.core.model.BlueGreenApplicationNameSuffix;
import org.cloudfoundry.multiapps.controller.core.util.NameUtil;
import org.cloudfoundry.multiapps.controller.process.Constants;
import org.cloudfoundry.multiapps.controller.process.variables.Variables;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;

import com.sap.cloudfoundry.client.facade.CloudControllerClient;
import com.sap.cloudfoundry.client.facade.domain.CloudApplication;

@Named("prepareApplicationRevertStep")
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class PrepareApplicationRevertStep extends SyncFlowableStep {

    @Override
    protected StepPhase executeStep(ProcessContext context) throws Exception {
        CloudApplication cloudApplication = context.getVariable(Variables.APP_TO_PROCESS);
        CloudControllerClient client = context.getControllerClient();

        String newApplicationName = BlueGreenApplicationNameSuffix.removeSuffix(cloudApplication.getName());
        newApplicationName = NameUtil.computeValidApplicationName(newApplicationName, Constants.MTA_PRESERVED_NAMESPACE, true);
        client.rename(cloudApplication.getName(), newApplicationName);
        String hashedMtaNamespace = MtaMetadataUtil.getHashedLabel(Constants.MTA_PRESERVED_NAMESPACE);
        client.updateApplicationMetadata(cloudApplication.getGuid(), Metadata.builder()
                                                                             .from(cloudApplication.getV3Metadata())
                                                                             // .label(MtaMetadataLabels.MTA_PRESERVED_DESCRIPTOR_CHECKSUM,
                                                                             // context.getVariable(Variables.CHECKSUM_OF_MERGED_DESCRIPTOR))
                                                                             .label(MtaMetadataLabels.MTA_NAMESPACE, hashedMtaNamespace)
                                                                             .annotation(MtaMetadataAnnotations.MTA_NAMESPACE,
                                                                                         Constants.MTA_PRESERVED_NAMESPACE)
                                                                             .build());

        return StepPhase.DONE;
    }

    @Override
    protected String getStepErrorMessage(ProcessContext context) {
        return MessageFormat.format("Error while preserve old applcation \"{0}\"", context.getVariable(Variables.APP_TO_PROCESS)
                                                                                          .getName());
    }

}
