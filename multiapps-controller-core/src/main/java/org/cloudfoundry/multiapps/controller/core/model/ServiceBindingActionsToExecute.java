package org.cloudfoundry.multiapps.controller.core.model;

import java.io.Serializable;
import java.util.Collections;
import java.util.Map;

import org.immutables.value.Value;

@Value.Immutable
public abstract class ServiceBindingActionsToExecute implements Serializable {

    private static final long serialVersionUID = 1L;

    public abstract String getName();

    public abstract boolean shouldBind();

    public abstract boolean shouldUnbind();

    @Value.Default
    public Map<String, Object> getBindingParameters() {
        return Collections.emptyMap();
    }
}
