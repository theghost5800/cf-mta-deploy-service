package com.sap.cloud.lm.sl.cf.process.steps;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.util.Collections;

import org.cloudfoundry.client.lib.CloudControllerClient;
import org.flowable.engine.ManagementService;
import org.flowable.engine.ProcessEngineConfiguration;
import org.flowable.engine.RuntimeService;
import org.flowable.engine.delegate.DelegateExecution;
import org.flowable.engine.runtime.Execution;
import org.flowable.engine.runtime.ExecutionQuery;
import org.flowable.job.api.DeadLetterJobQuery;
import org.junit.Before;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sap.cloud.lm.sl.cf.core.cf.CloudControllerClientProvider;
import com.sap.cloud.lm.sl.cf.core.persistence.service.ProgressMessageService;
import com.sap.cloud.lm.sl.cf.core.util.ApplicationConfiguration;
import com.sap.cloud.lm.sl.cf.persistence.services.FileService;
import com.sap.cloud.lm.sl.cf.persistence.services.ProcessLoggerProvider;
import com.sap.cloud.lm.sl.cf.persistence.services.ProcessLogsPersistenceService;
import com.sap.cloud.lm.sl.cf.persistence.services.ProcessLogsPersister;
import com.sap.cloud.lm.sl.cf.process.flowable.FlowableFacade;
import com.sap.cloud.lm.sl.cf.process.mock.MockDelegateExecution;
import com.sap.cloud.lm.sl.cf.process.util.StepLogger;
import com.sap.cloud.lm.sl.cf.process.variables.Variables;
import com.sap.cloud.lm.sl.common.util.Tester;
import com.sap.cloud.lm.sl.mta.model.Module;

public abstract class SyncFlowableStepTest<T extends SyncFlowableStep> {

    private static final Logger LOGGER = LoggerFactory.getLogger(SyncFlowableStepTest.class);

    protected static final String USER_NAME = "dummy";
    protected static final String ORG_NAME = "org";
    protected static final String SPACE_NAME = "space";
    protected static final String SPACE_GUID = "spaceGuid";
    protected final String TEST_CORRELATION_ID = "test";
    protected final String TEST_TASK_ID = "testTask";

    protected final Tester tester = Tester.forClass(getClass());

    protected final DelegateExecution execution = MockDelegateExecution.createSpyInstance();
    @Mock
    protected StepLogger.Factory stepLoggerFactory;
    protected StepLogger stepLogger;
    @Mock
    protected ProcessLogsPersistenceService processLogsPersistenceService;
    @Mock
    protected ProgressMessageService progressMessageService;
    @Mock
    protected FileService fileService;
    @Mock
    protected CloudControllerClient client;
    @Mock
    protected CloudControllerClientProvider clientProvider;
    @Mock
    protected FlowableFacade flowableFacadeFacade;
    @Mock
    protected ApplicationConfiguration configuration;
    @Mock
    protected ProcessEngineConfiguration processEngineConfiguration;
    protected final ProcessLoggerProvider processLoggerProvider = Mockito.spy(ProcessLoggerProvider.class);
    @InjectMocks
    protected ProcessLogsPersister processLogsPersister = Mockito.spy(ProcessLogsPersister.class);

    protected ProcessContext context;
    @InjectMocks
    protected T step = createStep();

    protected abstract T createStep();

    @Before
    @BeforeEach
    public void initMocks() {
        MockitoAnnotations.initMocks(this);
        this.stepLogger = Mockito.spy(new StepLogger(execution, progressMessageService, processLoggerProvider, LOGGER));
        this.context = step.createProcessContext(execution);
        when(stepLoggerFactory.create(any(), any(), any(), any())).thenReturn(stepLogger);
        context.setVariable(Variables.SPACE_NAME, SPACE_NAME);
        context.setVariable(Variables.SPACE_GUID, SPACE_GUID);
        context.setVariable(Variables.USER, USER_NAME);
        context.setVariable(Variables.ORGANIZATION_NAME, ORG_NAME);
        when(clientProvider.getControllerClient(any(), any(), any(), any())).thenReturn(client);
        when(clientProvider.getControllerClient(any(), any())).thenReturn(client);
        execution.setVariable("correlationId", getCorrelationId());
        execution.setVariable("__TASK_ID", getTaskId());
        prepareProcessEngineConfiguration();
        context.setVariable(Variables.MODULE_TO_DEPLOY, Module.createV3()
                                                              .setName("testModule"));
    }

    private void prepareProcessEngineConfiguration() {
        ExecutionQuery mockExecutionQuery = createExecutionQueryMock();
        mockExecutionQuery(mockExecutionQuery);
        DeadLetterJobQuery mockDeadLetterJobQuery = createDeadLetterJobQueryMock();
        mockManagementService(mockDeadLetterJobQuery);
    }

    private void mockManagementService(DeadLetterJobQuery mockDeadLetterJobQuery) {
        ManagementService mockManagementService = Mockito.mock(ManagementService.class);
        when(mockManagementService.createDeadLetterJobQuery()).thenReturn(mockDeadLetterJobQuery);
        when(processEngineConfiguration.getManagementService()).thenReturn(mockManagementService);
    }

    private DeadLetterJobQuery createDeadLetterJobQueryMock() {
        DeadLetterJobQuery mockDeadLetterJobQuery = Mockito.mock(DeadLetterJobQuery.class);
        when(mockDeadLetterJobQuery.processInstanceId(Mockito.anyString())).thenReturn(mockDeadLetterJobQuery);
        when(mockDeadLetterJobQuery.list()).thenReturn(Collections.emptyList());
        return mockDeadLetterJobQuery;
    }

    private void mockExecutionQuery(ExecutionQuery mockExecutionQuery) {
        Execution mockExecution = Mockito.mock(Execution.class);
        when(mockExecution.getActivityId()).thenReturn("1");
        when(mockExecutionQuery.list()).thenReturn(Collections.singletonList(mockExecution));
        when(mockExecutionQuery.processInstanceId(Mockito.anyString())).thenReturn(mockExecutionQuery);
    }

    private ExecutionQuery createExecutionQueryMock() {
        RuntimeService mockRuntimeService = Mockito.mock(RuntimeService.class);
        when(processEngineConfiguration.getRuntimeService()).thenReturn(mockRuntimeService);
        ExecutionQuery mockExecutionQuery = Mockito.mock(ExecutionQuery.class);
        when(mockRuntimeService.createExecutionQuery()).thenReturn(mockExecutionQuery);
        return mockExecutionQuery;
    }

    protected void assertStepFinishedSuccessfully() {
        assertEquals(StepPhase.DONE.toString(), getExecutionStatus());
    }

    protected String getExecutionStatus() {
        return (String) execution.getVariable("StepExecution");
    }

    protected String getCorrelationId() {
        return TEST_CORRELATION_ID;
    }

    private String getTaskId() {
        return TEST_TASK_ID;
    }

}
