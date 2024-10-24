package org.cloudfoundry.multiapps.controller.process.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.cloudfoundry.multiapps.controller.core.cf.metadata.MtaMetadataLabels;
import org.cloudfoundry.multiapps.controller.core.model.DeployedMta;
import org.cloudfoundry.multiapps.controller.core.model.DeployedMtaApplication;
import org.cloudfoundry.multiapps.controller.core.model.DeployedMtaApplication.ProductizationState;

import com.sap.cloudfoundry.client.facade.domain.CloudApplication;

public class ApplictationsPreserveCalculator {

    private final DeployedMta deployedMta;
    private final DeployedMta preservedMta;

    public ApplictationsPreserveCalculator(DeployedMta deployedMta, DeployedMta preservedMta) {
        this.deployedMta = deployedMta;
        this.preservedMta = preservedMta;
    }

    public List<CloudApplication> calculateAppsToPreserve(List<CloudApplication> appsToUndeploy, String checksumOfCurrentDescriptor) {
        // if (doesCurrentlyPreservedMtaMatch(checksumOfCurrentDescriptor)) {
        // return Collections.emptyList();
        // }

        // if (deployedMta.getMetadata()
        // .getVersion()
        // .toString()
        // .equals(deploymentDescriptorVersion)) {
        // return Collections.emptyList();
        // }

        // 1. Compare checksums between deployed mta and current deployment
        // 2. Compare checksum between preserved mta and current deployment
        // 3. Preserve apps only with live production state
        if (deployedMta.getApplications()
                       .stream()
                       .allMatch(deployedApplication -> doesApplicationChecksumMatchToCurrentDeployment(deployedApplication,
                                                                                                        checksumOfCurrentDescriptor))) {
            return Collections.emptyList();
        }

        if (preservedMta != null && preservedMta.getApplications()
                                                .stream()
                                                .allMatch(deployedApplication -> doesApplicationChecksumMatchToCurrentDeployment(deployedApplication,
                                                                                                                                 checksumOfCurrentDescriptor))) {
            return Collections.emptyList();
        }

        List<CloudApplication> appsToPreserve = new ArrayList<>();
        for (CloudApplication appToUndeploy : appsToUndeploy) {
            // String descriptorChecksumOfDeployedApplication = deployedApplication.getV3Metadata()
            // .getLabels()
            // .get("mta_preserved_descriptor_checksum");
            ProductizationState productizationStateOfDeployedApplication = deployedMta.getApplications()
                                                                                      .stream()
                                                                                      .filter(deployedMtaApp -> deployedMtaApp.getName()
                                                                                                                              .equals(appToUndeploy.getName()))
                                                                                      .map(DeployedMtaApplication::getProductizationState)
                                                                                      .findFirst()
                                                                                      .get();

            // if (descriptorChecksumOfDeployedApplication == null
            // || !checksumOfCurrentDescriptor.equals(descriptorChecksumOfDeployedApplication)
            // && productizationStateOfDeployedApplication == ProductizationState.LIVE) {
            // appsToPreserve.add(deployedApplication);
            // }

            if (productizationStateOfDeployedApplication == ProductizationState.LIVE) {
                appsToPreserve.add(appToUndeploy);
            }
        }

        return appsToPreserve;
    }

    private boolean doesApplicationChecksumMatchToCurrentDeployment(DeployedMtaApplication deployedApplication,
                                                                    String checksumOfCurrentDescriptor) {
        String checksumOfDeployedApplication = deployedApplication.getV3Metadata()
                                                                  .getLabels()
                                                                  .get(MtaMetadataLabels.MTA_DESCRIPTOR_CHECKSUM);

        return checksumOfDeployedApplication != null && checksumOfDeployedApplication.equals(checksumOfCurrentDescriptor);
    }

    // private boolean doesCurrentlyPreservedMtaMatch(String deploymentDescriptorVersion) {
    // return preservedMta != null && !preservedMta.getApplications()
    // .isEmpty()
    // && preservedMta.getMetadata()
    // .getVersion()
    // .toString()
    // .equals(deploymentDescriptorVersion)
    // && preservedMta.getMetadata()
    // .getVersion()
    // .equals(deployedMta.getMetadata()
    // .getVersion());
    // }

    public List<CloudApplication> calculateAppsToUndeploy(List<CloudApplication> appsToPreserve) {
        if (preservedMta == null || appsToPreserve.isEmpty()) {
            return Collections.emptyList();
        }

        List<String> appsToPreserveNames = appsToPreserve.stream()
                                                         .map(CloudApplication::getName)
                                                         .collect(Collectors.toList());
        List<DeployedMtaApplication> deployedPreservedApps = preservedMta.getApplications();
        return deployedPreservedApps.stream()
                                    .filter(application -> !appsToPreserveNames.contains(application.getName()))
                                    .collect(Collectors.toList());
    }

}
