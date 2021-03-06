package com.sap.cloud.lm.sl.cf.core.helpers;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.sap.cloud.lm.sl.cf.core.model.ApplicationColor;
import com.sap.cloud.lm.sl.cf.core.util.NameUtil;
import com.sap.cloud.lm.sl.mta.model.DeploymentDescriptor;
import com.sap.cloud.lm.sl.mta.model.Module;

public class ApplicationNameSuffixAppenderTest {

    @Test
    public void testGreenNameAppending() {
        DeploymentDescriptor descriptor = createDeploymentDescriptor("a", "b");
        descriptor.accept(getApplicationNameAppender(ApplicationColor.GREEN));

        Assertions.assertTrue(descriptor.getModules()
                                        .stream()
                                        .map(NameUtil::getApplicationName)
                                        .allMatch(appName -> appName.endsWith(ApplicationColor.GREEN.asSuffix())));
    }

    @Test
    public void testBlueNameAppending() {
        DeploymentDescriptor descriptor = createDeploymentDescriptor("a", "b");
        descriptor.accept(getApplicationNameAppender(ApplicationColor.BLUE));

        Assertions.assertTrue(descriptor.getModules()
                                        .stream()
                                        .map(NameUtil::getApplicationName)
                                        .allMatch(appName -> appName.endsWith(ApplicationColor.BLUE.asSuffix())));
    }

    private static DeploymentDescriptor createDeploymentDescriptor(String... moduleNames) {
        DeploymentDescriptor descriptor = DeploymentDescriptor.createV3();
        List<Module> modules = new ArrayList<>(5);
        for (String moduleName : moduleNames) {
            modules.add(Module.createV3()
                              .setName(moduleName));
        }
        descriptor.setModules(modules);
        return descriptor;
    }

    private static ApplicationNameSuffixAppender getApplicationNameAppender(ApplicationColor applicationColor) {
        return new ApplicationNameSuffixAppender(applicationColor.asSuffix());
    }

}
