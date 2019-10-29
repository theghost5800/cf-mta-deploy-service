package com.sap.cloud.lm.sl.cf.core.cf;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.inject.Named;

import org.apache.commons.collections4.map.AbstractReferenceMap.ReferenceStrength;
import org.apache.commons.collections4.map.ReferenceMap;

import com.sap.cloud.lm.sl.cf.core.performance.CloudControllerPerformanceOperation;
import com.sap.cloud.lm.sl.cf.core.performance.PerformanceOperation;
import com.sap.cloud.lm.sl.cf.core.util.UserMessageLogger;

@Named
public class OperationsPerformanceMonitor {

    private final Map<UserMessageLogger, List<PerformanceOperation>> operationsPerformanceMap = Collections.synchronizedMap(new ReferenceMap<>(ReferenceStrength.HARD,
                                                                                                                                               ReferenceStrength.SOFT));

    public void logControllerOperation(UserMessageLogger userMessageLogger, PerformanceOperation performanceOperation) {
        List<PerformanceOperation> operations = operationsPerformanceMap.getOrDefault(userMessageLogger, new ArrayList<>());
        addOperation(operations, performanceOperation);
        operationsPerformanceMap.put(userMessageLogger, operations);
    }

    private void addOperation(List<PerformanceOperation> operations, PerformanceOperation performanceOperation) {
        operations.add(performanceOperation);
    }

    public long getTotalControllerOperationsTime(UserMessageLogger userMessageLogger) {
        List<PerformanceOperation> operations = operationsPerformanceMap.get(userMessageLogger);
        if (operations == null) {
            return 0L;
        }
        return operations.stream()
                         .filter(CloudControllerPerformanceOperation.class::isInstance)
                         .mapToLong(PerformanceOperation::getExecutionTime)
                         .sum();
    }

    public void deletePerformanceDate(UserMessageLogger userMessageLogger) {
        System.out.println(MessageFormat.format("It will be deleted info for {0}, current map state {1}", userMessageLogger,
                                                operationsPerformanceMap));
        operationsPerformanceMap.remove(userMessageLogger);
    }
}
