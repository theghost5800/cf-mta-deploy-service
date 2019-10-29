package com.sap.cloud.lm.sl.cf.core.performance;

public class CloudControllerPerformanceOperation implements PerformanceOperation {

    private String operationName;
    private long executionTime;

    public CloudControllerPerformanceOperation(String operationName, long executionTime) {
        this.operationName = operationName;
        this.executionTime = executionTime;
    }

    @Override
    public String getOperationName() {
        return operationName;
    }

    @Override
    public long getExecutionTime() {
        return executionTime;
    }

    @Override
    public String toString() {
        return "CloudControllerPerformanceOperation [operationName=" + operationName + ", executionTime=" + executionTime + "]";
    }

}
