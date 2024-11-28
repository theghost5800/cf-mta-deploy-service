package org.cloudfoundry.multiapps.controller.process.steps;

import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.inject.Named;
import javax.persistence.NoResultException;

import org.cloudfoundry.multiapps.common.ContentException;
import org.cloudfoundry.multiapps.controller.core.cf.detect.DeployedMtaDetector;
import org.cloudfoundry.multiapps.controller.core.cf.metadata.MtaMetadataLabels;
import org.cloudfoundry.multiapps.controller.core.model.DeployedMta;
import org.cloudfoundry.multiapps.controller.core.model.DeployedMtaApplication;
import org.cloudfoundry.multiapps.controller.core.util.NameUtil;
import org.cloudfoundry.multiapps.controller.persistence.dto.MtaDescriptorPreserver;
import org.cloudfoundry.multiapps.controller.persistence.services.MtaDescriptorPreserverService;
import org.cloudfoundry.multiapps.controller.persistence.services.OperationService;
import org.cloudfoundry.multiapps.controller.process.Constants;
import org.cloudfoundry.multiapps.controller.process.util.ProcessConflictPreventer;
import org.cloudfoundry.multiapps.controller.process.variables.Variables;
import org.cloudfoundry.multiapps.mta.model.Module;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;

import com.sap.cloudfoundry.client.facade.CloudControllerClient;

@Named("preparePreservedMtaForDeploymentStep")
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class PreparePreservedMtaForDeploymentStep extends SyncFlowableStep {

    private MtaDescriptorPreserverService mtaDescriptorPreserverService;
    private DeployedMtaDetector deployedMtaDetector;
    private OperationService operationService;
    private Function<OperationService, ProcessConflictPreventer> conflictPreventerSupplier = ProcessConflictPreventer::new;

    @Inject
    public PreparePreservedMtaForDeploymentStep(MtaDescriptorPreserverService mtaDescriptorPreserverService,
                                                DeployedMtaDetector deployedMtaDetector, OperationService operationService) {
        this.mtaDescriptorPreserverService = mtaDescriptorPreserverService;
        this.deployedMtaDetector = deployedMtaDetector;
        this.operationService = operationService;
    }

    @Override
    protected StepPhase executeStep(ProcessContext context) throws Exception {
        getStepLogger().info("Prepare to revert mta \"{0}\"", context.getVariable(Variables.MTA_ID));

        CloudControllerClient client = context.getControllerClient();
        String spaceGuid = context.getVariable(Variables.SPACE_GUID);
        String mtaId = context.getVariable(Variables.MTA_ID);
        String mtaNamespace = context.getVariable(Variables.MTA_NAMESPACE);
        String mtaNamespaceWithSystemNamespace = NameUtil.computeUserNamespaceWithSystemNamespace(Constants.MTA_PRESERVED_NAMESPACE,
                                                                                                  mtaNamespace);

        acquireOperationLock(context, mtaId);

        Optional<DeployedMta> preservedMtaOptional = deployedMtaDetector.detectDeployedMtaByNameAndNamespace(mtaId,
                                                                                                             mtaNamespaceWithSystemNamespace,
                                                                                                             client);

        Optional<DeployedMta> deployedMtaOptional = deployedMtaDetector.detectDeployedMtaByNameAndNamespace(mtaId, mtaNamespace, client);
        if (preservedMtaOptional.isEmpty() || deployedMtaOptional.isEmpty()) {
            throw new ContentException("Revert of mta id \"{0}\" cannot be done due to missing deployed/preserved mta used to be revert",
                                       mtaId);
        }
        DeployedMta preservedMta = preservedMtaOptional.get();
        String descriptorChecksumOfPreservedMta = preservedMta.getApplications()
                                                              .get(0)
                                                              .getV3Metadata()
                                                              .getLabels()
                                                              .get(MtaMetadataLabels.MTA_DESCRIPTOR_CHECKSUM);

        if (descriptorChecksumOfPreservedMta == null) {
            throw new ContentException("Descriptor checksum is not set in the application metadata and rollback operation cannot be done");
        }

        if (!preservedMta.getApplications()
                         .stream()
                         .allMatch(application -> descriptorChecksumOfPreservedMta.equals(application.getV3Metadata()
                                                                                                     .getLabels()
                                                                                                     .get(MtaMetadataLabels.MTA_DESCRIPTOR_CHECKSUM)))) {
            throw new ContentException("Revert operation cannot be done due to preserved applications with different checksums!");
        }

        MtaDescriptorPreserver preservedDescriptor = null;
        try {
            preservedDescriptor = mtaDescriptorPreserverService.createQuery()
                                                               .mtaId(mtaId)
                                                               .spaceId(spaceGuid)
                                                               .namespace(mtaNamespace)
                                                               .checksum(descriptorChecksumOfPreservedMta)
                                                               .singleResult();
        } catch (NoResultException e) {
            throw new ContentException("Revert of mta id \"{0}\" cannot be done due to missing descriptor used to perform revert", mtaId);
        }

        for (DeployedMtaApplication deployedApplication : preservedMta.getApplications()) {
            String applicationChecksum = deployedApplication.getV3Metadata()
                                                            .getLabels()
                                                            .get(MtaMetadataLabels.MTA_DESCRIPTOR_CHECKSUM);
            if (applicationChecksum == null && !preservedDescriptor.getChecksum()
                                                                   .equals(applicationChecksum)) {
                throw new ContentException("Checksums between descriptor in persistence layer and deployed app not match and revert is not possible!");
            }
        }

        context.setVariable(Variables.DEPLOYMENT_DESCRIPTOR, preservedDescriptor.getDescriptor());
        context.setVariable(Variables.MTA_MAJOR_SCHEMA_VERSION, preservedDescriptor.getDescriptor()
                                                                                   .getMajorSchemaVersion());
        context.setVariable(Variables.MTA_ARCHIVE_MODULES, preservedDescriptor.getDescriptor()
                                                                              .getModules()
                                                                              .stream()
                                                                              .map(Module::getName)
                                                                              .collect(Collectors.toSet()));
        context.setVariable(Variables.DEPLOYED_MTA, deployedMtaOptional.get());
        context.setVariable(Variables.PRESERVED_MTA, preservedMta);

        return StepPhase.DONE;
    }

    private void acquireOperationLock(ProcessContext context, String mtaId) {
        conflictPreventerSupplier.apply(operationService)
                                 .acquireLock(mtaId, null, context.getVariable(Variables.SPACE_GUID),
                                              context.getVariable(Variables.CORRELATION_ID));
    }

    @Override
    protected String getStepErrorMessage(ProcessContext context) {
        return "Error during preparation preserved mta for deployment";
    }

}
