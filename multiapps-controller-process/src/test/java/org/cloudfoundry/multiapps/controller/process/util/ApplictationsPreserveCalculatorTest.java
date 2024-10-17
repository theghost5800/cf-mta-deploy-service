package org.cloudfoundry.multiapps.controller.process.util;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.cloudfoundry.client.v3.Metadata;
import org.cloudfoundry.multiapps.controller.core.cf.metadata.ImmutableMtaMetadata;
import org.cloudfoundry.multiapps.controller.core.cf.metadata.MtaMetadataLabels;
import org.cloudfoundry.multiapps.controller.core.model.DeployedMta;
import org.cloudfoundry.multiapps.controller.core.model.DeployedMtaApplication;
import org.cloudfoundry.multiapps.controller.core.model.DeployedMtaApplication.ProductizationState;
import org.cloudfoundry.multiapps.controller.core.model.ImmutableDeployedMta;
import org.cloudfoundry.multiapps.controller.core.model.ImmutableDeployedMtaApplication;
import org.cloudfoundry.multiapps.mta.model.Version;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import com.sap.cloudfoundry.client.facade.domain.CloudApplication;
import com.sap.cloudfoundry.client.facade.domain.ImmutableCloudApplication;

class ApplictationsPreserveCalculatorTest {

    private static final String MTA_ID = "test-mta";
    private static final Version MTA_VERSION = Version.parseVersion("1.0.0");

    private static Stream<Arguments> testCalculateAppsToPreserve() {
        return Stream.of(
                         // (1) Already deployed application match checksum of current deployment descriptor
                         Arguments.of(List.of(new TestApplication("app-1", "app-1-live", "1")), Collections.emptyList(), "1",
                                      List.of("app-1-live"), List.of()),
                         // (2) Current deployment descriptor checksum has different value than deployed mta
                         Arguments.of(List.of(new TestApplication("app-1", "app-1-live", "1"),
                                              new TestApplication("app-1", "app-1-idle", "2", ProductizationState.IDLE)),
                                      Collections.emptyList(), "2", List.of("app-1-live", "app-1-idle"),
                                      List.of(ImmutableCloudApplication.builder()
                                                                       .name("app-1-live")
                                                                       .v3Metadata(Metadata.builder()
                                                                                           .label(MtaMetadataLabels.MTA_DESCRIPTOR_CHECKSUM,
                                                                                                  "1")
                                                                                           .build())
                                                                       .build())),
                         // (3) Current deployment descriptor match checksum of deployed and preserved mta
                         Arguments.of(List.of(new TestApplication("app-1", "app-1-live", "1")),
                                      List.of(new TestApplication("app-1", "mta-preserved-app-1", "1")), "1", List.of("app-1-live"),
                                      Collections.emptyList()),
                         // (4) Current deployment descriptor checksum has different value of deployed and preserved mta
                         Arguments.of(List.of(new TestApplication("app-1", "app-1-live", "2")),
                                      List.of(new TestApplication("app-1",
                                                                  "mta-preserved-app-1",
                                                                  "1")),
                                      "3", List.of("app-1-live", "mta-preserved-app-1"), List.of(ImmutableCloudApplication.builder()
                                                                                                                          .name("app-1-live")
                                                                                                                          .v3Metadata(Metadata.builder()
                                                                                                                                              .label(MtaMetadataLabels.MTA_DESCRIPTOR_CHECKSUM,
                                                                                                                                                     "2")
                                                                                                                                              .build())
                                                                                                                          .build())),
                         // (5) Current deployment descriptor match checksum of deployed mta only
                         Arguments.of(List.of(new TestApplication("app-1", "app-1-live", "2")),
                                      List.of(new TestApplication("app-1", "mta-preserved-app-1", "1")), "2", List.of("app-1-live"),
                                      Collections.emptyList()),
                         // (6) Current deployment descriptor checksum match value of preserved mta
                         Arguments.of(List.of(new TestApplication("app-1", "app-1-live", "2")),
                                      List.of(new TestApplication("app-1", "mta-preserved-app-1", "1")), "1",
                                      List.of("app-1-live", "app-1-idle"), Collections.emptyList())
        // (7) Current deployment descriptor checksum cannot be matched with already deployed mta
        // Arguments.of(List.of(new TestApplication("app-1",
        // "app-1-live",
        // null)),
        // Collections.emptyList(), "2", List.of("app-1-live", "app-1-idle"),
        // List.of(ImmutableCloudApplication.builder()
        // .name("app-1-live")
        // .v3Metadata(Metadata.builder()
        // .label(MtaMetadataLabels.MTA_PRESERVED_DESCRIPTOR_CHECKSUM,
        // "2")
        // .build())
        // .build()))
        );
    }

    @ParameterizedTest
    @MethodSource
    void testCalculateAppsToPreserve(List<TestApplication> deployedApplications, List<TestApplication> preservedApplications,
                                     String currentDeploymentDescriptorChecksum, List<String> appNamesToUndeploy,
                                     List<CloudApplication> expectedAppsToPreserve) {
        DeployedMta deployedMta = geteDeployedMta(deployedApplications);
        DeployedMta preservedMta = geteDeployedMta(preservedApplications);

        ApplictationsPreserveCalculator calculator = new ApplictationsPreserveCalculator(deployedMta, preservedMta);

        List<CloudApplication> appsToUndeploy = getAppsToUndeploy(deployedMta.getApplications(), appNamesToUndeploy);
        List<CloudApplication> appsToPreserve = calculator.calculateAppsToPreserve(appsToUndeploy, currentDeploymentDescriptorChecksum);

        assertEquals(expectedAppsToPreserve, appsToPreserve);
    }

    private List<CloudApplication> getAppsToUndeploy(List<DeployedMtaApplication> deployedApplications, List<String> appNamesToUndeploy) {
        return deployedApplications.stream()
                                   .filter(deployedApplication -> appNamesToUndeploy.contains(deployedApplication.getName()))
                                   .map(ImmutableCloudApplication::copyOf)
                                   .collect(Collectors.toList());
    }

    private DeployedMta geteDeployedMta(List<TestApplication> deployedApplications) {
        if (deployedApplications.isEmpty()) {
            return null;
        }
        List<DeployedMtaApplication> deployedMtaApplications = new ArrayList<>();
        for (TestApplication application : deployedApplications) {
            deployedMtaApplications.add(ImmutableDeployedMtaApplication.builder()
                                                                       .moduleName(application.moduleName)
                                                                       .name(application.appName)
                                                                       .v3Metadata(Metadata.builder()
                                                                                           .label(MtaMetadataLabels.MTA_DESCRIPTOR_CHECKSUM,
                                                                                                  application.metadataDescriptorChecksum)
                                                                                           .build())
                                                                       .productizationState(application.productizationState)
                                                                       .build());
        }
        return ImmutableDeployedMta.builder()
                                   .applications(deployedMtaApplications)
                                   .metadata(ImmutableMtaMetadata.builder()
                                                                 .id(MTA_ID)
                                                                 .version(MTA_VERSION)
                                                                 .build())

                                   .build();
    }

    private static class TestApplication {
        String moduleName;
        String appName;
        String metadataDescriptorChecksum;
        ProductizationState productizationState;

        TestApplication(String moduleName, String appName, String metadataDescriptorChecksum) {
            this.moduleName = moduleName;
            this.appName = appName;
            this.metadataDescriptorChecksum = metadataDescriptorChecksum;
            this.productizationState = ProductizationState.LIVE;
        }

        TestApplication(String moduleName, String appName, String metadataDescriptorChecksum, ProductizationState productizationState) {
            this.moduleName = moduleName;
            this.appName = appName;
            this.metadataDescriptorChecksum = metadataDescriptorChecksum;
            this.productizationState = productizationState;
        }

    }

}
