package org.cloudfoundry.multiapps.controller.process.util;

import java.io.IOException;
import java.io.InputStream;
import java.text.MessageFormat;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.cloudfoundry.client.lib.CloudControllerClient;
import org.cloudfoundry.client.lib.CloudOperationException;
import org.cloudfoundry.client.lib.domain.CloudApplication;
import org.cloudfoundry.client.lib.domain.CloudEntity;
import org.cloudfoundry.client.lib.domain.CloudServiceBinding;
import org.cloudfoundry.client.lib.domain.CloudServiceInstance;
import org.cloudfoundry.multiapps.common.SLException;
import org.cloudfoundry.multiapps.common.util.JsonUtil;
import org.cloudfoundry.multiapps.common.util.MapUtil;
import org.cloudfoundry.multiapps.controller.client.lib.domain.CloudApplicationExtended;
import org.cloudfoundry.multiapps.controller.client.lib.domain.CloudServiceInstanceExtended;
import org.cloudfoundry.multiapps.controller.core.helpers.MtaArchiveElements;
import org.cloudfoundry.multiapps.controller.core.security.serialization.SecureSerialization;
import org.cloudfoundry.multiapps.controller.persistence.services.FileContentProcessor;
import org.cloudfoundry.multiapps.controller.persistence.services.FileService;
import org.cloudfoundry.multiapps.controller.persistence.services.FileStorageException;
import org.cloudfoundry.multiapps.controller.process.Messages;
import org.cloudfoundry.multiapps.controller.process.steps.ProcessContext;
import org.cloudfoundry.multiapps.controller.process.variables.Variables;
import org.cloudfoundry.multiapps.mta.handlers.ArchiveHandler;
import org.cloudfoundry.multiapps.mta.util.NameUtil;
import org.springframework.http.HttpStatus;

public class ServiceBindingParametersGetter {

    private final ProcessContext context;
    private final FileService fileService;
    private final long maxManifestSize;

    public ServiceBindingParametersGetter(ProcessContext context, FileService fileService, long maxManifestSize) {
        this.context = context;
        this.fileService = fileService;
        this.maxManifestSize = maxManifestSize;
    }

    public Map<String, Object> getServiceBindingParametersFromMta(CloudApplicationExtended app, String serviceName)
        throws FileStorageException {
        Optional<CloudServiceInstanceExtended> optionalService = getService(context.getVariable(Variables.SERVICES_TO_BIND), serviceName);
        if (optionalService.isEmpty()) {
            return Collections.emptyMap();
        }

        Map<String, Object> fileProvidedBindingParameters = getFileProvidedBindingParameters(app.getModuleName(), optionalService.get());
        Map<String, Object> descriptorProvidedBindingParameters = getDescriptorProvidedBindingParameters(app, optionalService.get());
        Map<String, Object> bindingParameters = MapUtil.mergeSafely(fileProvidedBindingParameters, descriptorProvidedBindingParameters);
        context.getStepLogger()
               .debug(Messages.BINDING_PARAMETERS_FOR_APPLICATION, app.getName(), SecureSerialization.toJson(bindingParameters));
        return bindingParameters;
    }

    private Optional<CloudServiceInstanceExtended> getService(List<CloudServiceInstanceExtended> services, String serviceName) {
        return services.stream()
                       .filter(service -> service.getName()
                                                 .equals(serviceName))
                       .findFirst();
    }

    private Map<String, Object> getFileProvidedBindingParameters(String moduleName, CloudServiceInstanceExtended service)
        throws FileStorageException {

        String requiredDependencyName = NameUtil.getPrefixedName(moduleName, service.getResourceName(),
                                                                 org.cloudfoundry.multiapps.controller.core.Constants.MTA_ELEMENT_SEPARATOR);
        return getFileProvidedBindingParameters(requiredDependencyName);

    }

    private Map<String, Object> getFileProvidedBindingParameters(String requiredDependencyName) throws FileStorageException {
        String archiveId = context.getRequiredVariable(Variables.APP_ARCHIVE_ID);
        MtaArchiveElements mtaArchiveElements = context.getVariable(Variables.MTA_ARCHIVE_ELEMENTS);
        String fileName = mtaArchiveElements.getRequiredDependencyFileName(requiredDependencyName);
        if (fileName == null) {
            return Collections.emptyMap();
        }
        FileContentProcessor<Map<String, Object>> fileProcessor = archive -> {
            try (InputStream file = ArchiveHandler.getInputStream(archive, fileName, maxManifestSize)) {
                return JsonUtil.convertJsonToMap(file);
            } catch (IOException e) {
                throw new SLException(e, Messages.ERROR_RETRIEVING_MTA_REQUIRED_DEPENDENCY_CONTENT, fileName);
            }
        };
        return fileService.processFileContent(context.getVariable(Variables.SPACE_GUID), archiveId, fileProcessor);
    }

    private Map<String, Object> getDescriptorProvidedBindingParameters(CloudApplicationExtended app, CloudServiceInstanceExtended service) {
        return app.getBindingParameters()
                  .getOrDefault(service.getResourceName(), Collections.emptyMap());
    }

    public Map<String, Object> getServiceBindingParametersFromExistingInstance(CloudApplication application, String serviceName) {
        CloudControllerClient client = context.getControllerClient();
        CloudServiceInstance serviceInstance = client.getServiceInstance(serviceName);
        CloudServiceBinding serviceBinding = getServiceBinding(client, application, serviceInstance);

        try {
            return client.getServiceBindingParameters(getGuid(serviceBinding));
        } catch (CloudOperationException e) {
            if (HttpStatus.NOT_IMPLEMENTED == e.getStatusCode() || HttpStatus.BAD_REQUEST == e.getStatusCode()) {
                context.getStepLogger()
                       .warnWithoutProgressMessage(Messages.CANNOT_RETRIEVE_PARAMETERS_OF_BINDING_BETWEEN_APPLICATION_0_AND_SERVICE_INSTANCE_1,
                                                   application.getName(), serviceInstance.getName());
                return null;
            }
            throw e;
        }
    }

    private CloudServiceBinding getServiceBinding(CloudControllerClient client, CloudApplication application,
                                                  CloudServiceInstance serviceInstance) {
        List<CloudServiceBinding> serviceBindings = client.getServiceBindings(getGuid(serviceInstance));
        context.getStepLogger()
               .debug(Messages.LOOKING_FOR_SERVICE_BINDINGS, getGuid(application), getGuid(serviceInstance),
                      SecureSerialization.toJson(serviceBindings));
        for (CloudServiceBinding serviceBinding : serviceBindings) {
            if (application.getMetadata()
                           .getGuid()
                           .equals(serviceBinding.getApplicationGuid())) {
                return serviceBinding;
            }
        }
        throw new IllegalStateException(MessageFormat.format(Messages.APPLICATION_UNBOUND_IN_PARALLEL, application.getName(),
                                                             serviceInstance.getName()));
    }

    private UUID getGuid(CloudEntity entity) {
        return entity.getMetadata()
                     .getGuid();
    }

}
