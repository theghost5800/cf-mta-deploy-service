package com.sap.cloud.lm.sl.cf.process.steps;

import java.text.MessageFormat;

import com.sap.cloud.lm.sl.cf.process.Constants;
import com.sap.cloud.lm.sl.cf.process.Messages;
import com.sap.cloud.lm.sl.cf.process.variables.Variables;
import com.sap.cloud.lm.sl.common.SLException;

public abstract class TimeoutAsyncFlowableStep extends AsyncFlowableStep {

    @Override
    public StepPhase executeStep(ProcessContext context) throws Exception {
        boolean hasTimedOut = hasTimedOut(context);
        if (hasTimedOut) {
            throw new SLException(MessageFormat.format(Messages.EXECUTION_OF_STEP_HAS_TIMED_OUT, getStepName()));
        }
        return super.executeStep(context);
    }

    private boolean hasTimedOut(ProcessContext context) {
        long stepStartTime = getStepStartTime(context);
        long currentTime = System.currentTimeMillis();
        return (currentTime - stepStartTime) >= getTimeout(context) * 1000;
    }

    private long getStepStartTime(ProcessContext context) {
        Long stepStartTime = (Long) context.getExecution()
                                           .getVariable(getStepStartTimeVariable());
        if (stepStartTime == null || isInRetry(context)) {
            stepStartTime = System.currentTimeMillis();
            context.getExecution()
                   .setVariable(getStepStartTimeVariable(), stepStartTime);
        }

        return stepStartTime;
    }

    private boolean isInRetry(ProcessContext context) {
        return context.getVariable(Variables.STEP_PHASE) == StepPhase.RETRY;
    }

    private String getStepStartTimeVariable() {
        return Constants.VAR_STEP_START_TIME + getStepName();
    }

    private String getStepName() {
        return getClass().getSimpleName();
    }

    public abstract Integer getTimeout(ProcessContext context);
}
