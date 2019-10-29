package com.sap.cloud.lm.sl.cf.core.cf;

import java.io.File;
import java.io.InputStream;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.cloudfoundry.client.lib.ApplicationServicesUpdateCallback;
import org.cloudfoundry.client.lib.CloudCredentials;
import org.cloudfoundry.client.lib.StartingInfo;
import org.cloudfoundry.client.lib.UploadStatusCallback;
import org.cloudfoundry.client.lib.domain.ApplicationStats;
import org.cloudfoundry.client.lib.domain.CloudApplication;
import org.cloudfoundry.client.lib.domain.CloudBuild;
import org.cloudfoundry.client.lib.domain.CloudDomain;
import org.cloudfoundry.client.lib.domain.CloudEvent;
import org.cloudfoundry.client.lib.domain.CloudInfo;
import org.cloudfoundry.client.lib.domain.CloudOrganization;
import org.cloudfoundry.client.lib.domain.CloudQuota;
import org.cloudfoundry.client.lib.domain.CloudRoute;
import org.cloudfoundry.client.lib.domain.CloudSecurityGroup;
import org.cloudfoundry.client.lib.domain.CloudService;
import org.cloudfoundry.client.lib.domain.CloudServiceBroker;
import org.cloudfoundry.client.lib.domain.CloudServiceInstance;
import org.cloudfoundry.client.lib.domain.CloudServiceKey;
import org.cloudfoundry.client.lib.domain.CloudServiceOffering;
import org.cloudfoundry.client.lib.domain.CloudSpace;
import org.cloudfoundry.client.lib.domain.CloudStack;
import org.cloudfoundry.client.lib.domain.CloudTask;
import org.cloudfoundry.client.lib.domain.CloudUser;
import org.cloudfoundry.client.lib.domain.DockerInfo;
import org.cloudfoundry.client.lib.domain.InstancesInfo;
import org.cloudfoundry.client.lib.domain.Staging;
import org.cloudfoundry.client.lib.domain.Upload;
import org.cloudfoundry.client.lib.domain.UploadToken;
import org.cloudfoundry.client.lib.rest.CloudControllerRestClient;
import org.springframework.security.oauth2.common.OAuth2AccessToken;

import com.sap.cloud.lm.sl.cf.client.ResilientCloudControllerClient;
import com.sap.cloud.lm.sl.cf.core.performance.CloudControllerPerformanceOperation;
import com.sap.cloud.lm.sl.cf.core.util.UserMessageLogger;

public class CloudControllerClientPerformanceLogger extends ResilientCloudControllerClient {

    private final UserMessageLogger userMessageLogger;
    private final OperationsPerformanceMonitor operationsPerformanceMonitor;

    public CloudControllerClientPerformanceLogger(CloudControllerRestClient delegate, UserMessageLogger userMessageLoger,
                                                  OperationsPerformanceMonitor operationsPerformanceMonitor) {
        super(delegate);
        this.userMessageLogger = userMessageLoger;
        this.operationsPerformanceMonitor = operationsPerformanceMonitor;
    }

    @Override
    public void createService(CloudService service) {
        Instant startTime = Instant.now();
        super.createService(service);
        Instant endTime = Instant.now();
        logExecutionTime("Create Service", startTime, endTime);
    }

    @Override
    public void addDomain(String domainName) {
        Instant startTime = Instant.now();
        super.addDomain(domainName);
        Instant endTime = Instant.now();
        logExecutionTime("Add Domain", startTime, endTime);
    }

    @Override
    public void addRoute(String host, String domainName, String path) {
        Instant startTime = Instant.now();
        super.addRoute(host, domainName, path);
        Instant endTime = Instant.now();
        logExecutionTime("Add Route", startTime, endTime);
    }

    @Override
    public void bindService(String applicationName, String serviceName) {
        Instant startTime = Instant.now();
        super.bindService(applicationName, serviceName);
        Instant endTime = Instant.now();
        logExecutionTime("Bind Service", startTime, endTime);
    }

    @Override
    public void bindService(String applicationName, String serviceName, Map<String, Object> parameters,
                            ApplicationServicesUpdateCallback applicationServicesUpdateCallback) {
        Instant startTime = Instant.now();
        super.bindService(applicationName, serviceName, parameters, applicationServicesUpdateCallback);
        Instant endTime = Instant.now();
        logExecutionTime("Bind Service", startTime, endTime);
    }

    @Override
    public void createApplication(String applicationName, Staging staging, Integer disk, Integer memory, List<String> uris,
                                  List<String> serviceNames, DockerInfo dockerInfo) {
        Instant startTime = Instant.now();
        super.createApplication(applicationName, staging, disk, memory, uris, serviceNames, dockerInfo);
        Instant endTime = Instant.now();
        logExecutionTime("Create Application", startTime, endTime);
    }

    @Override
    public void createServiceBroker(CloudServiceBroker serviceBroker) {
        Instant startTime = Instant.now();
        super.createServiceBroker(serviceBroker);
        Instant endTime = Instant.now();
        logExecutionTime("Create Service Broker", startTime, endTime);
    }

    @Override
    public void createUserProvidedService(CloudService service, Map<String, Object> credentials) {
        Instant startTime = Instant.now();
        super.createUserProvidedService(service, credentials);
        Instant endTime = Instant.now();
        logExecutionTime("Create User-Provided Service", startTime, endTime);
    }

    @Override
    public void deleteApplication(String applicationName) {
        Instant startTime = Instant.now();
        super.deleteApplication(applicationName);
        Instant endTime = Instant.now();
        logExecutionTime("Delete Application", startTime, endTime);
    }

    @Override
    public void deleteDomain(String domainName) {
        Instant startTime = Instant.now();
        super.deleteDomain(domainName);
        Instant endTime = Instant.now();
        logExecutionTime("Delete Domain", startTime, endTime);
    }

    @Override
    public List<CloudRoute> deleteOrphanedRoutes() {
        Instant startTime = Instant.now();
        List<CloudRoute> deletedRoutes = super.deleteOrphanedRoutes();
        Instant endTime = Instant.now();
        logExecutionTime("Delete Orphaned Routes", startTime, endTime);
        return deletedRoutes;
    }

    @Override
    public void deleteRoute(String host, String domainName, String path) {
        Instant startTime = Instant.now();
        super.deleteRoute(host, domainName, path);
        Instant endTime = Instant.now();
        logExecutionTime("Delete Route", startTime, endTime);
    }

    @Override
    public void deleteService(String service) {
        Instant startTime = Instant.now();
        super.deleteService(service);
        Instant endTime = Instant.now();
        logExecutionTime("Delete Service", startTime, endTime);
    }

    @Override
    public void deleteServiceBroker(String name) {
        Instant startTime = Instant.now();
        super.deleteServiceBroker(name);
        Instant endTime = Instant.now();
        logExecutionTime("Delete Service Broker", startTime, endTime);
    }

    @Override
    public CloudApplication getApplication(String applicationName) {
        Instant startTime = Instant.now();
        CloudApplication application = super.getApplication(applicationName);
        Instant endTime = Instant.now();
        logExecutionTime("Get Application", startTime, endTime);
        return application;
    }

    @Override
    public CloudApplication getApplication(String applicationName, boolean required) {
        Instant startTime = Instant.now();
        CloudApplication application = super.getApplication(applicationName, required);
        Instant endTime = Instant.now();
        logExecutionTime("Get Application", startTime, endTime);
        return application;
    }

    @Override
    public CloudApplication getApplication(UUID appGuid) {
        Instant startTime = Instant.now();
        CloudApplication application = super.getApplication(appGuid);
        Instant endTime = Instant.now();
        logExecutionTime("Get Application", startTime, endTime);
        return application;
    }

    @Override
    public InstancesInfo getApplicationInstances(String applicationName) {
        Instant startTime = Instant.now();
        InstancesInfo instancesInfo = super.getApplicationInstances(applicationName);
        Instant endTime = Instant.now();
        logExecutionTime("Get Application Instances", startTime, endTime);
        return instancesInfo;
    }

    @Override
    public InstancesInfo getApplicationInstances(CloudApplication app) {
        Instant startTime = Instant.now();
        InstancesInfo instancesInfo = super.getApplicationInstances(app);
        Instant endTime = Instant.now();
        logExecutionTime("Get Application Instances", startTime, endTime);
        return instancesInfo;
    }

    @Override
    public List<CloudApplication> getApplications() {
        Instant startTime = Instant.now();
        List<CloudApplication> applications = super.getApplications();
        Instant endTime = Instant.now();
        logExecutionTime("Get Applications", startTime, endTime);
        return applications;
    }

    @Override
    public List<CloudApplication> getApplications(boolean fetchAdditionalInfo) {
        Instant startTime = Instant.now();
        List<CloudApplication> applications = super.getApplications(fetchAdditionalInfo);
        Instant endTime = Instant.now();
        logExecutionTime("Get Applications", startTime, endTime);
        return applications;
    }

    @Override
    public CloudDomain getDefaultDomain() {
        Instant startTime = Instant.now();
        CloudDomain defaultDomain = super.getDefaultDomain();
        Instant endTime = Instant.now();
        logExecutionTime("Get Default Domain", startTime, endTime);
        return defaultDomain;
    }

    @Override
    public List<CloudDomain> getDomains() {
        Instant startTime = Instant.now();
        List<CloudDomain> domains = super.getDomains();
        Instant endTime = Instant.now();
        logExecutionTime("Get Domains", startTime, endTime);
        return domains;
    }

    @Override
    public List<CloudDomain> getDomainsForOrganization() {
        Instant startTime = Instant.now();
        List<CloudDomain> domainsForOrganization = super.getDomainsForOrganization();
        Instant endTime = Instant.now();
        logExecutionTime("Get Domains for Organizations", startTime, endTime);
        return domainsForOrganization;
    }

    @Override
    public CloudOrganization getOrganization(String organizationName) {
        Instant startTime = Instant.now();
        CloudOrganization organization = super.getOrganization(organizationName);
        Instant endTime = Instant.now();
        logExecutionTime("Get Organization", startTime, endTime);
        return organization;
    }

    @Override
    public CloudOrganization getOrganization(String organizationName, boolean required) {
        Instant startTime = Instant.now();
        CloudOrganization organization = super.getOrganization(organizationName, required);
        Instant endTime = Instant.now();
        logExecutionTime("Get Organization", startTime, endTime);
        return organization;
    }

    @Override
    public List<CloudDomain> getPrivateDomains() {
        Instant startTime = Instant.now();
        List<CloudDomain> domains = super.getPrivateDomains();
        Instant endTime = Instant.now();
        logExecutionTime("Get Private Domains", startTime, endTime);
        return domains;
    }

    @Override
    public List<CloudRoute> getRoutes(String domainName) {
        Instant startTime = Instant.now();
        List<CloudRoute> routes = super.getRoutes(domainName);
        Instant endTime = Instant.now();
        logExecutionTime("Get Routes", startTime, endTime);
        return routes;
    }

    @Override
    public CloudServiceBroker getServiceBroker(String name) {
        Instant startTime = Instant.now();
        CloudServiceBroker serviceBroker = super.getServiceBroker(name);
        Instant endTime = Instant.now();
        logExecutionTime("Get Service Broker", startTime, endTime);
        return serviceBroker;
    }

    @Override
    public CloudServiceBroker getServiceBroker(String name, boolean required) {
        Instant startTime = Instant.now();
        CloudServiceBroker serviceBroker = super.getServiceBroker(name, required);
        Instant endTime = Instant.now();
        logExecutionTime("Get Service Broker", startTime, endTime);
        return serviceBroker;
    }

    @Override
    public List<CloudServiceBroker> getServiceBrokers() {
        Instant startTime = Instant.now();
        List<CloudServiceBroker> serviceBrokers = super.getServiceBrokers();
        Instant endTime = Instant.now();
        logExecutionTime("Get Service Brokers", startTime, endTime);
        return serviceBrokers;
    }

    @Override
    public CloudServiceInstance getServiceInstance(String service) {
        Instant startTime = Instant.now();
        CloudServiceInstance serviceInstance = super.getServiceInstance(service);
        Instant endTime = Instant.now();
        logExecutionTime("Get Service Instance", startTime, endTime);
        return serviceInstance;
    }

    @Override
    public Map<String, Object> getServiceParameters(UUID guid) {
        Instant startTime = Instant.now();
        Map<String, Object> serviceParameters = super.getServiceParameters(guid);
        Instant endTime = Instant.now();
        logExecutionTime("Get Service Parameters", startTime, endTime);
        return serviceParameters;
    }

    @Override
    public CloudServiceInstance getServiceInstance(String service, boolean required) {
        Instant startTime = Instant.now();
        CloudServiceInstance serviceInstance = super.getServiceInstance(service, required);
        Instant endTime = Instant.now();
        logExecutionTime("Get Srevice Instance", startTime, endTime);
        return serviceInstance;
    }

    @Override
    public List<CloudDomain> getSharedDomains() {
        Instant startTime = Instant.now();
        List<CloudDomain> sharedDomains = super.getSharedDomains();
        Instant endTime = Instant.now();
        logExecutionTime("Get Shared Domains", startTime, endTime);
        return sharedDomains;
    }

    @Override
    public CloudSpace getSpace(UUID spaceGuid) {
        Instant startTime = Instant.now();
        CloudSpace space = super.getSpace(spaceGuid);
        Instant endTime = Instant.now();
        logExecutionTime("Get Space", startTime, endTime);
        return space;
    }

    @Override
    public CloudSpace getSpace(String organizationName, String spaceName) {
        Instant startTime = Instant.now();
        CloudSpace space = super.getSpace(organizationName, spaceName);
        Instant endTime = Instant.now();
        logExecutionTime("Get Space", startTime, endTime);
        return space;
    }

    @Override
    public CloudSpace getSpace(String organizationName, String spaceName, boolean required) {
        Instant startTime = Instant.now();
        CloudSpace space = super.getSpace(organizationName, spaceName, required);
        Instant endTime = Instant.now();
        logExecutionTime("Get Space", startTime, endTime);
        return space;
    }

    @Override
    public CloudSpace getSpace(String spaceName) {
        Instant startTime = Instant.now();
        CloudSpace space = super.getSpace(spaceName);
        Instant endTime = Instant.now();
        logExecutionTime("Get Space", startTime, endTime);
        return space;
    }

    @Override
    public CloudSpace getSpace(String spaceName, boolean required) {
        Instant startTime = Instant.now();
        CloudSpace space = super.getSpace(spaceName, required);
        Instant endTime = Instant.now();
        logExecutionTime("Get Space", startTime, endTime);
        return space;
    }

    @Override
    public List<CloudSpace> getSpaces() {
        Instant startTime = Instant.now();
        List<CloudSpace> spaces = super.getSpaces();
        Instant endTime = Instant.now();
        logExecutionTime("Get Spaces", startTime, endTime);
        return spaces;
    }

    @Override
    public List<CloudSpace> getSpaces(String organizationName) {
        Instant startTime = Instant.now();
        List<CloudSpace> spaces = super.getSpaces(organizationName);
        Instant endTime = Instant.now();
        logExecutionTime("Get Spaces", startTime, endTime);
        return spaces;
    }

    @Override
    public String getStagingLogs(StartingInfo info, int offset) {
        Instant startTime = Instant.now();
        String stagingLogs = super.getStagingLogs(info, offset);
        Instant endTime = Instant.now();
        logExecutionTime("Get Staging Logs", startTime, endTime);
        return stagingLogs;
    }

    @Override
    public void rename(String applicationName, String newName) {
        Instant startTime = Instant.now();
        super.rename(applicationName, newName);
        Instant endTime = Instant.now();
        logExecutionTime("Rename Application", startTime, endTime);
    }

    @Override
    public StartingInfo restartApplication(String applicationName) {
        Instant startTime = Instant.now();
        StartingInfo startingInfo = super.restartApplication(applicationName);
        Instant endTime = Instant.now();
        logExecutionTime("Restart Application", startTime, endTime);
        return startingInfo;
    }

    @Override
    public StartingInfo startApplication(String applicationName) {
        Instant startTime = Instant.now();
        StartingInfo startingInfo = super.startApplication(applicationName);
        Instant endTime = Instant.now();
        logExecutionTime("Start Application", startTime, endTime);
        return startingInfo;
    }

    @Override
    public void stopApplication(String applicationName) {
        Instant startTime = Instant.now();
        super.stopApplication(applicationName);
        Instant endTime = Instant.now();
        logExecutionTime("Stop Application", startTime, endTime);
    }

    @Override
    public void unbindService(String applicationName, String serviceName) {
        Instant startTime = Instant.now();
        super.unbindService(applicationName, serviceName);
        Instant endTime = Instant.now();
        logExecutionTime("Unbind Service", startTime, endTime);
    }

    @Override
    public void updateApplicationDiskQuota(String applicationName, int disk) {
        Instant startTime = Instant.now();
        super.updateApplicationDiskQuota(applicationName, disk);
        Instant endTime = Instant.now();
        logExecutionTime("Update Application Disk Quota", startTime, endTime);
    }

    @Override
    public void updateApplicationEnv(String applicationName, Map<String, String> env) {
        Instant startTime = Instant.now();
        super.updateApplicationEnv(applicationName, env);
        Instant endTime = Instant.now();
        logExecutionTime("Update Application Environment", startTime, endTime);
    }

    @Override
    public void updateApplicationInstances(String applicationName, int instances) {
        Instant startTime = Instant.now();
        super.updateApplicationInstances(applicationName, instances);
        Instant endTime = Instant.now();
        logExecutionTime("Update Application Instances", startTime, endTime);
    }

    @Override
    public void updateApplicationMemory(String applicationName, int memory) {
        Instant startTime = Instant.now();
        super.updateApplicationMemory(applicationName, memory);
        Instant endTime = Instant.now();
        logExecutionTime("Update Application Memory", startTime, endTime);
    }

    @Override
    public List<String> updateApplicationServices(String applicationName,
                                                  Map<String, Map<String, Object>> serviceNamesWithBindingParameters,
                                                  ApplicationServicesUpdateCallback applicationServicesUpdateCallback) {
        Instant startTime = Instant.now();
        List<String> applicationServices = super.updateApplicationServices(applicationName, serviceNamesWithBindingParameters,
                                                                           applicationServicesUpdateCallback);
        Instant endTime = Instant.now();
        logExecutionTime("Update Application Services", startTime, endTime);
        return applicationServices;
    }

    @Override
    public void updateApplicationStaging(String applicationName, Staging staging) {
        Instant startTime = Instant.now();
        super.updateApplicationStaging(applicationName, staging);
        Instant endTime = Instant.now();
        logExecutionTime("Update Application Staging", startTime, endTime);
    }

    @Override
    public void updateApplicationUris(String applicationName, List<String> uris) {
        Instant startTime = Instant.now();
        super.updateApplicationUris(applicationName, uris);
        Instant endTime = Instant.now();
        logExecutionTime("Update Application Uris", startTime, endTime);
    }

    @Override
    public void updateServiceBroker(CloudServiceBroker serviceBroker) {
        Instant startTime = Instant.now();
        super.updateServiceBroker(serviceBroker);
        Instant endTime = Instant.now();
        logExecutionTime("Update Service Broker", startTime, endTime);
    }

    @Override
    public void updateServicePlanVisibilityForBroker(String name, boolean visibility) {
        Instant startTime = Instant.now();
        super.updateServicePlanVisibilityForBroker(name, visibility);
        Instant endTime = Instant.now();
        logExecutionTime("Update Service plan Visibility for Broker", startTime, endTime);
    }

    @Override
    public void uploadApplication(String applicationName, File file, UploadStatusCallback callback) {
        Instant startTime = Instant.now();
        super.uploadApplication(applicationName, file, callback);
        Instant endTime = Instant.now();
        logExecutionTime("Upload Application", startTime, endTime);
    }

    @Override
    public void uploadApplication(String applicationName, InputStream inputStream, UploadStatusCallback callback) {
        Instant startTime = Instant.now();
        super.uploadApplication(applicationName, inputStream, callback);
        Instant endTime = Instant.now();
        logExecutionTime("Upload Application", startTime, endTime);
    }

    @Override
    public UploadToken asyncUploadApplication(String applicationName, File file, UploadStatusCallback callback) {
        Instant startTime = Instant.now();
        UploadToken uploadToken = super.asyncUploadApplication(applicationName, file, callback);
        Instant endTime = Instant.now();
        logExecutionTime("Async Upload Application", startTime, endTime);
        return uploadToken;
    }

    @Override
    public Upload getUploadStatus(String uploadToken) {
        Instant startTime = Instant.now();
        Upload upload = super.getUploadStatus(uploadToken);
        Instant endTime = Instant.now();
        logExecutionTime("Get Upload Status", startTime, endTime);
        return upload;
    }

    @Override
    public CloudService getService(String service) {
        Instant startTime = Instant.now();
        CloudService cloudService = super.getService(service);
        Instant endTime = Instant.now();
        logExecutionTime("Get Service", startTime, endTime);
        return cloudService;
    }

    @Override
    public CloudService getService(String service, boolean required) {
        Instant startTime = Instant.now();
        CloudService cloudService = super.getService(service, required);
        Instant endTime = Instant.now();
        logExecutionTime("Get Service", startTime, endTime);
        return cloudService;
    }

    @Override
    public List<CloudService> getServices() {
        Instant startTime = Instant.now();
        List<CloudService> services = super.getServices();
        Instant endTime = Instant.now();
        logExecutionTime("Get Services", startTime, endTime);
        return services;
    }

    @Override
    public void createApplication(String applicationName, Staging staging, Integer memory, List<String> uris, List<String> serviceNames) {
        Instant startTime = Instant.now();
        super.createApplication(applicationName, staging, memory, uris, serviceNames);
        Instant endTime = Instant.now();
        logExecutionTime("Create Application", startTime, endTime);
    }

    @Override
    public CloudServiceKey createServiceKey(String serviceName, String serviceKeyName, Map<String, Object> parameters) {
        Instant startTime = Instant.now();
        CloudServiceKey serviceKey = super.createServiceKey(serviceName, serviceKeyName, parameters);
        Instant endTime = Instant.now();
        logExecutionTime("Create Service Key", startTime, endTime);
        return serviceKey;
    }

    @Override
    public void createSpace(String spaceName) {
        Instant startTime = Instant.now();
        super.createSpace(spaceName);
        Instant endTime = Instant.now();
        logExecutionTime("Create Space", startTime, endTime);
    }

    @Override
    public void createUserProvidedService(CloudService service, Map<String, Object> credentials, String syslogDrainUrl) {
        Instant startTime = Instant.now();
        super.createUserProvidedService(service, credentials, syslogDrainUrl);
        Instant endTime = Instant.now();
        logExecutionTime("Create User-Provided Service", startTime, endTime);
    }

    @Override
    public void deleteAllApplications() {
        Instant startTime = Instant.now();
        super.deleteAllApplications();
        Instant endTime = Instant.now();
        logExecutionTime("Delete All Applications", startTime, endTime);
    }

    @Override
    public void deleteAllServices() {
        Instant startTime = Instant.now();
        super.deleteAllServices();
        Instant endTime = Instant.now();
        logExecutionTime("Delete All Services", startTime, endTime);
    }

    @Override
    public void deleteServiceKey(String service, String serviceKey) {
        Instant startTime = Instant.now();
        super.deleteServiceKey(service, serviceKey);
        Instant endTime = Instant.now();
        logExecutionTime("Delete Service Key", startTime, endTime);
    }

    @Override
    public void deleteSpace(String spaceName) {
        Instant startTime = Instant.now();
        super.deleteSpace(spaceName);
        Instant endTime = Instant.now();
        logExecutionTime("Delete Space", startTime, endTime);
    }

    @Override
    public Map<String, Object> getApplicationEnvironment(String applicationName) {
        Instant startTime = Instant.now();
        Map<String, Object> applicationEnvironment = super.getApplicationEnvironment(applicationName);
        Instant endTime = Instant.now();
        logExecutionTime("Get Application Environment", startTime, endTime);
        return applicationEnvironment;
    }

    @Override
    public Map<String, Object> getApplicationEnvironment(UUID appGuid) {
        Instant startTime = Instant.now();
        Map<String, Object> applicationEnvironment = super.getApplicationEnvironment(appGuid);
        Instant endTime = Instant.now();
        logExecutionTime("Get Application Environment", startTime, endTime);
        return applicationEnvironment;
    }

    @Override
    public List<CloudEvent> getApplicationEvents(String applicationName) {
        Instant startTime = Instant.now();
        List<CloudEvent> applicationEvents = super.getApplicationEvents(applicationName);
        Instant endTime = Instant.now();
        logExecutionTime("Get Application Events", startTime, endTime);
        return applicationEvents;
    }

    @Override
    public ApplicationStats getApplicationStats(String applicationName) {
        Instant startTime = Instant.now();
        ApplicationStats applicationStats = super.getApplicationStats(applicationName);
        Instant endTime = Instant.now();
        logExecutionTime("Get Application Stats", startTime, endTime);
        return applicationStats;
    }

    @Override
    public CloudInfo getCloudInfo() {
        Instant startTime = Instant.now();
        CloudInfo cloudInfo = super.getCloudInfo();
        Instant endTime = Instant.now();
        logExecutionTime("Get Cloud Info", startTime, endTime);
        return cloudInfo;
    }

    @Override
    public List<CloudEvent> getEvents() {
        Instant startTime = Instant.now();
        List<CloudEvent> events = super.getEvents();
        Instant endTime = Instant.now();
        logExecutionTime("Get Events", startTime, endTime);
        return events;
    }

    @Override
    public Map<String, CloudUser> getOrganizationUsers(String organizationName) {
        Instant startTime = Instant.now();
        Map<String, CloudUser> organizationUsers = super.getOrganizationUsers(organizationName);
        Instant endTime = Instant.now();
        logExecutionTime("Get Organization Users", startTime, endTime);
        return organizationUsers;
    }

    @Override
    public List<CloudOrganization> getOrganizations() {
        Instant startTime = Instant.now();
        List<CloudOrganization> organizations = super.getOrganizations();
        Instant endTime = Instant.now();
        logExecutionTime("Get Organizations", startTime, endTime);
        return organizations;
    }

    @Override
    public CloudQuota getQuota(String quotaName) {
        Instant startTime = Instant.now();
        CloudQuota quota = super.getQuota(quotaName);
        Instant endTime = Instant.now();
        logExecutionTime("Get Quota", startTime, endTime);
        return quota;
    }

    @Override
    public CloudQuota getQuota(String quotaName, boolean required) {
        Instant startTime = Instant.now();
        CloudQuota quota = super.getQuota(quotaName, required);
        Instant endTime = Instant.now();
        logExecutionTime("Get Quota", startTime, endTime);
        return quota;
    }

    @Override
    public List<CloudQuota> getQuotas() {
        Instant startTime = Instant.now();
        List<CloudQuota> quotas = super.getQuotas();
        Instant endTime = Instant.now();
        logExecutionTime("Get Quotas", startTime, endTime);
        return quotas;
    }

    @Override
    public List<CloudServiceKey> getServiceKeys(String serviceName) {
        Instant startTime = Instant.now();
        List<CloudServiceKey> serviceKeys = super.getServiceKeys(serviceName);
        Instant endTime = Instant.now();
        logExecutionTime("Get Service Keys", startTime, endTime);
        return serviceKeys;
    }

    @Override
    public List<CloudServiceOffering> getServiceOfferings() {
        Instant startTime = Instant.now();
        List<CloudServiceOffering> serviceOfferings = super.getServiceOfferings();
        Instant endTime = Instant.now();
        logExecutionTime("Get Service Offerings", startTime, endTime);
        return serviceOfferings;
    }

    @Override
    public List<UUID> getSpaceAuditors(String spaceName) {
        Instant startTime = Instant.now();
        List<UUID> spaceAuditors = super.getSpaceAuditors(spaceName);
        Instant endTime = Instant.now();
        logExecutionTime("Get Space Auditors", startTime, endTime);
        return spaceAuditors;
    }

    @Override
    public List<UUID> getSpaceAuditors(UUID spaceGuid) {
        Instant startTime = Instant.now();
        List<UUID> spaceAuditors = super.getSpaceAuditors(spaceGuid);
        Instant endTime = Instant.now();
        logExecutionTime("Get Space Auditors", startTime, endTime);
        return spaceAuditors;
    }

    @Override
    public List<UUID> getSpaceAuditors(String organizationName, String spaceName) {
        Instant startTime = Instant.now();
        List<UUID> spaceAuditors = super.getSpaceAuditors(organizationName, spaceName);
        Instant endTime = Instant.now();
        logExecutionTime("Get Space Auditors", startTime, endTime);
        return spaceAuditors;
    }

    @Override
    public List<UUID> getSpaceDevelopers(String spaceName) {
        Instant startTime = Instant.now();
        List<UUID> spaceDevelopers = super.getSpaceDevelopers(spaceName);
        Instant endTime = Instant.now();
        logExecutionTime("Get Space Developers", startTime, endTime);
        return spaceDevelopers;
    }

    @Override
    public List<UUID> getSpaceDevelopers(UUID spaceGuid) {
        Instant startTime = Instant.now();
        List<UUID> spaceDevelopers = super.getSpaceDevelopers(spaceGuid);
        Instant endTime = Instant.now();
        logExecutionTime("Get Space Developers", startTime, endTime);
        return spaceDevelopers;
    }

    @Override
    public List<UUID> getSpaceDevelopers(String organizationName, String spaceName) {
        Instant startTime = Instant.now();
        List<UUID> spaceDevelopers = super.getSpaceDevelopers(organizationName, spaceName);
        Instant endTime = Instant.now();
        logExecutionTime("Get Space Developers", startTime, endTime);
        return spaceDevelopers;
    }

    @Override
    public List<UUID> getSpaceManagers(String spaceName) {
        Instant startTime = Instant.now();
        List<UUID> spaceManagers = super.getSpaceManagers(spaceName);
        Instant endTime = Instant.now();
        logExecutionTime("Get Space Managers", startTime, endTime);
        return spaceManagers;
    }

    @Override
    public List<UUID> getSpaceManagers(UUID spaceGuid) {
        Instant startTime = Instant.now();
        List<UUID> spaceManagers = super.getSpaceManagers(spaceGuid);
        Instant endTime = Instant.now();
        logExecutionTime("Get Space Managers", startTime, endTime);
        return spaceManagers;
    }

    @Override
    public List<UUID> getSpaceManagers(String organizationName, String spaceName) {
        Instant startTime = Instant.now();
        List<UUID> spaceManagers = super.getSpaceManagers(organizationName, spaceName);
        Instant endTime = Instant.now();
        logExecutionTime("Get Space Managers", startTime, endTime);
        return spaceManagers;
    }

    @Override
    public CloudStack getStack(String name) {
        Instant startTime = Instant.now();
        CloudStack stack = super.getStack(name);
        Instant endTime = Instant.now();
        logExecutionTime("Get Stack", startTime, endTime);
        return stack;
    }

    @Override
    public CloudStack getStack(String name, boolean required) {
        Instant startTime = Instant.now();
        CloudStack stack = super.getStack(name, required);
        Instant endTime = Instant.now();
        logExecutionTime("Get Stack", startTime, endTime);
        return stack;
    }

    @Override
    public List<CloudStack> getStacks() {
        Instant startTime = Instant.now();
        List<CloudStack> stacks = super.getStacks();
        Instant endTime = Instant.now();
        logExecutionTime("Get Stacks", startTime, endTime);
        return stacks;
    }

    @Override
    public OAuth2AccessToken login() {
        Instant startTime = Instant.now();
        OAuth2AccessToken token = super.login();
        Instant endTime = Instant.now();
        logExecutionTime("Login", startTime, endTime);
        return token;
    }

    @Override
    public void removeDomain(String domainName) {
        Instant startTime = Instant.now();
        super.removeDomain(domainName);
        Instant endTime = Instant.now();
        logExecutionTime("Remove Domain", startTime, endTime);
    }

    @Override
    public void updatePassword(String newPassword) {
        Instant startTime = Instant.now();
        super.updatePassword(newPassword);
        Instant endTime = Instant.now();
        logExecutionTime("Update Password", startTime, endTime);
    }

    @Override
    public void updatePassword(CloudCredentials credentials, String newPassword) {
        Instant startTime = Instant.now();
        super.updatePassword(credentials, newPassword);
        Instant endTime = Instant.now();
        logExecutionTime("Update Password", startTime, endTime);
    }

    @Override
    public void updateQuota(CloudQuota quota, String name) {
        Instant startTime = Instant.now();
        super.updateQuota(quota, name);
        Instant endTime = Instant.now();
        logExecutionTime("Update Quota", startTime, endTime);
    }

    @Override
    public void updateSecurityGroup(CloudSecurityGroup securityGroup) {
        Instant startTime = Instant.now();
        super.updateSecurityGroup(securityGroup);
        Instant endTime = Instant.now();
        logExecutionTime("Update Security Group", startTime, endTime);
    }

    @Override
    public void updateSecurityGroup(String name, InputStream jsonRulesFile) {
        Instant startTime = Instant.now();
        super.updateSecurityGroup(name, jsonRulesFile);
        Instant endTime = Instant.now();
        logExecutionTime("Update Security Group", startTime, endTime);
    }

    @Override
    public void uploadApplication(String applicationName, String file) {
        Instant startTime = Instant.now();
        super.uploadApplication(applicationName, file);
        Instant endTime = Instant.now();
        logExecutionTime("Upload Application", startTime, endTime);
    }

    @Override
    public void uploadApplication(String applicationName, File file) {
        Instant startTime = Instant.now();
        super.uploadApplication(applicationName, file);
        Instant endTime = Instant.now();
        logExecutionTime("Upload Application", startTime, endTime);
    }

    @Override
    public void uploadApplication(String applicationName, InputStream inputStream) {
        Instant startTime = Instant.now();
        super.uploadApplication(applicationName, inputStream);
        Instant endTime = Instant.now();
        logExecutionTime("Upload Application", startTime, endTime);
    }

    @Override
    public UploadToken asyncUploadApplication(String applicationName, File file) {
        Instant startTime = Instant.now();
        UploadToken uploadToken = super.asyncUploadApplication(applicationName, file);
        Instant endTime = Instant.now();
        logExecutionTime("Async Upload Application", startTime, endTime);
        return uploadToken;
    }

    @Override
    public CloudTask getTask(UUID taskGuid) {
        Instant startTime = Instant.now();
        CloudTask task = super.getTask(taskGuid);
        Instant endTime = Instant.now();
        logExecutionTime("Get Task", startTime, endTime);
        return task;
    }

    @Override
    public List<CloudTask> getTasks(String applicationName) {
        Instant startTime = Instant.now();
        List<CloudTask> tasks = super.getTasks(applicationName);
        Instant endTime = Instant.now();
        logExecutionTime("Get Tasks", startTime, endTime);
        return tasks;
    }

    @Override
    public CloudTask runTask(String applicationName, CloudTask task) {
        Instant startTime = Instant.now();
        CloudTask cloudTask = super.runTask(applicationName, task);
        Instant endTime = Instant.now();
        logExecutionTime("Run Task", startTime, endTime);
        return cloudTask;
    }

    @Override
    public CloudTask cancelTask(UUID taskGuid) {
        Instant startTime = Instant.now();
        CloudTask cloudTask = super.cancelTask(taskGuid);
        Instant endTime = Instant.now();
        logExecutionTime("Cancel Task", startTime, endTime);
        return cloudTask;
    }

    @Override
    public CloudBuild createBuild(UUID packageGuid) {
        Instant startTime = Instant.now();
        CloudBuild cloudBuild = super.createBuild(packageGuid);
        Instant endTime = Instant.now();
        logExecutionTime("Create Build", startTime, endTime);
        return cloudBuild;
    }

    @Override
    public List<CloudBuild> getBuildsForPackage(UUID packageGuid) {
        Instant startTime = Instant.now();
        List<CloudBuild> packageBuilds = super.getBuildsForPackage(packageGuid);
        Instant endTime = Instant.now();
        logExecutionTime("Get Builds for Package", startTime, endTime);
        return packageBuilds;
    }

    @Override
    public CloudBuild getBuild(UUID buildGuid) {
        Instant startTime = Instant.now();
        CloudBuild build = super.getBuild(buildGuid);
        Instant endTime = Instant.now();
        logExecutionTime("Get Build", startTime, endTime);
        return build;
    }

    @Override
    public void bindDropletToApp(UUID dropletGuid, UUID appGuid) {
        Instant startTime = Instant.now();
        super.bindDropletToApp(dropletGuid, appGuid);
        Instant endTime = Instant.now();
        logExecutionTime("Bind Droplet to Application", startTime, endTime);
    }

    @Override
    public List<CloudBuild> getBuildsForApplication(UUID applicationGuid) {
        Instant startTime = Instant.now();
        List<CloudBuild> applicationBuilds = super.getBuildsForApplication(applicationGuid);
        Instant endTime = Instant.now();
        logExecutionTime("Get Builds for Application", startTime, endTime);
        return applicationBuilds;
    }

    @Override
    public void unbindService(String applicationName, String serviceName,
                              ApplicationServicesUpdateCallback applicationServicesUpdateCallback) {
        Instant startTime = Instant.now();
        super.unbindService(applicationName, serviceName, applicationServicesUpdateCallback);
        Instant endTime = Instant.now();
        logExecutionTime("Unbind Service", startTime, endTime);
    }

    private void logExecutionTime(String operationName, Instant startTime, Instant endTime) {
        long executionTime = Duration.between(startTime, endTime)
                                     .toMillis();
        userMessageLogger.debug("Controller operation \"{0}\" has taken {1} ms", operationName, executionTime);
        operationsPerformanceMonitor.logControllerOperation(userMessageLogger,
                                                                      new CloudControllerPerformanceOperation(operationName,
                                                                                                              executionTime));
    }

    public UserMessageLogger getUserMessageLogger() {
        return userMessageLogger;
    }

}
